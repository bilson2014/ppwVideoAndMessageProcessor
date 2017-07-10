package com.paipianwang.mq.utils;

import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import com.paipianwang.mq.consumer.domain.GlobalConstants;
import com.paipianwang.sms.utils.CCPRestSmsSDK;

@Component
public class SmsClient implements InitializingBean{

	private final static Logger logger = LoggerFactory.getLogger(SmsClient.class);
	
	// 通信地址
	private static String SMS_HTTPS_URL = GlobalConstants.SMS_HTTPS_URL;
	// 通信端口
	private static String SMS_HTTPS_PORT = GlobalConstants.SMS_HTTPS_PORT;
	// ACOUNT SID
	private static String SMS_ACOUNT_SID = GlobalConstants.SMS_ACCOUNTSID;
	// AUTH TOKEN
	private static String SMS_AUTH_TOKEN = GlobalConstants.SMS_AUTHTOKEN;
	// APPID
	private static String SMS_APP_ID = GlobalConstants.SMS_APPID;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		SMS_HTTPS_URL = GlobalConstants.SMS_HTTPS_URL;
		SMS_HTTPS_PORT = GlobalConstants.SMS_HTTPS_PORT;
		SMS_ACOUNT_SID = GlobalConstants.SMS_ACCOUNTSID;
		SMS_AUTH_TOKEN = GlobalConstants.SMS_AUTHTOKEN;
		SMS_APP_ID = GlobalConstants.SMS_APPID;
		
	}
	
	public boolean sendMessage(final String templateId ,final String telephone, final String[] content, final String logId) {
		
		HashMap<String, Object> result = null;
		
		//初始化SDK
		CCPRestSmsSDK restAPI = new CCPRestSmsSDK();
		restAPI.init(SMS_HTTPS_URL, SMS_HTTPS_PORT);
		restAPI.setAccount(SMS_ACOUNT_SID, SMS_AUTH_TOKEN);
		restAPI.setAppId(SMS_APP_ID);
		
		// 调用短信接口发送短信
		result = restAPI.sendTemplateSMS(telephone,templateId,content);
		if(content == null) {
			logger.info("Send message(sendMessage)[" + logId + "]" + " to " + telephone + " , content: [ null ] , result: [" + result + "]");
		} else {
			logger.info("Send message(sendMessage)[" + logId + "]" + " to " + telephone + " , content: [" + content == null ? "null content" : content.toString() + "] , result: [" + result + "]");
		}
		
		if("000000".equals(result.get("statusCode"))){
			//正常返回输出data包体信息(map)
			@SuppressWarnings("unchecked")
			HashMap<String,Object> data = (HashMap<String, Object>) result.get("data");
			Set<String> keySet = data.keySet();
			for(String key:keySet){
				Object object = data.get(key);
				logger.info("Send message(sendMessage)[" + logId + "]" + " info : " + object);
			}
			return true;
		}else{
			//异常返回输出错误码和错误信息
			logger.error("Send message(sendMessage)[" + logId + "]" + " error_code: " + result.get("statusCode") +" error_info: "+result.get("statusMsg"));
		}
		
		return false;
	}

}
