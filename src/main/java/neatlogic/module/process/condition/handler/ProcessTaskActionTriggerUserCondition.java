package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.GroupSearch;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import org.springframework.stereotype.Component;

@Component
public class ProcessTaskActionTriggerUserCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Override
    public String getName() {
        return "actiontriggeruser";
    }

    @Override
    public String getDisplayName() {
        return "动作触发人";
    }

    @Override
    public String getHandler(FormConditionModel processWorkcenterConditionType) {
        return FormHandlerType.USERSELECT.toString();
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public JSONObject getConfig(ConditionConfigType configType) {
        JSONObject config = new JSONObject();
        config.put("type", FormHandlerType.USERSELECT.toString());
        config.put("initConfig", new JSONObject() {
            {
                this.put("excludeList", new JSONArray() {{
                }});
                this.put("groupList", new JSONArray() {
                    {
                        this.add(GroupSearch.USER.getValue());
                    }
                });
                this.put("includeList", new JSONArray() {
                    {
                    }
                });
            }
        });
        config.put("multiple", true);
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
        return null;
    }

    @Override
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo){
        if(UserContext.get() != null){
            return UserContext.get().getUserUuid();
        }
        return null;
    }

}
