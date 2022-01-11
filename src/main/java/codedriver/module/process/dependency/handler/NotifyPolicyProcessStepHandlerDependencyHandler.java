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
import codedriver.framework.process.stephandler.core.ProcessStepHandlerTypeFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 节点关理组件引用通知策略处理器
 *
 * @author: linbq
 * @since: 2021/4/5 14:31
 **/
@Service
public class NotifyPolicyProcessStepHandlerDependencyHandler extends CustomTableDependencyHandlerBase {

    /**
     * 表名
     *
     * @return
     */
    @Override
    protected String getTableName() {
        return "process_step_handler_notify_policy";
    }

    /**
     * 被调用者字段
     *
     * @return
     */
    @Override
    protected String getFromField() {
        return "notify_policy_id";
    }

    /**
     * 调用者字段
     *
     * @return
     */
    @Override
    protected String getToField() {
        return "handler";
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
            String handler =  (String) map.get("handler");
            String name = ProcessStepHandlerTypeFactory.getName(handler);
            if (StringUtils.isNotBlank(name)) {
                DependencyInfoVo dependencyInfoVo = new DependencyInfoVo();
                dependencyInfoVo.setValue(handler);
                dependencyInfoVo.setText(String.format("<a href=\"/%s/process.html#/node-manage\" target=\"_blank\">%s-%s</a>", TenantContext.get().getTenantUuid(), "节点管理", name));
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
        return FromType.NOTIFY_POLICY;
    }
}
