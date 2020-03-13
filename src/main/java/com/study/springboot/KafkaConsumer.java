package com.study.springboot;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

	@KafkaListener(topics = "${demo.topic}")
	public void recvMsg(String msg) {
		//ConsumerRecord
		System.out.println(Thread.currentThread().getName() + " recv:" + msg);
	}
}
