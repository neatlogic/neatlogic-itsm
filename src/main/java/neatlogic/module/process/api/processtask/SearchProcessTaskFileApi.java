/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.api.processtask;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.form.constvalue.FormHandler;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dto.ProcessTaskFormAttributeDataVo;
import neatlogic.framework.process.exception.processtask.ProcessTaskNotFoundException;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchProcessTaskFileApi extends PrivateApiComponentBase {

    static Logger logger = LoggerFactory.getLogger(SearchProcessTaskFileApi.class);

    @Resource
    private ProcessTaskMapper processTaskMapper;

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
        List<ProcessTaskFormAttributeDataVo> formDataList = processTaskMapper.getProcessTaskFormAttributeDataListByProcessTaskIdAndFormType(processTaskId, FormHandler.FORMUPLOAD.getHandler());
        if (formDataList.size() > 0) {
            for (ProcessTaskFormAttributeDataVo dataVo : formDataList) {
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
