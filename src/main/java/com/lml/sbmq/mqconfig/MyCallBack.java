package com.lml.sbmq.mqconfig;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author lmlkyc
 * @date 2021-07-16 15:45
 */
@Slf4j
@Component
public class MyCallBack implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnsCallback{


    @Autowired
    private RabbitTemplate rabbitTemplate;

    //注入自定义方法
    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }
    /**
     * @Description:  交换机不管是否收到消息的一个回调方法
     *        1:发消息 交换机接收到了 回调
     *          1.1:correlationData 保存回调消息的id及相关信息
     *          1.2:交换机收到消息 true
     *          1.3:cause原因-null
     *        2：发消息 没有接收到 失败
     *          2.1:correlationData 保存回调消息的id及相关信息
     *          2.2:交换机收到消息 false
     *          1.3:cause失败原因
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

    /**
     * @Description:消息传递过程不可达将消息返回给生产者
     * @Author: lml
     * @Date: 2021/7/16 16:38
     * @ReturnedMessage: Message message;
     *                   int replyCode;
     *                   String replyText;
     *                   String exchange;
     *                   String routingKey;
     * @param returned:
     * @return: void
     **/
    @Override
    public void returnedMessage(ReturnedMessage returned) {
        log.error("消息:{},被交换机:{}退回,退回原因:{},路由key:{}",
                returned.getMessage(),returned.getExchange(),returned.getReplyText(),returned.getRoutingKey());
    }
}
