package com.ag777.util.remote.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;

import com.ag777.util.lang.collection.ListUtils;
import com.sun.mail.util.MailConnectException;

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
 * @version create on 2018年04月16日,last modify at 2020年08月14日
 */
public class MailBuilder {

	private String smtpHost;
	private Integer port;
	private String from;
	private String fromDisplay;
	private List<String> toList;
	private String user;
	private String pwd;
	private String subject;
	private String content;
	private List<File> attachmentList;
	
	private Integer timeoutConnect;
	private Integer timeoutWrite;
	
	private boolean isSsl;	//是否开启ssl模式
	
	private boolean skipFailure;	//是否忽视发送失败的邮箱
	
	private boolean useCache;	//是否使用缓存
	private boolean debug;	//是否开启debug模式
	
	/**
	 * 构造函数,写上一些连接的必要信息
	 * @param smtpHost 邮件服务器地址
	 * @param user 邮件服务器账号(登录用)
	 * @param pwd 邮件服务器密码(登录用)
	 */
	public MailBuilder(String smtpHost, String user, String pwd) {
		this.smtpHost = smtpHost;
		this.user = user;
		this.pwd = pwd;
		toList = ListUtils.newArrayList();
		attachmentList = ListUtils.newArrayList();	//懒代码，直接避免空指针
		isSsl = false;
		skipFailure=false;	//默认不跳过发送失败的邮件
		useCache = false;	//默认为不使用缓存
		debug = false;	//默认不开启debug模式
	}

	public MailBuilder port(int port) {
		this.port = port;
		return this;
	}
	
	/**
	 * 设置发件邮箱
	 * @param from 发件邮箱
	 * @param fromDisplay 邮件里实际显示的发件人
	 * @return
	 */
	public MailBuilder from(String from, String fromDisplay) {
		this.from = from;
		this.fromDisplay = fromDisplay;
		return this;
	}
	
	/**
	 * 设置发件邮箱
	 * <p>
	 * 发送人默认和发件邮箱相同
	 * </p>
	 * @param from 发送邮箱
	 * @return
	 */
	public MailBuilder from(String from) {
		this.from = from;
		this.fromDisplay = from;
		return this;
	}
	
	/**
	 * 设置接受者
	 * @param address 发送到的邮箱
	 * @return
	 */
	public MailBuilder to(String address) {
		this.toList.add(address);
		return this;
	}
	
	/**
	 * 设置邮件内容
	 * @param subject 主题,其实就是标题
	 * @param content 邮件内容
	 * @return
	 */
	public MailBuilder content(String subject, String content) {
		this.subject = subject;
		this.content = content;
		return this;
	}
	
	/**
	 * 添加附件
	 * @param attachment 附件
	 * @return
	 */
	public MailBuilder addAttachment(File attachment) {
		attachmentList.add(attachment);
		return this;
	}
	
	/**
	 * 添加附件数组
	 * @param attachments 一系列附件
	 * @return
	 */
	public MailBuilder addAttachment(File[] attachments) {
		for (File attachment : attachments) {
			addAttachment(attachment);
		}
		return this;
	}
	
	/**
	 * 设置连接超时,默认是无限等待
	 * @param timeoutConnect
	 */
	public MailBuilder timeoutConnect(Integer timeoutConnect) {
		this.timeoutConnect = timeoutConnect;
		return this;
	}

	/**
	 * 设置写出超时,默认是无限等待
	 * @param timeoutWrite
	 */
	public MailBuilder timeoutWrite(Integer timeoutWrite) {
		this.timeoutWrite = timeoutWrite;
		return this;
	}

	public MailBuilder setSsl() {
		this.isSsl = true;
		return this;
	}
	
	public boolean skipFailure() {
		return skipFailure;
	}

	public MailBuilder skipFailure(boolean skipFailure) {
		this.skipFailure = skipFailure;
		return this;
	}

	/**
	 * 设置是否使用缓存,默认为不使用
	 * <p>
	 * 具体效用见MailUtils类send()方法的注释说明
	 * </p>
	 * 
	 * @param useCache
	 * @return
	 */
	public MailBuilder useCache(boolean useCache) {
		this.useCache = useCache;
		return this;
	}
	
	/**
	 * 是否开启debug模式,默认为不开启
	 * @param debug
	 * @return
	 */
	public MailBuilder debug(boolean debug) {
		this.debug = debug;
		return this;
	}
	
	/**
	 * 发送邮件
	 * @return 成功返回true
	 */
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
				port,
				user,
				pwd,
				from,
				fromDisplay,
				toList,
				subject,
				content,
				attachments,
				timeoutConnect,
				timeoutWrite,
				isSsl,
				skipFailure,
				useCache,
				debug);
	}
	
	/**
	 * 发送邮件，失败抛出异常信息
	 * @throws IllegalArgumentException
	 * @throws MailConnectException
	 * @throws AuthenticationFailedException
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	public void sendWithException() throws IllegalArgumentException, MailConnectException, AuthenticationFailedException, UnsupportedEncodingException, MessagingException {
		File[] attachments = null;
		if(!ListUtils.isEmpty(attachmentList)) {
			attachments = new File[attachmentList.size()];
			for(int i=0;i<attachmentList.size();i++) {
				attachments[i] = attachmentList.get(i);
			}
		}
		
		MailUtils.sendWithException(
				smtpHost, 
				port,
				user,
				pwd,
				from,
				fromDisplay,
				toList,
				subject,
				content,
				attachments,
				timeoutConnect,
				timeoutWrite,
				isSsl,
				skipFailure,
				useCache,
				debug);
	}
	
	public static void main(String[] args) {
		boolean flag = new MailBuilder(
				"xx.xx.xx.xx", 
				"xxxx", "xxxxxxxx")
			.from("test@test.com", "测试发送人")
			.to("test@test.com")
			.content("标题", "内容")
			.send();
		System.out.println(flag);
		
		flag = new MailBuilder(
				"xx.xx.xx.xx", 
				"xxxx", "xxxx")
			.from("test@test.com", "测试发送人")
			.to("test@test.com")
			.content("标题", "内容")
			.send();
		System.out.println(flag);
	}
	
}
