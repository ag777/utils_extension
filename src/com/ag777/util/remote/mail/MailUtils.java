package com.ag777.util.remote.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
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

/**
 * 邮件操作工具类
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
public class MailUtils {

	private MailUtils() {}
	
	/**
	 * 发送邮件
	 * @param smtpHost 邮件服务器地址
	 * @param mailFrom 发件邮箱
	 * @param fromDisplay 邮件显示的发件人
	 * @param mailTo 收件邮箱
	 * @param mailUser 发件邮箱账号
	 * @param mailPwd 发件邮箱密码
	 * @param mailSubject 主题
	 * @param mailContent 邮件内容
	 * @param attachments n.（用电子邮件发送的）附件( attachment的名词复数 )
	 * @return
	 */
	public static boolean send(
			String smtpHost,
			String from,
			String fromDisplay,
			String to,
			String user,
			String password,
			String subject,
			String content,
			File[] attachments) {
		
		//验证附件存在性
		if(!ListUtils.isEmpty(attachments)) {
			for (File file : attachments) {
				Assert.notNull(file, "邮件附件文件不能为空");
				Assert.notExisted(file, "邮件附件文件不存在:" + file.getAbsolutePath());
			}
		}
		
		Transport transport = null;
		try {
			// 设置java mail属性，并添入数据源
			Properties properties = new Properties();
			properties.setProperty("mail.smtp.auth", "true");// 提供验证
			properties.setProperty("mail.transport.protocol", "smtp");// 使用的协议					
			properties.setProperty("mail.smtp.host", smtpHost);	// 这里是smtp协议
			// 设置发送验证机制
			Authenticator auth = new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(user, password);
				}
			};
			// 建立一个默认会话
			Session mailSession = Session.getDefaultInstance(properties, auth);
			MimeMessage msg = new MimeMessage(mailSession); // 创建MIME邮件对象
			MimeMultipart mp = new MimeMultipart();

			msg.setFrom(new InternetAddress(from, fromDisplay));

			// 设置收件者地址
			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to));
			msg.setSubject(MimeUtility.encodeText(StringUtils.emptyIfNull(subject), "gb2312",
					"B"));// 设置邮件的标题

			// 设置并处理信息内容格式转为text/html
			addContent(StringUtils.emptyIfNull(content), mp);
			// 设置邮件附件
			if (!ListUtils.isEmpty(attachments)) {
				addAttchments(attachments, mp);
			}

			msg.setContent(mp);
			msg.saveChanges();
			// 发送邮件
			transport = mailSession.getTransport();
			transport.connect(smtpHost, 25, user, password);
			transport.sendMessage(msg, new Address[] { new InternetAddress(
					to) });
			return true;
		} catch (Exception ex) {
//			ex.printStackTrace();
		} finally {
			if(transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
				}
				transport = null;
			}
		}
		return false;
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

}
