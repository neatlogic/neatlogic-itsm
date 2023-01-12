package codedriver.module.process.api.processtask;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import codedriver.framework.util.FileUtil;
import codedriver.module.process.dao.mapper.ProcessTaskDataMapper;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExportProcessTaskDataApi extends PrivateBinaryStreamApiComponentBase {
    private final static Logger logger = LoggerFactory.getLogger(ExportProcessTaskDataApi.class);

    @Resource
    private ProcessTaskDataMapper processTaskDataMapper;

    @Override
    public String getToken() {
        return "processtask/data/export";
    }

    @Override
    public String getName() {
        return "工单相关表数据导出";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单ID")
    })
    @Output({})
    @Description(desc = "工单相关表数据导出")
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long processTaskId = paramObj.getLong("processTaskId");
        JSONObject resultObj = getDataByProcessTaskId(processTaskId);
        String fileName = FileUtil.getEncodedFileName("工单_" + processTaskId + "_数据.pak");
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-Disposition", " attachment; filename=\"" + fileName + "\"");

        try (ZipOutputStream zipos = new ZipOutputStream(response.getOutputStream())) {
            zipos.putNextEntry(new ZipEntry(processTaskId + ".json"));
            zipos.write(JSONObject.toJSONBytes(resultObj));
            zipos.closeEntry();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private JSONObject getDataByProcessTaskId(Long processTaskId) {
        JSONObject resultObj = new JSONObject();
        Set<String> contentHashSet = new HashSet<>();
        Set<String> fileIdSet = new HashSet<>();
        {
            // 工单基本信息
            String sql = "SELECT a.*, b.* FROM `processtask` a LEFT JOIN `processtask_config` b on b.`hash` = a.`config_hash` WHERE `id` = " + processTaskId;
            Map<String, Object> processtask = processTaskDataMapper.getOne(sql);
            if (processtask == null) {
                return resultObj;
            }
            put(resultObj, "processtask", processtask);

            sql = "SELECT * FROM `process` WHERE `uuid` = '" + processtask.get("process_uuid") + "'";
            Map<String, Object> process = processTaskDataMapper.getOne(sql);
            put(resultObj, "process", process);

            String channelUuid = (String) processtask.get("channel_uuid");
            sql = "SELECT * FROM `channel` WHERE `uuid` = '" + channelUuid + "'";
            Map<String, Object> channel = processTaskDataMapper.getOne(sql);
            put(resultObj, "channel", channel);

            sql = "SELECT * FROM `channel_authority` WHERE `channel_uuid` = '" + channelUuid + "'";
            List<Map<String, Object>> channel_authority = processTaskDataMapper.getList(sql);
            put(resultObj, "channel_authority", channel_authority);

            sql = "SELECT * FROM `channel_priority` WHERE `channel_uuid` = '" + channelUuid + "'";
            List<Map<String, Object>> channel_priority = processTaskDataMapper.getList(sql);
            put(resultObj, "channel_priority", channel_priority);

            sql = "SELECT * FROM `channel_process` WHERE `channel_uuid` = '" + channelUuid + "'";
            Map<String, Object> channel_process = processTaskDataMapper.getOne(sql);
            put(resultObj, "channel_process", channel_process);

            sql = "SELECT * FROM `channel_user` WHERE `channel_uuid` = '" + channelUuid + "'";
            List<Map<String, Object>> channel_user = processTaskDataMapper.getList(sql);
            put(resultObj, "channel_user", channel_user);

            sql = "SELECT * FROM `channel_worktime` WHERE `channel_uuid` = '" + channelUuid + "'";
            Map<String, Object> channel_worktime = processTaskDataMapper.getOne(sql);
            put(resultObj, "channel_worktime", channel_worktime);

            sql = "SELECT * FROM `channel_relation` WHERE `source` = '" + channelUuid + "'";
            List<Map<String, Object>> channel_relation = processTaskDataMapper.getList(sql);
            put(resultObj, "channel_relation", channel_relation);

            sql = "SELECT * FROM `channel_relation_authority` WHERE `source` = '" + channelUuid + "'";
            List<Map<String, Object>> channel_relation_authority = processTaskDataMapper.getList(sql);
            put(resultObj, "channel_relation_authority", channel_relation_authority);

            sql = "SELECT * FROM `channel_relation_isusepreowner` WHERE `source` = '" + channelUuid + "'";
            List<Map<String, Object>> channel_relation_isusepreowner = processTaskDataMapper.getList(sql);
            put(resultObj, "channel_relation_isusepreowner", channel_relation_isusepreowner);
        }
        {
            // 指派处理人
            String sql = "SELECT * FROM `processtask_assignworker` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_assignworker = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_assignworker", processtask_assignworker);
        }
        {
            // 自动评分
            String sql = "SELECT * FROM `processtask_auto_score` WHERE `processtask_id` = " + processTaskId;
            Map<String, Object> processtask_auto_score = processTaskDataMapper.getOne(sql);
            put(resultObj, "processtask_auto_score", processtask_auto_score);
        }
        {
            // 工单文件
            String sql = "SELECT * FROM `processtask_file` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_file = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_file", processtask_file);
            for (Map<String, Object> map : processtask_file) {
                Object fileId = map.get("file_id");
                if (fileId != null) {
                    fileIdSet.add(fileId.toString());
                }
            }
        }
        {
            // 工单关注人
            String sql = "SELECT * FROM `processtask_focus` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_focus = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_focus", processtask_focus);
        }
        {
            // 工单表单配置信息
            String sql = "SELECT a.*, b.* FROM `processtask_form` a LEFT JOIN `processtask_form_content` b ON b.`hash` = a.`form_content_hash` WHERE a.`processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_form = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_form", processtask_form);
        }
        {
            // 工单表单数据
            String sql = "SELECT * FROM `processtask_formattribute_data` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_formattribute_data = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_formattribute_data", processtask_formattribute_data);
        }
        {
            // 工单关联
            String sql = "SELECT * FROM `processtask_relation` WHERE `source` = " + processTaskId + " OR `target` = " + processTaskId;
            List<Map<String, Object>> processtask_relation = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_relation", processtask_relation);
        }
        {
            // 工单转报
            String sql = "SELECT * FROM `processtask_tranfer_report` WHERE `from_processtask_id` = " + processTaskId + " OR `to_processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_tranfer_report = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_tranfer_report", processtask_tranfer_report);
        }
        {
            // 工单重复
            String sql = "SELECT `repeat_group_id` FROM `processtask_repeat` WHERE `processtask_id` = " + processTaskId;
            Long repeatGroupId = processTaskDataMapper.getLong(sql);
            if (repeatGroupId != null) {
                sql = "SELECT * FROM `processtask_repeat` WHERE `repeat_group_id` = " + repeatGroupId;
                List<Map<String, Object>> processtask_repeat = processTaskDataMapper.getList(sql);
                put(resultObj, "processtask_repeat", processtask_repeat);
            }
        }
        {
            // 评分
            String sql = "SELECT * FROM `processtask_score` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_score = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_score", processtask_score);
            Set<String> scoreTemplateIdSet = new HashSet<>();
            Set<String> scoreDimensionIdSet = new HashSet<>();
            for (Map<String, Object> map : processtask_score) {
                scoreTemplateIdSet.add(map.get("score_template_id").toString());
                scoreDimensionIdSet.add(map.get("score_dimension_id").toString());
            }
            if (CollectionUtils.isNotEmpty(scoreTemplateIdSet)) {
                String scoreTemplateIdSetStr = String.join(",", scoreTemplateIdSet);
                sql = "SELECT * FROM `score_template` WHERE `id` IN (" + scoreTemplateIdSetStr + ")";
                List<Map<String, Object>> score_template = processTaskDataMapper.getList(sql);
                put(resultObj, "score_template", score_template);
            }
            if (CollectionUtils.isNotEmpty(scoreDimensionIdSet)) {
                String scoreDimensionIdSetStr = String.join(",", scoreDimensionIdSet);
                sql = "SELECT * FROM `score_template_dimension` WHERE `id` IN (" + scoreDimensionIdSetStr + ")";
                List<Map<String, Object>> score_template_dimension = processTaskDataMapper.getList(sql);
                put(resultObj, "score_template_dimension", score_template_dimension);
            }
        }
        {
            // 评分
            String sql = "SELECT a.*, b.* FROM `processtask_score_content` a LEFT JOIN `processtask_content` b ON b.`hash` = a.`content_hash` WHERE a.`processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_score_content = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_score_content", processtask_score_content);
        }
        {
            // 评分模板
            String sql = "SELECT a.*, b.* FROM `processtask_score_template` a LEFT JOIN `processtask_score_template_config` b ON b.`hash` = a.`config_hash` WHERE a.`processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_score_template = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_score_template", processtask_score_template);
        }
        {
            // 工单号
            String sql = "SELECT * FROM `processtask_serial_number` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_serial_number = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_serial_number", processtask_serial_number);
        }
        {
            //时效
            String sql = "SELECT * FROM `processtask_sla` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_sla = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_sla", processtask_sla);
            List<String> slaIdList = new ArrayList<>();
            for (Map<String, Object> map : processtask_sla) {
                Object id = map.get("id");
                if (id != null) {
                    slaIdList.add(id.toString());
                }
            }
            if (CollectionUtils.isNotEmpty(slaIdList)) {
                String slaIdListStr = String.join(",", slaIdList);
                sql = "SELECT * FROM `processtask_sla_time` WHERE `sla_id` IN (" + slaIdListStr + ")";
                List<Map<String, Object>> processtask_sla_time = processTaskDataMapper.getList(sql);
                put(resultObj, "processtask_sla_time", processtask_sla_time);

                sql = "SELECT * FROM `processtask_sla_notify` WHERE `sla_id` IN (" + slaIdListStr + ")";
                List<Map<String, Object>> processtask_sla_notify = processTaskDataMapper.getList(sql);
                put(resultObj, "processtask_sla_notify", processtask_sla_notify);

                sql = "SELECT * FROM `processtask_sla_transfer` WHERE `sla_id` IN (" + slaIdListStr + ")";
                List<Map<String, Object>> processtask_sla_transfer = processTaskDataMapper.getList(sql);
                put(resultObj, "processtask_sla_transfer", processtask_sla_transfer);

                sql = "SELECT * FROM `processtask_step_sla` WHERE `sla_id` IN (" + slaIdListStr + ")";
                List<Map<String, Object>> processtask_step_sla = processTaskDataMapper.getList(sql);
                put(resultObj, "processtask_step_sla", processtask_step_sla);

                sql = "SELECT * FROM `processtask_step_sla_time` WHERE `sla_id` IN (" + slaIdListStr + ")";
                List<Map<String, Object>> processtask_step_sla_time = processTaskDataMapper.getList(sql);
                put(resultObj, "processtask_step_sla_time", processtask_step_sla_time);
            }
        }
        {
            // 步骤列表
            String sql = "SELECT a.*, b.* FROM `processtask_step` a LEFT JOIN `processtask_step_config` b ON b.`hash` = a.`config_hash` WHERE a.`processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step", processtask_step);
            List<String> stepIdList = new ArrayList<>();
            for (Map<String, Object> map : processtask_step) {
                Object id = map.get("id");
                if (id != null) {
                    stepIdList.add(id.toString());
                }
            }
            if (CollectionUtils.isNotEmpty(stepIdList)) {
                String stepIdListStr = String.join(",", stepIdList);
                sql = "SELECT * FROM `processtask_step_timeaudit` WHERE `processtask_step_id` IN (" + stepIdListStr + ")";
                List<Map<String, Object>> processtask_step_timeaudit = processTaskDataMapper.getList(sql);
                put(resultObj, "processtask_step_timeaudit", processtask_step_timeaudit);
            }
        }
        {
            // 代办用户
            String sql = "SELECT * FROM `processtask_step_agent` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_agent = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_agent", processtask_step_agent);
        }
        {
            // 活动列表
            String sql = "SELECT * FROM `processtask_step_audit` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_audit = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_audit", processtask_step_audit);
            List<String> auditIdList = new ArrayList<>();
            for (Map<String, Object> map : processtask_step_audit) {
                Object id = map.get("id");
                if (id != null) {
                    auditIdList.add(id.toString());
                }
                addNotNullElement(contentHashSet, map.get("description_hash"));
            }
            if (CollectionUtils.isNotEmpty(auditIdList)) {
                String auditIdListStr = String.join(",", auditIdList);
                sql = "SELECT * FROM `processtask_step_audit_detail` WHERE `audit_id` IN (" + auditIdListStr + ")";
                List<Map<String, Object>> processtask_step_audit_detail = processTaskDataMapper.getList(sql);
                put(resultObj, "processtask_step_audit_detail", processtask_step_audit_detail);
                for (Map<String, Object> map : processtask_step_audit_detail) {
                    String oldContent = (String) map.get("old_content");
                    if (StringUtils.length(oldContent) == 32) {
                        contentHashSet.add(oldContent);
                    }
                    String newContent = (String) map.get("new_content");
                    if (StringUtils.length(newContent) == 32) {
                        contentHashSet.add(newContent);
                    }
                }
            }
        }
        {
            // 自动处理
            String sql = "SELECT * FROM `processtask_step_automatic_request` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_automatic_request = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_automatic_request", processtask_step_automatic_request);
        }
        {
            // 步骤回复内容
            String sql = "SELECT a.*, b.* FROM `processtask_step_content` a LEFT JOIN `processtask_content` b ON b.`hash` = a.`content_hash` WHERE a.`processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_content = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_content", processtask_step_content);
        }
        {
            // processtask_step_data
            String sql = "SELECT * FROM `processtask_step_data` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_data = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_data", processtask_step_data);
        }
        {
            // 事件步骤
            String sql = "SELECT * FROM `processtask_step_event` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_event = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_event", processtask_step_event);
            List<String> eventIdList = new ArrayList<>();
            for (Map<String, Object> map : processtask_step_event) {
                Object eventId = map.get("event_id");
                if (eventId != null) {
                    eventIdList.add(eventId.toString());
                }
            }
            Set<String> eventTypeIdSet = new HashSet<>();
            Set<String> eventSolutionIdSet = new HashSet<>();
            if (CollectionUtils.isNotEmpty(eventIdList)) {
                String eventIdListStr = String.join(",", eventIdList);
                sql = "SELECT * FROM `event` WHERE `id` IN (" + eventIdListStr + ")";
                List<Map<String, Object>> event = processTaskDataMapper.getList(sql);
                put(resultObj, "event", event);
                for (Map<String, Object> map : event) {
                    Object eventTypeId = map.get("event_type_id");
                    if (eventTypeId != null) {
                        eventTypeIdSet.add(eventTypeId.toString());
                    }
                    Object eventSolutionId = map.get("event_solution_id");
                    if (eventSolutionId != null) {
                        eventSolutionIdSet.add(eventSolutionId.toString());
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(eventTypeIdSet)) {
                String eventTypeIdListStr = String.join(",", eventTypeIdSet);
                sql = "SELECT * FROM `event_type` WHERE `id` IN (" + eventTypeIdListStr + ")";
                List<Map<String, Object>> event_type = processTaskDataMapper.getList(sql);
                put(resultObj, "event_type", event_type);

                sql = "SELECT * FROM `event_type_authority` WHERE `event_type_id` IN (" + eventTypeIdListStr + ")";
                List<Map<String, Object>> event_type_authority = processTaskDataMapper.getList(sql);
                put(resultObj, "event_type_authority", event_type_authority);

                sql = "SELECT * FROM `event_type_solution` WHERE `event_type_id` IN (" + eventTypeIdListStr + ")";
                List<Map<String, Object>> event_type_solution = processTaskDataMapper.getList(sql);
                put(resultObj, "event_type_solution", event_type_solution);
                for (Map<String, Object> map : event_type_solution) {
                    eventSolutionIdSet.add(map.get("solution_id").toString());
                }
            }
            if (CollectionUtils.isNotEmpty(eventSolutionIdSet)) {
                String eventSolutionIdSetStr = String.join(",", eventSolutionIdSet);
                sql = "SELECT * FROM `event_solution` WHERE `id` IN (" + eventSolutionIdSetStr + ")";
                List<Map<String, Object>> event_solution = processTaskDataMapper.getList(sql);
                put(resultObj, "event_solution", event_solution);
            }
        }
        {
            // 步骤表单权限
            String sql = "SELECT * FROM `processtask_step_formattribute` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_formattribute = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_formattribute", processtask_step_formattribute);
        }
        {
            // 正在后台进行处理操作的步骤列表
            String sql = "SELECT * FROM `processtask_step_in_operation` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_in_operation = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_in_operation", processtask_step_in_operation);
        }
        {
            // 重审步骤
            String sql = "SELECT * FROM `processtask_step_reapproval_restore_backup` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_reapproval_restore_backup = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_reapproval_restore_backup", processtask_step_reapproval_restore_backup);
        }
        {
            // 步骤连线状态
            String sql = "SELECT * FROM `processtask_step_rel` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_rel = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_rel", processtask_step_rel);
            sql = "SELECT * FROM `processtask_converge` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_converge = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_converge", processtask_converge);
        }
        {
            //步骤提醒信息
            String sql = "SELECT a.*, b.* FROM `processtask_step_remind` a LEFT JOIN `processtask_content` b ON b.`hash` = a.`content_hash` WHERE a.`processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_remind = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_remind", processtask_step_remind);
        }
        {
            //步骤标签
            String sql = "SELECT * FROM `processtask_step_tag` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_tag = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_tag", processtask_step_tag);
        }
        {
            // 定时步骤
            String sql = "SELECT * FROM `processtask_step_timer` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_timer = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_timer", processtask_step_timer);
        }
        {
            // 步骤处理人
            String sql = "SELECT * FROM `processtask_step_user` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_user = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_user", processtask_step_user);
        }
        {
            // 步骤待处理人
            String sql = "SELECT * FROM `processtask_step_worker` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_worker = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_worker", processtask_step_worker);
        }
        {
            // 步骤处理人分配策略
            String sql = "SELECT * FROM `processtask_step_worker_policy` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_worker_policy = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_worker_policy", processtask_step_worker_policy);
        }
        {
            //工单标签
            String sql = "SELECT * FROM `processtask_tag` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_tag = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_tag", processtask_tag);
        }
        {
            // 工单耗时
            String sql = "SELECT * FROM `processtask_time_cost` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_time_cost = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_time_cost", processtask_time_cost);
        }
        {
            //子任务
            String sql = "SELECT a.*, b.* FROM `processtask_step_task` a LEFT JOIN `processtask_content` b ON b.`hash` = a.`content_hash` WHERE a.`processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_task = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_task", processtask_step_task);
            List<String> taskIdList = new ArrayList<>();
            List<String> taskConfigIdList = new ArrayList<>();
            for (Map<String, Object> map : processtask_step_task) {
                taskIdList.add(map.get("id").toString());
                taskConfigIdList.add(map.get("task_config_id").toString());
            }
            if (CollectionUtils.isNotEmpty(taskIdList)) {
                String taskIdListStr = String.join(",", taskIdList);
                sql = "SELECT * FROM `processtask_step_task_user` WHERE `processtask_step_task_id` IN (" + taskIdListStr + ")";
                List<Map<String, Object>> processtask_step_task_user = processTaskDataMapper.getList(sql);
                put(resultObj, "processtask_step_task_user", processtask_step_task_user);

                sql = "SELECT * FROM `processtask_step_task_user_agent` WHERE `processtask_step_task_id` IN (" + taskIdListStr + ")";
                List<Map<String, Object>> processtask_step_task_user_agent = processTaskDataMapper.getList(sql);
                put(resultObj, "processtask_step_task_user_agent", processtask_step_task_user_agent);

                sql = "SELECT * FROM `processtask_step_task_user_file` WHERE `processtask_step_task_id` IN (" + taskIdListStr + ")";
                List<Map<String, Object>> processtask_step_task_user_file = processTaskDataMapper.getList(sql);
                put(resultObj, "processtask_step_task_user_file", processtask_step_task_user_file);
                for (Map<String, Object> map : processtask_step_task_user_file) {
                    Object fileId = map.get("file_id");
                    if (fileId != null) {
                        fileIdSet.add(fileId.toString());
                    }
                }
                sql = "SELECT a.*, b.* FROM `processtask_step_task_user_content` a LEFT JOIN `processtask_content` b ON b.`hash` = a.`content_hash` WHERE a.`processtask_step_task_id` IN (" + taskIdListStr + ")";
                List<Map<String, Object>> processtask_step_task_user_content = processTaskDataMapper.getList(sql);
                put(resultObj, "processtask_step_task_user_content", processtask_step_task_user_content);
            }
            if (CollectionUtils.isNotEmpty(taskConfigIdList)) {
                String taskConfigIdListStr = String.join(",", taskConfigIdList);
                sql = "SELECT * FROM `task_config` WHERE `id` IN (" + taskConfigIdListStr + ")";
                List<Map<String, Object>> task_config = processTaskDataMapper.getList(sql);
                put(resultObj, "task_config", task_config);
            }
        }

        {
            //变更创建
            String sql = "SELECT * FROM `processtask_step_change_create` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_change_create = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_change_create", processtask_step_change_create);
            List<String> changeIdSet = new ArrayList<>();
            for (Map<String, Object> map : processtask_step_change_create) {
                changeIdSet.add(map.get("change_id").toString());
            }
            if (CollectionUtils.isNotEmpty(changeIdSet)) {
                String changeIdSetStr = String.join(",", changeIdSet);
                sql = "SELECT * FROM `change` WHERE `id` IN (" + changeIdSetStr + ")";
                List<Map<String, Object>> change = processTaskDataMapper.getList(sql);
                put(resultObj, "change", change);

                sql = "SELECT * FROM `change_user` WHERE `change_id` IN (" + changeIdSetStr + ")";
                List<Map<String, Object>> change_user = processTaskDataMapper.getList(sql);
                put(resultObj, "change_user", change_user);

                sql = "SELECT * FROM `change_change_template` WHERE `change_id` IN (" + changeIdSetStr + ")";
                List<Map<String, Object>> change_change_template = processTaskDataMapper.getList(sql);
                put(resultObj, "change_change_template", change_change_template);

                sql = "SELECT a.*, b.* FROM `change_description` a LEFT JOIN `change_content` b ON b.`hash` = a.`content_hash` WHERE a.`change_id` IN (" + changeIdSetStr + ")";
                List<Map<String, Object>> change_description = processTaskDataMapper.getList(sql);
                put(resultObj, "change_description", change_description);

                sql = "SELECT * FROM `change_file` WHERE `change_id` IN (" + changeIdSetStr + ")";
                List<Map<String, Object>> change_file = processTaskDataMapper.getList(sql);
                put(resultObj, "change_file", change_file);
                for (Map<String, Object> map : change_file) {
                    Object fileId = map.get("file_id");
                    if (fileId != null) {
                        fileIdSet.add(fileId.toString());
                    }
                }
                sql = "SELECT * FROM `change_step` WHERE `change_id` IN (" + changeIdSetStr + ")";
                List<Map<String, Object>> change_step = processTaskDataMapper.getList(sql);
                put(resultObj, "change_step", change_step);

                sql = "SELECT a.*, b.* FROM `change_step_content` a LEFT JOIN `change_content` b ON b.`hash` = a.`content_hash` WHERE a.`change_id` IN (" + changeIdSetStr + ")";
                List<Map<String, Object>> change_step_content = processTaskDataMapper.getList(sql);
                put(resultObj, "change_step_content", change_step_content);

                sql = "SELECT a.*, b.* FROM `change_step_comment` a LEFT JOIN `change_content` b ON b.`hash` = a.`content_hash` WHERE a.`change_id` IN (" + changeIdSetStr + ")";
                List<Map<String, Object>> change_step_comment = processTaskDataMapper.getList(sql);
                put(resultObj, "change_step_comment", change_step_comment);

                sql = "SELECT * FROM `change_step_file` WHERE `change_id` IN (" + changeIdSetStr + ")";
                List<Map<String, Object>> change_step_file = processTaskDataMapper.getList(sql);
                put(resultObj, "change_step_file", change_step_file);
                for (Map<String, Object> map : change_file) {
                    Object fileId = map.get("file_id");
                    if (fileId != null) {
                        fileIdSet.add(fileId.toString());
                    }
                }

                sql = "SELECT * FROM `change_step_team` WHERE `change_id` IN (" + changeIdSetStr + ")";
                List<Map<String, Object>> change_step_team = processTaskDataMapper.getList(sql);
                put(resultObj, "change_step_team", change_step_team);

                sql = "SELECT * FROM `change_step_user` WHERE `change_id` IN (" + changeIdSetStr + ")";
                List<Map<String, Object>> change_step_user = processTaskDataMapper.getList(sql);
                put(resultObj, "change_step_user", change_step_user);

                sql = "SELECT * FROM `change_step_pause_operate` WHERE `change_id` IN (" + changeIdSetStr + ")";
                List<Map<String, Object>> change_step_pause_operate = processTaskDataMapper.getList(sql);
                put(resultObj, "change_step_pause_operate", change_step_pause_operate);

                sql = "SELECT * FROM `change_auto_start` WHERE `change_id` IN (" + changeIdSetStr + ")";
                List<Map<String, Object>> change_auto_start = processTaskDataMapper.getList(sql);
                put(resultObj, "change_auto_start", change_auto_start);
            }
        }

        {
            // 变更处理
            String sql = "SELECT * FROM `processtask_step_change_handle` WHERE `processtask_id` = " + processTaskId;
            List<Map<String, Object>> processtask_step_change_handle = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_step_change_handle", processtask_step_change_handle);
        }

        {
            //`processtask_content`
            if (CollectionUtils.isNotEmpty(contentHashSet)) {
                String contentHashSetStr = String.join("','", contentHashSet);
                String sql = "SELECT * FROM `processtask_content` WHERE `hash` IN ('" + contentHashSetStr + "')";
                List<Map<String, Object>> processtask_content = processTaskDataMapper.getList(sql);
                put(resultObj, "processtask_content", processtask_content);
            }
        }

        {
            //文件列表
            if (CollectionUtils.isNotEmpty(fileIdSet)) {
                String fileIdSetStr = String.join(",", fileIdSet);
                String sql = "SELECT * FROM `file` WHERE `id` IN (" + fileIdSetStr + ")";
                List<Map<String, Object>> file = processTaskDataMapper.getList(sql);
                put(resultObj, "file", file);
            }
        }

        {
            // 节点管理
            String sql = "SELECT * FROM `process_step_handler`";
            List<Map<String, Object>> process_step_handler = processTaskDataMapper.getList(sql);
            put(resultObj, "process_step_handler", process_step_handler);
        }

        {
            // 个人设置-任务授权
            String sql = "SELECT * FROM `processtask_agent`";
            List<Map<String, Object>> processtask_agent = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_agent", processtask_agent);

            sql = "SELECT * FROM `processtask_agent_target`";
            List<Map<String, Object>> processtask_agent_target = processTaskDataMapper.getList(sql);
            put(resultObj, "processtask_agent_target", processtask_agent_target);
        }
        return resultObj;
    }

    private void addNotNullElement(Set set, Object obj) {
        if (obj == null) {
            return;
        }
        set.add(obj);
    }

    private void put(Map map, String key, Object value) {
        if (map.containsKey(key)) {
            ((ArrayList) map.computeIfAbsent("repeatKey", e -> new ArrayList<String>())).add(key);
            return;
        }
        map.put(key, value);
    }
}
