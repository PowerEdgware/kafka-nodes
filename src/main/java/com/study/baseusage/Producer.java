package com.study.baseusage;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

/**
 * ../config/server.properties
 * 修改kafkaServer监听ip和端口为：listeners=PLAINTEXT://192.168.132.129:9092  
 * 否则外部无法连接另外需要关闭防火墙或者开放 kafka9092端口
 * @author User
 *
 */
public class Producer extends Thread {

	static final String BOOT_STRAP_SERVER="192.168.132.129:9092";
	
	private AtomicBoolean stop=new AtomicBoolean(false);
	final KafkaProducer<Integer, String> producer;
	final String topic;

	public Producer(String topic) {
		Properties properties = new Properties();
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOT_STRAP_SERVER);
		properties.put(ProducerConfig.CLIENT_ID_CONFIG, "demo-producer");
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

		producer = new KafkaProducer<Integer, String>(properties);
		this.topic = topic;
	}

	@Override
	public void run() {
		int num = 0;
		String msg = "producer msg:";
		while (!stop.get()) {
			Future<RecordMetadata> future = producer.send(new ProducerRecord<Integer, String>(topic, msg + num));
			RecordMetadata recordMetadata=null;
			try {
				recordMetadata = future.get(5, TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
			}
			if (recordMetadata != null) {
				System.out.println(recordMetadata+" for num="+num);
			}
			num++;
			
			LockSupport.parkNanos(this, TimeUnit.MILLISECONDS.toNanos(2000));
		}
	}

	public boolean close() {
		return stop.compareAndSet(false, true);
	}
}
