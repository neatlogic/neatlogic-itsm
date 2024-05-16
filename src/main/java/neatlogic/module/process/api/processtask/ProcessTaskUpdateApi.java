/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.core.IFullTextIndexHandler;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskAuditDetailType;
import neatlogic.framework.process.constvalue.ProcessTaskAuditType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.module.process.dao.mapper.catalog.PriorityMapper;
import neatlogic.module.process.dao.mapper.process.ProcessTagMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.*;
import neatlogic.framework.process.exception.priority.PriorityNotFoundException;
import neatlogic.framework.process.fulltextindex.ProcessFullTextIndexType;
import neatlogic.framework.process.operationauth.core.ProcessAuthManager;
import neatlogic.module.process.service.IProcessStepHandlerUtil;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
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
    private ProcessTagMapper processTagMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

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
            @Param(name = "title", type = ApiParamType.STRING, maxLength = 80, desc = "标题"),
            @Param(name = "priorityUuid", type = ApiParamType.STRING, desc = "优先级uuid"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "描述"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "来源"),
            @Param(name = "tagList", type = ApiParamType.JSONARRAY, desc = "标签列表"),
            @Param(name = "fileIdList", type = ApiParamType.JSONARRAY, desc = "附件id列表")})
    @Description(desc = "更新工单信息")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        Long processTaskStepId = jsonObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);

        new ProcessAuthManager.TaskOperationChecker(processTaskId, ProcessTaskOperationType.PROCESSTASK_UPDATE)
                .build()
                .checkAndNoPermissionThrowException();
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
                List<ProcessTagVo> existTagList = processTagMapper.getProcessTagByNameList(tagNameList);
                List<String> existTagNameList =
                        existTagList.stream().map(ProcessTagVo::getName).collect(Collectors.toList());
                List<String> notExistTagList = ListUtils.removeAll(tagNameList, existTagNameList);
                for (String tagName : notExistTagList) {
                    ProcessTagVo tagVo = new ProcessTagVo(tagName);
                    processTagMapper.insertProcessTag(tagVo);
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
            ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
            if (processTaskStepVo == null) {
                processTaskStepVo = new ProcessTaskStepVo();
                processTaskStepVo.setProcessTaskId(processTaskId);
            }
            processTaskStepVo.getParamObj().putAll(jsonObj);
            processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.UPDATE);
            processStepHandlerUtil.calculateSla(new ProcessTaskVo(processTaskId), false);
        }

        //创建全文检索索引
        IFullTextIndexHandler indexHandler = FullTextIndexHandlerFactory.getHandler(ProcessFullTextIndexType.PROCESSTASK);
        if (indexHandler != null) {
            indexHandler.createIndex(startProcessTaskStepVo.getProcessTaskId());
        }
        return null;
    }

}
