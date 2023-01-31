/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.api.processtask.automatic;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @author linbq
 * @since 2021/12/17 9:55
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepAutomaticCallbackTestApi extends PublicApiComponentBase {

    @Override
    public String getToken() {
        return "processtask/step/automatic/callback/test";
    }

    @Override
    public String getName() {
        return "自动处理步骤回调测试接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "inputParam1", type = ApiParamType.STRING, isRequired = true, desc = "入参1"),
            @Param(name = "inputParam2", type = ApiParamType.LONG, isRequired = true, desc = "入参2")
    })
    @Output({
            @Param(name = "outputParam1", type = ApiParamType.STRING, desc = "出参1"),
            @Param(name = "outputParam2", type = ApiParamType.LONG, desc = "出参2")
    })
    @Description(desc = "自动处理步骤请求测试接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String inputParam1 = paramObj.getString("inputParam1");
        Long inputParam2 = paramObj.getLong("inputParam2");
        //System.out.println("inputParam1=" + inputParam1);
        //System.out.println("inputParam2=" + inputParam2);
        String outputParam1 = inputParam1 + "-out";
        Long outputParam2 = inputParam2 + 10;
        JSONObject output = new JSONObject();
        output.put("outputParam1", outputParam1);
        output.put("outputParam2", outputParam2);
        return output;
    }
}
