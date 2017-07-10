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
import com.paipianwang.mq.consumer.resource.model.MailParam;
import com.paipianwang.mq.consumer.service.MailService;

/**
 * 队列监听器
 * @author Jack
 *
 */

@Component
public class MailConsumerSessionAwareMessageListener implements SessionAwareMessageListener<Message> {

	private static final Logger logger = LoggerFactory.getLogger(MailConsumerSessionAwareMessageListener.class);

	@Autowired
	private JmsTemplate activeMqJmsTemplate;
	@Autowired
	private Destination mailQueue;
	@Autowired
	private MailService service;

	public synchronized void onMessage(Message message, Session session) {
		try {
			ActiveMQTextMessage msg = (ActiveMQTextMessage) message;
			final String ms = msg.getText();
			logger.info("Receive register message (onMessage) , content: " + ms);
			MailParam mail = JSONObject.parseObject(ms, MailParam.class);// 转换成相应的对象
			if (mail == null) {
				return;
			}
			
			try {
				service.sendMail(mail);
				Thread.sleep(200);
			} catch (Exception e) {
				// 发送异常，重新放回队列
				activeMqJmsTemplate.send(mailQueue, new MessageCreator() {
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(ms);
					}
				});
				logger.error("==>MailException:", e);
			}
		} catch (Exception e) {
			logger.error("==>", e);
		}
	}
}
