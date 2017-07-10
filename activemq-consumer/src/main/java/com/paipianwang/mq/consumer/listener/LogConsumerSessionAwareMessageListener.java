package com.paipianwang.mq.consumer.listener;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class LogConsumerSessionAwareMessageListener implements SessionAwareMessageListener<Message> {

	private static final Log log = LogFactory.getLog(LogConsumerSessionAwareMessageListener.class);

	@Autowired
	private JmsTemplate activeMqJmsTemplate;
	@Autowired
	private Destination logQueue;
	@Autowired
	private MailService service;

	public synchronized void onMessage(Message message, Session session) {
		try {
			ActiveMQTextMessage msg = (ActiveMQTextMessage) message;
			final String ms = msg.getText();
			log.info("==>receive messageA:" + ms);
			MailParam mailParam = JSONObject.parseObject(ms, MailParam.class);// 转换成相应的对象
			if (mailParam == null) {
				return;
			}
			mailParam.setFrom("itliucheng@126.com");
			
			try {
				service.sendMail(mailParam);
				Thread.sleep(100);
			} catch (Exception e) {
				// 发送异常，重新放回队列
				activeMqJmsTemplate.send(logQueue, new MessageCreator() {
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(ms);
					}
				});
				log.error("==>MailException:", e);
			}
		} catch (Exception e) {
			log.error("==>", e);
		}
	}
}
