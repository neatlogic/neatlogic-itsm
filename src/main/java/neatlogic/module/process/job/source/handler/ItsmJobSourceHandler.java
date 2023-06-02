/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.job.source.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.autoexec.dto.job.AutoexecJobRouteVo;
import neatlogic.framework.autoexec.source.IAutoexecJobSource;
import neatlogic.framework.process.constvalue.AutoExecJobProcessSource;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.module.process.dao.mapper.ProcessMapper;
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
