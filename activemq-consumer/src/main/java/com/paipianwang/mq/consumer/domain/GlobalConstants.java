package com.paipianwang.mq.consumer.domain;

import java.io.Serializable;

public class GlobalConstants implements Serializable {

	private static final long serialVersionUID = 567029541880959751L;
	
	public static String SMS_ACCOUNTSID; // 短信平台账户
	public static String SMS_AUTHTOKEN; // 短信平台token
	public static String SMS_APPID; // 短信平台应用ID
	public static String SMS_HTTPS_URL; // 短信平台通信地址
	public static String SMS_HTTPS_PORT; // 短信平台通信端口
	public static String SMS_TEMPLATE_REGISTER; // 用户注册模板
	public static String SMS_TEMPLATE_NOTICE; // 用户通知模板

	private String accountSID = "8a48b5514abd771c014abdf8fc340061";
	private String auth_token = "15e8b1c0b32d44fbb402a36877c43f50";
	private String appId = "8a48b5514b0b8727014b2a4c94041bf0";
	private String https_url = "app.cloopen.com";
	private String https_port = "8883";
	private String template_register = "register";
	private String template_notice = "notes";

	public GlobalConstants(String accountSID, String auth_token, String appId, String https_url, String https_port,
			String template_register, String template_notice) {
		SMS_ACCOUNTSID = accountSID;
		SMS_AUTHTOKEN = auth_token;
		SMS_APPID = appId;
		SMS_HTTPS_URL = https_url;
		SMS_HTTPS_PORT = https_port;
		SMS_TEMPLATE_REGISTER = template_register;
		SMS_TEMPLATE_NOTICE = template_notice;
	}

	public String getAccountSID() {
		return accountSID;
	}

	public void setAccountSID(String accountSID) {
		this.accountSID = accountSID;
	}

	public String getAuth_token() {
		return auth_token;
	}

	public void setAuth_token(String auth_token) {
		this.auth_token = auth_token;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getHttps_url() {
		return https_url;
	}

	public void setHttps_url(String https_url) {
		this.https_url = https_url;
	}

	public String getHttps_port() {
		return https_port;
	}

	public void setHttps_port(String https_port) {
		this.https_port = https_port;
	}

	public String getTemplate_register() {
		return template_register;
	}

	public void setTemplate_register(String template_register) {
		this.template_register = template_register;
	}

	public String getTemplate_notice() {
		return template_notice;
	}

	public void setTemplate_notice(String template_notice) {
		this.template_notice = template_notice;
	}

}
