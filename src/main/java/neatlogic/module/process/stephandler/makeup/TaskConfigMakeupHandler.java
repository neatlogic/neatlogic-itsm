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

package neatlogic.module.process.stephandler.makeup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.dto.ProcessStepTaskConfigVo;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IProcessStepMakeupHandler;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public class TaskConfigMakeupHandler implements IProcessStepMakeupHandler {

    @Resource
    private ProcessMapper processMapper;

    @Override
    public String getName() {
        return "taskConfig";
    }

    @Override
    public void makeup(IProcessStepInternalHandler processStepInternalHandler, ProcessStepVo processStepVo, JSONObject stepConfigObj, String action) {
        //保存子任务
        JSONObject taskConfig = stepConfigObj.getJSONObject("taskConfig");
        if (MapUtils.isNotEmpty(taskConfig)) {
            ProcessStepTaskConfigVo taskConfigVo = JSON.toJavaObject(taskConfig, ProcessStepTaskConfigVo.class);
            if (CollectionUtils.isNotEmpty(taskConfigVo.getIdList())) {
                if (Objects.equals(action, "save")) {
                    taskConfigVo.getIdList().forEach(id -> {
                        ProcessStepTaskConfigVo tmpVo = new ProcessStepTaskConfigVo(processStepVo.getUuid(), id);
                        processMapper.insertProcessStepTask(tmpVo);
                    });
                } else if (Objects.equals(action, "delete")) {
                    processMapper.deleteProcessStepTaskByProcessStepUuid(processStepVo.getUuid());
                }
            }
        }
    }
}
