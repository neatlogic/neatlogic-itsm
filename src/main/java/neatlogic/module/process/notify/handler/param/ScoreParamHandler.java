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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.notify.core.INotifyTriggerType;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dao.mapper.score.ProcessTaskScoreMapper;
import neatlogic.framework.process.dto.ProcessTaskScoreTemplateVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.score.ProcessTaskScoreVo;
import neatlogic.framework.process.dto.score.ScoreTemplateDimensionVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyTriggerType;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyParamHandlerBase;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ScoreParamHandler extends ProcessTaskNotifyParamHandlerBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskScoreMapper processTaskScoreMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Override
    public String getValue() {
        return ProcessTaskNotifyParam.PROCESS_TASK_SCORE.getValue();
    }

    @Override
    public Object getMyText(ProcessTaskStepVo processTaskStepVo, INotifyTriggerType notifyTriggerType) {
        if (!(notifyTriggerType == ProcessTaskNotifyTriggerType.SCOREPROCESSTASK)) {
            return null;
        }
        Long processTaskId = processTaskStepVo.getProcessTaskId();
        ProcessTaskScoreTemplateVo processTaskScoreTemplateVo = processTaskMapper.getProcessTaskScoreTemplateByProcessTaskId(processTaskId);
        if (processTaskScoreTemplateVo == null) {
            return null;
        }
        String configStr = selectContentByHashMapper.getProcessTaskScoreTempleteConfigStringIsByHash(processTaskScoreTemplateVo.getConfigHash());
        if (StringUtils.isBlank(configStr)) {
            return null;
        }
        JSONObject config = JSONObject.parseObject(configStr);
        if (MapUtils.isEmpty(config)) {
            return null;
        }
        JSONArray scoreTemplateDimensionArray = config.getJSONArray("scoreTemplateDimensionList");
        if (CollectionUtils.isEmpty(scoreTemplateDimensionArray)) {
            return null;
        }
        JSONArray resultArray = new JSONArray();
        List<ScoreTemplateDimensionVo> scoreTemplateDimensionList = scoreTemplateDimensionArray.toJavaList(ScoreTemplateDimensionVo.class);
        Map<Long, String> dimensionNameMap = scoreTemplateDimensionList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e.getName()));
        List<ProcessTaskScoreVo> processTaskScoreList = processTaskScoreMapper.getProcessTaskScoreByProcesstaskId(processTaskId);
        for (ProcessTaskScoreVo processTaskScoreVo : processTaskScoreList) {
            String dimensionName = dimensionNameMap.get(processTaskScoreVo.getScoreDimensionId());
            if (StringUtils.isBlank(dimensionName)) {
                continue;
            }
            Integer score = processTaskScoreVo.getScore();
            if (score == null) {
                continue;
            }
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("dimensionName", dimensionName);
            jsonObj.put("score", score);
            resultArray.add(jsonObj);
        }
        return resultArray;
    }
}
