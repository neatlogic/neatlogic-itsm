package codedriver.module.process.api.form;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskFormDataPublicApi extends PublicApiComponentBase {

    @Resource
    private ProcessTaskService processTaskService;
    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "processtask/form/data/public";
    }

    @Override
    public String getName() {
        return "查询工单表单数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
    })
    @Output({
            @Param(explode = ProcessTaskFormAttributeDataVo[].class, desc = "表单数据列表")
    })
    @Description(desc = "查询工单步骤表单数据")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long processTaskId = jsonObj.getLong("processTaskId");
        processTaskService.checkProcessTaskParamsIsLegal(processTaskId);
        return processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskId);
    }

}
