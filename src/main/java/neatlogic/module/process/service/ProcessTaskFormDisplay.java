package neatlogic.module.process.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.form.attribute.core.FormAttributeDataConversionHandlerFactory;
import neatlogic.framework.form.attribute.core.IFormAttributeDataConversionHandler;
import neatlogic.framework.form.constvalue.FormHandler;
import neatlogic.framework.form.dto.FormAttributeVo;
import neatlogic.framework.form.dto.FormVersionVo;
import neatlogic.framework.process.dto.ProcessTaskFormAttributeDataVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** 从已装载的工单表单中提取当前身份和步骤可读的字段，沿用组件注册的展示转换器。 */
final class ProcessTaskFormDisplay {
    private ProcessTaskFormDisplay() {}

    /** 按场景定义顺序输出已保存值；新旧表单的布局和行坐标分别解析。 */
    static List<JSONObject> fields(ProcessTaskVo task, String sceneUuid, String stepUuid, List<JSONObject> rules) {
        JSONObject config = task.getFormConfig();
        FormVersionVo form = new FormVersionVo();
        form.setFormConfig(config);
        Set<String> hidden = new HashSet<>(CollectionUtils.emptyIfNull(task.getFormAttributeHideList()));
        Set<String> hiddenRows = new HashSet<>();
        for (JSONObject rule : rules) {
            JSONArray steps = rule.getJSONArray("processStepUuidList");
            if (steps == null || !steps.contains(stepUuid) || !"hide".equals(rule.getString("action"))) continue;
            Object attributes = rule.get("attributeUuidList");
            List<String> affected = attributes instanceof JSONArray ids ? ids.toJavaList(String.class)
                    : attributes instanceof String id ? List.of(id) : List.of();
            if ("component".equals(rule.getString("type"))) hidden.addAll(affected);
            else if ("row".equals(rule.getString("type"))) hiddenRows.addAll(affected);
        }
        if (hidden.contains("all") || hiddenRows.contains("all")) return List.of();
        for (Object item : CollectionUtils.emptyIfNull(config.getJSONArray("hideComponentList"))) {
            collectHidden((JSONObject) item, hidden, true);
        }
        JSONObject scene = selectScene(config, sceneUuid);
        form.setSceneUuid(scene.getString("uuid"));
        if ("new".equals(config.getString("_type"))) {
            if (CollectionUtils.isEmpty(scene.getJSONArray("tableList"))) return List.of();
            JSONArray sceneHidden = scene.getJSONArray("hiddenRowList");
            for (Object item : scene.getJSONArray("tableList")) {
                JSONObject cell = (JSONObject) item;
                JSONObject component = cell.getJSONObject("component");
                int row = cell.getIntValue("row");
                if (component != null) {
                    collectHidden(component, hidden, hiddenRows.contains(String.valueOf(row + 1))
                            || sceneHidden != null && sceneHidden.contains(row));
                }
            }
            // 继承的容器在场景里可能只有 UUID，子字段仍须继承基础组件的隐藏状态。
            if (scene != config) collectLayoutRestrictions(config.getJSONArray("tableList"), hidden, false);
        } else {
            collectLayoutRestrictions(config.getJSONArray("controllerList"), hidden, false);
            JSONObject global = config.getJSONObject("globalSetting");
            if (global != null && global.getJSONArray("defaultHiderow") != null) {
                hiddenRows.addAll(global.getJSONArray("defaultHiderow").toJavaList(String.class));
            }
            JSONObject sheet = config.getJSONObject("sheetsConfig");
            JSONArray rows = sheet == null ? null : sheet.getJSONArray("tableList");
            for (int i = 0; rows != null && i < rows.size(); i++) {
                if (hiddenRows.contains(String.valueOf(i + 1))) collectLayoutRestrictions(rows.getJSONArray(i), hidden, true);
            }
        }
        Map<String, ProcessTaskFormAttributeDataVo> values = new HashMap<>();
        task.getProcessTaskFormAttributeDataList().forEach(value -> values.put(value.getAttributeUuid(), value));
        List<JSONObject> fields = new ArrayList<>();
        for (FormAttributeVo attr : CollectionUtils.emptyIfNull(form.getFormAttributeList())) {
            ProcessTaskFormAttributeDataVo value = values.get(attr.getUuid());
            if (value == null || hidden.contains(attr.getUuid()) || FormHandler.FORMPASSWORD.getHandler().equals(attr.getHandler())
                    || restricted(attr.getConfig())) continue;
            IFormAttributeDataConversionHandler handler = FormAttributeDataConversionHandlerFactory.getHandler(attr.getHandler());
            if (handler == null) continue;
            Object text = value.dataIsEmpty() ? "" : handler.dataTransformationForEmail(value, displayConfig(attr.getHandler(), attr.getConfig()));
            fields.add(new JSONObject(true).fluentPut("label", attr.getLabel()).fluentPut("value", displayValue(text)));
        }
        return fields;
    }

