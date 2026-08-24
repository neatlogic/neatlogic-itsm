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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                    regulateConditionRelList(moveonConfigVo);
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

    private void regulateConditionRelList(MoveonConfigVo moveonConfigVo) {
        List<ConditionGroupVo> conditionGroupList = moveonConfigVo.getConditionGroupList();
        if (conditionGroupList == null) {
            conditionGroupList = new ArrayList<>();
            moveonConfigVo.setConditionGroupList(conditionGroupList);
        } else {
            conditionGroupList.removeIf(Objects::isNull);
        }
        for (ConditionGroupVo conditionGroupVo : conditionGroupList) {
            List<ConditionVo> conditionList = conditionGroupVo.getConditionList();
            if (conditionList == null) {
                conditionList = new ArrayList<>();
                conditionGroupVo.setConditionList(conditionList);
            } else {
                conditionList.removeIf(Objects::isNull);
            }
            List<ConditionRelVo> oldConditionRelList = conditionGroupVo.getConditionRelList();
            List<ConditionRelVo> conditionRelList = new ArrayList<>();
            for (int i = 0; i < conditionList.size() - 1; i++) {
                ConditionRelVo conditionRelVo = new ConditionRelVo();
                conditionRelVo.setFrom(conditionList.get(i).getUuid());
                conditionRelVo.setTo(conditionList.get(i + 1).getUuid());
                conditionRelVo.setJoinType(getConditionJoinType(oldConditionRelList, i));
                conditionRelList.add(conditionRelVo);
            }
            conditionGroupVo.setConditionRelList(conditionRelList);
        }

        List<ConditionGroupRelVo> oldConditionGroupRelList = moveonConfigVo.getConditionGroupRelList();
        List<ConditionGroupRelVo> conditionGroupRelList = new ArrayList<>();
        for (int i = 0; i < conditionGroupList.size() - 1; i++) {
            ConditionGroupRelVo conditionGroupRelVo = new ConditionGroupRelVo();
            conditionGroupRelVo.setFrom(conditionGroupList.get(i).getUuid());
            conditionGroupRelVo.setTo(conditionGroupList.get(i + 1).getUuid());
            conditionGroupRelVo.setJoinType(getConditionGroupJoinType(oldConditionGroupRelList, i));
            conditionGroupRelList.add(conditionGroupRelVo);
        }
        moveonConfigVo.setConditionGroupRelList(conditionGroupRelList);
    }

    private String getConditionJoinType(List<ConditionRelVo> conditionRelList, int index) {
        if (CollectionUtils.isNotEmpty(conditionRelList) && index < conditionRelList.size()) {
            ConditionRelVo conditionRelVo = conditionRelList.get(index);
            if (conditionRelVo != null && conditionRelVo.getJoinType() != null) {
                return conditionRelVo.getJoinType();
            }
        }
        return "and";
    }

    private String getConditionGroupJoinType(List<ConditionGroupRelVo> conditionGroupRelList, int index) {
        if (CollectionUtils.isNotEmpty(conditionGroupRelList) && index < conditionGroupRelList.size()) {
            ConditionGroupRelVo conditionGroupRelVo = conditionGroupRelList.get(index);
            if (conditionGroupRelVo != null && conditionGroupRelVo.getJoinType() != null) {
                return conditionGroupRelVo.getJoinType();
            }
        }
        return "and";
    }
}
