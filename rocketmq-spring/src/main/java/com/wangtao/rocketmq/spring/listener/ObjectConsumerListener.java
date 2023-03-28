package com.wangtao.rocketmq.spring.listener;

import com.wangtao.rocketmq.spring.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * @author wangtao
 * Created at 2023/3/28 21:04
 */
@Slf4j
@Component
@RocketMQMessageListener(topic = "objMsg", consumerGroup = "spring-obj-consumer", selectorExpression = "tagA")
public class ObjectConsumerListener implements RocketMQListener<UserVO> {

    @Override
    public void onMessage(UserVO message) {
        log.info("{}", message);
    }
}
