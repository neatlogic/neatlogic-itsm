package codedriver.module.process.api.channel;

import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.CatalogMapper;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dto.CatalogVo;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.exception.catalog.CatalogIllegalParameterException;
import codedriver.framework.process.exception.catalog.CatalogNotFoundException;
import codedriver.framework.process.exception.channel.ChannelIllegalParameterException;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class ChannelMoveApi extends PrivateApiComponentBase {

	@Autowired
	private ChannelMapper channelMapper;
	
	@Autowired
	private CatalogMapper catalogMapper;
	
	@Override
	public String getToken() {
		return "process/channel/move";
	}

	@Override
	public String getName() {
		return "服务通道移动位置接口";
	}

	@Override
	public String getConfig() {
		return null;
	}
	@Input({
		@Param(name = "uuid", type = ApiParamType.STRING, isRequired = true, desc = "被移动的服务通道uuid"),
		@Param(name = "parentUuid", type = ApiParamType.STRING, isRequired = true, desc = "移动后的父级uuid"),
		@Param(name = "targetUuid", type = ApiParamType.STRING, isRequired = true, desc = "目标节点uuid"),
		@Param(name = "moveType", type = ApiParamType.ENUM, rule = "inner,prev,next", isRequired = true, desc = "移动类型")
	})
	@Description(desc = "服务通道移动位置接口")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		String uuid = jsonObj.getString("uuid");
		//目标节点uuid
		String targetUuid = jsonObj.getString("targetUuid");
		if(uuid.equals(targetUuid)) {
			throw new ChannelIllegalParameterException("被移动通道uuid与目标通道targetUuid不能相同");
		}
		ChannelVo moveChannel = channelMapper.getChannelByUuid(uuid);
		//判断被移动的服务通道是否存在
		if(moveChannel == null) {
			throw new ChannelNotFoundException(uuid);
		}
		String parentUuid = jsonObj.getString("parentUuid");
		//判断移动后的父级服务目录是否存在
		if(catalogMapper.checkCatalogIsExists(parentUuid) == 0) {
			throw new CatalogNotFoundException(parentUuid);
		}
		Integer targetSort;
		Integer newSort;
		//通道只能移动到通道前面，不能移动到目录前面
		//通道只能移动到通道后面，不能移动到目录后面
		//通道只能移进目录里面，不能移进通道里面
		//所以目标节点可能是目录或通道
		
		if(parentUuid.equals(targetUuid)) {
			if(parentUuid.equals(moveChannel.getParentUuid())) {
				throw new ChannelIllegalParameterException("服务通道：'" + uuid + "'已经是服务目录：'" + parentUuid + "'的子通道，不能进行移入操作");
			}
			//移进目标节点里面
			targetSort = channelMapper.getMaxSortByParentUuid(parentUuid);
		}else {
			//移动到目标节点后面
			ChannelVo targetChannel = channelMapper.getChannelByUuid(targetUuid);
			//判断目标服务通道是否存在
			if(targetChannel == null) {
				throw new ChannelNotFoundException(targetUuid);
			}
			if(!parentUuid.equals(targetChannel.getParentUuid())) {
				throw new ChannelIllegalParameterException("服务通道：'" + targetUuid + "'不是服务目录：'" + parentUuid + "'的子通道");
			}
			targetSort = targetChannel.getSort();
		}

		Integer oldSort = moveChannel.getSort();
		String moveType = jsonObj.getString("moveType");
		//判断是否是在相同目录下移动
		if(parentUuid.equals(moveChannel.getParentUuid())) {
			//相同目录下移动
			if(oldSort.compareTo(targetSort) == -1) {//往后移动, 移动前后两个位置直接的服务通道序号减一
				if("prev".equals(moveType)) {
					newSort = targetSort - 1;
				}else{
					newSort = targetSort;
				}
				channelMapper.updateSortDecrement(parentUuid, oldSort + 1, newSort);
			}else {//往前移动, 移动前后两个位置直接的服务通道序号加一
				if("prev".equals(moveType)) {
					newSort = targetSort;
				}else{
					newSort = targetSort + 1;
				}
				channelMapper.updateSortIncrement(parentUuid, newSort, oldSort - 1);
			}
		}else {
			//不同同目录下移动
			if("prev".equals(moveType)) {
				newSort = targetSort;
			}else{
				newSort = targetSort + 1;
			}
			//旧目录，被移动目录后面的兄弟节点序号减一
			channelMapper.updateSortDecrement(moveChannel.getParentUuid(), oldSort + 1, null);
			//新目录，目标目录后面的兄弟节点序号加一
			channelMapper.updateSortIncrement(parentUuid, newSort, null);
		}

		moveChannel.setSort(newSort);
		moveChannel.setParentUuid(parentUuid);
		channelMapper.updateChannelForMove(moveChannel);
		
		//判断移动后是否会脱离根目录
		String parentUuidTemp = parentUuid;
		while(!CatalogVo.ROOT_UUID.equals(parentUuidTemp)) {
			CatalogVo parent = catalogMapper.getCatalogByUuid(parentUuidTemp);
			if(parent == null) {
				throw new CatalogIllegalParameterException("将服务目录：" + uuid + "的parentUuid设置为：" + parentUuid + "会导致该目录脱离根目录");
			}
			parentUuidTemp = parent.getParentUuid();
			if(parentUuid.equals(parentUuidTemp)) {
				throw new CatalogIllegalParameterException("将服务目录：" + uuid + "的parentUuid设置为：" + parentUuid + "会导致该目录脱离根目录");
			}
		}
		return null;
	}

}
