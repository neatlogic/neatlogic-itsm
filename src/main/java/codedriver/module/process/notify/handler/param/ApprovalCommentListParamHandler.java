/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.notify.handler.param;

import codedriver.framework.process.dao.mapper.ProcessTagMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepReplyVo;
import codedriver.framework.process.dto.ProcessTaskStepTagVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import codedriver.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class ApprovalCommentListParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTagMapper processTagMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Override
    public String getValue() {
        return ProcessTaskNotifyParam.APPROVALCOMMENTLIST.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
        Long tagId = processTagMapper.getProcessTagIdByName("审批");
        if (tagId != null) {
            ProcessTaskStepTagVo processTaskStepTagVo = new ProcessTaskStepTagVo();
            processTaskStepTagVo.setProcessTaskId(processTaskStepVo.getProcessTaskId());
            processTaskStepTagVo.setTagId(tagId);
            List<Long> processTaskStepIdList = processTaskMapper.getProcessTaskStepIdListByProcessTaskIdAndTagId(processTaskStepTagVo);
            if (CollectionUtils.isNotEmpty(processTaskStepIdList)) {
                List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepIdList(processTaskStepIdList);
                if (CollectionUtils.isNotEmpty(processTaskStepContentList)) {
                    List<ProcessTaskStepReplyVo> commentList = new ArrayList<>();
                    for (ProcessTaskStepContentVo contentVo : processTaskStepContentList) {
                        ProcessTaskStepReplyVo comment = new ProcessTaskStepReplyVo(contentVo);
                        comment.setContent(selectContentByHashMapper.getProcessTaskContentStringByHash(contentVo.getContentHash()));
                        commentList.add(comment);
                    }
                    return commentList;
                }
            }
        }
        return null;
    }
}
