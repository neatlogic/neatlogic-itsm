package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONArray;
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
            @Param(name = "processTaskStepIdList", type = ApiParamType.JSONARRAY, desc = "工单步骤id列表")
    })
    @Output({
            @Param(name = "Return", explode = ProcessTaskStepAuditVo[].class, desc = "工单活动列表"),
            @Param(name = "Return[n].auditDetailList", explode = ProcessTaskStepAuditDetailVo[].class, desc = "工单活动详情列表")
    })
    @Description(desc = "工单活动列表接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<ProcessTaskStepAuditVo> resultList = new ArrayList<>();
        Long processTaskId = jsonObj.getLong("processTaskId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        ProcessAuthManager.Builder builder = new ProcessAuthManager.Builder().addProcessTaskId(processTaskId).addOperationType(ProcessTaskOperationType.PROCESSTASK_VIEW);
        List<Long> processTaskStepIdList = new ArrayList<>();
        Map<Long, ProcessTaskStepVo> processTaskStepMap = new HashMap<>();
        JSONArray processTaskStepIdArray = jsonObj.getJSONArray("processTaskStepIdList");
        if (CollectionUtils.isNotEmpty(processTaskStepIdArray)) {
            processTaskStepIdList = processTaskStepIdArray.toJavaList(Long.class);
        }
        List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(processTaskId, processTaskStepIdList);
        if (CollectionUtils.isEmpty(processTaskStepAuditList)) {
            return resultList;
        }
        processTaskStepIdList = processTaskStepAuditList.stream().map(ProcessTaskStepAuditVo::getProcessTaskStepId).collect(Collectors.toList());
        Long[] processTaskStepIds = new Long[processTaskStepIdList.size()];
        processTaskStepIdList.toArray(processTaskStepIds);
        builder.addProcessTaskStepId(processTaskStepIds)
                .addOperationType(ProcessTaskOperationType.STEP_VIEW);
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(processTaskStepIdList);
        processTaskStepMap = processTaskStepList.stream().collect(Collectors.toMap(ProcessTaskStepVo::getId, e -> e));
        Map<Long, Set<ProcessTaskOperationType>> operateMap = builder.build().getOperateMap();
        if (!operateMap.computeIfAbsent(processTaskId, k -> new HashSet<>()).contains(ProcessTaskOperationType.PROCESSTASK_VIEW)) {
            return resultList;
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
