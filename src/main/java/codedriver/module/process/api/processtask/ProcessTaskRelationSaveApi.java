package codedriver.module.process.api.processtask;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskRelationVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepUtilHandler;
import codedriver.framework.process.stephandler.core.ProcessStepUtilHandlerFactory;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class ProcessTaskRelationSaveApi extends PrivateApiComponentBase {
    
    @Autowired
    private ProcessTaskMapper processTaskMapper;
    
    @Autowired
    private ChannelMapper channelMapper;
    
    @Autowired
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/relation/save";
    }

    @Override
    public String getName() {
        return "保存工单关联";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
        @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
        @Param(name = "channelTypeRelationId", type = ApiParamType.LONG, isRequired = true, desc = "服务类型关系id"),
        @Param(name = "relationProcessTaskIdList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "被关联的工单id列表")
    })
    @Description(desc = "保存工单关联")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        IProcessStepUtilHandler handler = ProcessStepUtilHandlerFactory.getHandler();
        handler.verifyOperationAuthoriy(processTaskVo, ProcessTaskOperationType.TRANFERREPORT, true);
        Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");
        if(channelMapper.checkChannelTypeRelationIsExists(channelTypeRelationId) == 0) {
            throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
        }
        List<Long> relationProcessTaskIdList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("relationProcessTaskIdList")), Long.class);
        if(CollectionUtils.isNotEmpty(relationProcessTaskIdList)) {
            List<Long> processTaskIdList = processTaskMapper.checkProcessTaskIdListIsExists(relationProcessTaskIdList);
            if(CollectionUtils.isNotEmpty(processTaskIdList)) {               
                for(Long target : processTaskIdList) {
                    ProcessTaskRelationVo processTaskRelationVo = new ProcessTaskRelationVo();
                    processTaskRelationVo.setSource(processTaskId);
                    processTaskRelationVo.setChannelTypeRelationId(channelTypeRelationId);  
                    processTaskRelationVo.setTarget(target);
                    
                    processTaskMapper.replaceProcessTaskRelation(processTaskRelationVo);
                    ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
                    processTaskStepVo.setProcessTaskId(target);
                    processTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.CHANNELTYPERELATION.getParamName(), channelTypeRelationId);
                    processTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.PROCESSTASKLIST.getParamName(), JSON.toJSONString(Arrays.asList(processTaskId)));
                    handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.RELATION);
                }
                jsonObj.put(ProcessTaskAuditDetailType.PROCESSTASKLIST.getParamName(), JSON.toJSONString(processTaskIdList));
                ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
                processTaskStepVo.setProcessTaskId(processTaskId);
                processTaskStepVo.setParamObj(jsonObj);
                handler.activityAudit(processTaskStepVo, ProcessTaskAuditType.RELATION);
            }           
        }
        return null;
    }

}
