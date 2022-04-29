/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask.test;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.workcenter.WorkcenterMapper;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Title: RandomCreateProcessTaskApi
 * @Package processtask
 * @Description: 1、create:随机获取服务、用户、优先级创建工单
 * 2、execute:随机执行工单步骤(因为异步原因，在create后需延迟50s后再执行工单)
 * @Author: 89770
 * @Date: 2020/12/28 10:49
 **/
@Service
@OperationType(type = OperationTypeEnum.OPERATE)
@AuthAction(action = PROCESS_BASE.class)
class ConverWorkcenterConditionConfigApi extends PrivateApiComponentBase {
    @Resource
    private WorkcenterMapper workcenterMapper;


    @Override
    public String getName() {
        return "转换工单中心配置数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Description(desc = "转换工单中心配置数据（发版后使用，可以重复执行）")
    public Object myDoService(JSONObject paramJson) throws Exception {
        List<WorkcenterVo> workcenterList = workcenterMapper.getAllWorkcenterConditionConfig();
        if (CollectionUtils.isNotEmpty(workcenterList)) {
            for (WorkcenterVo workcenterVo : workcenterList) {
                if (MapUtils.isNotEmpty(workcenterVo.getConditionConfig())) {
                    if (workcenterVo.getConditionConfig().containsKey("conditionConfig")) {
                        workcenterVo.setConditionConfig(workcenterVo.getConditionConfig().getJSONObject("conditionConfig"));
                        workcenterMapper.updateWorkcenterCondition(workcenterVo);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getToken() {
        return "workcenter/converconditionconfig";
    }


}
