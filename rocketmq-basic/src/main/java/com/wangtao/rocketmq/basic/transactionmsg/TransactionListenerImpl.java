package com.wangtao.rocketmq.basic.transactionmsg;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.nio.charset.StandardCharsets;

/**
 * 事务消息监听器，实现本地事务执行和回查逻辑
 *
 * executeLocalTransaction: 在半消息发送成功后被回调，执行本地事务逻辑，
 *                          根据本地事务执行结果返回Commit、Rollback或Unknown
 * checkLocalTransaction:   当Broker长时间未收到二次确认时回调，用于回查本地事务状态，
 *                          需要根据本地事务记录返回对应的状态
 *
 * @author wangtao
 * Created at 2023/8/16 20:59
 */
public class TransactionListenerImpl implements TransactionListener {

    /**
     * 执行本地事务
     * 这里模拟本地事务逻辑：根据消息内容的索引决定事务状态
     * - 0: COMMIT（本地事务成功，消息投递给消费者）
     * - 1: ROLLBACK（本地事务失败，消息丢弃）
     * - 2: UNKNOWN（本地事务状态未知，Broker会回查）
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        int index = (int) arg;
        switch (index) {
            case 0:
                System.out.println("本地事务执行成功，提交消息");
                return LocalTransactionState.COMMIT_MESSAGE;
            case 1:
                System.out.println("本地事务执行失败，回滚消息");
                return LocalTransactionState.ROLLBACK_MESSAGE;
            default:
                System.out.println("本地事务状态未知，等待回查");
                return LocalTransactionState.UNKNOW;
        }
    }

    /**
     * 回查本地事务状态
     * 当Broker未收到二次确认时，会主动回查此方法来获取本地事务的最终状态。
     * 实际生产中应查询本地事务表来判断事务是否执行成功。
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        System.out.printf("回查本地事务状态, msg=%s", new String(msg.getBody(), StandardCharsets.UTF_8));
        // 实际生产中应查询本地事务表，这里简单返回COMMIT
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}