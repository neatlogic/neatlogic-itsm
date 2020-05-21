package codedriver.module.process.api.channel;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.dto.AuthorityVo;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.dto.ChannelPriorityVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ITree;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.process.exception.channel.ChannelIllegalParameterException;
import codedriver.framework.process.exception.channel.ChannelNameRepeatException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;

@Service
@Transactional
public class ChannelSaveApi extends ApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private CatalogMapper catalogMapper;
	
	@Autowired
	private ProcessMapper processMapper;
	
	@Autowired
	private PriorityMapper priorityMapper;
	
	@Autowired
	private WorktimeMapper worktimeMapper;
	
	@Override
	public String getToken() {
		return "process/channel/save";
	}

	@Override
	public String getName() {
		return "服务通道保存信息接口";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, desc = "服务通道uuid"),
		@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", isRequired= true, maxLength = 50, desc = "服务通道名称"),
		@Param(name = "parentUuid", type = ApiParamType.STRING, isRequired = true, desc = "父级uuid"),
		@Param(name = "processUuid", type = ApiParamType.STRING, isRequired = true, desc = "工作流uuid"),
		@Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, desc = "是否激活", rule = "0,1"),
		@Param(name = "worktimeUuid", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口uuid"),
		@Param(name = "desc", type = ApiParamType.STRING, desc = "服务说明", maxLength = 200, xss = true),
		@Param(name = "icon", type = ApiParamType.STRING, desc = "图标"),
		@Param(name = "color", type = ApiParamType.STRING, desc = "颜色"),
		@Param(name = "sla", type = ApiParamType.INTEGER, desc = "时效(单位：小时)"),
		@Param(name = "allowDesc", type = ApiParamType.ENUM, desc = "是否显示上报页描述", rule = "0,1"),
		@Param(name = "isActiveHelp", type = ApiParamType.ENUM, desc = "是否激活描述", rule = "0,1"),
		@Param(name = "help", type = ApiParamType.STRING, desc = "描述帮助"),
		@Param(name = "defaultPriorityUuid", type = ApiParamType.STRING, isRequired = true, desc = "默认优先级uuid"),
		@Param(name = "priorityUuidList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "关联优先级列表"),
		@Param(name = "priorityUuidList[0]", type = ApiParamType.STRING, isRequired = false, desc = "优先级uuid"),
		@Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "授权对象，可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]"),
		@Param(name = "channelTypeUuid", type = ApiParamType.STRING, desc = "服务类型uuid")
		})
	@Output({
		@Param(name = "Return", type = ApiParamType.STRING, desc = "服务通道uuid")
	})
	@Description(desc = "服务通道保存信息接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		ChannelVo channelVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<ChannelVo>() {});
		//获取父级信息
		String parentUuid = channelVo.getParentUuid();
		if(ITree.ROOT_UUID.equals(parentUuid)) {
			throw new ChannelIllegalParameterException("不能在根目录下创建通道，parentUuid=" + parentUuid);
		}
		if(catalogMapper.checkCatalogIsExists(parentUuid) == 0) {
			throw new CatalogNotFoundException(parentUuid);
		}
		if(channelMapper.checkChannelNameIsRepeat(channelVo) > 0) {
			throw new ChannelNameRepeatException(channelVo.getName());
		}
		int sort;
		String uuid = channelVo.getUuid();
		ChannelVo existedChannel = channelMapper.getChannelByUuid(uuid);
		if(existedChannel == null) {//新增
			channelVo.setUuid(null);
			uuid = channelVo.getUuid();
			sort = channelMapper.getMaxSortByParentUuid(parentUuid) + 1;
		}else {//修改
			channelMapper.deleteChannelPriorityByChannelUuid(uuid);
			channelMapper.deleteChannelAuthorityByChannelUuid(uuid);
			sort = existedChannel.getSort();
		}
		channelVo.setSort(sort);
		channelMapper.replaceChannel(channelVo);
		if(processMapper.checkProcessIsExists(channelVo.getProcessUuid()) == 0) {
			throw new ChannelIllegalParameterException("流程图：'" + channelVo.getProcessUuid() + "'不存在");
		}
		channelMapper.replaceChannelProcess(uuid, channelVo.getProcessUuid());

		if(worktimeMapper.checkWorktimeIsExists(channelVo.getWorktimeUuid()) == 0) {
			throw new ChannelIllegalParameterException("工作时间窗口：'" + channelVo.getWorktimeUuid() + "'不存在");
		}
		channelMapper.replaceChannelWorktime(uuid, channelVo.getWorktimeUuid());
		String defaultPriorityUuid = channelVo.getDefaultPriorityUuid();
		List<String> priorityUuidList = channelVo.getPriorityUuidList();
		for(String priorityUuid : priorityUuidList) {
			if(priorityMapper.checkPriorityIsExists(priorityUuid) == 0) {
				throw new ChannelIllegalParameterException("优先级：'" + priorityUuid + "'不存在");
			}
			ChannelPriorityVo channelPriority = new ChannelPriorityVo();
			channelPriority.setChannelUuid(uuid);
			channelPriority.setPriorityUuid(priorityUuid);
			if(defaultPriorityUuid.equals(priorityUuid)) {
				channelPriority.setIsDefault(1);
			}else {
				channelPriority.setIsDefault(0);
			}
			channelMapper.insertChannelPriority(channelPriority);
		}
		List<AuthorityVo> authorityList = channelVo.getAuthorityVoList();
		if(CollectionUtils.isNotEmpty(authorityList)) {
			for(AuthorityVo authorityVo : authorityList) {
				channelMapper.insertChannelAuthority(authorityVo,channelVo.getUuid());
			}
		}
		return uuid;
	}

}
