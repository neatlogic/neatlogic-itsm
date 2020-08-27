package codedriver.module.process.api.event;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.event.EventTypeMapper;
import codedriver.framework.process.dto.event.EventTypeVo;
import codedriver.framework.process.exception.event.EventTypeNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventTypeTreeApi extends PrivateApiComponentBase {

    @Autowired
    private EventTypeMapper eventTypeMapper;

    @Override
    public String getToken() {
        return "eventtype/tree";
    }

    @Override
    public String getName() {
        return "获取事件类型架构树";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "parentId", desc = "parentId，这里指父级id", type = ApiParamType.LONG),
             @Param( name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER),
             @Param( name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
             @Param( name = "pageSize", desc = "每页最大数", type = ApiParamType.INTEGER)
    })
    @Output({
           @Param( name = "tbodyList", explode = EventTypeVo[].class, desc = "事件类型架构集合"),
           @Param( explode = BasePageVo.class)
    })
    @Description(desc = "获取事件类型架构树")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        EventTypeVo eventTypeVo = new EventTypeVo();
        Boolean needPage = jsonObj.getBoolean("needPage");
        if (needPage != null){
            eventTypeVo.setNeedPage(needPage);
        }
        eventTypeVo.setCurrentPage(jsonObj.getInteger("currentPage"));
        eventTypeVo.setPageSize(jsonObj.getInteger("pageSize"));
        Long parentId = jsonObj.getLong("parentId");
        if (parentId != null){
            if(eventTypeMapper.checkEventTypeIsExists(parentId) == 0) {
                throw new EventTypeNotFoundException(parentId);
            }
        }else {
            parentId = EventTypeVo.ROOT_ID;
        }
        eventTypeVo.setParentId(parentId);
        if (eventTypeVo.getNeedPage()){
            int rowNum = eventTypeMapper.searchEventTypeCount(eventTypeVo);
            returnObj.put("currentPage", eventTypeVo.getCurrentPage());
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, eventTypeVo.getPageSize()));
            returnObj.put("pageSize", eventTypeVo.getPageSize());
            returnObj.put("rowNum", rowNum);
        }
        List<EventTypeVo> tbodyList = eventTypeMapper.searchEventType(eventTypeVo);

        /** 查询子类和关联的解决方案数量 */
        if(CollectionUtils.isNotEmpty(tbodyList)) {
            List<Long> eventTypeIdList = tbodyList.stream().map(EventTypeVo::getId).collect(Collectors.toList());
            List<EventTypeVo> eventTypeSolutionCountAndChildCountList = eventTypeMapper.getEventTypeSolutionCountAndChildCountListByIdList(eventTypeIdList);
            Map<Long, EventTypeVo> eventTypeSolutionCountAndChildCountMap = new HashMap<>();
            for(EventTypeVo eventType : eventTypeSolutionCountAndChildCountList) {
                eventTypeSolutionCountAndChildCountMap.put(eventType.getId(), eventType);
            }
            for(EventTypeVo eventType : tbodyList) {
                EventTypeVo eventTypeSolutionCountAndChildCount = eventTypeSolutionCountAndChildCountMap.get(eventType.getId());
                if(eventTypeSolutionCountAndChildCount != null) {
                    eventType.setChildCount(eventTypeSolutionCountAndChildCount.getChildCount());
                    eventType.setSolutionCount(eventTypeSolutionCountAndChildCount.getSolutionCount());
                }
            }
        }
        returnObj.put("tbodyList", tbodyList);
        return returnObj;
    }
}
