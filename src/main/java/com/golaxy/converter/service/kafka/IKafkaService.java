package com.golaxy.converter.service.kafka;

import com.golaxy.converter.entity.frontend.ConverterResult;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.List;
import java.util.Map;


public interface IKafkaService {

	public KafkaConsumer<String, String> kafkaConsumerInit(String topic);

	public boolean kafkaProduce(String uid, String fileMd5, String userName, String uploadFileName, String userSource, int cateId) throws Exception;

	public boolean kafkaProduceGitlabUploadImg(String fileMd5, Map<String, List<ConverterResult>> result) throws Exception;

    public boolean kafkaProduceIndexMd(String articleUid, List<ConverterResult> mdList) throws Exception;

}
