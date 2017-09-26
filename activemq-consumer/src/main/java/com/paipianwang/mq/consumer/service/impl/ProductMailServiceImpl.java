package com.paipianwang.mq.consumer.service.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.Calendar;
import com.paipianwang.mq.consumer.dao.MailDao;
import com.paipianwang.mq.consumer.resource.model.MailParam;
import com.paipianwang.mq.consumer.service.MailService;
import com.paipianwang.mq.consumer.service.ProductMailService;
import com.paipianwang.mq.utils.MailTemplateFactory;
import com.paipianwang.pat.common.config.PublicConfig;
import com.paipianwang.pat.common.enums.FileType;
import com.paipianwang.pat.common.util.DateUtils;
import com.paipianwang.pat.common.util.MoneyUtil;
import com.paipianwang.pat.common.util.ValidateUtil;
import com.paipianwang.pat.common.web.poi.util.GenerateExcel;
import com.paipianwang.pat.common.web.poi.util.GenerateWord;
import com.paipianwang.pat.facade.information.entity.PmsMail;
import com.paipianwang.pat.facade.information.service.PmsMailFacade;
import com.paipianwang.pat.facade.product.entity.PmsDimension;
import com.paipianwang.pat.facade.product.service.PmsDimensionFacade;
import com.paipianwang.pat.facade.right.entity.PmsEmployee;
import com.paipianwang.pat.facade.right.service.PmsEmployeeFacade;
import com.paipianwang.pat.facade.team.entity.PmsTeam;
import com.paipianwang.pat.facade.team.service.PmsTeamFacade;
import com.paipianwang.pat.facade.user.entity.PmsUser;
import com.paipianwang.pat.facade.user.service.PmsUserFacade;
import com.paipianwang.pat.workflow.entity.PmsProjectFlow;
import com.paipianwang.pat.workflow.entity.PmsProjectResource;
import com.paipianwang.pat.workflow.entity.PmsProjectSynergy;
import com.paipianwang.pat.workflow.entity.PmsProjectTeam;
import com.paipianwang.pat.workflow.entity.PmsProjectUser;
import com.paipianwang.pat.workflow.enums.ProjectRoleType;
import com.paipianwang.pat.workflow.enums.ProjectTeamType;
import com.paipianwang.pat.workflow.facade.PmsProjectFlowFacade;
import com.paipianwang.pat.workflow.facade.PmsProjectResourceFacade;
import com.paipianwang.pat.workflow.facade.PmsProjectSynergyFacade;
import com.paipianwang.pat.workflow.facade.PmsProjectTeamFacade;
import com.paipianwang.pat.workflow.facade.PmsProjectUserFacade;

@Service
public class ProductMailServiceImpl implements ProductMailService {
	private static final Logger logger = LoggerFactory.getLogger(ProductMailServiceImpl.class);
	
