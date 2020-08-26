package codedriver.module.process.api.processtask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessStepType;
import codedriver.framework.process.constvalue.ProcessTaskStepAction;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepCommentVo;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepFileVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.core.ApiComponentBase;
@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class processTaskStepContentFileDataConversion extends ApiComponentBase {
    
    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "processtask/step/content/file/data/conversion";
    }

    @Override
    public String getName() {
        return "工单步骤回复数据转换";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        List<Long> processTaskIdList = processTaskMapper.getProcessTaskIdList();
        for(Long processTaskId : processTaskIdList) {
            Map<Long, String> stepType = new HashMap<>();
            List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepBaseInfoByProcessTaskId(processTaskId);
            for(ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
                stepType.put(processTaskStepVo.getId(), processTaskStepVo.getType());
            }
            List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentProcessTaskId(processTaskId);
            for(ProcessTaskStepContentVo processTaskStepContentVo : processTaskStepContentList) {
                Long processTaskStepId = processTaskStepContentVo.getProcessTaskStepId();
                String type = stepType.get(processTaskStepId);
                if(StringUtils.isNotBlank(processTaskStepContentVo.getType())) {
                    if(ProcessStepType.START.getValue().equals(type)) {
                        processTaskStepContentVo.setType(ProcessTaskStepAction.STARTPROCESS.getValue());
                    }else {
                        processTaskStepContentVo.setType(ProcessTaskStepAction.COMPLETE.getValue());
                    }
                    processTaskMapper.updateProcessTaskStepContentType(processTaskStepContentVo);
                }
                processTaskMapper.updateProcessTaskStepFileContentId(processTaskStepId, processTaskStepContentVo.getId());
            }
        }
        List<ProcessTaskStepCommentVo> processTaskStepCommentList = processTaskMapper.getAllProcessTaskStepComment();
        for(ProcessTaskStepCommentVo processTaskStepCommentVo : processTaskStepCommentList) {
            parseProcessTaskStepComment(processTaskStepCommentVo);
            ProcessTaskStepContentVo processTaskStepContent = new ProcessTaskStepContentVo();
            processTaskStepContent.setProcessTaskId(processTaskStepCommentVo.getProcessTaskId());
            processTaskStepContent.setProcessTaskStepId(processTaskStepCommentVo.getProcessTaskStepId());
            processTaskStepContent.setContentHash(processTaskStepCommentVo.getContentHash());
            processTaskStepContent.setType(ProcessTaskStepAction.COMMENT.getValue());
            processTaskStepContent.setFcd(processTaskStepCommentVo.getFcd());
            processTaskStepContent.setFcu(processTaskStepCommentVo.getFcu());
            processTaskStepContent.setLcd(processTaskStepCommentVo.getLcd());
            processTaskStepContent.setLcu(processTaskStepCommentVo.getLcu());
            processTaskMapper.insertProcessTaskStepContent2(processTaskStepContent);
            for(Long fileId : processTaskStepCommentVo.getFileIdList()) {
                processTaskMapper.insertProcessTaskStepFile(new ProcessTaskStepFileVo(processTaskStepCommentVo.getProcessTaskId(), processTaskStepCommentVo.getProcessTaskStepId(), fileId, processTaskStepContent.getId()));
            }
        }
        return null;
    }

    public void parseProcessTaskStepComment(ProcessTaskStepCommentVo processTaskStepComment) {
        if(StringUtils.isNotBlank(processTaskStepComment.getFileIdListHash())) {
            String fileIdListString = processTaskMapper.getProcessTaskContentStringByHash(processTaskStepComment.getFileIdListHash());
            if(StringUtils.isNotBlank(fileIdListString)) {
                List<Long> fileIdList = JSON.parseArray(fileIdListString, Long.class);
                if(CollectionUtils.isNotEmpty(fileIdList)) {
                    processTaskStepComment.setFileIdList(fileIdList);
                }
            }
        }
    }

}
