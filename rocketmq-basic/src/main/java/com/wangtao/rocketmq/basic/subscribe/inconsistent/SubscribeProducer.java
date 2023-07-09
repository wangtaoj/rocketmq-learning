package com.wangtao.rocketmq.basic.subscribe.inconsistent;

import com.wangtao.rocketmq.basic.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import java.nio.charset.StandardCharsets;

/**
 * 用于模拟同一个消费者组下不同的消费者订阅同一个主题不同的tag, 观察消息不消费情况
 * @author wangtao
 * Created at 2022/10/19 19:40
 */
@Slf4j
public class SubscribeProducer {

    public static void main(String[] args) throws Exception {
        // 初始化一个producer并设置Producer group name
        DefaultMQProducer producer = new DefaultMQProducer("subscribe_producer");
        // 设置NameServer地址
        producer.setNamesrvAddr(Constant.NAME_SERVER);
        // 启动producer
        producer.start();
        for (int i = 0; i < 8; i++) {
            // 创建一条消息，并指定topic、tag、body等信息，tag可以理解成标签，对消息进行再归类，RocketMQ可以在消费端对tag进行过滤
            Message msg = new Message(Constant.SUBSCRIBE_INCONSISTENT_TOPIC,
                    "tagB",
                    ("B" + i).getBytes(StandardCharsets.UTF_8)
            );
            // 利用producer进行发送，并同步等待发送结果
            SendResult sendResult = producer.send(msg);
            log.info("{}", sendResult);
        }
        // 一旦producer不再使用，关闭producer
        producer.shutdown();
    }
}
