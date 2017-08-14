package com.paipianwang.mq.consumer.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.paipianwang.mq.consumer.resource.model.MailParam;
import com.paipianwang.mq.consumer.service.MailService;
import com.paipianwang.pat.common.util.ValidateUtil;
import com.paipianwang.pat.common.web.file.FastDFSClient;

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
				List<String> tempFileList=new ArrayList<>();
				try {
					final MimeMessage mailMessage = javaMailSender.createMimeMessage();
					MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true, "utf-8");
					//发送人-默认取系统配置
					messageHelper.setFrom(ValidateUtil.isValid(mail.getFrom())?mail.getFrom():javaMailSender.getUsername()); // 发送人,从配置文件中取得
					messageHelper.setTo(mail.getTo()); // 接收人
					messageHelper.setSubject(mail.getSubject());
					messageHelper.setText(mail.getContent(), true);
					//抄送人
					if(mail.getCc()!=null){
						messageHelper.setCc(mail.getCc());	
					}
					//密件抄送人
					if(mail.getBcc()!=null){
						messageHelper.setBcc(mail.getBcc());
					}
					//附件
					if(mail.getFiles()!=null && mail.getFiles().length>0){
						for(String[] file:mail.getFiles()){	
							if("temp".equals(file[2])){
								//本地临时生成文件
								messageHelper.addAttachment(file[0], new File(file[1]));
								tempFileList.add(file[1]);
							}else if("template".equals(file[2])){
								//本地模板文件
								messageHelper.addAttachment(file[0], new File(file[1]));
							}else{
								InputStream in=FastDFSClient.downloadFile(file[1]);
								if(in!=null){
									messageHelper.addAttachment(file[0],
										    new ByteArrayResource(IOUtils.toByteArray(in)));
								}	
							}
								
						}
					}
					
					javaMailSender.send(mailMessage);
				} catch (MailException e) {
					throw e;
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch(Exception ee){
					ee.printStackTrace();
				}finally{
					//删除临时生成文件
					for(String tempFile:tempFileList){
						File file=new File(tempFile);
						file.delete();
					}
				}
			}
		});

	}

}
