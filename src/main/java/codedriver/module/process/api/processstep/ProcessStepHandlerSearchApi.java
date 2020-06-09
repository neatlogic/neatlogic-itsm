package codedriver.module.process.api.processstep;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: codedriver
 * @description:
 * @create: 2020-03-18 11:05
 **/
@Service
public class ProcessStepHandlerSearchApi extends ApiComponentBase {

    @Autowired
    private ProcessStepHandlerMapper stepHandlerMapper;

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
        List<ProcessStepHandlerVo> processStepHandlerList = new ArrayList<>();
    	Map<String, ProcessStepHandlerVo> handlerConfigMap = new HashMap<>();
        List<ProcessStepHandlerVo> handlerConfigList = stepHandlerMapper.getProcessStepHandlerConfig();
        for(ProcessStepHandlerVo handlerConfig : handlerConfigList) {
        	handlerConfigMap.put(handlerConfig.getHandler(), handlerConfig);
        }
        List<ProcessStepHandlerVo> handlerList = ProcessStepHandlerFactory.getActiveProcessStepHandler();
        if (CollectionUtils.isNotEmpty(handlerList)){
        	String name = jsonObj.getString("name");
            for (ProcessStepHandlerVo handler : handlerList){
            	if(ProcessStepType.PROCESS.getValue().equals(handler.getType())) {
            		if(StringUtils.isBlank(name) || handler.getName().contains(name)) {
            			ProcessStepHandlerVo handlerConfig = handlerConfigMap.get(handler.getHandler());
            			if(handlerConfig == null) {
            				handlerConfig = new ProcessStepHandlerVo();
            				handlerConfig.setHandler(handler.getHandler());
            			}
            			handlerConfig.setName(handler.getName());
            			processStepHandlerList.add(handlerConfig);
            		}
            	}
            }
        }
        returnObj.put("stepHandlerList", processStepHandlerList);
        return returnObj;
    }
}
