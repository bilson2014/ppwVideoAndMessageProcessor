package com.paipianwang.mq.consumer.resource.model;

import com.paipianwang.mq.consumer.domain.BaseObject;

public class MailParam extends BaseObject {

	private static final long serialVersionUID = 6641496854728438244L;

	private String from; // 发件人

	private String to; // 收件人

	private String subject; // 主题

	private String content; // 邮件内容
	
	private String[] cc;//抄送人
	
	private String[] bcc;//密件抄送人
	
	private String[][] files;//附件--附件名称,fdfs文件路径

	

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String[] getCc() {
		return cc;
	}

	public void setCc(String[] cc) {
		this.cc = cc;
	}

	public String[][] getFiles() {
		return files;
	}

	public void setFiles(String[][] files) {
		this.files = files;
	}

	public String[] getBcc() {
		return bcc;
	}

	public void setBcc(String[] bcc) {
		this.bcc = bcc;
	}
}
