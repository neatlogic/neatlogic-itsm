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

package neatlogic.module.process.stephandler.regulatehandler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.dto.condition.ConditionGroupRelVo;
import neatlogic.framework.dto.condition.ConditionGroupVo;
import neatlogic.framework.dto.condition.ConditionRelVo;
import neatlogic.framework.dto.condition.ConditionVo;
import neatlogic.framework.process.dto.processconfig.MoveonConfigVo;
import neatlogic.framework.process.exception.process.ProcessConfigException;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IRegulateHandler;
import neatlogic.framework.process.stephandler.core.ProcessMessageManager;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class MoveonConfigListRegulateHandler implements IRegulateHandler {

    @Override
    public String getName() {
        return "moveonConfigList";
    }

    @Override
    public void regulateConfig(IProcessStepInternalHandler processStepInternalHandler, JSONObject oldConfigObj, JSONObject newConfigObj) {
        List<MoveonConfigVo> moveonConfigList = new ArrayList<>();
        JSONArray moveonConfigArray = oldConfigObj.getJSONArray("moveonConfigList");
        if(CollectionUtils.isNotEmpty(moveonConfigArray)){
            moveonConfigArray.removeIf(Objects::isNull);
            List<String> effectiveStepUuidList = ProcessMessageManager.getEffectiveStepUuidList();
            for(int i = 0; i < moveonConfigArray.size(); i++){
                MoveonConfigVo moveonConfigVo = moveonConfigArray.getObject(i, MoveonConfigVo.class);
                if(moveonConfigVo != null){
                    validateConditionGroupRelList(moveonConfigVo);
                    List<String> targetStepList = moveonConfigVo.getTargetStepList();
                    if (CollectionUtils.isNotEmpty(targetStepList)) {
                        List<String> list = ListUtils.removeAll(targetStepList, effectiveStepUuidList);
                        if (CollectionUtils.isNotEmpty(list) && ProcessMessageManager.getOperationType() == OperationTypeEnum.UPDATE) {
                            throw new ProcessConfigException(ProcessConfigException.Type.CONDITION, ProcessMessageManager.getStepName());
                        }
                    }
                    moveonConfigList.add(moveonConfigVo);
                }
            }
        }
        newConfigObj.put("moveonConfigList", moveonConfigList);
    }

    private void validateConditionGroupRelList(MoveonConfigVo moveonConfigVo) {
        List<ConditionGroupVo> conditionGroupList = moveonConfigVo.getConditionGroupList();
        Set<String> conditionGroupUuidSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(conditionGroupList)) {
            for (ConditionGroupVo conditionGroupVo : conditionGroupList) {
                if (conditionGroupVo == null || StringUtils.isBlank(conditionGroupVo.getUuid())) {
                    throw new ProcessConfigException(ProcessConfigException.Type.CONDITION, ProcessMessageManager.getStepName());
                }
                conditionGroupUuidSet.add(conditionGroupVo.getUuid());
                validateConditionRelList(conditionGroupVo);
            }
        }
        List<ConditionGroupRelVo> conditionGroupRelList = moveonConfigVo.getConditionGroupRelList();
        if (CollectionUtils.isNotEmpty(conditionGroupRelList)) {
            for (ConditionGroupRelVo conditionGroupRelVo : conditionGroupRelList) {
                if (conditionGroupRelVo == null
                        || !conditionGroupUuidSet.contains(conditionGroupRelVo.getFrom())
                        || !conditionGroupUuidSet.contains(conditionGroupRelVo.getTo())) {
                    throw new ProcessConfigException(ProcessConfigException.Type.CONDITION, ProcessMessageManager.getStepName());
                }
            }
        }
    }

    private void validateConditionRelList(ConditionGroupVo conditionGroupVo) {
        Set<String> conditionUuidSet = new HashSet<>();
        List<ConditionVo> conditionList = conditionGroupVo.getConditionList();
        if (CollectionUtils.isNotEmpty(conditionList)) {
            for (ConditionVo conditionVo : conditionList) {
                if (conditionVo == null || StringUtils.isBlank(conditionVo.getUuid())) {
                    throw new ProcessConfigException(ProcessConfigException.Type.CONDITION, ProcessMessageManager.getStepName());
                }
                conditionUuidSet.add(conditionVo.getUuid());
            }
        }
        List<ConditionRelVo> conditionRelList = conditionGroupVo.getConditionRelList();
        if (CollectionUtils.isNotEmpty(conditionRelList)) {
            for (ConditionRelVo conditionRelVo : conditionRelList) {
                if (conditionRelVo == null
                        || !conditionUuidSet.contains(conditionRelVo.getFrom())
                        || !conditionUuidSet.contains(conditionRelVo.getTo())) {
                    throw new ProcessConfigException(ProcessConfigException.Type.CONDITION, ProcessMessageManager.getStepName());
                }
            }
        }
    }
}
