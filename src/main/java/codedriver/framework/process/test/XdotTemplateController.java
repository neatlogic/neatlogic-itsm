package codedriver.framework.process.test;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/xdottemplate")
public class XdotTemplateController {

	Logger logger = LoggerFactory.getLogger(XdotTemplateController.class);

	@RequestMapping(value = "globalconfig.do")
	public String globalconfig(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("Cache-control", "max-age=86400");
		return "/templates/global";
	}

	/*@RequestMapping(value = "/get", method = RequestMethod.GET)
	public String getTemplate(String templatename, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (templatename != null && !templatename.equals("")) {
			
			response.setHeader("Cache-control", "private");
			//return templatename;
			request.setAttribute("templatePath", templatename);
			return "/xdottemplate/xdotTemplateFrame";
		} else {
			throw new RuntimeException(Translator.translate("模板名称不能为空", ""));
		}
	}*/
	
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public String getTemplate(String templatename, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (templatename != null && !templatename.equals("")) {
			templatename = "/templates/" + templatename.replace(".", "/");
			response.setHeader("Cache-control", "private");
			return templatename;
		} else {
			throw new RuntimeException("模板名称不能为空");
		}
	}
	
	@RequestMapping(value = "/getcontent", method = RequestMethod.GET)
	public String getTemplateContent(String templatepath, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (templatepath != null && !templatepath.equals("")) {
			templatepath = "/templates/" + templatepath.replace(".", "/");
			response.setHeader("Cache-control", "private");
			return templatepath;
		} else {
			throw new RuntimeException("模板名称不能为空");
		}
	}

}
