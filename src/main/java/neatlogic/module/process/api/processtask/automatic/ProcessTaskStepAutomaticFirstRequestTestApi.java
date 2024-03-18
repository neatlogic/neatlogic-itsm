/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

/**
 * @author linbq
 * @since 2021/12/17 9:54
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskStepAutomaticFirstRequestTestApi extends PublicApiComponentBase {

    @Override
    public String getToken() {
        return "processtask/step/automatic/firstrequest/test";
    }

    @Override
    public String getName() {
        return "自动处理步骤请求测试接口";
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
