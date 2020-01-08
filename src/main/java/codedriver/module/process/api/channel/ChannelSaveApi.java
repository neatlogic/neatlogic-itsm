package codedriver.module.process.api.channel;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.apiparam.core.ApiParamType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessMapper;
import codedriver.framework.process.dao.mapper.WorktimeMapper;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.process.exception.channel.ChannelIllegalParameterException;
import codedriver.framework.process.exception.channel.ChannelNameRepeatException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.ApiComponentBase;
import codedriver.module.process.dto.ChannelPriorityVo;
import codedriver.module.process.dto.ChannelVo;
import codedriver.module.process.dto.ITree;

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
		@Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "服务通道名称", length = 30, xss = true),
		@Param(name = "parentUuid", type = ApiParamType.STRING, isRequired = true, desc = "父级uuid"),
		@Param(name = "processUuid", type = ApiParamType.STRING, isRequired = true, desc = "工作流uuid"),
		@Param(name = "isActive", type = ApiParamType.ENUM, isRequired = true, desc = "是否激活", rule = "0,1"),
		@Param(name = "worktimeUuid", type = ApiParamType.STRING, isRequired = true, desc = "工作时间窗口uuid"),
		@Param(name = "time", type = ApiParamType.INTEGER, desc = "通道时效"),
		@Param(name = "desc", type = ApiParamType.STRING, desc = "通道说明", length = 200, xss = true),
		@Param(name = "icon", type = ApiParamType.STRING, desc = "图标"),
		@Param(name = "color", type = ApiParamType.STRING, desc = "颜色"),
		@Param(name = "allow_desc", type = ApiParamType.ENUM, isRequired = true, desc = "是否显示上报页描述", rule = "0,1"),
		@Param(name = "defaultPriorityUuid", type = ApiParamType.STRING, isRequired = true, desc = "默认优先级uuid"),
		@Param(name = "priorityUuidList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "关联优先级列表"),
		@Param(name = "priorityUuidList[0]", type = ApiParamType.STRING, isRequired = false, desc = "优先级uuid")
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
			sort = channelMapper.getMaxSortByParentUuid(parentUuid) + 1;
		}else {//修改
			channelMapper.deleteChannelPriorityByChannelUuid(uuid);
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
		return uuid;
	}

}
