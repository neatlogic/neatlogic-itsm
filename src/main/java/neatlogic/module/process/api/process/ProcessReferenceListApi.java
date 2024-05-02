package neatlogic.module.process.api.process;

import java.util.ArrayList;
import java.util.List;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.module.process.dao.mapper.catalog.ChannelMapper;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.framework.process.dto.ChannelTypeVo;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;

import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.service.CatalogService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dto.ChannelVo;
@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessReferenceListApi extends PrivateApiComponentBase {

	@Autowired
	private ProcessMapper processMapper;

	@Autowired
	private ChannelMapper channelMapper;

	@Autowired
	private CatalogService catalogService;

	@Autowired
	private ChannelTypeMapper channelTypeMapper;

	@Override
	public String getToken() {
		return "process/reference/list";
	}

	@Override
	public String getName() {
		return "流程引用列表接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "processUuid", type = ApiParamType.STRING, isRequired = true, desc = "流程uuid")
	})
	@Output({
			@Param(name = "channelList", explode = ChannelVo[].class, desc = "流程引用列表")
	})
	@Description(desc = "流程引用列表接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		resultObj.put("channelList", new ArrayList<>());
		String processUuid = jsonObj.getString("processUuid");
		List<String> channelUuidList = processMapper.getProcessReferenceUuidList(processUuid);
		if(CollectionUtils.isNotEmpty(channelUuidList)){
			List<ChannelVo> channelVoList = channelMapper.getChannelVoByUuidList(channelUuidList);
			for(ChannelVo channelVo : channelVoList){
				ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(channelVo.getChannelTypeUuid());
				channelVo.setChannelTypeVo(channelTypeVo.clone());
				boolean effectiveAuthority = catalogService.channelIsAuthority(channelVo.getUuid(), UserContext.get().getUserUuid(true));
				channelVo.setEffectiveAuthority(effectiveAuthority);
				channelVo.setAllowDesc(null);
				channelVo.setParentUuid(null);
				channelVo.setChannelTypeUuid(null);
				channelVo.setColor(null);
				channelVo.setIcon(null);
				channelVo.setIsActive(null);
			}
			resultObj.put("channelList", channelVoList);
		}
		return resultObj;
	}

}
