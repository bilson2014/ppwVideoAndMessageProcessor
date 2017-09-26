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
import com.paipianwang.mq.consumer.service.ProductMailService;
import com.paipianwang.mq.consumer.service.ProjectTopicMailService;
import com.paipianwang.pat.common.util.ValidateUtil;
/**
 * 添加留言回复邮件通知
 */
@Component
public class TopicReplyInformMailMessageListener implements SessionAwareMessageListener<Message>{
	private static final Logger logger = LoggerFactory.getLogger(TopicReplyInformMailMessageListener.class);
	
	@Autowired
	private JmsTemplate activeMqJmsTemplate;
	@Autowired
	private Destination topicReplyInformEmailQueue;
	@Autowired
	private ProjectTopicMailService ProjectTopicMailService;
	
	@Override
	public void onMessage(Message message, Session session) throws JMSException {

		try {
			ActiveMQTextMessage msg = (ActiveMQTextMessage) message;
			final String ms = msg.getText();
			logger.info("Receive topicReplyInformMail message (onMessage) , content: " + ms);
			if(!ValidateUtil.isValid(ms)){
				return ;
			}
			JSONObject json=JSONObject.parseObject(ms);
			String messageId=json.getString("messageId");
			Integer time=json.getInteger("time");//初始为0
			try {
				ProjectTopicMailService.sendTopicReplyInformEmail(messageId);
				Thread.sleep(200);
			} catch (Exception e) {
				// 发送异常，重新放回队列-最多尝试3次
				if(time<=2){
					time++;
					json.put("time", time);
					activeMqJmsTemplate.send(topicReplyInformEmailQueue,new MessageCreator() {
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(json.toString());
						}
					});
					logger.info("repeat send message ");
				}
				logger.error("==>MailException:", e);
			}
		} catch (Exception e) {
			logger.error("==>", e);
		}	
	}

}
