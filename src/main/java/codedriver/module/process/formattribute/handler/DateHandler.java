package codedriver.module.process.formattribute.handler;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.attribute.dto.AttributeDataVo;
import codedriver.framework.attribute.exception.AttributeValidException;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.form.FormIllegalParameterException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.formattribute.core.IFormAttributeHandler;
@Component
public class DateHandler implements IFormAttributeHandler {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private WorktimeMapper worktimeMapper;

	@Override
	public String getType() {
		return ProcessFormHandler.FORMDATE.getHandler();
	}

	@Override
	public boolean valid(AttributeDataVo attributeDataVo,JSONObject jsonObj) throws AttributeValidException {
		long data = Long.parseLong(attributeDataVo.getData());
		JSONObject configObj = jsonObj.getJSONObject("attributeConfig");
		List<String> validTypeList = JSON.parseArray(configObj.getString("validType"), String.class);
		if(CollectionUtils.isNotEmpty(validTypeList)) {
			if(validTypeList.contains("workdate")) {
				Long processTaskId = configObj.getLong("processTaskId");
				String channelUuid = configObj.getString("channelUuid");
				String worktimeUuid = null;
				if(processTaskId != null) {
					ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
					if(processTaskVo == null) {
						throw new ProcessTaskNotFoundException(processTaskId.toString());
					}
					worktimeUuid = processTaskVo.getWorktimeUuid();
				}else if(StringUtils.isNotBlank(channelUuid)){
					ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
					if(channelVo == null) {
						throw new ChannelNotFoundException(channelUuid);
					}
					worktimeUuid = channelVo.getWorktimeUuid();
				}else {
					throw new FormIllegalParameterException("config参数中必须包含'processTaskId'或'channelUuid'");
				}
				int count = worktimeMapper.checkIsWithinWorktimeRange(worktimeUuid, data);
				if(count > 0) {
					return true;
				}else {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public String getValue(AttributeDataVo attributeDataVo) {
		return null;
	}

}
