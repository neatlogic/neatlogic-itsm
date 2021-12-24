/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.file;

import codedriver.framework.file.core.FileTypeHandlerBase;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ChannelVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channel.ChannelNotFoundException;
import codedriver.framework.process.exception.file.ProcessTaskFileDownloadException;
import codedriver.framework.process.operationauth.core.ProcessAuthManager;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ProcessFileHandler extends FileTypeHandlerBase {
    @Resource
    ProcessTaskMapper processTaskMapper;
    @Resource
    ChannelMapper channelMapper;

    @Override
    public boolean valid(String userUuid, FileVo fileVo, JSONObject jsonObj) {
        /*
         * 1、根据fileId 反找工单id
         * 2、校验该用户是否工单干系人或拥有该工单服务的上报权限，满足才允许下载
         */
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepByFileId(fileVo.getId());
        if(processTaskStepVo == null){
            throw new ProcessTaskFileDownloadException(fileVo.getId());
        }
        if (!new ProcessAuthManager.TaskOperationChecker(processTaskStepVo.getProcessTaskId(), ProcessTaskOperationType.PROCESSTASK_VIEW).build().check()) {
            ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskStepVo.getProcessTaskId());
            ChannelVo channelVo = channelMapper.getChannelByUuid(processTaskVo.getChannelUuid());
            if (channelVo == null) {
                throw new ChannelNotFoundException(processTaskVo.getChannelUuid());
            }
            throw new ProcessTaskFileDownloadException(fileVo.getId(),channelVo.getName());
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
