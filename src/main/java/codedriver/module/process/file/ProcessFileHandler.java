/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.file;

import codedriver.framework.crossover.CrossoverServiceFactory;
import codedriver.framework.file.core.FileTypeHandlerBase;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.crossover.ICatalogCrossoverService;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.module.process.service.ProcessTaskService;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
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
        List<Long> processTaskIdList = new ArrayList<>();
        Long fileId = fileVo.getId();
        List<ProcessTaskStepVo> processTaskStepVoList = processTaskMapper.getProcessTaskStepVoListByFileId(fileId);
        if (CollectionUtils.isNotEmpty(processTaskStepVoList)) {
            processTaskIdList = processTaskStepVoList.stream().map(ProcessTaskStepVo::getProcessTaskId).collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(processTaskIdList)) {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskById(processTaskIdList.get(0));
            ICatalogCrossoverService iCatalogCrossoverService = CrossoverServiceFactory.getApi(ICatalogCrossoverService.class);
            if (iCatalogCrossoverService.channelIsAuthority(processTaskVo.getChannelUuid(), userUuid)) {
                return true;
            }
            return processTaskService.getProcessFileHasDownloadAuthWithFileIdAndProcessTaskIdList(fileVo.getId(), processTaskIdList);
        }
        return true;

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
