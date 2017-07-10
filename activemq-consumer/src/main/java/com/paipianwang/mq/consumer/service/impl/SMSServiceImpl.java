package com.paipianwang.mq.consumer.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paipianwang.mq.consumer.resource.model.SMSParam;
import com.paipianwang.mq.consumer.service.SMSService;
import com.paipianwang.mq.utils.SmsClient;

@Service
public class SMSServiceImpl implements SMSService {

	@Autowired
	private final SmsClient smsClient = null;
	
	private final String logId = UUID.randomUUID().toString();
	
	@Override
	public boolean sendMessage(SMSParam sms) {
		final boolean result = smsClient.sendMessage(sms.getSmsTemplateID(),sms.getTelephone(), sms.getContent(), logId);
		return result;
	}
	

}
