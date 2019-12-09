package codedriver.framework.process.workerdispatcher;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import codedriver.framework.common.RootComponent;
import codedriver.framework.process.dto.WorkerDispatcherVo;
import codedriver.framework.process.mapper.WorkerDispatcherMapper;

@RootComponent
public class WorkerDispatcherFactory implements ApplicationListener<ContextRefreshedEvent> {

	private static Map<String, IWorkerDispatcher> componentMap = new HashMap<String, IWorkerDispatcher>();

	@Autowired
	private WorkerDispatcherMapper workerDispatcherMapper;

	public static IWorkerDispatcher getDispatcher(String name) {
		if (!componentMap.containsKey(name) || componentMap.get(name) == null) {
			throw new RuntimeException("找不到类型为：" + name + "的处理人分派组件");
		}
		return componentMap.get(name);
	}

	@PostConstruct
	public void init() {
		workerDispatcherMapper.resetWorkerDispatcher();
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext context = event.getApplicationContext();
		Map<String, IWorkerDispatcher> myMap = context.getBeansOfType(IWorkerDispatcher.class);
		for (Map.Entry<String, IWorkerDispatcher> entry : myMap.entrySet()) {
			IWorkerDispatcher component = entry.getValue();
			if (StringUtils.isNotBlank(component.getHandler())) {
				componentMap.put(component.getHandler(), component);
				WorkerDispatcherVo workerDispatcherVo = new WorkerDispatcherVo();
				workerDispatcherVo.setHandler(component.getHandler());
				workerDispatcherVo.setName(component.getName());
				workerDispatcherVo.setIsActive(1);
				workerDispatcherVo.setHelp(component.getHelp());
				workerDispatcherVo.setConfigPage(component.getConfigPage());
				workerDispatcherMapper.replaceWorkerDispatcher(workerDispatcherVo);
			}
		}
	}
}
