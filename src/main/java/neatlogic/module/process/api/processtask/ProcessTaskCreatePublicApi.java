package neatlogic.module.process.api.processtask;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.crossover.IProcessTaskCreatePublicApiCrossoverService;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import neatlogic.module.process.service.ProcessTaskCreatePublicService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskCreatePublicApi extends PublicApiComponentBase implements IProcessTaskCreatePublicApiCrossoverService {


    @Resource
    private ProcessTaskCreatePublicService processTaskCreatePublicService;

    @Override
    public String getToken() {
        return "processtask/create/public";
    }

    @Override
    public String getName() {
        return "上报工单(供第三方使用)";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Input({
            @Param(name = "channel", type = ApiParamType.STRING, isRequired = true, desc = "支持channelUuid和channelName入参"),
            @Param(name = "title", type = ApiParamType.STRING, isRequired = true, maxLength = 200, desc = "标题"),
            @Param(name = "owner", type = ApiParamType.STRING, isRequired = true, desc = "上报人uuid和上报人id入参"),
            @Param(name = "reporter", type = ApiParamType.STRING, desc = "代报人"),
            @Param(name = "priority", type = ApiParamType.STRING, isRequired = true, desc = "优先级"),
            @Param(name = "formAttributeDataList", type = ApiParamType.JSONARRAY, desc = "表单属性数据列表"),
            @Param(name = "hidecomponentList", type = ApiParamType.JSONARRAY, desc = "隐藏表单属性列表"),
            @Param(name = "readcomponentList", type = ApiParamType.JSONARRAY, desc = "只读表单属性列表"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "描述"),
            @Param(name = "fileIdList", type = ApiParamType.JSONARRAY, desc = "附件id列表"),
            @Param(name = "handlerStepInfo", type = ApiParamType.JSONOBJECT, desc = "处理器特有的步骤信息"),
            @Param(name = "source", type = ApiParamType.STRING, desc = "来源"),
            @Param(name = "region", type = ApiParamType.STRING, desc = "地域")

    })
    @Output({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单id")
    })
    @Example(example = "{\"title\":\"test上报2\",\"channel\":\"linbq_1206_1119\",\"owner\":\"admin\",\"priority\":\"普通\",\"formAttributeDataList\":[{\"label\":\"下拉框_1\",\"dataList\":[\"ezproxy\",\"balantflow\"]},{\"label\":\"下拉框_2\",\"dataList\":[\"a2\",\"a1\"]},{\"label\":\"下拉框_3\",\"dataList\":[\"子系统描述_1\",\"子系统描述_2\",\"子系统描述_3\"]},{\"label\":\"下拉框_4\",\"dataList\":[\"闫雅\",\"陈宁\",\"蒋琪\"]}]}")
    @Description(desc = "上报工单(供第三方使用)")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return processTaskCreatePublicService.createProcessTask(jsonObj);
    }
}
