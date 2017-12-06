package com.golaxy.converter.convert;

import java.lang.Thread;
import java.util.Map.Entry;

import com.golaxy.converter.entity.kafka.TopicInfo;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by yangzongze on 2017/12/1.
 *
 * 启动线程
 */
@Component
public class DocProcess implements ApplicationContextAware {

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		// 先启动监控线程
		ThreadMonitor monitor = ThreadMonitor.getInstance();

		for (Entry<String, TopicInfo> entry : GlobalVars.topics.entrySet()) {
			String topic = entry.getKey();
			String consumerName = entry.getValue().getConsumerName();
			int consumerNum = entry.getValue().getConsumerThreadNum();

			RecordBuffer recordBuffer = new RecordBuffer();

			RecordBufferProducer producer = new RecordBufferProducer(recordBuffer, topic);
			RecordBufferConsumer consumer = new RecordBufferConsumer(recordBuffer);

			Thread proThread = new Thread(producer);
			proThread.setName("KafKaReader_"+topic);
			proThread.start();
			monitor.add(proThread, producer);

			for (int i=1; i<=consumerNum; i++) {
				Thread conThread = new Thread(consumer);
				conThread.setName(consumerName+i);
				conThread.start();
				monitor.add(conThread, consumer);
			}
		}
	}
}

