package codedriver.module.process.formattribute.handler;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.process.constvalue.ProcessFormHandler;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.AttributeDataVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.form.AttributeValidException;
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

	private final static String DATE_FORMAT = "yyyy-MM-dd";
	private final static String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
	
	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
	
	@Override
	public String getType() {
		return ProcessFormHandler.FORMDATE.getHandler();
	}

	@Override
	public boolean valid(AttributeDataVo attributeDataVo,JSONObject jsonObj) throws AttributeValidException {
		JSONObject configObj = jsonObj.getJSONObject("attributeConfig");
		List<String> validTypeList = JSON.parseArray(configObj.getString("validType"), String.class);
		if(CollectionUtils.isNotEmpty(validTypeList)) {
			if(validTypeList.contains("workdate")) {
				Long processTaskId = jsonObj.getLong("processTaskId");
				String channelUuid = jsonObj.getString("channelUuid");
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
				int count = 0;
				String data = attributeDataVo.getData();
				String styleType = configObj.getString("styleType");
				String showType = configObj.getString("showType");
				if(DATE_FORMAT.equals(showType)) {
					String date = data.replace(styleType, "-");
					try {
						dateFormatter.parse(date);
						count = worktimeMapper.checkIsWithinWorktime(worktimeUuid, date);
					}catch(DateTimeParseException ex) {
						String format = DATE_FORMAT.replace("-", styleType);
						throw new ParamIrregularException("参数“data”不符合“" +format+ "”格式要求"); 
					}
				}else if(DATETIME_FORMAT.equals(showType)){
					String dateTime = data.replace(styleType, "-");
					try {
						TemporalAccessor temporalAccessor = dateTimeFormatter.parse(dateTime);
						LocalDateTime endLocalDateTime = LocalDateTime.from(temporalAccessor);
						long datetime = endLocalDateTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli();
						count = worktimeMapper.checkIsWithinWorktimeRange(worktimeUuid, datetime);
					}catch(DateTimeParseException ex) {
						String format = DATETIME_FORMAT.replace("-", styleType);
						throw new ParamIrregularException("参数“data”不符合“" +format+ "”格式要求"); 
					}
				}
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
	public Object getValue(AttributeDataVo attributeDataVo, JSONObject configObj) {
		return attributeDataVo.getData();
	}

}
