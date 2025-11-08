/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.dao.mapper.catalog;

import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.process.crossover.IChannelCrossoverMapper;
import neatlogic.framework.process.dto.ChannelPriorityVo;
import neatlogic.framework.process.dto.ChannelRelationVo;
import neatlogic.framework.process.dto.ChannelVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ChannelMapper extends IChannelCrossoverMapper {

    int searchChannelCount(ChannelVo channelVo);

    List<ChannelVo> searchChannelList(ChannelVo channelVo);

    List<ChannelVo> getAllChannelList();

    List<ValueTextVo> searchChannelListForSelect(ChannelVo channelVo);

    ChannelVo getChannelByUuid(String channelUuid);

    ChannelVo getChannelByName(String channelName);

    List<ChannelVo> getChannelByUuidList(@Param("channelUuidList") List<String> channelUuidList);

    List<ChannelVo> getChannelVoByUuidList(List<String> uuidList);

    List<String> getChannelUuidListByParentUuidList(List<String> parentUuidList);

    List<ChannelVo> getAllChannelPriorityList();

    int getMaxSortByParentUuid(String parentUuid);

    List<ChannelPriorityVo> getChannelPriorityListByChannelUuid(String uuid);

    int checkChannelIsExists(String channelUuid);

    int checkChannelNameIsRepeat(ChannelVo channelVo);

    List<ChannelVo> getChannelListForTree(Integer isActive);

    String getProcessUuidByChannelUuid(String channelUuid);

    String getWorktimeUuidByChannelUuid(String channelUuid);

    List<AuthorityVo> getChannelAuthorityListByChannelUuid(String uuid);

    List<String> getAuthorizedChannelUuidList(@Param("userUuid") String userUuid,
                                              @Param("teamUuidList") List<String> teamUuidList,
                                              @Param("roleUuidList") List<String> roleUuidList,
                                              @Param("action") String action,
                                              @Param("channelUuid") String channelUuid);

    List<String> getActiveAuthorizedChannelUuidList(@Param("userUuid") String userUuid,
                                                    @Param("teamUuidList") List<String> teamUuidList,
                                                    @Param("roleUuidList") List<String> roleUuidList,
                                                    @Param("action") String action,
                                                    @Param("channelUuid") String channelUuid);

    List<ChannelVo> getAuthorizedChannelListByParentUuid(@Param("userUuid") String userUuid,
                                                                @Param("teamUuidList") List<String> teamUuidList, @Param("roleUuidList") List<String> roleUuidList,
                                                                @Param("parentUuid") String parentUuid);

    List<String> getAllAncestorNameListByParentUuid(String parentUuid);

    int checkChannelIsFavorite(@Param("userUuid") String userUuid, @Param("channelUuid") String channelUuid);

    List<ChannelVo> getChannelListByChannelTypeUuidList(List<String> channelTypeUuidList);

    List<ChannelVo> getChannelListByChannelUuidList(List<String> channelUuidList);

    List<ChannelRelationVo> getChannelRelationListBySource(String channelUuid);

    List<ChannelRelationVo> getChannelRelationAuthorityListBySource(String channelUuid);

    List<ChannelRelationVo> getChannelRelationTargetList(ChannelRelationVo channelRelationVo);

    List<ChannelVo> getFavoriteChannelList(ChannelVo channelVo);

    FormVersionVo getFormVersionByChannelUuid(String channelUuid);

    List<FormAttributeVo> getFormAttributeByChannelUuid(String channelUuid);

    List<String> getFormUuidListByChannelUuidList(List<String> channelUuidList);

    Integer getChannelRelationIsUsePreOwnerBySourceAndChannelTypeRelationId(ChannelRelationVo channelRelationVo);

    int replaceChannelUser(@Param("userUuid") String userUuid, @Param("channelUuid") String channelUuid);

    int replaceChannel(ChannelVo channelVo);

    int insertChannelPriority(ChannelPriorityVo channelPriority);

    int replaceChannelProcess(@Param("channelUuid") String channelUuid,
                              @Param("processUuid") String processUuid);

    int replaceChannelWorktime(@Param("channelUuid") String channelUuid,
                               @Param("worktimeUuid") String worktimeUuid);

    int insertChannelAuthority(@Param("authorityVo") AuthorityVo authority,
                               @Param("channelUuid") String channelUuid);

    int insertChannelRelation(ChannelRelationVo channelRelationVo);

    int insertChannelRelationAuthority(ChannelRelationVo channelRelationVo);

    int insertChannelRelationIsUsePreOwner(ChannelRelationVo channelRelationVo);

    int updateChannelForMove(ChannelVo channelVo);

    int updateSortIncrement(@Param("parentUuid") String parentUuid, @Param("fromSort") Integer fromSort,
                            @Param("toSort") Integer toSort);

    int updateSortDecrement(@Param("parentUuid") String parentUuid, @Param("fromSort") Integer fromSort,
                            @Param("toSort") Integer toSort);

    int updateChannelConfig(ChannelVo channelVo);

    int deleteChannelUser(@Param("userUuid") String userUuid, @Param("channelUuid") String channelUuid);

    int deleteChannelByUuid(String uuid);

    int deleteChannelPriorityByChannelUuid(String channelUuid);

    int deleteChannelProcessByChannelUuid(String channelUuid);

    int deleteChannelWorktimeByChannelUuid(String channelUuid);

    int deleteChannelUserByChannelUuid(String channelUuid);

    int deleteChannelAuthorityByChannelUuid(String uuid);

    int deleteChannelRelationBySource(String channelUuid);

    int deleteChannelRelationAuthorityBySource(String channelUuid);

    int deleteChannelRelationIsUsePreOwnerBySource(String channelUuid);
}
