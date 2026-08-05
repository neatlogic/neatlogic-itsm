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

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//@Service
@Deprecated
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class UpdateProcessTaskNeedScoreApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;
    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Override
    public String getToken() {
        return "processtask/needscore/update";
    }

    @Override
    public String getName() {
        return "nmpap.updateprocesstaskneedscoreapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "idList", type = ApiParamType.JSONARRAY, desc = "nmpap.updateprocesstaskneedscoreapi.input.param.desc.idlist")
    })
    @Output({})
    @Description(desc = "nmpap.updateprocesstaskneedscoreapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONArray idArray = paramObj.getJSONArray("idList");
        if (CollectionUtils.isNotEmpty(idArray)) {
            List<Long> idList = idArray.toJavaList(Long.class);
            updateNeedScoreField(idList);
        } else {
            int rowNum = processTaskMapper.getAllProcessTaskCount();
            if (rowNum > 0) {
                ProcessTaskVo searchVo = new ProcessTaskVo();
                searchVo.setRowNum(rowNum);
                searchVo.setPageSize(100);
                int pageCount = searchVo.getPageCount();
                for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
                    searchVo.setCurrentPage(currentPage);
                    List<Long> idList = processTaskMapper.getProcessTaskIdList(searchVo);
                    if (CollectionUtils.isNotEmpty(idList)) {
                        updateNeedScoreField(idList);
                    }
                }
            }
        }
        return null;
    }

    private void updateNeedScoreField(List<Long> idList) {
        List<Long> needScoreProcessTaskIdList = new ArrayList<>();
        List<Long> noNeedScoreProcessTaskIdList = new ArrayList<>();
        List<ProcessTaskVo> processTaskList = processTaskMapper.getProcessTaskListByIdList(idList);
        for (ProcessTaskVo processTaskVo : processTaskList) {
            if (processTaskVo.getNeedScore() == null) {
                String config = selectContentByHashMapper.getProcessTaskConfigStringByHash(processTaskVo.getConfigHash());
                Integer isActive = (Integer) JSONPath.read(config, "process.scoreConfig.isActive");
                if (Objects.equals(isActive, 1)) {
                    needScoreProcessTaskIdList.add(processTaskVo.getId());
                } else {
                    noNeedScoreProcessTaskIdList.add(processTaskVo.getId());
                }
            }

        }
        if (CollectionUtils.isNotEmpty(needScoreProcessTaskIdList)) {
            processTaskMapper.updateProcessTaskNeedScoreByIdList(needScoreProcessTaskIdList, 1);
        }
        if (CollectionUtils.isNotEmpty(noNeedScoreProcessTaskIdList)) {
            processTaskMapper.updateProcessTaskNeedScoreByIdList(noNeedScoreProcessTaskIdList, 0);
        }
    }
}
