package com.golaxy.converter.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golaxy.converter.entity.es.ESSetData;
import com.golaxy.converter.entity.frontend.ConverterResult;
import com.golaxy.converter.service.es.IEsService;
import com.golaxy.converter.service.gitlab.IGitlabService;
import com.golaxy.converter.service.kafka.IKafkaService;
import com.golaxy.converter.utils.CommonUtils;
import com.golaxy.converter.utils.ContextUtil;
import com.golaxy.converter.utils.JackJsonUtils;
import org.apache.ibatis.javassist.NotFoundException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * md提交ES线程
 */
public class MdIndex implements Runnable {

    public static MdIndex instance;
    public static Thread instanceThread;
    private final static Logger logger = LoggerFactory.getLogger(MdIndex.class);
    private static String topic = "mdIndex";

    @Override
    public void run() {
        IKafkaService kafkaService = null;
        IEsService esService = null;

        do {
            // 等待kafkaService服务注入成功可以被调用
            try {
                if (kafkaService == null)
                    kafkaService = (IKafkaService) ContextUtil.getBean("kafkaService");
                if (esService == null)
                    esService = (IEsService) ContextUtil.getBean("esService");
            } catch (NullPointerException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        } while (kafkaService==null || esService==null);

        KafkaConsumer<String, String> consumer = kafkaService.kafkaConsumerInit(topic);

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                String articleUid = record.key();
                try {
                    boolean indexStatus = MdSave.getArticleIndexStatus(articleUid);
                    if (indexStatus)
                        continue;
                } catch (NotFoundException e) {
                    logger.info(articleUid + " | " + e.getMessage());
                    continue;
                }

                List<ConverterResult> mdList = JackJsonUtils.fromJson(record.value(), new TypeReference<List<ConverterResult>>(){});
                Iterator<ConverterResult> it = mdList.iterator();
                while (it.hasNext()) {
                    ConverterResult md = it.next();
                    try {
                        ESSetData esSetData = new ESSetData();
                        esSetData.setArticle_uid(articleUid);
                        esSetData.setTitle(CommonUtils.getFileNameNoExt(md.getName()));
                        esSetData.setContent(CommonUtils.read(md.getAbsolutePath(), "UTF-8"));
                        esSetData.setPath(md.getGitPath().replace("keepwork/baike", ""));
                        esSetData.setPage(md.getPage());
                        esSetData.setTotalpage(mdList.size());
                        esSetData.setPublic_status(true);
                        esSetData.setSource(md.getUploadUserSourceId());
                        esSetData.setAuthor("");
                        esSetData.setPublish_time("");

                        String mdUid = esService.esIndex(esSetData);
                        if (mdUid != null)
                            MdSave.mysqlUpdateEsId(md.getMdId(), mdUid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                MdSave.mysqlUpdateEsIndexStatus(articleUid,true);
                logger.info(articleUid + " | 索引成功");
            }
        }
    }

    public static synchronized MdIndex getInstance() {
        if (instance == null) {
            instance = new MdIndex();
            instanceThreadStart(instance);
        }
        return instance;
    }

    public static synchronized void instanceThreadStart(MdIndex instance) {
        if (instanceThread == null) {
            instanceThread = new Thread(instance);
            instanceThread.setName("mdIndex");
            instanceThread.start();
        }
    }

}
