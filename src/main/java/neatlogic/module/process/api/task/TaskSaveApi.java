/*
Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.process.api.task;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.task.TaskMapper;
import neatlogic.framework.process.dto.TaskConfigVo;
import neatlogic.framework.process.exception.processtask.task.TaskConfigButtonNameRepeatException;
import neatlogic.framework.process.exception.processtask.task.TaskConfigNameRepeatException;
import neatlogic.framework.process.exception.processtask.task.TaskConfigNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service

@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class TaskSaveApi extends PrivateApiComponentBase {

    @Resource
    TaskMapper taskMapper;
    @Override
    public String getToken() {
        return "task/save";
    }

    @Override
    public String getName() {
        return "新增|更新子任务";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "任务id,存在则修改，否则新增"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "关键字，匹配名称"),
            @Param(name = "num", type = ApiParamType.INTEGER, isRequired = true, desc = "参与人数。-1：不做限制"),
            @Param(name = "policy", type = ApiParamType.ENUM, isRequired = true, desc = "其中一个人完成即可：any,所有人完成：all", rule = "any,all"),
            @Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, desc = "是否激活,激活：1，禁用：0", rule = "0,1"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "配置信息")
    })
    @Output({

    })
    @Description(desc = "新增|更新子任务接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long taskId = jsonObj.getLong("id");
        TaskConfigVo taskConfigVo = JSONObject.toJavaObject(jsonObj, TaskConfigVo.class);
        if(taskMapper.checkTaskConfigNameIsRepeat(taskConfigVo) > 0) {
            throw new TaskConfigNameRepeatException(taskConfigVo.getName());
        }
        JSONObject config = taskConfigVo.getConfig();
        if (MapUtils.isNotEmpty(config)) {
            JSONArray customButtonList = config.getJSONArray("customButtonList");
            if (CollectionUtils.isNotEmpty(customButtonList)) {
                List<String> nameList = new ArrayList<>();
                for (int i = 0; i < customButtonList.size(); i++) {
                    JSONObject customButton = customButtonList.getJSONObject(i);
                    if (MapUtils.isNotEmpty(customButton)) {
                        String name = customButton.getString("name");
                        if (nameList.contains(name)) {
                            throw new TaskConfigButtonNameRepeatException(name);
                        }
                        nameList.add(name);
                    }
                }
            }
        }
        if (taskId != null) {
            TaskConfigVo taskConfigTmp = taskMapper.getTaskConfigById(taskId);
            if(taskConfigTmp == null){
                throw new TaskConfigNotFoundException(taskId.toString());
            }
            taskMapper.updateTaskConfig(taskConfigVo);
        }else{
            taskMapper.insertTaskConfig(taskConfigVo);
        }
        return taskConfigVo.getId();
    }

    public IValid name() {
        return value -> {
            TaskConfigVo taskConfigVo = JSONObject.toJavaObject(value, TaskConfigVo.class);
            if(taskMapper.checkTaskConfigNameIsRepeat(taskConfigVo) > 0) {
                return new FieldValidResultVo(new TaskConfigNameRepeatException(taskConfigVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}
