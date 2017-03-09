package com.shtd.datasyncer.utils;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Properties;

public class SendEmail {

	private static Logger logger = Logger.getLogger(Constant.LOGGER_NAME);

	private static final String UTF8 = "UTF-8";

	public static void Send(String msgContent) {
		try {
			String emailSmtpHost = ConfigReader.getInstance().getValue("email.smtp.host");

			String sendMail = ConfigReader.getInstance().getValue("email.sender.user");
			String sendMailUserName = ConfigReader.getInstance().getValue("email.sender.username");
			String sendMailPassword = EncryptionUtil.decrypt(ConfigReader.getInstance().getValue("email.sender.password"));
			String receiveMail = ConfigReader.getInstance().getValue("email.receive.user");
			String subject = ConfigReader.getInstance().getValue("email.sender.subject");

			// 1. 创建参数配置, 用于连接邮件服务器的参数配置
			Properties props = new Properties(); // 参数配置
			props.setProperty("mail.transport.protocol", "smtp"); // 使用的协议（JavaMail规范要求）
			props.setProperty("mail.smtp.host", emailSmtpHost); // 发件人的邮箱的 SMTP
																// 服务器地址
			props.setProperty("mail.smtp.auth", "true"); // 需要请求认证

			// 2. 根据配置创建会话对象, 用于和邮件服务器交互
			Session session = Session.getDefaultInstance(props);
			session.setDebug(false); // 设置为debug模式, 可以查看详细的发送 log

			// 3. 创建一封邮件
			MimeMessage message = createMimeMessage(session, sendMail, sendMailUserName, receiveMail, subject, msgContent);

			// 4. 根据 Session 获取邮件传输对象
			Transport transport = session.getTransport();

			// 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
			transport.connect(sendMail, sendMailPassword);

			// 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients()
			// 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
			transport.sendMessage(message, message.getAllRecipients());

			// 7. 关闭连接
			transport.close();

		} catch (Exception e) {
			logger.error("发送邮件失败，失败原因：" + e);

		}
	}

	/**
	 * 创建一封邮件
	 * 
	 * @param session 和服务器交互的会话
	 * @param sendMail 发件人邮箱
	 * @param sendUserName 发件人姓名
	 * @param receiveMail 收件人邮箱
	 * @param subject 邮件主题
	 * @param msgContent 邮件内容
	 * @return
	 * @throws Exception
	 * @author Josh
	 */
	public static MimeMessage createMimeMessage(Session session,
			String sendMail, String sendUserName, String receiveMail,
			String subject, String msgContent) throws Exception {
		
		// 1. 创建一封邮件
		MimeMessage message = new MimeMessage(session);

		// 2. From: 发件人
		message.setFrom(new InternetAddress(sendMail, sendUserName, UTF8));

		// 3. To: 收件人（可以增加多个收件人、抄送、密送）
		message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, "", UTF8));

		// 4. Subject: 邮件主题
		message.setSubject(subject, UTF8);

		// 5. Content: 邮件正文（可以使用html标签）
		message.setContent(msgContent, "text/html;charset=UTF-8");

		// 6. 设置发件时间
		message.setSentDate(new Date());

		// 7. 保存设置
		message.saveChanges();

		return message;
	}
}