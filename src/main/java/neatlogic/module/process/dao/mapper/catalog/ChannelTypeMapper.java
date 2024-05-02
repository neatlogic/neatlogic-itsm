/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.process.dao.mapper.catalog;

import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.process.crossover.IChannelTypeCrossoverMapper;
import neatlogic.framework.process.dto.ChannelTypeRelationChannelVo;
import neatlogic.framework.process.dto.ChannelTypeRelationVo;
import neatlogic.framework.process.dto.ChannelTypeVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @Title: ChannelTypeMapper
 * @Package neatlogic.framework.process.dao.mapper
 * @Description: TODO
 * @Author: linbq
 * @Date: 2021/1/29 18:24

 **/
public interface ChannelTypeMapper extends IChannelTypeCrossoverMapper {

    int searchChannelTypeCount(ChannelTypeVo channelTypeVo);

    List<ChannelTypeVo> searchChannelTypeList(ChannelTypeVo channelTypeVo);

    List<ValueTextVo> searchChannelTypeListForSelect(ChannelTypeVo channelTypeVo);

    ChannelTypeVo getChannelTypeByUuid(String uuid);

    List<ChannelTypeVo> getChannelTypeByUuidList(List<String> uuidList);

    int checkChannelTypeIsExists(String uuid);

    int checkChannelTypeNameIsRepeat(ChannelTypeVo channelTypeVo);

    Integer getChannelTypeMaxSort();

    int checkChannelTypeRelationIsExists(Long id);

    int checkChannelTypeRelationNameIsRepeat(ChannelTypeRelationVo channelTypeRelationVo);

    ChannelTypeRelationVo getChannelTypeRelationById(Long channelTypeRelationId);

    ChannelTypeRelationVo getChannelTypeRelationLockById(Long channelTypeRelationId);

    List<ChannelTypeRelationVo> getChannelTypeRelationList(ChannelTypeRelationVo channelTypeRelationVo);

    int getChannelTypeRelationCount(ChannelTypeRelationVo channelTypeRelationVo);

    List<ValueTextVo> getChannelTypeRelationListForSelect(ChannelTypeRelationVo channelTypeRelationVo);

    int getChannelTypeRelationCountForSelect(ChannelTypeRelationVo channelTypeRelationVo);

    List<String> getChannelTypeRelationSourceListByChannelTypeRelationId(Long channelTypeRelationId);

    List<String> getChannelTypeRelationTargetListByChannelTypeRelationId(Long channelTypeRelationId);

    List<ChannelTypeRelationChannelVo>
    getChannelTypeRelationSourceListByChannelTypeRelationIdList(List<Long> channelTypeRelationIdList);

    List<ChannelTypeRelationChannelVo> getChannelTypeRelationTargetListByChannelTypeRelationIdList(List<Long> channelTypeRelationIdList);

    List<Long> getChannelTypeRelationIdListBySourceChannelTypeUuid(String sourceChannelTypeUuid);

    List<Long> getAuthorizedChannelTypeRelationIdListBySourceChannelUuid(
            @Param("source") String source,
            @Param("userUuid") String userUuid,
            @Param("teamUuidList") List<String> teamUuidList,
            @Param("roleUuidList") List<String> roleUuidList,
            @Param("processUserTypeList") List<String> processUserTypeList
    );

    List<String> getChannelUuidListByParentUuidListAndChannelTypeUuidList(
            @Param("parentUuidList") List<String> parentUuidList,
            @Param("channelTypeUuidList") List<String> channelTypeUuidList
    );

    int getActiveChannelCountByParentUuidAndChannelTypeUuidList(
            @Param("parentUuid") String parentUuid,
            @Param("channelTypeUuidList") List<String> channelTypeUuidList
    );

    Long checkChannelTypeRelationIsUsedByChannelTypeRelationId(Long channelTypeRelationId);

    Set<String> getChannelTypeRelationReferenceUuidListByChannelTypeRelationId(Long channelTypeRelationId);

    int checkChannelTypeRelationHasReference(Long channelTypeRelationId);

    int checkChannelTypeHasReference(String channelTypeUuid);

    ChannelTypeVo getChannelTypeByName(String name);

    int insertChannelType(ChannelTypeVo channelTypeVo);

    int insertChannelTypeRelation(ChannelTypeRelationVo channelTypeRelationVo);

    int insertChannelTypeRelationSource(
            @Param("channelTypeRelationId") Long channelTypeRelationId,
            @Param("channelTypeUuid") String channelTypeUuid
    );

    int insertChannelTypeRelationTarget(
            @Param("channelTypeRelationId") Long channelTypeRelationId,
            @Param("channelTypeUuid") String channelTypeUuid
    );

    int updateChannelTypeByUuid(ChannelTypeVo channelTypeVo);

    int updateChannelTypeRelationById(ChannelTypeRelationVo channelTypeRelationVo);

    int updateChannelTypeRelationIsActiveById(Long channelTypeRelationId);

    int updateChannelTypeRelationToDeleteById(Long channelTypeRelationId);

    int deleteChannelTypeByUuid(String uuid);

    int deleteChannelTypeRelationById(Long channelTypeRelationId);

    int deleteChannelTypeRelationSourceByChannelTypeRelationId(Long id);

    int deleteChannelTypeRelationTargetByChannelTypeRelationId(Long id);
}
