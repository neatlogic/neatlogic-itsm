/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.process.service;

import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.dao.mapper.ChannelTypeMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskMapper;
import neatlogic.framework.process.dao.mapper.ProcessTaskSerialNumberMapper;
import neatlogic.framework.process.dto.ChannelTypeVo;
import neatlogic.framework.process.dto.ProcessTaskSerialNumberPolicyVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.channeltype.ChannelTypeNotFoundException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DateFormat;
import java.util.*;
import java.util.function.Function;

@Service
public class ProcessTaskSerialNumberServiceImpl implements ProcessTaskSerialNumberService {

    static Logger logger = LoggerFactory.getLogger(ProcessTaskSerialNumberServiceImpl.class);

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
    public JSONObject makeupConfig(JSONObject jsonObj, int prefixDigits) {
        JSONObject resultObj = new JSONObject();
        Long startValue = jsonObj.getLong("startValue");
        if (startValue == null) {
            startValue = 0L;
        }
        resultObj.put("startValue", startValue);
        Integer digits = jsonObj.getInteger("digits");
        if (digits != null) {
            resultObj.put("digits", digits);
            resultObj.put("numberOfDigits", digits - prefixDigits);
        }
        return resultObj;
    }

    @Override
    public String genarate(String channelTypeUuid, DateFormat dateFormat) {
        Function<ProcessTaskSerialNumberPolicyVo, Long> function = (serialNumberPolicyVo) -> {
            int numberOfDigits = serialNumberPolicyVo.getConfig().getIntValue("numberOfDigits");
            long max = (long) Math.pow(10, numberOfDigits) - 1;
            long serialNumberSeed = serialNumberPolicyVo.getSerialNumberSeed();
            if (serialNumberSeed > max) {
                serialNumberSeed -= max;
            }
            return serialNumberSeed + 1;
        };
        Long serialNumberSeed = updateProcessTaskSerialNumberPolicySerialNumberSeedByChannelTypeUuid(channelTypeUuid, function);
        ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo = processTaskSerialNumberMapper.getProcessTaskSerialNumberPolicyByChannelTypeUuid(channelTypeUuid);
        int numberOfDigits = processTaskSerialNumberPolicyVo.getConfig().getIntValue("numberOfDigits");
        ChannelTypeVo channelTypeVo = channelTypeMapper.getChannelTypeByUuid(channelTypeUuid);
        if (channelTypeVo == null) {
            throw new ChannelTypeNotFoundException(channelTypeUuid);
        }
        String prefix = channelTypeVo.getPrefix();
        if (dateFormat != null) {
            prefix += dateFormat.format(new Date());
        }
        return prefix + String.format("%0" + numberOfDigits + "d", serialNumberSeed - 1);
    }

    @Override
    public int batchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo, DateFormat dateFormat) {
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
            processTaskVo.setPageSize(100);
            int pageCount = PageUtil.getPageCount(rowNum, processTaskVo.getPageSize());
            Map<String, Long> serNumMap = new HashMap<>();
            for (int currentPage = 1; currentPage <= pageCount; currentPage++) {
                processTaskVo.setCurrentPage(currentPage);
                List<ProcessTaskVo> processTaskList =
                        processTaskMapper.getProcessTaskListByChannelTypeUuidAndStartTime(processTaskVo);
                for (ProcessTaskVo processTask : processTaskList) {
                    String startTimeFormat = dateFormat.format(processTask.getStartTime());
                    serialNumberSeed = serNumMap.getOrDefault(startTimeFormat, startValue);
                    String serialNumber = channelTypeVo.getPrefix() + startTimeFormat + String.format("%0" + numberOfDigits + "d", serialNumberSeed);
                    processTaskMapper.updateProcessTaskSerialNumberById(processTask.getId(), serialNumber);
                    processTaskSerialNumberMapper.insertProcessTaskSerialNumber(processTask.getId(), serialNumber);
                    serialNumberSeed++;
                    if (serialNumberSeed > max) {
                        serialNumberSeed -= max;
                    }
                    serNumMap.put(startTimeFormat, serialNumberSeed);
                }
            }
        }
        return rowNum;
    }

    @Override
    public Long calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo, boolean startTimeLimit) {
        return calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(processTaskSerialNumberPolicyVo, startTimeLimit, null);
    }

    @Override
    public Long calculateSerialNumberSeedAfterBatchUpdateHistoryProcessTask(ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo, boolean startTimeLimit, Date startDate) {
        int numberOfDigits = processTaskSerialNumberPolicyVo.getConfig().getIntValue("numberOfDigits");
        long max = (long) Math.pow(10, numberOfDigits) - 1;
        long startValue = processTaskSerialNumberPolicyVo.getConfig().getLongValue("startValue");
        ProcessTaskVo processTaskVo = new ProcessTaskVo();
        processTaskVo.setChannelTypeUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid());
        if (startTimeLimit) {
            processTaskVo.setStartTime(startDate);
        }
        int rowNum = processTaskMapper.getProcessTaskCountByChannelTypeUuidAndStartTime(processTaskVo);
        rowNum += startValue;
        return rowNum % max;
    }

    @Override
    public void serialNumberSeedReset(String handlerClassName) {
        List<ProcessTaskSerialNumberPolicyVo> processTaskSerialNumberPolicyList =
                processTaskSerialNumberMapper.getProcessTaskSerialNumberPolicyListByHandler(handlerClassName);
        for (ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo : processTaskSerialNumberPolicyList) {
            Function<ProcessTaskSerialNumberPolicyVo, Long> function = (serialNumberPolicyVo) -> {
                Long serialNumberSeed = 1L;
                Long startValue = serialNumberPolicyVo.getConfig().getLong("startValue");
                if (startValue != null) {
                    serialNumberSeed = startValue;
                }
                return serialNumberSeed;
            };
            updateProcessTaskSerialNumberPolicySerialNumberSeedByChannelTypeUuid(processTaskSerialNumberPolicyVo.getChannelTypeUuid(), function);
        }
    }

    @Override
    public Long updateProcessTaskSerialNumberPolicySerialNumberSeedByChannelTypeUuid(String channelTypeUuid, Function<ProcessTaskSerialNumberPolicyVo, Long> function) {
        ProcessTaskSerialNumberPolicyVo processTaskSerialNumberPolicyVo = processTaskSerialNumberMapper.getProcessTaskSerialNumberPolicyLockByChannelTypeUuid(channelTypeUuid);
        Long serialNumberSeed = function.apply(processTaskSerialNumberPolicyVo);
        processTaskSerialNumberMapper.updateProcessTaskSerialNumberPolicySerialNumberSeedByChannelTypeUuid(channelTypeUuid, serialNumberSeed);
        return serialNumberSeed;
    }
}
