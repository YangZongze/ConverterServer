package com.golaxy.converter.service.kafka.impl;

import com.golaxy.converter.convert.GlobalVars;
import com.golaxy.converter.entity.frontend.ConverterResult;
import com.golaxy.converter.entity.frontend.ResponseResult;
import com.golaxy.converter.entity.frontend.StatusCode;
import com.golaxy.converter.entity.kafka.KafkaUserFile;
import com.golaxy.converter.service.kafka.IKafkaService;
import com.golaxy.converter.utils.JackJsonUtils;
import com.golaxy.converter.websocket.SessionHandler;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

@Service("kafkaService")
public class KafkaServiceImpl implements IKafkaService {

    private final static Logger logger = LoggerFactory.getLogger(KafkaServiceImpl.class);
    private static Producer<String, String> producer;

    static {
        producer = kafkaProducerInit();
    }

	public static Producer<String, String> kafkaProducerInit() {
		Properties props = new Properties();
		props.put("bootstrap.servers", GlobalVars.kafkaServer);
    	props.put("acks", "all");
    	props.put("retries", 0);
    	props.put("batch.size", 16384);
    	props.put("linger.ms", 1);
    	props.put("buffer.memory", 33554432);
    	props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    	props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    	
    	return new KafkaProducer<>(props);
	}

	@Override
	public KafkaConsumer<String, String> kafkaConsumerInit(String topic) {
		Properties props = new Properties();
	    props.put("bootstrap.servers", GlobalVars.kafkaServer);
	    props.put("group.id", "dafu");
	    props.put("enable.auto.commit", "true");
	    props.put("auto.commit.interval.ms", "1000");
	    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

	    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
	    consumer.subscribe(Arrays.asList(topic));
	    
	    return consumer;
	}

    /**
     * 提交kafka队列
     *
     * @param uid
     * @param userName
     * @param uploadFileName
     * @param userSource
     * @param cateId
     */
    @Override
    public boolean kafkaProduce(String uid, String fileMd5, String userName, String uploadFileName,
                                String userSource, int cateId) throws Exception {

		/* 判断文件类型，放入相应队列
		 * doc/docx/ppt/pptx 队列
		 * pdf/html/txt 队列
		 * pdf_ocr队列
		 */
        String fileType = GlobalVars.judgeFileType(uploadFileName);
        String topic = GlobalVars.getKafkaTopic(fileType);

        KafkaUserFile userFile = new KafkaUserFile();
        userFile.setFileMd5(fileMd5);
        userFile.setUserName(userName);
        userFile.setFileName(uploadFileName);
        userFile.setUserSource(userSource);
        userFile.setCateId(cateId);

        Future<RecordMetadata> future = producer.send(new ProducerRecord<String, String>(topic, uid, JackJsonUtils.toJson(userFile)),
            new Callback() {
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    if (e != null) {
                        logger.error("[Kafka]: <格式转换> 写入失败 | md5: "+fileMd5+" | uid: " + uid
                                +" | 文件名:" + userFile.getFileName() +" | errMsg: "+e.getMessage());
                        SessionHandler.closeSession(uid);
                    } else {
                        logger.info("[Kafka]: <格式转换> 写入成功 | offset: " + metadata.offset()
                                + " | md5: "+fileMd5+" | uid: " + uid + " | 文件名:" + userFile.getFileName());
                        // offset写入session
                        WebSocketSession session = SessionHandler.addAttr(uid, "offset", String.valueOf(metadata.offset()));
                        // 把该session存入该topic对应的session组中
                        GlobalVars.put(topic, uid, session);
                        // 刚写入队列的文档与改队列中正在换砖的文档间的队列长度
                        long waitQueueNum = metadata.offset() - GlobalVars.getCurrProcessOffset(topic);

                        ResponseResult res = new ResponseResult();
                        res.setCode(StatusCode.WEBSOCKET_CONVERT_QUEUE);
                        res.setUid(uid);
                        res.setWaitqueue((int) waitQueueNum);

                        SessionHandler.sendMessage(session, JackJsonUtils.toJson(res));
                    }
                }
            });

        int offset = -1;
        boolean flag = true;
        while(flag){
            if (future.isDone()) {
                flag = false;
                if (!future.isCancelled()) {
                    RecordMetadata metadata = future.get();
                    offset = (int) metadata.offset();
                }
            }
        }

        return offset>0 ? true : false;
    }

    /**
     * 提交kafka上传图片到gitlab队列
     *
     * @param fileMd5
     * @param result
     */
    @Override
    public boolean kafkaProduceGitlabUploadImg(String fileMd5, Map<String, List<ConverterResult>> result) throws Exception {

        String topic = "imgUpload";

        Future<RecordMetadata> future = producer.send(new ProducerRecord<String, String>(topic, fileMd5, JackJsonUtils.toJson(result)),
                new Callback() {
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        if (e != null) {
                            logger.info("[Kafka]: <图片提交gitlab> 写入失败 | errMsg: " + e.getMessage());
                        } else {
                            logger.info("[Kafka]: <图片提交gitlab> 写入成功 | offset: " + metadata.offset() + " | md5: " + fileMd5);
                        }
                    }
                });

        int offset = -1;
        boolean flag = true;
        while(flag){
            if (future.isDone()) {
                flag = false;
                if (!future.isCancelled()) {
                    RecordMetadata metadata = future.get();
                    offset = (int) metadata.offset();
                }
            }
        }

        return offset>0 ? true : false;
    }

    /**
     * 提交kafka md索引 队列
     *
     * @param articleUid
     * @param mdList
     */
    @Override
    public boolean kafkaProduceIndexMd(String articleUid, List<ConverterResult> mdList) throws Exception {

        String topic = "mdIndex";

        Future<RecordMetadata> future = producer.send(new ProducerRecord<String, String>(topic, articleUid, JackJsonUtils.toJson(mdList)),
                new Callback() {
                    public void onCompletion(RecordMetadata metadata, Exception e) {
                        if (e != null) {
                            logger.error("[Kafka]: <md提交索引> 写入失败: "+ e.getMessage());
                        } else {
                            logger.info("[Kafka]: <md提交索引> 写入成功  | offset:" + metadata.offset() + " | uid: " + articleUid);
                        }
                    }
                });

        int offset = -1;
        boolean flag = true;
        while(flag){
            if (future.isDone()) {
                flag = false;
                if (!future.isCancelled()) {
                    RecordMetadata metadata = future.get();
                    offset = (int) metadata.offset();
                }
            }
        }

        return offset>0 ? true : false;
    }

}
