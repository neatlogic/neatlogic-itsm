package codedriver.module.process.service;

import java.text.ParseException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.techsure.multiattrsearch.MultiAttrsObject;
import com.techsure.multiattrsearch.QueryResultSet;
import com.techsure.multiattrsearch.query.QueryResult;

import codedriver.framework.process.workcenter.dto.WorkcenterVo;

public interface WorkcenterService {

    /**
     * 搜索工单
     * 
     * @param workcenterVo
     * @return
     */
    QueryResult searchTask(WorkcenterVo workcenterVo);

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
    Object getStepAction(MultiAttrsObject el) throws ParseException;

    /**
     * 根据关键字获取所有过滤选项
     * 
     * @param keyword
     * @return
     */
    JSONArray getKeywordOptions(String keyword, Integer pageSize);

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

}
