package com.study.springboot;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {

	final KafkaTemplate<String, String> kafkaTemplate;
	public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
		super();
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void send(String msg,String topic) {
		kafkaTemplate.send(topic, null, msg);
	}
}
