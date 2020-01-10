package codedriver.framework.process.dao.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;

import codedriver.module.process.dto.ChannelPriorityVo;
import codedriver.module.process.dto.ChannelRoleVo;
import codedriver.module.process.dto.ChannelVo;

public interface ChannelMapper {

	int searchChannelCount(ChannelVo channelVo);

	List<ChannelVo> searchChannelList(ChannelVo channelVo);

	Set<String> searchChannelParentUuidList(ChannelVo channelVo);

	ChannelVo getChannelByUuid(String channelUuid);

	int searchChannelRoleCount(ChannelRoleVo channelRoleVo);
	
	List<ChannelRoleVo> searchChannelRoleList(ChannelRoleVo channelRoleVo);
	
	List<ChannelRoleVo> getChannelRoleListByChannelUuid(String channelUuid);
	
	int getMaxSortByParentUuid(String parentUuid);

	List<ChannelPriorityVo> getChannelPriorityListByChannelUuid(String uuid);
	
	int checkChannelIsExists(String channelUuid);

	int checkChannelNameIsRepeat(ChannelVo channelVo);

	List<ChannelVo> getChannelListForTree(Integer isActive);
	
	int replaceChannelUser(@Param("userId")String userId, @Param("channelUuid")String channelUuid);	

	int replaceChannel(ChannelVo channelVo);

	int replaceChannelRole(ChannelRoleVo channelRole);

	int insertChannelPriority(ChannelPriorityVo channelPriority);
	
	int replaceChannelProcess(@Param("channelUuid")String channelUuid, @Param("processUuid")String processUuid);

	int replaceChannelWorktime(@Param("channelUuid")String channelUuid, @Param("worktimeUuid")String worktimeUuid);
	
	int updateAllNextChannelSortForMove(@Param("sort")Integer sort, @Param("parentUuid")String parentUuid);

	int updateChannelForMove(ChannelVo channelVo);

	int deleteChannelUser(@Param("userId")String userId, @Param("channelUuid")String channelUuid);
	
	int deleteChannelByUuid(String uuid);

	int deleteChannelRole(ChannelRoleVo channelRole);

	int deleteChannelPriorityByChannelUuid(String channelUuid);

	int deleteChannelProcessByChannelUuid(String channelUuid);

	int deleteChannelWorktimeByChannelUuid(String channelUuid);

	int deleteChannelRoleByChannelUuid(String channelUuid);

	int deleteChannelUserByChannelUuid(String channelUuid);
	
}
