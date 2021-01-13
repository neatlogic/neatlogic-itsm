package codedriver.module.process.api.processtask;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.auth.label.NO_AUTH;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentFactory;
import codedriver.framework.restful.core.publicapi.PublicApiComponentBase;
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = NO_AUTH.class)
public class ProcessTaskCreatePublicApi extends PublicApiComponentBase {

  
	@Override
	public String getName() {
		return "上报工单(供第三方使用)";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@SuppressWarnings("unchecked")
    @Input({
        @Param(name="channelUuid", type= ApiParamType.STRING, isRequired=true, desc="服务uuid"),
        @Param(name="title", type=ApiParamType.STRING, isRequired=true, maxLength = 80, desc = "标题"),
        @Param(name="owner", type=ApiParamType.STRING, isRequired=true, desc="请求人"),
        @Param(name="reporter", type=ApiParamType.STRING, desc="代报人"),
        @Param(name="priorityUuid", type=ApiParamType.STRING, isRequired=true, desc="优先级uuid"),
        @Param(name="formAttributeDataList", type = ApiParamType.JSONARRAY, desc = "表单属性数据列表"),
        @Param(name="hidecomponentList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "隐藏表单属性列表"),
        @Param(name="readcomponentList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "只读表单属性列表"),
        @Param(name="content", type=ApiParamType.STRING, desc = "描述"),
        @Param(name="fileIdList", type=ApiParamType.JSONARRAY, desc = "附件id列表"),
        @Param(name="handlerStepInfo", type=ApiParamType.JSONOBJECT, desc="处理器特有的步骤信息")
	})
    @Output({
        @Param(name="processTaskId", type = ApiParamType.LONG, desc="工单id")
    })
	@Description(desc = "上报工单(供第三方使用)")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
	    JSONObject result = new JSONObject();
	    
	    //暂存
	    jsonObj.put("isNeedValid", 1);
	    ProcessTaskDraftSaveApi drafSaveApi = (ProcessTaskDraftSaveApi)PrivateApiComponentFactory.getInstance(ProcessTaskDraftSaveApi.class.getName());
	    JSONObject saveResultObj =  JSONObject.parseObject(drafSaveApi.doService(PrivateApiComponentFactory.getApiByToken(drafSaveApi.getToken()), jsonObj).toString());
	    saveResultObj.put("action", "start");
	    
	    //查询可执行下一步骤
	    ProcessTaskProcessableStepList stepListApi  = (ProcessTaskProcessableStepList)PrivateApiComponentFactory.getInstance(ProcessTaskProcessableStepList.class.getName());
	    Object nextStepListObj = stepListApi.doService(PrivateApiComponentFactory.getApiByToken(stepListApi.getToken()),saveResultObj);
	    List<ProcessTaskStepVo> nextStepList  =  (List<ProcessTaskStepVo>)nextStepListObj;
	    if(CollectionUtils.isEmpty(nextStepList) && nextStepList.size() != 1) {
	        throw new RuntimeException("抱歉！暂不支持开始节点连接多个后续节点。");
	    } 
	    saveResultObj.put("nextStepId", nextStepList.get(0).getId());
	    
	    //流转
	    ProcessTaskStartProcessApi startProcessApi  = (ProcessTaskStartProcessApi)PrivateApiComponentFactory.getInstance(ProcessTaskStartProcessApi.class.getName());
	    startProcessApi.doService(PrivateApiComponentFactory.getApiByToken(startProcessApi.getToken()),saveResultObj);
        
        result.put("processTaskId", saveResultObj.getString("processTaskId"));
		return result;
	}

}
