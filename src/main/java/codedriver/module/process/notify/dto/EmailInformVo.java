package codedriver.module.process.notify.dto;

import java.util.List;

/**
 * @program: codedriver
 * @description:
 * @create: 2019-12-11 18:07
 **/
public class EmailInformVo extends NotifyVo {
    private List<String> ccUserIdList;

    public List<String> getCcUserIdList() {
        return ccUserIdList;
    }

    public void setCcUserIdList(List<String> ccUserIdList) {
        this.ccUserIdList = ccUserIdList;
    }
}
