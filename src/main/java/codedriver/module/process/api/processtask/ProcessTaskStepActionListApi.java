package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.core.ProcessTaskRuntimeException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskStepNotFoundException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
public class ProcessTaskStepActionListApi extends ApiComponentBase {
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private ProcessStepHandlerMapper processStepHandlerMapper;
	
	@Override
	public String getToken() {
		return "processtask/step/action/list";
	}

	@Override
	public String getName() {
		return "工单步骤当前用户操作权限列表获取接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
		@Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "工单步骤id")
	})
	@Output({
		@Param(name = "Return", explode = ValueTextVo[].class, desc = "当前用户操作权限列表")
	})
	@Description(desc = "工单步骤当前用户操作权限列表获取接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long processTaskId = jsonObj.getLong("processTaskId");
		ProcessTaskStepVo processTaskStepVo = null;
		ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
		if(processTaskVo == null) {
			throw new ProcessTaskNotFoundException(processTaskId.toString());
		}
		Map<String, String> customButtonMap = new HashMap<>();
		Long processTaskStepId = jsonObj.getLong("processTaskStepId");
		if(processTaskStepId != null) {
			processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepId);
			if(processTaskStepVo == null) {
				throw new ProcessTaskStepNotFoundException(processTaskStepId.toString());
			}
			if(!processTaskId.equals(processTaskStepVo.getProcessTaskId())) {
				throw new ProcessTaskRuntimeException("步骤：'" + processTaskStepId + "'不是工单：'" + processTaskId + "'的步骤");
			}
			/** 节点管理按钮映射 **/
			ProcessStepHandlerVo processStepHandlerVo = processStepHandlerMapper.getProcessStepHandlerByHandler(processTaskStepVo.getHandler());
			if(processStepHandlerVo != null && StringUtils.isNotBlank(processStepHandlerVo.getConfig())) {
				JSONObject globalConfig = JSON.parseObject(processStepHandlerVo.getConfig());
				if(MapUtils.isNotEmpty(globalConfig)) {
					JSONArray customButtonList = globalConfig.getJSONArray("customButtonList");
					if(CollectionUtils.isNotEmpty(customButtonList)) {
						for(int i = 0; i < customButtonList.size(); i++) {
							JSONObject customButton = customButtonList.getJSONObject(i);
							String value = customButton.getString("value");
							if(StringUtils.isNotBlank(value)) {
								customButtonMap.put(customButton.getString("name"), value);
							}
						}
					}
				}
			}
			/** 节点设置按钮映射 **/
			String stepConfig = processTaskMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());
			JSONObject stepConfigObj = JSON.parseObject(stepConfig);
			if(MapUtils.isNotEmpty(stepConfigObj)) {
				JSONArray customButtonList = stepConfigObj.getJSONArray("customButtonList");
				if(CollectionUtils.isNotEmpty(customButtonList)) {
					for(int i = 0; i < customButtonList.size(); i++) {
						JSONObject customButton = customButtonList.getJSONObject(i);
						String value = customButton.getString("value");
						if(StringUtils.isNotBlank(value)) {
							customButtonMap.put(customButton.getString("name"), value);
						}
					}
				}
			}
		}
		List<ValueTextVo> resultList = new ArrayList<>();
		List<String> actionList = ProcessStepHandlerFactory.getHandler().getProcessTaskStepActionList(processTaskId, processTaskStepId);
		//TODO automatic ，临时处理，重构后删去
		if(processTaskStepVo!=null&&ProcessStepHandler.AUTOMATIC.getHandler().equals(processTaskStepVo.getHandler())) {
//			actionList = Arrays.asList("view", "transfer", "pocesstaskview", "work", "complete");
			actionList.remove(ProcessTaskStepAction.STARTPROCESS.getValue());
			actionList.remove(ProcessTaskStepAction.START.getValue());
			actionList.remove(ProcessTaskStepAction.ACTIVE.getValue());
			actionList.remove(ProcessTaskStepAction.RETREAT.getValue());
			actionList.remove(ProcessTaskStepAction.ACCEPT.getValue());
			actionList.remove(ProcessTaskStepAction.WORK.getValue());
			actionList.remove(ProcessTaskStepAction.ABORT.getValue());
			actionList.remove(ProcessTaskStepAction.RECOVER.getValue());
			actionList.remove(ProcessTaskStepAction.BACK.getValue());
			actionList.remove(ProcessTaskStepAction.UPDATE.getValue());
			actionList.remove(ProcessTaskStepAction.COMMENT.getValue());
			actionList.remove(ProcessTaskStepAction.CREATESUBTASK.getValue());
			actionList.remove(ProcessTaskStepAction.URGE.getValue());
		}
		//
		
		for(String action : actionList) {
			String text = customButtonMap.get(action);//自定义优先
			if(StringUtils.isBlank(text)) {
				text = ProcessTaskStepAction.getText(action);
			}
			if(StringUtils.isNotBlank(text)) {
				ValueTextVo valueText = new ValueTextVo();
				valueText.setValue(action);
				valueText.setText(text);
				resultList.add(valueText);
			}
		}
		return resultList;
	}

}
