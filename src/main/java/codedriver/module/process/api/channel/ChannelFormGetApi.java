package codedriver.module.process.api.channel;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.form.constvalue.FormConditionModel;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVo;
import codedriver.framework.process.dto.ProcessVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.form.exception.FormIllegalParameterException;
import codedriver.framework.form.exception.FormNotFoundException;
import codedriver.framework.process.exception.process.ProcessNotFoundException;
import codedriver.framework.form.attribute.core.FormAttributeHandlerFactory;
import codedriver.framework.form.attribute.core.IFormAttributeHandler;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ChannelFormGetApi extends PrivateApiComponentBase {

    @Autowired
    private FormMapper formMapper;

    @Autowired
    private ProcessMapper processMapper;

    @Autowired
    private ChannelMapper channelMapper;

    @Override
    public String getToken() {
        return "process/channel/form/get";
    }

    @Override
    public String getName() {
        return "服务绑定的表单属性信息获取接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "channelUuidList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "服务uuidList"),
        @Param(name = "conditionModel", type = ApiParamType.ENUM, rule = "simple,custom", isRequired = true,
            desc = "条件模型 simple|custom,  simple:目前用于用于工单中心条件过滤简单模式, custom:目前用于用于工单中心条件过自定义模式、条件分流和sla条件;默认custom"),})
    @Output({@Param(name = "Return", explode = FormAttributeVo[].class, desc = "表单属性列表")})
    @Description(desc = "服务绑定的表单属性信息获取接口")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<String> channelUuidList =
            JSONObject.parseArray(jsonObj.getJSONArray("channelUuidList").toJSONString(), String.class);
        List<ChannelVo> channelList = channelMapper.getChannelByUuidList(channelUuidList);
        if (CollectionUtils.isEmpty(channelList)) {
            throw new ChannelNotFoundException(channelList.toString());
        }
        List<FormAttributeVo> allFormAttributeList = new ArrayList<FormAttributeVo>();
        for (ChannelVo channel : channelList) {
            String processUuid = channel.getProcessUuid();
            if (processUuid == null) {
                continue;
            }
            ProcessVo process = processMapper.getProcessByUuid(processUuid);
            if (process == null) {
                throw new ProcessNotFoundException(processUuid);
            }
            String formUuid = process.getFormUuid();
            if (formUuid == null) {
                continue;
            }
            FormVo formVo = formMapper.getFormByUuid(formUuid);
            // 判断表单是否存在
            if (formVo == null) {
                throw new FormNotFoundException(formUuid);
            }
            String conditionModel = jsonObj.getString("conditionModel");
            List<FormAttributeVo> formAttributeList = formMapper.getFormAttributeList(new FormAttributeVo(formUuid));
            ListIterator<FormAttributeVo> formIterator = formAttributeList.listIterator();
            while (formIterator.hasNext()) {
                FormAttributeVo formAttributeVo = formIterator.next();
                IFormAttributeHandler handler = FormAttributeHandlerFactory.getHandler(formAttributeVo.getHandler());
                if (handler == null || !handler.isConditionable()) {
                    formIterator.remove();
                    continue;
                }
                formAttributeVo.setConditionModel(FormConditionModel.getFormConditionModel(conditionModel));
                formAttributeVo.setType("form");
                formAttributeVo.setIsUseFormConfig(handler.isUseFormConfig());
            }
            allFormAttributeList.addAll(formAttributeList);
        }
        return allFormAttributeList;
    }

}
