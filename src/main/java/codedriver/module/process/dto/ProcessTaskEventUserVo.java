package codedriver.module.process.dto;

import java.io.Serializable;

/**
 * @program: balantflow
 * @description: 流程步骤事件收件人实体类
 * @author: lixs
 * @create: 2018-10-11 20:09
 **/
public class ProcessTaskEventUserVo implements Serializable{
    /** 
	* @Fields serialVersionUID : TODO 
	*/
	private static final long serialVersionUID = -4030346499749506137L;
	private Long flowStepEventId;
    private String userId;
    private String userName;
    private String userShowName;

    public Long getFlowStepEventId() {
        return flowStepEventId;
    }

    public void setFlowStepEventId(Long flowStepEventId) {
        this.flowStepEventId = flowStepEventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserShowName() {
        return userName + "[" + userId + "]";
    }

    public void setUserShowName(String userShowName) {
        this.userShowName = userShowName;
    }
}
