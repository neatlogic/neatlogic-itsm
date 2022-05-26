/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.task;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.exception.type.ParamNotExistsException;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.task.TaskMapper;
import codedriver.framework.process.dto.TaskConfigVo;
import codedriver.framework.process.exception.processtask.task.TaskConfigButtonNameRepeatException;
import codedriver.framework.process.exception.processtask.task.TaskConfigNameRepeatException;
import codedriver.framework.process.exception.processtask.task.TaskConfigNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            @Param(name = "num", type = ApiParamType.INTEGER, isRequired = true, desc = "参与人数。-1：不做限制", rule = "single,many"),
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
