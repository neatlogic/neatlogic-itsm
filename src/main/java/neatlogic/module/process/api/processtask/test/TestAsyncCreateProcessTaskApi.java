/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.process.api.processtask.test;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.dto.ProcessTaskAsyncCreateVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskAsyncCreateService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@AuthAction(action = PROCESSTASK_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class TestAsyncCreateProcessTaskApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskAsyncCreateService processTaskAsyncCreateService;

    @Override
    public String getName() {
        return "nmpapt.testasynccreateprocesstaskapi.getname";
    }

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
            @Param(name = "source", type = ApiParamType.STRING, desc = "common.source"),
//            @Param(name = "newProcessTaskId", type = ApiParamType.LONG, desc = "指定工单id，则会使用该id作为工单id"),
            @Param(name = "region", type = ApiParamType.STRING, desc = "common.region", help = "全路径or地域id"),
            @Param(name = "count", type = ApiParamType.INTEGER, isRequired = true, desc = "common.count")
    })
    @Output({
            @Param(name = "processTaskIdList", type = ApiParamType.LONG, desc = "term.itsm.processtaskidlist")
    })
    @Description(desc = "nmpapt.testasynccreateprocesstaskapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date startDate = new Date();
        System.out.println("start = " + dateFormat.format(startDate));
        Integer count = paramObj.getInteger("count");
        String title = paramObj.getString("title");
        String jsonString = paramObj.toJSONString();
        List<Long> processTaskIdList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            JSONObject config = JSONObject.parseObject(jsonString);
//            Long processTaskId = SnowflakeUtil.uniqueLong();
//            config.put("newProcessTaskId", processTaskId);
            config.put("title", title + "-" + i);
            Long processTaskId = processTaskAsyncCreateService.addNewProcessTaskAsyncCreate(new ProcessTaskAsyncCreateVo(config));
            processTaskIdList.add(processTaskId);
        }
        Date endDate = new Date();
        System.out.println("end = " + dateFormat.format(endDate));
        System.out.println("cost = " + (endDate.getTime() - startDate.getTime()));
        JSONObject resultObj = new JSONObject();
        resultObj.put("processTaskIdList", processTaskIdList);
        return resultObj;
    }

    @Override
    public String getToken() {
        return "processtask/asynccreate/test";
    }
}
