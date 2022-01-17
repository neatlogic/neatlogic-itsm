/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.dependency.handler;

import codedriver.framework.dependency.constvalue.FromType;
import codedriver.framework.dependency.core.CustomTableDependencyHandlerBase;
import codedriver.framework.dependency.core.IFromType;
import codedriver.framework.dependency.dto.DependencyInfoVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 工单引用服务窗口处理器
 *
 * @author: linbq
 * @since: 2021/4/2 18:14
 **/
@Service
public class WorktimeProcessTaskDependencyHandler extends CustomTableDependencyHandlerBase {
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
     * 被引用者（上游）字段
     *
     * @return
     */
    @Override
    protected String getFromField() {
        return "worktime_uuid";
    }

    /**
     * 引用者（下游）字段
     *
     * @return
     */
    @Override
    protected String getToField() {
        return "id";
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
        return null;
    }

    /**
     * 被引用者（上游）类型
     *
     * @return
     */
    @Override
    public IFromType getFromType() {
        return FromType.WORKTIME;
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
