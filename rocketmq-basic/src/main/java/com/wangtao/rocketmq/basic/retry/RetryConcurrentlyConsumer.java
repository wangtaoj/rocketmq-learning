package com.wangtao.rocketmq.basic.retry;

import com.wangtao.rocketmq.basic.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于演示并发消费时消息重试场景
 * @author wangtao
 * Created at 2023/5/17 20:07
 */
@Slf4j
public class RetryConcurrentlyConsumer {

    /**
     * 无序消息、集群模式
     * 最大重试次数: 16
     * 每次重试时间会递增, 10s 30s 1m等
     * 完整的重试间隔等级见org.apache.rocketmq.store.config.MessageStoreConfig.messageDelayLevel
     * 注: 该类在RocketMQ服务端源码中, 重试是在第三个等级开始的, 第一个等级为1s  第二个等级为5s
     * 也就是说消息重试本质上是通过延时消息实现的, RocketMQ会将消息发送到消费者的重试队列中, 并且设置延时等级
     *
     * 无序消息、广播模式不会进行重试
     * 官方文档: <a href="https://rocketmq.apache.org/zh/docs/4.x/consumer/02push" />
     */
    public static void main(String[] args) throws Exception {
        AtomicInteger consumeCount = new AtomicInteger(0);
        // 初始化consumer，并设置consumer group name
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("retry_concurrently_consumer");

        // 设置NameServer地址
        consumer.setNamesrvAddr(Constant.NAME_SERVER);
        //订阅一个或多个topic，并指定tag过滤条件，这里指定*表示接收所有tag的消息
        consumer.subscribe("retryMsg", "*");
        //注册回调接口来处理从Broker中收到的消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt msg : msgs) {
                    consumeCount.getAndIncrement();
                    try {
                        log.info("Receive New Messages: {}", new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                if (consumeCount.get() == 3) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });
        // 启动Consumer
        consumer.start();
        System.out.printf("Consumer Started.%n");
    }
}
