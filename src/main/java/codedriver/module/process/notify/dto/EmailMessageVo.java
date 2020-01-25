package codedriver.module.process.notify.dto;

import codedriver.framework.dto.UserVo;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-11 18:57
 **/
public class EmailMessageVo extends NotifyVo {
    private List<UserVo> ccUserList;

    public List<UserVo> getCcUserList() {
        return ccUserList;
    }

    public void setCcUserList(List<UserVo> ccUserList) {
        this.ccUserList = ccUserList;
    }
}
