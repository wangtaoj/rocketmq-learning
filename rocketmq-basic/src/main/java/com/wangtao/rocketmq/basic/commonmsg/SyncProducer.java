package com.wangtao.rocketmq.basic.commonmsg;

import com.wangtao.rocketmq.basic.Constant;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * 同步发送消息
 * 每一个主题默认有4个队列, 轮询发送到每个队列中
 * @author wangtao
 * Created at 2022/10/19 19:40
 */
public class SyncProducer {

    public static void main(String[] args) throws Exception {
        // 初始化一个producer并设置Producer group name
        DefaultMQProducer producer = new DefaultMQProducer("syncProducer");
        // 设置NameServer地址
        producer.setNamesrvAddr(Constant.NAME_SERVER);
        // 启动producer
        producer.start();
        for (int i = 0; i < 2; i++) {
            // 创建一条消息，并指定topic、tag、body等信息，tag可以理解成标签，对消息进行再归类，RocketMQ可以在消费端对tag进行过滤
            Message msg = new Message("commonMsg",
                    "TagA",
                    ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET)
            );
            // 利用producer进行发送，并同步等待发送结果
            SendResult sendResult = producer.send(msg);
            System.out.printf("%s%n", sendResult);
        }
        // 一旦producer不再使用，关闭producer
        producer.shutdown();
    }
}
