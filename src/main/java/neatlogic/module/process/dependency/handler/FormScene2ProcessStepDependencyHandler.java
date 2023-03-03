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

package neatlogic.module.process.dependency.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.FixedTableDependencyHandlerBase;
import neatlogic.framework.dependency.core.IFromType;
import neatlogic.framework.dependency.dto.DependencyInfoVo;
import neatlogic.framework.dependency.dto.DependencyVo;
import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.module.process.dao.mapper.ProcessMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class FormScene2ProcessStepDependencyHandler extends FixedTableDependencyHandlerBase {

    @Resource
    private ProcessMapper processMapper;

    @Override
    protected DependencyInfoVo parse(DependencyVo dependencyVo) {
        JSONObject config = dependencyVo.getConfig();
        if (MapUtils.isEmpty(config)) {
            return null;
        }
        String stepUuid = dependencyVo.getTo();
        String processUuid = config.getString("processUuid");
        ProcessVo processVo = processMapper.getProcessBaseInfoByUuid(processUuid);
        if (processVo == null) {
            return null;
        }
        JSONObject processConfig = processVo.getConfig();
        if (MapUtils.isEmpty(processConfig)) {
            return null;
        }
        JSONObject processObj = processConfig.getJSONObject("process");
        if (MapUtils.isEmpty(processObj)) {
            return null;
        }
        JSONArray stepList = processObj.getJSONArray("stepList");
        if (CollectionUtils.isEmpty(stepList)) {
            return null;
        }
        for (int i = 0; i < stepList.size(); i++) {
            JSONObject stepObj = stepList.getJSONObject(i);
            if (MapUtils.isEmpty(stepObj)) {
                continue;
            }
            String uuid = stepObj.getString("uuid");
            if (!Objects.equals(uuid, stepUuid)) {
                continue;
            }
            JSONObject stepConfig = stepObj.getJSONObject("stepConfig");
            if (MapUtils.isEmpty(stepConfig)) {
                return null;
            }
            String formSceneUuid = stepConfig.getString("formSceneUuid");
            if (StringUtils.isBlank(formSceneUuid)) {
                return null;
            }
            if (!Objects.equals(formSceneUuid, dependencyVo.getFrom())) {
                return null;
            }
            JSONObject dependencyInfoConfig = new JSONObject();
            dependencyInfoConfig.put("processUuid", processUuid);
            dependencyInfoConfig.put("stepUuid", stepUuid);
            List<String> pathList = new ArrayList<>();
            pathList.add("流程管理");
            pathList.add(processVo.getName());
            String urlFormat = "/" + TenantContext.get().getTenantUuid() + "/process.html#/flow-edit?uuid=${DATA.processUuid}&stepUuid=${DATA.stepUuid}";
            return new DependencyInfoVo(stepUuid, dependencyInfoConfig, stepObj.getString("name"), pathList, urlFormat, this.getGroupName());
        }
        return null;
    }

    @Override
    public IFromType getFromType() {
        return FrameworkFromType.FORMSCENE;
    }
}
