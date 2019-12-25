package codedriver.module.process.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.exception.ProcessDuplicateNameException;
import codedriver.module.process.constvalue.ProcessStepType;
import codedriver.module.process.dto.ProcessAttributeVo;
import codedriver.module.process.dto.ProcessConfigHistoryVo;
import codedriver.module.process.dto.ProcessFormVo;
import codedriver.module.process.dto.ProcessStepAttributeVo;
import codedriver.module.process.dto.ProcessStepFormAttributeVo;
import codedriver.module.process.dto.ProcessStepRelVo;
import codedriver.module.process.dto.ProcessStepTimeoutPolicyVo;
import codedriver.module.process.dto.ProcessStepVo;
import codedriver.module.process.dto.ProcessStepWorkerPolicyVo;
import codedriver.module.process.dto.ProcessVo;

@Service
public class ProcessServiceImpl implements ProcessService {

	@Autowired
	private ProcessMapper processMapper;


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
			startStep.setAttributeList(processMapper.getProcessStepAttributeByStepUuid(new ProcessStepAttributeVo(startStep.getUuid(), null)));
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
		if(processMapper.checkProcessIsDuplicateName(processVo) > 0) {
			throw new ProcessDuplicateNameException(processVo.getName());
		}
		if (processMapper.checkProcessIsExists(processVo.getUuid()) > 0) {
			processMapper.deleteProcessAttributeByProcessUuid(processVo.getUuid());
			processMapper.deleteProcessStepWorkerPolicyByProcessUuid(processVo.getUuid());
			processMapper.deleteProcessStepByProcessUuid(processVo.getUuid());
			processMapper.deleteProcessStepRelByProcessUuid(processVo.getUuid());
			processMapper.deleteProcessStepAttributeByProcessUuid(processVo.getUuid());
			processMapper.deleteProcessStepFormAttributeByProcessUuid(processVo.getUuid());
			processMapper.deleteProcessStepTimeoutPolicyByProcessUuid(processVo.getUuid());
		}
		processMapper.replaceProcess(processVo);
		if (StringUtils.isNotBlank(processVo.getFormUuid())) {
			processMapper.replaceProcessForm(processVo.getUuid(), processVo.getFormUuid());
		}
		if (processVo.getAttributeList() != null && processVo.getAttributeList().size() > 0) {
			for (ProcessAttributeVo processAttributeVo : processVo.getAttributeList()) {
				processMapper.insertProcessAttribute(processAttributeVo);
			}
		}

		if (processVo.getStepList() != null && processVo.getStepList().size() > 0) {
			for (ProcessStepVo stepVo : processVo.getStepList()) {
				processMapper.insertProcessStep(stepVo);
				if (stepVo.getAttributeList() != null && stepVo.getAttributeList().size() > 0) {
					for (ProcessStepAttributeVo processStepAttributeVo : stepVo.getAttributeList()) {
						processMapper.insertProcessStepAttribute(processStepAttributeVo);
					}
				}
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
				if (stepVo.getTimeoutPolicyList() != null && stepVo.getTimeoutPolicyList().size() > 0) {
					for (ProcessStepTimeoutPolicyVo processStepTimeoutPolicyVo : stepVo.getTimeoutPolicyList()) {
						processMapper.insertProcessStepTimeoutPolicy(processStepTimeoutPolicyVo);
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

	@Override
	public List<ProcessStepAttributeVo> getProcessStepAttributeByStepUuid(ProcessStepAttributeVo processStepAttributeVo) {
		return processMapper.getProcessStepAttributeByStepUuid(processStepAttributeVo);
	}

	@Override
	public ProcessConfigHistoryVo getProcessConfigHistoryByMd(String historyMd) {
		return processMapper.getProcessConfigHistoryByMd(historyMd);
	}

	@Override
	public int saveProcessConfigHistory(ProcessConfigHistoryVo processConfigHistoryVo) {
		return processMapper.insertProcessConfigHistory(processConfigHistoryVo);
	}

}
