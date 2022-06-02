/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dependency.handler;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.dependency.constvalue.FrameworkFromType;
import codedriver.framework.dependency.core.CustomTableDependencyHandlerBase;
import codedriver.framework.dependency.core.IFromType;
import codedriver.framework.dependency.dto.DependencyInfoVo;
import codedriver.framework.process.dto.ProcessStepVo;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.module.process.dao.mapper.ProcessMapper;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 流程步骤动作引用集成处理器
 *
 * @author: linbq
 * @since: 2021/4/6 10:59
 **/
@Service
public class IntegrationProcessStepDependencyHandler extends CustomTableDependencyHandlerBase {
    @Resource
    private ProcessMapper processMapper;

    /**
     * 表名
     *
     * @return
     */
    @Override
    protected String getTableName() {
        return "process_step_integration";
    }

    /**
     * 被引用者（上游）字段
     *
     * @return
     */
    @Override
    protected String getFromField() {
        return "integration_uuid";
    }

    /**
     * 引用者（下游）字段
     *
     * @return
     */
    @Override
    protected String getToField() {
        return "process_step_uuid";
    }

    @Override
    protected List<String> getToFieldList() {
        return null;
    }

    /**
     * 解析数据，拼装跳转url，返回引用下拉列表一个选项数据结构
     *
     * @param dependencyObj 引用关系数据
     * @return
     */
    @Override
    protected DependencyInfoVo parse(Object dependencyObj) {
        if (dependencyObj instanceof Map) {
            Map<String, Object> map = (Map) dependencyObj;
            String processStepUuid =  (String) map.get("process_step_uuid");
            ProcessStepVo processStepVo = processMapper.getProcessStepByUuid(processStepUuid);
            if (processStepVo != null) {
                ProcessVo processVo = processMapper.getProcessByUuid(processStepVo.getProcessUuid());
                if (processVo != null) {
                    JSONObject dependencyInfoConfig = new JSONObject();
                    dependencyInfoConfig.put("processUuid", processVo.getUuid());
//                    dependencyInfoConfig.put("processName", processVo.getName());
//                    dependencyInfoConfig.put("processStepName", processStepVo.getName());
                    List<String> pathList = new ArrayList<>();
                    pathList.add("流程管理");
                    pathList.add(processVo.getName());
                    String lastName = processStepVo.getName();
//                    String pathFormat = "流程-${DATA.processName}-${DATA.processStepName}";
                    String urlFormat = "/" + TenantContext.get().getTenantUuid() + "/process.html#/flow-edit?uuid=${DATA.processUuid}";
                    return new DependencyInfoVo(processVo.getUuid(), dependencyInfoConfig, lastName, pathList, urlFormat, this.getGroupName());
                }
            }
        }
        return null;
    }

    /**
     * 被引用者（上游）类型
     *
     * @return
     */
    @Override
    public IFromType getFromType() {
        return FrameworkFromType.INTEGRATION;
    }
}
