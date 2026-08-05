package neatlogic.module.process.api.form;

import neatlogic.framework.util.$;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.exception.type.ParamIrregularException;
import neatlogic.framework.form.attribute.core.FormAttributeHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeHandler;
import neatlogic.framework.form.dao.mapper.FormMapper;
import neatlogic.framework.form.dto.AttributeDataVo;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.form.exception.FormActiveVersionNotFoundExcepiton;
import neatlogic.framework.form.exception.FormAttributeNotFoundException;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.process.dto.ProcessFormVo;
import neatlogic.framework.process.dto.ProcessTaskFormVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.channel.ChannelNotFoundException;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class FormAttributeCheckApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ChannelMapper channelMapper;

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private FormMapper formMapper;

    @Override
    public String getToken() {
        return "process/form/attribute/check";
    }

    @Override
    public String getName() {
        return "nmpaf.formattributecheckapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "attributeUuid", type = ApiParamType.STRING, isRequired = true, desc = "nmpaf.formattributecheckapi.input.param.desc.attributeuuid"),
            @Param(name = "data", type = ApiParamType.STRING, isRequired = true, desc = "nmpaf.formattributecheckapi.input.param.desc.data"),
            @Param(name = "config", type = ApiParamType.JSONOBJECT, isRequired = true, desc = "nmpaf.formattributecheckapi.input.param.desc.config")
    })
    @Output({
            @Param(name = "Return", type = ApiParamType.BOOLEAN, desc = "nmpaf.formattributecheckapi.output.param.desc.return.name")
    })
    @Description(desc = "nmpaf.formattributecheckapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject config = jsonObj.getJSONObject("config");
        Long processTaskId = config.getLong("processTaskId");
        String channelUuid = config.getString("channelUuid");
        FormVersionVo formVersionVo = null;
        String worktimeUuid = null;
        if (processTaskId != null) {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskId);
            if (processTaskVo == null) {
                throw new ProcessTaskNotFoundException(processTaskId);
            }
            worktimeUuid = processTaskVo.getWorktimeUuid();
            ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
            if (processTaskFormVo == null || StringUtils.isBlank(processTaskFormVo.getFormContent())) {
                return false;
            }
//			String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(processTaskFormVo.getFormContentHash());
//            if(StringUtils.isBlank(formContent)) {
//				return false;
//            }
            formVersionVo = new FormVersionVo();
            formVersionVo.setFormUuid(processTaskFormVo.getFormUuid());
            formVersionVo.setFormName(processTaskFormVo.getFormName());
            formVersionVo.setFormConfig(JSON.parseObject(processTaskFormVo.getFormContent()));

        } else if (StringUtils.isNotBlank(channelUuid)) {
            ChannelVo channelVo = channelMapper.getChannelByUuid(channelUuid);
            if (channelVo == null) {
                throw new ChannelNotFoundException(channelUuid);
            }
            worktimeUuid = channelMapper.getWorktimeUuidByChannelUuid(channelUuid);
            String processUuid = channelMapper.getProcessUuidByChannelUuid(channelUuid);
            ProcessFormVo processFormVo = processMapper.getProcessFormByProcessUuid(processUuid);
            if (processFormVo == null) {
                return false;
            }
            formVersionVo = formMapper.getActionFormVersionByFormUuid(processFormVo.getFormUuid());
            if (formVersionVo == null) {
                throw new FormActiveVersionNotFoundExcepiton(processFormVo.getFormUuid());
            }
        } else {
            throw new ParamIrregularException("config", $.t("nmpaf.formattributecheckapi.configinvalid"));
        }
        String attributeUuid = jsonObj.getString("attributeUuid");
        String mainSceneUuid = formVersionVo.getFormConfig().getString("uuid");
        formVersionVo.setSceneUuid(mainSceneUuid);
        List<FormAttributeVo> formAttributeList = formVersionVo.getFormAttributeList();
        for (FormAttributeVo formAttribute : formAttributeList) {
            if (attributeUuid.equals(formAttribute.getUuid())) {
                IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(formAttribute.getHandler());
                if (handler != null) {
                    AttributeDataVo attributeDataVo = new AttributeDataVo();
                    attributeDataVo.setAttributeUuid(attributeUuid);
                    attributeDataVo.setData(jsonObj.getString("data"));
                    JSONObject configObj = formAttribute.getConfig();
                    configObj.put("worktimeUuid", worktimeUuid);
                    return handler.valid(attributeDataVo, configObj);
                } else {
//					throw new FormAttributeHandlerNotFoundException(formAttribute.getHandler());
                    return false;
                }
            }
        }
        throw new FormAttributeNotFoundException(attributeUuid);
    }

}
