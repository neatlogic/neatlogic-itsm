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
}
