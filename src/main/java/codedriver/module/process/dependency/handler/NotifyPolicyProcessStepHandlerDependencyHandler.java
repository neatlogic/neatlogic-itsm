/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dependency.handler;

import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dependency.constvalue.CalleeType;
import codedriver.framework.dependency.core.DependencyHandlerBase;
import codedriver.framework.dependency.core.ICalleeType;
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
public class NotifyPolicyProcessStepHandlerDependencyHandler extends DependencyHandlerBase {

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
    protected String getCalleeField() {
        return "notify_policy_id";
    }

    /**
     * 调用者字段
     *
     * @return
     */
    @Override
    protected String getCallerField() {
        return "handler";
    }

    @Override
    protected List<String> getCallerFieldList() {
        return null;
    }

    /**
     * 解析数据，拼装跳转url，返回引用下拉列表一个选项数据结构
     *
     * @param caller 调用者值
     * @return
     */
    @Override
    protected ValueTextVo parse(Object caller) {
        if (caller instanceof Map) {
            Map<String, Object> map = (Map)caller;
            String handler =  (String) map.get("handler");
            String name = ProcessStepHandlerTypeFactory.getName(handler);
            if (StringUtils.isNotBlank(name)) {
                ValueTextVo valueTextVo = new ValueTextVo();
                valueTextVo.setValue(handler);
                valueTextVo.setText(String.format("<a href=\"/%s/process.html#/node-manage\" target=\"_blank\">%s-%s</a>", TenantContext.get().getTenantUuid(), "节点管理", name));
                return valueTextVo;
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
    public ICalleeType getCalleeType() {
        return CalleeType.NOTIFY_POLICY;
    }
}
