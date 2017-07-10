package com.paipianwang.mq.consumer.service.impl;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.paipianwang.mq.consumer.resource.model.MailParam;
import com.paipianwang.mq.consumer.service.MailService;

@Service
public class MailServiceImpl implements MailService {

	@Resource
	private JavaMailSenderImpl javaMailSender;// spring配置中定义

	@Autowired
	private ThreadPoolTaskExecutor threadPool;
	
	@Override
	public void sendMail(final MailParam mail) {
		threadPool.execute(new Runnable() {
			public void run() {
				try {
					final MimeMessage mailMessage = javaMailSender.createMimeMessage();
					MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true, "utf-8");
					messageHelper.setFrom(javaMailSender.getUsername()); // 发送人,从配置文件中取得
					messageHelper.setTo(mail.getTo()); // 接收人
					messageHelper.setSubject(mail.getSubject());
					messageHelper.setText(mail.getContent(), true);
					javaMailSender.send(mailMessage);
				} catch (MailException e) {
					throw e;
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

}
