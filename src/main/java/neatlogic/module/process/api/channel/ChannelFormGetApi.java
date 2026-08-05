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

package neatlogic.module.process.api.channel;

import com.alibaba.fastjson.JSON;
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
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.process.ProcessNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.framework.form.attribute.handler.CheckboxHandler;
import neatlogic.module.framework.form.attribute.handler.RadioHandler;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelFormGetApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ChannelMapper channelMapper;

    @Override
    public String getToken() {
        return "process/channel/form/get";
    }

    @Override
    public String getName() {
        return "nmpac.channelformgetapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "channelUuidList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "nmpac.channelformgetapi.input.param.desc.channeluuidlist"),
            @Param(name = "conditionModel", type = ApiParamType.ENUM, rule = "simple,custom,all", isRequired = true,
                    desc = "nmpac.channelformgetapi.input.param.desc.conditionmodel")
    })
    @Output({@Param(name = "Return", explode = FormAttributeVo[].class, desc = "nmpac.channelformgetapi.output.param.desc.return.name")})
    @Description(desc = "nmpac.channelformgetapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<String> channelUuidList =
                JSON.parseArray(jsonObj.getJSONArray("channelUuidList").toJSONString(), String.class);
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
            String formVersionUuid = null;
            String conditionModel = jsonObj.getString("conditionModel");
            {
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
                    formVersionUuid = formAttributeVo.getFormVersionUuid();
                }
                allFormAttributeList.addAll(formAttributeList);
            }
            if (formVersionUuid != null) {
                List<FormAttributeVo> formExtendAttributeList = formMapper.getFormExtendAttributeListByFormUuidAndFormVersionUuid(formUuid, formVersionUuid);
                ListIterator<FormAttributeVo> formIterator = formExtendAttributeList.listIterator();
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
                allFormAttributeList.addAll(formExtendAttributeList);
            }
        }
        return allFormAttributeList;
    }

}
