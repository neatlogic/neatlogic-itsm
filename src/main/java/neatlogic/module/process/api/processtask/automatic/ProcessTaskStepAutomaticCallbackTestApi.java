/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 
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
