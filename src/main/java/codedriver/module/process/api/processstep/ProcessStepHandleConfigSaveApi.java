package codedriver.module.process.api.processstep;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.notify.core.NotifyPolicyInvokerManager;
import codedriver.framework.notify.dto.NotifyPolicyInvokerVo;
import codedriver.framework.process.constvalue.ProcessStepHandler;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

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
public class ProcessStepHandleConfigSaveApi extends ApiComponentBase {

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
    	@Param( name = "handler", type = ApiParamType.STRING, desc = "流程节点组件", isRequired = true),
        @Param( name = "config", type = ApiParamType.JSONOBJECT, desc = "流程节点组件配置", isRequired = true)
    })
    @Description(desc = "流程节点组件配置保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	ProcessStepHandlerVo stepHandlerVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessStepHandlerVo>() {});

        stepHandlerMapper.deleteProcessStepHandlerConfigByHandler(stepHandlerVo.getHandler());
        stepHandlerMapper.insertProcessStepHandlerConfig(stepHandlerVo);
        notifyPolicyInvokerManager.removeInvoker(stepHandlerVo.getHandler());
        JSONObject config = jsonObj.getJSONObject("config");
        JSONObject notifyPolicyConfig = config.getJSONObject("notifyPolicyConfig");
        Long policyId = notifyPolicyConfig.getLong("policyId");
        if(policyId != null) {
        	NotifyPolicyInvokerVo notifyPolicyInvokerVo = new NotifyPolicyInvokerVo();
        	notifyPolicyInvokerVo.setPolicyId(policyId);
        	notifyPolicyInvokerVo.setInvoker(stepHandlerVo.getHandler());
        	JSONObject notifyPolicyInvokerConfig = new JSONObject();
        	notifyPolicyInvokerConfig.put("function", "processstephandler");
        	notifyPolicyInvokerConfig.put("name", "节点管理-" + ProcessStepHandler.getName(stepHandlerVo.getHandler()));
        	notifyPolicyInvokerConfig.put("handler", stepHandlerVo.getHandler());
        	notifyPolicyInvokerVo.setConfig(notifyPolicyInvokerConfig.toJSONString());
        	notifyPolicyInvokerManager.addInvoker(notifyPolicyInvokerVo);
        }
        return null;
    }
}
