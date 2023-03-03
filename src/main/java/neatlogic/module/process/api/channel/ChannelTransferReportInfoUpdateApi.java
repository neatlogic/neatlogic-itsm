package neatlogic.module.process.api.channel;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ChannelMapper;
import neatlogic.framework.process.dto.ChannelRelationVo;
import neatlogic.framework.process.dto.ChannelVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class ChannelTransferReportInfoUpdateApi extends PrivateApiComponentBase {
	
	@Autowired
	private ChannelMapper channelMapper;

	@Override
	public String getToken() {
		return "process/channel/transferreportinfo/update";
	}

	@Override
	public String getName() {
		return "服务转报设置信息更新";
	}

	@Override
	public String getConfig() {
		return null;
	}


	@Description(desc = "服务转报设置信息更新")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		List<ChannelVo> channelList = channelMapper.getAllChannelList();
		for (ChannelVo channelVo : channelList) {
			if (getChannelTransferReportInfo(channelVo) == 1) {
				System.out.println(channelVo.getConfig());
				String configStr = channelVo.getConfigStr();
				System.out.println(configStr);
				channelMapper.updateChannelConfig(channelVo);
				ChannelVo channel = channelMapper.getChannelByName(channelVo.getName());
				if (Objects.equals(configStr, channel.getConfigStr())) {
					System.out.println("更新服务(" + channelVo.getName() + "-" +channelVo.getUuid() + ")转报设置信息成功" );
				}
			}
		}
		return null;
	}

	private int getChannelTransferReportInfo(ChannelVo channel) {
		/** 转报设置 **/
		JSONObject config = channel.getConfig();
		if (config != null) {
			JSONArray channelRelationArray = config.getJSONArray("channelRelationList");
			if (CollectionUtils.isNotEmpty(channelRelationArray)) {
				return 0;
			}
		} else {
			config = new JSONObject();
			channel.setConfig(config);
		}

		String uuid = channel.getUuid();
		List<ChannelRelationVo> channelRelationList = channelMapper.getChannelRelationListBySource(uuid);
		if(CollectionUtils.isNotEmpty(channelRelationList)) {
			Map<Long, List<String>> channelRelationTargetMap = new HashMap<>();
			for(ChannelRelationVo channelRelationVo : channelRelationList) {
				channelRelationTargetMap.computeIfAbsent(channelRelationVo.getChannelTypeRelationId(), v ->new ArrayList<>()).add(channelRelationVo.getType() + "#" + channelRelationVo.getTarget());
			}
			Map<Long, List<String>> channelRelationAuthorityMap = new HashMap<>();
			List<ChannelRelationVo> channelRelationAuthorityList = channelMapper.getChannelRelationAuthorityListBySource(uuid);
			for(ChannelRelationVo channelRelationVo : channelRelationAuthorityList) {
				channelRelationAuthorityMap.computeIfAbsent(channelRelationVo.getChannelTypeRelationId(), v ->new ArrayList<>()).add(channelRelationVo.getType() + "#" + channelRelationVo.getUuid());
			}
			Long channelRelationId = null;

			List<ChannelRelationVo> channelRelationVoList = new ArrayList();
			for(ChannelRelationVo channelRelation : channelRelationList) {
				if(channelRelation.getChannelTypeRelationId().equals(channelRelationId)){
					continue;
				}
				channelRelationId = channelRelation.getChannelTypeRelationId();
				ChannelRelationVo channelRelationVo = new ChannelRelationVo();
				channelRelationVo.setChannelTypeRelationId(channelRelationId);
				List<String> targetList = channelRelationTargetMap.get(channelRelationId);
				if(CollectionUtils.isNotEmpty(targetList)){
					channelRelationVo.setTargetList(targetList);
				}
				List<String> authorityList = channelRelationAuthorityMap.get(channelRelationId);
				if(CollectionUtils.isNotEmpty(authorityList)) {
					channelRelationVo.setAuthorityList(authorityList);
				}
				channelRelationVoList.add(channelRelationVo);
			}
			config.put("allowTranferReport", 0);
			if (CollectionUtils.isNotEmpty(channelRelationVoList)) {
				config.put("channelRelationList", channelRelationVoList);
				config.put("allowTranferReport", 1);
				Integer isUsePreOwner = config.getInteger("isUsePreOwner");
				if (isUsePreOwner == null) {
					config.put("isUsePreOwner", 0);
				}
				return 1;
			}
		}
		return 0;
	}
}
