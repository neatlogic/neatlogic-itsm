package codedriver.module.process.api.processstep;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessStepHandlerService;

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
    private ProcessStepHandlerService stepHandlerService;

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

    @Input( {@Param( name = "handler", type = ApiParamType.STRING, desc = "流程节点组件", isRequired = true),
            @Param( name = "config", type = ApiParamType.JSONOBJECT, desc = "流程节点组件配置")})
    @Description(desc = "流程节点组件配置保存接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	ProcessStepHandlerVo handlerVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ProcessStepHandlerVo>() {});
        stepHandlerService.saveStepHandlerConfig(handlerVo);
        return null;
    }
}
