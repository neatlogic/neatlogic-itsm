/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.exception.type.PermissionDeniedException;
import codedriver.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import codedriver.framework.fulltextindex.core.IFullTextIndexHandler;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.exception.priority.PriorityNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNoPermissionException;
import codedriver.framework.process.fulltextindex.ProcessFullTextIndexType;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskUpdateApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private PriorityMapper priorityMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private IProcessStepHandlerUtil IProcessStepHandlerUtil;

    @Override
    public String getToken() {
        return "processtask/update";
    }

    @Override
    public String getName() {
        return "更新工单信息";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, desc = "步骤id"),
            @Param(name = "title", type = ApiParamType.STRING, xss = true, maxLength = 80, desc = "标题"),
            @Param(name = "priorityUuid", type = ApiParamType.STRING, desc = "优先级uuid"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "描述"),
            @Param(name = "tagList", type = ApiParamType.JSONARRAY, desc = "标签列表"),
            @Param(name = "fileIdList", type = ApiParamType.JSONARRAY, desc = "附件id列表")})
    @Description(desc = "更新工单信息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);

        try {
            new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_UPDATE).build()
                    .checkAndNoPermissionThrowException();
        } catch (ProcessTaskNoPermissionException e) {
            throw new PermissionDeniedException();
        }
        // 锁定当前流程
        processTaskMapper.getProcessTaskLockById(processTaskId);

        boolean isUpdate = false;
        String oldTitle = processTaskVo.getTitle();
        String title = jsonObj.getString("title");
        if (StringUtils.isNotBlank(title) && !title.equals(oldTitle)) {
            isUpdate = true;
            processTaskVo.setTitle(title);
            jsonObj.put(ProcessTaskAuditDetailType.TITLE.getOldDataParamName(), oldTitle);
        } else {
            jsonObj.remove("title");
        }

        String priorityUuid = jsonObj.getString("priorityUuid");
        String oldPriorityUuid = processTaskVo.getPriorityUuid();
        if (StringUtils.isNotBlank(priorityUuid) && !priorityUuid.equals(oldPriorityUuid)) {
            if (priorityMapper.checkPriorityIsExists(priorityUuid) == 0) {
                throw new PriorityNotFoundException(priorityUuid);
            }
            isUpdate = true;
            processTaskVo.setPriorityUuid(priorityUuid);
            jsonObj.put(ProcessTaskAuditDetailType.PRIORITY.getOldDataParamName(), oldPriorityUuid);
        } else {
            jsonObj.remove("priorityUuid");
        }
        if (isUpdate) {
            processTaskMapper.updateProcessTaskTitleOwnerPriorityUuid(processTaskVo);
        }

        // 跟新标签
        List<String> tagNameList =
                JSONObject.parseArray(JSON.toJSONString(jsonObj.getJSONArray("tagList")), String.class);
        if (tagNameList != null) {
            List<ProcessTagVo> oldTagList = processTaskMapper.getProcessTaskTagListByProcessTaskId(processTaskId);
            processTaskMapper.deleteProcessTaskTagByProcessTaskId(processTaskId);
            if (CollectionUtils.isNotEmpty(tagNameList)) {
                jsonObj.put(ProcessTaskAuditDetailType.TAGLIST.getParamName(), String.join(",", tagNameList));
                List<ProcessTagVo> existTagList = processMapper.getProcessTagByNameList(tagNameList);
                List<String> existTagNameList =
                        existTagList.stream().map(ProcessTagVo::getName).collect(Collectors.toList());
                List<String> notExistTagList = ListUtils.removeAll(tagNameList, existTagNameList);
                for (String tagName : notExistTagList) {
                    ProcessTagVo tagVo = new ProcessTagVo(tagName);
                    processMapper.insertProcessTag(tagVo);
                    existTagList.add(tagVo);
                }
                List<ProcessTaskTagVo> processTaskTagVoList = new ArrayList<>();
                for (ProcessTagVo processTagVo : existTagList) {
                    processTaskTagVoList.add(new ProcessTaskTagVo(processTaskId, processTagVo.getId()));
                }
                processTaskMapper.insertProcessTaskTag(processTaskTagVoList);
            } else {
                jsonObj.remove(ProcessTaskAuditDetailType.TAGLIST.getParamName());
            }
            int diffCount = tagNameList.stream()
                    .filter(a -> !oldTagList.stream().map(b -> b.getName()).collect(Collectors.toList()).contains(a))
                    .collect(Collectors.toList()).size();
            if (tagNameList.size() != oldTagList.size() || diffCount > 0) {
                List<String> oldTagNameList = new ArrayList<>();
                for (ProcessTagVo tag : oldTagList) {
                    oldTagNameList.add(tag.getName());
                }
                if (CollectionUtils.isNotEmpty(oldTagNameList)) {
                    jsonObj.put(ProcessTaskAuditDetailType.TAGLIST.getOldDataParamName(), String.join(",", oldTagNameList));
                }
                isUpdate = true;
            } else {
                jsonObj.remove(ProcessTaskAuditDetailType.TAGLIST.getParamName());
            }
        }

        // 获取开始步骤id
        ProcessTaskStepVo startProcessTaskStepVo = processTaskMapper.getStartProcessTaskStepByProcessTaskId(processTaskId);
        Long startProcessTaskStepId = startProcessTaskStepVo.getId();
        String content = jsonObj.getString("content");
        List<Long> fileIdList = JSON.parseArray(JSON.toJSONString(jsonObj.getJSONArray("fileIdList")), Long.class);
        if (content != null || fileIdList != null) {
            ProcessTaskStepReplyVo oldReplyVo = null;
            List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(startProcessTaskStepId);
            for (ProcessTaskStepContentVo processTaskStepContent : processTaskStepContentList) {
                if (ProcessTaskOperationType.PROCESSTASK_START.getValue().equals(processTaskStepContent.getType())) {
                    oldReplyVo = new ProcessTaskStepReplyVo(processTaskStepContent);
                    break;
                }
            }
            if (oldReplyVo == null) {
                if (StringUtils.isNotBlank(content) || CollectionUtils.isNotEmpty(fileIdList)) {
                    oldReplyVo = new ProcessTaskStepReplyVo();
                    oldReplyVo.setProcessTaskId(processTaskId);
                    oldReplyVo.setProcessTaskStepId(startProcessTaskStepId);
                    isUpdate = processTaskService.saveProcessTaskStepReply(jsonObj, oldReplyVo);
                }
            } else {
                isUpdate = processTaskService.saveProcessTaskStepReply(jsonObj, oldReplyVo);
            }
        }

        // 生成活动
        if (isUpdate) {
            ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
            processTaskStepVo.setProcessTaskId(processTaskId);
            processTaskStepVo.setId(processTaskStepId);
            processTaskStepVo.setParamObj(jsonObj);
            IProcessStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.UPDATE);
            IProcessStepHandlerUtil.calculateSla(new ProcessTaskVo(processTaskId), false);
        }

        //创建全文检索索引
        IFullTextIndexHandler indexHandler = FullTextIndexHandlerFactory.getHandler(ProcessFullTextIndexType.PROCESSTASK);
        if (indexHandler != null) {
            indexHandler.createIndex(startProcessTaskStepVo.getProcessTaskId());
        }
        return null;
    }

}
