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

package neatlogic.module.process.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskStepWorkerVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterTheadVo;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface NewWorkcenterService {

    /**
     * @Description: 搜索工单
     * @Author: 89770
     * @Date: 2021/1/19 20:09
     * @Params: [workcenterVo]
     * @Returns: com.alibaba.fastjson.JSONObject
     **/
    JSONObject doSearch(WorkcenterVo workcenterVo);


    /**
     * 根据工单号查询工单信息  目前主要用于工单操作按钮渲染
     *
     * @param processTaskIdList 工单号列表
     * @return json格式工单操作信息
     * @throws ParseException 转换异常
     */
    JSONObject doSearch(List<Long> processTaskIdList) throws ParseException;

    /**
     * @Description: 搜索工单数
     * @Author: 89770
     * @Date: 2021/1/19 20:11
     * @Params: [workcenterVo]
     * @Returns: java.lang.Integer
     **/
    Integer doSearchLimitCount(WorkcenterVo workcenterVo);


    /**
     * @Description: 搜索工单号、标题、内容
     * @Author: 89770
     * @Date: 2021/1/19 20:09
     * @Params: [workcenterVo]
     * @Returns: com.alibaba.fastjson.JSONObject
     **/
    List<ProcessTaskVo> doSearchKeyword(WorkcenterVo workcenterVo);

    /**
     * @Description: 根据标题 id获取所有过滤选项 pc端
     * @Author: 89770
     * @Date: 2021/2/4 18:59
     * @Params: [keyword, pageSize]
     * @Returns: com.alibaba.fastjson.JSONArray
     **/
    JSONArray getKeywordOptionsPCNew(WorkcenterVo workcenterVo);


    /**
     * @Description: 获取用户工单中心table column theadList
     * @Author: 89770
     * @Date: 2021/1/19 20:38
     * @Params: [workcenterVo, columnComponentMap, sortColumnList]
     * @Returns: java.util.List<neatlogic.framework.process.workcenter.dto.WorkcenterTheadVo>
     **/
    List<WorkcenterTheadVo> getWorkcenterTheadList(WorkcenterVo workcenterVo, Map<String, IProcessTaskColumn> columnComponentMap);

    /**
     * 获取步骤待处理人
     * @param workerArray 处理人数组
     * @param stepVo 工单步骤
     */
    void getStepTaskWorkerList(JSONArray workerArray,ProcessTaskStepVo stepVo);

    /**
     * 任务处理人
     *
     * @param workerVo    处理人
     * @param stepVo      工单步骤
     * @param workerArray 处理人数组
     */
    void stepTaskWorker(ProcessTaskStepWorkerVo workerVo, ProcessTaskStepVo stepVo, JSONArray workerArray, List<String> workerUuidTypeList);

    /**
     * 其它模块协助处理人
     *
     * @param workerVo    处理人
     * @param stepVo      工单步骤
     * @param workerArray 处理人数组
     */
    void otherWorker(ProcessTaskStepWorkerVo workerVo, ProcessTaskStepVo stepVo, JSONArray workerArray, List<String> workerUuidTypeList);

    /**
     * 拼凑处理人column数据
     *
     * @param workerVo    处理人
     * @param workerJson  处理人json
     * @param workerArray 处理人数组
     */
    void getWorkerInfo(ProcessTaskStepWorkerVo workerVo, JSONObject workerJson, JSONArray workerArray);
}
