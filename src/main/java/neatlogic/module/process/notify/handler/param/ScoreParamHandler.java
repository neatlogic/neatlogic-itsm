/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.notify.handler.param;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dao.mapper.score.ProcessTaskScoreMapper;
import neatlogic.framework.process.dto.ProcessTaskScoreTemplateVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.score.ProcessTaskScoreVo;
import neatlogic.framework.process.dto.score.ScoreTemplateDimensionVo;
import neatlogic.framework.process.notify.constvalue.ProcessTaskNotifyParam;
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
    public Object getMyText(ProcessTaskStepVo processTaskStepVo) {
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
        List<String> resultList = new ArrayList<>();
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
            resultList.add(dimensionName + "：" + score + "分");
        }
        return String.join("、", resultList);
    }
}
