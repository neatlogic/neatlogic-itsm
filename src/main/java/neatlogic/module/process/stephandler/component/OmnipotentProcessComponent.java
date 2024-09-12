package neatlogic.module.process.stephandler.component;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import neatlogic.framework.process.autocompleterule.core.AutoCompleteRuleHandlerFactory;
import neatlogic.framework.process.autocompleterule.core.IAutoCompleteRuleHandler;
import neatlogic.framework.process.constvalue.ProcessStepHandlerType;
import neatlogic.framework.process.constvalue.ProcessStepMode;
import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskException;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerBase;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class OmnipotentProcessComponent extends ProcessStepHandlerBase {
	static Logger logger = LoggerFactory.getLogger(OmnipotentProcessComponent.class);

	@Resource
	private SelectContentByHashMapper selectContentByHashMapper;

	@Resource
	private IProcessStepHandlerUtil processStepHandlerUtil;

	@Override
	public String getHandler() {
		return ProcessStepHandlerType.OMNIPOTENT.getHandler();
	}

	@Override
	public String getType() {
		return ProcessStepHandlerType.OMNIPOTENT.getType();
	}

	@Override
	public ProcessStepMode getMode() {
		return ProcessStepMode.MT;
	}

	@SuppressWarnings("serial")
	@Override
	public JSONObject getChartConfig() {
		return new JSONObject() {
			{
				this.put("icon", "tsfont-circle-o");
				this.put("shape", "L-rectangle:R-rectangle");
				this.put("width", 68);
				this.put("height", 40);
			}
		};
	}

	@Override
	public String getName() {
		return ProcessStepHandlerType.OMNIPOTENT.getName();
	}

	@Override
	public int getSort() {
		return 4;
	}

	@Override
	protected int myActive(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		try {
			String configHash = currentProcessTaskStepVo.getConfigHash();
			String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(configHash);
			if (StringUtils.isNotBlank(stepConfig)) {
				int size = AutoCompleteRuleHandlerFactory.getHandlerSize();
				for (int i = 0; i < size; i++) {
					IAutoCompleteRuleHandler autoCompleteRuleHandler = AutoCompleteRuleHandlerFactory.getHandler(i);
					if (autoCompleteRuleHandler != null) {
						Integer autoCompleteRule = (Integer) JSONPath.read(stepConfig, autoCompleteRuleHandler.getHandler());
						if (Objects.equals(autoCompleteRule, 1)) {
							if (autoCompleteRuleHandler.execute(currentProcessTaskStepVo)) {
								break;
							}
						}
					}
				}
			}
			return 0;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ProcessTaskException(e);
		}
	}
	
	@Override
	protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, Set<ProcessTaskStepWorkerVo> workerSet) throws ProcessTaskException {
		return defaultAssign(currentProcessTaskStepVo, workerSet);
	}

	@Override
	protected int myStart(ProcessTaskStepVo processTaskStepVo) {
		return 0;
	}

	@Override
	public Boolean isAllowStart() {
		return true;
	}

	@Override
	protected Set<Long> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo, List<Long> nextStepIdList, Long nextStepId) throws ProcessTaskException {
		return defaultGetNext(nextStepIdList, nextStepId);
	}

	@Override
	protected int myRedo(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myStartProcess(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 1;
	}

	@Override
	public boolean isAsync() {
		return false;
	}

	@Override
	protected int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myComplete(ProcessTaskStepVo currentProcessTaskStepVo) {		
		return 1;
	}
	
	@Override
	protected int myCompleteAudit(ProcessTaskStepVo currentProcessTaskStepVo) {
		if(StringUtils.isNotBlank(currentProcessTaskStepVo.getError())) {
			currentProcessTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.CAUSE.getParamName(), currentProcessTaskStepVo.getError());
		}
		/** 处理历史记录 **/
		String action = currentProcessTaskStepVo.getParamObj().getString("action");
		processStepHandlerUtil.audit(currentProcessTaskStepVo, ProcessTaskAuditType.getProcessTaskAuditType(action));
		return 1;
	}

	@Override
	protected int myReapprovalAudit(ProcessTaskStepVo currentProcessTaskStepVo) {
		if(StringUtils.isNotBlank(currentProcessTaskStepVo.getError())) {
			currentProcessTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.CAUSE.getParamName(), currentProcessTaskStepVo.getError());
		}
		/** 处理历史记录 **/
		processStepHandlerUtil.audit(currentProcessTaskStepVo, ProcessTaskAuditType.REAPPROVAL);
		return 1;
	}

	@Override
	protected int myReapproval(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myRetreat(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 1;
	}

	@Override
	protected int myAbort(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myBack(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 0;
	}

	@Override
	protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myRecover(ProcessTaskStepVo currentProcessTaskStepVo) {
		return 0;
	}

	@Override
	protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList) throws ProcessTaskException {
		return 1;
	}

	@Override
	protected int mySaveDraft(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
		return 1;
	}

    @Override
    protected int myPause(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

}
