package com.lml.sbmq.controller;

import com.lml.sbmq.mqconfig.ConfirmBackupConfig;
import com.lml.sbmq.mqconfig.DelayedQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author lmlkyc
 * @date 2021-07-08 20:22
 */
@Slf4j
@RequestMapping("ttl")
@RestController
public class SendMsgController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("sendMsg/{message}")
    public void sendMsg(@PathVariable String message){
        log.info("当前时间：{},发送一条信息给两个 TTL 队列:{}", new Date(), message);
        rabbitTemplate.convertAndSend("X", "XA", "消息来自 ttl 为 10S 的队列: "+message);
        rabbitTemplate.convertAndSend("X", "XB", "消息来自 ttl 为 40S 的队列: "+message);
    }

    /**
     * 带ttl时间参数的通用队列
     * @param message
     * @param ttlTime
     * 消息ttl方式，消息可能不好立即死亡。会按照第一个消息死亡时间进行，第二个消息就算延迟时间短也会在第一个消息后执行
     */
    @GetMapping("sendMsg/{message}/{ttlTime}")
    public void sendTtlMsg(@PathVariable String message,@PathVariable String ttlTime){
        log.info("当前时间：{},发送一条时长{}毫秒 TTL 信息给队列 C:{}", new Date(),ttlTime, message);
        rabbitTemplate.convertAndSend("X", "XC", message,
                //设置过期时常
                msg ->{msg.getMessageProperties().setExpiration(ttlTime);
        return msg;
});
    }
    /**
     * @Description: 基于插件的延迟队列 控制层来判断是否提前消费低延迟的消息
     * @Author: lml
     * @Date: 2021/7/16 11:11
     * @param message:
     * @param delayTime:
     * @return: void
     * test:http://localhost:8080/ttl/sendDelayMsg/come on baby1/20000
     *      http://localhost:8080/ttl/sendDelayMsg/come on baby2/2000
     **/
    @GetMapping("sendDelayMsg/{message}/{delayTime}")
    public void sendDelayMsg(@PathVariable String message,@PathVariable Integer delayTime){
        log.info("当前时间：{},发送一条时长{}毫秒  信息给延迟队列队列 C:{}", new Date(),delayTime, message);
        rabbitTemplate.convertAndSend(DelayedQueueConfig.DELAYED_EXCHANGE_NAME, DelayedQueueConfig.DELAYED_ROUTING_KEY, message,
                //设置延迟优先级
                msg -> {
                    msg.getMessageProperties().setDelay(delayTime);
                    msg.getMessageProperties().setPriority(5);
                    return msg;

                });
    }

/**
 * @Description: 发布确认高级 回调
 * @Author: lml
 * @Date: 2021/7/16 15:17
 * @param message:
 * @return: void
 * test: http://localhost:8080/ttl/sendConfirmMsg/hello confirmMsgCallback
 **/
    @GetMapping("sendConfirmMsg/{message}")
    public void sendConfirmMsg(@PathVariable String message){
        CorrelationData correlationData =new CorrelationData("1");
        rabbitTemplate.convertAndSend(ConfirmBackupConfig.CONFIRM_EXCHANGE_NAME,"key1", message+"key1",correlationData);
        log.info("当前：{}  信息给确认回调队列", message+"key1");

        CorrelationData correlationData2 =new CorrelationData("2");
        rabbitTemplate.convertAndSend(ConfirmBackupConfig.CONFIRM_EXCHANGE_NAME,"key12", message+"key12",correlationData2);
        log.info("当前：{}  信息给确认回调队列", message+"key12");
    }

}
