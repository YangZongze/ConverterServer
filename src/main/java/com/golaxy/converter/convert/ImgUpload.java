package com.golaxy.converter.convert;

import com.golaxy.converter.entity.frontend.ConverterResult;
import com.golaxy.converter.service.gitlab.IGitlabService;
import com.golaxy.converter.service.kafka.IKafkaService;
import com.golaxy.converter.utils.CommonUtils;
import com.golaxy.converter.utils.ContextUtil;
import com.golaxy.converter.utils.JackJsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 图片提交gitlab线程
 */
public class ImgUpload implements Runnable {

    public static ImgUpload instance;
    public static Thread instanceThread;
    private final static Logger logger = LoggerFactory.getLogger(ImgUpload.class);
    private static String topic = "imgUpload";

    @Override
    public void run() {
        IKafkaService kafkaService = null;
        IGitlabService gitlabService = null;
        do {
            // 等待kafkaService服务注入成功可以被调用
            try {
                if (kafkaService == null)
                    kafkaService = (IKafkaService) ContextUtil.getBean("kafkaService");
                if (gitlabService == null)
                    gitlabService = (IGitlabService) ContextUtil.getBean("gitlabService");
            } catch (NullPointerException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        } while (kafkaService==null || gitlabService==null);

        KafkaConsumer<String, String> consumer = kafkaService.kafkaConsumerInit(topic);

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {

                Map<String, List<ConverterResult>> result = JackJsonUtils.fromJson(record.value(), new TypeReference<Map<String, List<ConverterResult>>>(){});
                List<ConverterResult> imgList = result.get("img");
                List<ConverterResult> mdList = result.get("md");

                Map<String, String> imgUrlMap = new HashMap<>();
                for (ConverterResult img: imgList) {
                    String imgUrl = img.getUrl();
                    String imgName = img.getName();
                    String imgPath = img.getAbsolutePath();
                    String imgGitPath = gitlabService.gitlabSaveImg(imgName, imgPath);
                    String imgGitUrl = GlobalVars.gitlabRawBaseUrl + imgGitPath;

                    imgUrlMap.put(imgUrl, imgGitUrl);
                }

                for(ConverterResult md: mdList) {
                    String mdLocalPath = md.getAbsolutePath();
                    String mdGitPath = md.getGitPath();

                    // 修改本地md
                    logger.info("修改本地md图片url");
                    try {
                        String mdLocalContent = CommonUtils.read(mdLocalPath, "UTF-8");

                        for (String imgLocalUrl : imgUrlMap.keySet()) {
                            String imgGitUrl = imgUrlMap.get(imgLocalUrl);
                            mdLocalContent = mdLocalContent.replace(imgLocalUrl, imgGitUrl);
                            CommonUtils.write(mdLocalPath, mdLocalContent, "UTF-8");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 修改gitlab上md
                    logger.info("修改gitlab md图片url");
                    try {
                        String mdGitContent = gitlabService.gitlabGet(mdGitPath);
                        for (String imgLocalUrl : imgUrlMap.keySet()) {
                            String imgGitUrl = imgUrlMap.get(imgLocalUrl);
                            mdGitContent = mdGitContent.replace(imgLocalUrl, imgGitUrl);
                            gitlabService.gitlabSaveContent(mdGitPath, mdGitContent,"后台", true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    public static synchronized ImgUpload getInstance() {
        if (instance == null) {
            instance = new ImgUpload();
            instanceThreadStart(instance);
        }
        return instance;
    }

    public static synchronized void instanceThreadStart(ImgUpload instance) {
        if (instanceThread == null) {
            instanceThread = new Thread(instance);
            instanceThread.setName("ImgUpload");
            instanceThread.start();
        }
    }

}
