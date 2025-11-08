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

package neatlogic.module.process.dao.mapper.workcenter;

import neatlogic.framework.process.workcenter.dto.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WorkcenterMapper {
    List<WorkcenterVo> getAllWorkcenterConditionConfig();

    List<String> getAuthorizedWorkcenterUuidList(
            @Param("workcenter") WorkcenterVo workcenter,
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

    int getCustomWorkcenterCountByOwner(String userUuid);

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
