/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

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

package neatlogic.module.process.api.channel;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVo;
import neatlogic.framework.form.exception.FormNotFoundException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.framework.form.attribute.handler.CheckboxHandler;
import neatlogic.module.framework.form.attribute.handler.RadioHandler;
import neatlogic.module.process.dao.mapper.ProcessMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelFormGetApi extends PrivateApiComponentBase {

    @Autowired
    private FormMapper formMapper;

    @Autowired
    private ProcessMapper processMapper;

    @Autowired
    private ChannelMapper channelMapper;

    @Override
    public String getToken() {
        return "process/channel/form/get";
    }

    @Override
    public String getName() {
        return "服务绑定的表单属性信息获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "channelUuidList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "服务uuidList"),
            @Param(name = "conditionModel", type = ApiParamType.ENUM, rule = "simple,custom,all", isRequired = true,
                    desc = "条件模型 simple|custom|all  ,  simple:目前用于用于工单中心条件过滤简单模式, custom:目前用于用于工单中心条件过自定义模式、条件分流和sla条件;默认custom")
    })
    @Output({@Param(name = "Return", explode = FormAttributeVo[].class, desc = "表单属性列表")})
    @Description(desc = "服务绑定的表单属性信息获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<String> channelUuidList =
                JSONObject.parseArray(jsonObj.getJSONArray("channelUuidList").toJSONString(), String.class);
        if (CollectionUtils.isEmpty(channelUuidList)) {
            throw new ChannelNotFoundException(channelUuidList.toString());
        }
        List<ChannelVo> channelList = channelMapper.getChannelByUuidList(channelUuidList);
        if (CollectionUtils.isEmpty(channelList)) {
            throw new ChannelNotFoundException(channelList.toString());
        }
        List<FormAttributeVo> allFormAttributeList = new ArrayList<>();
        for (ChannelVo channel : channelList) {
            String processUuid = channel.getProcessUuid();
            if (processUuid == null) {
                continue;
            }
            ProcessVo process = processMapper.getProcessByUuid(processUuid);
            if (process == null) {
                throw new ProcessNotFoundException(processUuid);
            }
            String formUuid = process.getFormUuid();
            if (formUuid == null) {
                continue;
            }
            FormVo formVo = formMapper.getFormByUuid(formUuid);
            // 判断表单是否存在
            if (formVo == null) {
                throw new FormNotFoundException(formUuid);
            }
            String conditionModel = jsonObj.getString("conditionModel");
            List<FormAttributeVo> formAttributeList = formMapper.getFormAttributeList(new FormAttributeVo(formUuid));
            ListIterator<FormAttributeVo> formIterator = formAttributeList.listIterator();
            while (formIterator.hasNext()) {
                FormAttributeVo formAttributeVo = formIterator.next();
                //如果是radio 则改为 checkbox，前端解决多选，取消选择问题
                if (!Objects.equals("all", conditionModel) && Objects.equals(formAttributeVo.getHandler(), new RadioHandler().getHandler())) {
                    formAttributeVo.setHandler(new CheckboxHandler().getHandler());
                }
                IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
                if (handler == null || (!Objects.equals("all", conditionModel) && !handler.isConditionable())) {
                    formIterator.remove();
                    continue;
                }
                formAttributeVo.setConditionModel(FormConditionModel.getFormConditionModel(conditionModel));
                formAttributeVo.setType("form");
                formAttributeVo.setChannelUuid(channel.getUuid());
                formAttributeVo.setIsUseFormConfig(handler.isUseFormConfig());
            }
            allFormAttributeList.addAll(formAttributeList);
        }
        return allFormAttributeList;
    }

}
