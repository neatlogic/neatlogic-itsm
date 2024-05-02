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
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
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