    /** 步骤引用的场景已移除时，与详情页一样优先选用表单的默认场景。 */
    private static JSONObject selectScene(JSONObject config, String sceneUuid) {
        String selected = StringUtils.defaultIfBlank(sceneUuid, config.getString("defaultSceneUuid"));
        JSONObject fallback = config;
        if (Objects.equals(selected, config.getString("uuid"))) return config;
        for (Object item : CollectionUtils.emptyIfNull(config.getJSONArray("sceneList"))) {
            JSONObject candidate = (JSONObject) item;
            if (Objects.equals(selected, candidate.getString("uuid"))) return candidate;
            if (Objects.equals(config.getString("defaultSceneUuid"), candidate.getString("uuid"))) fallback = candidate;
        }
        return fallback;
    }

    /** 旧版字段列表直接存组件，布局单元格则将组件放在 component 属性内。 */
    private static void collectLayoutRestrictions(JSONArray cells, Set<String> hidden, boolean hideRow) {
        for (Object item : CollectionUtils.emptyIfNull(cells)) {
            if (item instanceof JSONObject cell) {
                collectHidden(cell.get("component") instanceof JSONObject component ? component : cell, hidden, hideRow);
            }
        }
    }

    /** 对组件本身、场景覆盖和父容器的限制取并集。 */
    private static void collectHidden(JSONObject component, Set<String> hidden, boolean parentHidden) {
        String uuid = component.getString("uuid");
        boolean hide = parentHidden || hidden.contains(uuid) || restricted(component) || restricted(component.getJSONObject("config"))
                || restricted(component.getJSONObject("override_config"));
        if (hide) hidden.add(uuid);
        if (!(component.get("component") instanceof JSONArray children)) return;
        for (Object child : children) {
            collectHidden((JSONObject) child, hidden, hide);
        }
    }

    /** 展示材料排除隐藏和遮盖的字段。 */
    private static boolean restricted(JSONObject config) {
        return config != null && (config.getBooleanValue("isHide") || config.getBooleanValue("isMask"));
    }

    /** 转换前裁剪明细表的列定义，密码和受限嵌套列不会进入组件转换器的展示结果。 */
    private static JSONObject displayConfig(String handler, JSONObject config) {
        if (!FormHandler.FORMTABLEINPUTER.getHandler().equals(handler) && !FormHandler.FORMTABLESELECTOR.getHandler().equals(handler)) return config;
        if (config == null || !(config.get("dataConfig") instanceof JSONArray columns)) return config;
        JSONArray visible = new JSONArray();
        for (Object item : columns) {
            if (!(item instanceof JSONObject column)) continue;
            JSONObject child = column.getJSONObject("config");
            if (FormHandler.FORMPASSWORD.getHandler().equals(column.getString("handler")) || restricted(column)
                    || restricted(child) || Boolean.FALSE.equals(column.getBoolean("isPC"))) continue;
            visible.add(new JSONObject(new HashMap<>(column)).fluentPut("config", displayConfig(column.getString("handler"), child)));
        }
        return new JSONObject(new HashMap<>(config)).fluentPut("dataConfig", visible);
    }

    /** 将组件显示文本或多选显示值转换成可以整行复制的纯文本。 */
    private static String displayValue(Object value) {
        if (value instanceof JSONObject detail) {
            JSONArray columns = detail.getJSONArray("theadList");
            JSONArray rows = detail.getJSONArray("tbodyList");
            if (columns != null && rows != null) {
                List<String> lines = new ArrayList<>();
                for (int i = 0; i < rows.size(); i++) {
                    List<String> cells = new ArrayList<>();
                    JSONObject row = rows.getJSONObject(i);
                    for (int j = 0; j < columns.size(); j++) {
                        JSONObject column = columns.getJSONObject(j);
                        if (FormHandler.FORMPASSWORD.getHandler().equals(column.getString("handler"))) continue;
                        cells.add(column.getString("title") + "  " + displayValue(row.get(column.getString("key"))));
                    }
                    lines.add(String.join(" · ", cells));
                }
                return String.join("\n", lines);
            }
            // 复合组件可能附带技术标识，仅输出组件提供的展示内容。
            for (String display : List.of("text", "textList", "name", "label")) {
                if (detail.containsKey(display)) return displayValue(detail.get(display));
            }
            return "";
        }
        if (value instanceof Collection<?> items) {
            return items.stream().map(ProcessTaskFormDisplay::displayValue).collect(Collectors.joining(", "));
        }
        return value == null ? "" : plain(String.valueOf(value));
    }

    /** 只解析富文本标签，保留工单普通文本的换行及字段中的比较运算符。 */
    private static String plain(String content) {
        if (StringUtils.isEmpty(content) || !content.contains("<")) return StringUtils.defaultString(content);
        // 保留富文本中的段落和换行，复制到编辑器后仍能按原段落排版。
        return Jsoup.parseBodyFragment(content.replaceAll("(?i)<br\\s*/?>|</(?:p|div|li)>", "\n")).body().wholeText().strip();
    }
}
