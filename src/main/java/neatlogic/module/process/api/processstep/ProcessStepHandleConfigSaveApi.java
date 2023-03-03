package neatlogic.module.process.api.processstep;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dependency.core.DependencyManager;
import neatlogic.framework.notify.dao.mapper.NotifyMapper;
import neatlogic.framework.notify.exception.NotifyPolicyNotFoundException;
import neatlogic.framework.process.dao.mapper.ProcessStepHandlerMapper;
import neatlogic.framework.process.dto.ProcessStepHandlerVo;
import neatlogic.framework.notify.dto.InvokeNotifyPolicyConfigVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.process.auth.PROCESS_STEP_HANDLER_MODIFY;

import neatlogic.module.process.dependency.handler.NotifyPolicyProcessStepHandlerDependencyHandler;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @program: neatlogic
 * @description:
 * @create: 2020-03-18 15:26
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_STEP_HANDLER_MODIFY.class)
public class ProcessStepHandleConfigSaveApi extends PrivateApiComponentBase {

    @Resource
    private ProcessStepHandlerMapper stepHandlerMapper;

    @Resource
    private NotifyMapper notifyMapper;

    @Override
    public String getToken() {
        return "process/step/handler/config/save";
    }

    @Override
    public String getName() {
        return "流程节点组件配置保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    	@Param(name = "processStepHandlerList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "流程节点组件配置信息列表")
    })
    @Description(desc = "流程节点组件配置保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	List<ProcessStepHandlerVo> processStepHandlerList = jsonObj.getJSONArray("processStepHandlerList").toJavaList(ProcessStepHandlerVo.class);
    	for(ProcessStepHandlerVo stepHandlerVo :processStepHandlerList) {
    	    String handler = stepHandlerVo.getHandler();
            IProcessStepInternalHandler processStepInternalHandler = ProcessStepInternalHandlerFactory.getHandler(handler);
            if (processStepInternalHandler != null) {
                JSONObject config = processStepInternalHandler.makeupConfig(stepHandlerVo.getConfig());
                if (MapUtils.isNotEmpty(config)) {
                    stepHandlerVo.setConfig(config.toJSONString());
                    stepHandlerMapper.replaceProcessStepHandlerConfig(stepHandlerVo);
                    DependencyManager.delete(NotifyPolicyProcessStepHandlerDependencyHandler.class, handler);
                    JSONObject notifyPolicyConfig = config.getJSONObject("notifyPolicyConfig");
                    InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = JSONObject.toJavaObject(notifyPolicyConfig, InvokeNotifyPolicyConfigVo.class);
                    if (invokeNotifyPolicyConfigVo != null) {
                        Long policyId = invokeNotifyPolicyConfigVo.getPolicyId();
                        if (policyId != null) {
                            if(notifyMapper.checkNotifyPolicyIsExists(policyId) == 0){
                                throw new NotifyPolicyNotFoundException(policyId.toString());
                            }
                            DependencyManager.insert(NotifyPolicyProcessStepHandlerDependencyHandler.class, policyId, handler);
                        }
                    }
    			} 			
    		}
    	}        
        return null;
    }
}
