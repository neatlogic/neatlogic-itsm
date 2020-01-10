package codedriver.framework.process.test;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javers.core.ChangesByObject;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSONObject;

@Controller
@RequestMapping("/xdottemplate")
public class XdotTemplateController {

	Logger logger = LoggerFactory.getLogger(XdotTemplateController.class);

	@RequestMapping(value = "globalconfig.do")
	public String globalconfig(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("Cache-control", "max-age=86400");
		return "/templates/global";
	}

	/*
	 * @RequestMapping(value = "/get", method = RequestMethod.GET) public String
	 * getTemplate(String templatename, HttpServletRequest request,
	 * HttpServletResponse response) throws IOException { if (templatename !=
	 * null && !templatename.equals("")) {
	 * 
	 * response.setHeader("Cache-control", "private"); //return templatename;
	 * request.setAttribute("templatePath", templatename); return
	 * "/xdottemplate/xdotTemplateFrame"; } else { throw new
	 * RuntimeException(Translator.translate("模板名称不能为空", "")); } }
	 */

	@RequestMapping(value = "/get",
			method = RequestMethod.GET)
	public String getTemplate(String templatename, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (templatename != null && !templatename.equals("")) {
			templatename = "/templates/" + templatename.replace(".", "/");
			response.setHeader("Cache-control", "private");
			return templatename;
		} else {
			throw new RuntimeException("模板名称不能为空");
		}
	}

	@RequestMapping(value = "/getcontent",
			method = RequestMethod.GET)
	public String getTemplateContent(String templatepath, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (templatepath != null && !templatepath.equals("")) {
			templatepath = "/templates/" + templatepath.replace(".", "/");
			response.setHeader("Cache-control", "private");
			return templatepath;
		} else {
			throw new RuntimeException("模板名称不能为空");
		}
	}

	public static void main(String[] argv) {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("环境", "stg");
		jsonObj.put("名称", "chen");
		JSONObject jsonObj4=new JSONObject();
		jsonObj4.put("紧急度", "P1");
		jsonObj.put("表单", jsonObj4);
		
		
		JSONObject jsonObj2 = new JSONObject();
		jsonObj2.put("名称", "chen");
		jsonObj2.put("环境", "stg");
		JSONObject jsonObj3=new JSONObject();
		jsonObj3.put("紧急度", "P1");
		jsonObj3.put("受益人", "我");
		jsonObj2.put("表单", jsonObj3);
		Javers javers = JaversBuilder.javers().build();
		
		Diff diff = javers.compare(jsonObj, jsonObj2);
		System.out.println(diff.getChanges().size());
		for(ChangesByObject change:diff.groupByObject()) {
			//System.out.println("=======");
			//System.out.println(change);
			//System.out.println(change.)
		}
		System.out.println(javers.getJsonConverter().toJson(diff));
	}

}
