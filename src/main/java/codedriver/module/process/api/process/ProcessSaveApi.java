package codedriver.module.process.api.process;

import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.process.exception.process.ProcessNameRepeatException;
import codedriver.framework.process.util.ProcessConfigUtil;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import codedriver.module.process.dao.mapper.ProcessMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.auth.PROCESS_MODIFY;
import codedriver.module.process.service.ProcessService;

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
            @Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired = true, maxLength = 50, desc = "流程名称"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, desc = "流程配置内容", isRequired = true)
    })
    @Output({
            @Param(name = "uuid", type = ApiParamType.STRING, desc = "流程uuid")
    })
    @Description(desc = "流程保存接口")
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ProcessVo processVo = JSON.toJavaObject(jsonObj, ProcessVo.class);
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
