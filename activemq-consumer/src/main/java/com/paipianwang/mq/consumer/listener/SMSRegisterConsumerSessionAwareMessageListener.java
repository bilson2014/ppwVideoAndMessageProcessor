package com.paipianwang.mq.consumer.listener;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.paipianwang.mq.consumer.resource.model.SMSParam;
import com.paipianwang.mq.consumer.service.SMSService;

/**
 * 注册短信队列监听器
 * @author Jack
 *
 */

@Component
public class SMSRegisterConsumerSessionAwareMessageListener implements SessionAwareMessageListener<Message> {

	private static final Logger logger = LoggerFactory.getLogger(SMSRegisterConsumerSessionAwareMessageListener.class);

	@Autowired
	private JmsTemplate activeMqJmsTemplate;
	@Autowired
	private Destination smsQueue;
	@Autowired
	private final SMSService smsService = null;
	
	public synchronized void onMessage(Message message, Session session) {
		try {
			ActiveMQTextMessage msg = (ActiveMQTextMessage) message;
			final String ms = msg.getText();
			logger.info("Receive register message (onMessage) , content: " + ms);
			SMSParam param = JSONObject.parseObject(ms, SMSParam.class);// 转换成相应的对象
			
			if (param == null || "".equals(param.getTelephone()) || param.getTelephone() == null) {
				return;
			}
			
			try {
				smsService.sendMessage(param);
				Thread.sleep(200);
			} catch (Exception e) {
				// 发送异常，重新放回队列
				activeMqJmsTemplate.send(smsQueue, new MessageCreator() {
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(ms);
					}
				});
				logger.error("==>SMSException:", e);
			}
		} catch (Exception e) {
			logger.error("==>", e);
		}
	}
}
