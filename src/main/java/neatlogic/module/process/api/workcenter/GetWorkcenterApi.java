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

package neatlogic.module.process.api.workcenter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.condition.core.IConditionHandler;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.condition.core.ProcessTaskConditionFactory;
import neatlogic.framework.process.exception.workcenter.WorkcenterNotFoundException;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.workcenter.WorkcenterMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetWorkcenterApi extends PrivateApiComponentBase {

    @Resource
    WorkcenterMapper workcenterMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    ChannelMapper channelMapper;

    @Resource
    FormMapper formMapper;

    @Override
    public String getToken() {
        return "workcenter/get";
    }

    @Override
    public String getName() {
        return "nmpaw.getworkcenterapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }


    private class FormUtils {
        Map<String, List<FormAttributeVo>> channelFormAttributeMap = new HashMap<>();
        Map<String, JSONObject> channelFormConfigMap = new HashMap<>();

        public String getAttributeUuid(String channelUuid, String attributeLabel) {
            if (!channelFormAttributeMap.containsKey(channelUuid)) {
                channelFormAttributeMap.put(channelUuid, channelMapper.getFormAttributeByChannelUuid(channelUuid));
            }
            if (CollectionUtils.isNotEmpty(channelFormAttributeMap.get(channelUuid))) {
                Optional<FormAttributeVo> op = channelFormAttributeMap.get(channelUuid).stream().filter(d -> d.getLabel().equals(attributeLabel)).findFirst();
                if (op.isPresent()) {
                    return op.get().getUuid();
                }
            }
            return null;
        }

        public JSONObject getFormConfig(String channelUuid) {
            if (!channelFormConfigMap.containsKey(channelUuid)) {
                JSONObject formConfig = channelMapper.getFormVersionByChannelUuid(channelUuid).getFormConfig();
                if (MapUtils.isNotEmpty(formConfig)) {
                    channelFormConfigMap.put(channelUuid, formConfig);
                }
            }
            return channelFormConfigMap.get(channelUuid);
        }
    }


    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "nmpaw.getworkcenterapi.input.param.desc.uuid", isRequired = true)
    })
    @Output({
            @Param(name = "workcenter", explode = WorkcenterVo.class, desc = "nmpaw.getworkcenterapi.output.param.desc.workcenter")
    })
    @Description(desc = "nmpaw.getworkcenterapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        WorkcenterVo workcenter = workcenterMapper.getWorkcenterByUuid(uuid);
        if (workcenter == null) {
            throw new WorkcenterNotFoundException(uuid);
        }
        //完善条件数据
        FormUtils formUtils = new FormUtils();
        JSONObject conditionConfig = workcenter.getConditionConfig();
        JSONArray conditionGroupList = conditionConfig.getJSONArray("conditionGroupList");
        if (CollectionUtils.isNotEmpty(conditionGroupList)) {
            for (int i = 0; i < conditionGroupList.size(); i++) {
                JSONObject conditionGroupObj = conditionGroupList.getJSONObject(i);
                JSONArray conditionList = conditionGroupObj.getJSONArray("conditionList");
                JSONArray channelUuidList = conditionGroupObj.getJSONArray("channelUuidList");
                if (CollectionUtils.isNotEmpty(conditionList)) {
                    for (int j = 0; j < conditionList.size(); j++) {
                        JSONObject conditionObj = conditionList.getJSONObject(j);
                        if (conditionObj.getString("type").equalsIgnoreCase("common")) {
                            IConditionHandler conditionHandler = ProcessTaskConditionFactory.getHandler(conditionObj.getString("name"));
                            if (conditionHandler != null) {
                                Object textList = conditionHandler.valueConversionText(conditionObj.get("valueList"), null);
                                conditionObj.put("text", textList);
                                conditionObj.put("expressionList", conditionHandler.getExpressionList());
                                conditionObj.put("label", conditionHandler.getDisplayName());
                            }
                        } else if (conditionObj.getString("type").equalsIgnoreCase("form") && CollectionUtils.isNotEmpty(channelUuidList) && channelUuidList.size() == 1) {
                            //表单条件仅支持只有一个通道的情况
                            String channelUuid = channelUuidList.getString(0);
                            IConditionHandler conditionHandler = ProcessTaskConditionFactory.getHandler("form");
                            if (conditionHandler != null) {
                                //获取表单属性的uuid
                                JSONObject formConfig = formUtils.getFormConfig(channelUuid);
                                String attributeUuid = formUtils.getAttributeUuid(channelUuid, conditionObj.getString("name"));
                                if (StringUtils.isNotBlank(attributeUuid) && formConfig != null) {
                                    JSONObject configObj = new JSONObject();
                                    configObj.put("formConfig", formConfig);
                                    configObj.put("attributeUuid", attributeUuid);
                                    Object textList = conditionHandler.valueConversionText(conditionObj.get("valueList"), configObj);
                                    conditionObj.put("text", textList);
                                    //表单条件的name就是Label，可以直接使用
                                    conditionObj.put("label", conditionObj.getString("name"));
                                }
                            }
                        }
                    }
                }
            }
        }
        workcenter.setConditionConfig(conditionConfig);
        return workcenter;
    }
}
