package com.study.springboot;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BootMain {

	void readme() {
		//KafkaProperties
		//Partitioner 
		//RoundRobinAssignor
		//BootStrap
	}
	
	public static void main(String[] args) {
		ConfigurableApplicationContext ctx=SpringApplication.run(BootMain.class, args);
		KafkaProducer kafkaProducer=ctx.getBean(KafkaProducer.class);
		int x=100;
		String topic="test-topic";
		while(x>0) {
			kafkaProducer.send("demo"+x, topic);
			--x;
		}
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ctx.close();
	}
}
