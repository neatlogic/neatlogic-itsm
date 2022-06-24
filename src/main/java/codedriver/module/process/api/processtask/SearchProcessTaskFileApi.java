/*
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.file.dao.mapper.FileMapper;
import codedriver.framework.file.dto.FileVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.framework.form.attribute.handler.UploadHandler;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
            @Param(name = "processTaskId", type = ApiParamType.LONG, desc = "工单ID"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(explode = BasePageVo.class),
            @Param(name = "list", explode = FileVo[].class)
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
        List<FileVo> fileList = fileMapper.getFileDetailListByProcessTaskId(processTaskId);
        List<Long> fileIdList = fileList.stream().map(FileVo::getId).collect(Collectors.toList());
        Set<Long> fileIdSet = new HashSet<>();
        // 表单附件
        List<ProcessTaskFormAttributeDataVo> formDataList = processTaskMapper.getProcessTaskFormAttributeDataListByProcessTaskIdAndFormType(processTaskId, UploadHandler.handler);
        if (formDataList.size() > 0) {
            for (ProcessTaskFormAttributeDataVo dataVo : formDataList) {
                String data = dataVo.getData();
                if (StringUtils.isNotBlank(data)) {
                    try {
                        JSONArray array = JSONArray.parseArray(data);
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            Long id = object.getLong("id");
                            if (!fileIdList.contains(id)) {
                                fileIdSet.add(id);
                            }
                        }
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        }
        if (fileIdSet.size() > 0) {
            fileList.addAll(fileMapper.getFileDetailListByIdList(new ArrayList<>(fileIdSet)));
        }
        fileIdList = fileList.stream().map(FileVo::getId).collect(Collectors.toList());
        // 子任务附件
        List<FileVo> taskFileList = fileMapper.getProcessTaskStepTaskFileListByProcessTaskId(processTaskId);
        if (taskFileList.size() > 0) {
            for (FileVo vo : taskFileList) {
                if (!fileIdList.contains(vo.getId())) {
                    fileList.add(vo);
                }
            }
        }
        if (fileList.size() > 0) {
            if (basePageVo.getNeedPage()) {
                int rowNum = fileList.size();
                int pageCount = PageUtil.getPageCount(rowNum, basePageVo.getPageSize());
                result.put("currentPage", basePageVo.getCurrentPage());
                result.put("pageSize", basePageVo.getPageSize());
                result.put("pageCount", pageCount);
                result.put("rowNum", rowNum);
                int fromIndex = basePageVo.getStartNum();
                if (fromIndex < rowNum) {
                    int toIndex = fromIndex + basePageVo.getPageSize();
                    toIndex = Math.min(toIndex, rowNum);
                    result.put("list", fileList.subList(fromIndex, toIndex));
                } else {
                    result.put("list", new ArrayList<>());
                }
            } else {
                result.put("list", fileList);
            }
        }
        return result;
    }

}
