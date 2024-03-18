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

package neatlogic.module.process.api.processtask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.form.constvalue.FormHandler;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskFormAttributeDataVo;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundException;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.service.ProcessTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchProcessTaskFileApi extends PrivateApiComponentBase {

    static Logger logger = LoggerFactory.getLogger(SearchProcessTaskFileApi.class);

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Resource
    private FileMapper fileMapper;

    @Override
    public String getToken() {
        return "processtask/file/search";
    }

    @Override
    public String getName() {
        return "工单附件列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单ID"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "tbodyList", explode = FileVo[].class)
    })
    @Description(desc = "工单附件列表")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject result = new JSONObject();
        BasePageVo basePageVo = JSON.toJavaObject(jsonObj, BasePageVo.class);
        Long processTaskId = jsonObj.getLong("processTaskId");
        if (processTaskMapper.getProcessTaskById(processTaskId) == null) {
            throw new ProcessTaskNotFoundException(processTaskId.toString());
        }
        // 工单上报与步骤附件
        List<FileVo> fileList = processTaskMapper.getFileDetailListByProcessTaskId(processTaskId);
        List<Long> fileIdList = new ArrayList<>();
        // 表单附件
        List<ProcessTaskFormAttributeDataVo> formDataList = processTaskService.getProcessTaskFormAttributeDataListByProcessTaskId(processTaskId);
        if (formDataList.size() > 0) {
            for (ProcessTaskFormAttributeDataVo dataVo : formDataList) {
                if (!Objects.equals(dataVo.getHandler(), FormHandler.FORMUPLOAD.getHandler())) {
                    continue;
                }
                String data = dataVo.getData();
                if (StringUtils.isNotBlank(data)) {
                    try {
                        JSONArray array = JSONArray.parseArray(data);
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            Long id = object.getLong("id");
                            if (id != null) {
                                fileIdList.add(id);
                            }
                        }
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        }
        if (fileIdList.size() > 0) {
            fileList.addAll(fileMapper.getFileDetailListByIdList(fileIdList));
        }
        // 子任务附件
        List<FileVo> taskFileList = processTaskMapper.getProcessTaskStepTaskFileListByProcessTaskId(processTaskId);
        if (taskFileList.size() > 0) {
            fileList.addAll(taskFileList);
        }
        List<Long> stepFileIdList = new ArrayList<>();
        List<ProcessTaskStepVo> processTaskStepList = processTaskMapper.getProcessTaskStepListByProcessTaskId(processTaskId);
        for (ProcessTaskStepVo processTaskStepVo : processTaskStepList) {
            IProcessStepInternalHandler handler = ProcessStepInternalHandlerFactory.getHandler(processTaskStepVo.getHandler());
            if (handler != null) {
                stepFileIdList.addAll(handler.getFileIdList(processTaskStepVo));
            }
        }
        if (CollectionUtils.isNotEmpty(stepFileIdList)) {
            fileList.addAll(fileMapper.getFileDetailListByIdList(stepFileIdList));
        }
        if (fileList.size() > 0) {
            fileList = fileList.stream().sorted(Comparator.comparing(FileVo::getUploadTime, Comparator.nullsLast(Date::compareTo).reversed())).collect(Collectors.toList());
            if (basePageVo.getNeedPage()) {
                basePageVo.setRowNum(fileList.size());
                Integer pageCount = basePageVo.getPageCount();
                result.put("currentPage", basePageVo.getCurrentPage());
                result.put("pageSize", basePageVo.getPageSize());
                result.put("pageCount", pageCount);
                result.put("rowNum", basePageVo.getRowNum());
                int fromIndex = basePageVo.getStartNum();
                if (fromIndex < basePageVo.getRowNum()) {
                    int toIndex = fromIndex + basePageVo.getPageSize();
                    toIndex = Math.min(toIndex, basePageVo.getRowNum());
                    result.put("tbodyList", fileList.subList(fromIndex, toIndex));
                } else {
                    result.put("tbodyList", new ArrayList<>());
                }
            } else {
                result.put("tbodyList", fileList);
            }
        }
        return result;
    }

}
