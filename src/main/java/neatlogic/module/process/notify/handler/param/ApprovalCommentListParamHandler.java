/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package neatlogic.module.process.notify.handler.param;

import neatlogic.framework.common.constvalue.TeamLevel;
import neatlogic.framework.dao.mapper.TeamMapper;
import neatlogic.framework.dao.mapper.UserMapper;
import neatlogic.framework.dto.*;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dto.ProcessTaskStepContentVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import neatlogic.framework.util.HtmlUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author linbq
 * @since 2021/10/16 15:52
 **/
@Component
public class ApprovalCommentListParamHandler extends ProcessTaskNotifyParamHandlerBase {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private TeamMapper teamMapper;

    @Override
    public String getValue() {
        return ProcessTaskNotifyParam.APPROVALCOMMENTLIST.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
        List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskId(processTaskStepVo.getProcessTaskId());
        if (CollectionUtils.isNotEmpty(processTaskStepContentList)) {
            List<String> commentList = new ArrayList<>();
            for (ProcessTaskStepContentVo contentVo : processTaskStepContentList) {
                String content = selectContentByHashMapper.getProcessTaskContentStringByHash(contentVo.getContentHash());
                if (StringUtils.isNotBlank(content)) {
                    if (content.startsWith("<p>")) {
                        content = content.substring(3);
                    }
                    if (content.endsWith("</p>")) {
                        content = content.substring(0, content.length() - 4);
                    }
                    if (StringUtils.isNotBlank(content)) {
                        List<UrlInfoVo> urlInfoVoList = HtmlUtil.getUrlInfoList(content, "<img src=\"", "\"");
                        content = HtmlUtil.urlReplace(content, urlInfoVoList);
                        StringBuilder commentStringBuilder = new StringBuilder();
                        commentStringBuilder.append("<p>");
                        commentStringBuilder.append(content);
                        commentStringBuilder.append("<br>");
                        String lcu = contentVo.getLcu();
                        if (StringUtils.isNotBlank(lcu)) {
                            UserVo userVo = userMapper.getUserBaseInfoByUuid(lcu);
                            if (userVo != null) {
                                String teamName = null;
                                String titleName = null;
                                List<TeamUserTitleVo> teamUserTitleList = teamMapper.getTeamUserTitleListByUserUuid(lcu);
                                if (CollectionUtils.isNotEmpty(teamUserTitleList)) {
                                    for (TeamUserTitleVo teamUserTitleVo : teamUserTitleList) {
                                        TeamVo teamVo = teamMapper.getTeamByUuid(teamUserTitleVo.getTeamUuid());
                                        if (teamVo != null && TeamLevel.DEPARTMENT.getValue().equals(teamVo.getLevel())) {
                                            teamName = teamVo.getName();
                                            UserTitleVo userTitleVo = userMapper.getUserTitleById(teamUserTitleVo.getTitleId());
                                            if (userTitleVo != null) {
                                                titleName = userTitleVo.getName();
                                            }
                                        }
                                    }
                                }
                                if (StringUtils.isBlank(teamName)) {
                                    List<String> teamUuidList = teamMapper.getTeamUuidListByUserUuid(lcu);
                                    for (String teamUuid : teamUuidList) {
                                        TeamVo teamVo = teamMapper.getTeamByUuid(teamUuid);
                                        if (teamVo != null) {
                                            if (TeamLevel.DEPARTMENT.getValue().equals(teamVo.getLevel())) {
                                                teamName = teamVo.getName();
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (StringUtils.isNotBlank(teamName)) {
                                    commentStringBuilder.append(teamName);
                                    commentStringBuilder.append("/");
                                }
                                if (StringUtils.isNotBlank(titleName)) {
                                    commentStringBuilder.append(titleName);
                                    commentStringBuilder.append("/");
                                }
                                commentStringBuilder.append(userVo.getUserName());
                            }
                            commentStringBuilder.append(" ");
                        }
                        commentStringBuilder.append(" ");
                        Date lcd = contentVo.getLcd();
                        if (lcd != null) {
                            commentStringBuilder.append(sdf.format(contentVo.getLcd()));
                        }
                        commentStringBuilder.append("</p>");
                        commentList.add(commentStringBuilder.toString());
                    }
                }
            }
            return commentList;
        }
        return null;
    }
}
