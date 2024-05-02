package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.WorkAssignmentUnitVo;
import neatlogic.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import neatlogic.framework.process.audithandler.core.ProcessTaskAuditDetailTypeFactory;
import neatlogic.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerFactory;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dto.ProcessTaskStepAuditDetailVo;
import neatlogic.framework.process.dto.ProcessTaskStepAuditVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
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
        new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_VIEW)
                .build()
                .checkAndNoPermissionThrowException();

        List<ProcessTaskStepAuditVo> resultList = new ArrayList<>();
        ProcessTaskStepAuditVo processTaskStepAuditVo = new ProcessTaskStepAuditVo();
        processTaskStepAuditVo.setProcessTaskId(processTaskId);
        processTaskStepAuditVo.setProcessTaskStepId(processTaskStepId);
        List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskStepAuditVo);
        Map<Long, Set<ProcessTaskOperationType>> operateMap = new HashMap<>();
        Map<Long, ProcessTaskStepVo> processTaskStepMap = new HashMap<>();
        List<Long> processtaskStepIdList = processTaskStepAuditList.stream().filter(e -> e.getProcessTaskStepId() != null).map(ProcessTaskStepAuditVo::getProcessTaskStepId).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(processtaskStepIdList)){
            Long[] processTaskStepIds = new Long[processtaskStepIdList.size()];
            processtaskStepIdList.toArray(processTaskStepIds);
            operateMap = new ProcessAuthManager.Builder().addProcessTaskStepId(processTaskStepIds).addOperationType(ProcessTaskOperationType.STEP_VIEW).build().getOperateMap();
            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(processtaskStepIdList);
            processTaskStepMap = processTaskStepList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
        }
        for (ProcessTaskStepAuditVo processTaskStepAudit : processTaskStepAuditList) {
            if (processTaskStepAudit.getProcessTaskStepId() != null) {
                // 判断当前用户是否有权限查看该节点信息
                if (!operateMap.computeIfAbsent(processTaskStepAudit.getProcessTaskStepId(), k -> new HashSet<>()).contains(ProcessTaskOperationType.STEP_VIEW)) {
                    continue;
                }
                ProcessTaskStepVo processTaskStepVo = processTaskStepMap.get(processTaskStepAudit.getProcessTaskStepId());
                if (processTaskStepVo != null) {
                    processTaskStepAudit.setFormSceneUuid(processTaskStepVo.getFormSceneUuid());
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
