package com.golaxy.converter.convert;

import com.golaxy.converter.service.kafka.IKafkaService;
import com.golaxy.converter.utils.ContextUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * kafka读取线程
 */
public class RecordBufferProducer implements Runnable{

	private RecordBuffer recordBuffer;
	private String topic;
	
	public RecordBufferProducer(RecordBuffer recordBuffer, String topic) {
		this.recordBuffer = recordBuffer;
		this.topic = topic;
	}
	
	@Override
	public void run() {
        IKafkaService kafkaService = null;
        do {
            // 等待kafkaService服务注入成功可以被调用
            try {
                kafkaService = (IKafkaService) ContextUtil.getBean("kafkaService");
            } catch (NullPointerException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        } while (kafkaService == null);

		KafkaConsumer<String, String> consumer = kafkaService.kafkaConsumerInit(topic);

		while (true) {
	    	ConsumerRecords<String, String> records = consumer.poll(100);
	        for (ConsumerRecord<String, String> record : records) {
	        	try {
					recordBuffer.put(record);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
	    }
	}	
}
