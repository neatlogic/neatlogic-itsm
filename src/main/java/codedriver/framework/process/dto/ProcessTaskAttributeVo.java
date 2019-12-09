package codedriver.framework.process.dto;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

public class ProcessTaskAttributeVo implements Serializable, Comparable<ProcessTaskAttributeVo> {
	private static final long serialVersionUID = -7092718684833552558L;
	private Long processTaskId;
	private String attributeUuid;
	private String handler;
	private String unit;
	private String dataCubeUuid;
	private String dataCubeValueField;
	private String dataCubeTextField;
	private ProcessDataCubeVo dataCubeVo;
	private String name;
	private String label;
	private Integer width;
	private String help;
	private String config;
	private Integer sort;
	private JSONObject configObj;

	public ProcessTaskAttributeVo() {

	}

	public ProcessTaskAttributeVo(ProcessAttributeVo processAttributeVo) {
		this.setAttributeUuid(processAttributeVo.getAttributeUuid());
		this.setHandler(processAttributeVo.getHandler());
		this.setUnit(processAttributeVo.getUnit());
		this.setName(processAttributeVo.getName());
		this.setLabel(processAttributeVo.getLabel());
		this.setHelp(processAttributeVo.getHelp());
		this.setSort(processAttributeVo.getSort());
		this.setWidth(processAttributeVo.getWidth());
		this.setConfig(processAttributeVo.getConfig());
		this.setDataCubeUuid(processAttributeVo.getDataCubeUuid());
		this.setDataCubeValueField(processAttributeVo.getDataCubeValueField());
		this.setDataCubeTextField(processAttributeVo.getDataCubeTextField());
	}

	public Long getProcessTaskId() {
		return processTaskId;
	}

	public void setProcessTaskId(Long processTaskId) {
		this.processTaskId = processTaskId;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getDataCubeValueField() {
		return dataCubeValueField;
	}

	public void setDataCubeValueField(String dataCubeValueField) {
		this.dataCubeValueField = dataCubeValueField;
	}

	public String getDataCubeTextField() {
		return dataCubeTextField;
	}

	public void setDataCubeTextField(String dataCubeTextField) {
		this.dataCubeTextField = dataCubeTextField;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getHelp() {
		return help;
	}

	public void setHelp(String help) {
		this.help = help;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public JSONObject getConfigObj() {
		return configObj;
	}

	public void setConfigObj(JSONObject configObj) {
		this.configObj = configObj;
	}

	public ProcessDataCubeVo getDataCubeVo() {
		return dataCubeVo;
	}

	public void setDataCubeVo(ProcessDataCubeVo dataCubeVo) {
		this.dataCubeVo = dataCubeVo;
	}

	@Override
	public int compareTo(ProcessTaskAttributeVo o) {
		if (this.sort != null && o.getSort() != null) {
			return this.sort.compareTo(o.getSort());
		} else {
			return 0;
		}
	}

	public String getAttributeUuid() {
		return attributeUuid;
	}

	public void setAttributeUuid(String attributeUuid) {
		this.attributeUuid = attributeUuid;
	}

	public String getDataCubeUuid() {
		return dataCubeUuid;
	}

	public void setDataCubeUuid(String dataCubeUuid) {
		this.dataCubeUuid = dataCubeUuid;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

}
