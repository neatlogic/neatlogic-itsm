package codedriver.module.process.api.processstep;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessStepHandlerSearchApi extends PrivateApiComponentBase {

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
            @Param( name = "keywork", type = ApiParamType.STRING, xss = true, desc = "流程节点组件名称")
    })
    @Output({
            @Param( name = "stepHandlerList", explode = ProcessStepHandlerVo[].class, desc = "流程节点组件列表")
    })
    @Description(desc = "流程节点组件检索接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
    	Map<String, ProcessStepHandlerVo> handlerConfigMap = new HashMap<>();
        List<ProcessStepHandlerVo> handlerConfigList = stepHandlerMapper.getProcessStepHandlerConfig();
        for(ProcessStepHandlerVo handlerConfig : handlerConfigList) {
        	handlerConfigMap.put(handlerConfig.getHandler(), handlerConfig);
        }

        List<ProcessStepHandlerVo> processStepHandlerList = new ArrayList<>();
        List<ProcessStepHandlerVo> handlerList = ProcessStepHandlerFactory.getActiveProcessStepHandler();
        if (CollectionUtils.isNotEmpty(handlerList)){
        	String keywork = jsonObj.getString("keywork");
            for (ProcessStepHandlerVo handler : handlerList){
        		if(StringUtils.isBlank(keywork) || handler.getName().toLowerCase().contains(keywork.toLowerCase())) {
        			ProcessStepHandlerVo handlerConfig = handlerConfigMap.get(handler.getHandler());
        			IProcessStepUtilHandler processStepUtilHandler= ProcessStepUtilHandlerFactory.getHandler(handler.getHandler());
        			if(processStepUtilHandler != null) {      				
        				JSONObject config = processStepUtilHandler.makeupConfig(handlerConfig != null ? handlerConfig.getConfig() : null);
        				if(MapUtils.isNotEmpty(config)) {
        					processStepHandlerList.add(new ProcessStepHandlerVo(handler.getHandler(), handler.getName(), config));
        				}      			
        			}
        		}
            }
        }
        returnObj.put("stepHandlerList", processStepHandlerList);
        return returnObj;
    }
}
