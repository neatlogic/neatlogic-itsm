/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.notify.handler.param;

import codedriver.framework.common.constvalue.TeamLevel;
import codedriver.framework.dao.mapper.TeamMapper;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.TeamUserTitleVo;
import codedriver.framework.dto.TeamVo;
import codedriver.framework.dto.UserTitleVo;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.dao.mapper.ProcessTagMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessTaskStepContentVo;
import codedriver.framework.process.dto.ProcessTaskStepTagVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import codedriver.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
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
    private ProcessTagMapper processTagMapper;

    @Resource
    private SelectContentByHashMapper selectContntByHashMapper;

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
        Long tagId = processTagMapper.getProcessTagIdByName("审批");
        if (tagId != null) {
            ProcessTaskStepTagVo processTaskStepTagVo = new ProcessTaskStepTagVo();
            processTaskStepTagVo.setProcessTaskId(processTaskStepVo.getProcessTaskId());
            processTaskStepTagVo.setTagId(tagId);
            List<Long> processTaskStepIdList = processTaskMapper.getProcessTaskStepIdListByProcessTaskIdAndTagId(processTaskStepTagVo);
            if (CollectionUtils.isNotEmpty(processTaskStepIdList)) {
                List<ProcessTaskStepContentVo> processTaskStepContentList = processTaskMapper.getProcessTaskStepContentByProcessTaskStepIdList(processTaskStepIdList);
                if (CollectionUtils.isNotEmpty(processTaskStepContentList)) {
                    List<String> commentList = new ArrayList<>();
                    for (ProcessTaskStepContentVo contentVo : processTaskStepContentList) {
                        String content = selectContntByHashMapper.getProcessTaskContentStringByHash(contentVo.getContentHash());
                        if (StringUtils.isNotBlank(content)) {
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
                                            if (TeamLevel.DEPARTMENT.getValue().equals(teamVo.getLevel())) {
                                                teamName = teamVo.getName();
                                                UserTitleVo userTitleVo = userMapper.getUserTitleById(teamUserTitleVo.getTitleId());
                                                if (userTitleVo != null) {
                                                    titleName = userTitleVo.getName();
                                                }
                                            }
                                        }
                                    }
                                    if (StringUtils.isBlank(teamName)) {
                                        List<TeamVo> teamList =  teamMapper.getTeamListByUserUuid(lcu);
                                        for (TeamVo teamVo : teamList) {
                                            if (TeamLevel.DEPARTMENT.getValue().equals(teamVo.getLevel())) {
                                                teamName = teamVo.getName();
                                                break;
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
                            commentStringBuilder.append("<br>");
                            commentList.add(commentStringBuilder.toString());
                        }
                    }
                    return commentList;
                }
            }
        }
        return null;
    }
}
