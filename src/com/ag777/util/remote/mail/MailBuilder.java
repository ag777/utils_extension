package com.ag777.util.remote.mail;

import java.io.File;
import java.util.List;

import com.ag777.util.lang.collection.ListUtils;

/**
 * 邮件辅助构建类
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>javax.mail.xxx.jar</li>
 * </ul>
 * 最新包请从github上获取
 * </p>
 * 
 * @author ag777
 * @version create on 2018年04月16日,last modify at 2018年04月16日
 */
public class MailBuilder {

	private String smtpHost;
	private String from;
	private String fromDisplay;
	private String to;
	private String user;
	private String pwd;
	private String subject;
	private String content;
	private List<File> attachmentList;
	
	public MailBuilder(String smtpHost, String user, String pwd) {
		this.smtpHost = smtpHost;
		this.user = user;
		this.pwd = pwd;
		attachmentList = ListUtils.newArrayList();
	}

	/**
	 * 设置发送者
	 * @param from 发送邮箱
	 * @param fromDisplay 邮件里实际显示的发件人
	 * @return
	 */
	public MailBuilder from(String from, String fromDisplay) {
		this.from = from;
		this.fromDisplay = fromDisplay;
		return this;
	}
	
	/**
	 * 设置接受者
	 * @param to
	 * @return
	 */
	public MailBuilder to(String to) {
		this.to = to;
		return this;
	}
	
	/**
	 * 设置邮件内容
	 * @param subject
	 * @param content
	 * @return
	 */
	public MailBuilder content(String subject, String content) {
		this.subject = subject;
		this.content = content;
		return this;
	}
	
	/**
	 * 添加附件
	 * @param attachment
	 * @return
	 */
	public MailBuilder addAttachment(File attachment) {
		attachmentList.add(attachment);
		return this;
	}
	
	/**
	 * 添加附件数组
	 * @param attachments
	 * @return
	 */
	public MailBuilder addAttachment(File[] attachments) {
		for (File attachment : attachments) {
			addAttachment(attachment);
		}
		return this;
	}
	
	public boolean send() {
		File[] attachments = null;
		if(!ListUtils.isEmpty(attachmentList)) {
			attachments = new File[attachmentList.size()];
			for(int i=0;i<attachmentList.size();i++) {
				attachments[i] = attachmentList.get(i);
			}
		}
		
		return MailUtils.send(
				smtpHost, 
				from,
				fromDisplay,
				to,
				user,
				pwd,
				subject,
				content,
				attachments);
	}
}
