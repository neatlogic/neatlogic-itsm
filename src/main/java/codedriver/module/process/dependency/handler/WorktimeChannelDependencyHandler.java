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
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 服务引用服务窗口处理器
 *
 * @author: linbq
 * @since: 2021/4/2 17:41
 **/
@Service
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
            String channelUuid =  (String) map.get("channel_uuid");
            ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
            if (channelVo != null) {
                ValueTextVo valueTextVo = new ValueTextVo();
                valueTextVo.setValue(channelVo.getUuid());
                valueTextVo.setText(String.format("<a href=\"/%s/process.html#/catalog-manage?uuid=%s\" target=\"_blank\">%s</a>", TenantContext.get().getTenantUuid(), channelVo.getUuid(), channelVo.getName()));
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
