package com.golaxy.converter.service.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;


public interface IKafkaService {

	public KafkaConsumer<String, String> kafkaConsumerInit(String topic);

	public boolean kafkaProduce(String uid, String fileMd5, String userName, String uploadFileName, String userSource, int cateId) throws Exception;
}
