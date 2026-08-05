package neatlogic.module.process.api.channeltype;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ChannelTypeVo;
import neatlogic.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import neatlogic.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.ProcessTaskSerialNumberPolicyHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSerialNumberMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
@AuthAction(action = PROCESS_BASE.class)
public class ChannelTypeGetApi extends PrivateApiComponentBase {

    @Resource
    private ChannelTypeMapper channelTypeMapper;
    @Resource
    private ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;

    @Override
    public String getToken() {
        return "process/channeltype/get";
    }

    @Override
    public String getName() {
        return "nmpac.channeltypegetapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "nmpac.channeltypegetapi.input.param.desc.uuid")})
    @Output({@Param(name = "Return", explode = ChannelTypeVo.class, desc = "nmpac.channeltypegetapi.output.param.desc.return.name")})
    @Description(desc = "nmpac.channeltypegetapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        ChannelTypeVo channelType = channelTypeMapper.getChannelTypeByUuid(uuid);
        if (channelType == null) {
            throw new ChannelTypeNotFoundException(uuid);
        }
        ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo =
                processTaskSerialNumberMapper.getProcessTaskSerialNumberPolicyByChannelTypeUuid(uuid);
        if (processTaskSerialNumberPolicyVo != null) {
            channelType.setHandler(processTaskSerialNumberPolicyVo.getHandler());
            JSONObject resultObj = (JSONObject) JSON.toJSON(channelType);
            IProcessTaskSerialNumberPolicyHandler handler =
                    ProcessTaskSerialNumberPolicyHandlerFactory.getHandler(processTaskSerialNumberPolicyVo.getHandler());
            if (handler != null) {
                JSONObject config = handler.makeupConfig(processTaskSerialNumberPolicyVo.getConfig());
                resultObj.putAll(config);
            }
            return resultObj;
        }
        return channelType;
    }

}
