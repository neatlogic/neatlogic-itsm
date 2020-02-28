package codedriver.module.process.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.exception.process.ProcessNameRepeatException;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.dto.ProcessDraftVo;
import codedriver.module.process.dto.ProcessFormVo;
import codedriver.module.process.dto.ProcessSlaVo;
import codedriver.module.process.dto.ProcessStepFormAttributeVo;
import codedriver.module.process.dto.ProcessStepNotifyTemplateVo;
import codedriver.module.process.dto.ProcessStepRelVo;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.module.process.dto.ProcessVo;

@Service
public class ProcessServiceImpl implements ProcessService {

	@Autowired
	private ProcessMapper processMapper;

	@Autowired
	private FormMapper formMapper;

	@Override
	public ProcessVo getProcessByUuid(String processUuid) {
		return processMapper.getProcessByUuid(processUuid);
	}

	@Override
	public ProcessFormVo getProcessFormByProcessUuid(String processUuid) {
		return processMapper.getProcessFormByProcessUuid(processUuid);
	}

	@Override
	public List<ProcessStepFormAttributeVo> getProcessStepFormAttributeByStepUuid(ProcessStepFormAttributeVo processStepFormAttributeVo) {
		return processMapper.getProcessStepFormAttributeByStepUuid(processStepFormAttributeVo);
	}

	@Override
	public ProcessStepVo getProcessStartStep(String processUuid) {
		ProcessStepVo processStepVo = new ProcessStepVo();
		processStepVo.setProcessUuid(processUuid);
		processStepVo.setType(ProcessStepType.START.getValue());
		List<ProcessStepVo> processStepList = processMapper.searchProcessStep(processStepVo);
		if (processStepList != null && processStepList.size() == 1) {
			ProcessStepVo startStep = processStepList.get(0);
			startStep.setFormAttributeList(processMapper.getProcessStepFormAttributeByStepUuid(new ProcessStepFormAttributeVo(startStep.getUuid(), null)));
			ProcessFormVo processFormVo = processMapper.getProcessFormByProcessUuid(processUuid);
			if (processFormVo != null) {
				startStep.setFormUuid(processFormVo.getFormUuid());
			}
			return startStep;
		}
		return null;
	}

	@Override
	public List<ProcessStepVo> searchProcessStep(ProcessStepVo processStepVo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int saveProcess(ProcessVo processVo) {
		if (processMapper.checkProcessNameIsRepeat(processVo) > 0) {
			throw new ProcessNameRepeatException(processVo.getName());
		}
		String uuid = processVo.getUuid();
		if (processMapper.checkProcessIsExists(processVo.getUuid()) > 0) {
			processMapper.deleteProcessStepWorkerPolicyByProcessUuid(uuid);
			processMapper.deleteProcessStepByProcessUuid(uuid);
			processMapper.deleteProcessStepNotifyTemplateByProcessUuid(uuid);
			processMapper.deleteProcessStepRelByProcessUuid(uuid);
			processMapper.deleteProcessStepFormAttributeByProcessUuid(uuid);
			processMapper.deleteProcessFormByProcessUuid(uuid);
			processMapper.deleteProcessSlaByProcessUuid(uuid);
			processMapper.updateProcess(processVo);
		} else {
			processVo.setFcu(UserContext.get().getUserId());
			processMapper.insertProcess(processVo);
		}
		// 删除草稿
		ProcessDraftVo processDraftVo = new ProcessDraftVo();
		processDraftVo.setProcessUuid(uuid);
		processDraftVo.setFcu(UserContext.get().getUserId());
		processMapper.deleteProcessDraft(processDraftVo);

		String formUuid = processVo.getFormUuid();
		if (StringUtils.isNotBlank(formUuid)) {
			processMapper.insertProcessForm(new ProcessFormVo(uuid, formUuid));
		}

		if (processVo.getSlaList() != null && processVo.getSlaList().size() > 0) {
			for (ProcessSlaVo slaVo : processVo.getSlaList()) {
				if (slaVo.getProcessStepUuidList().size() > 0) {
					processMapper.insertProcessSla(slaVo);
					for (String stepUuid : slaVo.getProcessStepUuidList()) {
						processMapper.insertProcessStepSla(stepUuid, slaVo.getUuid());
					}
				}
			}
		}

		if (processVo.getStepList() != null && processVo.getStepList().size() > 0) {

			for (ProcessStepVo stepVo : processVo.getStepList()) {
				processMapper.insertProcessStep(stepVo);
				if (stepVo.getFormAttributeList() != null && stepVo.getFormAttributeList().size() > 0) {
					for (ProcessStepFormAttributeVo processStepAttributeVo : stepVo.getFormAttributeList()) {
						processMapper.insertProcessStepFormAttribute(processStepAttributeVo);
					}
				}
				if (stepVo.getWorkerPolicyList() != null && stepVo.getWorkerPolicyList().size() > 0) {
					for (ProcessStepWorkerPolicyVo processStepWorkerPolicyVo : stepVo.getWorkerPolicyList()) {
						processMapper.insertProcessStepWorkerPolicy(processStepWorkerPolicyVo);
					}
				}
				if (stepVo.getTemplateUuidList() != null && stepVo.getTemplateUuidList().size() > 0) {
					for (String templateUuid : stepVo.getTemplateUuidList()) {
						processMapper.insertProcessStepNotifyTemplate(new ProcessStepNotifyTemplateVo(uuid, stepVo.getUuid(), templateUuid));
					}
				}
			}
		}

		if (processVo.getStepRelList() != null && processVo.getStepRelList().size() > 0) {
			for (ProcessStepRelVo stepRelVo : processVo.getStepRelList()) {
				processMapper.insertProcessStepRel(stepRelVo);
			}
		}

		return 1;
	}

}
