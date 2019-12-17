package codedriver.framework.process.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import codedriver.module.process.dto.ChannelVo;

public interface ChannelMapper {

	int searchChannelCount(ChannelVo channelVo);

	List<ChannelVo> searchChannelList(ChannelVo channelVo);

	ChannelVo getChannelByUuid(String channelUuid);

	int replaceChannelUser(@Param("userId")String userId, @Param("channelUuid")String channelUuid);

	int deleteChannelUser(@Param("userId")String userId, @Param("channelUuid")String channelUuid);

	int insertChannel(ChannelVo channelVo);

}
