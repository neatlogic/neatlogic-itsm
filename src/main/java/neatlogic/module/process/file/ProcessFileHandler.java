/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.process.file;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.file.core.FileTypeHandlerBase;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.process.crossover.ICatalogCrossoverService;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundException;
import neatlogic.module.process.service.ProcessTaskService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
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
    public boolean validDeleteFile(FileVo fileVo) {
        // 根据fileId查找工单信息，如果找不到工单信息，说明工单未上报，当前用户是附件上传者时有删除权限，否则没有。
        List<ProcessTaskVo> processTaskVoList = processTaskMapper.getProcessTaskStepVoListByFileId(fileVo.getId());
        if (CollectionUtils.isEmpty(processTaskVoList)) {
            if (Objects.equals(fileVo.getUserUuid(), UserContext.get().getUserUuid())) {
                return true;
            } else {
                return false;
            }
        }
        return false;
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
