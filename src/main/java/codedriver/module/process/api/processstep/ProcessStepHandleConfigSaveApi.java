package codedriver.module.process.api.processstep;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.notify.core.NotifyPolicyInvokerManager;
import codedriver.framework.notify.dto.NotifyPolicyInvokerVo;
import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.PROCESS_STEP_HANDLER_MODIFY;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-18 15:26
 **/
@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_STEP_HANDLER_MODIFY.class)
public class ProcessStepHandleConfigSaveApi extends PrivateApiComponentBase {

    @Autowired
    private NotifyPolicyInvokerManager notifyPolicyInvokerManager;

    @Autowired
    private ProcessStepHandlerMapper stepHandlerMapper;

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
    	List<ProcessStepHandlerVo> processStepHandlerList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("processStepHandlerList")), ProcessStepHandlerVo.class);
    	for(ProcessStepHandlerVo stepHandlerVo :processStepHandlerList) {
            IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(stepHandlerVo.getHandler());
            if (handler != null) {
                JSONObject config = handler.makeupConfig(stepHandlerVo.getConfig());
                if (MapUtils.isNotEmpty(config)) {
                    stepHandlerVo.setConfig(config.toJSONString());
                    stepHandlerMapper.deleteProcessStepHandlerConfigByHandler(stepHandlerVo.getHandler());
                    stepHandlerMapper.insertProcessStepHandlerConfig(stepHandlerVo);
                    notifyPolicyInvokerManager.removeInvoker(stepHandlerVo.getHandler());
                    JSONObject notifyPolicyConfig = config.getJSONObject("notifyPolicyConfig");
                    Long policyId = notifyPolicyConfig.getLong("policyId");
                    if (policyId != null) {
                    	NotifyPolicyInvokerVo notifyPolicyInvokerVo = new NotifyPolicyInvokerVo();
                    	notifyPolicyInvokerVo.setPolicyId(policyId);
                    	notifyPolicyInvokerVo.setInvoker(stepHandlerVo.getHandler());
                    	JSONObject notifyPolicyInvokerConfig = new JSONObject();
                    	notifyPolicyInvokerConfig.put("function", "processstephandler");
                    	notifyPolicyInvokerConfig.put("name", "节点管理-" + ProcessStepHandlerType.getName(stepHandlerVo.getHandler()));
                    	notifyPolicyInvokerConfig.put("handler", stepHandlerVo.getHandler());
                    	notifyPolicyInvokerVo.setConfig(notifyPolicyInvokerConfig.toJSONString());
                    	notifyPolicyInvokerManager.addInvoker(notifyPolicyInvokerVo);
                    }
    			} 			
    		}
    	}        
        return null;
    }
}
