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

package neatlogic.module.process.api.workcenter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.Expression;
import neatlogic.framework.common.dto.BaseEditorVo;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.process.condition.core.IProcessTaskCondition;
import neatlogic.framework.process.workcenter.dto.WorkcenterVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.service.NewWorkcenterService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class WorkcenterKeywordSearchApi extends PrivateApiComponentBase {
    @Resource
    ProcessTaskMapper processTaskMapper;

    @Resource
    NewWorkcenterService newWorkcenterService;

    @Override
    public String getToken() {
        return "workcenter/keyword/search";
    }

    @Override
    public String getName() {
        return "nmpaw.workcenterkeywordsearchapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, maxLength = 500, desc = "nmpaw.workcenterkeywordsearchapi.input.param.desc.keyword"),
            @Param(name = "pageSize", type = ApiParamType.STRING, desc = "nmpaw.workcenterkeywordsearchapi.input.param.desc.pagesize"),
            @Param(name = "isCombine", type = ApiParamType.INTEGER, desc = "nmpaw.workcenterkeywordsearchapi.input.param.desc.iscombine")})
    @Output({@Param(name = "dataList", type = ApiParamType.JSONARRAY, desc = "nmpaw.workcenterkeywordsearchapi.output.param.desc.datalist")})
    @Description(desc = "nmpaw.workcenterkeywordsearchapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        String keyword = jsonObj.getString("keyword");
        Integer isCombine = jsonObj.getInteger("isCombine");
        if (StringUtils.isEmpty(keyword)) {
            return CollectionUtils.EMPTY_COLLECTION;
        }
        if (isCombine != null && isCombine == 0) {
            if (StringUtils.isNotBlank(keyword)) {
                BaseEditorVo baseEditorVo = new BaseEditorVo();
                baseEditorVo.setKeyword(keyword);
                baseEditorVo.setPageSize(jsonObj.getInteger("pageSize"));
                return processTaskMapper.getProcessTaskIdAndTitleByIndexKeyword(new ArrayList<>(baseEditorVo.getKeywordList()), baseEditorVo.getPageSize());
            }
            return CollectionUtils.EMPTY_COLLECTION;
        }
        return newWorkcenterService.getKeywordOptionsPCNew(new WorkcenterVo(jsonObj));
    }

    /**
     * 根据关键字获取所有过滤选项 移动端
     *
     * @param keyword
     * @return
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    private JSONArray getKeywordOptionMB(List<IProcessTaskCondition> conditionList, String keyword, Integer pageSize) {
        JSONArray returnArray = new JSONArray();
        /*WorkcenterVo workcenter = getKeywordConditionMB(conditionList, keyword);
        workcenter.setPageSize(pageSize);
        IElasticSearchHandler<WorkcenterVo, QueryResult> esHandler =
            ElasticSearchHandlerFactory.getHandler(ESHandler.PROCESSTASK.getValue());
        List<MultiAttrsObject> dataList = esHandler.search(workcenter).getData();
        if (!dataList.isEmpty()) {
            conditionList.add(ProcessTaskConditionFactory.getHandler(ProcessWorkcenterField.STATUS.getValue()));
            JSONObject titleObj = new JSONObject();
            JSONArray resultDateList = new JSONArray();
            for (MultiAttrsObject titleEl : dataList) {
                JSONObject data = new JSONObject();
                for (IProcessTaskCondition condition : conditionList) {
                    IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(condition.getName());
                    if (column == null) {
                        continue;
                    }
                    data.put(condition.getName(), column.getValue(titleEl));
                }
                resultDateList.add(data);
            }
            titleObj.put("dataList", resultDateList);
            returnArray.add(titleObj);
        }*/
        return returnArray;
    }

    /**
     * 拼接关键字过滤选项 移动端
     *
     * @param conditionList 搜索内容类型
     * @return
     */
    @Deprecated
    private WorkcenterVo getKeywordConditionMB(List<IProcessTaskCondition> conditionList, String keyword) {

        JSONObject searchObj = new JSONObject();
        JSONArray conditionGroupList = new JSONArray();
        JSONObject conditionGroup = new JSONObject();
        JSONArray conditionArray = new JSONArray();
        JSONArray conditionRelArray = new JSONArray();
        String fromUuid = null;
        for (int i = 0; i < conditionList.size(); i++) {
            IProcessTaskCondition con = conditionList.get(i);
            JSONObject conditionObj = new JSONObject();
            String uuid = UUID.randomUUID().toString().replace("-", "");
            conditionObj.put("uuid", uuid);
            conditionObj.put("name", con.getName());
            conditionObj.put("type", con.getType());
            JSONArray valueList = new JSONArray();
            valueList.add(keyword);
            conditionObj.put("valueList", valueList);
            conditionObj.put("expression", Expression.LIKE.getExpression());
            conditionArray.add(conditionObj);
            if (i != 0) {
                JSONObject conditionRel = new JSONObject();
                conditionRel.put("from", fromUuid);
                conditionRel.put("to", uuid);
                conditionRel.put("joinType", "or");
                conditionRelArray.add(conditionRel);
            } else {
                fromUuid = uuid;
            }

        }
        conditionGroup.put("conditionList", conditionArray);
        conditionGroup.put("conditionRelList", conditionRelArray);
        conditionGroupList.add(conditionGroup);
        searchObj.put("conditionGroupList", conditionGroupList);
        return new WorkcenterVo(searchObj);

    }


    /**
     * 根据单个关键字获取过滤选项 pc端
     *
     * @param keyword
     * @return
     */
   /* @SuppressWarnings("unchecked")
    @Deprecated
    private JSONArray getKeywordOptionPC(IProcessTaskCondition condition, String keyword, Integer pageSize) {
        JSONArray returnArray = new JSONArray();
        WorkcenterVo workcenter = getKeywordConditionPC(condition, keyword);
        workcenter.setPageSize(pageSize);
        IElasticSearchHandler<WorkcenterVo, QueryResult> esHandler =
            ElasticSearchHandlerFactory.getHandler(ESHandler.PROCESSTASK.getValue());
        List<MultiAttrsObject> dataList = esHandler.search(workcenter).getData();
        if (!dataList.isEmpty()) {
            JSONObject titleObj = new JSONObject();
            JSONArray titleDataList = new JSONArray();
            for (MultiAttrsObject titleEl : dataList) {
                IProcessTaskColumn column = ProcessTaskColumnFactory.getHandler(condition.getName());
                if (column == null) {
                    continue;
                }
                titleDataList.add(column.getValue(titleEl));
            }
            titleObj.put("dataList", titleDataList);
            titleObj.put("value", condition.getName());
            titleObj.put("text", condition.getDisplayName());
            returnArray.add(titleObj);
        }
        return returnArray;
    }*/

    /**
     * 拼接关键字过滤选项 pc端
     *
     * @param keyword 搜索内容类型
     * @return
     */
    @Deprecated
    private WorkcenterVo getKeywordConditionPC(IProcessTaskCondition condition, String keyword) {
        JSONObject searchObj = new JSONObject();
        JSONArray conditionGroupList = new JSONArray();
        JSONObject conditionGroup = new JSONObject();
        JSONArray conditionList = new JSONArray();
        JSONObject conditionObj = new JSONObject();
        conditionObj.put("name", condition.getName());
        conditionObj.put("type", condition.getType());
        JSONArray valueList = new JSONArray();
        valueList.add(keyword);
        conditionObj.put("valueList", valueList);
        conditionObj.put("expression", Expression.LIKE.getExpression());
        conditionList.add(conditionObj);
        conditionGroup.put("conditionList", conditionList);
        conditionGroupList.add(conditionGroup);
        searchObj.put("conditionGroupList", conditionGroupList);

        return new WorkcenterVo(searchObj);

    }
    /**
     * 根据关键字获取所有过滤选项 pc端
     *
     * @param keyword
     * @return
     */
   /* @Deprecated
    private JSONArray getKeywordOptionsPC(String keyword, Integer pageSize) {
        // 搜索标题
        JSONArray returnArray = getKeywordOptionPC(new ProcessTaskTitleCondition(), keyword, pageSize);
        // 搜索ID
        returnArray.addAll(getKeywordOptionPC(new ProcessTaskSerialNumberCondition(), keyword, pageSize));
        return returnArray;
    }*/
}