	@Autowired
	private final MailDao mailDao = null;
	@Autowired
	private final PmsMailFacade pmsMailFacade = null;
	@Autowired
	private PmsProjectFlowFacade pmsProjectFlowFacade = null;
	@Autowired
	private PmsProjectResourceFacade pmsProjectResourceFacade;
	@Autowired
	private PmsProjectUserFacade pmsProjectUserFacade;
	@Autowired
	private PmsProjectSynergyFacade pmsProjectSynergyFacade;
	@Autowired
	private PmsProjectTeamFacade pmsProjectTeamFacade;
	@Autowired
	private PmsEmployeeFacade pmsEmployeeFacade;
	@Autowired
	private PmsTeamFacade pmsTeamFacade;
	@Autowired
	private MailService service;
	@Autowired
	private PmsUserFacade pmsUserFacade;
	@Autowired
	private PmsDimensionFacade pmsDimensionFacade;

	
	// 发送项目确认启动函
	@Override
	public void sendProjectConfirmStart(String projectId) {
		logger.info("into sendProjectConfirmStart");
		String mailType = "projectConfirmStart";
		Map<String, Map<String, Object>> amap = new HashMap<>();
		Map<String, Object> each = new HashMap<String, Object>();
		// 邮件处理--收件人(客户)、抄送人、发送人、附件
		getByMail(mailType, projectId, amap, each);
		// 邮件内容参数
		List<String> metaData = new ArrayList<String>();
		metaData.add("projectId");//
		metaData.add("principal");
		metaData.add("projectDescription");
		metaData.add("projectName");
		metaData.add("productName");
		metaData.add("productConfigLevelName");
		metaData.add("estimatedPrice");
		metaData.add("productConfigLengthName");
		metaData.add("productConfigAdditionalPackageName");

		PmsProjectFlow flow = pmsProjectFlowFacade.getProjectFlowByProjectId(metaData, projectId);
		PmsProjectUser user = pmsProjectUserFacade.getProjectUserByProjectId(projectId);
		PmsEmployee principal = pmsEmployeeFacade.findEmployeeById(flow.getPrincipal());
		String[] value = new String[6];
		value[0] = user.getUserName();// 客户名称
		value[1] = flow.getProjectName();
		value[2] = flow.getProductName() + "+" + flow.getProductConfigLevelName();
		if(ValidateUtil.isValid(flow.getProductConfigLengthName())){
			
				value[2]=value[2]+ "+" + flow.getProductConfigLengthName();
			
		}
		
		if(ValidateUtil.isValid(flow.getProductConfigAdditionalPackageName())){
			value[2]=value[2]+ "+" + flow.getProductConfigAdditionalPackageName();
		}
		
		value[3] = flow.getEstimatedPrice() + "元";
		value[4] = principal.getEmployeeRealName();// 负责人-创建项目的人
		value[5] = principal.getPhoneNumber();
		each.put("value", value);

		each.put("subject", new String[] { flow.getProjectName() });

		sendAttachMailsByType(mailType, amap);
	}

	/**
	 * 根据邮件配置获取邮件信息
	 * 
	 * @param mailType
	 * @param projectId
	 * @param amap
	 * @param each
	 */
	private void getByMail(String mailType, String projectId, Map<String, Map<String, Object>> amap,
			Map<String, Object> each) {
		PmsMail m = null;//mailDao.getMailFromRedis(mailType);
		if (null == m){
			m = pmsMailFacade.getTemplateByType(mailType);
		}
			
		if (m != null) {
			String receiver = m.getReceiver();
			String recieverRole = m.getReceiverRole();
			// 获取收件人
			Set<String> receivers = getEmailBySetting(receiver, recieverRole, projectId);
			logger.info("receivers:"+receivers+"");
			if (!ValidateUtil.isValid(receivers)) {
				// 模板异常
				throw new RuntimeException("receiver is null:"+projectId+" with "+receiver+" and "+recieverRole);
			}
			receivers.forEach(rec -> amap.put(rec, each));
			// 获取抄送人
			Set<String> bcc = getEmailBySetting(m.getBcc(), m.getBccRole(), projectId);
			logger.info("bcc:"+bcc+"");
			if (ValidateUtil.isValid(bcc)) {
				each.put("bcc", bcc.toArray(new String[bcc.size()]));
			}
			// 获取发送人-唯一
			Set<String> senders = getEmailBySetting(m.getSender(), m.getSenderRole(), projectId);
			if (ValidateUtil.isValid(senders)) {
				each.put("from", senders.iterator().next());
			}
			// 获取附件
			String files = m.getMailFile();
			Set<String[]> fileUrls = new HashSet<>();
			if (ValidateUtil.isValid(files)) {
				String[] fileArray = files.split(",");
				fileDealt: for (String fileType : fileArray) {

					FileType[] values = FileType.values();
					
					for (FileType value : values) {
						//固定模板 
						if(value.getType() == 3 && value.getId().equals(fileType)){
							fileUrls.add(new String[] { value.getText()+value.getName().substring(value.getName().lastIndexOf(".")), PublicConfig.FILE_TEMPLATE_PATH+File.separator+"model"+File.separator+value.getName(),"template" });
							continue fileDealt;
						}
						// 自动生成
						if (value.getType() == 2 && value.getId().equals(fileType)) {
							fileUrls.add(new String[] { value.getName(), "","temp" });
							continue fileDealt;
						}
					}		

					//手动上传
					PmsProjectResource resource=pmsProjectResourceFacade.getValidProjectResources(projectId, fileType);
					
					if (resource!=null) {
						fileUrls.add(new String[] { resource.getResourceName(),resource.getResourcePath(),"" });
					}
				}
				if (ValidateUtil.isValid(fileUrls)) {
					each.put("files", fileUrls.toArray(new String[fileUrls.size()][]));
				}
			}
		}
	}


