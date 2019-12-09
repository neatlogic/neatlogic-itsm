package codedriver.framework.process.attribute.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.common.RootComponent;
import codedriver.framework.process.dto.AttributeVo;
import codedriver.framework.process.mapper.AttributeMapper;
import codedriver.module.process.constvalue.AttributeType;

@RootComponent
public class AttributeHandlerFactory implements ApplicationListener<ContextRefreshedEvent> {
	private static List<AttributeComponentBase> componentList = new ArrayList<>();
	private static Map<String, IAttributeHandler> handlerMap = new HashMap<>();

	@Autowired
	private AttributeMapper attributeMapper;

	public static IAttributeHandler getHandler(String type) {
		return handlerMap.get(type);
	}

	static {
		Reflections reflections = new Reflections("com.techsure.balantflow.process.attribute.component");
		Set<Class<? extends AttributeComponentBase>> modules = reflections.getSubTypesOf(AttributeComponentBase.class);
		for (Class<? extends AttributeComponentBase> c : modules) {
			AttributeComponentBase maker;
			try {
				maker = c.newInstance();
				componentList.add(maker);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	@PostConstruct
	public void init() {
		attributeMapper.resetAttributeActive();
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, IAttributeHandler> myMap = context.getBeansOfType(IAttributeHandler.class);
		for (Map.Entry<String, IAttributeHandler> entry : myMap.entrySet()) {
			IAttributeHandler handler = entry.getValue();
			if (handler.getType() != null) {
				handlerMap.put(handler.getType(), handler);
				for (AttributeComponentBase component : componentList) {
					if (component.getHandler().equals(handler.getType())) {
						AttributeVo attributeVo = new AttributeVo();
						attributeVo.setLabel(component.getLabel());
						attributeVo.setHandler(component.getHandler());
						attributeVo.setConfig(component.getConfigObj().toString());
						attributeVo.setUuid(component.getUuid());
						attributeVo.setUnit(component.getUnit());
						attributeVo.setIsActive(1);
						attributeVo.setType(AttributeType.SYSTEM.getValue());
						attributeVo.setDescription(component.getDescription());
						attributeMapper.replaceAttribute(attributeVo);
					}
				}
			}
		}
	}
}
