package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

import neatlogic.framework.util.$;
@Component
public class ProcessTaskOwnerLevelCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getName() {
        return "ownerlevel";
    }

    @Override
    public String getDisplayName() {
        return $.t("nmpch.processtaskownerlevelcondition.getdisplayname");
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
        config.put("type", FormHandlerType.SELECT.toString());
        JSONArray dataList = new JSONArray();
        dataList.add(new ValueTextVo("1", $.t("nmpch.processtaskownerlevelcondition.runtime.label.1")));
        dataList.add(new ValueTextVo("0", $.t("nmpch.processtaskownerlevelcondition.runtime.label.2")));
        config.put("dataList", dataList);
        return config;
    }

    @Override
    public Integer getSort() {
        return 19;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.ARRAY;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        if (Objects.equals(value, "1")) {
            return $.t("nmpch.processtaskownerlevelcondition.runtime.label.1");
        } else if (Objects.equals(value, "0")) {
            return $.t("nmpch.processtaskownerlevelcondition.runtime.label.2");
        }
        return value;
    }

    @Override
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo) {
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskStepVo.getProcessTaskId());
        if (processTaskVo == null) {
            return null;
        }
        UserVo ownerVo = userMapper.getUserBaseInfoByUuid(processTaskVo.getOwner());
        if (ownerVo != null) {
            return ownerVo.getVipLevel();
        }
        return null;
    }
}
