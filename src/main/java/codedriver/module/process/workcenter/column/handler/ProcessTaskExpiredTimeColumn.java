package codedriver.module.process.workcenter.column.handler;

import codedriver.framework.process.column.core.IProcessTaskColumn;
import codedriver.framework.process.column.core.ProcessTaskColumnBase;
import codedriver.framework.process.constvalue.ProcessFieldType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessTaskStepUserStatus;
import codedriver.framework.process.dto.ProcessTaskSlaTimeVo;
import codedriver.framework.process.dto.ProcessTaskSlaVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.workcenter.dto.JoinTableColumnVo;
import codedriver.framework.process.workcenter.dto.SelectColumnVo;
import codedriver.framework.process.workcenter.dto.TableSelectColumnVo;
import codedriver.framework.process.workcenter.table.*;
import codedriver.framework.process.workcenter.table.util.SqlTableUtil;
import codedriver.framework.worktime.dao.mapper.WorktimeMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProcessTaskExpiredTimeColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {

    @Autowired
    WorktimeMapper worktimeMapper;

    @Override
    public String getName() {
        return "expiretime";
    }

    @Override
    public String getDisplayName() {
        return "剩余时间";
    }

    @Override
    public Boolean allowSort() {
        return false;
    }

    @Override
    public String getType() {
        return ProcessFieldType.COMMON.getValue();
    }

    @Override
    public String getClassName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Integer getSort() {
        return 14;
    }

    @Override
    public Boolean getIsSort() {
        return true;
    }

    @Override
    public String getSimpleValue(ProcessTaskVo taskVo) {
        StringBuilder sb = new StringBuilder();
        JSONArray resultArray = JSONArray.parseArray(getValue(taskVo).toString());
        if (CollectionUtils.isNotEmpty(resultArray)) {
            for (int i = 0; i < resultArray.size(); i++) {
                JSONObject object = resultArray.getJSONObject(i);
                Long expireTime = object.getLong("expireTime");
                Long willOverTime = object.getLong("willOverTime");
                long time;
                if (willOverTime != null && System.currentTimeMillis() > willOverTime) {
                    time = System.currentTimeMillis() - willOverTime;
                    sb.append(object.getString("slaName"))
                            .append("距离超时：")
                            .append(Math.floor(time / (1000 * 60 * 60 * 24)))
                            .append("天;");
                } else if (expireTime != null && System.currentTimeMillis() > expireTime) {
                    time = System.currentTimeMillis() - expireTime;
                    sb.append(object.getString("slaName"))
                            .append("已超时：")
                            .append(Math.floor(time / (1000 * 60 * 60 * 24)))
                            .append("天;");
                }
            }
        }
        return sb.toString();
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        List<ProcessTaskSlaVo> processTaskSlaList = processTaskVo.getProcessTaskSlaVoList();
        JSONArray resultArray = new JSONArray();
        if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus()) && CollectionUtils.isNotEmpty(processTaskSlaList)) {
            for (ProcessTaskSlaVo slaVo : processTaskSlaList) {
                //判断需要 同时满足 该步骤是进行中状态，以及包含sla策略
                if (processTaskVo.getStepList().stream().noneMatch(o -> (Objects.equals(o.getStatus(), ProcessTaskStatus.RUNNING.getValue()) || (Objects.equals(o.getStatus(), ProcessTaskStatus.PENDING.getValue()) && o.getIsActive() == 1))
                        && CollectionUtils.isNotEmpty(o.getSlaTimeList())
                        && o.getSlaTimeList().stream().anyMatch(c -> Objects.equals(c.getSlaId(), slaVo.getId())))) {
                    continue;
                }
                JSONObject tmpJson = new JSONObject();
                ProcessTaskSlaTimeVo slaTimeVo = slaVo.getSlaTimeVo();
                if (slaTimeVo == null || Objects.equals(ProcessTaskStepUserStatus.DONE.getValue(), slaTimeVo.getStatus())) {
                    continue;
                }
                Long expireTimeLong = slaTimeVo.getExpireTime() != null ? slaTimeVo.getExpireTime().getTime() : null;
                Long realExpireTimeLong = slaTimeVo.getRealExpireTime() != null ? slaTimeVo.getRealExpireTime().getTime() : null;
                if (expireTimeLong != null) {
                    long timeLeft = worktimeMapper.calculateCostTime(processTaskVo.getWorktimeUuid(), System.currentTimeMillis(), expireTimeLong);
                    tmpJson.put("timeLeft", timeLeft);
                    tmpJson.put("expireTime", expireTimeLong);
                }
                if (realExpireTimeLong != null) {
                    long realTimeLeft = realExpireTimeLong - System.currentTimeMillis();
                    tmpJson.put("realTimeLeft", realTimeLeft);
                    tmpJson.put("realExpireTime", realExpireTimeLong);
                }
                tmpJson.put("slaName", slaVo.getName());
                //获取即将超时规则，默认分钟（从超时通知策略获取）
                JSONObject configJson = slaVo.getConfigObj();
                if (configJson.containsKey("notifyPolicyList") && CollectionUtils.isNotEmpty(configJson.getJSONArray("notifyPolicyList"))) {
                    JSONArray notifyPolicyList = configJson.getJSONArray("notifyPolicyList");
                    int time = -1;
                    for (int i = 0; i < notifyPolicyList.size(); i++) {
                        JSONObject notifyPolicyJson = notifyPolicyList.getJSONObject(i);
                        if (notifyPolicyJson.getString("expression").equals("before")) {
                            if (time == -1 || time > notifyPolicyJson.getIntValue("time")) {
                                time = notifyPolicyJson.getIntValue("time");
                            }
                        }
                    }
                    tmpJson.put("willOverTimeRule", time);
                    if (expireTimeLong != null) {
                        tmpJson.put("willOverTime", expireTimeLong - time * 60 * 100);
                    }
                }
                resultArray.add(tmpJson);
            }
        }
        return resultArray;
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<TableSelectColumnVo>() {
            {
                add(new TableSelectColumnVo(new ProcessTaskSlaSqlTable(),
                        Arrays.asList(
                                new SelectColumnVo(ProcessTaskSlaSqlTable.FieldEnum.ID.getValue(), "processTaskSlaId"),
                                new SelectColumnVo(ProcessTaskSlaSqlTable.FieldEnum.NAME.getValue(), "processTaskSlaName"),
                                new SelectColumnVo(ProcessTaskSlaSqlTable.FieldEnum.CONFIG.getValue(), "processTaskSlaConfig")
                        )
                ));
                add(new TableSelectColumnVo(new ProcessTaskSlaTimeSqlTable(),
                        Arrays.asList(
                                new SelectColumnVo(ProcessTaskSlaTimeSqlTable.FieldEnum.EXPIRE_TIME.getValue(), "expireTime")
                                , new SelectColumnVo(ProcessTaskSlaTimeSqlTable.FieldEnum.REALEXPIRE_TIME.getValue(), "realExpireTime")
                                , new SelectColumnVo(ProcessTaskSlaTimeSqlTable.FieldEnum.TIME_LEFT.getValue(), "timeLeft")
                                , new SelectColumnVo(ProcessTaskSlaTimeSqlTable.FieldEnum.REALTIME_LEFT.getValue(), "realTimeLeft")
                                , new SelectColumnVo(ProcessTaskSlaTimeSqlTable.FieldEnum.STATUS.getValue(), "slaTimeStatus")
                        )
                ));
                add(new TableSelectColumnVo(new ProcessTaskSqlTable(),
                        Collections.singletonList(
                                new SelectColumnVo(ProcessTaskSqlTable.FieldEnum.STATUS.getValue())
                        )
                ));
                /*add(new TableSelectColumnVo(new ProcessTaskStepSlaSqlTable(),
                        Arrays.asList(
                                new SelectColumnVo(ProcessTaskStepSlaSqlTable.FieldEnum.PROCESSTASK_STEP_ID.getValue(), "slaStepId"),
                                new SelectColumnVo(ProcessTaskStepSlaSqlTable.FieldEnum.SLA_ID.getValue(), "slaId")
                        )
                ));*/
            }
        };
    }

    @Override
    public List<JoinTableColumnVo> getMyJoinTableColumnList() {
        return SqlTableUtil.getExpireTimeJoinTableSql();
    }

    @Override
    public String getMySortSqlColumn(Boolean isColumn) {
        return String.format(" %s MAX(%s.%s) ", isColumn ? StringUtils.EMPTY : "-", getMySortSqlTable().getShortName(), ProcessTaskSlaTimeSqlTable.FieldEnum.EXPIRE_TIME.getValue());
    }

    @Override
    public ISqlTable getMySortSqlTable() {
        return new ProcessTaskSlaTimeSqlTable();
    }
}
