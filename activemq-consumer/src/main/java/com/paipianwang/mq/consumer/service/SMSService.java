package com.paipianwang.mq.consumer.service;

import com.paipianwang.mq.consumer.resource.model.SMSParam;

public interface SMSService {

	public boolean sendMessage(final SMSParam sms);
}
