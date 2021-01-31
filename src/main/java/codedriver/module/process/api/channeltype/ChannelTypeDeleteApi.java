package codedriver.module.process.api.channeltype;

import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.exception.channeltype.ChannelTypeHasReferenceException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.process.auth.label.CHANNELTYPE_MODIFY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskSerialNumberMapper;
import codedriver.framework.process.exception.channeltype.ChannelTypeNotFoundException;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
@AuthAction(action = CHANNELTYPE_MODIFY.class)
public class ChannelTypeDeleteApi extends PrivateApiComponentBase {

    @Autowired
    private ChannelTypeMapper channelTypeMapper;

    @Autowired
    private ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;

    @Override
    public String getToken() {
        return "process/channeltype/delete";
    }

    @Override
    public String getName() {
        return "服务类型信息删除接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "服务类型uuid")})
    @Output({})
    @Description(desc = "服务类型信息删除接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String uuid = jsonObj.getString("uuid");
        if (channelTypeMapper.checkChannelTypeIsExists(uuid) == 0) {
            throw new ChannelTypeNotFoundException(uuid);
        }
        if (channelTypeMapper.checkChannelTypeHasReference(uuid) > 0) {
            ChannelTypeVo type = channelTypeMapper.getChannelTypeByUuid(uuid);
            throw new ChannelTypeHasReferenceException(type.getName(), "删除");
        }
        channelTypeMapper.deleteChannelTypeByUuid(uuid);
        processTaskSerialNumberMapper.deleteProcessTaskSerialNumberPolicyByChannelTypeUuid(uuid);
        return null;
    }

}
