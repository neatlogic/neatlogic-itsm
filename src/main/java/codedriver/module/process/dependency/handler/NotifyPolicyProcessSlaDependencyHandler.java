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
import codedriver.framework.process.dto.ProcessSlaVo;
import codedriver.framework.process.dto.ProcessVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 流程时效引用通知策略处理器
 *
 * @author: linbq
 * @since: 2021/4/5 14:31
 **/
@Service
public class NotifyPolicyProcessSlaDependencyHandler extends CustomTableDependencyHandlerBase {
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
                    dependencyInfoConfig.put("processName", processVo.getName());
                    dependencyInfoConfig.put("processSlaName", processSlaVo.getName());
                    String pathFormat = "流程-${DATA.processName}-${DATA.processSlaName}";
                    String urlFormat = "/" + TenantContext.get().getTenantUuid() + "/process.html#/flow-edit?uuid=${DATA.processUuid}";
                    return new DependencyInfoVo(processVo.getUuid(), dependencyInfoConfig, pathFormat, urlFormat);
//                    DependencyInfoVo dependencyInfoVo = new DependencyInfoVo();
//                    dependencyInfoVo.setValue(processSlaVo.getUuid());
//                    dependencyInfoVo.setText(String.format("<a href=\"/%s/process.html#/flow-edit?uuid=%s\" target=\"_blank\">%s-%s</a>", TenantContext.get().getTenantUuid(), processVo.getUuid(), processVo.getName(), processSlaVo.getName()));
//                    return dependencyInfoVo;
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
        return FromType.NOTIFY_POLICY;
    }
}