	/**
	 * 根据邮件配置获取相关邮箱地址（发件人、收件人、抄送人）--项目流程类
	 * 
	 * @param emails
	 * @param emailRoles
	 * @param projectId
	 * @return
	 */
	private Set<String> getEmailBySetting(String emails, String emailRoles, String projectId) {
		logger.info(emails+"--"+emailRoles+"--"+projectId);
		Set<String> result = new HashSet<String>();
		if (ValidateUtil.isValid(emails)) {
			String[] emailList = emails.split(",");
			for (String email : emailList) {
				result.add(email);
			}
		}
		// 根据角色获取收件人邮箱地址
		if (ValidateUtil.isValid(emailRoles)) {
			String[] emailRoleList = emailRoles.split(",");
			for (String rr : emailRoleList) {
				if (ProjectRoleType.customer.getId().equals(rr)) {
					// 客户
					PmsProjectUser user = pmsProjectUserFacade.getProjectUserByProjectId(projectId);
					if (user != null)
						result.add(user.getEmail());
				} else if (ProjectRoleType.teamPlan.getId().equals(rr)
						|| ProjectRoleType.teamProduct.getId().equals(rr)) {
					// 策划供应商/制作供应商
					List<String> params = new ArrayList<String>();
//					params.add("email");
					params.add("teamId");
					if(ProjectRoleType.teamPlan.getId().equals(rr)){
						rr=ProjectTeamType.scheme.getCode();//TODO 邮件与枚举一致
					}else{
						rr=ProjectTeamType.produce.getCode();
					}
					PmsProjectTeam team = pmsProjectTeamFacade.getProjectTeamByMap(params, projectId, rr);
					PmsTeam pmsTeam=pmsTeamFacade.findTeamById(team.getTeamId().longValue());//TODO 构造简化方法，只获取邮箱
					if (pmsTeam != null)
						result.add(pmsTeam.getEmail());
				} else if (ProjectRoleType.sale.getId().equals(rr)) {
					// 项目负责人(销售)
					List<String> metaData = new ArrayList<String>();
					metaData.add("principal");
					PmsProjectFlow flow = pmsProjectFlowFacade.getProjectFlowByProjectId(metaData, projectId);
					PmsEmployee principal = pmsEmployeeFacade.findEmployeeById(flow.getPrincipal());
					result.add(principal.getEmail());
				} else {
					// 根据角色获取协同人
					List<PmsProjectSynergy> synergies = pmsProjectSynergyFacade.getSynergys(projectId, rr);
					if (ValidateUtil.isValid(synergies)) {
						Set<Long> ids = new HashSet<Long>();
						for (PmsProjectSynergy sy : synergies) {
							ids.add(sy.getEmployeeId().longValue());
						}
						List<PmsEmployee> employees = pmsEmployeeFacade
								.findEmployeeByIds(ids.toArray(new Long[ids.size()]));
						for (PmsEmployee employee : employees) {
							result.add(employee.getEmail());
						}
					}
				}
			}
		}
		return result;
	}

