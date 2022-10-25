/*
 * Copyright(c) 2022 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.api.form;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.form.attribute.core.IFormAttributeHandler;
import codedriver.framework.form.dao.mapper.FormMapper;
import codedriver.framework.form.dto.FormAttributeVo;
import codedriver.framework.form.dto.FormVersionVo;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dao.mapper.SelectContentByHashMapper;
import codedriver.framework.process.dto.ProcessTaskFormAttributeDataVo;
import codedriver.framework.process.dto.ProcessTaskFormVo;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.UuidUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author linbq
 * @since 2022/3/8 16:29
 **/
//@Service
//@Transactional
public class FormFormStaticListConfigUpdateApi extends PrivateApiComponentBase {

    @Resource
    private FormMapper formMapper;

    @Resource
    private SelectContentByHashMapper selectContentByHashMapper;

    @Resource
    private ProcessTaskMapper processTaskMapper;

    /**
     * 接口唯一标识，也是访问URI
     *
     * @return 接口唯一地址
     */
    @Override
    public String getToken() {
        return "form/formstaticlist/config/update";
    }
    /**
     * 接口中文名
     *
     * @return 中文名
     */
    @Override
    public String getName() {
        return "表单表格输入组件config更新";
    }

    /**
     * 额外配置
     *
     * @return 配置json
     */
    @Override
    public String getConfig() {
        return null;
    }

