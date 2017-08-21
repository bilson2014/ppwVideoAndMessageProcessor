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

import com.paipianwang.mq.consumer.service.ProductMailService;
import com.paipianwang.pat.common.util.ValidateUtil;
@Component
public class ProjectAcceptLetterMailMessageListener implements SessionAwareMessageListener<Message> {
	private static final Logger logger = LoggerFactory.getLogger(ProjectAcceptLetterMailMessageListener.class);
	
	@Autowired
	private JmsTemplate activeMqJmsTemplate;
	@Autowired
	private Destination mailQueue;
	@Autowired
	private ProductMailService productMailService;
	
	@Override
	public void onMessage(Message message, Session session) throws JMSException {
		try {
			ActiveMQTextMessage msg = (ActiveMQTextMessage) message;
			final String ms = msg.getText();
			logger.info("Receive projectAcceptLetter message (onMessage) , content: " + ms);
			if(!ValidateUtil.isValid(ms)){
				return ;
			}
			
			try {
				productMailService.sendProjectAcceptLetter(ms);;
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