	// 发送项目策划启动函
	@Override
	public void sendProjectPlanStart(String projectId) {
		String mailType = "projectPlanStart";
		Map<String, Map<String, Object>> amap = new HashMap<>();
		Map<String, Object> each = new HashMap<String, Object>();
		// 收件人-对接策划供应商
		getByMail(mailType, projectId, amap, each);

		List<String> metaData = new ArrayList<String>();
		metaData.add("projectId");
		metaData.add("projectName");
		metaData.add("productName");
		PmsProjectFlow flow = pmsProjectFlowFacade.getProjectFlowByProjectId(metaData, projectId);
		List<String> teamData = new ArrayList<String>();
		teamData.add("teamName");
		teamData.add("budget");
		teamData.add("remark");
		teamData.add("linkman");
		teamData.add("telephone");
		teamData.add("email");
		teamData.add("teamId");
		teamData.add("planContent");
		teamData.add("planTime");
		teamData.add("accessMan");
		teamData.add("accessManTelephone");
		teamData.add("teamType");
		PmsProjectTeam team = pmsProjectTeamFacade.getProjectTeamByMap(teamData, projectId,
				ProjectTeamType.scheme.getCode());// 策划供应商

		// 生成项目制作单
		List<String> params = new ArrayList<>();
		String path = generateExcel(params, flow, team);
		// 邮件内容参数
		String[] mainValue = new String[8];
		mainValue[0] = team.getTeamName();
		mainValue[1] = flow.getProjectName();
		mainValue[2] = team.getPlanContent();// 供应商策划内容
		mainValue[3] = team.getBudget() + "";// 取供应商预算价格
		if(ValidateUtil.isValid(team.getPlanTime())){
			mainValue[4] = DateUtils.getDateByFormatStr(DateUtils.getDateByFormat(team.getPlanTime(), "yyyy-MM-dd"), "yyyy年MM月dd日");// 项目策划交付时间
		}
		mainValue[5] = team.getRemark();
		mainValue[6]=team.getAccessMan();//TODO 模板添加对接人、手机--取新字段
		mainValue[7]=team.getAccessManTelephone();

		String[] value = new String[mainValue.length + params.size()];
		System.arraycopy(mainValue, 0, value, 0, mainValue.length);
		System.arraycopy(params.toArray(), 0, value, mainValue.length, params.size());

		each.put("value", value);

		each.put("subject", new String[] { flow.getProjectName() });

		// 策划方案模板/解说词模板--暂时产品线配策划方案
		if (each.get("files") != null) {
			String[][] files = (String[][]) each.get("files");
			for (String[] file : files) {
				if (FileType.autoProjectSheet.getName().equals(file[0])) {// 自动生成项目制作单
					file[1] = path;
				}
			}
		}

		sendAttachMailsByType("projectPlanStart", amap);

	}

	// 发送项目告知函
	@Override
	public void sendProjectInfoLetter(String projectId) {
		String mailType = "projectInformLetter";
		Map<String, Map<String, Object>> amap = new HashMap<>();
		Map<String, Object> each = new HashMap<String, Object>();
		// 收件人-客户
	
		getByMail(mailType, projectId, amap, each);

		List<String> metaData = new ArrayList<String>();
		metaData.add("projectId");
		metaData.add("projectName");
		metaData.add("principal");
		metaData.add("projectBudget");
		metaData.add("productName");

		PmsProjectFlow flow = pmsProjectFlowFacade.getProjectFlowByProjectId(metaData, projectId);
		// 生成附件
		List<String> params = new ArrayList<>();
		String path = generateWord(params, projectId, flow);

		// 邮件内容参数
		String[] value = new String[params.size()];
		for (int i = 0; i < params.size(); i++) {
			value[i] = params.get(i);
		}

		each.put("value", value);

		each.put("subject", new String[] { flow.getProjectName() });

		if (each.get("files") != null) {
			String[][] files = (String[][]) each.get("files");
			for (String[] file : files) {
				if (FileType.autoCpsl.getName().equals(file[0])) {// 自动项目客户服务函
					file[1] = path;
				}
			}
		}

		sendAttachMailsByType(mailType, amap);
	}

