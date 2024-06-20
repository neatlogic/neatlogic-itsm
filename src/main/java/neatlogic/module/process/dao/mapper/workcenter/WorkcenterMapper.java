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

package neatlogic.module.process.dao.mapper.workcenter;

import neatlogic.framework.process.workcenter.dto.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WorkcenterMapper {
    List<WorkcenterVo> getAllWorkcenterConditionConfig();

    List<String> getAuthorizedWorkcenterUuidList(
            @Param("userUuid") String userUuid,
            @Param("teamUuidList") List<String> teamUuidList,
            @Param("roleUuidList") List<String> roleUuidList,
            @Param("deviceType") String deviceType,
            @Param("isHasModifiedAuth") int isHasModifiedAuth,
            @Param("isHasNewTypeAuth") int isHasNewTypeAuth
    );

    List<WorkcenterVo> getAuthorizedWorkcenterListByUuidList(@Param("uuidList") List<String> uuidList);

    List<WorkcenterCatalogVo> getWorkcenterCatalogListByName(String keyword);

    Integer checkWorkcenterNameIsRepeat(@Param("name") String workcenterName, @Param("uuid") String workcenterUuid);

    WorkcenterCatalogVo getWorkcenterCatalogByName(String name);

    int checkWorkcenterCatalogNameIsRepeats(WorkcenterCatalogVo vo);

    int checkWorkcenterCatalogIsExists(Long id);

    int checkWorkcenterCatalogIsUsed(Long id);

    WorkcenterVo getWorkcenterByUuid(@Param("uuid") String workcenterUuid);

    //Map<String,String> getWorkcenterConditionConfig();

    WorkcenterVo getWorkcenterThead(WorkcenterTheadVo workcenterTheadVo);

    List<WorkcenterVo> getWorkcenterVoListByUuidList(@Param("uuidList") List<String> uuidList);

    List<WorkcenterAuthorityVo> getWorkcenterAuthorityVoListByUuidList(@Param("uuidList") List<String> uuidList);

    WorkcenterUserProfileVo getWorkcenterUserProfileByUserUuid(String userUuid);

    String getWorkcenterTheadConfigByHash(String theadConfigHash);

    Integer deleteWorkcenterUserProfileByUserUuid(String userUuid);

    Integer deleteWorkcenterByUuid(@Param("workcenterUuid") String workcenterUuid);

    Integer deleteWorkcenterAuthorityByUuid(@Param("workcenterUuid") String workcenterUuid);

    Integer deleteWorkcenterOwnerByUuid(@Param("workcenterUuid") String workcenterUuid);

    Integer deleteWorkcenterThead(WorkcenterTheadVo workcenterTheadVo);

    void deleteWorkcenterCatalogById(Long id);

    void insertWorkcenter(WorkcenterVo workcenterVo);

    Integer insertWorkcenterAuthority(WorkcenterAuthorityVo authorityVo);

    Integer insertWorkcenterOwner(@Param("userUuid") String owner, @Param("uuid") String workcenterUuid);

    Integer insertWorkcenterThead(@Param("workcenter") WorkcenterVo workcenterVo, @Param("userUuid") String userUuid);

    Integer insertWorkcenterUserProfile(WorkcenterUserProfileVo workcenterUserProfileVo);

    Integer updateWorkcenter(WorkcenterVo workcenterVo);

    Integer updateWorkcenterCondition(WorkcenterVo workcenterVo);

    Integer insertWorkcenterCatalog(WorkcenterCatalogVo catalogVo);

    Integer insertWorkcenterTheadConfig(@Param("hash") String theadConfigHash, @Param("config") String theadConfigStr);

}
