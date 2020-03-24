package codedriver.module.process.api.processstep;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.service.ProcessStepHandlerService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-18 11:05
 **/
@Service
public class ProcessStepHandlerSearchApi extends ApiComponentBase {

    @Autowired
    private ProcessStepHandlerService processStepService;

    @Override
    public String getToken() {
        return "process/step/handler/search";
    }

    @Override
    public String getName() {
        return "流程节点组件检索接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param( name = "name", desc = "流程节点组件名称", type = ApiParamType.STRING)
    })
    @Output({
            @Param( name = "stepHandlerList", desc = "流程节点组件列表", explode = ProcessStepHandlerVo[].class)
    })
    @Description(desc = "流程节点组件检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        List<ProcessStepHandlerVo> processStepHandlerList = processStepService.searchProcessComponent(jsonObj.getString("name"));
        returnObj.put("stepHandlerList", processStepHandlerList);
        return returnObj;
    }
}
