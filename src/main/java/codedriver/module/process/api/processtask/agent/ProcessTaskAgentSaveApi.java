/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask.agent;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.exception.type.ParamIrregularException;
import codedriver.framework.exception.user.*;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskAgentMapper;
import codedriver.framework.process.dto.agent.ProcessTaskAgentCompobVo;
import codedriver.framework.process.dto.agent.ProcessTaskAgentInfoVo;
import codedriver.framework.process.dto.agent.ProcessTaskAgentTargetVo;
import codedriver.framework.process.dto.agent.ProcessTaskAgentVo;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskAgentSaveApi extends PrivateApiComponentBase {

	@Resource
	private ProcessTaskAgentMapper processTaskAgentMapper;
	@Resource
	private UserMapper userMapper;
	@Resource
	private ChannelMapper channelMapper;
	@Resource
	private CatalogMapper catalogMapper;

	@Override
	public String getToken() {
		return "processtask/agent/save";
	}

	@Override
	public String getName() {
		return "保存用户任务授权信息";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "beginTime", type = ApiParamType.LONG, isRequired = true,desc = "开始时间"),
		@Param(name = "endTime", type = ApiParamType.LONG, isRequired = true,desc = "结束时间"),
		@Param(name = "compobList", type = ApiParamType.JSONARRAY, isRequired = true,desc = "授权对象列表")
	})
	@Output({})
	@Description(desc = "保存用户任务授权信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ProcessTaskAgentInfoVo processTaskAgentInfoVo = JSONObject.toJavaObject(jsonObj, ProcessTaskAgentInfoVo.class);
		List<ProcessTaskAgentCompobVo> compobList = processTaskAgentInfoVo.getCompobList();
		if (CollectionUtils.isEmpty(compobList)) {
			throw new ParamIrregularException("compobList");
		}
		String fromUserUuid = UserContext.get().getUserUuid(true);
		List<Long> processTaskAgentIdList = processTaskAgentMapper.getProcessTaskAgentIdListByFromUserUuid(fromUserUuid);
		if (CollectionUtils.isNotEmpty(processTaskAgentIdList)) {
			processTaskAgentMapper.deleteProcessTaskAgentByFromUserUuid(fromUserUuid);
			processTaskAgentMapper.deleteProcessTaskAgentTargetByProcessTaskAgentIdList(processTaskAgentIdList);
		}

		Date beginTime = processTaskAgentInfoVo.getBeginTime();
		Date endTime = processTaskAgentInfoVo.getEndTime();
		ProcessTaskAgentVo processTaskAgentVo = new ProcessTaskAgentVo();
		processTaskAgentVo.setBeginTime(beginTime);
		processTaskAgentVo.setEndTime(endTime);
		processTaskAgentVo.setFromUserUuid(fromUserUuid);
		for (ProcessTaskAgentCompobVo compobVo : compobList) {
			processTaskAgentVo.setId(null);
			String toUserUuid = compobVo.getToUserUuid();
			if (userMapper.checkUserIsExists(toUserUuid) == 0) {
				throw new UserNotFoundException(toUserUuid);
			}
			processTaskAgentVo.setToUserUuid(toUserUuid);
			processTaskAgentMapper.insertProcessTaskAgent(processTaskAgentVo);
			ProcessTaskAgentTargetVo processTaskAgentTargetVo = new ProcessTaskAgentTargetVo();
			processTaskAgentTargetVo.setProcessTaskAgentId(processTaskAgentVo.getId());
			List<String> targetList = compobVo.getTargetList();
			for (String target : targetList) {
				if (target.contains("#")) {
					String[] split = target.split("#");
					if ("channel".equals(split[0])) {
						if (channelMapper.checkChannelIsExists(split[1]) == 0) {
							throw new ChannelNotFoundException(split[1]);
						}
						processTaskAgentTargetVo.setType(split[0]);
						processTaskAgentTargetVo.setTarget(split[1]);
						processTaskAgentMapper.insertProcessTaskAgentTarget(processTaskAgentTargetVo);
					} else if ("catalog".equals(split[0])) {
						if (catalogMapper.checkCatalogIsExists(split[1]) == 0) {
							throw new CatalogNotFoundException(split[1]);
						}
						processTaskAgentTargetVo.setType(split[0]);
						processTaskAgentTargetVo.setTarget(split[1]);
						processTaskAgentMapper.insertProcessTaskAgentTarget(processTaskAgentTargetVo);
					}
				}
			}
		}
		return null;
	}

}
