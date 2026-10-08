package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.systemuser.SystemUser;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.dto.WorkAssignmentUnitVo;
import neatlogic.framework.process.audithandler.core.IProcessTaskStepAuditDetailHandler;
import neatlogic.framework.process.audithandler.core.ProcessTaskAuditDetailTypeFactory;
import neatlogic.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerFactory;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskStepOperationType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskAuditListApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getToken() {
        return "processtask/audit/list";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskauditlistapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public boolean disableReturnCircularReferenceDetect() {
        return true;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtaskauditlistapi.input.param.desc.processtaskid"),
            @Param(name = "processTaskStepIdList", type = ApiParamType.JSONARRAY, desc = "nmpap.processtaskauditlistapi.input.param.desc.processtaskstepidlist"),
            @Param(name = "sortDirection", type = ApiParamType.ENUM, rule="asc,desc", desc = "排序方向"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "common.currentpage"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "common.pagesize"),
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = ProcessTaskStepAuditVo[].class, desc = "common.tbodylist"),
    })
    @Description(desc = "nmpap.processtaskauditlistapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessTaskStepAuditSearchVo searchVo = jsonObj.toJavaObject(ProcessTaskStepAuditSearchVo.class);
//        List<ProcessTaskStepAuditVo> resultList = new ArrayList<>();
        Long processTaskId = searchVo.getProcessTaskId();
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        List<Long> processTaskStepIdList = searchVo.getProcessTaskStepIdList();
//        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
        if (CollectionUtils.isEmpty(processTaskStepIdList)) {
//            List<Long> processTaskStepIdList = processTaskStepList.stream().map(ProcessTaskStepVo::getId).collect(Collectors.toList());
            processTaskStepIdList = processTaskMapper.getHasAuditProcessTaskStepIdListByProcessTaskId(processTaskId);
//            searchVo.setProcessTaskStepIdList(processTaskStepIdList);
        }
        ProcessAuthManager.Builder builder = new ProcessAuthManager.Builder().addProcessTaskId(processTaskId).addOperationType(ProcessTaskOperationType.PROCESSTASK_VIEW);
//        List<Long> processTaskStepIdList = new ArrayList<>();
        Map<Long, ProcessTaskStepVo> processTaskStepMap = new HashMap<>();
//        JSONArray processTaskStepIdArray = jsonObj.getJSONArray("processTaskStepIdList");
//        if (CollectionUtils.isNotEmpty(processTaskStepIdArray)) {
//            processTaskStepIdList = processTaskStepIdArray.toJavaList(Long.class);
//        }
//        List<Long> processTaskStepIdList = processTaskStepAuditList.stream().map(ProcessTaskStepAuditVo::getProcessTaskStepId).collect(Collectors.toList());
        List<ProcessTaskStepVo> processTaskStepList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(processTaskStepIdList)) {
            Long[] processTaskStepIds = new Long[processTaskStepIdList.size()];
            processTaskStepIdList.toArray(processTaskStepIds);
            builder.addProcessTaskStepId(processTaskStepIds).addOperationType(ProcessTaskStepOperationType.STEP_VIEW);
            processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(processTaskStepIdList);
            processTaskStepMap = processTaskStepList.stream().collect(Collectors.toMap(ProcessTaskStepVo::getId, e -> e));
        }
        Map<Long, Set<IOperationType>> operateMap = builder.build().getOperateMap();
        if (!operateMap.computeIfAbsent(processTaskId, k -> new HashSet<>()).contains(ProcessTaskOperationType.PROCESSTASK_VIEW)) {
            return TableResultUtil.getResult(new ArrayList(), searchVo);
        }
        if (CollectionUtils.isNotEmpty(processTaskStepIdList)) {
            List<Long> noViewProcessTaskStepIdList = new ArrayList<>();
            for (Long processTaskStepId : processTaskStepIdList) {
                // 判断当前用户是否有权限查看该节点信息
                if (!operateMap.computeIfAbsent(processTaskStepId, k -> new HashSet<>()).contains(ProcessTaskStepOperationType.STEP_VIEW)) {
                    noViewProcessTaskStepIdList.add(processTaskStepId);
                }
            }
            searchVo.setNoViewProcessTaskStepIdList(noViewProcessTaskStepIdList);
            if (CollectionUtils.isNotEmpty(searchVo.getProcessTaskStepIdList())) {
                searchVo.getProcessTaskStepIdList().removeAll(noViewProcessTaskStepIdList);
            }
        }
        List<ProcessTaskStepAuditVo> processTaskStepAuditList = processTaskMapper.getProcessTaskStepAuditList(searchVo);
        if (CollectionUtils.isEmpty(processTaskStepAuditList)) {
            return TableResultUtil.getResult(processTaskStepAuditList, searchVo);
        }
        int rowNum = processTaskMapper.getProcessTaskStepAuditCount(searchVo);
        searchVo.setRowNum(rowNum);
        List<Long> auditIdList = processTaskStepAuditList.stream().map(ProcessTaskStepAuditVo::getId).collect(Collectors.toList());
        List<ProcessTaskStepAuditDetailVo> allProcessTaskStepAuditDetailList = processTaskMapper.getProcessTaskStepAuditDetailListByAuditIdList(auditIdList);
        Map<String, String> hash2ConfigMap = new HashMap<>();
        List<String> configHashList = processTaskStepList.stream().map(ProcessTaskStepVo::getConfigHash).filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(configHashList)) {
            List<ProcessTaskStepConfigVo> configList = selectContentByHashMapper.getProcessTaskStepConfigListByHashList(configHashList);
            for (ProcessTaskStepConfigVo processTaskStepConfigVo : configList) {
                hash2ConfigMap.put(processTaskStepConfigVo.getHash(), processTaskStepConfigVo.getConfig());
            }
        }
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            String stepConfig = hash2ConfigMap.get(processTaskStepVo.getConfigHash());
            if (StringUtils.isNotBlank(stepConfig)) {
                JSONObject configObj = JSONObject.parseObject(stepConfig);
                String formSceneUuid = configObj.getString("formSceneUuid");
                processTaskStepVo.setFormSceneUuid(formSceneUuid);
            }
        }
        Set<String> contentHashSet = new HashSet<>();
        Set<String> userUuidSet = new HashSet<>();
        for (ProcessTaskStepAuditVo processTaskStepAudit : processTaskStepAuditList) {
            if (StringUtils.isNotBlank(processTaskStepAudit.getDescriptionHash())) {
                contentHashSet.add(processTaskStepAudit.getDescriptionHash());
            }
            if (!Objects.equals(processTaskStepAudit.getUserUuid(), SystemUser.SYSTEM.getUserUuid())) {
                userUuidSet.add(processTaskStepAudit.getUserUuid());
            }
            if (!Objects.equals(processTaskStepAudit.getOriginalUser(), SystemUser.SYSTEM.getUserUuid())) {
                userUuidSet.add(processTaskStepAudit.getOriginalUser());
            }
            List<ProcessTaskStepAuditDetailVo> processTaskStepAuditDetailList = new ArrayList<>();
            for (ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo : allProcessTaskStepAuditDetailList) {
                if (!Objects.equals(processTaskStepAuditDetailVo.getAuditId(), processTaskStepAudit.getId())) {
                    continue;
                }
                processTaskStepAuditDetailList.add(processTaskStepAuditDetailVo);
                if (ProcessTaskAuditDetailTypeFactory.getNeedCompression(processTaskStepAuditDetailVo.getType())) {
                    String oldContent = processTaskStepAuditDetailVo.getOldContent();
                    if (StringUtils.isNotBlank(oldContent)) {
                        contentHashSet.add(oldContent);
                    }
                    String newContent = processTaskStepAuditDetailVo.getNewContent();
                    if (StringUtils.isNotBlank(newContent)) {
                        contentHashSet.add(newContent);
                    }
                }
            }
            processTaskStepAudit.setAuditDetailList(processTaskStepAuditDetailList);
        }
        Map<String, UserVo> userMap = new HashMap<>();
        Map<String, String> hashToContentMap = new HashMap<>();
        List<String> userUuidList = userUuidSet.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(userUuidList)) {
            List<UserVo> userList = userMapper.getUserByUserUuidList(userUuidList);
            userMap = userList.stream().collect(Collectors.toMap(UserVo::getUuid, e -> e));
        }
        List<String> contentHashList = contentHashSet.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(contentHashList)) {
            System.out.println("contentHashList.size() = " + contentHashList.size());
            List<ProcessTaskContentVo> processTaskContentList = selectContentByHashMapper.getProcessTaskContentListByHashList(contentHashList);
            hashToContentMap = processTaskContentList.stream().collect(Collectors.toMap(ProcessTaskContentVo::getHash, ProcessTaskContentVo::getContent));
        }

        for (ProcessTaskStepAuditVo processTaskStepAudit : processTaskStepAuditList) {
            if (processTaskStepAudit.getProcessTaskStepId() != null) {
                // 判断当前用户是否有权限查看该节点信息
//                if (!operateMap.computeIfAbsent(processTaskStepAudit.getProcessTaskStepId(), k -> new HashSet<>()).contains(ProcessTaskStepOperationType.STEP_VIEW)) {
//                    continue;
//                }
                ProcessTaskStepVo processTaskStepVo = processTaskStepMap.get(processTaskStepAudit.getProcessTaskStepId());
                if (processTaskStepVo != null) {
                    processTaskStepAudit.setFormSceneUuid(processTaskStepVo.getFormSceneUuid());
                }
            }
            if (StringUtils.isNotBlank(processTaskStepAudit.getDescriptionHash())) {
                String description = hashToContentMap.get(processTaskStepAudit.getDescriptionHash());
                processTaskStepAudit.setDescription(description);
            }
            if (SystemUser.SYSTEM.getUserUuid().equals(processTaskStepAudit.getUserUuid())) {
                processTaskStepAudit.setUserVo(new WorkAssignmentUnitVo(SystemUser.SYSTEM.getUserVo()));
            } else {
                UserVo userVo = userMap.get(processTaskStepAudit.getUserUuid());
                if (userVo == null) {
                    userVo = new UserVo(processTaskStepAudit.getUserUuid());
                }
                processTaskStepAudit.setUserVo(new WorkAssignmentUnitVo(userVo));
            }
            if (SystemUser.SYSTEM.getUserUuid().equals(processTaskStepAudit.getOriginalUser())) {
                processTaskStepAudit.setOriginalUserVo(new WorkAssignmentUnitVo(SystemUser.SYSTEM.getUserVo()));
            } else if (StringUtils.isNotBlank(processTaskStepAudit.getOriginalUser())) {
                UserVo userVo = userMap.get(processTaskStepAudit.getOriginalUser());
                if (userVo == null) {
                    userVo = new UserVo(processTaskStepAudit.getOriginalUser());
                }
                processTaskStepAudit.setOriginalUserVo(new WorkAssignmentUnitVo(userVo));
            }
            List<ProcessTaskStepAuditDetailVo> processTaskStepAuditDetailList = processTaskStepAudit.getAuditDetailList();
            processTaskStepAuditDetailList.sort(ProcessTaskStepAuditDetailVo::compareTo);
            Iterator<ProcessTaskStepAuditDetailVo> iterator = processTaskStepAuditDetailList.iterator();
            while (iterator.hasNext()) {
                ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo = iterator.next();
                if (ProcessTaskAuditDetailTypeFactory.getNeedCompression(processTaskStepAuditDetailVo.getType())) {
                    String oldContent = processTaskStepAuditDetailVo.getOldContent();
                    if (StringUtils.isNotBlank(oldContent)) {
                        processTaskStepAuditDetailVo.setOldContent(hashToContentMap.get(oldContent));
                    }
                    String newContent = processTaskStepAuditDetailVo.getNewContent();
                    if (StringUtils.isNotBlank(newContent)) {
                        processTaskStepAuditDetailVo.setNewContent(hashToContentMap.get(newContent));
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
//            resultList.add(processTaskStepAudit);
        }
//        if(CollectionUtils.isNotEmpty(resultList)){
//            resultList.sort((e1, e2) -> e2.getId().compareTo(e1.getId()));
//        }
        return TableResultUtil.getResult(processTaskStepAuditList, searchVo);

//        for (ProcessTaskStepAuditVo processTaskStepAudit : processTaskStepAuditList) {
//            if (processTaskStepAudit.getProcessTaskStepId() != null) {
//                // 判断当前用户是否有权限查看该节点信息
//                if (!operateMap.computeIfAbsent(processTaskStepAudit.getProcessTaskStepId(), k -> new HashSet<>()).contains(ProcessTaskStepOperationType.STEP_VIEW)) {
//                    continue;
//                }
//                ProcessTaskStepVo processTaskStepVo = processTaskStepMap.get(processTaskStepAudit.getProcessTaskStepId());
//                if (processTaskStepVo != null) {
//                    processTaskStepAudit.setFormSceneUuid(processTaskStepVo.getFormSceneUuid());
//                }
//            }
//
//            if(StringUtils.isNotBlank(processTaskStepAudit.getDescriptionHash())){
//                String description = selectContentByHashMapper.getProcessTaskContentStringByHash(processTaskStepAudit.getDescriptionHash());
//                processTaskStepAudit.setDescription(description);
//            }
//            if(SystemUser.SYSTEM.getUserUuid().equals(processTaskStepAudit.getUserUuid())){
//                processTaskStepAudit.setUserVo(new WorkAssignmentUnitVo(SystemUser.SYSTEM.getUserVo()));
//            }else {
//                UserVo userVo = userMapper.getUserBaseInfoByUuid(processTaskStepAudit.getUserUuid());
//                if(userVo == null){
//                    userVo = new UserVo(processTaskStepAudit.getUserUuid());
//                }
//                processTaskStepAudit.setUserVo(new WorkAssignmentUnitVo(userVo));
//            }
//
//            if(SystemUser.SYSTEM.getUserUuid().equals(processTaskStepAudit.getOriginalUser())){
//                processTaskStepAudit.setOriginalUserVo(new WorkAssignmentUnitVo(SystemUser.SYSTEM.getUserVo()));
//            }else if(StringUtils.isNotBlank(processTaskStepAudit.getOriginalUser())) {
//                UserVo userVo = userMapper.getUserBaseInfoByUuid(processTaskStepAudit.getOriginalUser());
//                if(userVo == null){
//                    userVo = new UserVo(processTaskStepAudit.getOriginalUser());
//                }
//                processTaskStepAudit.setOriginalUserVo(new WorkAssignmentUnitVo(userVo));
//            }
//            List<ProcessTaskStepAuditDetailVo> processTaskStepAuditDetailList = processTaskStepAudit.getAuditDetailList();
//            processTaskStepAuditDetailList.sort(ProcessTaskStepAuditDetailVo::compareTo);
//            Iterator<ProcessTaskStepAuditDetailVo> iterator = processTaskStepAuditDetailList.iterator();
//            while (iterator.hasNext()) {
//                ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo = iterator.next();
//                if(ProcessTaskAuditDetailTypeFactory.getNeedCompression(processTaskStepAuditDetailVo.getType())){
//                    String oldContent = processTaskStepAuditDetailVo.getOldContent();
//                    if(StringUtils.isNotBlank(oldContent)) {
//                        processTaskStepAuditDetailVo.setOldContent(selectContentByHashMapper.getProcessTaskContentStringByHash(oldContent));
//                    }
//                    String newContent = processTaskStepAuditDetailVo.getNewContent();
//                    if(StringUtils.isNotBlank(newContent)) {
//                        processTaskStepAuditDetailVo.setNewContent(selectContentByHashMapper.getProcessTaskContentStringByHash(newContent));
//                    }
//                }
//                IProcessTaskStepAuditDetailHandler auditDetailHandler = ProcessTaskStepAuditDetailHandlerFactory.getHandler(processTaskStepAuditDetailVo.getType());
//                if (auditDetailHandler != null) {
//                    int isShow = auditDetailHandler.handle(processTaskStepAuditDetailVo);
//                    if (isShow == 0) {
//                        iterator.remove();
//                    }
//                }
//            }
//            resultList.add(processTaskStepAudit);
//        }
//        if(CollectionUtils.isNotEmpty(resultList)){
//            resultList.sort((e1, e2) -> e2.getId().compareTo(e1.getId()));
//        }
//        return resultList;
    }

}
