package codedriver.module.process.workerpolicy.handler;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.workerpolicy.core.IWorkerPolicyHandler;

//@Service
public class AttributeWorkerPolicyHandler implements IWorkerPolicyHandler {

	@Override
	public String getType() {
		//return WorkerPolicy.ATTRIBUTE.getValue();
		return "attribute";
	}
	
	@Override
	public String getName() {
		//return WorkerPolicy.ATTRIBUTE.getText();
		return "attribute";
	}
	
	@Autowired
	private ProcessTaskMapper processTaskMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private TeamMapper teamMapper;

	@Override
	public List<ProcessTaskStepWorkerVo> execute(ProcessTaskStepWorkerPolicyVo workerPolicyVo, ProcessTaskStepVo currentProcessTaskStepVo) {
		List<ProcessTaskStepWorkerVo> workerList = new ArrayList<>();
		if (workerPolicyVo.getConfigObjList() != null) {
			/*List<ProcessTaskAttributeValueVo> attributeValueList = processTaskMapper.getProcessTaskAttributeValueByProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
			if (attributeValueList != null && attributeValueList.size() > 0) {
				for (int i = 0; i < workerPolicyVo.getConfigObjList().size(); i++) {
					String attributeUuid = workerPolicyVo.getConfigObjList().getString(i);
					for (ProcessTaskAttributeValueVo attributeValue : attributeValueList) {
						if (attributeUuid.equals(attributeValue.getAttributeUuid())) {
							AttributeVo attributeVo = attributeMapper.getAttributeByUuid(attributeUuid);
							if (attributeVo != null && (attributeVo.getHandler().equals(AttributeHandler.USER.getValue()) || attributeVo.getHandler().equals(AttributeHandler.MUSER.getValue()))) {
								String userId = attributeValue.getValue();
								if (StringUtils.isNotBlank(userId)) {
									if (userMapper.getUserByUserId(userId) != null) {
										ProcessTaskStepWorkerVo workerVo = new ProcessTaskStepWorkerVo();
										workerVo.setUserId(userId);
										workerVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
										workerVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
										if (!workerList.contains(workerVo)) {
											workerList.add(workerVo);
										}
									}
								}
							} else if (attributeVo != null && (attributeVo.getHandler().equals(AttributeHandler.TEAM.getValue()) || attributeVo.getHandler().equals(AttributeHandler.MTEAM.getValue()))) {
								String tid = null;
								try {
									tid = attributeValue.getValue();
								} catch (Exception ex) {

								}
								if (tid != null && teamMapper.getTeamByUuid(tid) != null) {
									ProcessTaskStepWorkerVo workerVo = new ProcessTaskStepWorkerVo();
									workerVo.setTeamUuid(tid);
									workerVo.setProcessTaskId(currentProcessTaskStepVo.getProcessTaskId());
									workerVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
									if (!workerList.contains(workerVo)) {
										workerList.add(workerVo);
									}
								}
							}
						}
					}
				}
			}*/
		}
		return workerList;
	}
}