	// 发送项目制作启动函
	@Override
	public void sendProjectMakingList(String projectId) {
		String mailType = "projectMakingList";
		Map<String, Map<String, Object>> amap = new HashMap<>();
		Map<String, Object> each = new HashMap<String, Object>();
		// 收件人-项目对接制作供应商
		getByMail(mailType, projectId, amap, each);

		List<String> metaData = new ArrayList<String>();
		metaData.add("projectId");
		metaData.add("projectName");
		metaData.add("filmDestPath");
		metaData.add("productName");
		PmsProjectFlow flow = pmsProjectFlowFacade.getProjectFlowByProjectId(metaData, projectId);
		List<String> teamData = new ArrayList<String>();
		teamData.add("teamName");
		teamData.add("budget");
		teamData.add("remark");
		teamData.add("linkman");
		teamData.add("telephone");
		teamData.add("email");
		teamData.add("teamId");
		teamData.add("teamType");
		teamData.add("makeContent");
		teamData.add("makeTime");
		PmsProjectTeam team = pmsProjectTeamFacade.getProjectTeamByMap(teamData, projectId,
				ProjectTeamType.produce.getCode());// 制作供应商
		List<PmsProjectSynergy> cmoSynergy = pmsProjectSynergyFacade.getSynergys(projectId,
				ProjectRoleType.supervise.getId());// 监制
		PmsEmployee producer = pmsEmployeeFacade.findEmployeeById(cmoSynergy.get(0).getEmployeeId());

		// 生成项目制作单
		List<String> params = new ArrayList<>();
		String path = generateExcel(params, flow, team);

		// 邮件内容参数
		String[] mainValue = new String[7];
		mainValue[0] = team.getLinkman();
		mainValue[1] = team.getTeamName();
		mainValue[2] = team.getTeamName();
		// mainValue[2] = flow.getProjectName();
		mainValue[3] = flow.getFilmDestPath();
		mainValue[4] = "分镜头脚本示例";//TODO 配置名称
		mainValue[5] = producer.getEmployeeRealName();// 监制
		mainValue[6] = producer.getPhoneNumber();
		// mainValue[5] = team.getRemark();

		String[] value = new String[mainValue.length + params.size()];
		System.arraycopy(mainValue, 0, value, 0, mainValue.length);
		System.arraycopy(params.toArray(), 0, value, mainValue.length, params.size());

		each.put("value", value);

		each.put("subject", new String[] { flow.getProjectName() });

		if (each.get("files") != null) {
			String[][] files = (String[][]) each.get("files");
			for (String[] file : files) {
				if (FileType.autoProjectSheet.getName().equals(file[0])) {// 自动项目制作单
					file[1] = path;
				}
			}
		}
		sendAttachMailsByType(mailType, amap);
	}

	// 发送项目样片修改意见
	@Override
	public void sendProjectSampleMIdea(String projectId) {
		String mailType = "projectSampleMIdea";
		Map<String, Map<String, Object>> amap = new HashMap<>();
		Map<String, Object> each = new HashMap<String, Object>();
		// 收件人-制作供应商
		
		getByMail(mailType, projectId, amap, each);

		List<String> metaData = new ArrayList<String>();
		metaData.add("projectName");
		PmsProjectFlow flow = pmsProjectFlowFacade.getProjectFlowByProjectId(metaData, projectId);
		List<String> teamData = new ArrayList<String>();
		teamData.add("teamName");
		PmsProjectTeam team = pmsProjectTeamFacade.getProjectTeamByMap(teamData, projectId,
				ProjectTeamType.produce.getCode());
		// 邮件内容参数
		String[] value = new String[2];
		value[0] = team.getTeamName();
		value[1] = flow.getProjectName();

		each.put("value", value);

		each.put("subject", new String[] { flow.getProjectName() });

		sendAttachMailsByType(mailType, amap);
	}

	// 发送项目验收确认函
	@Override
	public void sendProjectAcceptConfirm(String projectId) {
		String mailType = "projectAcceptConfirm";
		Map<String, Map<String, Object>> amap = new HashMap<>();
		Map<String, Object> each = new HashMap<String, Object>();
		// 收件人-客户
		
		getByMail(mailType, projectId, amap, each);

		List<String> metaData = new ArrayList<String>();
		metaData.add("projectId");
		metaData.add("projectName");
		PmsProjectFlow flow = pmsProjectFlowFacade.getProjectFlowByProjectId(metaData, projectId);
		PmsProjectUser user = pmsProjectUserFacade.getProjectUserByProjectId(projectId);
		// 邮件内容参数
		String[] value = new String[6];
		value[0] = user.getUserName();
		value[1] = flow.getProjectName();
		value[2] = flow.getProjectName();
		value[3] = user.getUserName();
		value[4] = flow.getProjectId();
		value[5] = new SimpleDateFormat("yyyy-MM-dd").format(new Date());//交付时间取当前时间

		each.put("value", value);

		each.put("subject", new String[] { flow.getProjectName() });

		sendAttachMailsByType(mailType, amap);
	}

