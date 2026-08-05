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

package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.dao.mapper.region.RegionMapper;
import neatlogic.framework.dto.condition.ConditionGroupVo;
import neatlogic.framework.dto.region.RegionVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import neatlogic.framework.util.$;
@Component
public class ProcessTaskRegionCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Resource
    RegionMapper regionMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "region";
    }

    @Override
    public String getDisplayName() {
        return $.t("nmpch.processtaskregioncondition.getdisplayname");
    }

	@Override
	public String getHandler(FormConditionModel processWorkcenterConditionType) {
		return FormHandlerType.SELECT.toString();
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

    @Override
    public JSONObject getConfig(ConditionConfigType type) {
        JSONObject config = new JSONObject();
        /** 新数据结构，参考前端表单数据结构**/
        config.put("type", FormHandlerType.SELECT.toString());
        config.put("search", true);
        config.put("dynamicUrl", "api/rest/region/search");
        config.put("rootName", "tbodyList");
        config.put("valueName", "id");
        config.put("textName", "name");
        config.put("tooltipName", "upwardNamePath");
        config.put("multiple", true);
        config.put("value", "");
        config.put("defaultValue", "");
        /** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", true);
        return config;
    }

    @Override
    public Integer getSort() {
        return 12;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        if (value != null) {
            if (value instanceof Long) {
                RegionVo regionVo = regionMapper.getRegionById(Long.parseLong(value.toString()));
                if (regionVo != null) {
                    return regionVo.getUpwardNamePath();
                }
            } else if (value instanceof List) {
                List<Long> valueList = JSON.parseArray(JSON.toJSONString(value), Long.class);
                List<String> textList = new ArrayList<>();
                List<RegionVo> regionVos = regionMapper.getRegionListByIdList(valueList);
                if(CollectionUtils.isNotEmpty(regionVos)){
                    Map<Long,RegionVo> regionVoMap = regionVos.stream().collect(Collectors.toMap(RegionVo::getId, o->o));
                    for (Long valueL : valueList) {
                        if (regionVoMap.containsKey(valueL)){
                            RegionVo region = regionVoMap.get(valueL);
                            if(region != null){
                                textList.add(region.getUpwardNamePath());
                            }else{
                                textList.add(value.toString());
                            }
                        }
                    }
                }
                return String.join("、", textList);
            }
        }
        return value;
    }

    @Override
    public void getSqlConditionWhere(ConditionGroupVo groupVo, Integer index, StringBuilder sqlSb) {
        getSimpleSqlConditionWhere(groupVo.getConditionList().get(index), sqlSb, new ProcessTaskSqlTable().getShortName(), ProcessTaskSqlTable.FieldEnum.REGION_ID.getValue());
    }

    @Override
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
        if (processTaskVo == null) {
            return null;
        }
        return processTaskVo.getRegionId();
    }

    @Override
    public Object getConditionParamDataForHumanization(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
        if (processTaskVo == null) {
            return null;
        }
        if (processTaskVo.getRegionId() == null) {
            return null;
        }
        RegionVo regionVo = regionMapper.getRegionById(processTaskVo.getRegionId());
        if (regionVo == null) {
            return null;
        }
        return regionVo.getName();
    }
}
