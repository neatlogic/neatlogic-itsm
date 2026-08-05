package neatlogic.module.process.api.channeltype;

import neatlogic.framework.util.$;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.process.auth.CHANNELTYPE_MODIFY;
import neatlogic.framework.process.dto.ChannelTypeVo;
import neatlogic.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import neatlogic.framework.process.exception.channeltype.ChannelTypeHasReferenceException;
import neatlogic.framework.process.exception.channeltype.ChannelTypeNameRepeatException;
import neatlogic.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import neatlogic.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberPolicyHandlerNotFoundException;
import neatlogic.framework.process.exception.processtaskserialnumberpolicy.ProcessTaskSerialNumberUpdateInProcessException;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.ProcessTaskSerialNumberPolicyHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSerialNumberMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
@AuthAction(action = CHANNELTYPE_MODIFY.class)
public class ChannelTypeSaveApi extends PrivateApiComponentBase {

    @Resource
    private ChannelTypeMapper channelTypeMapper;
    @Resource
    private ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;

    @Override
    public String getToken() {
        return "process/channeltype/save";
    }

    @Override
    public String getName() {
        return "nmpac.channeltypesaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "uuid", type = ApiParamType.STRING, desc = "nmpac.channeltypesaveapi.input.param.desc.uuid"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "nmpac.channeltypesaveapi.input.param.desc.name"),
            @Param(name = "isActive", type = ApiParamType.ENUM, rule = "0,1", isRequired = true, desc = "nmpac.channeltypesaveapi.input.param.desc.isactive"),
            @Param(name = "prefix", type = ApiParamType.STRING, isRequired = true, desc = "nmpac.channeltypesaveapi.input.param.desc.prefix"),
            @Param(name = "handler", type = ApiParamType.STRING, isRequired = true, desc = "nmpac.channeltypesaveapi.input.param.desc.handler"),
            @Param(name = "color", type = ApiParamType.STRING, isRequired = true, desc = "nmpac.channeltypesaveapi.input.param.desc.color"),
            @Param(name = "description", type = ApiParamType.STRING, xss = true, desc = "nmpac.channeltypesaveapi.input.param.desc.description")})
    @Output({@Param(name = "Return", type = ApiParamType.STRING, desc = "nmpac.channeltypesaveapi.output.param.desc.return.name")})
    @Description(desc = "nmpac.channeltypesaveapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        ChannelTypeVo channelTypeVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ChannelTypeVo>() {
        });
        if (channelTypeMapper.checkChannelTypeNameIsRepeat(channelTypeVo) > 0) {
            throw new ChannelTypeNameRepeatException(channelTypeVo.getName());
        }

        Integer sort = channelTypeMapper.getChannelTypeMaxSort();
        if (sort == null) {
            sort = 0;
        }
        sort++;
        channelTypeVo.setSort(sort);
        String uuid = jsonObj.getString("uuid");
        if (uuid != null) {
            if (channelTypeMapper.checkChannelTypeIsExists(uuid) == 0) {
                throw new ChannelTypeNotFoundException(uuid);
            }
            if (channelTypeMapper.checkChannelTypeHasReference(uuid) > 0
                    && Objects.equals(channelTypeVo.getIsActive(), 0)) {
                throw new ChannelTypeHasReferenceException(channelTypeVo.getName(), $.t("nmpac.channeltypesaveapi.operation.disable"));
            }
            channelTypeMapper.updateChannelTypeByUuid(channelTypeVo);
        } else {
            channelTypeMapper.insertChannelType(channelTypeVo);
        }

        IProcessTaskSerialNumberPolicyHandler handler =
                ProcessTaskSerialNumberPolicyHandlerFactory.getHandler(channelTypeVo.getHandler());
        if (handler == null) {
            throw new ProcessTaskSerialNumberPolicyHandlerNotFoundException(channelTypeVo.getHandler());
        }
        JSONObject config = handler.makeupConfig(jsonObj);
        ProcessTaskSerialNumberPolicyVo policy = new ProcessTaskSerialNumberPolicyVo();
        policy.setChannelTypeUuid(channelTypeVo.getUuid());
        policy.setHandler(channelTypeVo.getHandler());
        policy.setConfig(config.toJSONString());
        ProcessTaskSerialNumberPolicyVo oldPolicy = processTaskSerialNumberMapper.getProcessTaskSerialNumberPolicyByChannelTypeUuid(uuid);
        if (oldPolicy != null) {
            if (Objects.equals(oldPolicy.getHandler(), policy.getHandler()) && Objects.equals(oldPolicy.getConfigStr(), config.toJSONString())) {
                return channelTypeVo.getUuid();
            }
            if (oldPolicy.getStartTime() != null && oldPolicy.getEndTime() == null) {
                throw new ProcessTaskSerialNumberUpdateInProcessException();
            }
            processTaskSerialNumberMapper.updateProcessTaskSerialNumberPolicyByChannelTypeUuid(policy);
        } else {
            Long startValue = config.getLong("startValue");
            policy.setSerialNumberSeed(startValue);
            processTaskSerialNumberMapper.insertProcessTaskSerialNumberPolicy(policy);
        }
        return channelTypeVo.getUuid();
    }

    public IValid name() {
        return value -> {
            ChannelTypeVo channelTypeVo = JSON.toJavaObject(value, ChannelTypeVo.class);
            if (channelTypeMapper.checkChannelTypeNameIsRepeat(channelTypeVo) > 0) {
                return new FieldValidResultVo(new ChannelTypeNameRepeatException(channelTypeVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }

}