	// 发送项目验收函
	@Override
	public void sendProjectAcceptLetter(String projectId) {
		String mailType = "projectAcceptLetter";
		Map<String, Map<String, Object>> amap = new HashMap<>();
		Map<String, Object> each = new HashMap<String, Object>();
		// 收件人-制作供应商
		
		getByMail(mailType, projectId, amap, each);

		List<String> metaData = new ArrayList<String>();
		metaData.add("projectId");
		metaData.add("projectName");
		PmsProjectFlow flow = pmsProjectFlowFacade.getProjectFlowByProjectId(metaData, projectId);
		List<String> teamData = new ArrayList<String>();
		teamData.add("teamName");
		teamData.add("budget");
		teamData.add("remark");
		teamData.add("linkman");
		teamData.add("telephone");
		teamData.add("email");
		PmsProjectTeam team = pmsProjectTeamFacade.getProjectTeamByMap(teamData, projectId,
				ProjectTeamType.produce.getCode());

		// 邮件内容参数
		String[] value = new String[4];
		value[0] = flow.getProjectName();
		value[1] = team.getTeamName();
		value[2] = flow.getProjectName();
		if("1".equals(team.getInvoiceHead())){
			value[3] = "发票名称：北京拍片乐科技有限公司<br />" 
					+ "发票类型：增值税专用发票<br />" 
					+ "纳税人识别号：91110108348400996C<br />"
					+ "地址：北京市丰台区汽车博物馆东路6号3号楼1单元7层701-A03(园区)<br />" 
					+ "电话：010-82316139<br />"
					+ "开户行及账号：建设北京财满街支行 11001119400052546467";
		}else{
			value[3] = "发票名称：北京攀峰文化传播有限公司<br />" 
					+ "发票类型：增值税普通发票<br />" 
					+ "纳税人识别号：911101050592401725<br />"
					+ "地址：北京市朝阳区南沙滩66号院1号楼商业1-2-(2)B区a023号<br />" 
					+ "电话：010-59005943<br />"
					+ "开户行及账号：中国建设银行北京光明支行 11001071200053012339";
		}
		

		each.put("value", value);

		each.put("subject", new String[] { flow.getProjectName() });

		sendAttachMailsByType(mailType, amap);
	}

	// 生成项目制作单
	private String generateExcel(List<String> params, PmsProjectFlow flow, PmsProjectTeam team) {
		params.add(flow.getProjectName());
		params.add(flow.getProjectId());
		params.add(flow.getProjectName());
		params.add(flow.getProductName());// 视频类型-取产品线
		if (ProjectTeamType.scheme.getCode().equals(team.getTeamType())) {
			params.add(team.getPlanContent());
		} else {
			params.add(team.getMakeContent());
		}
		params.add(team.getBudget() + "");
		params.add(MoneyUtil.changeToChinese(team.getBudget().doubleValue()));
		String time="";
		if (ProjectTeamType.scheme.getCode().equals(team.getTeamType())) {
			time=team.getPlanTime();
		} else {
			time=team.getMakeTime();
		}
		Calendar calendar=Calendar.getInstance();
		try {
			calendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(time));
			params.add(calendar.get(Calendar.MONTH)+"");
			params.add(calendar.get(Calendar.DAY_OF_MONTH)+"");
		} catch (Exception e) {
			logger.error("format lead time \'"+time+"\'error");
			params.add("");
			params.add("");
		}
		

		params.add(team.getTeamName());
		params.add(team.getLinkman());
		params.add(team.getTelephone());
//		params.add(team.getEmail());

		PmsTeam pmsTeam = team.getTeamId() == null ? null : pmsTeamFacade.findTeamById(team.getTeamId().longValue());
		params.add(pmsTeam == null ? "" : pmsTeam.getEmail());
		params.add(pmsTeam == null ? "" : pmsTeam.getAddress());

		Map<String, String> beanParams = new HashMap<>();
		for (int i = 1; i <= params.size(); i++) {
			beanParams.put("_" + i, params.get(i - 1));
		}
		
