/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dependency.handler;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dependency.constvalue.CalleeType;
import codedriver.framework.dependency.core.DependencyHandlerBase;
import codedriver.framework.dependency.core.ICalleeType;
import org.springframework.stereotype.Service;

/**
 * 工单引用服务窗口处理器
 *
 * @author: linbq
 * @since: 2021/4/2 18:14
 **/
@Service
public class WorktimeProcessTaskDependencyHandler extends DependencyHandlerBase {
    /**
     * 表名
     *
     * @return
     */
    @Override
    protected String getTableName() {
        return "processtask";
    }

    /**
     * 被调用者字段
     *
     * @return
     */
    @Override
    protected String getCalleeField() {
        return "worktime_uuid";
    }

    /**
     * 调用者字段
     *
     * @return
     */
    @Override
    protected String getCallerField() {
        return "id";
    }

    /**
     * 解析数据，拼装跳转url，返回引用下拉列表一个选项数据结构
     *
     * @param caller 调用者值
     * @return
     */
    @Override
    protected ValueTextVo parse(Object caller) {
        return null;
    }

    /**
     * 被调用方名
     *
     * @return
     */
    @Override
    public ICalleeType getCalleeType() {
        return CalleeType.WORKTIME;
    }

    /**
     * 依赖关系能否解除
     *
     * @return
     */
    @Override
    public boolean canBeLifted() {
        return false;
    }
}
