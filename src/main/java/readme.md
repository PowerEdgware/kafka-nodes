
##kafka 学习笔记
> 基于linux centos7。jdk版本：java version "1.8.0_221"

####第一部分  基本使用、安装、集群配置等

**下载解压即可**  
> 版本：kafka_2.11-2.0.0 

**配置修改**
../config/server.properties
 * 修改kafkaServer监听ip和端口为：listeners=PLAINTEXT://192.168.132.129:9092  
 * 否则外部无法连接另外需要关闭防火墙或者开放 kafka 9092端口


**防火墙的一些操作**  
* 开放 9092 `firewall-cmd --zone=public --add-port=9092/tcp --permanent` 
* 重启防火墙：`firewall-cmd --reload`  
* 关闭防火墙：`systemctl stop firewalld.service`
* 查看防火墙状态：`firewall-cmd --state` or `systemctl status firewalld.service`
* 禁止firewall开机启动：`systemctl disable firewalld.service`
*  查询有哪些端口是开启的：`firewall-cmd --list-port`
* 查询指定端口是否开启：`firewall-cmd --query-port=port/tcp`

**集群配置**  
新增两个配置：  
`cp config/server.properties config/server-1.properties`  
`cp config/server.properties config/server-2.properties`

broker.id配置：  
* server.id=1  
* server.id=2  

端口修改：  
* listeners=PLAINTEXT://ip:9093  
* listeners=PLAINTEXT://ip:9094

数据目录配置：  
* log.dirs=/tmp/kafka-logs-1  
* log.dirs=/tmp/kafka-logs-2

启动：  
* bin/kafka-server-start.sh -daemon config/server-1.properties  
* bin/kafka-server-start.sh -daemon config/server-2.properties


#### 第二部分   kafka基本原理

使用java客户端连接kafka:  

```
<dependency>    
 <groupId>org.apache.kafka</groupId>   
 <artifactId>kafka-clients</artifactId>    
 <version>2.0.0</version> 
</dependency>
```

**基础概念配置**  
`group.id`  
每个消费者都属于一个消费者组，一个组内可以有多个消费者实例。组内的多个消费者可以订阅主题topic，实现分区消费。  
每个主题的分区只能有消费者组内某个消费者消费。主题类似与queue的概念，不同的组内消费者可以消费同一个主题。  

`enable.auto.commit`  
消费者消费消息后自动提交，提交后的消息不会再次收到。可配合`auto.commit.interval.ms`来控制自动提交的频率。
当然上述值为false时消费者可以设置手动提交。  

`auto.offset.reset `  
该参数是针对新的groupid组内的消费者消费消息而言的，对新的消费者组的消费者加入消费topic时，指定消费策略将会从该topic的不同位置进行消息消费。  
`auto.offset.reset=latest`：新的消费者将会从其他消费者最后消费的offset处开始消费topic下的消息。  
`auto.offset.reset= earliest`:新的消费者会从该topic最早的消息开始消费。  
`auto.offset.reset=none`：新的消费者加入后，不存在之前的offset，那么直接抛出异常。  


`max.poll.records `  
消费者每次拉去消息的条数，控制消费能力。  


** Topic and Partition**  

每个topic可以有多个分区，每个topic可以有多个生产者发送消息，也可以有多个消费者消费消息。  
分区：  每个topic可以分成多个分区，同一个topic的不同分区存储的消息是不同的，相当于topic做分片。  
分区被物理的分到kafka节点上。同一个分区内消费根据offset做记录，每个分区独立，同一个分区内消息是有序的。  

分区是以物理文件的形式存储在文件系统中，比如创建topic=demo-topic并指定三个分区。  

`sh kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 3 --topic firstTopic`

会在kafka数据目录下生成三个目录：demo-topic0-2，命名规则：`<topic_name>-<partition_id> `   

** Kafka消息分发策略**  

kafka中发送到其中的每条消息都是有：key-value组成，生产者发送消息时可以指定key也可以不指定。生产者根据key和分区规则来决定
消息发送到哪个分区中。还可以通过实现:`org.apache.kafka.clients.producer.Partitioner`接口，利用key实现消息发送到
指定的分区中。  

消息默认分发策略：hash取模算法，如果key为null，则随机分配一个分区，每个随机的分区都有一个时间限定，在这个限定时间内，key为Null
的消息都会发送到这个固定的分区，可以通过`metadata.max.age.ms`设置更新频率，默认十分钟更新一次。  

** 消息的消费原理**  

* 消费者消费指定的分区  
//消费指定的分区 

```
TopicPartition topicPartition=new TopicPartition(topic,0); 
kafkaConsumer.assign(Arrays.asList(topicPartition));
```

每个topic可以指定多个分区，实现broker上消息的分片存储，避免单文件过大，减少单个文件消息容量提升IO性能。  
在一个topic有多个分区时，多个消费者如何消费消费分区的数据呢？ 这涉及到消费者负载均衡的问题。  

同一个消费者组(group.id)内的多个消费者协调起来共同消费所订阅主题的所有分区，每一个分区只能有同一个消费者组内的某一个消费者  
来消费。

消费者消费分区的规则：(分区分配策略`Partition Assignment Strategy`)
接口：`PartitionAssignor`    
 
同一个组内同一个toic下分区多于消费者；分区少于消费者；分区等于消费者数量。  

*分区分配策略：*    
> 同一个组内同一个topic的多个分区，多个消费者如何消费分区的问题就是分区分欸策略。

* Range分区(默认)：  

1,首先对同一个topic多个partitions按序号排序，并对消费者按字母顺序排序；    
2,分区数/消费者数得到结果n;
3,分区数%消费者数量得到模数m;  
消费者数：cc
结果：前m个消费者每个得到n+1个分区，后面(cc-m)个消费者每个分配n个。  
不同的topic分区都是这样分配。  

缺点： 多个topic时，靠前的消费者总是分到较多的分区导致分区分配不均衡。  


* RoundRibbon(轮询分区)  

对消费者和分区按照hashCode进行排序，通过轮询算法给消费者分配partitions。如果consumer订阅是相同的，那么partitions会均匀分配。

使用轮询需要满足两个条件：  
1,每个主题的消费者具有相同数据的流  
2,每个消费者订阅的主题必须相同。  

* StrickyAssignor粘滞策略    
分配的分区尽可能均匀；  
分区的分配尽可能和上次相同。  
均匀优先于相同。  

好处，分区尽可能保持不变，减少分区移动。  

*重新分配策略触发的时机*  
1，同一个consumer组内新增了消费者。  
2，消费者组内消费者退出。  
3，topic分区发送变化。  

rebalance机制确保了同一个消费者组内的消费者合理分配订阅的topic的多个分区，达到消息均匀消费。  












