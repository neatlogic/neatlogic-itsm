package codedriver.framework.process.notify.dao.mapper;

import java.util.List;

import codedriver.module.process.notify.dto.NotifyTemplateVo;

public interface NotifyMapper {

	public int searchNotifyTemplateCount(NotifyTemplateVo notifyTemplateVo);

	public List<NotifyTemplateVo> searchNotifyTemplate(NotifyTemplateVo notifyTemplateVo);

	public NotifyTemplateVo getNotifyTemplateByUuid(String uuid);

}
