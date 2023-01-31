/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package neatlogic.module.process.api.processtask;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.SelectContentByHashMapper;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
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
        return "批量更新工单的needScore字段值";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "idList", type = ApiParamType.JSONARRAY, desc = "工单ID列表")
    })
    @Output({})
    @Description(desc = "批量更新工单的needScore字段值")
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
