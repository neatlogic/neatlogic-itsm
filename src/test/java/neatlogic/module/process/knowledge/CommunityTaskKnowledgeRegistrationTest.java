package neatlogic.module.process.knowledge;

import neatlogic.framework.knowledge.generation.KnowledgeGenerationSources;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/** 验证纯社区 ITSM 的生产模块配置，避免商业接口、告警材料或关系 Mapper 随公共底座一起装载。 */
public class CommunityTaskKnowledgeRegistrationTest {
    /** 原有工单服务与纯社区 Mapper 仍可发现，旧的独立审计接口已移除。 */
    @Test
    public void communityModuleDoesNotRegisterCommercialKnowledge() throws Exception {
        DefaultListableBeanFactory definitions = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(definitions).loadBeanDefinitions("classpath:neatlogic/module/process/itsm-servlet-context.xml");
        assertFalse(definitions.containsBeanDefinition("processTaskTransferKnowledgeAuditApi"));
        assertFalse(definitions.containsBeanDefinition("getTaskKnowledgeMaterialsApi"));
        assertFalse(definitions.containsBeanDefinition("taskKnowledgeBindApi"));
        assertFalse(definitions.containsBeanDefinition("taskKnowledgeService"));
        assertNull(KnowledgeGenerationSources.get("alert-handling"));
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:neatlogic/module/process/**/dao/mapper/**/*.xml");
        List<String> names = Arrays.stream(resources).map(Resource::getFilename).toList();
        assertTrue(names.contains("ProcessTaskMapper.xml"));
        assertFalse(names.contains("TaskKnowledgeMapper.xml"));
    }
}
