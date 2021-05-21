package codedriver.module.process.api.process;

import codedriver.framework.lcs.LCSUtil;
import codedriver.framework.lcs.PrintSingeColorFormatUtil;
import codedriver.framework.lcs.SegmentPair;
import codedriver.framework.lcs.SegmentRange;
import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.exception.process.ProcessStepUtilHandlerNotFoundException;
import codedriver.framework.process.stephandler.core.IProcessStepInternalHandler;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import codedriver.framework.process.util.ProcessConfigUtil;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.process.auth.PROCESS_MODIFY;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.exception.process.ProcessNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = PROCESS_MODIFY.class)
public class ProcessGetApi extends PrivateApiComponentBase {

    @Autowired
    private ProcessMapper processMapper;

    @Override
    public String getToken() {
        return "process/get";
    }

    @Override
    public String getName() {
        return "获取单个流程图数据接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
			@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "流程uuid")
    })
    @Output({
			@Param(explode = ProcessVo.class)
    })
    @Description(desc = "获取单个流程图数据接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        ProcessVo processVo = processMapper.getProcessByUuid(uuid);
        if (processVo == null) {
            throw new ProcessNotFoundException(uuid);
        }
        processVo.setConfig(ProcessConfigUtil.makeupProcessConfig(processVo.getConfig()));
        int count = processMapper.getProcessReferenceCount(uuid);
        processVo.setReferenceCount(count);
        return processVo;
    }
}
