package codedriver.module.process.notify.handler;

import codedriver.framework.dao.mapper.MailServerMapper;
import codedriver.framework.dto.MailServerVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.notify.core.INotifyHandler;
import codedriver.module.process.notify.dto.EmailMessageVo;
import codedriver.module.process.notify.dto.NotifyVo;
import codedriver.module.process.notify.exception.EmailNoServerException;

import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-09 15:34
 **/
@Component
public class EmailNotifyHandler implements INotifyHandler {

	private static Logger logger = LoggerFactory.getLogger(EmailNotifyHandler.class);
	private static String SMTP_SERVER_HOST = null;
	private static String USER_MAIL_USERNAME = null;
	private static String USER_MAIL_PASSWORD = null;
	private static String SMTP_SERVER_USERNAME = null;
	private static Integer SMTP_PORT = null;
	private static String FROM_ADDRESS = null;

	@Autowired
	private MailServerMapper mailServerMapper;

	@Override
	public void execute(NotifyVo messageVo) {
		//this.loadSmtpInfo();
		this.sendEmail(messageVo);
	}

	@Override
	public String getId() {
		return "emailPlugin";
	}

	@Override
	public String getTemplateContent() {
		return null;
	}

	@Override
	public String getTemplateTitle() {
		return null;
	}

	private void sendEmail(NotifyVo message) {
//		EmailMessageVo emailMessage = (EmailMessageVo) message;
//		if (SMTP_SERVER_HOST != null && USER_MAIL_USERNAME != null && USER_MAIL_PASSWORD != null) {
//			List<UserVo> toUserList = emailMessage.getToUserList();
//			List<UserVo> ccUserList = emailMessage.getCcUserList();
//			HtmlEmail se = null;
//			try {
//				if (toUserList.size() > 0) {
//					se = new HtmlEmail();
//					se.setHostName(SMTP_SERVER_HOST);
//
//					if (USER_MAIL_USERNAME != null && !"".equals(USER_MAIL_USERNAME) && USER_MAIL_PASSWORD != null && !"".equals(USER_MAIL_PASSWORD)) {
//						se.setAuthentication(USER_MAIL_USERNAME, USER_MAIL_PASSWORD);
//					}
//					se.setSmtpPort(SMTP_PORT);
//					se.setFrom((FROM_ADDRESS == null || FROM_ADDRESS.equals("") ? USER_MAIL_USERNAME : FROM_ADDRESS), (emailMessage.getFromUser() != null && !"".equals(emailMessage.getFromUser())) ? emailMessage.getFromUser() : SMTP_SERVER_USERNAME);
//					se.setSubject(clearStringHTML(emailMessage.getTitle()));
//					StringBuilder sb = new StringBuilder();
//					sb.append("<html>");
//					sb.append("<head>");
//					sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
//					sb.append("<style type=\"text/css\">");
//					sb.append("</style>");
//					sb.append("</head><body>");
//					sb.append(emailMessage.getContent());
//					sb.append("</body></html>");
//					se.addPart(sb.toString(), "text/html;charset=utf-8");
//					for (UserVo user : toUserList) {
//						se.addTo(user.getEmail());
//					}
//					if (ccUserList != null && ccUserList.size() > 0) {
//						for (UserVo ccUser : ccUserList) {
//							se.addCc(ccUser.getEmail());
//						}
//					}
//					se.send();
//				}
//			} catch (Exception ex) {
//				if (se != null) {
//					StringBuilder sb = new StringBuilder();
//					sb.append("hostname:" + se.getHostName() + "\n");
//					sb.append("smtpport:" + se.getSmtpPort() + "\n");
//					sb.append("from:" + se.getFromAddress() + "\n");
//					sb.append("subject:" + se.getSubject() + "\n");
//					logger.error(sb.toString() + ex.getMessage());
//				}
//				logger.error(ex.getMessage(), ex);
//			}
//		} else {
//			throw new EmailNoServerException("MAIL SMTP CONFIGURATION INIT FAILURED! ::  Current System has not been configured mail server!");
//		}
//	}
//
//	private void loadSmtpInfo() {
//		MailServerVo mailServerVo = mailServerMapper.getActiveMailServer();
//		if (mailServerVo != null) {
//			SMTP_SERVER_HOST = mailServerVo.getHost();
//			USER_MAIL_USERNAME = mailServerVo.getUserName();
//			USER_MAIL_PASSWORD = mailServerVo.getPassword();
//			SMTP_SERVER_USERNAME = mailServerVo.getName();
//			SMTP_PORT = mailServerVo.getPort();
//			FROM_ADDRESS = mailServerVo.getFromAddress();
//		}
	}

	public String clearStringHTML(String sourceContent) {
		String content = "";
		if (sourceContent != null) {
			content = sourceContent.replaceAll("</?[^>]+>", "");
		}
		return content;
	}

	@Override
	public String getName() {
		return "邮件通知插件";
	}
}
