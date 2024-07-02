/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
