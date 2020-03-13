package com.study.baseusage;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class Consumer extends Thread {

	private final KafkaConsumer<Integer, String> consumer;
	private String topic;

	public Consumer(String topic) {
		Properties properties = new Properties();

		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Producer.BOOT_STRAP_SERVER);
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, "demo-consumer");
		// �Զ��ύ
		properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
		// �Զ��ύ���
		properties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");

		properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000");
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		this.consumer = new KafkaConsumer<Integer, String>(properties);
//		consumer.assign(partitions);
		this.topic = topic;

	}

	@Override
	public void run() {
		consumer.subscribe(Collections.singleton(this.topic));
		while (!stop.get()) {
			ConsumerRecords<Integer, String> dataRecords = consumer.poll(Duration.ofMillis(1000));
			dataRecords.forEach(record->{
				System.out.println(record.key()+","+record.value()+" =>"+record.offset());
			});
		}
	}

	private AtomicBoolean stop = new AtomicBoolean(false);

	public boolean close() {
		return stop.compareAndSet(false, true);
	}
}
