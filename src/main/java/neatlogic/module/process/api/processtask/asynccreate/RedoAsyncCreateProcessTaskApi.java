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

package neatlogic.module.process.api.processtask.asynccreate;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESSTASK_MODIFY;
import neatlogic.framework.process.dto.ProcessTaskAsyncCreateVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskAsyncCreateMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.ProcessTaskAsyncCreateService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESSTASK_MODIFY.class)
@OperationType(type = OperationTypeEnum.CREATE)
public class RedoAsyncCreateProcessTaskApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskAsyncCreateMapper processTaskAsyncCreateMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskAsyncCreateService processTaskAsyncCreateService;

    @Override
    public String getName() {
        return "nmpap.redoasynccreateprocesstaskapi.getname";
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "id"),
            @Param(name = "serverId", type = ApiParamType.INTEGER, desc = "term.framework.serverid"),
    })
    @Output({
            @Param(name = "processTaskIdList", type = ApiParamType.LONG, desc = "term.itsm.processtaskidlist")
    })
    @Description(desc = "nmpap.redoasynccreateprocesstaskapi.getname")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        List<Long> processTaskIdArray = new ArrayList<>();
        Long id = paramObj.getLong("id");
        Integer serverId = paramObj.getInteger("serverId");
        if (id != null) {
            ProcessTaskAsyncCreateVo processTaskAsyncCreateVo = processTaskAsyncCreateMapper.getProcessTaskAsyncCreateById(id);
            if (processTaskAsyncCreateVo != null
                    && (Objects.equals(processTaskAsyncCreateVo.getStatus(), "redo")
                    || Objects.equals(processTaskAsyncCreateVo.getStatus(), "failed"))
            ) {
                Long processTaskId = processTaskAsyncCreateVo.getProcessTaskId();
                if (processTaskMapper.getProcessTaskById(processTaskId) != null) {
                    return resultObj;
                }
                processTaskIdArray.add(processTaskId);
                processTaskAsyncCreateService.addRedoProcessTaskAsyncCreate(processTaskAsyncCreateVo.getId());
            }
        } else if (serverId != null) {
            List<Long> doneIdList = new ArrayList<>();
            List<Long> redoIdList = new ArrayList<>();
            ProcessTaskAsyncCreateVo searchVo = new ProcessTaskAsyncCreateVo();
            searchVo.setStatus("redo");
            searchVo.setServerId(serverId);
            int rowNum = processTaskAsyncCreateMapper.getProcessTaskAsyncCreateCount(searchVo);
            if (rowNum > 0) {
                searchVo.setRowNum(rowNum);
                searchVo.setPageSize(100);
                Integer pageCount = searchVo.getPageCount();
                for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
                    searchVo.setCurrentPage(currentPage);
                    List<ProcessTaskAsyncCreateVo> list = processTaskAsyncCreateMapper.getProcessTaskAsyncCreateList(searchVo);
                    List<Long> processTaskIdList = list.stream().map(ProcessTaskAsyncCreateVo::getProcessTaskId).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(processTaskIdList)) {
                        processTaskIdList = processTaskMapper.checkProcessTaskIdListIsExists(processTaskIdList);
                    }
                    for (ProcessTaskAsyncCreateVo processTaskAsyncCreateVo : list) {
                        if (processTaskIdList.contains(processTaskAsyncCreateVo.getProcessTaskId())) {
                            doneIdList.add(processTaskAsyncCreateVo.getId());
                        } else {
                            processTaskIdArray.add(processTaskAsyncCreateVo.getProcessTaskId());
                            redoIdList.add(processTaskAsyncCreateVo.getId());
                        }

                    }
                }
            }
            if (CollectionUtils.isNotEmpty(doneIdList)) {
                processTaskAsyncCreateMapper.deleteProcessTaskAsyncCreateByIdList(doneIdList);
            }
            redoIdList.sort(Long::compareTo);
            for (Long redoId : redoIdList) {
                processTaskAsyncCreateService.addRedoProcessTaskAsyncCreate(redoId);
            }
        }
        resultObj.put("processTaskIdList", processTaskIdArray);
        return resultObj;
    }

    @Override
    public String getToken() {
        return "processtask/asynccreate/redo";
    }
}
