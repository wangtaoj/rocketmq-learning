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
     *
     * 生产者发送延迟消息时，其实只是发送到一个内部名为SCHEDULE_TOPIC_XXXX的主题中(到达broker后，由broker来替换的)，
     * 这个主题有18个队列(分区)，刚好对应延迟消息的18个等级，后台定时任务会扫描这些队列，判断它有没有到期，若到期则投递
     * 到真正的主题中。
     * 注意:
     * 多master broker集群，如brokera、brokerb、brokerc每个都会有18个队列，每个 Broker 都维护自己的延迟队列
     * 不能设计成共享的，因为消息本来就是按照主题分布在多个队列中，如果消息本身路由到brokerb中，延迟等级设置为1，如果是
     * 共享的，brokera(1-6)，brokerb(7-12), brokerc(13-18)，此时brokerb就没有延迟等级为1的队列了，没法进行投递。
     *
     * 如何判断是否到期:
     * consumequeue文件每行为20个字节，commitLogOffset(8)、messageSize(4)、tagHashCode(8)
     * 本来tagHashCode只需要4个字节(java的hashcode方法返回int)，就是因为延迟消息使用了tagHashCode这个部分来
     * 存储消息到期时间戳，所以给这部分设计成了8个字节。
     *
     * 定时任务行为:
     * 1. 每个队列都对应一个定时任务
     * 2. 同一延迟等级都发往了同一个队列，这样队列中的消息就是天然有序的(取决于到达broker的时间)，定时任务只需要扫描队头的元素有没有到期。
     * 若没有到期，则会计算时间直接睡眠消息到期时再执行，而不是周期性扫描(每秒一次)，因此CPU开销会很低。
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
