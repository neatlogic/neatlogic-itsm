package neatlogic.module.process.api.process;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.process.auth.PROCESS_MODIFY;
import neatlogic.framework.process.dto.ProcessVo;
import neatlogic.framework.process.exception.process.ProcessNameRepeatException;
import neatlogic.framework.process.stephandler.core.ProcessMessageManager;
import neatlogic.framework.process.util.ProcessConfigUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.service.ProcessService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
@AuthAction(action = PROCESS_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class ProcessSaveApi extends PrivateApiComponentBase {

    @Resource
    private ProcessService processService;

    @Resource
    private ProcessMapper processMapper;

    @Override
    public String getToken() {
        return "process/save";
    }

    @Override
    public String getName() {
        return "流程保存接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Input({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "流程uuid", isRequired = true),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, isRequired = true, maxLength = 50, desc = "流程名称"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "流程配置内容", isRequired = true)
    })
    @Output({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "流程uuid")
    })
    @Description(desc = "流程保存接口")
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessVo processVo = JSON.toJavaObject(jsonObj, ProcessVo.class);
        ProcessMessageManager.setOperationType(OperationTypeEnum.UPDATE);
        processVo.setConfig(ProcessConfigUtil.regulateProcessConfig(processVo.getConfig()));
        processVo.makeupConfigObj();
        processService.saveProcess(processVo);
        return processVo.getUuid();
    }

    public IValid name() {
        return value -> {
            ProcessVo processVo = JSON.toJavaObject(value, ProcessVo.class);
            if (processMapper.checkProcessNameIsRepeat(processVo) > 0) {
                return new FieldValidResultVo(new ProcessNameRepeatException(processVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

}
