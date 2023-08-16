package com.wangtao.rocketmq.basic.delaymsg;

import com.wangtao.rocketmq.basic.Constant;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * @author wangtao
 * Created at 2023/8/16 20:59
 */
public class DelayProducer {

    /**
     * 延时消息
     * RocketMQ不支持任意精度的延时消息, 目前只有18个等级
     * 1s 5s 10s 30s 1m... 10m 20m 30m 1h 2h
     * 完整的等级见org.apache.rocketmq.store.config.MessageStoreConfig.messageDelayLevel
     */
    public static void main(String[] args) throws Exception{
        // 初始化一个producer并设置Producer group name
        DefaultMQProducer producer = new DefaultMQProducer("delay_producer");
        // 设置NameServer地址
        producer.setNamesrvAddr(Constant.NAME_SERVER);
        // 启动producer
        producer.start();
        for (int i = 1; i <= 5; i++) {
            // 创建一条消息，并指定topic、tag、body等信息，tag可以理解成标签，对消息进行再归类，RocketMQ可以在消费端对tag进行过滤
            Message msg = new Message("delayMsg",
                    ("delayMsg " + i).getBytes(RemotingHelper.DEFAULT_CHARSET)
            );
            // 设置延时等级
            msg.setDelayTimeLevel(i);
            // 利用producer进行发送，并同步等待发送结果
            SendResult sendResult = producer.send(msg);
            System.out.printf("%s%n", sendResult);
        }
        // 一旦producer不再使用，关闭producer
        producer.shutdown();
    }
}
