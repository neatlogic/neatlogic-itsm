package codedriver.module.process.service;

import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.dto.ProcessStepHandlerVo;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;

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
 * @create: 2020-03-18 11:17
 **/
@Service
public class ProcessStepHandlerServiceImpl implements ProcessStepHandlerService {

    @Autowired
    private ProcessStepHandlerMapper stepHandlerMapper;

    @Override
    public List<ProcessStepHandlerVo> searchProcessComponent(String name) {
    	List<ProcessStepHandlerVo> resultList = new ArrayList<>();
    	Map<String, ProcessStepHandlerVo> handlerConfigMap = new HashMap<>();
        List<ProcessStepHandlerVo> handlerConfigList = stepHandlerMapper.getProcessStepHandlerConfig();
        for(ProcessStepHandlerVo handlerConfig : handlerConfigList) {
        	handlerConfigMap.put(handlerConfig.getHandler(), handlerConfig);
        }
        List<ProcessStepHandlerVo> handlerList = ProcessStepHandlerFactory.getActiveProcessStepHandler();
        if (CollectionUtils.isNotEmpty(handlerList)){
            for (ProcessStepHandlerVo handler : handlerList){
            	if(ProcessStepType.PROCESS.getValue().equals(handler.getType())) {
            		if(StringUtils.isBlank(name) || handler.getName().contains(name)) {
            			ProcessStepHandlerVo handlerConfig = handlerConfigMap.get(handler.getHandler());
            			if(handlerConfig == null) {
            				handlerConfig = new ProcessStepHandlerVo();
            				handlerConfig.setHandler(handler.getHandler());
            			}
            			handlerConfig.setName(handler.getName());
        				resultList.add(handlerConfig);
            		}
            	}
            }
        }
        return resultList;
    }

    @Override
    public void saveStepHandlerConfig(ProcessStepHandlerVo stepHandlerVo) {
        stepHandlerMapper.deleteProcessStepHandlerConfigByHandler(stepHandlerVo.getHandler());
        stepHandlerMapper.insertProcessStepHandlerConfig(stepHandlerVo);
    }
}
