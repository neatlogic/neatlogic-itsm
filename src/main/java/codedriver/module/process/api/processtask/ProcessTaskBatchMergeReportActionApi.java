/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.notify.core.INotifyHandler;
import codedriver.framework.notify.core.NotifyHandlerFactory;
import codedriver.framework.notify.dto.NotifyVo;
import codedriver.framework.notify.exception.NotifyHandlerNotFoundException;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskRelationVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channeltype.ChannelTypeRelationNotFoundException;
import codedriver.framework.process.exception.processtask.ProcessTaskNotFoundException;
import codedriver.framework.process.service.ProcessTaskService;
import codedriver.framework.process.stephandler.core.IProcessStepHandlerUtil;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.service.AuthenticationInfoService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author linbq
 * @since 2021/8/11 11:55
 **/
@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class ProcessTaskBatchMergeReportActionApi extends PrivateApiComponentBase {

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ChannelTypeMapper channelTypeMapper;

    @Resource
    private IProcessStepHandlerUtil processStepHandlerUtil;

    @Resource
    private AuthenticationInfoService authenticationInfoService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private ProcessTaskService processTaskService;

    @Override
    public String getToken() {
        return "suzhoubank/processtask/batchmergereport/action";
    }

    @Override
    public String getName() {
        return "工单批量合并上报触发动作";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "processTaskId", type = ApiParamType.LONG, isRequired = true, desc = "工单id")
    })
    @Description(desc = "工单批量合并上报触发动作")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        INotifyHandler handler = NotifyHandlerFactory.getHandler("EmailNotifyHandler");
        if (handler == null) {
            throw new NotifyHandlerNotFoundException("EmailNotifyHandler");
        }
        Long processTaskId = paramObj.getLong("processTaskId");
        ProcessTaskVo processTaskVo = processTaskMapper.getProcessTaskBaseInfoById(processTaskId);
        if (processTaskVo == null) {
            throw new ProcessTaskNotFoundException(processTaskId.toString());
        }
        /**
         * 定制接口：对以上通过的子工单，实现关联到当前这个父工单。
         * 定制通知策略：对以上通过的子工单，发送通知：【子工单标题】审批通过。没通过的工单，发送通知：【子工单标题】审批没有通过。
         */

        //从工单表单数据中获取通过与不通过的数据
        List<Long> checkedProcessTaskIdList = new ArrayList<>();
        List<Long> uncheckedProcessTaskIdList = new ArrayList<>();
        List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataVoList = processTaskMapper.getProcessTaskStepFormAttributeDataByProcessTaskId(processTaskId);
        for (ProcessTaskFormAttributeDataVo attributeDataVo : processTaskFormAttributeDataVoList) {
            if ("custommergeprocess".equals(attributeDataVo.getType())) {
                JSONObject dataObj = (JSONObject) attributeDataVo.getDataObj();
                if (MapUtils.isNotEmpty(dataObj)) {
                    JSONArray selectArray = dataObj.getJSONArray("selectList");
                    if (CollectionUtils.isNotEmpty(selectArray)) {
                        checkedProcessTaskIdList = selectArray.toJavaList(Long.class);
                    }
                    JSONArray unSelectArray = dataObj.getJSONArray("unSelectList");
                    if (CollectionUtils.isNotEmpty(unSelectArray)) {
                        uncheckedProcessTaskIdList = unSelectArray.toJavaList(Long.class);
                    }
                }
//                Object dataObj = attributeDataVo.getDataObj();
//                if (dataObj != null) {
//                    JSONArray dataArray = (JSONArray) dataObj;
//                    for (int i = 0; i < dataArray.size(); i++) {
//                        JSONObject data = dataArray.getJSONObject(i);
//                        if (MapUtils.isNotEmpty(data)) {
//                            Long id = data.getLong("id");
//                            if (id != null) {
//                                Integer checked = data.getInteger("checked");
//                                if (Objects.equals(checked, 1)) {
//                                    checkedProcessTaskIdList.add(id);
//                                } else {
//                                    uncheckedProcessTaskIdList.add(id);
//                                }
//                            }
//                        }
//                    }
//                }
            }
        }
        if (CollectionUtils.isNotEmpty(checkedProcessTaskIdList)) {
            List<ProcessTaskVo> checkedProcessTaskVoList = processTaskMapper.getProcessTaskListByIdList(checkedProcessTaskIdList);
            // 关联工单开始
            String userUuid = UserContext.get().getUserUuid(true);
            AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(userUuid);
            List<String> processUserTypeList = processTaskService.getProcessUserTypeList(processTaskId, authenticationInfoVo);
            String channelUuid = processTaskVo.getChannelUuid();
            List<Long> channelTypeRelationIdList = channelTypeMapper.getAuthorizedChannelTypeRelationIdListBySourceChannelUuid(channelUuid, userUuid, authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList(), processUserTypeList);
            /** 2021-10-11 开晚会时确认用户个人设置任务授权不包括服务上报权限 **/
//            if (CollectionUtils.isEmpty(channelTypeRelationIdList)) {
//                String agentUuid = userMapper.getUserUuidByAgentUuidAndFunc(userUuid, "processtask");
//                if (StringUtils.isNotBlank(agentUuid)) {
//                    AuthenticationInfoVo agentAuthenticationInfoVo = authenticationInfoService.getAuthenticationInfo(agentUuid);
//                    processUserTypeList = processTaskService.getProcessUserTypeList(processTaskId, agentAuthenticationInfoVo);
//                    channelTypeRelationIdList = channelTypeMapper.getAuthorizedChannelTypeRelationIdListBySourceChannelUuid(channelUuid, agentUuid, agentAuthenticationInfoVo.getTeamUuidList(), agentAuthenticationInfoVo.getRoleUuidList(), processUserTypeList);
//
//                }
//            }
            if (CollectionUtils.isNotEmpty(channelTypeRelationIdList)) {
                channelTypeRelationIdList.sort(Long::compareTo);
                Long channelTypeRelationId = channelTypeRelationIdList.get(0);
                if (channelTypeMapper.checkChannelTypeRelationIsExists(channelTypeRelationId) == 0) {
                    throw new ChannelTypeRelationNotFoundException(channelTypeRelationId);
                }
                for (ProcessTaskVo checkedProcessTaskVo : checkedProcessTaskVoList) {
                    ProcessTaskRelationVo processTaskRelationVo = new ProcessTaskRelationVo();
                    processTaskRelationVo.setSource(processTaskId);
                    processTaskRelationVo.setChannelTypeRelationId(channelTypeRelationId);
                    processTaskRelationVo.setTarget(checkedProcessTaskVo.getId());

                    processTaskMapper.replaceProcessTaskRelation(processTaskRelationVo);
                    ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
                    processTaskStepVo.setProcessTaskId(checkedProcessTaskVo.getId());
                    processTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.CHANNELTYPERELATION.getParamName(),
                            channelTypeRelationId);
                    processTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.PROCESSTASKLIST.getParamName(),
                            JSON.toJSONString(Arrays.asList(processTaskId)));
                    processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.RELATION);
                }
                paramObj.put(ProcessTaskAuditDetailType.PROCESSTASKLIST.getParamName(),
                        JSON.toJSONString(checkedProcessTaskIdList));
                ProcessTaskStepVo processTaskStepVo = new ProcessTaskStepVo();
                processTaskStepVo.setProcessTaskId(processTaskId);
                processTaskStepVo.getParamObj().putAll(paramObj);
                processStepHandlerUtil.audit(processTaskStepVo, ProcessTaskAuditType.RELATION);
            }
            //关联工单结束

            //发送通知
            for (ProcessTaskVo checkedProcessTaskVo : checkedProcessTaskVoList) {
                NotifyVo.Builder notifyBuilder = new NotifyVo.Builder();
                notifyBuilder.withTitleTemplate("【" + checkedProcessTaskVo.getTitle() + "】审批通过。")
//                    .withContentTemplate("")
                        .addUserUuid(checkedProcessTaskVo.getOwner());
                NotifyVo notifyVo = notifyBuilder.build();
                handler.execute(notifyVo);
            }
        }
        //发送通知
        if (CollectionUtils.isNotEmpty(uncheckedProcessTaskIdList)) {
            List<ProcessTaskVo> uncheckedProcessTaskVoList = processTaskMapper.getProcessTaskListByIdList(uncheckedProcessTaskIdList);
            for (ProcessTaskVo uncheckedProcessTaskVo : uncheckedProcessTaskVoList) {
                NotifyVo.Builder notifyBuilder = new NotifyVo.Builder();
                notifyBuilder.withTitleTemplate("【" + uncheckedProcessTaskVo.getTitle() + "】审批没有通过。")
//                    .withContentTemplate("")
                        .addUserUuid(uncheckedProcessTaskVo.getOwner());
                NotifyVo notifyVo = notifyBuilder.build();
                handler.execute(notifyVo);
            }
        }
        return null;
    }
}
