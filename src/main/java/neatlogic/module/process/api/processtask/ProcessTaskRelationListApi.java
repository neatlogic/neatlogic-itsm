package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.exception.type.PermissionDeniedException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
        return "nmpap.processtaskrelationlistapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtaskrelationlistapi.input.param.desc.processtaskid"),
        @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "nmpap.processtaskrelationlistapi.input.param.desc.needpage"),
        @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "nmpap.processtaskrelationlistapi.input.param.desc.pagesize"),
        @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "nmpap.processtaskrelationlistapi.input.param.desc.currentpage")})
    @Output({@Param(name = "processTaskRelationList", explode = ProcessTaskRelationVo[].class, desc = "nmpap.processtaskrelationlistapi.output.param.desc.processtaskrelationlist"),
        @Param(explode = BasePageVo.class)})
    @Description(desc = "nmpap.processtaskrelationlistapi.getname")
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
        List<ProcessTaskRelationVo> resultList = new ArrayList<>();
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
            List<ProcessTaskRelationVo> processTaskRelationList = processTaskMapper.getProcessTaskRelationList(processTaskRelationVo);
            Set<Long> processTaskIdSet = processTaskRelationList.stream().filter(Objects::nonNull).map(ProcessTaskRelationVo::getProcessTaskId).collect(Collectors.toSet());
            Map<Long, ProcessTaskVo> processTaskMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(processTaskIdSet)) {
                List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByIdList(new ArrayList<>(processTaskIdSet));
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
                    ChannelTypeRelationVo channelTypeRelationVo = channelTypeMapper.getChannelTypeRelationById(processTaskRelation.getChannelTypeRelationId());
                    if (channelTypeRelationVo != null) {
                        processTaskRelation.setChannelTypeRelationName(channelTypeRelationVo.getName());
                    }
                    resultList.add(processTaskRelation);
                }
            }
        }
        resultObj.put("processTaskRelationList", resultList);
        return resultObj;
    }

}
