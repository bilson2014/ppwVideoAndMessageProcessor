package com.paipianwang.mq.consumer.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paipianwang.mq.consumer.dao.MailDao;
import com.paipianwang.mq.consumer.resource.model.MailParam;
import com.paipianwang.mq.consumer.service.MailService;
import com.paipianwang.mq.consumer.service.ProjectTopicMailService;
import com.paipianwang.mq.utils.MailTemplateFactory;
import com.paipianwang.pat.common.util.ValidateUtil;
import com.paipianwang.pat.common.util.sms.DateUtil;
import com.paipianwang.pat.facade.information.entity.PmsMail;
import com.paipianwang.pat.facade.information.service.PmsMailFacade;
import com.paipianwang.pat.facade.right.entity.PmsEmployee;
import com.paipianwang.pat.facade.right.service.PmsEmployeeFacade;
import com.paipianwang.pat.facade.team.entity.PmsTeam;
import com.paipianwang.pat.facade.team.service.PmsTeamFacade;
import com.paipianwang.pat.facade.user.entity.PmsUser;
import com.paipianwang.pat.facade.user.service.PmsUserFacade;
import com.paipianwang.pat.workflow.entity.PmsProjectFlow;
import com.paipianwang.pat.workflow.entity.PmsProjectMessage;
import com.paipianwang.pat.workflow.entity.PmsProjectTeam;
import com.paipianwang.pat.workflow.entity.PmsProjectUser;
import com.paipianwang.pat.workflow.facade.PmsProjectFlowFacade;
import com.paipianwang.pat.workflow.facade.PmsProjectMessageFacade;
import com.paipianwang.pat.workflow.facade.PmsProjectTeamFacade;
import com.paipianwang.pat.workflow.facade.PmsProjectUserFacade;

/**
 * 留言回复
 */
@Service
public class ProjectTopicMailServiceImpl implements ProjectTopicMailService{
	private static final Logger logger = LoggerFactory.getLogger(ProjectTopicMailServiceImpl.class);

	@Autowired
	private final MailDao mailDao = null;
	@Autowired
	private final PmsMailFacade pmsMailFacade = null;
	@Autowired
	private final PmsProjectMessageFacade pmsProjectMessageFacade=null;
	@Autowired
	private final PmsEmployeeFacade pmsEmployeeFacade=null;
	@Autowired
	private final PmsProjectFlowFacade pmsProjectFlowFacade=null;
	@Autowired
	private MailService mailService=null;
	@Autowired
	private final PmsTeamFacade pmsTeamFacade=null;
	@Autowired
	private final PmsUserFacade pmsUserFacade=null;
	@Autowired
	private final PmsProjectTeamFacade pmsProjectTeamFacade=null;
	@Autowired
	private final PmsProjectUserFacade pmsProjectUserFacade=null;
	
	/**
	 * 留言新回复通知邮件发送
	 */
	@Override
	public void sendTopicReplyInformEmail(String messageId) {
		String mailType="topicReplyInform";
		PmsMail m = mailDao.getMailFromRedis(mailType);
		if (null == m){
			m = pmsMailFacade.getTemplateByType(mailType);
		}
		
		PmsProjectMessage currentMessage=pmsProjectMessageFacade.getById(Long.parseLong(messageId));
		if(currentMessage.getParentId()==null){
			return ;
		}
		List<PmsProjectMessage> relativeMessage=pmsProjectMessageFacade.getRelativeMessage(currentMessage.getParentId());
		//组装 邮件接收人
		Set<String> sendToId=new HashSet<String>();
		for(PmsProjectMessage message:relativeMessage){
			sendToId.add(message.getFromId());
		}
		sendToId.remove(currentMessage.getFromId());
		if(!ValidateUtil.isValid(sendToId)){
			return ;
		}
		//组装邮件内容
		String content=m.getContent();
		String subject=m.getSubject();
		
		//转码
		try {
			content = new String(Base64.getDecoder().decode(content),"utf-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(" content decode error ");
			return ;
		}
		
		List<String> metaData = new ArrayList<String>();
		metaData.add("projectId");
		metaData.add("projectName");

		PmsProjectFlow flow = pmsProjectFlowFacade.getProjectFlowByProjectId(metaData, currentMessage.getProjectId());
		String[] values=new String[5];
		values[1]=flow.getProjectName();
		
		values[2]=currentMessage.getCreateDate();
		values[3]=currentMessage.getFromName()+":"+currentMessage.getContent();
		values[4]="http://www.apaipian.com/project/running";
		
		
		
		subject=MailTemplateFactory.decorate(new String[]{flow.getProjectName()}, subject);
		
		for(String each:sendToId){
			MailParam mail =new MailParam();
			//获取接收人
			if(each.startsWith("employee_")){
				PmsEmployee employee=pmsEmployeeFacade.findEmployeeById(Long.parseLong(each.split("employee_")[1]));
				values[0]=employee.getEmployeeRealName();
				mail.setTo(employee.getEmail());
			}else if(each.startsWith("user_")){
				PmsUser user=pmsUserFacade.findUserById(Long.parseLong(each.split("user_")[1]));
				mail.setTo(user.getEmail());
				PmsProjectUser pu=pmsProjectUserFacade.getProjectUserById(Long.parseLong(each.split("user_")[1]));
				values[0]=pu.getLinkman();
			}else if(each.startsWith("team_")){
				PmsTeam team=pmsTeamFacade.getTeamInfo(Long.parseLong(each.split("team_")[1]));
				mail.setTo(team.getEmail());
				PmsProjectTeam pt=pmsProjectTeamFacade.getProjectTeamByProjectTeamId(Long.parseLong(each.split("team_")[1]));
				values[0]=pt.getLinkman();
			}
			//邮件发送
			content=MailTemplateFactory.decorate(values,content);	
			mail.setContent(content);
			mail.setSubject(subject);
			mailService.sendMail(mail);
		}
	}

}
