package com.paipianwang.mq.consumer.listener;

import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;

import com.alibaba.fastjson.JSONObject;
import com.paipianwang.facade.video.service.VideoHandlerFacade;
import com.paipianwang.mq.consumer.resource.model.FileParam;
import com.paipianwang.pat.facade.product.service.PmsProductFacade;

/**
 * 队列监听器
 * @author Jack
 *
 */
@Deprecated
//@Component
public class FileConverConsumerSessionAwareMessageListener implements SessionAwareMessageListener<Message> {

	private static final Log log = LogFactory.getLog(FileConverConsumerSessionAwareMessageListener.class);

	@Autowired
	private final VideoHandlerFacade videoHandlerFacade = null;
	@Autowired
	private final PmsProductFacade pmsProductFacade =null;

	public synchronized void onMessage(Message message, Session session) {
		try {
			ActiveMQTextMessage msg = (ActiveMQTextMessage) message;
			final String ms = msg.getText();
			log.info("==>receive file convert message:" + ms);
			FileParam fileParam = JSONObject.parseObject(ms, FileParam.class);// 转换成相应的对象
			if (fileParam == null) 
				return ;
			
			if(StringUtils.isBlank(fileParam.getConvertFileId()) || fileParam.getProductId() == 0) 
				return ;
			
			try {
				// 文件转换
				final String targetFileId = videoHandlerFacade.converFile2Normal(fileParam.getConvertFileId());
				if(StringUtils.isNotBlank(targetFileId)) {
					// 如果文件转换之后的路径为null，那么说明转换异常或者失败
					// 返回fileId不为空，则说明转换成功
					pmsProductFacade.updateFileConvertInfo(targetFileId, fileParam.getProductId());
					System.out.println("File ID is " + fileParam.getProductId() + " ,转换原路径为: " + fileParam.getConvertFileId() + " , 转换之后的路径为" + targetFileId + " ，转换完成 ...");
				}
				Thread.sleep(400);
			} catch (Exception e) {
				// 发送异常，重新放回队列
				/*activeMqJmsTemplate.send(fileConverQueue, new MessageCreator() {
					public Message createMessage(Session session) throws JMSException {
						return session.createTextMessage(ms);
					}
				});*/
				log.error("==> File Convert Error, File ID is" + fileParam.getProductId() + " ,转换原路径为: " + fileParam.getConvertFileId());
				log.error("==>FileConvertException:", e);
			}
		} catch (Exception e) {
			log.error("==>", e);
		}
	}
}