		return GenerateExcel.generateByModel(beanParams, "项目制作单"+new Date().getTime(), 1);
	}

	// 生成项目服务函
	private String generateWord(List<String> params, String projectId, PmsProjectFlow flow) {

		PmsProjectUser user = pmsProjectUserFacade.getProjectUserByProjectId(projectId);
		 PmsUser
		 puser=user.getUserId()==null?null:pmsUserFacade.findUserById(user.getUserId().longValue());

		PmsEmployee principal = pmsEmployeeFacade.findEmployeeById(flow.getPrincipal());

		List<PmsProjectSynergy> cmoSynergy = pmsProjectSynergyFacade.getSynergys(projectId,
				ProjectRoleType.saleDirector.getId());// "销售总监"
		PmsEmployee cmo = pmsEmployeeFacade.findEmployeeById(cmoSynergy.get(0).getEmployeeId());

		params.add(user.getUserName());
		params.add(flow.getProjectId());
		params.add(flow.getProjectName());
		if(ValidateUtil.isValid(user.getAppointedTime())){
			params.add(DateUtils.getDateByFormatStr(DateUtils.getDateByFormat(user.getAppointedTime(), "yyyy-MM-dd"), "yyyy年MM月dd日"));
		}else{
			params.add("");
		}
		
		if(ValidateUtil.isValid(user.getDeliveryTime())){
			params.add(DateUtils.getDateByFormatStr(DateUtils.getDateByFormat(user.getDeliveryTime(), "yyyy-MM-dd"), "yyyy年MM月dd日"));
		}else{
			params.add("");
		}
		params.add(MoneyUtil.changeToChinese(flow.getProjectBudget().doubleValue()));
		params.add(flow.getProjectBudget() + "");
		params.add(user.getUserName());
		params.add(user.getLinkman());
		params.add(user.getTelephone());
		params.add(user.getEmail());
		params.add(puser.getWeChat());//使用微信

		params.add("");
		params.add("");
		params.add("");
		params.add("");

		params.add(principal.getEmployeeRealName());// 视频管家-销售-负责人
		params.add(principal.getPhoneNumber());
		params.add(principal.getEmail());

		params.add(cmo.getEmployeeRealName());// 销售总监
		params.add(cmo.getPhoneNumber());
		params.add(cmo.getEmail());

		Map<String, String> beanParams = new HashMap<>();
		for (int i = 1; i <= params.size(); i++) {
			beanParams.put(i + "", params.get(i - 1));
		}

		return GenerateWord.generateByModel(beanParams, "客户项目服务函"+new Date().getTime(), 1);
	}
	
	public void sendAttachMailsByType(String mailType, Map<String, Map<String,Object>> map) {
		PmsMail m = mailDao.getMailFromRedis(mailType);
		if(null == m)m = pmsMailFacade.getTemplateByType(mailType);
		if(null != m){
			try {
				String content = m.getContent();
				//1.转码
				content = new String(Base64.getDecoder().decode(content),"utf-8");
				//2.添加首尾模板
				content = MailTemplateFactory.addHtml(content);
				content = MailTemplateFactory.addImgHost(content);
				Iterator<Entry<String, Map<String,Object>>> i = map.entrySet().iterator(); 
				while(i.hasNext()){
					Map.Entry<String, Map<String,Object>> entry = (Map.Entry<String, Map<String,Object>>)i.next();
					String email = entry.getKey().toString();
					Map<String,Object> value = entry.getValue();
					String c=content;
					if(value.get("value")!=null){
						c = MailTemplateFactory.decorate((String[])value.get("value"),content);
					}
					//邮件主题参数
					String subject=m.getSubject();
					if(value.get("subject")!=null){
						subject = MailTemplateFactory.decorate((String[])value.get("subject"),subject);
					}
					//抄送人
					String[] cc=value.get("cc")==null?null:(String[])value.get("cc");
					//密送人
					String[] bcc=value.get("bcc")==null?null:(String[])value.get("bcc");
					//附件路径（fdfs）
					String[][] files=value.get("files")==null?null:(String[][])value.get("files");
					//发送人
					String from=value.get("from")==null?null:(String)value.get("from");
					
					// 发送邮件
					MailParam mail =new MailParam();
					mail.setBcc(bcc);
					mail.setCc(cc);
					mail.setContent(c);
					mail.setFiles(files);
					mail.setFrom(from);
					mail.setSubject(subject);
					mail.setTo(email);
					service.sendMail(mail);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else{
			logger.error("the mail type" + mailType + " is not exist");
		}
	}
}
