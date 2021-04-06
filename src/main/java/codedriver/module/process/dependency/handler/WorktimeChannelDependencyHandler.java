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
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.ChannelVo;

import javax.annotation.Resource;

/**
 * 服务引用服务窗口处理器
 *
 * @author: linbq
 * @since: 2021/4/2 17:41
 **/
public class WorktimeChannelDependencyHandler extends DependencyHandlerBase {

    @Resource
    private ChannelMapper channelMapper;

    /**
     * 表名
     *
     * @return
     */
    @Override
    protected String getTableName() {
        return "channel_worktime";
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
        return "channel_uuid";
    }

    /**
     * 解析数据，拼装跳转url，返回引用下拉列表一个选项数据结构
     *
     * @param caller 调用者值
     * @return
     */
    @Override
    protected ValueTextVo parse(Object caller) {
        if (caller instanceof String) {
            ChannelVo channelVo = channelMapper.getChannelByUuid((String) caller);
            if (channelVo != null) {
                ValueTextVo valueTextVo = new ValueTextVo();
                valueTextVo.setValue(caller);
                valueTextVo.setText(String.format("<a href=\"/%s/process.html#/catalog-manage?uuid=%s\">%s</a>", TenantContext.get().getTenantUuid(), channelVo.getUuid(), channelVo.getName()));
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
        return CalleeType.WORKTIME;
    }
}
