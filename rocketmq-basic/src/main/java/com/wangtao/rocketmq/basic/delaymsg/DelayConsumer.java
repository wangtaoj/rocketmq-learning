package com.wangtao.rocketmq.basic.delaymsg;

import com.wangtao.rocketmq.basic.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author wangtao
 * Created at 2022/10/19 19:53
 */
@Slf4j
public class DelayConsumer {

    public static void main(String[] args) throws Exception {
        // 初始化consumer，并设置consumer group name
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("delay_consumer");

        // 设置NameServer地址
        consumer.setNamesrvAddr(Constant.NAME_SERVER);
        //订阅一个或多个topic，并指定tag过滤条件，这里指定*表示接收所有tag的消息
        consumer.subscribe("delayMsg", "*");
        //注册回调接口来处理从Broker中收到的消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    log.info(new String(msg.getBody(), StandardCharsets.UTF_8));
                    System.out.println("====================================");
                }
                // 返回消息消费状态，ConsumeConcurrentlyStatus.CONSUME_SUCCESS为消费成功
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // 启动Consumer
        consumer.start();
        System.out.printf("Consumer Started.%n");
    }
}
