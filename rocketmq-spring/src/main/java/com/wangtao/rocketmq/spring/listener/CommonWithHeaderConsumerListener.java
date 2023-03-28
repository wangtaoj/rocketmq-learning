package com.wangtao.rocketmq.spring.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * @author wangtao
 * Created at 2023/3/28 20:33
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "commonMsg", consumerGroup = "spring-common-header-consumer", selectorExpression = "tagB")
public class CommonWithHeaderConsumerListener implements RocketMQListener<MessageExt> {

    @Override
    public void onMessage(MessageExt message) {
        log.info("properties: {}", message.getProperties());
        log.info("key: {}", message.getKeys());
        log.info("customHeader: {}", message.getProperty("customHeader"));
        log.info("msg: {}", new String(message.getBody(), StandardCharsets.UTF_8));
    }
}
