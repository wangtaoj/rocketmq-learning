package com.wangtao.rocketmq.spring.controller;

import com.wangtao.rocketmq.spring.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.MessageConst;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 同一个消费者组下的消费者务必订阅同一个topic、tag, 否则存在消失丢失情况
 * 默认是集群消费, 且后面启动的消费者会更新订阅信息(topic、tag), 意味着前一个消费者订阅会失效。
 * 比如消费者组存在消费者A1、消费者A2
 * 分别订阅topic A, tag a1; topic A, tag a2;
 * 如果消费者A2后启动, 其实消费者A1以及A2订阅的都是topic A, tag a2
 * 这将导致tag a1的消息永远不会消费, tag a2只能被消费者A2消费, 如果被分配到
 * 消费者A1, 由于消费者A1进行了tag过滤, 也不会消费tag a2的消息
 * @author wangtao
 * Created at 2023/3/28 20:24
 */
@Slf4j
@RequestMapping("/msg")
@RestController
public class MsgController {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @GetMapping("/create/{msgNo}")
    public void create(@PathVariable String msgNo) {
        // commonMsg:tagA (使用冒号分割, 若要指定tag)
        String topic = "commonMsg:tagA";
        Message<String> msg = MessageBuilder.withPayload("Hello RocketMQ " + msgNo)
                .build();
        SendResult result = rocketMQTemplate.syncSend(topic, msg);
        log.info("{}", result);
        // rocketMQTemplate.convertAndSend(topic, "Hello RocketMQ " + msgNo, );
    }

    @GetMapping("/createWithHead1/{msgNo}")
    public void createWithHead1(@PathVariable String msgNo) {
        String topic = "commonMsg:tagB";
        Message<String> msg = MessageBuilder.withPayload("Hello RocketMQ")
                .setHeader(MessageConst.PROPERTY_KEYS, msgNo)
                .setHeader("customHeader", "customHeader")
                .build();
        SendResult result = rocketMQTemplate.syncSend(topic, msg);
        log.info("{}", result);
    }

    @GetMapping("/createWithHead2/{msgNo}")
    public void createWithHead2(@PathVariable String msgNo) {
        String topic = "commonMsg:tagB";
        Map<String, Object> headers = new HashMap<>();
        headers.put(MessageConst.PROPERTY_KEYS, msgNo);
        headers.put("customHeader", "customHeader");
        rocketMQTemplate.convertAndSend(topic, "Hello RocketMQ " + msgNo, headers);
    }

    @GetMapping("/createObj/{userId}")
    public void createObj(@PathVariable Long userId) {
        UserVO userVO = new UserVO(userId, "user-" + userId);
        rocketMQTemplate.convertAndSend("objMsg:tagA", userVO);
    }
}
