package codedriver.framework.process.notify.core;

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
		myExecute(notifyVo);
	}

	protected abstract void myExecute(NotifyVo notifyVo);
}
