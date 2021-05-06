package codedriver.module.process.api.workcenter.elasticsearch;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class EsProcessTaskSyncApi extends PrivateApiComponentBase {

    @Autowired
    ProcessTaskMapper processTaskMapper;

    @Override
    public String getToken() {
        return "processtask/es/sync";
    }

    @Override
    public String getName() {
        return "更新es工单数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "fromDate", type = ApiParamType.STRING, desc = "创建时间>=fromDate"),
        @Param(name = "toDate", type = ApiParamType.STRING, desc = "创建时间<toDate"),
        @Param(name = "documentIdList", type = ApiParamType.JSONARRAY, desc = "documentId数组"),
        @Param(name = "action", type = ApiParamType.STRING, desc = "delete,refresh")})
    @Output({

    })
    @Description(desc = "修改工单数据到es")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
       /* List<Object> taskIds = jsonObj.getJSONArray("processTaskIds");
        List<Long> taskIdList = null;
        List<String> taskIdStrList = null;
        if (CollectionUtils.isNotEmpty(taskIds)) {
            taskIdList = taskIds.stream().map(object -> Long.parseLong(object.toString())).collect(Collectors.toList());
            taskIdStrList = taskIds.stream().map(object -> object.toString()).collect(Collectors.toList());
        }
        String fromDate = jsonObj.getString("fromDate");
        String toDate = jsonObj.getString("toDate");
        String action = jsonObj.getString("action");
        if (action == null) {
            action = "refresh";
        }
        // 删除符合条件es数据
        String whereSql = StringUtils.EMPTY;
        if (StringUtils.isNotBlank(fromDate)) {
            whereSql = String.format(" where common.starttime >= '%s'", fromDate);
        }
        if (StringUtils.isNotBlank(toDate)) {
            if (StringUtils.isBlank(whereSql)) {
                whereSql = String.format(" where common.starttime < '%s'", toDate);
            } else {
                whereSql = whereSql + String.format(" and common.starttime < '%s'", toDate);
            }
        }

        if (CollectionUtils.isNotEmpty(taskIdList)) {
            if (StringUtils.isBlank(whereSql)) {
                whereSql = String.format(" where common.id contains any ( '%s' )", String.join("','", taskIdStrList));
            } else {
                whereSql =
                    whereSql + String.format(" and common.id contains any ( '%s' )", String.join("','", taskIdStrList));
            }
        }
        String esSql =
            String.format("select common.id from %s %s limit 0,20 ", TenantContext.get().getTenantUuid(), whereSql);
        MultiAttrsObjectPool objectPool = ElasticSearchPoolManager.getObjectPool(ESHandler.PROCESSTASK.getValue());
        objectPool.checkout(TenantContext.get().getTenantUuid());
        QueryParser parser = objectPool.createQueryParser();
        MultiAttrsQuery query = parser.parse(esSql);
        QueryResultSet resultSet = query.iterate();
        while (resultSet.hasMoreResults()) {
            QueryResult result = resultSet.fetchResult();
            if (!result.getData().isEmpty()) {
                for (MultiAttrsObject el : result.getData()) {
                    objectPool.delete(el.getId());
                }
            }
        }
        // 如果需要更新
        if (action.equals("refresh")) {
            List<ProcessTaskVo> processTaskVoList =
                processTaskMapper.getProcessTaskListByIdListAndStartTime(taskIdList, fromDate, toDate);
            for (ProcessTaskVo processTaskVo : processTaskVoList) {
                ElasticSearchHandlerFactory.getHandler(ESHandler.PROCESSTASK.getValue()).save(processTaskVo.getId());
            }
        }*/

        return null;
    }
}
