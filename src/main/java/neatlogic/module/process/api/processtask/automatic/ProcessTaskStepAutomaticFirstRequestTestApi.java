/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.api.processtask.automatic;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.auth.label.NoAuth;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.systemuser.SystemUser;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.ApiAnonymousAccessSupportEnum;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author linbq
 * @since 2021/12/17 9:54
 **/
@Service
@AuthAction(action = NoAuth.class)
@AuthUser(SystemUser.ANONYMOUS)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepAutomaticFirstRequestTestApi extends PrivateApiComponentBase {

    @Override
    public String getToken() {
        return "processtask/step/automatic/firstrequest/test";
    }

    @Override
    public String getName() {
        return "nmpapa.processtaskstepautomaticfirstrequesttestapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "inputParam1", type = ApiParamType.STRING, isRequired = true, desc = "nmpapa.processtaskstepautomaticfirstrequesttestapi.input.param.desc.inputparam1"),
            @Param(name = "inputParam2", type = ApiParamType.LONG, isRequired = true, desc = "nmpapa.processtaskstepautomaticfirstrequesttestapi.input.param.desc.inputparam2")
    })
    @Output({
            @Param(name = "outputParam1", type = ApiParamType.STRING, desc = "nmpapa.processtaskstepautomaticfirstrequesttestapi.output.param.desc.outputparam1"),
            @Param(name = "outputParam2", type = ApiParamType.LONG, desc = "nmpapa.processtaskstepautomaticfirstrequesttestapi.output.param.desc.outputparam2"),
            @Param(name = "error", type = ApiParamType.STRING, desc = "nmpapa.processtaskstepautomaticfirstrequesttestapi.output.param.desc.error")
    })
    @Description(desc = "nmpapa.processtaskstepautomaticfirstrequesttestapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String inputParam1 = paramObj.getString("inputParam1");
        Long inputParam2 = paramObj.getLong("inputParam2");
        System.out.println("inputParam1=" + inputParam1);
        System.out.println("inputParam2=" + inputParam2);
        StringBuilder stringBuilder = new StringBuilder();
        if (!inputParam1.contains("自动处理节点")) {
            stringBuilder.append("入参'inputParam1'内容不包含'自动处理节点'");
        }
        if (inputParam2 < 10000L) {
            stringBuilder.append(" 入参'inputParam2'值小于'10000'");
        }
        String outputParam1 = inputParam1 + "-out";
        Long outputParam2 = inputParam2 + 10;
        JSONObject output = new JSONObject();
        output.put("outputParam1", outputParam1);
        output.put("outputParam2", outputParam2);
        String error = stringBuilder.toString();
        if (StringUtils.isNotBlank(error)) {
            output.put("error", error);
        }
        return output;
    }

    /**
     * 是否支持匿名访问
     *
     * @return true false
     */
    @Override
    public ApiAnonymousAccessSupportEnum supportAnonymousAccess() {
        return ApiAnonymousAccessSupportEnum.ANONYMOUS_ACCESS_WITHOUT_ENCRYPTION;
    }
}
