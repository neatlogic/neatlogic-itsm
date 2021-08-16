/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author linbq
 * @since 2021/8/9 11:28
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskListFormApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Override
    public String getToken() {
        return "suzhoubank/processtask/list/form";
    }
    @Override
    public String getName() {
        return "查询工单列表的表单数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskIdList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "工单id列表")
    })
    @Output({
            @Param(explode = ProcessTaskVo[].class, desc = "工单列表表单数据")
    })
    @Description(desc = "查询工单列表的表单数据")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<ProcessTaskVo> resultList = new ArrayList();
        JSONArray processTaskIdArray = paramObj.getJSONArray("processTaskIdList");
        if (CollectionUtils.isEmpty(processTaskIdArray)) {
            return resultList;
        }
        List<Long> processTaskIdList = processTaskIdArray.toJavaList(Long.class);
        List<Long> existsProcessTaskIdList = processTaskMapper.checkProcessTaskIdListIsExists(processTaskIdList);
        if (processTaskIdList.size() > existsProcessTaskIdList.size()) {
            processTaskIdList.removeAll(existsProcessTaskIdList);
            if (CollectionUtils.isNotEmpty(processTaskIdList)) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Long processTaskId : processTaskIdList) {
                    stringBuilder.append(processTaskId);
                    stringBuilder.append("、");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                throw new ProcessTaskNotFoundException(stringBuilder.toString());
            }
        }
        List<Long> existsFormProcessTaskIdList = new ArrayList<>();
        Map<Long, ProcessTaskVo> processTaskMap = new HashMap<>();
        List<ProcessTaskVo> existsProcessTaskVoList = processTaskMapper.getProcessTaskListByIdList(existsProcessTaskIdList);
        for (ProcessTaskVo processTaskVo : existsProcessTaskVoList) {
            processTaskMap.put(processTaskVo.getId(), processTaskVo);
        }
        List<ProcessTaskFormVo> processTaskFormList = processTaskMapper.getProcessTaskFormListByProcessTaskIdList(existsProcessTaskIdList);;
        for (ProcessTaskFormVo processTaskFormVo : processTaskFormList) {
            String formContentHash = processTaskFormVo.getFormContentHash();
            if (StringUtils.isNotBlank(formContentHash)) {
                String formContent = selectContentByHashMapper.getProcessTaskFromContentByHash(formContentHash);
                if (StringUtils.isNotBlank(formContent)) {
                    Long processTaskId = processTaskFormVo.getProcessTaskId();
                    ProcessTaskVo processTaskVo = processTaskMap.get(processTaskId);
//                    ProcessTaskVo processTaskVo = new ProcessTaskVo();
//                    processTaskVo.setId(processTaskId);
                    processTaskVo.setFormConfig(JSONObject.parseObject(formContent));
                    resultList.add(processTaskVo);
//                    processTaskMap.put(processTaskId, processTaskVo);
                    existsFormProcessTaskIdList.add(processTaskId);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(existsFormProcessTaskIdList)) {
            List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskIdList(existsFormProcessTaskIdList);
            for (ProcessTaskFormAttributeDataVo dataVo : processTaskFormAttributeDataList) {
                Long processTaskId = dataVo.getProcessTaskId();
                ProcessTaskVo processTaskVo = processTaskMap.get(processTaskId);
                if (processTaskVo != null) {
                    processTaskVo.getFormAttributeDataMap().put(dataVo.getAttributeUuid(), dataVo.getDataObj());
                }
            }
        }
        return resultList;
    }
}
