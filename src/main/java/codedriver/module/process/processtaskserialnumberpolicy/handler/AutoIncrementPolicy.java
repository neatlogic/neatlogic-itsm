package codedriver.module.process.processtaskserialnumberpolicy.handler;

import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskSerialNumberMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import codedriver.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import codedriver.module.process.service.ProcessTaskSerialnumberService;
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
    private ProcessTaskSerialnumberService processTaskSerialnumberService;

    @Override
    public String getName() {
        return "自增序列";
    }

    @SuppressWarnings("serial")
    @Override
    public JSONArray makeupFormAttributeList() {
        return processTaskSerialnumberService.makeupFormAttributeList(5, 8);
    }

    @Override
    public JSONObject makeupConfig(JSONObject jsonObj) {
        return processTaskSerialnumberService.makeupConfig(jsonObj, 0);
    }

    @Override
    public String genarate(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        return processTaskSerialnumberService.genarate(processTaskSerialNumberPolicyVo, null);
    }

    @Override
    public int batchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        try {
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
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            processTaskSerialNumberMapper.updateProcessTaskSerialNumberPolicyEndTimeByChannelTypeUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        }
        return 0;
    }

    @Override
    public Long calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        return processTaskSerialnumberService.calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(processTaskSerialNumberPolicyVo, false);
    }
}
