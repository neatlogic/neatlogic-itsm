/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.file;

import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.file.core.FileTypeHandlerBase;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.process.crossover.ICatalogCrossoverService;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundException;
import neatlogic.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProcessFileHandler extends FileTypeHandlerBase {

    @Resource
    ProcessTaskMapper processTaskMapper;

    @Resource
    ProcessTaskService processTaskService;

    @Override
    public boolean valid(String userUuid, FileVo fileVo, JSONObject jsonObj) {
        Long fileId = fileVo.getId();
        List<ProcessTaskVo> processTaskVoList = processTaskMapper.getProcessTaskStepVoListByFileId(fileId);
        if (CollectionUtils.isNotEmpty(processTaskVoList)) {
            ICatalogCrossoverService iCatalogCrossoverService = CrossoverServiceFactory.getApi(ICatalogCrossoverService.class);
            if (iCatalogCrossoverService.channelIsAuthority(processTaskVoList.get(0).getChannelUuid(), userUuid)) {
                return true;
            }
            return processTaskService.getProcessFileHasDownloadAuthWithFileIdAndProcessTaskIdList(fileVo.getId(), processTaskVoList.stream().map(ProcessTaskVo::getId).collect(Collectors.toList()));
        }
        throw new ProcessTaskNotFoundException();
    }

    @Override
    public String getDisplayName() {
        return "IT服务附件";
    }


    @Override
    public String getName() {
        return "ITSM";
    }

    @Override
    protected boolean myDeleteFile(FileVo fileVo, JSONObject paramObj) {
        return true;
    }
}
