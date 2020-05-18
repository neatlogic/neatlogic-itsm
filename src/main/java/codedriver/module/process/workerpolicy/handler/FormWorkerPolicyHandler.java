package codedriver.module.process.workerpolicy.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.constvalue.WorkerPolicy;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.workerpolicy.core.IWorkerPolicyHandler;

@Service
public class FormWorkerPolicyHandler implements IWorkerPolicyHandler {

	@Override
	public String getType() {
		return WorkerPolicy.FORM.getValue();
	}

	@Override
	public String getName() {
		return WorkerPolicy.FORM.getText();
	}
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;
	
	@Autowired
	private UserMapper userMapper;

	@Override
	public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskStepWorkerVo> processTaskStepWorkerList = new ArrayList<>();
		if (MapUtils.isEmpty(workerPolicyVo.getConfigObj())) {
			return processTaskStepWorkerList;
		}
		String attributeUuid = workerPolicyVo.getConfigObj().getString("attributeUuid");
		if(StringUtils.isBlank(attributeUuid)) {
			return processTaskStepWorkerList;
		}
		List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
		if(CollectionUtils.isEmpty(processTaskFormAttributeDataList)) {
			return processTaskStepWorkerList;
		}
		for(ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
			if(!attributeUuid.equals(processTaskFormAttributeDataVo.getAttributeUuid())) {
				continue;
			}
			String data = processTaskFormAttributeDataVo.getData();
			if(StringUtils.isBlank(data)) {
				return processTaskStepWorkerList;
			}
			if(data.startsWith("[") && data.endsWith("]")) {
				List<String> dataList = JSON.parseArray(data, String.class);
				if(CollectionUtils.isEmpty(dataList)) {
					return processTaskStepWorkerList;
				}
				for(String userId : dataList) {
					if(userMapper.getUserBaseInfoByUuid(userId) != null) {
						processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), GroupSearch.USER.getValue(), userId));
					}
				}
			}else {
				if(userMapper.getUserBaseInfoByUuid(data) != null) {
					processTaskStepWorkerList.add(new ProcessTaskStepWorkerVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), GroupSearch.USER.getValue(), data));				
				}
			}
			return processTaskStepWorkerList;
		}
		//List<ProcessTaskAssignUserVo> assignUserList = processTaskMapper.getProcessAssignUserByToStepId(currentProcessTaskStepVo.getId());
		return processTaskStepWorkerList;
	}
}
