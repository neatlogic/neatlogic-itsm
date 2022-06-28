package codedriver.module.process.service;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskSerialNumberMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class ProcessTaskSerialnumberServiceImpl implements ProcessTaskSerialnumberService {

    static Logger logger = LoggerFactory.getLogger(ProcessTaskSerialnumberServiceImpl.class);

    @Resource
    private ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    @Resource
    private ChannelTypeMapper channelTypeMapper;

    @Override
    public JSONArray makeupFormAttributeList(int startDigit, int endDigit) {
        JSONArray resultArray = new JSONArray();
        {
            /** 起始值 **/
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("type", "text");
            jsonObj.put("name", "startValue");
            jsonObj.put("value", "");
            jsonObj.put("defaultValue", 1);
            jsonObj.put("width", 200);
            jsonObj.put("maxlength", 5);
            jsonObj.put("label", "起始位");
            jsonObj.put("validateList", Arrays.asList("required", new JSONObject() {
                {
                    this.put("name", "integer_p");
                    this.put("message", "请输入正整数");
                }
            }));
            jsonObj.put("placeholder", "1-99999");
            resultArray.add(jsonObj);
        }
        {
            /** 位数 **/
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("type", "select");
            jsonObj.put("name", "digits");
            jsonObj.put("value", "");
            jsonObj.put("defaultValue", "");
            jsonObj.put("width", 200);
            jsonObj.put("label", "工单号位数");
            jsonObj.put("maxlength", 5);
            jsonObj.put("validateList", Arrays.asList("required"));
            ArrayList<ValueTextVo> digitList = new ArrayList<>();
            for (int i = startDigit; i <= endDigit; i++) {
                digitList.add(new ValueTextVo(i, String.valueOf(i)));
            }
            jsonObj.put("dataList", digitList);
            resultArray.add(jsonObj);
        }
        return resultArray;
    }

    @Override
    public String genarate(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo, DateFormat dateFormat) {
        processTaskSerialNumberPolicyVo = processTaskSerialNumberMapper.getProcessTaskSerialNumberPolicyLockByChannelTypeUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        int numberOfDigits = processTaskSerialNumberPolicyVo.getConfig().getIntValue("numberOfDigits");
        long max = (long) Math.pow(10, numberOfDigits) - 1;
        long serialNumberSeed = processTaskSerialNumberPolicyVo.getSerialNumberSeed();
        if (serialNumberSeed > max) {
            serialNumberSeed -= max;
        }
        processTaskSerialNumberMapper.updateProcessTaskSerialNumberPolicySerialNumberSeedByChannelTypeUuid(
                processTaskSerialNumberPolicyVo.getChannelTypeUuid(), serialNumberSeed + 1);
        ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        if (channelTypeVo == null) {
            throw new ChannelTypeNotFoundException(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        }
        String number = channelTypeVo.getPrefix();
        if (dateFormat != null) {
            number += dateFormat.format(new Date());
        }
        number += String.format("%0" + numberOfDigits + "d", serialNumberSeed);
        return number;
    }

    @Override
    public int batchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo, DateFormat dateFormat) {
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
                long serialNumberSeed = startValue;
                String timeFormat = null;
                processTaskVo.setPageSize(100);
                int pageCount = PageUtil.getPageCount(rowNum, processTaskVo.getPageSize());
                for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
                    processTaskVo.setCurrentPage(currentPage);
                    List<ProcessTaskVo> processTaskList =
                            processTaskMapper.getProcessTaskListByChannelTypeUuidAndStartTime(processTaskVo);
                    for (ProcessTaskVo processTask : processTaskList) {
                        String startTimeFormat = dateFormat.format(processTask.getStartTime());
                        if (!Objects.equals(timeFormat, startTimeFormat)) {
                            serialNumberSeed = startValue;
                            timeFormat = startTimeFormat;
                        }
                        String serialNumber = channelTypeVo.getPrefix() + startTimeFormat + String.format("%0" + numberOfDigits + "d", serialNumberSeed);
                        processTaskMapper.updateProcessTaskSerialNumberById(processTask.getId(), serialNumber);
                        processTaskSerialNumberMapper.insertProcessTaskSerialNumber(processTask.getId(), serialNumber);
                        serialNumberSeed++;
                        if (serialNumberSeed > max) {
                            serialNumberSeed -= max;
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
    public Long calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo, boolean startTimeLimit) {
        int numberOfDigits = processTaskSerialNumberPolicyVo.getConfig().getIntValue("numberOfDigits");
        long max = (long) Math.pow(10, numberOfDigits) - 1;
        long startValue = processTaskSerialNumberPolicyVo.getConfig().getLongValue("startValue");
        ProcessTaskVo processTaskVo = new ProcessTaskVo();
        processTaskVo.setChannelTypeUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        if (startTimeLimit) {
            processTaskVo.setStartTime(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        int rowNum = processTaskMapper.getProcessTaskCountByChannelTypeUuidAndStartTime(processTaskVo);
        rowNum += startValue;
        return rowNum % max;
    }
}
