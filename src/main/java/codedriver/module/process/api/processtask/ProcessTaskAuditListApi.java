package codedriver.module.process.api.processtask;

import java.util.*;
import java.util.stream.Collectors;

import codedriver.framework.common.constvalue.SystemUser;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.dto.WorkAssignmentUnitVo;
import codedriver.framework.process.audithandler.core.ProcessTaskAuditDetailTypeFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerFactory;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.framework.process.dto.ProcessTaskStepAuditVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.module.process.service.ProcessTaskService;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskAuditListApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private ProcessTaskService processTaskService;

    @Autowired
    private SelectContentByHashMapper selectContentByHashMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "processtask/audit/list";
    }

    @Override
    public String getName() {
        return "工单活动列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "工单步骤id")
    })
    @Output({
            @Param(name = "Return", explode = ProcessTaskStepAuditVo[].class, desc = "工单活动列表"),
            @Param(name = "Return[n].auditDetailList", explode = ProcessTaskStepAuditDetailVo[].class, desc = "工单活动详情列表")
    })
    @Description(desc = "工单活动列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        try {
            new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.TASK_VIEW).build()
                    .checkAndNoPermissionThrowException();
        } catch (ProcessTaskNoPermissionException e) {
            throw new PermissionDeniedException();
        }

        List<ProcessTaskStepAuditVo> resultList = new ArrayList<>();
        ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
        processTaskStepAuditVo.setProcessTaskId(processTaskId);
        processTaskStepAuditVo.setProcessTaskStepId(processTaskStepId);
        List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
        Map<Long, Set<ProcessTaskOperationType>> operateMap = new HashMap<>();
        List<Long> processtaskStepIdList = processTaskStepAuditList.stream().filter(e -> e.getProcessTaskStepId() != null).map(ProcessTaskStepAuditVo::getProcessTaskStepId).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(processtaskStepIdList)){
            Long[] processTaskStepIds = new Long[processtaskStepIdList.size()];
            processtaskStepIdList.toArray(processTaskStepIds);
            operateMap = new ProcessAuthManager.Builder().addProcessTaskStepId(processTaskStepIds).addOperationType(ProcessTaskOperationType.STEP_VIEW).build().getOperateMap();
        }
        for (ProcessTaskStepAuditVo processTaskStepAudit : processTaskStepAuditList) {
            if (processTaskStepAudit.getProcessTaskStepId() != null) {
                // 判断当前用户是否有权限查看该节点信息
                if (!operateMap.computeIfAbsent(processTaskStepAudit.getProcessTaskStepId(), k -> new HashSet<>()).contains(ProcessTaskOperationType.STEP_VIEW)) {
                    continue;
                }
            }

            if(StringUtils.isNotBlank(processTaskStepAudit.getDescriptionHash())){
                String description = selectContentByHashMapper.getProcessTaskContentStringByHash(processTaskStepAudit.getDescriptionHash());
                processTaskStepAudit.setDescription(description);
            }
            if(SystemUser.SYSTEM.getUserUuid().equals(processTaskStepAudit.getUserUuid())){
                processTaskStepAudit.setUserVo(new WorkAssignmentUnitVo(SystemUser.SYSTEM.getUserVo()));
            }else {
                UserVo userVo = userMapper.getUserBaseInfoByUuid(processTaskStepAudit.getUserUuid());
                if(userVo == null){
                    userVo = new UserVo(processTaskStepAudit.getUserUuid());
                }
                processTaskStepAudit.setUserVo(new WorkAssignmentUnitVo(userVo));
            }

            if(SystemUser.SYSTEM.getUserUuid().equals(processTaskStepAudit.getOriginalUser())){
                processTaskStepAudit.setOriginalUserVo(new WorkAssignmentUnitVo(SystemUser.SYSTEM.getUserVo()));
            }else if(StringUtils.isNotBlank(processTaskStepAudit.getOriginalUser())) {
                UserVo userVo = userMapper.getUserBaseInfoByUuid(processTaskStepAudit.getOriginalUser());
                if(userVo == null){
                    userVo = new UserVo(processTaskStepAudit.getOriginalUser());
                }
                processTaskStepAudit.setOriginalUserVo(new WorkAssignmentUnitVo(userVo));
            }
            List<ProcessTaskStepAuditDetailVo> processTaskStepAuditDetailList = processTaskStepAudit.getAuditDetailList();
            processTaskStepAuditDetailList.sort(ProcessTaskStepAuditDetailVo::compareTo);
            Iterator<ProcessTaskStepAuditDetailVo> iterator = processTaskStepAuditDetailList.iterator();
            while (iterator.hasNext()) {
                ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo = iterator.next();
                if(ProcessTaskAuditDetailTypeFactory.getNeedCompression(processTaskStepAuditDetailVo.getType())){
                    String oldContent = processTaskStepAuditDetailVo.getOldContent();
                    if(StringUtils.isNotBlank(oldContent)) {
                        processTaskStepAuditDetailVo.setOldContent(selectContentByHashMapper.getProcessTaskContentStringByHash(oldContent));
                    }
                    String newContent = processTaskStepAuditDetailVo.getNewContent();
                    if(StringUtils.isNotBlank(newContent)) {
                        processTaskStepAuditDetailVo.setNewContent(selectContentByHashMapper.getProcessTaskContentStringByHash(newContent));
                    }
                }
                IProcessTaskStepAuditDetailHandler auditDetailHandler = ProcessTaskStepAuditDetailHandlerFactory.getHandler(processTaskStepAuditDetailVo.getType());
                if (auditDetailHandler != null) {
                    int isShow = auditDetailHandler.handle(processTaskStepAuditDetailVo);
                    if (isShow == 0) {
                        iterator.remove();
                    }
                }
            }
            resultList.add(processTaskStepAudit);
        }
        if(CollectionUtils.isNotEmpty(resultList)){
            resultList.sort((e1, e2) -> e2.getId().compareTo(e1.getId()));
        }
        return resultList;
    }

}
