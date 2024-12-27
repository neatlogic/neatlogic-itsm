/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.process.stephandler.makeup;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.dto.ProcessStepTagVo;
import neatlogic.framework.process.dto.ProcessStepVo;
import neatlogic.framework.process.dto.ProcessTagVo;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IProcessStepMakeupHandler;
import neatlogic.module.process.dao.mapper.process.ProcessMapper;
import neatlogic.module.process.dao.mapper.process.ProcessTagMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
public class TagListMakeupHandler implements IProcessStepMakeupHandler {

    @Resource
    private ProcessMapper processMapper;

    @Resource
    private ProcessTagMapper processTagMapper;
    @Override
    public String getName() {
        return "tagList";
    }

    @Override
    public void makeup(IProcessStepInternalHandler processStepInternalHandler, ProcessStepVo processStepVo, JSONObject stepConfigObj, String action) {
        JSONArray tagList = stepConfigObj.getJSONArray("tagList");
        if (CollectionUtils.isNotEmpty(tagList)) {
            if (Objects.equals(action, "save")) {
                List<String> tagNameList = tagList.toJavaList(String.class);
                if (CollectionUtils.isNotEmpty(tagNameList)) {
                    ProcessStepTagVo processStepTagVo = new ProcessStepTagVo();
                    processStepTagVo.setProcessUuid(processStepVo.getProcessUuid());
                    processStepTagVo.setProcessStepUuid(processStepVo.getUuid());
                    List<ProcessTagVo> processTagList = processTagMapper.getProcessTagByNameList(tagNameList);
                    for (ProcessTagVo processTagVo : processTagList) {
                        processStepTagVo.setTagId(processTagVo.getId());
                        tagNameList.remove(processTagVo.getName());
                        processStepTagVo.setProcessUuid(processStepVo.getProcessUuid());
                        processMapper.insertProcessStepTag(processStepTagVo);
                    }
                    if (CollectionUtils.isNotEmpty(tagNameList)) {
                        for (String tagName : tagNameList) {
                            ProcessTagVo processTagVo = new ProcessTagVo(tagName);
                            processTagMapper.insertProcessTag(processTagVo);
                            processStepTagVo.setTagId(processTagVo.getId());
                            processStepTagVo.setProcessUuid(processStepVo.getProcessUuid());
                            processMapper.insertProcessStepTag(processStepTagVo);
                        }
                    }
                }
            } else if (Objects.equals(action, "delete")) {
                processMapper.deleteProcessStepTagByProcessStepUuid(processStepVo.getUuid());
            }
        }
    }
}
