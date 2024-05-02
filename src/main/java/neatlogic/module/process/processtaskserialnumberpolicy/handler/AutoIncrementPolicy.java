package neatlogic.module.process.processtaskserialnumberpolicy.handler;

import neatlogic.framework.common.util.PageUtil;
import neatlogic.module.process.dao.mapper.catalog.ChannelTypeMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskMapper;
import neatlogic.module.process.dao.mapper.processtask.ProcessTaskSerialNumberMapper;
import neatlogic.framework.process.dto.ChannelTypeVo;
import neatlogic.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import neatlogic.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import neatlogic.module.process.service.ProcessTaskSerialNumberService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AutoIncrementPolicy implements IProcessTaskSerialNumberPolicyHandler {

    private static Logger logger = LoggerFactory.getLogger(AutoIncrementPolicy.class);
    @Resource
    private ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ChannelTypeMapper channelTypeMapper;

    @Resource
    private ProcessTaskSerialNumberService processTaskSerialNumberService;

    @Override
    public String getName() {
        return "自增序列";
    }

    @SuppressWarnings("serial")
    @Override
    public JSONArray makeupFormAttributeList() {
        return processTaskSerialNumberService.makeupFormAttributeList(5, 8);
    }

    @Override
    public JSONObject makeupConfig(JSONObject jsonObj) {
        return processTaskSerialNumberService.makeupConfig(jsonObj, 0);
    }

    @Override
    public String genarate(String channelTypeUuid) {
        return processTaskSerialNumberService.genarate(channelTypeUuid, null);
    }

    @Override
    public int batchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        ProcessTaskVo processTaskVo = new ProcessTaskVo();
        processTaskVo.setChannelTypeUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        if (channelTypeVo == null) {
            throw new ChannelTypeNotFoundException(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        }
        int rowNum = processTaskMapper.getProcessTaskCountByChannelTypeUuidAndStartTime(processTaskVo);
        if (rowNum > 0) {
            int numberOfDigits = processTaskSerialNumberPolicyVo.getConfig().getIntValue("numberOfDigits");
            long max = (long) Math.pow(10, numberOfDigits) - 1;
            long startValue = processTaskSerialNumberPolicyVo.getConfig().getLongValue("startValue");
            processTaskVo.setPageSize(100);
            int pageCount = PageUtil.getPageCount(rowNum, processTaskVo.getPageSize());
            for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
                processTaskVo.setCurrentPage(currentPage);
                List<ProcessTaskVo> processTaskList =
                        processTaskMapper.getProcessTaskListByChannelTypeUuidAndStartTime(processTaskVo);
                for (ProcessTaskVo processTask : processTaskList) {
                    String serialNumber = channelTypeVo.getPrefix() + String.format("%0" + numberOfDigits + "d", startValue);
                    processTaskMapper.updateProcessTaskSerialNumberById(processTask.getId(), serialNumber);
                    processTaskSerialNumberMapper.insertProcessTaskSerialNumber(processTask.getId(), serialNumber);
                    startValue++;
                    if (startValue > max) {
                        startValue -= max;
                    }
                }
            }
        }
        return rowNum;
    }

    @Override
    public Long calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        return processTaskSerialNumberService.calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(processTaskSerialNumberPolicyVo, false);
    }
}
