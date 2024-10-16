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

package neatlogic.module.process.dao.mapper;

import neatlogic.framework.process.crossover.ISelectContentByHashCrossoverMapper;
import neatlogic.framework.process.dto.ProcessTaskConfigVo;
import neatlogic.framework.process.dto.ProcessTaskContentVo;

import java.util.List;

public interface SelectContentByHashMapper extends ISelectContentByHashCrossoverMapper {

    String getProcessTaskStepConfigByHash(String hash);

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