    @Input({})
    @Output({
            @Param(name = "tbodyList", type = ApiParamType.JSONARRAY, desc = "更新情况")
    })
    @Description(desc="表单表格输入组件config更新")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<String> resultList = new ArrayList<>();
        int a = 0;
        int b = 0;
        int c = 0;
        int d = 0;
        int e = 0;
        int f = 0;
        String handler = "formstaticlist";
        //保存已经更新过的config生成的hash
        Set<String> newFormConfigHashSet = new HashSet<>();
        //第一步
        //在form_version表中找出有表格输入组件的表单版本
        List<FormVersionVo> formVersionList =  formMapper.getFormVersionListByFormConfigLikeKeyword(handler);
        for (FormVersionVo formVersionVo : formVersionList) {
            String oldFormConfig = formVersionVo.getFormConfig();
            String oldFormConfigHash = DigestUtils.md5DigestAsHex(oldFormConfig.getBytes());
            //判断config是否已经更新过了
//            if (newFormConfigHashSet.contains(oldFormConfigHash)) {
//                continue;
//            }
            String newFormConfig = updateConfig(oldFormConfig);
            //如果更新前后相同，则说明已经转换过了(重复执行本接口)
            if (Objects.equals(newFormConfig, oldFormConfig)) {
                continue;
            }
            String newFormConfigHash = DigestUtils.md5DigestAsHex(newFormConfig.getBytes());
            newFormConfigHashSet.add(newFormConfigHash);
            FormVersionVo newFormVersionVo = new FormVersionVo();
            newFormVersionVo.setUuid(formVersionVo.getUuid());
            newFormVersionVo.setFormConfig(newFormConfig);
            formMapper.updateFormVersionConfigByUuid(newFormVersionVo);
            a++;
            //如果当前表单版本是激活版本，则需要更新form_attribute表中对应表格输入组件的config
            if (Objects.equals(formVersionVo.getIsActive(), 1)) {
                newFormVersionVo.setVersion(formVersionVo.getVersion());
                newFormVersionVo.setIsActive(formVersionVo.getIsActive());
                newFormVersionVo.setFormUuid(formVersionVo.getFormUuid());
                List<FormAttributeVo> formAttributeList = newFormVersionVo.getFormAttributeList();
                for (FormAttributeVo formAttributeVo : formAttributeList) {
                    if ("formstaticlist".equals(formAttributeVo.getHandler())) {
                        formMapper.updateFormAttributeConfig(formAttributeVo);
                        b++;
                    }
                }
            }
            //判断当前表单版本是否存在被工单绑定的副本，如果存在，则
            // 1.向processtask_form_content表插入更新后的newFormConfigHash和newFormConfig
            // 2.将processtask_form表中form_content_hash字段的值是oldFormConfigHash的更新成newFormConfigHash
            // 3.删除processtask_form_content表中hash字段值是oldFormConfigHash的数据
            if (selectContentByHashMapper.getProcessTaskFromContentCountByHash(oldFormConfigHash) > 0) {
                ProcessTaskFormVo processTaskFormVo = new ProcessTaskFormVo();
                processTaskFormVo.setFormContent(newFormConfig);
                processTaskFormVo.setFormContentHash(newFormConfigHash);
                processTaskMapper.insertIgnoreProcessTaskFormContent(processTaskFormVo);
                c++;
                int updateCount = processTaskMapper.updateProcessTaskFormFormContentHashByFormContentHash(oldFormConfigHash, newFormConfigHash);
                d += updateCount;
                processTaskMapper.deleteProcessTaskFormContentByHash(oldFormConfigHash);
                e++;
            }
        }
        resultList.add("form_version表更新" + a + "条数据。");
        resultList.add("form_attribute表更新" + b + "条数据。");
//        System.out.println("form_version表更新" + a + "条数据。");
//        System.out.println("form_attribute表更新" + b + "条数据。");
        //第二步
        //在processtask_form_content中找出有表格输入组件的表单配置副本
        List<ProcessTaskFormVo> processTaskFormList = processTaskMapper.getProcessTaskFormContentListByContentLikeKeyword(handler);
        for (ProcessTaskFormVo processTaskFormVo : processTaskFormList) {
            String oldFormConfigHash = processTaskFormVo.getFormContentHash();
            //第一步已经更新过了
            if (newFormConfigHashSet.contains(oldFormConfigHash)) {
                continue;
            }
            String oldFormConfig = processTaskFormVo.getFormContent();
            String newFormConfig = updateConfig(oldFormConfig);
            //如果更新前后相同，则说明已经转换过了(重复执行本接口)
            if (Objects.equals(newFormConfig, oldFormConfig)) {
                continue;
            }
            String newFormConfigHash = DigestUtils.md5DigestAsHex(newFormConfig.getBytes());
            ProcessTaskFormVo newProcessTaskFormVo = new ProcessTaskFormVo();
            newProcessTaskFormVo.setFormContent(newFormConfig);
            newProcessTaskFormVo.setFormContentHash(newFormConfigHash);
            processTaskMapper.insertIgnoreProcessTaskFormContent(newProcessTaskFormVo);
            c++;
            int updateCount = processTaskMapper.updateProcessTaskFormFormContentHashByFormContentHash(oldFormConfigHash, newFormConfigHash);
            d += updateCount;
            processTaskMapper.deleteProcessTaskFormContentByHash(oldFormConfigHash);
            e++;
        }
        resultList.add("processtask_form_content表插入" + c + "条数据。");
        resultList.add("processtask_form_content表删除" + e + "条数据。");
        resultList.add("processtask_form表更新" + d + "条数据。");
//        System.out.println("processtask_form_content表插入" + c + "条数据。");
//        System.out.println("processtask_form_content表删除" + e + "条数据。");
//        System.out.println("processtask_form表更新" + d + "条数据。");
        //第三步
        //在processtask_formattribute_data中找出表格输入组件的数据信息列表
        Map<Long, String> formContentHashMap = new HashMap<>();
        List<ProcessTaskFormAttributeDataVo> processTaskFormAttributeDataList = processTaskMapper.getProcessTaskStepFormAttributeDataListByType(handler);
        for (ProcessTaskFormAttributeDataVo processTaskFormAttributeDataVo : processTaskFormAttributeDataList) {
            String data = processTaskFormAttributeDataVo.getData();
            if (StringUtils.isBlank(data)) {
                continue;
            }
            Object dataObj = processTaskFormAttributeDataVo.getDataObj();
            if (dataObj instanceof JSONObject) {
                continue;
            }
            Long processTaskId = processTaskFormAttributeDataVo.getProcessTaskId();
            String formContentHash = formContentHashMap.get(processTaskId);
            if (StringUtils.isBlank(formContentHash)) {
                ProcessTaskFormVo processTaskFormVo = processTaskMapper.getProcessTaskFormByProcessTaskId(processTaskId);
                if (processTaskFormVo != null) {
                    formContentHash = processTaskFormVo.getFormContentHash();
                    if (StringUtils.isNotBlank(formContentHash)) {
                        formContentHashMap.put(processTaskId, formContentHash);
                    }
                }
            }
            if (StringUtils.isNotBlank(formContentHash)) {
                String formConfig = selectContentByHashMapper.getProcessTaskFromContentByHash(formContentHash);
                if (StringUtils.isNotBlank(formConfig)) {
                    JSONObject attributeConfig = getAttributeConfig(processTaskFormAttributeDataVo.getAttributeUuid(), formConfig);
                    if (MapUtils.isEmpty(attributeConfig)) {
                        continue;
                    }
                    String newData = updateAttributeData(attributeConfig, data);
                    ProcessTaskFormAttributeDataVo processTaskFormAttributeData = new ProcessTaskFormAttributeDataVo();
                    processTaskFormAttributeData.setProcessTaskId(processTaskId);
                    processTaskFormAttributeData.setAttributeUuid(processTaskFormAttributeDataVo.getAttributeUuid());
                    processTaskFormAttributeData.setData(newData);
                    processTaskMapper.updateProcessTaskFormAttributeDataByProcessTaskIdAndAttributeUuid(processTaskFormAttributeData);
                    f++;
                }
            }
        }
        resultList.add("processtask_formattribute_data表更新" + f + "条数据。");
//        System.out.println("processtask_formattribute_data表更新" + f + "条数据。");
        return resultList;
    }

    /**
     * 通过组件uuid在表单配置信息中找到组件的配置信息
     * @param attributeUuid 组件uuid
     * @param configStr 表单配置信息
     * @return
     */
    private JSONObject getAttributeConfig(String attributeUuid, String configStr) {
        JSONObject config = JSONObject.parseObject(configStr);
        if (MapUtils.isEmpty(config)) {
            return null;
        }
        JSONArray controllerList = config.getJSONArray("controllerList");
        if (CollectionUtils.isNotEmpty(controllerList)) {
            for (int i = 0; i < controllerList.size(); i++) {
                JSONObject controllerObj = controllerList.getJSONObject(i);
                if (MapUtils.isEmpty(controllerObj)) {
                    continue;
                }
                String handler = controllerObj.getString("handler");
                if (!"formstaticlist".equals(handler)) {
                    continue;
                }
                String uuid = controllerObj.getString("uuid");
                if (Objects.equals(attributeUuid, uuid)) {
                    return controllerObj.getJSONObject("config");
                }
            }
        }
        return null;
    }

    /**
     * 将旧配置信息转换成新的配置信息，主要是给表格输入组件的每一列加上attributeUuid，如果列的类型是table，把table里的每一列加上attributeUuid。
     * @param configStr 旧的配置信息
     * @return
     */
    private String updateConfig(String configStr) {
        if (StringUtils.isBlank(configStr)) {
            return configStr;
        }
        JSONObject config = JSONObject.parseObject(configStr);
        if (MapUtils.isEmpty(config)) {
            return configStr;
        }
        //记录每一列对应的attributeUuid，sheetsConfig.tableList[x][x].component中需要用到
        Map<String, Map<String, String>> attributeUuidMap = new HashMap<>();
        Map<String, Map<String, String>> secondAttributeUuidMap = new HashMap<>();
        JSONArray controllerList = config.getJSONArray("controllerList");
        if (CollectionUtils.isNotEmpty(controllerList)) {
            //遍历表单的所有组件
            for (int i = 0; i < controllerList.size(); i++) {
                JSONObject controllerObj = controllerList.getJSONObject(i);
                if (MapUtils.isEmpty(controllerObj)) {
                    continue;
                }
                //只更新表格输入组件
                String handler = controllerObj.getString("handler");
                if (!"formstaticlist".equals(handler)) {
                    continue;
                }
                JSONObject controllerConfig = controllerObj.getJSONObject("config");
                if (MapUtils.isEmpty(controllerConfig)) {
                    continue;
                }
                JSONArray attributeList = controllerConfig.getJSONArray("attributeList");
                if (CollectionUtils.isEmpty(attributeList)) {
                    continue;
                }
                String uuid = controllerObj.getString("uuid");
                Map<String, String> firstAttributeUuidMap = attributeUuidMap.computeIfAbsent(uuid, key -> new HashMap<>());
                //遍历表格输入组件的所有列，添加attributeUuid字段
                updateAttributeList(attributeList, firstAttributeUuidMap, secondAttributeUuidMap);
            }
        }
        //sheetsConfig.tableList[x][x].component中表格输入组件
        JSONObject sheetsConfig = config.getJSONObject("sheetsConfig");
        if (MapUtils.isEmpty(sheetsConfig)) {
            return config.toJSONString();
        }
        JSONArray tableList = sheetsConfig.getJSONArray("tableList");
        if (CollectionUtils.isEmpty(tableList)) {
            return config.toJSONString();
        }
        for (int i = 0; i < tableList.size(); i++) {
            JSONArray row = tableList.getJSONArray(i);
            if (CollectionUtils.isEmpty(row)) {
                continue;
            }
            for (int j = 0; j < row.size(); j++) {
                Object cell = row.get(j);
                if (cell instanceof JSONObject) {
                    JSONObject cellObj = (JSONObject) cell;
                    if (MapUtils.isEmpty(cellObj)) {
                        continue;
                    }
                    JSONObject component = cellObj.getJSONObject("component");
                    if (MapUtils.isEmpty(component)) {
                        continue;
                    }
                    String handler = component.getString("handler");
                    if (!"formstaticlist".equals(handler)) {
                        continue;
                    }
                    JSONObject componentConfig = component.getJSONObject("config");
                    if (MapUtils.isEmpty(componentConfig)) {
                        continue;
                    }
                    JSONArray attributeList = componentConfig.getJSONArray("attributeList");
                    if (CollectionUtils.isEmpty(attributeList)) {
                        continue;
                    }
                    String uuid = component.getString("uuid");
                    Map<String, String> firstAttributeUuidMap = attributeUuidMap.computeIfAbsent(uuid, key -> new HashMap<>());
                    //遍历表格输入组件的所有列，添加attributeUuid字段
                    updateAttributeList(attributeList, firstAttributeUuidMap, secondAttributeUuidMap);
                }
            }
        }
        return config.toJSONString();
    }

    /**
     * 遍历表格输入组件的所有列，添加attributeUuid字段
     * @param attributeList
     * @param firstAttributeUuidMap
     * @param secondttributeUuidMap
     */
    private void updateAttributeList(JSONArray attributeList, Map<String, String> firstAttributeUuidMap, Map<String, Map<String, String>> secondttributeUuidMap) {
        for (int i = 0; i < attributeList.size(); i++) {
            JSONObject attributeObj = attributeList.getJSONObject(i);
            if (MapUtils.isEmpty(attributeObj)) {
                continue;
            }
            String attributeUuid = attributeObj.getString("attributeUuid");
            if (StringUtils.isBlank(attributeUuid)) {
                String attribute = attributeObj.getString("attribute");
                attributeUuid = firstAttributeUuidMap.get(attribute);
                if (StringUtils.isBlank(attributeUuid)) {
                    attributeUuid = UuidUtil.randomUuid();
                    firstAttributeUuidMap.put(attribute, attributeUuid);
                }
                attributeObj.put("attributeUuid", attributeUuid);
            }

            String type = attributeObj.getString("type");
            if ("table".equals(type)) {
                //如果是嵌套表格，就继续遍历
                JSONObject attrConfig = attributeObj.getJSONObject("attrConfig");
                if (MapUtils.isEmpty(attrConfig)) {
                    continue;
                }
                JSONArray attributeArray = attrConfig.getJSONArray("attributeList");
                if (CollectionUtils.isEmpty(attributeArray)) {
                    continue;
                }
                Map<String, String> map = secondttributeUuidMap.computeIfAbsent(attributeUuid, key -> new HashMap<>());
                updateAttributeList(attributeArray, map, null);
            }
        }
    }

    /**
     * 更新整一个表格输入组件的数据
     * @param config 配置信息
     * @param data 旧数据
     * @return 返回新数据
     */
    private String updateAttributeData(JSONObject config, String data) {
        JSONObject resultObj = new JSONObject();
        //每行数据uuid列表
        List<String> selectUuidList = new ArrayList<>();
        resultObj.put("selectUuidList", selectUuidList);
        //详细数据
        Map<String, Object> detailData = new LinkedHashMap<>();
        //简单数据
        Map<String, Object> extendedData = new LinkedHashMap<>();
        resultObj.put("detailData", detailData);
        resultObj.put("extendedData", extendedData);
        JSONArray tableArray = JSONObject.parseArray(data);
        if (CollectionUtils.isEmpty(tableArray)) {
            return resultObj.toJSONString();
        }
        JSONArray attributeList = config.getJSONArray("attributeList");
        if (CollectionUtils.isEmpty(attributeList)) {
            return resultObj.toJSONString();
        }
        //遍历表格数据
        for (int i = 0; i < tableArray.size(); i++) {
            //一行数据
            JSONArray row = tableArray.getJSONArray(i);
            if (CollectionUtils.isEmpty(row)) {
                continue;
            }
            //随机生成行uuid
            String rowUuid = UuidUtil.randomUuid();
            selectUuidList.add(rowUuid);
            Map<String, Object> newRowDetailData = new LinkedHashMap<>();
            Map<String, Object> newRowExtendedData = new LinkedHashMap<>();
            //遍历一行数据
            for (int j = 0; j < row.size(); j++) {
                JSONObject attributeObj = attributeList.getJSONObject(j);
                if (MapUtils.isEmpty(attributeObj)) {
                    continue;
                }
                //一个单元格数据
                Object cell = row.get(j);
                String attributeUuid = attributeObj.getString("attributeUuid");
                String type = attributeObj.getString("type");
                //更新单元格数据
                JSONObject newCellDetailData = updateCellData(attributeObj, cell);
                newRowDetailData.put(attributeUuid, newCellDetailData);
                if ("table".equals(type)) {
                    Map<String, Object> newCellExtendedData = new LinkedHashMap<>();
                    JSONObject newCellValueData = newCellDetailData.getJSONObject("value");
                    for (Map.Entry<String, Object> cellEntry : newCellValueData.entrySet()) {
                        Map<String, Object> newRowValueObj = new LinkedHashMap<>();
                        LinkedHashMap<String, Object> rowValueObj = (LinkedHashMap) cellEntry.getValue();
                        for (Map.Entry<String, Object> rowEntry : rowValueObj.entrySet()) {
                            JSONObject valueObj = (JSONObject)rowEntry.getValue();
                            newRowValueObj.put(rowEntry.getKey(), valueObj.get("value"));
                        }
                        newCellExtendedData.put(cellEntry.getKey(), newRowValueObj);
                    }
                    newRowExtendedData.put(attributeUuid, newCellExtendedData);
                } else {
                    newRowExtendedData.put(attributeUuid, cell);
                }
            }
            detailData.put(rowUuid, newRowDetailData);
            extendedData.put(rowUuid, newRowExtendedData);
        }
        return JSONObject.toJSONString(resultObj, SerializerFeature.DisableCircularReferenceDetect);
    }

    /**
     * 更新一个单元格数据
     * @param config 单元格所在列的配置信息
     * @param oldCellData 旧数据
     * @return 返回新数据
     */
    private JSONObject updateCellData(JSONObject config, Object oldCellData) {
        JSONObject resultObj = new JSONObject();
        String type = config.getString("type");
        resultObj.put("type", type);
        if (oldCellData == null) {
            return resultObj;
        }
        if ("text".equals(type)) {
            resultObj.put("value", oldCellData);
            resultObj.put("text", oldCellData);
        } else if ("textarea".equals(type)) {
            resultObj.put("value", oldCellData);
            resultObj.put("text", oldCellData);
        } else if ("date".equals(type)) {
            resultObj.put("value", oldCellData);
            resultObj.put("text", oldCellData);
        } else if ("time".equals(type)) {
            resultObj.put("value", oldCellData);
            resultObj.put("text", oldCellData);
        } else if ("select".equals(type) || "radio".equals(type)) {
            resultObj.put("value", oldCellData);
            JSONObject attrConfig = config.getJSONObject("attrConfig");
            String dataSource = attrConfig.getString("dataSource");
            if ("static".equals(dataSource)) {
                Map<String, String> map = new HashMap<>();
                JSONArray dataArray = attrConfig.getJSONArray("dataList");
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONObject dataObj = dataArray.getJSONObject(i);
                    map.put(dataObj.getString("value"), dataObj.getString("text"));
                }
                String text = map.get(oldCellData);
                resultObj.put("text", text);
            } else {
                String cellStr = (String) oldCellData;
                if (StringUtils.isBlank(cellStr)) {
                    resultObj.put("text", cellStr);
                } else {
                    String[] split = cellStr.split(IFormAttributeHandler.SELECT_COMPOSE_JOINER);
                    resultObj.put("text", split[1]);
                }
            }
        } else if ("selects".equals(type) || "checkbox".equals(type)) {
            resultObj.put("value", oldCellData);
            JSONObject attrConfig = config.getJSONObject("attrConfig");
            String dataSource = attrConfig.getString("dataSource");
            if ("static".equals(dataSource)) {
                Map<String, String> map = new HashMap<>();
                JSONArray dataArray = attrConfig.getJSONArray("dataList");
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONObject dataObj = dataArray.getJSONObject(i);
                    map.put(dataObj.getString("value"), dataObj.getString("text"));
                }
                List<String> textList = new ArrayList<>();
                JSONArray cellDataArray = (JSONArray) oldCellData;
                List<String> cellDataList = cellDataArray.toJavaList(String.class);
                for (String cellData : cellDataList) {
                    String text = map.get(cellData);
                    textList.add(text);
                }
                resultObj.put("text", textList);
            } else {
                List<String> textList = new ArrayList<>();
                JSONArray cellDataArray = (JSONArray) oldCellData;
                List<String> cellDataList = cellDataArray.toJavaList(String.class);
                for (String cellData : cellDataList) {
                    String[] split = cellData.split(IFormAttributeHandler.SELECT_COMPOSE_JOINER);
                    textList.add(split[1]);
                }
                resultObj.put("text", textList);
            }
        } else if ("table".equals(type)) {
            JSONObject attrConfig = config.getJSONObject("attrConfig");
            JSONArray attributeList = attrConfig.getJSONArray("attributeList");
            if (CollectionUtils.isEmpty(attributeList)) {
                return resultObj;
            }
            Map<String, Object> detailData = new LinkedHashMap<>();
            JSONArray tableArray = (JSONArray) oldCellData;
            for (int i = 0; i < tableArray.size(); i++) {
                JSONArray row = tableArray.getJSONArray(i);
                if (CollectionUtils.isEmpty(row)) {
                    continue;
                }
                String rowUuid = UuidUtil.randomUuid();
                Map<String, Object> newRowDetailData = new LinkedHashMap<>();
                for (int j = 0; j < row.size(); j++) {
                    JSONObject attributeObj = attributeList.getJSONObject(j);
                    if (MapUtils.isEmpty(attributeObj)) {
                        continue;
                    }
                    Object cell = row.get(j);
                    String attributeUuid = attributeObj.getString("attributeUuid");
                    JSONObject newCellDetailData = updateCellData(attributeObj, cell);
                    newRowDetailData.put(attributeUuid, newCellDetailData);
                }
                detailData.put(rowUuid, newRowDetailData);
            }
            resultObj.put("value", detailData);
        }
        return resultObj;
    }
}
