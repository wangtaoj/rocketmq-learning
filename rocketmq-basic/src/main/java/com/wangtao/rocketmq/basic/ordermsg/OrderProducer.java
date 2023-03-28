package com.wangtao.rocketmq.basic.ordermsg;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.util.List;

/**
 * 顺序消息, 指定消息发送给哪个队列
 * @author wangtao
 * Created at 2022/10/19 20:44
 */
public class OrderProducer {

    public static void main(String[] args) throws Exception {
        // 初始化一个producer并设置Producer group name
        DefaultMQProducer producer = new DefaultMQProducer("order_producer");
        // 设置NameServer地址
        producer.setNamesrvAddr("localhost:9876");
        // 启动producer
        producer.start();
        for (int i = 0; i < 10; i++) {
            // 创建一条消息，并指定topic、tag、body等信息，tag可以理解成标签，对消息进行再归类，RocketMQ可以在消费端对tag进行过滤
            Message msg = new Message("orderMsg",
                    "TagA",
                    ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET)
            );
            int finalI = i;
            // 利用producer进行发送，并同步等待发送结果
            SendResult sendResult = producer.send(msg, new MessageQueueSelector() {
                @Override
                public MessageQueue select(List<MessageQueue> mqs, Message msg, Object arg) {
                    int queueIndex = finalI % mqs.size();
                    return mqs.get(queueIndex);
                }
            }, null);
            System.out.printf("%s%n", sendResult);
        }
        // 一旦producer不再使用，关闭producer
        producer.shutdown();
    }
}
