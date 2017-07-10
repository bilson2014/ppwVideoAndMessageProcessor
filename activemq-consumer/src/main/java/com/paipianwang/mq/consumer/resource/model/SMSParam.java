package com.paipianwang.mq.consumer.resource.model;

import com.paipianwang.mq.consumer.domain.BaseObject;

public class SMSParam extends BaseObject {

	private static final long serialVersionUID = -1260327667384125424L;

	// 短信内容
	private String[] content = null; 
	
	// 电话号码
	private String telephone = null; 
	
	// 短信模版
	private String smsTemplateID = null;

	public String[] getContent() {
		return content;
	}

	public void setContent(String[] content) {
		this.content = content;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getSmsTemplateID() {
		return smsTemplateID;
	}

	public void setSmsTemplateID(String smsTemplateID) {
		this.smsTemplateID = smsTemplateID;
	}

}
