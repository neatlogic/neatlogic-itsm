package codedriver.module.process.dto;

import java.io.Serializable;

/**
 * @program: balantflow
 * @description: 流程步骤事件收件组实体类
 * @create: 2019-04-12 11:23
 **/
public class ProcessTaskEventTeamVo implements Serializable {
	/**
	 * @Fields serialVersionUID : TODO
	 */
	private static final long serialVersionUID = -6413428557143512736L;
	private Long flowStepEventId;
	private Long teamId;
	private String teamName;

	public Long getFlowStepEventId() {
		return flowStepEventId;
	}

	public void setFlowStepEventId(Long flowStepEventId) {
		this.flowStepEventId = flowStepEventId;
	}

	public Long getTeamId() {
		return teamId;
	}

	public void setTeamId(Long teamId) {
		this.teamId = teamId;
	}

	public String getTeamName() {
		return teamName;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

}
