# **1.** **消息队列** 

### **1.1.** **MQ** **的相关概念** 

##### **1.1.1.** **什么是MQ** 

```
MQ(message queue)，从字面意思上看，本质是个队列，FIFO 先入先出，只不过队列中存放的内容是
message 而已，还是一种跨进程的通信机制，用于上下游传递消息。在互联网架构中，MQ 是一种非常常
见的上下游“逻辑解耦+物理解耦”的消息通信服务。使用了 MQ 之后，消息发送上游只需要依赖 MQ，不
用依赖其他服务。
```

##### **1.1.2.** **为什么要用MQ及作用**

```
1.流量消峰
举个例子，如果订单系统最多能处理一万次订单，这个处理能力应付正常时段的下单时绰绰有余，正
常时段我们下单一秒后就能返回结果。但是在高峰期，如果有两万次下单操作系统是处理不了的，只能限
制订单超过一万后不允许用户下单。使用消息队列做缓冲，我们可以取消这个限制，把一秒内下的订单分
散成一段时间来处理，这时有些用户可能在下单十几秒后才能收到下单成功的操作，但是比不能下单的体
验要好。
```

![image-20210624144959040](https://gitee.com/lml000/tuc/raw/master/images/image-20210624144959040.png)



```
2.应用解耦
以电商应用为例，应用中有订单系统、库存系统、物流系统、支付系统。用户创建订单后，如果耦合
调用库存系统、物流系统、支付系统，任何一个子系统出了故障，都会造成下单操作异常。当转变成基于
消息队列的方式后，系统间调用的问题会减少很多，比如物流系统因为发生故障，需要几分钟来修复。在
这几分钟的时间里，物流系统要处理的内存被缓存在消息队列中，用户的下单操作可以正常完成。当物流
系统恢复后，继续处理订单信息即可，中单用户感受不到物流系统的故障，提升系统的可用性。
```

![image-20210624145213648](https://gitee.com/lml000/tuc/raw/master/images/image-20210624145213648.png)



```
3.异步处理
有些服务间调用是异步的，例如 A 调用 B，B 需要花费很长时间执行，但是 A 需要知道 B 什么时候可
以执行完，以前一般有两种方式，A 过一段时间去调用 B 的查询 api 查询。或者 A 提供一个 callback api， B 执行完之后调用 api 通知 A 服务。这两种方式都不是很优雅，使用消息总线，可以很方便解决这个问题，
A 调用 B 服务后，只需要监听 B 处理完成的消息，当 B 处理完成后，会发送一条消息给 MQ，MQ 会将此消
息转发给 A 服务。这样 A 服务既不用循环调用 B 的查询 api，也不用提供 callback api。同样B 服务也不用
做这些操作。A 服务还能及时的得到异步处理成功的消息。
```

![image-20210624145417318](https://gitee.com/lml000/tuc/raw/master/images/image-20210624145417318.png)



##### **1.1.3.** **MQ 的分类** 

```
1.ActiveMQ
优点：单机吞吐量万级，时效性 ms 级，可用性高，基于主从架构实现高可用性，消息可靠性较
低的概率丢失数据
缺点:官方社区现在对 ActiveMQ 5.x 维护越来越少，高吞吐量场景较少使用。

2.Kafka
 大数据的杀手锏，谈到大数据领域内的消息传输，则绕不开 Kafka，这款为大数据而生的消息中间件，
以其百万级 TPS 的吞吐量名声大噪，迅速成为大数据领域的宠儿，在数据采集、传输、存储的过程中发挥
着举足轻重的作用。目前已经被 LinkedIn，Uber, Twitter, Netflix 等大公司所采纳。
 优点: 性能卓越，单机写入 TPS 约在百万条/秒，最大的优点，就是吞吐量高。时效性 ms 级可用性非
常高，kafka 是分布式的，一个数据多个副本，少数机器宕机，不会丢失数据，不会导致不可用,消费者采
用 Pull 方式获取消息, 消息有序, 通过控制能够保证所有消息被消费且仅被消费一次;有优秀的第三方Kafka
Web 管理界面 Kafka-Manager；在日志领域比较成熟，被多家公司和多个开源项目使用；功能支持： 功能
较为简单，主要支持简单的 MQ 功能，在大数据领域的实时计算以及日志采集被大规模使用
 缺点：Kafka 单机超过 64 个队列/分区，Load 会发生明显的飙高现象，队列越多，load 越高，发送消
息响应时间变长，使用短轮询方式，实时性取决于轮询间隔时间，消费失败不支持重试；支持消息顺序，
但是一台代理宕机后，就会产生消息乱序，社区更新较慢；

3.RocketMQ
 RocketMQ 出自阿里巴巴的开源产品，用 Java 语言实现，在设计时参考了 Kafka，并做出了自己的一
些改进。被阿里巴巴广泛应用在订单，交易，充值，流计算，消息推送，日志流式处理，binglog 分发等场
景。
优点:单机吞吐量十万级,可用性非常高，分布式架构,消息可以做到 0 丢失,MQ 功能较为完善，还是分
布式的，扩展性好,支持 10 亿级别的消息堆积，不会因为堆积导致性能下降,源码是 java 我们可以自己阅
读源码，定制自己公司的 MQ
 缺点：支持的客户端语言不多，目前是 java 及 c++，其中 c++不成熟；社区活跃度一般,没有在MQ
核心中去实现 JMS 等接口,有些系统要迁移需要修改大量代码

4.RabbitMQ
 2007 年发布，是一个在AMQP(高级消息队列协议)基础上完成的，可复用的企业消息系统，是当前最
主流的消息中间件之一。
 优点:由于 erlang 语言的高并发特性，性能较好；吞吐量到万级，MQ 功能比较完备,健壮、稳定、易
用、跨平台、支持多种语言 如：Python、Ruby、.NET、Java、JMS、C、PHP、ActionScript、XMPP、STOMP
等，支持 AJAX 文档齐全；开源提供的管理界面非常棒，用起来很好用,社区活跃度高；更新频率相当高
https://www.rabbitmq.com/news.html
 缺点：商业版需要收费,学习成本较高
```



##### **1.1.4.** **MQ 的选择** 

```
1.Kafka
 Kafka 主要特点是基于Pull 的模式来处理消息消费，追求高吞吐量，一开始的目的就是用于日志收集
和传输，适合产生大量数据的互联网服务的数据收集业务。大型公司建议可以选用，如果有日志采集功能，
肯定是首选 kafka 了。

2.RocketMQ
天生为金融互联网领域而生，对于可靠性要求很高的场景，尤其是电商里面的订单扣款，以及业务削
峰，在大量交易涌入时，后端可能无法及时处理的情况。RoketMQ 在稳定性上可能更值得信赖，这些业务
场景在阿里双 11 已经经历了多次考验，如果你的业务有上述并发场景，建议可以选择 RocketMQ。

3.RabbitMQ
 结合 erlang 语言本身的并发优势，性能好时效性微秒级，社区活跃度也比较高，管理界面用起来十分
方便，如果你的数据量没有那么大，中小型公司优先选择功能比较完备的 RabbitMQ。
```



### **1.2.** **RabbitMQ**

##### **1.2.1.** **RabbitMQ 的概念** 

```
RabbitMQ 是一个消息中间件：它接受并转发消息。你可以把它当做一个快递站点，当你要发送一个包
裹时，你把你的包裹放到快递站，快递员最终会把你的快递送到收件人那里，按照这种逻辑 RabbitMQ 是
一个快递站，一个快递员帮你传递快件。RabbitMQ 与快递站的主要区别在于，它不处理快件而是接收，
存储和转发消息数据。
```



##### **1.2.2.** **四大核心概念** 

```
生产者
产生数据发送消息的程序是生产者

交换机
交换机是 RabbitMQ 非常重要的一个部件，一方面它接收来自生产者的消息，另一方面它将消息
推送到队列中。交换机必须确切知道如何处理它接收到的消息，是将这些消息推送到特定队列还是推
送到多个队列，亦或者是把消息丢弃，这个得有交换机类型决定

队列
队列是 RabbitMQ 内部使用的一种数据结构，尽管消息流经 RabbitMQ 和应用程序，但它们只能存
储在队列中。队列仅受主机的内存和磁盘限制的约束，本质上是一个大的消息缓冲区。许多生产者可
以将消息发送到一个队列，许多消费者可以尝试从一个队列接收数据。这就是我们使用队列的方式

消费者
消费与接收具有相似的含义。消费者大多时候是一个等待接收消息的程序。请注意生产者，消费
者和消息中间件很多时候并不在同一机器上。同一个应用程序既可以是生产者又是可以是消费者。
```

![image-20210624150546420](https://gitee.com/lml000/tuc/raw/master/images/image-20210624150546420.png)



##### **1.2.3.** **RabbitMQ 核心部分** 

```java
--简单模式
--工作模式
--发布/订阅模式
--路由模式
--主题模式
--发布确认模式    
```

![image-20210624150753397](https://gitee.com/lml000/tuc/raw/master/images/image-20210624150753397.png)



##### **1.2.4.** **各个名词介绍** 

![image-20210624150916192](https://gitee.com/lml000/tuc/raw/master/images/image-20210624150916192.png)

```java
Broker：接收和分发消息的应用，RabbitMQ Server 就是 Message Broker
    
Virtual host：出于多租户和安全因素设计的，把 AMQP 的基本组件划分到一个虚拟的分组中，类似
于网络中的 namespace 概念。当多个不同的用户使用同一个 RabbitMQ server 提供的服务时，可以划分出
多个 vhost，每个用户在自己的 vhost 创建 exchange／queue 等
    
Connection：publisher／consumer 和 broker 之间的 TCP 连接
    
Channel：如果每一次访问 RabbitMQ 都建立一个 Connection，在消息量大的时候建立 TCP Connection 的开销将是巨大的，效率也较低。Channel 是在 connection 内部建立的逻辑连接，如果应用程序支持多线程，通常每个 thread 创建单独的 channel 进行通讯，AMQP method 包含了 channel id 帮助客户端和 message broker 识别 channel，所以 channel 之间是完全隔离的。Channel 作为轻量级的Connection 极大减少了操作系统建立 TCP connection 的开销 
    
Exchange：message 到达 broker 的第一站，根据分发规则，匹配查询表中的 routing key，分发
消息到 queue 中去。常用的类型有：direct (point-to-point), topic (publish-subscribe) and fanout
(multicast)
    
Queue：消息最终被送到这里等待 consumer 取走
    
Binding：exchange 和 queue 之间的虚拟连接，binding 中可以包含 routing key，Binding 信息被保
存到 exchange 中的查询表中，用于 message 的分发依据 --Exchange与Queue的连线
```



##### **1.2.5.** **安装**

```shell
systemctl start docker
systemctl enable docker  #设置docker开机自启
docker start 容器id
docker pull rabbitmq:management
docker run -dit --name my-rabbitmq -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin -p 15672:15672 -p 5672:5672 rabbitmq:management

#web 访问地址 ifconfig:15672
# lsof -i:15672  查看指定端口是否开放
--systemctl status firewalld
--systemctl firewalld start(restart stop)
--firewall-cmd --list-ports
--firewall-cmd --zone=public --add-port=80/tcp --permanent
```

```sh
#阿里云镜像加速
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["https://1vbbrszu.mirror.aliyuncs.com"]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```



# **2.** **Work Queues**

```java
生产者大量消息发送给队列->多个工资线程处理 消息只能被处理一次
--轮询
```

![image-20210628090012030](https://gitee.com/lml000/tuc/raw/master/images/image-20210628090012030.png)



### **2.1.** **消息应答**

##### **2.1.1.** **概念** 

```java
	消费者完成一个任务可能需要一段时间，如果其中一个消费者处理一个长的任务并仅只完成了部分突然它挂掉了，会发生什么情况。RabbitMQ 一旦向消费者传递了一条消息，便立即将该消息标记为删除。在这种情况下，突然有个消费者挂掉了，我们将丢失正在处理的消息。以及后续发送给该消费这的消息，因为它无法接收到。
	为了保证消息在发送过程中不丢失，rabbitmq 引入消息应答机制，消息应答就是:消费者在接收到消息并且处理该消息之后，告诉 rabbitmq 它已经处理了，rabbitmq 可以把该消息删除了。
--应该rabbitmq回答后进行删除        
```

##### **2.1.1.** **自动应答** 

```
这种模式仅适用在消费者可以高效并以某种速率能够处理这些消息的情况下使用。不适应高吞吐量和数据传输安全性方面
```

##### **2.1.2.** **手动应答** 

```java
--A:Channel.basicAck(用于肯定确认) 
	 RabbitMQ 已知道该消息并且成功的处理消息，可以将其丢弃了
--B:Channel.basicNack(用于否定确认) 
--C:Channel.basicReject(用于否定确认) 
	与 Channel.basicNack 相比少一个参数 (Multiple批量处理)
 	不处理该消息了直接拒绝，可以将其丢弃了
```

##### **2.1.3.** **Multiple批量参数** 

```java
--手动应答的好处是可以批量应答并且减少网络拥堵
  channel.basicAck(deliveryTag,true)    
```

![image-20210628100618363](https://gitee.com/lml000/tuc/raw/master/images/image-20210628100618363.png)



##### **2.1.4.** **重新入队(已丢失)** 

```
--消息已丢失。
(其通道已关闭，连接已关闭或 TCP 连接丢失)，导致消息未发送 ACK 确认，RabbitMQ 将了解到消息未完全处理，并将对其重新排队。如果此时其他消费者可以处理，它将很快将其重新分发给另一个消费者。
```



### **2.2. RabbitMQ 持久化**

##### **2.2.1.** **概念** 

```
保障当 RabbitMQ 服务停掉以后消息生产者发送过来的消息不丢失。默认情况下 RabbitMQ 退出或由于某种原因崩溃时，它忽视队列和消息，除非告知它不要这样做。确保消息不会丢失需要做两件事：我们需要将队列和消息都标
记为持久化。
```

##### **2.2.2.** **队列实现持久化** 

```java
--声明队列时 durable
--如之前队列不是 durable ,需删除重新创建一个durable队列   
            /**
             * 生成一个队列
             * 1.队列名称
             * 2.队列里面的消息是否持久化(磁盘) 默认消息存储在内存中 源码:持久队列
             * 3.该队列是否只供一个消费者进行消费 是否进行共享 true 可以多个消费者消费
             * 4.是否自动删除 最后一个消费者端开连接以后 该队列是否自动删除 true 自动删除
             * 5.其他参数
             */
            channel.queueDeclare(TASK_QUEUE_NAME,true,false,false,null);
```

![image-20210629094006956](https://gitee.com/lml000/tuc/raw/master/images/image-20210629094006956.png)

##### **2.2.3.** **消息实现持久化** 

```java
--生产者发送消息时
--只是告诉消息队列尽量保存到磁盘
-- 发布/确认 持久化策略
--MessageProperties.PERSISTENT_TEXT_PLAIN    
                /**
                 * 发送一个消息
                 * 1.发送到那个交换机
                 * 2.路由的 key 是哪个
                 * 3.其他的参数信息
                 * 4.发送消息的消息体
                 * 字符串转二进制
                 */
channel.basicPublish("",QUEUE_NAME,MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes());
```

##### **2.2.4.** **不公平分发** 

```java
RabbitMQ 分发消息采用的轮训分发，但是在某种场景下这种策略并不是很好，比方说有两个消费者在处理任务，其中有个消费者 1 处理任务的速度非常快，而另外一个消费者 2处理速度却很慢，这个时候我们还是采用轮训分发的化就会到这处理速度快的这个消费者很大一部分时间处于空闲状态，而处理慢的那个消费者一直在干活，这种分配方式在这种情况下其实就不太好，但是RabbitMQ 并不知道这种情况它依然很公平的进行分发。

//消费者端更改
channel.basicQos(1);    
```

##### **2.2.5.预取值分发** 

```java
//大于1就是预取值分发
channel.basicQos(2);
channel.basicQos(5);
```

![image-20210701090505297](https://gitee.com/lml000/tuc/raw/master/images/image-20210701090505297.png)



# **3. 发布确认**

### **3.1. 发布确认的策略**

```
生产者将信道设置成 confirm 模式，一旦信道进入 confirm 模式，所有在该信道上面发布的消息都将会被指派一个唯一的 ID(从 1 开始)，一旦消息被投递到所有匹配的队列之后，broker 就会发送一个确认给生产者(包含消息的唯一 ID)，这就使得生产者知道消息已经正确到达目的队列了，如果消息和队列是可持久化的，那么确认消息会在将消息写入磁盘之后发出，broker 回传给生产者的确认消息中 delivery-tag 域包含了确认消息的序列号，此外 broker 也可以设置basic.ack 的multiple 域，表示到这个序列号之前的所有消息都已经得到了处理。confirm 模式最大的好处在于他是异步的，一旦发布一条消息，生产者应用程序就可以在等信道返回确认的同时继续发送下一条消息，当消息最终得到确认之后，生产者应用便可以通过回调方法来处理该确认消息，如果 RabbitMQ 因为自身内部错误导致消息丢失，就会发送一条 nack 消息，生产者应用程序同样可以在回调方法中处理该 nack 消息。
```

```
发布确认保证消息不丢失
```

![image-20210705201015479](https://gitee.com/lml000/tuc/raw/master/images/image-20210705201015479.png)



##### **3.1.1.** **开启发布确认**

```
发布确认默认是没有开启的，如果要开启需要调用方法 confirmSelect，每当你要想使用发布
确认，都需要在 channel 上调用该方法
```



```java
         //开启发布确认
         channel.confirmSelect(); 
```

##### **3.1.2.** **单个确认发布 **

```java
这是一种简单的确认方式，它是一种同步确认发布的方式，也就是发布一个消息之后只有它被确认发布，后续的消息才能继续发布,waitForConfirmsOrDie(long)这个方法只有在消息被确认的时候才返回，如果在指定时间范围内这个消息没有被确认那么它将抛出异常。
这种确认方式有一个最大的缺点就是:发布速度特别的慢，因为如果没有确认发布的消息就会阻塞所有后续消息的发布，这种方式最多提供每秒不超过数百条发布消息的吞吐量。当然对于某些应用程序来说这可能已经足够了。
```

```java
            channel.basicPublish("",queueName,null,message.getBytes());
            //单个消息确认
            boolean b = channel.waitForConfirms();
```

##### **3.1.3.** **批量确认发布 **

```
缺点就是:当发生故障导致发布出现问题时，不知道是哪个消息出现问题了，我们必须将整个批处理保存在内存中，以记录重要的信息而后重新发布消息。当然这种方案仍然是同步的，也一样阻塞消息的发布
			与单个确认代码一致，只是不再一条条确认，而是根据条件批量确认一次
```

##### **3.1.4.** **异步确认发布 **

```
异步确认虽然编程逻辑比上两个要复杂，但是性价比最高，无论是可靠性还是效率都没得说，他是利用回调函数来达到消息可靠性传递的，这个中间件也是通过函数回调来保证是否投递成功
--根据编号,不在考虑是否成功，全靠broker
```

![image-20210705204303643](https://gitee.com/lml000/tuc/raw/master/images/image-20210705204303643.png)

```java
       //消息确认成功
        ConfirmCallback  ackCallback= ( deliveryTag,multiple)->{
            System.out.println("消息确认成功");
        };
        /**
         * 消息的标记
         * 是否批量确认
         */
        ConfirmCallback  nackCallback= (deliveryTag,multiple)->{
            System.out.println("未确认消息"+deliveryTag);
        };
        //消息的监听器 监听成功失败
        channel.addConfirmListener(ackCallback,nackCallback);
```

>失败回调

```java
ConcurrentLinkedQueue 在发消息时进行全部标记的记录
确认时筛选所有已接收记录的标记，剩下就是未确认
```

```java
   /**
         * 线程安全有序的哈希表
         */
        ConcurrentSkipListMap<Long, String> skipListMap = new ConcurrentSkipListMap<>();
        //消息确认成功
        ConfirmCallback  ackCallback= ( deliveryTag,multiple)->{
            //删除已确认的
            if (multiple){
                ConcurrentNavigableMap<Long, String> headMap = skipListMap.headMap(deliveryTag);
                headMap.clear();
            }else {
                skipListMap.remove(deliveryTag);
            }
            System.out.println("消息确认成功");
        };
        /**
         * 消息的标记
         * 是否批量确认
         */
        ConfirmCallback  nackCallback= (deliveryTag,multiple)->{
            String s = skipListMap.get(deliveryTag);
            System.out.println("未确认消息"+s);
        };

        //消息的监听器 监听成功失败
        channel.addConfirmListener(ackCallback,nackCallback);
        //异步发消息

        for (int i = 0; i < 1000; i++) {
            String message=i+"";
            channel.basicPublish("",queueName,null,message.getBytes());
            //记录所有标记 消息的总和
            skipListMap.put(channel.getNextPublishSeqNo(),message);
        }
```



# **4. 交换机**

### 4.1 发布订阅模式

```java
队列中消息只能让消息消费一次
    
交换机绑定多个队列 来实现一个消息多个消费者消费    
```

![image-20210707163337649](https://gitee.com/lml000/tuc/raw/master/images/image-20210707163337649.png)



##### **4.1.1.** **Exchanges概念及类型**

```
RabbitMQ 消息传递模型的核心思想是: 生产者生产的消息从不会直接发送到队列。
	生产者只能将消息发送到交换机(exchange)，交换机工作的内容非常简单，一方面它接收来自生产者的消息，另一方面将它们推入队列。交换机必须确切知道如何处理收到的消息。是应该把这些消息放到特定队列还是说把他们到许多队列中还是说应该丢弃它们。这就的由交换机的类型来决定。
```

```java
默认交换机("")AMQP default    不知道routingKey 队列名称
直接(direct)
主题(topic)
标题(headers) 
扇出(fanout)
```

>临时队列

```java
//2种创建方式,不持久化的队列
            /**
             * 生成一个队列
             * 1.队列名称
             * 2.队列里面的消息是否持久化(磁盘) 默认消息存储在内存中 源码:持久队列
             * 3.该队列是否只供一个消费者进行消费 是否进行共享 true 可以多个消费者消费
             * 4.是否自动删除 最后一个消费者端开连接以后 该队列是否自动删除 true 自动删除
             * 5.其他参数
             */
 channel.queueDeclare(TASK_QUEUE_NAME,false,false,false,null);
 channel.queueDeclare().getQueue()
```

##### 4.1.2 bindngs绑定

```
根据routingKey区分发送到那个队列
```



![image-20210707184952010](https://gitee.com/lml000/tuc/raw/master/images/image-20210707184952010.png)

### 4.2 fanout模式

```
将接收到的所有消息广播到它知道的所有队列中 routingKey相同
```

```java
//生产者
channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
//消费者
//声明交换机名称及类型
channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        /**
         * 交换机与队列捆绑
         * 1.队列名称
         * 2.交换机名称
         * 3.routingkey
         */
channel.queueBind(queueName, EXCHANGE_NAME, "");
channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
```

### 4.3 direct模式

```
routingKey不同
```

### 4.3 topic模式

```java
direct模式局限性:只能路由一个交换机，可以绑定多个，但无法发送多个队列
topic模式可发送多个队列
    
    发送到类型是 topic 交换机的消息的 routing_key 不能随意写，必须满足一定的要求，它必须是一个单词列表，以点号分隔开。这些单词可以是任意单词，比如说："stock.usd.nyse","nyse.vmw","quick.orange.rabbit".这种类型的。当然这个单词列表最多不能超过 255 个字节。
    *(星号)可以代替一个单词
	#(井号)可以替代零个或多个单词
如下图: Q1-->绑定的是
			中间带 orange 带 3 个单词的字符串(*.orange.*)
		Q2-->绑定的是
			最后一个单词是 rabbit 的 3 个单词(*.*.rabbit)
			第一个单词是 lazy 的多个单词(lazy.#)   
```

![image-20210707204316143](https://gitee.com/lml000/tuc/raw/master/images/image-20210707204316143.png)

```java
   channel.exchangeDeclare(EXCHANGE_NAME, "topic");
```



# **5. 死信队列**

### 5.1 死信架构

```java
无法被消费的消息
	producer 将消息投递到 broker 或者直接到queue 里了，consumer 从 queue 取出消息进行消费，但某些时候由于特定的原因导致 queue 中的某些消息无法被消费，这样的消息如果没有后续的处理，就变成了死信，有死信自然就有了死信队列。
	应用场景:为了保证订单业务的消息数据不丢失，需要使用到 RabbitMQ 的死信队列机制，当消息消费发生异常时，将消息投入死信队列中.还有比如说: 用户在商城下单成功并点击去支付后在指定时间未支付时自动失效
	
--死信来源	 
	消息 TTL 过期
	队列达到最大长度(队列满了，无法再添加数据到 mq 中)
	消息被拒绝(basic.reject 或 basic.nack)并且 requeue=false.
	
--死信队列
	type=direct
```

![image-20210708111644518](https://gitee.com/lml000/tuc/raw/master/images/image-20210708111644518.png)

```java
--Consumer
//声明死信和普通交换机 类型为 direct
        channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(DEAD_EXCHANGE, BuiltinExchangeType.DIRECT);
//声明死信队列
        String deadQueue = "dead-queue";
        channel.queueDeclare(deadQueue, false, false, false, null);
//死信队列绑定死信交换机与 routingkey
        channel.queueBind(deadQueue, DEAD_EXCHANGE, "lisi");

//正常队列绑定死信队列信息
        Map<String, Object> params = new HashMap<>();
//死信队列设置过期时间 可在生产或消费者2处设置 建议在生产者端控制
		//params.put("x-message-ttl", 10000);
//正常队列设置死信交换机 参数 key 是固定值
        params.put("x-dead-letter-exchange", DEAD_EXCHANGE);
//正常队列设置死信 routing-key 参数 key 是固定值
        params.put("x-dead-letter-routing-key", "lisi");
        String normalQueue = "normal-queue";
        channel.queueDeclare(normalQueue, false, false, false, params);
        channel.queueBind(normalQueue, NORMAL_EXCHANGE, "zhangsan");
```

### 5.2 死信3大来源

```java
//	消息 TTL 过期，队列达到最大长度，消息被拒绝 设置

--Producer
    //设置消息的 TTL 时间
        AMQP.BasicProperties properties =
                       new AMQP.BasicProperties().builder().expiration("10000").build();
		channel.basicPublish(NORMAL_EXCHANGE, "zhangsan",properties,message.getBytes());
--Consumer
    //  params.put("x-message-ttl", 10000);
    
//队列达到最大长度
--Consumer
    	 params.put("x-max-length", 6);
//消息被拒绝
        DeliverCallback deliverCallback = (consumerTag, delivery) ->
        {String message = new String(delivery.getBody(), "UTF-8");
            if (message.equals("info5")){
                /**
                 * 当前队列的标记
                 * true重新排队,进去死信
                 */
                channel.basicReject(delivery.getEnvelope().getDeliveryTag(),false);
                /**
                 * 当前队列的标记
                 * true拒绝所有标签
                 */
                //channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
                System.out.println("此消息是被拒绝掉的"+message);
            }
        };
```



### 5.3 延迟队列

```
延迟队列其实就是基于死信队列的ttl过期
	延时队列,队列内部是有序的，最重要的特性就体现在它的延时属性上，延时队列中的元素是希望在指定时间到了以后或之前取出和处理，简单来说，延时队列就是用来存放需要在指定时间被处理的元素的队列
```

>延迟使用场景

```
1.订单在十分钟之内未支付则自动取消
2.新创建的店铺，如果在十天内都没有上传过商品，则自动发送消息提醒。
3.用户注册成功后，如果三天内没有登陆则进行短信提醒。
4.用户发起退款，如果三天内没有得到处理则通知相关运营人员。
5.预定会议后，需要在预定的时间点前十分钟通知各个与会人员参加会议

这些场景都有一个特点，需要在某个事件发生之后或者之前的指定时间点完成某一项任务，如：发生订单生成事件，在十分钟之后检查该订单支付状态，然后将未支付的订单进行关闭；看起来似乎使用定时任务，一直轮询数据，每秒查一次，取出需要被处理的数据，然后处理不就完事了吗？如果数据量比较少，确实可以这样做，比如：对于“如果账单一周内未支付则进行自动结算”这样的需求，如果对于时间不是严格限制，而是宽松意义上的一周，那么每天晚上跑个定时任务检查一下所有未支付的账单，确实也是一个可行的方案。但对于数据量比较大，并且时效性较强的场景，如：“订单十分钟内未支付则关闭“，短期内未支付的订单数据可能会有很多，活动期间甚至会达到百万甚至千万级别，对这么庞大的数据量仍旧使用轮询的方式显然是不可取的，很可能在一秒内无法完成所有订单的检查，同时会给数据库带来很大压力，无法满足业务要求而且性能低下。
```

![image-20210708143638523](https://gitee.com/lml000/tuc/raw/master/images/image-20210708143638523.png)



### **5.4 整合springboot**

```
原先交换机队列死信队列 绑定关系 都由消费者来承担维护，现在都由配置类来完成创建
```

![image-20210708145512282](https://gitee.com/lml000/tuc/raw/master/images/image-20210708145512282.png)

```java
--配置类 完成交换机 队列 及绑定的相关操作
	1.声明交换机
    2.声明队列Queue//QueueBuilder.durable(QUEUE_A).withArguments(args).build();
    3.绑定队列与交换机及key//BindingBuilder.bind(queueA).to(xExchange).with("XA");
--生产者
    rabbitTemplate.convertAndSend("X", "XA", "消息来自 ttl 为 10S 的队列: "+message);
--消费者(监听者)
    @RabbitListener(queues = "QD") //监听指定队列
```

![image-20210708203207093](https://gitee.com/lml000/tuc/raw/master/images/image-20210708203207093.png)

### 5.5 优化代码及死信缺陷

```
生产者来指定时间的传递，而不是在队列中定死。通用队列
死信缺陷：
--RabbitMQ 只会检查第一个消息是否过期，如果过期则丢到死信队列，如果第一个消息的延时时长很长，而第二个消息的延时时长很短，第二个消息并不会优先得到执行。
```

### 5.6 基于插件解决死信缺陷

```java
https://www.rabbitmq.com/community-plugins.html
rabbitmq_delayed_message_exchange
https://github.com/rabbitmq/rabbitmq-delayed-message-exchange    
```

>docker安装

```sh
#上传到内部容器的plugins中
docker exec -it 5283d4d3cfe0 /bin/bash
#复制到容器内
docker cp rabbitmq_delayed_message_exchange-3.8.17.8f537ac.ez 5283d4d3cfe0:/plugins
#启动安装
rabbitmq-plugins enable rabbitmq_delayed_message_exchange
#重启
docker restart 5283d4d3cfe0
```

>延迟插件

```
基于死信的是在队列控制，而基于插件的是在交换机控制更简洁了

	延时队列在需要延时处理的场景下非常有用，使用 RabbitMQ 来实现延时队列可以很好的利用RabbitMQ 的特性，如：消息可靠发送、消息可靠投递、死信队列来保障消息至少被消费一次以及未被正确处理的消息不会被丢弃。另外，通过 RabbitMQ 集群的特性，可以很好的解决单点故障问题，不会因为单个节点挂掉导致延时队列不可用或者消息丢失。
	延时队列还有很多其它选择，比如利用 Java 的 DelayQueue，利用 Redis 的 zset，利用 Quartz或者利用 kafka 的时间轮，这些方式各有特点,看需要适用的场景。
```

![image-20210714204140345](https://gitee.com/lml000/tuc/raw/master/images/image-20210714204140345.png)



# **5. 发布确认高级**

```
在生产环境中由于一些不明原因，导致 rabbitmq 重启，在 RabbitMQ 重启期间生产者消息投递失败，导致消息丢失，需要手动处理和恢复。于是，我们开始思考，如何才能进行 RabbitMQ 的消息可靠投递呢？ 特别是在这样比较极端的情况，RabbitMQ 集群不可用的时候，无法投递的消息该如何处理呢
	--需要一个消息回调来确认mq是否接收到了发送的消息
```

### 5.1  交换机回调接口

```yml
#yml配置启动
⚫ NONE
禁用发布确认模式，是默认值
⚫ CORRELATED
发布消息成功到交换器后会触发回调方法
⚫ SIMPLE
经测试有两种效果，其一效果和 CORRELATED 值一样会触发回调方法，其二在发布消息成功后使用 rabbitTemplate 调用 waitForConfirms 或 waitForConfirmsOrDie 方法等待 broker 节点返回发送结果，根据返回结果来判定下一步的逻辑，要注意的点是waitForConfirmsOrDie 方法如果返回 false 则会关闭 channel，则接下来无法发送消息到 broker
spring:
  rabbitmq:
    publisher-confirm-type: correlated
```

```java
就算集群，也要做好最坏措施，当mq全部不能使用时，使用回调接口，保证消息能够回来，不丢失
@Component  
public class MyCallBack implements RabbitTemplate.ConfirmCallback{

    //
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //注入自定义方法 因为是自己实现所有还需要初始化注入进去调用
    //PostConstruct 注解用于需要在依赖注入完成后执行任何初始化的方法 最先执行MyCallBack -》RabbitTemplate -》@PostConstruct  否则set值会null值异常
    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
    }
   /**
     * @Author: lml
     * @Date: 2021/7/16 15:46
     * @param correlationData:保存回调消息的id及相关信息 由生产者发送过来 convertAndSend重载
     * @param ack:交换机是否收到消息 true
     * @param cause:cause原因-null 成功(null)失败原因
     * @return: void
     **/
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String id =correlationData!=null?correlationData.getId():"";
        if (ack){
            log.info("交换机已经收到了id为:{}的消息",id);
        }else {
            log.info("交换机还没收到id未:{}的消息,由于:{}原因",id,cause);
        }
    }
}
```

### 5.2  队列回退消息

```
在仅开启了生产者确认机制的情况下，交换机接收到消息后，会直接给消息生产者发送确认消息，如果发现该消息不可路由(无法转到指定队列)，那么消息会被直接丢弃，此时生产者是不知道消息被丢弃这个事件的。
通过设置 mandatory 参数可以在当消息传递过程中不可达目的地时将消息返回给生产者。
```

```yml
#启动队列发布退回
spring:
  rabbitmq:
    publisher-returns: true
```

```java
public class MyCallBack implements RabbitTemplate.ReturnsCallback{
    
      //注入自定义方法
    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }
}
```



### 5.3 备份交换机

```java
有了 mandatory 参数和回退消息，我们获得了对无法投递消息的感知能力，有机会在生产者的消息无法被投递时发现并处理。但有时候，我们并不知道该如何处理这些无法路由的消息，最多打个日志，然后触发报警，再来手动处理。而通过日志来处理这些无法路由的消息是很不优雅的做法，特别是当生产者所在的服务有多台机器的时候，手动复制日志会更加麻烦而且容易出错。
	而且设置 mandatory 参数会增加生产者的复杂性，需要添加处理这些被退回的消息的逻辑。如果既不想丢失息，又不想增加生产者的复杂性，该怎么做呢？前面在设置死信队列的文章中，我们提到，可以为队列设置死信交换机来存储那些处理失败的消息，可是这些不可路由消息根本没有机会进入到队列，因此无法使用死信队列来保存消息。
	在 RabbitMQ 中，有一种备份交换机的机制存在，可以很好的应对这个问题。什么是备份交换机呢？备份交换机可以理解为 RabbitMQ 中交换机的“备胎”，当我们为某一个交换机声明一个对应的备份交换机时，就是为它创建一个备胎，当交换机接收到一条不可路由消息时，将会把这条消息转发到备份交换机中，由备份交换机来进行转发和处理，通常备份交换机的类型为 Fanout ，这样就能把所有消息都投递到与其绑定的队列中，然后我们在备份交换机下绑定一个队列，这样所有那些原交换机无法被路由的消息，就会都进入这个队列了。当然，我们还可以建立一个报警队列，用独立的消费者来进行监测和报警。


1.可以不用使用5.1和5.2的回调回退方法而使用备份交换机来解决消息不可达或交换机错误
  1.1:回调回退方法不知道该如何处理这些无法路由的消息，最多打个日志
2.不可路由消息根本没有机会进入到队列，因此无法使用死信队列来保存消息
3.备份交换机的机制,类型为 Fanout
4.好处:我们还可以建立一个报警队列，用独立的消费者来进行监测和报警。
```

![image-20210716165212059](https://gitee.com/lml000/tuc/raw/master/images/image-20210716165212059.png)

```java
   //设置确认交换机绑定备份交换机
   //备份交换机优先级高。
	@Bean("confirmExchange")
    public DirectExchange confirmExchange(){
        ExchangeBuilder exchangeBuilder = ExchangeBuilder.directExchange(CONFIRM_EXCHANGE_NAME)
                    .durable(true)
                    .withArgument("alternate-exchange", BACKUP_EXCHANGE_NAME);
        return (DirectExchange)exchangeBuilder.build();
    }
```



# 6.mq重复消费

### 6.1 幂等性

```
用户对于同一操作发起的一次请求或者多次请求的结果是一致的，不会因为多次点击而产生了副作用。举个最简单的例子，那就是支付，用户购买商品后支付，支付扣款成功，但是返回结果的时候网络异常，此时钱已经扣了，用户再次点击按钮，此时会进行第二次扣款，返回结果成功，用户查询余额发现多扣钱了，流水记录也变成了两条。在以前的单应用系统中，我们只需要把数据操作放入事务中即可，发生错误立即回滚，但是再响应客户端的时候也有可能出现网络中断或者异常等等
```

> mq消息重复消费

```
消费者在消费 MQ 中的消息时，MQ 已把消息发送给消费者，消费者在给MQ 返回 ack 时网络中断，故 MQ 未收到确认信息，该条消息会重新发给其他的消费者，或者在网络重连后再次发送给该消费者，但实际上该消费者已成功消费了该条消息，造成消费者消费了重复的消息
```

>解决方案

```java
--唯一id(mq的该id)先判断该消息是否已消费过。
--a.唯一 ID+指纹码机制,利用数据库主键去重, b.利用 redis 的原子性去实现    
    指纹码:我们的一些规则或者时间戳加别的服务给到的唯一信息码,它并不一定是我们系统生成的，基本都是由我们的业务规则拼接而来，但是一定要保证唯一性，然后就利用查询语句进行判断这个 id 是否存在数据库中,优势就是实现简单就一个拼接，然后查询判断是否重复；劣势就是在高并发时，如果是单个数据库就会有写入性能瓶颈当然也可以采用分库分表提升性能，但也不是我们最推荐的方式。
    利用 redis 执行 setnx 命令，天然具有幂等性。从而实现不重复消费
```



# 7.优先级队列

```java
//优先级队列 0-255 越大越优先
在我们系统中有一个订单催付的场景，我们的客户在天猫下的订单,淘宝会及时将订单推送给我们，如果在用户设定的时间内未付款那么就会给用户推送一条短信提醒，很简单的一个功能对吧，但是，tmall商家对我们来说，肯定是要分大客户和小客户的对吧，比如像苹果，小米这样大商家一年起码能给我们创造很大的利润，所以理应当然，他们的订单必须得到优先处理，而曾经我们的后端系统是使用 redis 来存放的定时轮询，大家都知道 redis 只能用 List 做一个简简单单的消息队列，并不能实现一个优先级的场景，所以订单量大了后采用 RabbitMQ 进行改造和优化,如果发现是大客户的订单给一个相对比较高的优先级，否则就是默认优先级。
```

```java
--队列需要设置为优先级队列，消息需要设置消息的优先级
--springboot
  rabbitTemplate.convertAndSend(DelayedQueueConfig.DELAYED_EXCHANGE_NAME, DelayedQueueConfig.DELAYED_ROUTING_KEY, message,
                //设置延迟优先级
                msg -> {
                    msg.getMessageProperties().setDelay(delayTime);
                    msg.getMessageProperties().setPriority(5);
                    return msg;

                });  

--java
  AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().priority(5).build();
  channel.basicPublish("",QUEUE_NAME,properties,message.getBytes());
```



# 8.惰性队列

```
消息保存内存中还是在磁盘中
正常:内存
惰性:磁盘 (先去在磁盘->内存在消费)
场景:消费者下线、宕机亦或者是由于维护而关闭等)而致使长时间内不能消费消息造成堆积时，惰性队列就很有必要了。
```

```java
--java
Map<String, Object> args = new HashMap<String, Object>();
args.put("x-queue-mode", "lazy");
channel.queueDeclare("myqueue", false, false, false, args);

--springboot
     //声明队列
    @Bean("queueC")
    public Queue queueC(){
        Map<String, Object> args = new HashMap<>(3);
        //声明当前队列绑定的死信交换机
        args.put("x-dead-letter-exchange", Y_DEAD_LETTER_EXCHANGE);
        //声明当前队列的死信路由 key
        args.put("x-dead-letter-routing-key", "YD");
        args.put("x-queue-mode", "lazy");
        return QueueBuilder.durable(QUEUE_C).withArguments(args).build();
    }   

--             QueueBuilder.durable().lazy().build();
```



# 9.mq集群

```sh
--linux 版
#修改每个主机机器名称 在重启
vim /etc/hostname
#设置每个所有集群ip --ip名 hostname
vim /etc/hosts
10.211.55.74 node1
10.211.55.75 node2
10.211.55.76 node3

#cookie一致 以node1远程copy替换
scp /var/lib/rabbitmq/.erlang.cookie root@node2:/var/lib/rabbitmq/.erlang.cookie
scp /var/lib/rabbitmq/.erlang.cookie root@node3:/var/lib/rabbitmq/.erlang.cookie

#重启
rabbitmq-server -detached

#node2与node3加入进node1集群
rabbitmqctl stop_app
(rabbitmqctl stop 会将Erlang 虚拟机关闭，rabbitmqctl stop_app 只关闭 RabbitMQ 服务)
rabbitmqctl reset
rabbitmqctl join_cluster rabbit@node1
rabbitmqctl start_app(只启动应用服务)

#查询集群状态
rabbitmqctl cluster_status

#重新设置用户
创建账号
rabbitmqctl add_user admin 123
设置用户角色
rabbitmqctl set_user_tags admin administrator
设置用户权限
rabbitmqctl set_permissions -p "/" admin ".*" ".*" ".*"

#解除节点
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl start_app
rabbitmqctl cluster_status
rabbitmqctl forget_cluster_node rabbit@node2(node1 机器上执行)
```

### 9.1 镜像队列

```java
问题:mq集群node1创建的队列其他节点无法复用
    解决:镜像队列队列可复用
--暂时只看到页面操作和Linux操作，但没在QueueBuilder中找到参数后续会找如何java配置
  name:策略名称	pattern:正则表达式(mirrior开头的交换机或队列才镜像)	
  ha-mode:指明镜像队列的模式，有效值为 all/exactly/nodes
     	all：表示在集群中所有的节点上进行镜像
        exactly：表示在指定个数的节点上进行镜像，节点的个数由ha-params指定
        nodes：表示在指定的节点上进行镜像，节点名称通过ha-params指定
  ha-params：ha-mode模式需要用到的参数
  ha-sync-mode：进行队列中消息的同步方式，有效值为automatic和manual
      
--Linux
  所有:
  rabbitmqctl set_policy -p zat ha-allqueue "^" '{"ha-mode":"all"}'
  限制:
  rabbitmqctl set_policy ha-two "^" '{"ha-mode":"exactly","ha-params":2,"ha-sync-mode":"automatic"}'
```

>   页面

![image-20210720165523531](https://gitee.com/lml000/tuc/raw/master/images/image-20210720165523531.png)

### 9.2 Haproxy实现负载均衡

```
HAProxy 提供高可用性、负载均衡及基于TCPHTTP 应用的代理，支持虚拟主机，它是免费、快速并且可靠的一种解决方案，包括 Twitter,Reddit,StackOverflow,GitHub 在内的多家知名互联网公司在使用。HAProxy 实现了一种事件驱动、单一进程模型，此模型支持非常大的井发连接数。
nginx,lvs,haproxy 之间的区别: http://www.ha97.com/5646.html
```

```sh
--haproxy.cfg配置:vim /etc/haproxy/haproxy.cfg
```

[haproxy.cfg配置详解网址](https://www.cnblogs.com/2567xl/p/11640991.html)

[hub.docker启动](https://hub.docker.com/_/haproxy)

### **9.3 Keepalived实现高可用**

```sh
# Keepalived 它能够通过自身健康检查、资源接管功能做高可用(双机热备)，实现故障转移。
修改/etc/keepalived/keepalived.conf
```

[简略介绍](https://www.cnblogs.com/mzsg/p/5623585.html)

![image-20210720194748885](https://gitee.com/lml000/tuc/raw/master/images/image-20210720194748885.png)



### **9.4 Federation模式插件**

```sh
(broker 北京)，(broker 深圳)彼此之间相距甚远，网络延迟是一个不得不面对的问题。
Federation 插件就可以很好地解决这个问题

#/plugins目录下启动
rabbitmq-plugins enable rabbitmq_federation
rabbitmq-plugins enable rabbitmq_federation_management

2种方式交换机或队列
#交换机
#先创建好下游节点downstream 交换机
#数据由上游联邦交换机->下游
#配置信息由上游配置

#队列
```

后续太浅了

>Shovel 插件

```sh
与Federation相似功能 队列(源端->目的端)
rabbitmq-plugins enable rabbitmq_shovel
rabbitmq-plugins enable rabbitmq_shovel_management
```

