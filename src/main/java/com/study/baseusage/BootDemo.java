package com.study.baseusage;

import java.io.IOException;

public class BootDemo {

	public static void main(String[] args) {
		String topic = "test";
		Producer producer = new Producer(topic);
		Consumer consumer = new Consumer(topic);
		
		producer.start();
		consumer.start();
		
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		producer.close();
		consumer.close();
	}
}
