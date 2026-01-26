package neatlogic.module.process.condition.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.FormHandlerType;
import neatlogic.framework.common.constvalue.ParamType;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.form.constvalue.FormConditionModel;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.condition.core.ProcessTaskConditionBase;
import neatlogic.framework.process.constvalue.ConditionConfigType;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessUserType;
import neatlogic.framework.process.dto.ProcessTaskStepUserVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class ProcessTaskStepUserIdCondition extends ProcessTaskConditionBase implements IProcessTaskCondition {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getName() {
        return "stepuserid";
    }

    @Override
    public String getDisplayName() {
        return "步骤处理人用户ID";
    }

	@Override
	public String getHandler(FormConditionModel processWorkcenterConditionType) {
		return FormHandlerType.INPUT.toString();
	}
	
	@Override
	public String getType() {
		return ProcessFieldType.COMMON.getValue();
	}

    @Override
    public JSONObject getConfig(ConditionConfigType type) {
        JSONObject config = new JSONObject();
        config.put("type", "text");
        config.put("value", "");
        config.put("defaultValue", "");
        config.put("maxlength", 16);
        return config;
    }

    @Override
    public Integer getSort() {
        return 1;
    }

    @Override
    public ParamType getParamType() {
        return ParamType.STRING;
    }

    @Override
    public Object valueConversionText(Object value, JSONObject config) {
        return value;
    }

    @Override
    public Object getConditionParamData(ProcessTaskStepVo processTaskStepVo) {
        if (processTaskStepVo.getId() != null) {
            List<ProcessTaskStepUserVo> processTaskStepUserList = processTaskMapper.getProcessTaskStepUserByStepId(processTaskStepVo.getId(), ProcessUserType.MAJOR.getValue());
            if (CollectionUtils.isNotEmpty(processTaskStepUserList)) {
                UserVo user = userMapper.getUserByUserUuid(processTaskStepUserList.get(0).getUserUuid());
                if (user != null) {
                    return user.getUserId();
                }
            }
        }
        return null;
    }
}
