package com.ag777.util.remote.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import com.ag777.util.lang.StringUtils;
import com.ag777.util.lang.collection.ListUtils;
import com.ag777.util.lang.exception.Assert;
import com.sun.mail.util.MailConnectException;

/**
 * 邮件操作工具类
 * <p>
 * 	需要jar包:
 * <ul>
 * <li>javax.mail.xxx.jar</li>
 * </ul>
 * 最新包请从github上获取
 * https://github.com/javaee/javamail/releases
 * </p>
 * 
 * @author ag777
 * @version create on 2018年04月16日,last modify at 2020年04月24日
 */
public class MailUtils {

	private MailUtils() {}
	
	/**
	 * 测试连接
	 * @param smtpHost
	 * @param user
	 * @param password
	 * @param timeoutConnect
	 * @param debug
	 * @return
	 */
	public static boolean testConnect(
			String smtpHost,
			String user,
			String password,
			Integer timeoutConnect,
			Boolean debug) {
		try {
			return testWithException(smtpHost, user, password, timeoutConnect, debug);
		} catch (MessagingException | IllegalArgumentException | UnsupportedEncodingException e) {
			return false;
		}
	}
	
	/**
	 * 测试连接(抛出异常)
	 * @param smtpHost
	 * @param user
	 * @param password
	 * @param timeoutConnect
	 * @param debug
	 * @return
	 * @throws IllegalArgumentException 参数验证异常
	 * @throws MailConnectException 连接失败
	 * @throws AuthenticationFailedException 账号密码错误
	 * @throws MessagingException 其他异常,包含很多子类，比如:SMTPAddressFailedException-一般表示发送邮件失败,可能是对方邮箱地址有问题(访问不了或其它)
	 * @throws UnsupportedEncodingException InternetAddress转换失败(发件箱，收件箱)
	 */
	public static boolean testWithException(
			String smtpHost,
			String user,
			String password,
			Integer timeoutConnect,
			Boolean debug) throws IllegalArgumentException, MailConnectException, AuthenticationFailedException, MessagingException, UnsupportedEncodingException {
		Transport transport = null;
		Session mailSession = null;
		try {
			Properties properties = new Properties();
			properties.setProperty("mail.smtp.auth", "true");// 提供验证
			properties.setProperty("mail.transport.protocol", "smtp");// 使用的协议					
			properties.setProperty("mail.smtp.host", smtpHost);	// 这里是smtp协议
			if(timeoutConnect != null) {
				properties.put("mail.smtp.connectiontimeout", timeoutConnect);	//连接超时
			}
			// 设置发送验证机制
			Authenticator auth = new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, password);
				}
			};
			mailSession = Session.getInstance(properties, auth);
			if(debug != null) {
				mailSession.setDebug(debug);
			}
			transport = mailSession.getTransport();
			transport.connect(smtpHost, 25, user, password);
			return true;
		} catch(MailConnectException ex) { //连接失败
			throw ex;
		} catch(AuthenticationFailedException ex) {	//账号密码错误
			throw ex;
		} catch (MessagingException ex) {	//其他异常
			throw ex;
		} finally {
			mailSession = null;
			if(transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
				}
				transport = null;
			}
		}
	}
	
	/**
	 * 发送邮件
	 * <p>
	 * 是否使用缓存
	 * ①是:创建session时会使用getDefaultInstance方法,
	 * javamail首先是从缓存中查找是否有properties存在 
		如果存在，则加载默认的properties 
		如果不存在才加载用户自己定义的properties,
		单例模式,里面的username和password属性是final型的，无法更改
		选择使用缓存会导致一个问题，比如你登录了一个邮箱，然后使用错误的密码再次登录依然能够成功发送邮件
		②否:创建session时会使用getInstance方法,每次都会创建一个新对象,可以避免①中的问题，但是会增大系统开销
	 * </p>
	 * 
	 * @param smtpHost 邮件服务器地址
	 * @param user 发件邮箱账号
	 * @param password 发件邮箱密码
	 * @param from 发件邮箱
	 * @param fromDisplay 邮件显示的发件人
	 * @param toList 收件邮箱列表
	 * @param subject 主题
	 * @param content 邮件内容
	 * @param attachments n.（用电子邮件发送的）附件( attachment的名词复数 )
	 * @param timeoutConnect 连接超时
	 * @param timeoutWrite 写出超时
	 * @param skipFailure 是否跳过发送失败的邮件，开启这个时目标邮箱列表中一个或多个发送失败不影响其它地址的发送,并且不抛出发送失败的异常
	 * @param useCache 是否使用缓存
	 * @param debug 是否开启debug模式
	 * @return
	 */
	public static boolean send(
			String smtpHost,
			String user,
			String password,
			String from,
			String fromDisplay,
			List<String> toList,
			String subject,
			String content,
			File[] attachments,
			Integer timeoutConnect,
			Integer timeoutWrite,
			boolean skipFailure,
			boolean useCache,
			boolean debug) {
		try {
			Assert.notBlank(smtpHost, "邮件服务器地址不能为空");
			sendWithException(smtpHost, user, password, from, fromDisplay, toList, subject, content, attachments, timeoutConnect, timeoutWrite, skipFailure, useCache, debug);
			return true;
		} catch (UnsupportedEncodingException | IllegalArgumentException | MessagingException ex) {
//			ex.printStackTrace();
		}

		return false;
	}
	
	/**
	 * 发送邮件(伴随异常返回),如果是忽略失败邮箱的模式，会返回失败邮箱的列表
	 * <p>
	 * 详见send()方法
	 * </p>
	 * 
	 * @param smtpHost
	 * @param user
	 * @param password
	 * @param from
	 * @param fromDisplay
	 * @param toList
	 * @param subject
	 * @param content
	 * @param attachments
	 * @param timeoutConnect 连接超时
	 * @param timeoutWrite 写出超时
	 * @param skipFailure 是否跳过发送失败的邮件，开启这个时目标邮箱列表中一个或多个发送失败不影响其它地址的发送,并且不抛出发送失败的异常
	 * @param useCache 是否使用缓存
	 * @param debug 是否开启debug模式
	 * @return 发送失败的邮箱地址列表
	 * @throws IllegalArgumentException 参数验证异常
	 * @throws MailConnectException 连接失败
	 * @throws AuthenticationFailedException 账号密码错误
	 * @throws MessagingException 其他异常,包含很多子类，比如:SMTPAddressFailedException-一般表示发送邮件失败,可能是对方邮箱地址有问题(访问不了或其它)
	 * @throws UnsupportedEncodingException InternetAddress转换失败(发件箱，收件箱)
	 */
	public static List<String> sendWithException(
			String smtpHost,
			String user,
			String password,
			String from,
			String fromDisplay,
			List<String> toList,
			String subject,
			String content,
			File[] attachments,
			Integer timeoutConnect,
			Integer timeoutWrite,
			boolean skipFailure,
			boolean useCache,
			boolean debug) throws IllegalArgumentException, MailConnectException, AuthenticationFailedException, MessagingException, UnsupportedEncodingException {
		
		/*参数验证*/
		Assert.notBlank(smtpHost, "邮件服务器地址不能为空");
		Assert.notBlank(from, "发件邮箱不能为空");
		Assert.notEmpty(toList, "收件邮箱不能为空");
		for (String address : toList) {
			Assert.notBlank(address, "收件邮箱不能为空串");
		}
		
		/*
		 * bug:邮件附件名字过长时，收件会得到一个名字奇怪的附件,通过修改系统属性能够解决这个问题
		 * <p>
		 * 参考资料:https://blog.csdn.net/baidu_35962462/article/details/81062629
		 * </p>
		 */
		System.getProperties().setProperty("mail.mime.splitlongparameters", "false");
		
		String to = ListUtils.toString(toList, ",");	//实际发送地址用逗号分隔
		
		if(fromDisplay == null) {	//发送人默认为邮箱
			fromDisplay = from;
		}
		subject = StringUtils.emptyIfNull(subject);
		content = StringUtils.emptyIfNull(content);
		
		//验证附件存在性
		if(!ListUtils.isEmpty(attachments)) {
			for (File file : attachments) {
				Assert.notNull(file, "邮件附件文件不能为空");
				Assert.notExisted(file, "邮件附件文件不存在:" + file.getAbsolutePath());
			}
		}
		/*参数验证 end*/
		
		Transport transport = null;
		Session mailSession = null;
		try {
			// 设置java mail属性，并添入数据源
			Properties properties = new Properties();
			properties.setProperty("mail.smtp.auth", "true");// 提供验证
			properties.setProperty("mail.transport.protocol", "smtp");// 使用的协议					
			properties.setProperty("mail.smtp.host", smtpHost);	// 这里是smtp协议
			if(timeoutConnect != null) {
				properties.put("mail.smtp.connectiontimeout", timeoutConnect);	//连接超时
			}
			if(timeoutWrite != null) {
				properties.put("mail.smtp.writetimeout", timeoutWrite);	//写出超时
			}
//			properties.put("mail.smtp.timeout", timeoutWrite);	//读取超时(据说)
			
			// 设置发送验证机制
			Authenticator auth = new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, password);
				}
			};
			// 建立一个默认会话
			if(useCache) {	//使用缓存
				mailSession = Session.getDefaultInstance(properties, auth);
			} else {	//不使用缓存
				mailSession = Session.getInstance(properties, auth);
			}
			
			mailSession.setDebug(debug);
			
			MimeMessage msg = getMessage(mailSession, subject, content, attachments); // 创建MIME邮件对象
			
			//发送方邮箱
			msg.setFrom(new InternetAddress(from, fromDisplay));
			//发送地址数组
			InternetAddress[] addresses = InternetAddress.parse(to);
			
			//连接发送邮箱
			transport = mailSession.getTransport();
			connect(transport,smtpHost, user, password);
			// 发送邮件
			if(skipFailure) {	//跳过错误邮箱,需要对收件邮箱逐一进行发送
				List<String> failureList = ListUtils.newArrayList();
				for (InternetAddress address : addresses) {
					try {
						if(!transport.isConnected()) {	//如果之前失败过会断开连接,这里需要重新连接一下
							connect(transport,smtpHost, user, password);
						}
						// 设置收件者地址
						msg.setRecipient(Message.RecipientType.TO,
								address);
						transport.sendMessage(msg, msg.getAllRecipients());
					} catch(Exception ex) {
//						System.err.println("发送失败:"+address);
						failureList.add(address.toString());
					}
				}
				return failureList;
			} else {	//直接群发
				msg.setRecipients(Message.RecipientType.TO,
						addresses);
				transport.sendMessage(msg, msg.getAllRecipients());
			}
			return ListUtils.newArrayList();
			//发送完成
			
		} catch(MailConnectException ex) { //连接失败
			throw ex;
		} catch(AuthenticationFailedException ex) {	//账号密码错误
			throw ex;
		} catch (MessagingException ex) {		//其他异常
			throw ex;
		} catch (UnsupportedEncodingException ex) {	//InternetAddress转换失败(发件箱，收件箱)
			throw ex;
		} finally {
			mailSession = null;
			if(transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
				}
				transport = null;
			}
		}
	}
	
	/**
	 * 连接发件地址
	 * @param transport transport
	 * @param smtpHost 发件服务地址
	 * @param user 发件服务账号
	 * @param password 发件服务密码
	 * @throws MessagingException
	 */
	private static void connect(Transport transport, String smtpHost, String user, String password) throws MessagingException {
		transport.connect(smtpHost, 25, user, password);
	}
	
	/**
	 * 构建邮件
	 * @param mailSession
	 * @param subject 邮件标题
	 * @param content 邮件内容
	 * @param attachments 附件,可以为null
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	private static MimeMessage getMessage(
			Session mailSession,
			String subject,
			String content,
			File[] attachments) throws UnsupportedEncodingException, MessagingException {
		MimeMessage msg = new MimeMessage(mailSession); // 创建MIME邮件对象
		MimeMultipart mp = new MimeMultipart();
		
		msg.setSubject(MimeUtility.encodeText(StringUtils.emptyIfNull(subject), "gb2312",
				"B"));// 设置邮件的标题

		// 设置并处理信息内容格式转为text/html
		addContent(StringUtils.emptyIfNull(content), mp);
		// 设置邮件附件
		if (!ListUtils.isEmpty(attachments)) {
			addAttchments(attachments, mp);
		}

		msg.setContent(mp);
		// 保存并生成最终的邮件内容,这步不知道能否去掉
		msg.saveChanges();
		return msg;
	}
	
	
	
	/**
	 * 往邮件里添加邮件内容
	 * @param mailBody 邮件内容
	 * @param multipart 容器
	 * @throws MessagingException
	 */
	private static void addContent(String mailContent, Multipart multipart) throws MessagingException {
		BodyPart bp = new MimeBodyPart();
		bp.setContent(
				"<meta http-equiv=Content-Type content=text/html; charset=gb2312>"
						+ mailContent, "text/html;charset=GB2312");
		multipart.addBodyPart(bp);
	}
	
	/**
	 * 往邮件里添加附件
	 * @param attachments 附件数组
	 * @param multipart
	 * @return
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException 
	 */
	private static void addAttchments(File[] attachments, Multipart multipart) throws MessagingException, UnsupportedEncodingException {
		for (File file : attachments) {
			addAttchment(file, multipart);
		}
	}
	
	/**
	 * 往邮件里添加附件
	 * @param attachment 附件
	 * @param multipart
	 * @return
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException 
	 */
	private static void addAttchment(File attachment, Multipart multipart) throws MessagingException, UnsupportedEncodingException {
		BodyPart bp = new MimeBodyPart();
		FileDataSource fileds = new FileDataSource(attachment);
		bp.setDataHandler(new DataHandler(fileds));
		bp.setFileName(MimeUtility.encodeText(fileds.getName()));
		multipart.addBodyPart(bp);
	}

	public static void main(String[] args) {
//		System.out.println(
//				testConnect(
//				"192.168.161.106", 
//				"test", "123456")
//				);
		
		try {
			sendWithException(
					"xx", 
					"xx", "xxxx", "test@test.com", null, ListUtils.of("test@test.com"), null, null, null, 30*1000,5*60*1000, false, false, false);
			System.out.println("成功");
		} catch (IllegalArgumentException e) {
			System.out.println("参数异常");
			e.printStackTrace();
		} catch (MailConnectException e) {
			System.out.println("连接不上");
			e.printStackTrace();
		} catch (AuthenticationFailedException e) {
			System.out.println("账号密码错误");
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			System.out.println("地址转换失败");
			e.printStackTrace();
		} catch (MessagingException e) {
			System.out.println("其他异常");
			e.printStackTrace();
		}
	}
}
