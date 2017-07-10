package com.paipianwang.mq.consumer.service;

import com.paipianwang.mq.consumer.resource.model.MailParam;

public interface MailService {

	public void sendMail(final MailParam param);
}
