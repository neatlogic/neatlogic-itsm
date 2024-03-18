/*Copyright (C) $today.year  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

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
