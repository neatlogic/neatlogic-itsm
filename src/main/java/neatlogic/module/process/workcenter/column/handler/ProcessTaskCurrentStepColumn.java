package neatlogic.module.process.workcenter.column.handler;

import neatlogic.framework.process.column.core.IProcessTaskColumn;
import neatlogic.framework.process.column.core.ProcessTaskColumnBase;
import neatlogic.framework.process.constvalue.ProcessFieldType;
import neatlogic.framework.process.constvalue.ProcessTaskStatus;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.workcenter.dto.TableSelectColumnVo;
import neatlogic.module.process.service.NewWorkcenterService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessTaskCurrentStepColumn extends ProcessTaskColumnBase implements IProcessTaskColumn{
	@Resource
	private NewWorkcenterService newWorkcenterService;
	
	@Override
	public String getName() {
		return "currentstep";
	}

	@Override
	public String getDisplayName() {
		return "handler.processtask.column.currentstep";
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
		return 5;
	}

	@Override
	public String getSimpleValue(ProcessTaskVo processTaskVo) {
		List<ProcessTaskStepVo> stepVoList = processTaskVo.getStepList();
		List<String> stepNameList = new ArrayList<>();
		if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
			for (ProcessTaskStepVo stepVo : stepVoList) {
				if (((ProcessTaskStatus.PENDING.getValue().equals(stepVo.getStatus()) && stepVo.getIsActive() == 1) || ProcessTaskStatus.RUNNING.getValue().equals(stepVo.getStatus()))) {
					stepNameList.add(stepVo.getName());
				}
			}
		}
		return String.join(",", stepNameList);
	}

	@Override
	public List<TableSelectColumnVo> getTableSelectColumn() {
		return new ArrayList<>();
	}

	@Override
	public Boolean getMyIsExport() {
        return false;
    }

	@Override
	public Object getValue(ProcessTaskVo processTaskVo) {
		JSONArray currentStepArray = new JSONArray();
		List<ProcessTaskStepVo> stepVoList =  processTaskVo.getStepList();
		if(ProcessTaskStatus.RUNNING.getValue().equals(processTaskVo.getStatus())) {
			for (ProcessTaskStepVo stepVo : stepVoList) {
				if(ProcessTaskStatus.DRAFT.getValue().equals(stepVo.getStatus()) ||
						ProcessTaskStatus.RUNNING.getValue().equals(stepVo.getStatus()) ||
						(ProcessTaskStatus.PENDING.getValue().equals(stepVo.getStatus())&& stepVo.getIsActive() == 1)
				) {
					JSONObject currentStepJson  = new JSONObject();
					currentStepJson.put("name",stepVo.getName());
					JSONObject currentStepStatusJson = new JSONObject();
					currentStepStatusJson.put("name",stepVo.getStatus());
					currentStepStatusJson.put("text", ProcessTaskStatus.getText(stepVo.getStatus()));
					currentStepStatusJson.put("color", ProcessTaskStatus.getColor(stepVo.getStatus()));
					currentStepJson.put("status",currentStepStatusJson);

					//查询其它步骤handler minorList
					JSONArray workerArray = new JSONArray();
					newWorkcenterService.getStepTaskWorkerList(workerArray,stepVo);

					currentStepJson.put("workerList",workerArray);
					currentStepArray.add(currentStepJson);
				}
			}
		}
		return currentStepArray;
	}

}
