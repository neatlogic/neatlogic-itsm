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
        return "地域";
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
