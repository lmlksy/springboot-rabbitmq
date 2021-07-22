package com.lml.sbmq.consumer;

import com.lml.sbmq.mqconfig.ConfirmBackupConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author lmlkyc
 * @date 2021-07-19 19:39
 */
@Slf4j
@Component
public class WarningConsumer {

    @RabbitListener(queues = ConfirmBackupConfig.WARNING_QUEUE_NAME)
    public void receiveWarningMsg(Message message) {
        String msg = new String(message.getBody());
        log.error("报警发现不可路由消息：{}", msg);
    }

}
