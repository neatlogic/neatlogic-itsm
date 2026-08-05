package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.dto.condition.ConditionGroupVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTagVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.SqlDecoratorVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskTagSqlTable;
import neatlogic.module.process.dao.mapper.process.ProcessTagMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

import neatlogic.framework.util.$;
@Component
public class ProcessTaskTagCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {
    @Resource
    private ProcessTagMapper processTagMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "tag";
    }

    @Override
    public String getDisplayName() {
        return $.t("nmpch.processtasktagcondition.getdisplayname");
    }

    @Override
    public String getHandler(FormConditionModel formConditionModel) {
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
        config.put("dynamicUrl", "api/rest/process/tag/get");
        config.put("rootName", "list");
        config.put("valueName", "value");
        config.put("textName", "text");
        config.put("multiple", true);
        config.put("value", "");
        config.put("defaultValue", "");

        /** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", true);
        return config;
    }

    @Override
    public Integer getSort() {
        return 25;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        if (value != null) {
            if (value instanceof Long) {
                List<ProcessTagVo> processTagVos = processTagMapper.getProcessTagByIdList(List.of(Long.valueOf(value.toString())));
                if (CollectionUtils.isNotEmpty(processTagVos)) {
                    return processTagVos.get(0).getName();
                }
            } else if (value instanceof List) {
                List<Long> valueList = JSON.parseArray(JSON.toJSONString(value), Long.class);
                List<ProcessTagVo> processTagVos = processTagMapper.getProcessTagByIdList(valueList);
                if (CollectionUtils.isNotEmpty(processTagVos)) {
                    return processTagVos.stream().map(ProcessTagVo::getName).collect(Collectors.joining("、"));
                }
            }
        }
        return value;
    }


    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList(SqlDecoratorVo sqlDecoratorVo) {
        return List.of(
                new JoinTableColumnVo(
                        new ProcessTaskSqlTable(),
                        new ProcessTaskTagSqlTable(),
                        List.of(
                                new JoinOnVo(
                                        ProcessTaskSqlTable.FieldEnum.ID.getValue(),
                                        ProcessTaskTagSqlTable.FieldEnum.PROCESSTASK_ID.getValue()
                                )
                        )
                )
        );

    }


    @Override
    public void getSqlConditionWhere(ConditionGroupVo groupVo, Integer index, StringBuilder sqlSb) {
        getSimpleSqlConditionWhere(groupVo.getConditionList().get(index), sqlSb, new ProcessTaskTagSqlTable().getShortName(), ProcessTaskTagSqlTable.FieldEnum.TAG_ID.getValue());
    }

    @Override
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo) {
        List<ProcessTagVo> processTaskTagVoList = processTaskMapper.getProcessTaskTagListByProcessTaskId(processTaskStepVo.getProcessTaskId());
        if (CollectionUtils.isEmpty(processTaskTagVoList)) {
            return null;
        }
        return processTaskTagVoList.stream().map(ProcessTagVo::getId).toList();
    }

    @Override
    public Object getConditionParamDataForHumanization(ProcessTaskStepVo processTaskStepVo) {
        List<ProcessTagVo> processTaskTagVoList = processTaskMapper.getProcessTaskTagListByProcessTaskId(processTaskStepVo.getProcessTaskId());
        if (CollectionUtils.isEmpty(processTaskTagVoList)) {
            return null;
        }
        return processTaskTagVoList.stream().map(ProcessTagVo::getName).toList();
    }
}
