package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.dto.condition.ConditionGroupVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessTaskConditionType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.SqlDecoratorVo;
import neatlogic.framework.process.workcenter.dto.JoinOnVo;
import neatlogic.framework.process.workcenter.dto.JoinTableColumnVo;
import neatlogic.framework.process.workcenter.table.ProcessTaskSqlTable;
import neatlogic.framework.process.workcenter.table.ProcessTaskStepSqlTable;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import neatlogic.framework.util.$;
@Component
public class ProcessTaskStepNameCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {
    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "stepname";
    }

    @Override
    public String getDisplayName() {
        return $.t("nmpch.processtaskstepnamecondition.getdisplayname");
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
    public JSONObject getConfig(ConditionConfigType configType) {
        JSONObject config = new JSONObject();
        /** 新数据结构，参考前端表单数据结构**/
        config.put("type", FormHandlerType.SELECT.toString());
        config.put("search", true);
        config.put("dynamicUrl", "api/rest/process/channel/search/forselect");
        config.put("rootName", "list");
        config.put("valueName", "value");
        config.put("textName", "text");
        config.put("multiple", true);
        config.put("value", "");
        config.put("defaultValue", "");

        /** 以下代码是为了兼容旧数据结构，前端有些地方还在用 **/
        config.put("isMultiple", true);
        JSONObject mappingObj = new JSONObject();
        mappingObj.put("value", "value");
        mappingObj.put("text", "text");
        config.put("mapping", mappingObj);
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
        return value;
    }

    @Override
    public void getSqlConditionWhere(ConditionGroupVo groupVo, Integer index, StringBuilder sqlSb) {
        getSimpleSqlConditionWhere(groupVo.getConditionList().get(index), sqlSb, new ProcessTaskStepSqlTable().getShortName(), ProcessTaskStepSqlTable.FieldEnum.NAME.getValue());
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList(SqlDecoratorVo sqlDecoratorVo) {
        return new ArrayList<JoinTableColumnVo>() {
            {
                add(new JoinTableColumnVo(new ProcessTaskSqlTable(), new ProcessTaskStepSqlTable(), new ArrayList<JoinOnVo>() {{
                    add(new JoinOnVo(ProcessTaskSqlTable.FieldEnum.ID.getValue(), ProcessTaskStepSqlTable.FieldEnum.PROCESSTASK_ID.getValue()));
                }}));
            }
        };
    }

    @Override
    public boolean isShow(JSONObject object, String type) {
        return !Objects.equals(type, ProcessTaskConditionType.WORKCENTER.getValue());
    }

    @Override
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo) {
        String stepName = processTaskStepVo.getName();
        if (StringUtils.isBlank(stepName) && processTaskStepVo.getId() != null) {
            ProcessTaskStepVo stepVo = processTaskMapper.getProcessTaskStepBaseInfoById(processTaskStepVo.getId());
            if (stepVo != null) {
                stepName = stepVo.getName();
                processTaskStepVo.setName(stepName);
            }
        }
        return stepName;
    }
}
