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

package neatlogic.module.process.notify.handler.param;

import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.*;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dto.ProcessTaskContentVo;
import neatlogic.framework.process.dto.ProcessTaskStepContentVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyTriggerType;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.module.process.notify.constvalue.SlaNotifyTriggerType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class StepCommentListParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public String getValue() {
        return ProcessTaskStepNotifyParam.STEP_COMMENT_LIST.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        if (!(notifyTriggerType instanceof ProcessTaskStepNotifyTriggerType) && !(notifyTriggerType instanceof SlaNotifyTriggerType)) {
            return null;
        }
        List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepId(processTaskStepVo.getId());
        if (CollectionUtils.isEmpty(processTaskStepContentList)) {
            return null;
        }
        List<String> commentList = new ArrayList<>();
        Set<String> contentHashSet = processTaskStepContentList.stream().filter(e -> e.getContentHash() != null).map(ProcessTaskStepContentVo::getContentHash).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(contentHashSet)) {
            return null;
        }
        Map<String, UserVo> userMap = new HashMap<>();
        Set<String> userUuidSet = processTaskStepContentList.stream().filter(e -> StringUtils.isNotBlank(e.getLcu())).map(ProcessTaskStepContentVo::getLcu).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(userUuidSet)) {
            List<UserVo> userList = userMapper.getUserByUserUuidList(new ArrayList<>(userUuidSet));
            userMap = userList.stream().collect(Collectors.toMap(e -> e.getUuid(), e -> e));
        }
        List<ProcessTaskContentVo> processTaskContentList = selectContentByHashMapper.getProcessTaskContentListByHashList(new ArrayList<>(contentHashSet));
        Map<String, String> hashToContentMap = processTaskContentList.stream().collect(Collectors.toMap(ProcessTaskContentVo::getHash, ProcessTaskContentVo::getContent));
        for (ProcessTaskStepContentVo contentVo : processTaskStepContentList) {
            String content = hashToContentMap.get(contentVo.getContentHash());
            if (StringUtils.isBlank(content)) {
                continue;
            }
            content= processContent(content);
            if (StringUtils.isBlank(content)) {
                continue;
            }
            StringBuilder commentStringBuilder = new StringBuilder();
            commentStringBuilder.append("<p>");
            commentStringBuilder.append(content);
            commentStringBuilder.append("<br>");
            String lcu = contentVo.getLcu();
            if (StringUtils.isNotBlank(lcu)) {
                UserVo userVo = userMap.get(lcu);
                if (userVo != null) {
                    commentStringBuilder.append(userVo.getUserName());
                    commentStringBuilder.append("(");
                    commentStringBuilder.append(userVo.getUserId());
                    commentStringBuilder.append(")");
                }
                commentStringBuilder.append(" ");
            }
            commentStringBuilder.append(" ");
            Date lcd = contentVo.getLcd();
            if (lcd != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                commentStringBuilder.append(sdf.format(contentVo.getLcd()));
            }
            commentStringBuilder.append("</p>");
            commentList.add(commentStringBuilder.toString());
        }
        return commentList;
    }
}
