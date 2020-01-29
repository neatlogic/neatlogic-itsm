package codedriver.module.process.notify.handler;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import codedriver.framework.dao.mapper.MailServerMapper;
import codedriver.framework.dto.MailServerVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.notify.core.NotifyHandlerBase;
import codedriver.module.process.notify.dto.NotifyVo;
import codedriver.module.process.notify.exception.EmailServerNotFoundException;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-09 15:34
 **/
@Component
public class EmailNotifyHandler extends NotifyHandlerBase {

	private static Logger logger = LoggerFactory.getLogger(EmailNotifyHandler.class);

	@Autowired
	private MailServerMapper mailServerMapper;

	@Override
	public void myExecute(NotifyVo notifyVo) {
		this.sendEmail(notifyVo);
	}

	@Override
	public String getId() {
		return ClassUtils.getUserClass(this.getClass()).getName();
	}

	@Override
	public String getTemplateContent() {
		return null;
	}

	@Override
	public String getTemplateTitle() {
		return null;
	}

	private void sendEmail(NotifyVo notifyVo) {
		if (notifyVo.getToUserList().size() > 0) {
			try {
				MailServerVo mailServerVo = mailServerMapper.getActiveMailServer();
				if (mailServerVo != null && StringUtils.isNotBlank(mailServerVo.getHost()) && mailServerVo.getPort() != null) {
					HtmlEmail se = new HtmlEmail();
					se.setHostName(mailServerVo.getHost());
					se.setSmtpPort(mailServerVo.getPort());
					if (StringUtils.isNotBlank(mailServerVo.getUserName()) && StringUtils.isNotBlank(mailServerVo.getPassword())) {
						se.setAuthentication(mailServerVo.getUserName(), mailServerVo.getPassword());
					}
					if (StringUtils.isNotBlank(notifyVo.getFromUserEmail())) {
						se.setFrom(notifyVo.getFromUserEmail(), notifyVo.getFromUser());
					} else {
						if (StringUtils.isNotBlank(mailServerVo.getFromAddress())) {
							se.setFrom(mailServerVo.getFromAddress(), mailServerVo.getName());
						}
					}

					se.setSubject(clearStringHTML(notifyVo.getTitle()));
					StringBuilder sb = new StringBuilder();
					sb.append("<html>");
					sb.append("<head>");
					sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
					sb.append("<style type=\"text/css\">");
					sb.append("</style>");
					sb.append("</head><body>");
					sb.append(notifyVo.getContent());
					sb.append("</body></html>");
					se.addPart(sb.toString(), "text/html;charset=utf-8");
					for (UserVo user : notifyVo.getToUserList()) {
						if (StringUtils.isNotBlank(user.getEmail())) {
							se.addTo(user.getEmail());
						}
					}
					se.send();
				} else {
					throw new EmailServerNotFoundException();
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
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
