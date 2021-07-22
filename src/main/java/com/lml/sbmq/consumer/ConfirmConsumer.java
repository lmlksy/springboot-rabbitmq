package com.lml.sbmq.consumer;

import com.lml.sbmq.mqconfig.ConfirmBackupConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author lmlkyc
 * @date 2021-07-16 15:35
 */
@Slf4j
@Component
public class ConfirmConsumer {
    @RabbitListener(queues = ConfirmBackupConfig.CONFIRM_QUEUE_NAME)
    public void receiveConfirmMsg(Message message){
        String msg=new String(message.getBody());
        log.info("接受到队列 confirm.queue 消息:{}",msg);
    }

}
