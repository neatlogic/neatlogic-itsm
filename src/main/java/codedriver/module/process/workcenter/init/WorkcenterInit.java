/*
 * Copyright (c)  2021 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.workcenter.init;

import codedriver.framework.applicationlistener.core.ApplicationListenerBase;
import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadlocal.TenantContext;
import codedriver.framework.asynchronization.threadpool.CachedThreadPool;
import codedriver.framework.dao.mapper.TenantMapper;
import codedriver.framework.dto.TenantVo;
import codedriver.framework.process.constvalue.ProcessWorkcenterType;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: 工单中心默认分类
 * @Package codedriver.module.process.workcenter.init
 * @Description: 工单中心默认分类
 * @Author: 89770
 * @Date: 2021/1/5 17:55
 **/
@Component
public class WorkcenterInit extends ApplicationListenerBase {
    public List<WorkcenterVo> workcenterList = new ArrayList<>();
    private List<TenantVo> tenantList = new ArrayList<>();

    @Autowired
    private TenantMapper tenantMapper;

    @Autowired
    private WorkcenterMapper workcenterMapper;

    /**
     * @Description:
     * @Author: 89770
     * @Date: 2021/1/5 18:01
     * @Params: * @param null:
     * @Returns: * @return: null
     **/
    private WorkcenterVo all() {
        WorkcenterVo workcenterVo = new WorkcenterVo();
        workcenterVo.setUuid("allProcessTask");
        workcenterVo.setName("所有工单");
        workcenterVo.setConditionConfig("{\"handlerType\":\"simple\",\"conditionGroupList\":[{\"conditionList\":[{\"expression\":\"between\",\"valueList\":[{\"timeRange\":\"1\",\"timeUnit\":\"year\"}],\"name\":\"starttime\",\"type\":\"common\",\"uuid\":\"fce9f6d909b04fb5b10ee40a3e806396\"}],\"channelUuidList\":[],\"uuid\":\"6289d02329b442ad8912628e142be837\"}],\"conditionGroupRelList\":[]}");
        workcenterVo.setType(ProcessWorkcenterType.FACTORY.getValue());
        workcenterVo.setSort(1);
        return  workcenterVo;
    }
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        for (TenantVo tenantVo : tenantList) {
            CachedThreadPool.execute(new InsertWorkcenterRunner(tenantVo.getUuid()));
        }
    }
    class InsertWorkcenterRunner extends CodeDriverThread {
        private final String tenantUuid;
        public InsertWorkcenterRunner(String tenantUuid){
            this.tenantUuid = tenantUuid;
        }

        @Override
        protected void execute() {
            Thread.currentThread().setName("WORKCENTER-INIT-" + tenantUuid);
            // 切换租户数据源
            TenantContext.get().switchTenant(tenantUuid).setUseDefaultDatasource(false);
            for (WorkcenterVo workcenterVo :workcenterList){
                workcenterMapper.insertWorkcenter(workcenterVo);
            }
        }
    }

    @Override
    protected void myInit() {
        tenantList = tenantMapper.getAllActiveTenant();
        workcenterList.add(all());
    }
}
