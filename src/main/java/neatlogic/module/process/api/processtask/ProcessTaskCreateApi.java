package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskCreatePublicService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class ProcessTaskCreateApi extends PrivateApiComponentBase {


    @Resource
    private ProcessTaskCreatePublicService processTaskCreatePublicService;

    @Override
    public String getToken() {
        return "processtask/create";
    }

    @Override
    public String getName() {
        return "nmpap.processtaskcreateapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Input({
            @Param(name = "channel", type = ApiParamType.STRING, isRequired = true, desc = "term.itsm.channel", help = "支持channelUuid和channelName入参"),
            @Param(name = "title", type = ApiParamType.STRING, isRequired = true, maxLength = 80, desc = "common.title"),
            @Param(name = "owner", type = ApiParamType.STRING, isRequired = true, desc = "term.itsm.owner", help = "上报人uuid和上报人id入参"),
            @Param(name = "reporter", type = ApiParamType.STRING, desc = "term.itsm.reporter"),
            @Param(name = "priority", type = ApiParamType.STRING, isRequired = true, desc = "common.priority"),
            @Param(name = "formAttributeDataList", type = ApiParamType.JSONARRAY, desc = "term.itsm.formattributedatalist"),
            @Param(name = "hidecomponentList", type = ApiParamType.JSONARRAY, desc = "term.itsm.hidecomponentlist"),
            @Param(name = "readcomponentList", type = ApiParamType.JSONARRAY, desc = "term.itsm.readcomponentlist"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "common.content"),
            @Param(name = "filePathPrefix", type = ApiParamType.STRING, defaultValue = "file:", desc = "common.filepathprefix"),
            @Param(name = "filePathList", type = ApiParamType.JSONARRAY, desc = "common.filepathlist"),
            @Param(name = "fileIdList", type = ApiParamType.JSONARRAY, desc = "common.fileidlist"),
            @Param(name = "handlerStepInfo", type = ApiParamType.JSONOBJECT, desc = "term.itsm.handlerstepinfo"),
            @Param(name = "source", type = ApiParamType.STRING, desc = "common.source")
    })
    @Output({
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "term.itsm.processtaskid")
    })
    @Example(example = "{\"title\":\"test上报2\",\"channel\":\"linbq_1206_1119\",\"owner\":\"admin\",\"priority\":\"普通\",\"formAttributeDataList\":[{\"label\":\"下拉框_1\",\"dataList\":[\"ezproxy\",\"balantflow\"]},{\"label\":\"下拉框_2\",\"dataList\":[\"a2\",\"a1\"]},{\"label\":\"下拉框_3\",\"dataList\":[\"子系统描述_1\",\"子系统描述_2\",\"子系统描述_3\"]},{\"label\":\"下拉框_4\",\"dataList\":[\"闫雅\",\"陈宁\",\"蒋琪\"]}]}")
    @Description(desc = "nmpap.processtaskcreateapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        return processTaskCreatePublicService.createProcessTask(jsonObj);
    }
}
