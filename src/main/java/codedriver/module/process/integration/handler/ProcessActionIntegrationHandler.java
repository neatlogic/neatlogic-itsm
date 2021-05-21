/*
 * Copyright(c) 2021 TechSureCo.,Ltd.AllRightsReserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 */

package codedriver.module.process.integration.handler;

import codedriver.framework.exception.core.ApiRuntimeException;
import codedriver.framework.integration.core.IntegrationHandlerBase;
import codedriver.framework.integration.dto.IntegrationResultVo;
import codedriver.framework.integration.dto.IntegrationVo;
import codedriver.framework.integration.dto.PatternVo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 流程动作访问外部接口查询
 * @author linbq
 * @since 2021/5/21 18:21
 **/
@Component
public class ProcessActionIntegrationHandler extends IntegrationHandlerBase {
    @Override
    public String getName() {
        return "流程动作访问";
    }

    @Override
    public Integer hasPattern() {
        return null;
    }

    @Override
    public List<PatternVo> getInputPattern() {
        return null;
    }

    @Override
    public List<PatternVo> getOutputPattern() {
        return null;
    }

    @Override
    public void validate(IntegrationResultVo resultVo) throws ApiRuntimeException {

    }

    @Override
    protected void beforeSend(IntegrationVo integrationVo) {

    }

    @Override
    protected void afterReturn(IntegrationVo integrationVo) {

    }
}
