package com.wangtao.rocketmq.basic.retry;

import com.wangtao.rocketmq.basic.Constant;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;

/**
 * 用于演示消息重试的生产者
 * @author wangtao
 * Created at 2023/5/17 19:58
 */
public class RetryProducer {

    public static void main(String[] args) throws Exception {
        // 初始化一个producer并设置Producer group name
        DefaultMQProducer producer = new DefaultMQProducer("retryProducer");
        // 设置NameServer地址
        producer.setNamesrvAddr(Constant.NAME_SERVER);
        // 启动producer
        producer.start();
        for (int i = 0; i < 1; i++) {
            // 创建一条消息，并指定topic、tag、body等信息，tag可以理解成标签，对消息进行再归类，RocketMQ可以在消费端对tag进行过滤
            Message msg = new Message("retryMsg",
                    "TagA",
                    "retry-" + i,
                    ("Hello RocketMQ " + i).getBytes(StandardCharsets.UTF_8)
            );
            // 利用producer进行发送，并同步等待发送结果
            SendResult sendResult = producer.send(msg);
            System.out.printf("%s%n", sendResult);
        }
        // 一旦producer不再使用，关闭producer
        producer.shutdown();
    }
}
