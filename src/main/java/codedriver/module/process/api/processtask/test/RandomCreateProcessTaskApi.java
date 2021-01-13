/*
 * Copyright (c)  2020 TechSure Co.,Ltd.  All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.processtask.test;

import codedriver.framework.asynchronization.thread.CodeDriverThread;
import codedriver.framework.asynchronization.threadpool.CommonThreadPool;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.NO_AUTH;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.dao.mapper.UserMapper;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.dao.mapper.ChannelMapper;
import codedriver.framework.process.dao.mapper.PriorityMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.module.process.api.processtask.ProcessTaskCompleteApi;
import codedriver.module.process.api.processtask.ProcessTaskDraftSaveApi;
import codedriver.module.process.api.processtask.ProcessTaskStartProcessApi;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @Title: RandomCreateProcessTaskApi
 * @Package processtask
 * @Description: 1、create:随机获取服务、用户、优先级创建工单
 * 2、execute:随机执行工单步骤(因为异步原因，在create后需延迟50s后再执行工单)
 * @Author: 89770
 * @Date: 2020/12/28 10:49
 **/
@Service
@OperationType(type = OperationTypeEnum.OPERATE)
@AuthAction(action = NO_AUTH.class)
class RandomCreateProcessTaskApi extends PrivateApiComponentBase {
    private final Map<String, Action<JSONObject>> actionMap = new HashMap<>();
    CountDownLatch latch = null;
    @Autowired
    ProcessTaskMapper processtaskMapper;

    @Autowired
    ChannelMapper channelMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    PriorityMapper priorityMapper;

