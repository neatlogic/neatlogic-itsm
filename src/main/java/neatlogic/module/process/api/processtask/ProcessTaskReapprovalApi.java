/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.fulltextindex.core.FullTextIndexHandlerFactory;
import neatlogic.framework.fulltextindex.core.IFullTextIndexHandler;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.constvalue.ProcessTaskStepDataType;
import neatlogic.framework.process.dto.ProcessTaskStepDataVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.process.ProcessStepHandlerNotFoundException;
import neatlogic.framework.process.fulltextindex.ProcessFullTextIndexType;
import neatlogic.framework.process.stephandler.core.IProcessStepHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskStepDataMapper;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author linbq
 * @since 2021/9/17 10:54
 **/
@Service
@OperationType(type = OperationTypeEnum.OPERATE)
@AuthAction(action = PROCESS_BASE.class)
public class ProcessTaskReapprovalApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private ProcessTaskStepDataMapper processTaskStepDataMapper;

    @Override
    public String getToken() {
        return "processtask/reapproval";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskreapprovalapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtaskreapprovalapi.input.param.desc.processtaskid"),
            @Param(name = "processTaskStepId", type = ApiParamType.LONG, isRequired = true, desc = "nmpap.processtaskreapprovalapi.input.param.desc.processtaskstepid"),
            @Param(name = "source", type = ApiParamType.STRING, defaultValue = "pc", desc = "nmpap.processtaskreapprovalapi.input.param.desc.source")
    })
    @Description(desc = "nmpap.processtaskreapprovalapi.getname")
    @Override
    @ResubmitInterval(3)
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        Long processTaskStepId = paramObj.getLong("processTaskStepId");
        ProcessTaskVo processTaskVo = processTaskService.checkProcessTaskParamsIsLegal(processTaskId, processTaskStepId);
        ProcessTaskStepVo processTaskStepVo = processTaskVo.getCurrentProcessTaskStep();
        IProcessStepHandler handler = ProcessStepHandlerFactory.getHandler(processTaskStepVo.getHandler());
        if (handler == null) {
            throw new ProcessStepHandlerNotFoundException(processTaskStepVo.getHandler());
        }

        ProcessTaskStepDataVo processTaskStepDataVo = new ProcessTaskStepDataVo();
        processTaskStepDataVo.setProcessTaskId(processTaskId);
        processTaskStepDataVo.setProcessTaskStepId(processTaskStepId);
        processTaskStepDataVo.setFcu(UserContext.get().getUserUuid(true));
        processTaskStepDataVo.setType(ProcessTaskStepDataType.STEPDRAFTSAVE.getValue());
        ProcessTaskStepDataVo stepDraftSaveData = processTaskStepDataMapper.getProcessTaskStepData(processTaskStepDataVo);
        if (stepDraftSaveData != null) {
            JSONObject dataObj = stepDraftSaveData.getData();
            if (MapUtils.isNotEmpty(dataObj)) {
                JSONArray formAttributeDataList = dataObj.getJSONArray("formAttributeDataList");
                if (CollectionUtils.isNotEmpty(formAttributeDataList)) {
                    paramObj.put("formAttributeDataList", formAttributeDataList);
                }
                JSONArray hidecomponentList = dataObj.getJSONArray("hidecomponentList");
                if (CollectionUtils.isNotEmpty(hidecomponentList)) {
                    paramObj.put("hidecomponentList", hidecomponentList);
                }
                JSONArray readcomponentList = dataObj.getJSONArray("readcomponentList");
                if (CollectionUtils.isNotEmpty(readcomponentList)) {
                    paramObj.put("readcomponentList", readcomponentList);
                }
                JSONObject handlerStepInfo = dataObj.getJSONObject("handlerStepInfo");
                if (MapUtils.isNotEmpty(handlerStepInfo)) {
                    paramObj.put("handlerStepInfo", handlerStepInfo);
                }
                String priorityUuid = dataObj.getString("priorityUuid");
                if (StringUtils.isNotBlank(priorityUuid)) {
                    paramObj.put("priorityUuid", priorityUuid);
                }
                JSONArray fileIdList = dataObj.getJSONArray("fileIdList");
                if (CollectionUtils.isNotEmpty(fileIdList)) {
                    paramObj.put("fileIdList", fileIdList);
                }
                if (!paramObj.containsKey("content")) {
                    String content = dataObj.getString("content");
                    if (StringUtils.isNotBlank(content)) {
                        paramObj.put("content", content);
                    }
                }
            }
        }
        processTaskStepVo.getParamObj().putAll(paramObj);
        handler.reapproval(processTaskStepVo);
        processTaskStepDataMapper.deleteProcessTaskStepData(processTaskStepDataVo);

        //创建全文检索索引
        IFullTextIndexHandler indexFormHandler = FullTextIndexHandlerFactory.getHandler(ProcessFullTextIndexType.PROCESSTASK);
        if (indexFormHandler != null) {
            indexFormHandler.createIndex(processTaskStepVo.getProcessTaskId());
        }
        return null;
    }
}
