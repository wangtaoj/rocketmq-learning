package com.wangtao.rocketmq.basic.retry;

import com.wangtao.rocketmq.basic.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用于演示顺序消费时消息重试场景
 * @author wangtao
 * Created at 2023/5/17 20:07
 */
@Slf4j
public class RetryOrderConsumer {

    /**
     * 顺序消息、集群模式
     * 最大重试次数: Integer.MAX_VALUE
     * 每次重试时间: 1s
     *
     * 广播模式目前不支持顺序消息
     * 并且由于是顺序消费, 同一队列后面的消息不会被消费, 直到该消息消费成功
     */
    public static void main(String[] args) throws Exception {
        AtomicInteger consumeCount = new AtomicInteger(0);
        // 初始化consumer，并设置consumer group name
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("retry_order_consumer");

        // 设置NameServer地址
        consumer.setNamesrvAddr(Constant.NAME_SERVER);
        //订阅一个或多个topic，并指定tag过滤条件，这里指定*表示接收所有tag的消息
        consumer.subscribe("retryMsg", "*");
        //注册回调接口来处理从Broker中收到的消息
        consumer.registerMessageListener(new MessageListenerOrderly() {
            @Override
            public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
                consumeCount.getAndIncrement();
                for (MessageExt msg : msgs) {
                    try {
                        log.info("Receive New Messages: {}", new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                // 模拟第三次才消费成功
                if (consumeCount.get() == 10) {
                    return ConsumeOrderlyStatus.SUCCESS;
                }
                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
            }
        });
        // 启动Consumer
        consumer.start();
        System.out.printf("Consumer Started.%n");
    }
}