    @PostConstruct
    public void actionDispatcherInit() {
        /*
         * @Description: 随机获取服务、用户、优先级创建工单
         * @Author: 89770
         * @Date: 2020/12/28 11:22
         */
        actionMap.put("create", (jsonParam) -> {
            int unitCount = 100000;
            JSONObject paramJson = new JSONObject();
            Integer count = jsonParam.getInteger("count");
            int latchCount = count / unitCount;
            if (count % unitCount > 0) {
                latchCount++;
            }
            latch = new CountDownLatch(latchCount);
            int startIndex = 0;
            int endIndex = startIndex + unitCount;
            for (int i = 0; i < latchCount; i++) {
                if (latchCount > 1) {
                    startIndex = i * unitCount + 1;
                }
                endIndex = startIndex + unitCount;
                if (latchCount == 1 || startIndex + unitCount > count) {
                    endIndex = count;
                }
                MyCreateThread thread = new MyCreateThread(startIndex, endIndex);
                CommonThreadPool.execute(thread);
            }
            try {
                latch.await(); // 主线程等待
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        /*
         * @Description: 随机执行工单步骤(因为异步原因，在create后需延迟50s后再执行工单)
         * @Author: 89770
         * @Date: 2020/12/31 11:22
         */
        actionMap.put("execute", (jsonParam) -> {
            Integer count = jsonParam.getInteger("count");
            List<ProcessTaskVo> processTaskVoList = processtaskMapper.getProcessTaskByStatusList(Collections.singletonList(ProcessTaskStatus.RUNNING.getValue()), count);
            List<Long> taskIdList = processTaskVoList.stream().map(ProcessTaskVo::getId).collect(Collectors.toList());
            List<ProcessTaskStepUserVo> stepUserVoList = processtaskMapper.getProcessTaskStepUserListByProcessTaskIdListAndStatusList(taskIdList, Collections.singletonList(ProcessTaskStatus.RUNNING.getValue()));
            List<Long> stepIdList = stepUserVoList.stream().map(ProcessTaskStepUserVo::getProcessTaskStepId).collect(Collectors.toList());
            ProcessTaskCompleteApi completeProcessApi = (ProcessTaskCompleteApi) PrivateApiComponentFactory.getInstance(ProcessTaskCompleteApi.class.getName());
            Map<String, Long> processTaskNextStepMap = new HashMap<>();
            List<ProcessTaskStepRelVo> stepRelVoList = processtaskMapper.getProcessTaskStepRelListByFromIdList(stepIdList);
            for (ProcessTaskStepRelVo stepRelVo : stepRelVoList) {
                processTaskNextStepMap.put(String.format("%s_%s", stepRelVo.getProcessTaskId(), stepRelVo.getFromProcessTaskStepId()), stepRelVo.getToProcessTaskStepId());
            }
            for (ProcessTaskStepUserVo stepUserVo : stepUserVoList) {
                JSONObject completeJson = new JSONObject();
                completeJson.put("processTaskId", stepUserVo.getProcessTaskId());
                completeJson.put("processTaskStepId", stepUserVo.getProcessTaskStepId());
                completeJson.put("action", "complete");
                completeJson.put("nextStepId", processTaskNextStepMap.get(String.format("%s_%s", stepUserVo.getProcessTaskId(), stepUserVo.getProcessTaskStepId())));
                completeProcessApi.doService(PrivateApiComponentFactory.getApiByToken(completeProcessApi.getToken()), completeJson);
            }
        });
    }

    @Override
    public String getName() {
        return "随机创建|执行工单";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    @Input({
            @Param(name = "count", type = ApiParamType.INTEGER, desc = "需要随机创建|执行工单数", isRequired = true),
            @Param(name = "type", type = ApiParamType.STRING, desc = "create:创建工单，execute:执行工单(因为异步原因，在create后需延迟50s后再执行工单)", isRequired = true)
    })
    public Object myDoService(JSONObject paramJson) throws Exception {
        String type = paramJson.getString("type");
        actionMap.get(type).execute(paramJson);
        return null;
    }

    @Override
    public String getToken() {
        return "processtask/randomCreateProcessTask";
    }

    @FunctionalInterface
    public interface Action<T> {
        void execute(T t) throws Exception;
    }

    class MyCreateThread extends CodeDriverThread {
        private final Integer startIndex;
        private final Integer endIndex;

        public MyCreateThread(Integer startIndex,Integer endIndex) {
            super();
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }
        @SuppressWarnings("unchecked")
        public void execute() {
            JSONObject paramJson = new JSONObject();
            ChannelVo channelVo = new ChannelVo();
            channelVo.setNeedPage(false);
            List<ValueTextVo> channelKeyValueList = channelMapper.searchChannelListForSelect(channelVo);
            UserVo user = new UserVo();
            user.setNeedPage(true);
            user.setStartNum(0);
            user.setPageSize(1000);
            List<UserVo> userList = userMapper.searchUserForSelect(new UserVo());
            List<ValueTextVo> priorityKeyValueList = priorityMapper.searchPriorityListForSelect(new PriorityVo());
            for (int i = startIndex; i < endIndex; i++) {

                try {
                    int randomChannelIndex = (int) Math.round(Math.random() * (channelKeyValueList.size()-1));
                    int randomUserIndex = (int) Math.round(Math.random() * (userList.size()-1));
                    int randomPriorityIndex = (int) Math.round(Math.random() * (priorityKeyValueList.size()-1));
                    ValueTextVo channelValueText = channelKeyValueList.get(randomChannelIndex);
                    UserVo ownerVo = userList.get(randomUserIndex);
                    ValueTextVo priorityValueText = priorityKeyValueList.get(randomPriorityIndex);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    paramJson.put("channelUuid", channelValueText.getValue());
                    paramJson.put("title", String.format("%s 上报了 服务为 '%s' ,优先级为 '%s' 的工单", ownerVo.getUserName(), channelValueText.getText(), priorityValueText.getText()));
                    paramJson.put("owner", ownerVo.getUuid());
                    paramJson.put("priorityUuid", priorityValueText.getValue());
                    int startContentIndex = (int) Math.round(Math.random() * (Text.text.length()-1));
                    int endContentIndex = (int) Math.round(Math.random() * (Text.text.length()-1));
                    if(startContentIndex > endContentIndex){
                        int tmpIndex = startContentIndex ;
                        startContentIndex = endContentIndex;
                        endContentIndex = tmpIndex;
                    }
                    paramJson.put("content", Text.text.substring(startContentIndex,endContentIndex));
                    //暂存
                    paramJson.put("isNeedValid", 1);
                    paramJson.put("hidecomponentList", new JSONArray());
                    paramJson.put("readcomponentList", new JSONArray());

                    ProcessTaskDraftSaveApi draftSaveApi = (ProcessTaskDraftSaveApi) PrivateApiComponentFactory.getInstance(ProcessTaskDraftSaveApi.class.getName());
                    JSONObject saveResultObj = JSONObject.parseObject(draftSaveApi.doService(PrivateApiComponentFactory.getApiByToken(draftSaveApi.getToken()), paramJson).toString());
                    saveResultObj.put("action", "start");
                    //查询可执行下一步骤
                    List<ProcessTaskStepVo> nextStepList =  processtaskMapper.getToProcessTaskStepByFromIdAndType(saveResultObj.getLong("processTaskStepId"), null);
                    saveResultObj.put("nextStepId", nextStepList.get((int) Math.round(Math.random() * (nextStepList.size()-1))).getId());
                    //流转
                    ProcessTaskStartProcessApi startProcessApi = (ProcessTaskStartProcessApi) PrivateApiComponentFactory.getInstance(ProcessTaskStartProcessApi.class.getName());
                    startProcessApi.doService(PrivateApiComponentFactory.getApiByToken(startProcessApi.getToken()), saveResultObj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            latch.countDown(); // 执行完毕，计数器减1

        }
    }
}
