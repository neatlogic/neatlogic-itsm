/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.dependency.handler;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.CustomTableDependencyHandlerBase;
import neatlogic.framework.dependency.core.IFromType;
import neatlogic.framework.dependency.dto.DependencyInfoVo;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dto.ChannelVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 服务引用服务窗口处理器
 *
 * @author: linbq
 * @since: 2021/4/2 17:41
 **/
@Service
public class WorktimeChannelDependencyHandler extends CustomTableDependencyHandlerBase {

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
        return "channel_uuid";
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
            String channelUuid =  (String) map.get("channel_uuid");
            ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
            if (channelVo != null) {
                JSONObject dependencyInfoConfig = new JSONObject();
                dependencyInfoConfig.put("channelUuid", channelVo.getUuid());
//                dependencyInfoConfig.put("channelName", channelVo.getName());
                List<String> pathList = new ArrayList<>();
                pathList.add("服务目录管理");
                String lastName = channelVo.getName();
//                String pathFormat = "服务目录管理-${DATA.channelName}";
                String urlFormat = "/" + TenantContext.get().getTenantUuid() + "/process.html#/catalog-manage?uuid=#{DATA.channelUuid}";
                return new DependencyInfoVo(channelVo.getUuid(), dependencyInfoConfig, lastName, pathList, urlFormat, this.getGroupName());
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
        return FrameworkFromType.WORKTIME;
    }
}
