package codedriver.module.process.workcenter.action;

import java.util.Comparator;

import org.apache.commons.collections4.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.dto.ProcessTaskVo;

public class WorkcenterActionBuilder {

    private JSONArray actionArray = new JSONArray();

    public JSONArray build() {
        actionArray.sort(Comparator.comparing(obj -> ((JSONObject)obj).getInteger("sort")));
        return actionArray;
    }

    public WorkcenterActionBuilder setHandleAction(JSONArray handleArray) {
        int isEnable = 0;
        if (CollectionUtils.isNotEmpty(handleArray)) {
            isEnable = 1;
        }
        JSONObject actionJson = new WorkcenterBuildActionBuilder().setAction(ProcessTaskOperationType.WORKCURRENTSTEP).setSort(1)
            .setIsEnable(isEnable).setHandleArray(handleArray).build();
        actionArray.add(actionJson);
        return this;
    }

    public WorkcenterActionBuilder setAbortRecoverAction(Boolean isHasAbort, Boolean isHasRecover, ProcessTaskVo processTaskVo) {
        JSONObject actionJson = null;
        JSONObject configJson = new JSONObject();
        if (isHasRecover) {
            configJson.put("taskid", processTaskVo.getId());
            configJson.put("interfaceurl", "api/rest/processtask/recover?processTaskId=" + processTaskVo.getId());
            actionJson = new WorkcenterBuildActionBuilder().setAction(ProcessTaskOperationType.RECOVER).setSort(2).setConfig(configJson).setIsEnable(1).build();
            actionArray.add(actionJson);
        } else {
            if (isHasAbort) {
                configJson.put("taskid", processTaskVo.getId());
                configJson.put("interfaceurl", "api/rest/processtask/abort?processTaskId=" + processTaskVo.getId());
                actionJson = new WorkcenterBuildActionBuilder().setAction(ProcessTaskOperationType.ABORTPROCESSTASK).setSort(2).setConfig(configJson).setIsEnable(1).build();
            } else {
                actionJson = new WorkcenterBuildActionBuilder().setAction(ProcessTaskOperationType.ABORTPROCESSTASK).setSort(2).setIsEnable(0).build();
            }
        }
        actionArray.add(actionJson);
        return this;
    }

    public WorkcenterActionBuilder setUrgeAction(Boolean isHasUrge, ProcessTaskVo processTaskVo) {
        JSONObject actionJson = null;
        if (isHasUrge) {
            JSONObject configJson = new JSONObject();
            configJson.put("taskid", processTaskVo.getId());
            configJson.put("interfaceurl", "api/rest/processtask/urge?processTaskId=" + processTaskVo.getId());
            actionJson = new WorkcenterBuildActionBuilder().setAction(ProcessTaskOperationType.URGE).setSort(3)
                .setConfig(configJson).setIsEnable(1).build();
        } else {
            actionJson = new WorkcenterBuildActionBuilder().setAction(ProcessTaskOperationType.URGE).setSort(3).setIsEnable(0).build();
        }
        actionArray.add(actionJson);
        return this;
    }
    
    public WorkcenterActionBuilder setShowHideAction(ProcessTaskVo processTaskVo) {
        if(processTaskVo.getParamObj().getBoolean("isHasProcessTaskAuth")) {
            int isShowParam = 1;
            ProcessTaskOperationType type = ProcessTaskOperationType.SHOW;
            if(processTaskVo.getIsShow() == 1) {
                type = ProcessTaskOperationType.HIDE;
                isShowParam = 0;
            }
            JSONObject configJson = new JSONObject();
            configJson.put("taskid", processTaskVo.getId());
            configJson.put("interfaceurl", String.format("api/rest/processtask/show/hide?processTaskId=%s&isShow=%d" , processTaskVo.getId(),isShowParam));
            JSONObject actionJson = new WorkcenterBuildActionBuilder().setAction(type).setSort(4).setIsEnable(1).setConfig(configJson).build();
            actionArray.add(actionJson);
        }
        return this;
    }
    
    public WorkcenterActionBuilder setDeleteAction(ProcessTaskVo processTaskVo) {
        if(processTaskVo.getParamObj().getBoolean("isHasProcessTaskAuth")) {
            JSONObject configJson = new JSONObject();
            configJson.put("taskid", processTaskVo.getId());
            configJson.put("interfaceurl", String.format("api/rest/processtask/delete?processTaskId=%s" , processTaskVo.getId()));
            JSONObject actionJson = new WorkcenterBuildActionBuilder().setAction(ProcessTaskOperationType.DELETE).setSort(5).setIsEnable(1).setConfig(configJson).build();
            actionArray.add(actionJson);
        }
        return this;
    }
}
