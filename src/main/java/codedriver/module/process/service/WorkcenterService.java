package codedriver.module.process.service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.workcenter.dto.WorkcenterTheadVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.QueryResultSet;

import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.WorkcenterVo;

public interface WorkcenterService {

    /**
     * 工单中心根据条件获取工单列表数据
     * 
     * @param workcenterVo
     * @return
     * @throws ParseException 
     */
    JSONObject doSearch(WorkcenterVo workcenterVo) throws ParseException;

    /**
     * 工单中心 获取操作按钮
     * 
     * @param MultiAttrsObject
     *            el
     * @return
     * @throws ParseException 
     */
    Object getStepAction(MultiAttrsObject el,Boolean isHasProcessTaskAuth) throws ParseException;

    /**
     * 工单中心根据条件获取工单列表数据
     * 
     * @param workcenterVo
     * @return
     */
    Integer doSearchCount(WorkcenterVo workcenterVo);

    /**
     * 流式搜索工单
     * 
     * @param workcenterVo
     * @return
     */
    QueryResultSet searchTaskIterate(WorkcenterVo workcenterVo);

    /**
     * 流式分批获取并处理数据
     */
    JSONObject getSearchIterate(QueryResultSet resultSet, WorkcenterVo workcenterVo);

    JSONObject doSearch(Long processtaskId) throws ParseException;

    JSONObject getProcessTaskESObject(ProcessTaskVo processTaskVo);

    List<WorkcenterTheadVo> getWorkcenterTheadList(WorkcenterVo workcenterVo, Map<String, IProcessTaskColumn> columnComponentMap, JSONArray sortColumnList);

}
