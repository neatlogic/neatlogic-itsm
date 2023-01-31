/*
 * Copyright(c) 2022 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */


package neatlogic.module.process.api.processtask;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class ProcessTaskStepBatchCompleteApi extends PublicApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "processtask/step/batch/complete";
    }

    @Override
    public String getName() {
        return "批量完成工单步骤";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskIdList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "工单Id列表"),
            @Param(name = "tag", type = ApiParamType.STRING, desc = "步骤标签"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "处理意见"),
            @Param(name = "userId", type = ApiParamType.STRING, isRequired = true, desc = "处理人userId"),
    })
    @Description(desc = "批量完成工单步骤")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return processTaskService.batchCompleteProcessTaskStep(jsonObj);
    }

}
