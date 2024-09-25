/*
 * Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package neatlogic.module.process.stephandler.regulatehandler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.notify.dto.InvokeNotifyPolicyConfigVo;
import neatlogic.framework.process.dto.processconfig.SlaCalculatePolicyVo;
import neatlogic.framework.process.dto.processconfig.SlaNotifyPolicyVo;
import neatlogic.framework.process.dto.processconfig.SlaTransferPolicyVo;
import neatlogic.framework.process.exception.process.ProcessConfigException;
import neatlogic.framework.process.stephandler.core.IProcessStepInternalHandler;
import neatlogic.framework.process.stephandler.core.IRegulateHandler;
import neatlogic.framework.process.stephandler.core.ProcessMessageManager;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.module.process.notify.handler.SlaNotifyPolicyHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class SlaListRegulateHandler implements IRegulateHandler {

    @Override
    public String getName() {
        return "slaList";
    }

    @Override
    public void regulateConfig(IProcessStepInternalHandler processStepInternalHandler, JSONObject oldConfigObj, JSONObject newConfigObj) {
        JSONArray slaList = oldConfigObj.getJSONArray("slaList");
        JSONArray slaArray = new JSONArray();
        if (CollectionUtils.isNotEmpty(slaList)) {
            List<String> effectiveStepUuidList = ProcessMessageManager.getEffectiveStepUuidList();
            for (int i = 0; i < slaList.size(); i++) {
                JSONObject sla = slaList.getJSONObject(i);
                if (MapUtils.isNotEmpty(sla)) {
                    JSONObject slaObj = new JSONObject();
                    String slaName = sla.getString("name");
                    List<String> processStepUuidList = sla.getJSONArray("processStepUuidList").toJavaList(String.class);
                    if (processStepUuidList == null) {
                        processStepUuidList = new ArrayList<>();
                    } else {
                        processStepUuidList.removeIf(Objects::isNull);
                    }
                    List<String> list = ListUtils.removeAll(processStepUuidList, effectiveStepUuidList);
                    if (CollectionUtils.isNotEmpty(list) && ProcessMessageManager.getOperationType() == OperationTypeEnum.UPDATE) {
                        throw new ProcessConfigException(ProcessConfigException.Type.SLA, slaName);
                    }
                    slaObj.put("processStepUuidList", processStepUuidList);
                    List<SlaTransferPolicyVo> slaTransferPolicyList = new ArrayList<>();
                    JSONArray transferPolicyList = sla.getJSONArray("transferPolicyList");
                    if (CollectionUtils.isNotEmpty(transferPolicyList)) {
                        transferPolicyList.removeIf(Objects::isNull);
                        for (int j = 0; j < transferPolicyList.size(); j++) {
                            SlaTransferPolicyVo slaTransferPolicyVo = transferPolicyList.getObject(j, SlaTransferPolicyVo.class);
                            if (slaTransferPolicyVo != null) {
                                slaTransferPolicyList.add(slaTransferPolicyVo);
                            }
                        }
                    }
                    slaObj.put("transferPolicyList", slaTransferPolicyList);

                    List<SlaCalculatePolicyVo> calculatePolicyArray = new ArrayList<>();
                    JSONArray calculatePolicyList = sla.getJSONArray("calculatePolicyList");
                    if (CollectionUtils.isNotEmpty(calculatePolicyList)) {
                        calculatePolicyList.removeIf(Objects::isNull);
                        for (int j = 0; j < calculatePolicyList.size(); j++) {
                            SlaCalculatePolicyVo slaCalculatePolicyVo = calculatePolicyList.getObject(j, SlaCalculatePolicyVo.class);
                            if (slaCalculatePolicyVo != null) {
                                calculatePolicyArray.add(slaCalculatePolicyVo);
                            }
                        }
                    }
                    slaObj.put("calculatePolicyList", calculatePolicyArray);

                    List<SlaNotifyPolicyVo> notifyPolicyArray = new ArrayList<>();
                    JSONArray notifyPolicyList = sla.getJSONArray("notifyPolicyList");
                    if (CollectionUtils.isNotEmpty(notifyPolicyList)) {
                        notifyPolicyList.removeIf(Objects::isNull);
                        for (int j = 0; j < notifyPolicyList.size(); j++) {
                            SlaNotifyPolicyVo slaNotifyPolicyVo = notifyPolicyList.getObject(j, SlaNotifyPolicyVo.class);
                            if (slaNotifyPolicyVo != null) {
                                InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = slaNotifyPolicyVo.getNotifyPolicyConfig();
                                invokeNotifyPolicyConfigVo.setHandler(SlaNotifyPolicyHandler.class.getName());
                                notifyPolicyArray.add(slaNotifyPolicyVo);
                            }
                        }
                    }
                    slaObj.put("notifyPolicyList", notifyPolicyArray);
                    String slaUuid = sla.getString("uuid");
                    String calculateHandler = sla.getString("calculateHandler");
                    slaObj.put("uuid", slaUuid);
                    slaObj.put("name", slaName);
                    slaObj.put("calculateHandler", calculateHandler);
                    slaArray.add(slaObj);
                }
            }
        }
        newConfigObj.put("slaList", slaArray);
    }
}
