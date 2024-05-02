package neatlogic.module.process.api.processtask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ChannelTypeRelationVo;
import neatlogic.framework.process.dto.ChannelTypeVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.ProcessTaskRelationVo;
import neatlogic.framework.process.dto.ProcessTaskStatusVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskRelationListApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private ChannelMapper channelMapper;

    @Autowired
    private ChannelTypeMapper channelTypeMapper;

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
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskRelationVo.getProcessTaskId());
        try {
            new ProcessAuthManager.TaskOperationChecker(processTaskRelationVo.getProcessTaskId(), ProcessTaskOperationType.PROCESSTASK_VIEW)
                    .build()
                    .checkAndNoPermissionThrowException();
        } catch (ProcessTaskPermissionDeniedException e) {
            throw new PermissionDeniedException(e.getMessage());
        }
//        if (!new ProcessAuthManager.TaskOperationChecker(processTaskRelationVo.getProcessTaskId(), ProcessTaskOperationType.PROCESSTASK_VIEW).build().check()) {
//            if (ProcessTaskStatus.DRAFT.getValue().equals(processTaskVo.getStatus())) {
//                throw new ProcessTaskViewDeniedException();
//            } else {
//                ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
//                if (channelVo == null) {
//                    throw new ChannelNotFoundException(processTaskVo.getChannelUuid());
//                }
//                throw new ProcessTaskViewDeniedException(channelVo.getName());
//            }
//        }
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
                    channelTypeMapper.getChannelTypeRelationById(processTaskRelation.getChannelTypeRelationId());
                if (channelTypeRelationVo != null) {
                    processTaskRelation.setChannelTypeRelationName(channelTypeRelationVo.getName());
                }
            }
            Map<Long, ProcessTaskVo> processTaskMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(processTaskIdSet)) {
                List<ProcessTaskVo> processTaskList =
                        processTaskMapper.getProcessTaskListByIdList(new ArrayList<>(processTaskIdSet));
                for (ProcessTaskVo processTask : processTaskList) {
                    ChannelVo channelVo = channelMapper.getChannelByUuid(processTask.getChannelUuid());
                    if (channelVo != null && StringUtils.isNotBlank(channelVo.getChannelTypeUuid())) {
                        ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
                        if (channelTypeVo == null) {
                            channelTypeVo = new ChannelTypeVo();
                            channelTypeVo.setUuid(channelVo.getChannelTypeUuid());
                        }
                        processTask.setChannelType(channelTypeVo.clone());
                    }
                    processTaskMap.put(processTask.getId(), processTask);
                }
            }
            for (ProcessTaskRelationVo processTaskRelation : processTaskRelationList) {
                ProcessTaskVo processTask = processTaskMap.get(processTaskRelation.getProcessTaskId());
                if (processTask != null) {
                    processTaskRelation.setSerialNumber(processTask.getSerialNumber());
                    processTaskRelation.setTitle(processTask.getTitle());
                    processTaskRelation.setStatusVo(new ProcessTaskStatusVo(processTask.getStatus()));
                    processTaskRelation.setChannelTypeVo(processTask.getChannelType().clone());
                }
            }
            resultObj.put("processTaskRelationList", processTaskRelationList);
        }
        return resultObj;
    }

}
