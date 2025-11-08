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

package neatlogic.module.process.job.source.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.autoexec.dto.job.AutoexecJobRouteVo;
import neatlogic.framework.autoexec.source.IAutoexecJobSource;
import neatlogic.framework.process.constvalue.AutoExecJobProcessSource;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ItsmJobSourceHandler implements IAutoexecJobSource {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getValue() {
        return AutoExecJobProcessSource.ITSM.getValue();
    }

    @Override
    public String getText() {
        return AutoExecJobProcessSource.ITSM.getText();
    }

    @Override
    public List<AutoexecJobRouteVo> getListByUniqueKeyList(List<String> uniqueKeyList) {
        if (CollectionUtils.isEmpty(uniqueKeyList)) {
            return null;
        }
        List<Long> idList = new ArrayList<>();
        for (String str : uniqueKeyList) {
            // 对于旧数据流程步骤uuid忽略，不做跳转
            if (str.length() == 32) {
                continue;
            }
            idList.add(Long.valueOf(str));
        }
        if (CollectionUtils.isEmpty(idList)) {
            return null;
        }
        List<AutoexecJobRouteVo> resultList = new ArrayList<>();
//        List<ProcessStepVo> processStepList = processMapper.getProcessStepListByUuidList(uniqueKeyList);
//        if (CollectionUtils.isEmpty(processStepList)) {
//            return null;
//        }
//        Set<String> processUuidSet = processStepList.stream().map(ProcessStepVo::getProcessUuid).collect(Collectors.toSet());
//        if (CollectionUtils.isEmpty(processUuidSet)) {
//            return null;
//        }
//        List<ProcessVo> processList = processMapper.getProcessListByUuidList(new ArrayList<>(processUuidSet));
//        Map<String, String> processNameMap = processList.stream().collect(Collectors.toMap(e -> e.getUuid(), e -> e.getName()));
//        for (ProcessStepVo processStepVo : processStepList) {
//            JSONObject config = new JSONObject();
//            config.put("stepUuid", processStepVo.getUuid());
//            config.put("uuid", processStepVo.getProcessUuid());
//            String label = "";
//            String processName = processNameMap.get(processStepVo.getProcessUuid());
//            if (StringUtils.isNotBlank(processName)) {
//                label = processName + "/";
//            }
//            label += processStepVo.getName();
//            resultList.add(new AutoexecJobRouteVo(processStepVo.getUuid(), label, config));
//        }

        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByIdList(idList);
        if (CollectionUtils.isEmpty(processTaskStepList)) {
            return resultList;
        }
        Map<Long, String> idToTitleMap = new HashMap<>();
        Set<Long> processTaskIdSet = processTaskStepList.stream().map(ProcessTaskStepVo::getProcessTaskId).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(processTaskIdSet)) {
            List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByIdList(new ArrayList<>(processTaskIdSet));
            idToTitleMap = processTaskList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e.getTitle()));
        }
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            JSONObject config = new JSONObject();
            config.put("processTaskId", processTaskStepVo.getProcessTaskId());
            config.put("processTaskStepId", processTaskStepVo.getId());
            String label = "";
            String title = idToTitleMap.get(processTaskStepVo.getProcessTaskId());
            if (StringUtils.isNotBlank(title)) {
                label = title + "/";
            }
            label += processTaskStepVo.getName();
            resultList.add(new AutoexecJobRouteVo(processTaskStepVo.getId(), label, config));
        }
        return resultList;
    }
}
