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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ProcessTaskCurrentStepWorkerColumn extends ProcessTaskColumnBase implements IProcessTaskColumn {
    @Resource
    private NewWorkcenterService newWorkcenterService;

    @Override
    public String getName() {
        return "currentstepworker";
    }

    @Override
    public String getDisplayName() {
        return "当前步骤处理对象";
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
        return 7;
    }

    @Override
    public String getSimpleValue(ProcessTaskVo processTaskVo) {
        JSONArray workerArray = JSONArray.parseArray(getValue(processTaskVo).toString());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < workerArray.size(); i++) {
            sb.append(workerArray.getJSONObject(i).getJSONObject("workerVo").getString("name")).append(";");
        }
        return sb.toString();
    }

    @Override
    public List<TableSelectColumnVo> getTableSelectColumn() {
        return new ArrayList<>();
    }

    @Override
    public Object getValue(ProcessTaskVo processTaskVo) {
        JSONArray workerArray = new JSONArray();
        List<ProcessTaskStepVo> stepVoList = processTaskVo.getStepList();
        if (Arrays.asList(ProcessTaskStatus.RUNNING.getValue(),ProcessTaskStatus.HANG.getValue()).contains(processTaskVo.getStatus())) {
            for (ProcessTaskStepVo stepVo : stepVoList) {
                //查询其它步骤handler minorList
                newWorkcenterService.getStepTaskWorkerList(workerArray,stepVo);
            }
        }
        return workerArray;
    }

}
