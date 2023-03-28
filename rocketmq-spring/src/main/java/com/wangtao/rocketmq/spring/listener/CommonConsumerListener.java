package com.wangtao.rocketmq.spring.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author wangtao
 * Created at 2023/3/28 20:33
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "commonMsg", consumerGroup = "spring-common-consumer", selectorExpression = "tagA")
public class CommonConsumerListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        log.info(message);
    }
}
