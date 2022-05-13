package codedriver.module.process.processtaskserialnumberpolicy.handler;

import codedriver.framework.common.dto.ValueTextVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.dao.mapper.ChannelTypeMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.ProcessTaskSerialNumberMapper;
import codedriver.framework.process.dto.ChannelTypeVo;
import codedriver.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import codedriver.framework.process.processtaskserialnumberpolicy.core.IProcessTaskSerialNumberPolicyHandler;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AutoIncrementPolicy implements IProcessTaskSerialNumberPolicyHandler {

    private static Logger logger = LoggerFactory.getLogger(AutoIncrementPolicy.class);
    @Autowired
    private ProcessTaskSerialNumberMapper processTaskSerialNumberMapper;

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private ChannelTypeMapper channelTypeMapper;

    @Override
    public String getName() {
        return "自增序列";
    }

    @SuppressWarnings("serial")
    @Override
    public JSONArray makeupFormAttributeList() {
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
            jsonObj.put("dataList", new ArrayList<ValueTextVo>() {
                {
                    this.add(new ValueTextVo(5, "5"));
                    this.add(new ValueTextVo(6, "6"));
                    this.add(new ValueTextVo(7, "7"));
                    this.add(new ValueTextVo(8, "8"));
                }
            });
            resultArray.add(jsonObj);
        }
        return resultArray;
    }

    @Override
    public JSONObject makeupConfig(JSONObject jsonObj) {
        JSONObject resultObj = new JSONObject();
        Long startValue = jsonObj.getLong("startValue");
        if (startValue == null) {
            startValue = 0L;
        }
        resultObj.put("startValue", startValue);
        Integer digits = jsonObj.getInteger("digits");
        if (digits != null) {
            resultObj.put("digits", digits);
            resultObj.put("numberOfDigits", digits);
        }
        return resultObj;
    }

    @Override
    public String genarate(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo) {
        processTaskSerialNumberPolicyVo = processTaskSerialNumberMapper.getProcessTaskSerialNumberPolicyLockByChannelTypeUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        int numberOfDigits = processTaskSerialNumberPolicyVo.getConfig().getIntValue("numberOfDigits");
        long max = (long) Math.pow(10, numberOfDigits) - 1;
        long serialNumberSeed = processTaskSerialNumberPolicyVo.getSerialNumberSeed();
        if (serialNumberSeed > max) {
            serialNumberSeed -= max;
        }
        processTaskSerialNumberMapper.updateProcessTaskSerialNumberPolicySerialNumberSeedByChannelTypeUuid(
                processTaskSerialNumberPolicyVo.getChannelTypeUuid(), serialNumberSeed + 1);
        return String.format("%0" + numberOfDigits + "d", serialNumberSeed);
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
        int numberOfDigits = processTaskSerialNumberPolicyVo.getConfig().getIntValue("numberOfDigits");
        long max = (long) Math.pow(10, numberOfDigits) - 1;
        long startValue = processTaskSerialNumberPolicyVo.getConfig().getLongValue("startValue");
        ProcessTaskVo processTaskVo = new ProcessTaskVo();
        processTaskVo.setChannelTypeUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        int rowNum = processTaskMapper.getProcessTaskCountByChannelTypeUuidAndStartTime(processTaskVo);
        rowNum += startValue;
        return rowNum % max;
    }
}
