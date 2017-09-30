package com.paipianwang.mq.consumer.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
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
import com.paipianwang.pat.common.config.PublicConfig;
import com.paipianwang.pat.common.util.DateUtils;
import com.paipianwang.pat.common.util.ValidateUtil;
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
	private final PmsUserFacade pmsUserFacade=null;
	@Autowired
	private final PmsProjectUserFacade pmsProjectUserFacade=null;
//	@Autowired
//	private final PmsTeamFacade pmsTeamFacade=null;
//	@Autowired
//	private final PmsProjectTeamFacade pmsProjectTeamFacade=null;
	
	
	
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
		//邮件内容
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
		//组装邮件内容
		String[] values=new String[8];
		values[1]=flow.getProjectName();
		values[2]=DateUtils.getDateByFormatStr(DateUtils.getDateByFormat(currentMessage.getCreateDate(), "yyyy-MM-dd HH:mm:ss"), "yyyy年MM月dd日HH时");
		values[7]="<a href='http://"+PublicConfig.FILM_URL+"/mgr/login' style='font-family:.PingFang-SC-Regular;font-size:14px;color:#fe5453;'>"
				+ "http://"+PublicConfig.FILM_URL+"/mgr/login</a> ";
		
		String reply="";
		StringBuilder replyResult=new StringBuilder();
		if(content.contains("{begin-1}")&&content.contains("{end-1}")){
			reply=content.substring(content.indexOf("{begin-1}")+"{begin-1}".length(), content.indexOf("{end-1}"));
		}
		
		String topicSender="";
		for(PmsProjectMessage message:relativeMessage){
			if(message.getParentId()==null){
				//topic
				topicSender=message.getFromId();
				values[4]=message.getFromName();
				values[5]=message.getContent();
				values[6]=message.getCreateDate();
			}else{
				//处理reply
				String[] replyValue=new String[3];
				replyValue[0]=message.getFromName();
				replyValue[1]=message.getContent();
				replyValue[2]=message.getCreateDate();
				replyResult.append(MailTemplateFactory.decorate(replyValue,reply));
			}
		}
		
		content=content.replace("{begin-1}"+reply+"{end-1}", replyResult.toString());
			
		subject=MailTemplateFactory.decorate(new String[]{flow.getProjectName()}, subject);
		String defaultPic="group1/M00/00/AF/Cgpw7FnPS-2AM5NKAAAGk0kkEz8002.png";//默认头像
		for(String each:sendToId){
			String pic=defaultPic;
			MailParam mail =new MailParam();
			//获取接收人
			if(each.startsWith("employee_")){
				PmsEmployee employee=pmsEmployeeFacade.findEmployeeById(Long.parseLong(each.split("employee_")[1]));
				values[0]=employee.getEmployeeRealName();
				mail.setTo(employee.getEmail());
				if(each.equals(topicSender)){
					if(ValidateUtil.isValid(employee.getEmployeeImg())){
						pic=employee.getEmployeeImg();//topic图片	
					}
//					values[3]="http://123.59.86.252:8888/"+employee.getEmployeeImg();//topic图片	
				}
			}else if(each.startsWith("user_")){
				PmsUser user=pmsUserFacade.findUserById(Long.parseLong(each.split("user_")[1]));
				mail.setTo(user.getEmail());
				PmsProjectUser pu=pmsProjectUserFacade.getProjectUserById(Long.parseLong(each.split("user_")[1]));
				values[0]=pu.getLinkman();
				if(each.equals(topicSender)){
					if(ValidateUtil.isValid(user.getImgUrl())){
						pic=user.getImgUrl();//topic图片
					}
//					values[3]="http://123.59.86.252:8888/"+user.getImgUrl();//topic图片
				}
			}
//			else if(each.startsWith("team_")){//供应商暂无法发送、查看留言
//				PmsTeam team=pmsTeamFacade.getTeamInfo(Long.parseLong(each.split("team_")[1]));
//				mail.setTo(team.getEmail());
//				PmsProjectTeam pt=pmsProjectTeamFacade.getProjectTeamByProjectTeamId(Long.parseLong(each.split("team_")[1]));
//				values[0]=pt.getLinkman();
//				if(each.equals(topicSender)){
//					values[3]=""+team.getTeamPhotoUrl();//topic图片	
//				}
//			}
			if(!ValidateUtil.isValid(mail.getTo())){
				logger.error(values[0]+" no mail error ");
				continue;
			}
			values[3]="http://resource.apaipian.com/resource/"+pic;
			//添加首尾模板
			content = MailTemplateFactory.addHtml(content);
			content = MailTemplateFactory.addImgHost(content);
			//邮件发送
			content=MailTemplateFactory.decorate(values,content);	
			mail.setContent(content);
			mail.setSubject(subject);
			mailService.sendMail(mail);
		}
	}
}
