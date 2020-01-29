package codedriver.framework.process.notify.core;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.module.process.notify.dto.NotifyVo;

public abstract class NotifyHandlerBase implements INotifyHandler {
	@Autowired
	private UserMapper userMapper;

	public final void execute(NotifyVo notifyVo) {
		if ((notifyVo.getToUserList() == null || notifyVo.getToUserList().size() <= 0) && notifyVo.getToUserIdList() != null && notifyVo.getToUserIdList().size() > 0) {
			for (String userId : notifyVo.getToUserIdList()) {
				UserVo userVo = userMapper.getUserBaseInfoByUserId(userId);
				if (userVo != null) {
					notifyVo.addUser(userVo);
				}
			}
		}
		if (StringUtils.isNotBlank(notifyVo.getFromUser())) {
			UserVo userVo = userMapper.getUserBaseInfoByUserId(notifyVo.getFromUser());
			if(userVo!=null && StringUtils.isNotBlank(userVo.getEmail())) {
				notifyVo.setFromUserEmail(userVo.getEmail());
			}
		}
		myExecute(notifyVo);
	}

	protected abstract void myExecute(NotifyVo notifyVo);
}
