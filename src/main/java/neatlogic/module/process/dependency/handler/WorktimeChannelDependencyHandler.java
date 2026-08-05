/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.dependency.handler;

import neatlogic.framework.util.$;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.CustomDependencyHandlerBase;
import neatlogic.framework.dependency.core.IFromType;
import neatlogic.framework.dependency.dto.DependencyInfoVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
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
public class WorktimeChannelDependencyHandler extends CustomDependencyHandlerBase {

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
        pathList.add($.t("nmpdh.path.servicecatalogmanagement"));
                String lastName = channelVo.getName();
//                String pathFormat = "服务目录管理-${DATA.channelName}";
                String urlFormat = "/" + TenantContext.get().getTenantUuid() + "/process.html#/catalog-manage?uuid=${DATA.channelUuid}";
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
