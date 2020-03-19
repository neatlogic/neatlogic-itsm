package codedriver.module.process.service;

import codedriver.framework.process.dao.mapper.ProcessStepHandlerMapper;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerFactory;
import codedriver.module.process.dto.ProcessStepHandlerVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Iterator;
import java.util.List;

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
        List<ProcessStepHandlerVo> handlerConfigList = stepHandlerMapper.getProcessStepHandlerConfig();
        List<ProcessStepHandlerVo> handlerList = ProcessStepHandlerFactory.getActiveProcessStepHandler();
        if (CollectionUtils.isNotEmpty(handlerList)){
            for (ProcessStepHandlerVo handler : handlerList){
                for (ProcessStepHandlerVo handlerConfig : handlerConfigList){
                    if (handler.getHandler().equals(handlerConfig.getHandler())){
                        handler.setConfig(handlerConfig.getConfig());
                        break;
                    }
                }
            }
            if (StringUtils.isNotBlank(name)){
                Iterator<ProcessStepHandlerVo> iterator = handlerList.iterator();
                while (iterator.hasNext()){
                    ProcessStepHandlerVo handler = iterator.next();
                    if (!handler.getName().contains(name)){
                        iterator.remove();
                    }
                }
            }
        }
        return handlerList;
    }

    @Override
    public void saveStepHandlerConfig(ProcessStepHandlerVo stepHandlerVo) {
        stepHandlerMapper.deleteProcessStepHandlerConfigByHandler(stepHandlerVo.getHandler());
        stepHandlerMapper.insertProcessStepHandlerConfig(stepHandlerVo);
    }
}
