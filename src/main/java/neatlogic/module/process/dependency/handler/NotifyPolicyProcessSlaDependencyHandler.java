/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.dependency.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.dependency.constvalue.FrameworkFromType;
import neatlogic.framework.dependency.core.CustomDependencyHandlerBase;
import neatlogic.framework.dependency.core.IFromType;
import neatlogic.framework.dependency.dto.DependencyInfoVo;
import neatlogic.framework.process.dto.ProcessSlaVo;
import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 流程时效引用通知策略处理器
 *
 * @author: linbq
 * @since: 2021/4/5 14:31
 **/
@Service
public class NotifyPolicyProcessSlaDependencyHandler extends CustomDependencyHandlerBase {
    @Resource
    private ProcessMapper processMapper;

    /**
     * 表名
     *
     * @return
     */
    @Override
    protected String getTableName() {
        return "process_sla_notify_policy";
    }

    /**
     * 被引用者（上游）字段
     *
     * @return
     */
    @Override
    protected String getFromField() {
        return "notify_policy_id";
    }

    /**
     * 引用者（下游）字段
     *
     * @return
     */
    @Override
    protected String getToField() {
        return "sla_uuid";
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
            String slaUuid =  (String) map.get("sla_uuid");
            ProcessSlaVo processSlaVo = processMapper.getProcessSlaByUuid(slaUuid);
            if (processSlaVo != null) {
                ProcessVo processVo = processMapper.getProcessByUuid(processSlaVo.getProcessUuid());
                if (processVo != null) {
                    JSONObject dependencyInfoConfig = new JSONObject();
                    dependencyInfoConfig.put("processUuid", processVo.getUuid());
//                    dependencyInfoConfig.put("processName", processVo.getName());
//                    dependencyInfoConfig.put("processSlaName", processSlaVo.getName());
                    List<String> pathList = new ArrayList<>();
                    pathList.add("流程管理");
                    pathList.add(processVo.getName());
                    String lastName = processSlaVo.getName();
//                    String pathFormat = "流程-${DATA.processName}-${DATA.processSlaName}";
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
        return FrameworkFromType.NOTIFY_POLICY;
    }
}
