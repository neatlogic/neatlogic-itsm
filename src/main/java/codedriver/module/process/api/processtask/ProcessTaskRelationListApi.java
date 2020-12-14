package codedriver.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ChannelTypeRelationVo;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ProcessTaskRelationVo;
import codedriver.framework.process.dto.ProcessTaskStatusVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.ProcessOperateManager;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskRelationListApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/relation/list";
    }

    @Override
    public String getName() {
        return "查询关联工单列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
        @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
        @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
        @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")})
    @Output({@Param(name = "processTaskRelationList", explode = ProcessTaskRelationVo[].class, desc = "关联工单列表"),
        @Param(explode = BasePageVo.class)})
    @Description(desc = "查询关联工单列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessTaskRelationVo processTaskRelationVo = JSON.toJavaObject(jsonObj, ProcessTaskRelationVo.class);
        processTaskService.checkProcessTaskParamsIsLegal(processTaskRelationVo.getProcessTaskId());

        new ProcessOperateManager.TaskOperationChecker(processTaskRelationVo.getProcessTaskId(),
            ProcessTaskOperationType.POCESSTASKVIEW).build().checkAndNoPermissionThrowException();
        JSONObject resultObj = new JSONObject();
        resultObj.put("processTaskRelationList", new ArrayList<>());
        int pageCount = 0;
        if (processTaskRelationVo.getNeedPage()) {
            int rowNum =
                processTaskMapper.getProcessTaskRelationCountByProcessTaskId(processTaskRelationVo.getProcessTaskId());
            pageCount = PageUtil.getPageCount(rowNum, processTaskRelationVo.getPageSize());
            resultObj.put("currentPage", processTaskRelationVo.getCurrentPage());
            resultObj.put("pageSize", processTaskRelationVo.getPageSize());
            resultObj.put("pageCount", pageCount);
            resultObj.put("rowNum", rowNum);
        }
        if (!processTaskRelationVo.getNeedPage() || processTaskRelationVo.getCurrentPage() <= pageCount) {
            Set<Long> processTaskIdSet = new HashSet<>();
            List<ProcessTaskRelationVo> processTaskRelationList =
                processTaskMapper.getProcessTaskRelationList(processTaskRelationVo);
            for (ProcessTaskRelationVo processTaskRelation : processTaskRelationList) {
                processTaskIdSet.add(processTaskRelation.getProcessTaskId());
                ChannelTypeRelationVo channelTypeRelationVo =
                    channelMapper.getChannelTypeRelationById(processTaskRelation.getChannelTypeRelationId());
                if (channelTypeRelationVo != null) {
                    processTaskRelation.setChannelTypeRelationName(channelTypeRelationVo.getName());
                }
            }
            Map<Long, ProcessTaskVo> processTaskMap = new HashMap<>();
            List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByKeywordAndIdList(null,
                new ArrayList<>(processTaskIdSet), null, null);
            for (ProcessTaskVo processTask : processTaskList) {
                ChannelVo channelVo = channelMapper.getChannelByUuid(processTask.getChannelUuid());
                if (channelVo != null && StringUtils.isNotBlank(channelVo.getChannelTypeUuid())) {
                    ChannelTypeVo channelTypeVo = channelMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
                    if (channelTypeVo == null) {
                        channelTypeVo = new ChannelTypeVo();
                        channelTypeVo.setUuid(channelVo.getChannelTypeUuid());
                    }
                    processTask.setChannelType(new ChannelTypeVo(channelTypeVo));
                }
                processTaskMap.put(processTask.getId(), processTask);
            }
            for (ProcessTaskRelationVo processTaskRelation : processTaskRelationList) {
                ProcessTaskVo processTask = processTaskMap.get(processTaskRelation.getProcessTaskId());
                if (processTask != null) {
                    processTaskRelation.setTilte(processTask.getTitle());
                    processTaskRelation.setStatusVo(new ProcessTaskStatusVo(processTask.getStatus()));
                    processTaskRelation.setChannelTypeVo(new ChannelTypeVo(processTask.getChannelType()));
                }
            }
            resultObj.put("processTaskRelationList", processTaskRelationList);
        }
        return resultObj;
    }

}
