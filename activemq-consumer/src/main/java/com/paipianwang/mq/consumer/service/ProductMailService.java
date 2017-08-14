package com.paipianwang.mq.consumer.service;

/**
 * 项目流程自动邮件发送通道
 */
public interface ProductMailService {

	/**
	 * 发送项目告知函
	 * @param projectId
	 */
	public void sendProjectInfoLetter(String projectId);
	
	/**
	 * 发送项目策划启动函
	 * @param projectId
	 */
	public void sendProjectPlanStart(String projectId);
	
	/**
	 * 发送项目确认启动函
	 * @param projectId
	 */
	public void sendProjectConfirmStart(String projectId);
	
	/**
	 * 发送项目制作启动函
	 * @param projectId
	 */
	public void sendProjectMakingList(String projectId);
	
	/**
	 * 发送项目样片修改意见
	 * @param projectId
	 */
	public void sendProjectSampleMIdea(String projectId);
	
	/**
	 * 发送项目验收确认函
	 * @param projectId
	 */
	public void sendProjectAcceptConfirm(String projectId);
	
	/**
	 * 发送项目验收函
	 * @param projectId
	 */
	public void sendProjectAcceptLetter(String projectId);
	
}
