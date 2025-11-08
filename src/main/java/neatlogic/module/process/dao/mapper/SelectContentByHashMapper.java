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

package neatlogic.module.process.dao.mapper;

import neatlogic.framework.process.crossover.ISelectContentByHashCrossoverMapper;
import neatlogic.framework.process.dto.ProcessTaskConfigVo;
import neatlogic.framework.process.dto.ProcessTaskContentVo;
import neatlogic.framework.process.dto.ProcessTaskStepConfigVo;

import java.util.List;

public interface SelectContentByHashMapper extends ISelectContentByHashCrossoverMapper {

    String getProcessTaskStepConfigByHash(String hash);

    List<ProcessTaskStepConfigVo> getProcessTaskStepConfigListByHashList(List<String> hashList);

    ProcessTaskContentVo getProcessTaskContentByHash(String hash);

    List<ProcessTaskContentVo> getProcessTaskContentListByHashList(List<String> contentHashList);
    
    String getProcessTaskContentStringByHash(String hash);

    String getProcessTaskFromContentByHash(String hash);

//    String getProcessTaskFromContentByProcessTaskId(Long processTaskId);

//    int getProcessTaskFromContentCountByHash(String hash);

    int checkProcessTaskScoreTempleteConfigIsExists(String hash);
    
    String getProcessTaskScoreTempleteConfigStringIsByHash(String hash);

    String getProcessTaskConfigStringByHash(String configHash);

    List<ProcessTaskConfigVo> getProcessTaskConfigListByHashList(List<String> configHashList);
}
