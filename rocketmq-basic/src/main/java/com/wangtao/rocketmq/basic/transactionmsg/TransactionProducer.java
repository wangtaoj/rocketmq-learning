package com.wangtao.rocketmq.basic.transactionmsg;

import com.wangtao.rocketmq.basic.Constant;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 事务消息发送
 *
 * 事务消息采用二阶段提交方式，结合本地事务执行器来保证消息发送与本地事务的最终一致性：
 * 1. 生产者向Broker发送半消息（Half Message），此时消息对消费者不可见
 * 2. Broker存储半消息后向生产者返回确认，生产者开始执行本地事务
 * 3. 根据本地事务执行结果，生产者向Broker提交二次确认（Commit或Rollback）：
 *    - COMMIT：Broker将半消息投递给消费者
 *    - ROLLBACK：消费者不会收到该消息
 *    - UNKNOW：本地事务状态未知，等待Broker消息回查
 * 4. 如果Broker长时间未收到二次确认（如生产者宕机），会主动回查本地事务状态，
 *    生产者需要实现checkLocalTransaction方法来返回本地事务状态
 *
 * 5. 消息回查机制说明
 *    - 消息回查频率：默认值60s，由参数transactionCheckInterval=60000控制
 *    - 消息回查最大次数：默认值15，由参数transactionCheckMax=15控制
 *    - transactionCheckInterval以及transactionCheckMax参数需要在Broker的broker.conf文件配置
 *    - 生产者端，可以单独对消息设置首次回查的免疫时间：
 *      msg.putUserProperty(MessageConst.PROPERTY_CHECK_IMMUNITY_TIME_IN_SECONDS, "15");
 *      默认15秒，半消息发送后多久才开始回查，这个值决定 Broker从发送半消息起等待多长时间才开始第一次回查。
 *
 * 6. RMQ_SYS_TRANS_HALF_TOPIC：半消息主题，生产者发送事务消息时，Broker会先将消息存入该主题，此时不会被消费者消费。
 *    RMQ_SYS_TRANS_OP_HALF_TOPIC：操作记录主题，无论事务最终是提交 (Commit) 还是回滚 (Rollback)
 *    Broker都会在此记录一个OP消息（包含原消息的物理偏移量），用于标识该半消息已被处理。
 *    后台的回查服务会扫描半消息主题，并利用操作记录主题过滤掉已处理的消息
 *
 * @author wangtao
 * Created at 2023/8/16 20:59
 */
public class TransactionProducer {

    public static void main(String[] args) throws Exception {
        TransactionMQProducer producer = new TransactionMQProducer("transaction_producer");
        producer.setNamesrvAddr(Constant.NAME_SERVER);
        ExecutorService executorService = new ThreadPoolExecutor(2, 5, 100, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("client-transaction-msg-check-thread");
                return thread;
            }
        });
        // 设置事务回查线程池, 默认的线程池是一个单线程
        producer.setExecutorService(executorService);
        // 设置本地事务执行器
        producer.setTransactionListener(new TransactionListenerImpl());
        producer.start();

        for (int i = 0; i < 3; i++) {
            Message msg = new Message("transactionMsg",
                    ("事务消息 " + i).getBytes(RemotingHelper.DEFAULT_CHARSET)
            );
            // 发送事务消息
            TransactionSendResult sendResult = producer.sendMessageInTransaction(msg, i);
            System.out.printf("发送结果: %s, 本地事务状态: %s%n", sendResult.getSendStatus(), sendResult.getLocalTransactionState());
        }
        // 服务端会向生产者进行消息回查，不要关闭
        // producer.shutdown();
    }
}