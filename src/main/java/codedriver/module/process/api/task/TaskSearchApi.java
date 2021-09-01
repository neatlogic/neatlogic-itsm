/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.task;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.task.TaskMapper;
import codedriver.framework.process.dto.TaskConfigVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.TableResultUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service

@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class TaskSearchApi extends PrivateApiComponentBase {
    @Resource
    TaskMapper taskMapper;

    @Override
    public String getToken() {
        return "task/search";
    }

    @Override
    public String getName() {
        return "搜索任务";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字，匹配名称"),
            @Param(name = "isActive", type = ApiParamType.ENUM, desc = "是否激活", rule = "0,1"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = true, desc = "当前页码"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = true, desc = "页大小"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, isRequired = true, desc = "总页数"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, isRequired = true, desc = "总行数"),
            @Param(name = "tbodyList", explode = TaskConfigVo[].class, desc = "优先级列表")
    })
    @Description(desc = "搜索任务接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        TaskConfigVo taskConfigVo = JSONObject.toJavaObject(jsonObj, TaskConfigVo.class);
        List<TaskConfigVo> taskConfigVoList = new ArrayList<>();
        int rowNum = taskMapper.searchTaskConfigCount(taskConfigVo);
        taskConfigVo.setRowNum(rowNum);
        if(rowNum >0){
            taskConfigVoList = taskMapper.searchTaskConfig(taskConfigVo);
        }
        return TableResultUtil.getResult(taskConfigVoList,taskConfigVo);
    }

}
