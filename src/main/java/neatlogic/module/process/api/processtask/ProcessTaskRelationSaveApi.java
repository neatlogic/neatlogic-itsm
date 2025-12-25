package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.crossover.IProcessTaskRelationSaveApiCrossoverService;
import neatlogic.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskRelationSaveApi extends PrivateApiComponentBase implements IProcessTaskRelationSaveApiCrossoverService {

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private ChannelTypeMapper channelTypeMapper;

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
            @Param(name = "relationProcessTaskIdList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "被关联的工单id列表"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源"),
    })
    @Description(desc = "保存工单关联")
    @ResubmitInterval(3)
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_TRANSFERREPORT)
                .build()
                .checkAndNoPermissionThrowException();
        Long channelTypeRelationId = jsonObj.getLong("channelTypeRelationId");
        if (channelTypeMapper.checkChannelTypeRelationIsExists(channelTypeRelationId) == 0) {
            throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
        }
        List<Long> relationProcessTaskIdList =
            JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("relationProcessTaskIdList")), Long.class);
        if (CollectionUtils.isNotEmpty(relationProcessTaskIdList)) {
            List<Long> processTaskIdList = processTaskMapper.checkProcessTaskIdListIsExists(relationProcessTaskIdList);
            if (CollectionUtils.isNotEmpty(processTaskIdList)) {
                String source = jsonObj.getString("source");
                processTaskService.saveProcessTaskRelation(processTaskId, channelTypeRelationId, processTaskIdList, source);
            }
        }
        return null;
    }

}
