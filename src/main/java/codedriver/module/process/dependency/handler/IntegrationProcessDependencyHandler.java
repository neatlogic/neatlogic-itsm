/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dependency.handler;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.dependency.constvalue.FromType;
import codedriver.framework.dependency.core.CustomTableDependencyHandlerBase;
import codedriver.framework.dependency.core.IFromType;
import codedriver.framework.dependency.dto.DependencyInfoVo;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ProcessVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 流程动作引用集成处理器
 *
 * @author: linbq
 * @since: 2021/4/6 10:58
 **/
@Service
public class IntegrationProcessDependencyHandler extends CustomTableDependencyHandlerBase {
    @Resource
    private ProcessMapper processMapper;

    /**
     * 表名
     *
     * @return
     */
    @Override
    protected String getTableName() {
        return "process_integration";
    }

    /**
     * 被调用者字段
     *
     * @return
     */
    @Override
    protected String getFromField() {
        return "integration_uuid";
    }

    /**
     * 调用者字段
     *
     * @return
     */
    @Override
    protected String getToField() {
        return "process_uuid";
    }

    @Override
    protected List<String> getToFieldList() {
        return null;
    }

    /**
     * 解析数据，拼装跳转url，返回引用下拉列表一个选项数据结构
     *
     * @param to 调用者值
     * @return
     */
    @Override
    protected DependencyInfoVo parse(Object to) {
        if (to instanceof Map) {
            Map<String, Object> map = (Map) to;
            String processUuid =  (String) map.get("process_uuid");
            ProcessVo processVo = processMapper.getProcessByUuid(processUuid);
            if (processVo != null) {
                DependencyInfoVo dependencyInfoVo = new DependencyInfoVo();
                dependencyInfoVo.setValue(processVo.getUuid());
                dependencyInfoVo.setText(String.format("<a href=\"/%s/process.html#/flow-edit?uuid=%s\" target=\"_blank\">流程-%s</a>", TenantContext.get().getTenantUuid(), processVo.getUuid(), processVo.getName()));
                return dependencyInfoVo;
            }
        }
        return null;
    }

    /**
     * 被调用方名
     *
     * @return
     */
    @Override
    public IFromType getFromType() {
        return FromType.INTEGRATION;
    }
}
