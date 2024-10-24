/*Copyright (C) 2023  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.api.processtask.automatic;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.ApiAnonymousAccessSupportEnum;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author linbq
 * @since 2021/12/17 9:55
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepAutomaticCallbackTestApi extends PrivateApiComponentBase {

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
            @Param(name = "outputParam2", type = ApiParamType.LONG, desc = "出参2"),
            @Param(name = "error", type = ApiParamType.STRING, desc = "异常信息")
    })
    @Description(desc = "自动处理步骤回调测试接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        String inputParam1 = paramObj.getString("inputParam1");
        Long inputParam2 = paramObj.getLong("inputParam2");
        System.out.println("inputParam1=" + inputParam1);
        System.out.println("inputParam2=" + inputParam2);
        StringBuilder stringBuilder = new StringBuilder();
        if (!inputParam1.contains("自动处理节点2")) {
            stringBuilder.append("入参'inputParam1'内容不包含'自动处理节点2'");
        }
        if (inputParam2 < 20000L) {
            stringBuilder.append(" 入参'inputParam2'值小于'20000'");
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
