package codedriver.module.process.workcenter.core.sqldecorator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Title: SqlDecoratorChain
 * @Package: codedriver.module.process.workcenter.core.sqldecorator
 * @Description: TODO
 * @Author: 89770
 * @Date: 2021/1/19 15:32
 * Copyright(c) 2021 TechSure Co., Ltd. All Rights Reserved.
 * 本内容仅限于深圳市赞悦科技有限公司内部传阅，禁止外泄以及用于其他的商业项目。
 **/
@Service
public class SqlDecoratorChain implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;

    public static ISqlDecorator firstSqlDecorator;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, ISqlDecorator> beansOfTypeMap = applicationContext.getBeansOfType(ISqlDecorator.class);
        if (beansOfTypeMap.size() == 0) {
            return;
        }
        List<ISqlDecorator> decoratorList = beansOfTypeMap.values().stream().sorted((e1, e2) -> e1.getSort() - e2.getSort()).collect(Collectors.toList());
        for (int i = 0; i < decoratorList.size(); i++) {
            ISqlDecorator payHandler = decoratorList.get(i);
            if (i != decoratorList.size() - 1) {
                payHandler.setNextSqlDecorator(decoratorList.get(i + 1));
            }
        }
        firstSqlDecorator = decoratorList.get(0);
    }
}
