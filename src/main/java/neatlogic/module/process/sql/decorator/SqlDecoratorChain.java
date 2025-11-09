/*
 *
 * Copyright (C) 2025  TechSure Co., Ltd.  All Rights Reserved.
 * This file is part of the NeatLogic software.
 * Licensed under the NeatLogic Sustainable Use License (NSUL), Version 4.x – 2025.
 * You may use this file only in compliance with the License.
 * See the LICENSE file distributed with this work for the full license text.
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 */

package neatlogic.module.process.sql.decorator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            ISqlDecorator decoratorHandler = decoratorList.get(i);
            if (i != decoratorList.size() - 1) {
                decoratorHandler.setNextSqlDecorator(decoratorList.get(i + 1));
            }
        }
        firstSqlDecorator = decoratorList.get(0);
    }
}
