package neatlogic.module.process.api.processstep;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessStepType;
import neatlogic.module.process.dao.mapper.process.ProcessStepHandlerMapper;
import neatlogic.framework.process.dto.ProcessStepHandlerVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

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
 * @program: neatlogic
 * @description:
 * @create: 2020-03-18 11:05
 **/
@Service
@AuthAction(action = PROCESS_BASE.class)
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
                if (ProcessStepType.END.getValue().equals(handler.getType())) {
                    continue;
                }
        		if(StringUtils.isBlank(keywork) || handler.getName().toLowerCase().contains(keywork.toLowerCase())) {
                    ProcessStepHandlerVo handlerConfig = handlerConfigMap.get(handler.getHandler());
                    IProcessStepInternalHandler processStepUtilHandler = ProcessStepInternalHandlerFactory.getHandler(handler.getHandler());
                    if (processStepUtilHandler != null) {
                        JSONObject config = processStepUtilHandler.makeupConfig(handlerConfig != null ? handlerConfig.getConfig() : null);
                        if (MapUtils.isNotEmpty(config)) {
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
