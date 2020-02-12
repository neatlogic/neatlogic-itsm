package codedriver.module.process.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerUtil {
	static Logger logger = LoggerFactory.getLogger(FreemarkerUtil.class);

	public static String getNotifyContent(JSONObject dataObj, String content) {
		String resultStr = "";
		if (content != null) {
			Configuration cfg = new Configuration();
			cfg.setNumberFormat("0.##");
			cfg.setClassicCompatible(true);
			StringTemplateLoader stringLoader = new StringTemplateLoader();
			stringLoader.putTemplate("template", content);
			cfg.setTemplateLoader(stringLoader);
			Template temp;
			Writer out = null;
			try {
				temp = cfg.getTemplate("template", "utf-8");
				out = new StringWriter();
				temp.process(dataObj, out);
				resultStr = out.toString();
				out.flush();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			} catch (TemplateException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return resultStr;
	}
}
