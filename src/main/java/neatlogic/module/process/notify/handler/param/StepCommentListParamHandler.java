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

package neatlogic.module.process.notify.handler.param;

import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.UserVo;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.dto.ProcessTaskContentVo;
import neatlogic.framework.process.dto.ProcessTaskStepContentVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
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
        if (notifyTriggerType instanceof ProcessTaskNotifyTriggerType) {
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
