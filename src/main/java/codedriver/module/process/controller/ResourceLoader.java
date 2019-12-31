package codedriver.module.process.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ResourceLoader {
	Logger logger = LoggerFactory.getLogger(ResourceLoader.class);

	private static Map<String, JSONObject> jsonMap = new HashMap<String, JSONObject>();

	@SuppressWarnings("unchecked")
	public ResourceLoader(String moduleName) throws Exception {
		if (!jsonMap.containsKey(moduleName)) {
			synchronized (jsonMap) {
				if (!jsonMap.containsKey(moduleName)) {
					BufferedReader filebr = null;
					InputStream in = null;
					try {
						StringBuilder str = new StringBuilder();
						in = this.getClass().getClassLoader().getResourceAsStream("resource-define.json");
						filebr = new BufferedReader(new InputStreamReader(in, "utf-8"));
						String inLine = "";
						while ((inLine = filebr.readLine()) != null) {
							str.append(inLine + "\n");
						}
						JSONObject jsonObj = JSONObject.parseObject(str.toString());
						if (moduleName != null && !moduleName.equals("")) {
							StringBuilder str2 = new StringBuilder();
							in = this.getClass().getClassLoader().getResourceAsStream("resource-define-" + moduleName + ".json");
							if (in != null) {
								filebr = new BufferedReader(new InputStreamReader(in, "utf-8"));
								inLine = "";
								while ((inLine = filebr.readLine()) != null) {
									str2.append(inLine + "\n");
								}
								JSONObject jsonObj2 = JSONObject.parseObject(str2.toString());
								Set<String> keySet =  jsonObj2.keySet();
								for (String key : keySet) {
									jsonObj.put(key, jsonObj2.get(key));
								}
							}
						}

						jsonMap.put(moduleName, jsonObj);
					} catch (Exception ex) {
						logger.error(ex.getMessage(), ex);
						throw ex;
					} finally {
						if (filebr != null) {
							try {
								filebr.close();
							} catch (IOException e) {
								logger.error(e.getMessage(), e);
							}
						}
						if (in != null) {
							try {
								in.close();
							} catch (IOException e) {
								logger.error(e.getMessage(), e);
							}
						}
					}
				}
			}
		}
	}

	public String getResources(String moduleName, String[] need, String[] exclude) {
		StringBuilder jsBuilder = new StringBuilder();
		StringBuilder cssBuilder = new StringBuilder();
		StringBuilder cssPrintBuilder = new StringBuilder();
		if (need != null && need.length > 0) {
			Map<String, Integer> resourceMap = new HashMap<String, Integer>();
			load(moduleName, need, exclude, resourceMap, jsBuilder, cssBuilder, cssPrintBuilder);
		}

		String jscss = cssBuilder.toString() + jsBuilder.toString();
		return jscss;
	}

	private String transferByCookie(Cookie[] cookies, String js) {
		Pattern pattern = Pattern.compile("\\$\\{([^\\}]+)\\}", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher matcher = null;
		matcher = pattern.matcher(js);
		StringBuffer temp = new StringBuffer();
		while (matcher.find()) {
			String key = matcher.group(1);
			String defaultValue = "";
			boolean isFound = false;
			if (key.indexOf(":") > -1) {
				defaultValue = key.split(":")[1];
				key = key.split(":")[0];
			}
			if (cookies != null && cookies.length > 0) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().equals(key)) {
						matcher.appendReplacement(temp, Matcher.quoteReplacement(cookie.getValue()));
						isFound = true;
						break;
					}
				}
			}
			if (!isFound) {
				matcher.appendReplacement(temp, Matcher.quoteReplacement(defaultValue));
			}
			matcher.appendTail(temp);
			js = temp.toString();
		}
		return js;
	}

	public void load(String moduleName, String[] need, String[] exclude, Map<String, Integer> resourceMap, StringBuilder jsBuilder, StringBuilder cssBuilder, StringBuilder cssPrintBuilder) {
		String contextPath = "/codedriver/";
		Cookie[] cookies = null;
		

		String locale = "zh_CN";
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("locale")) {
					locale = cookie.getValue();
				}
			}
		}

		JSONObject jsonObj = jsonMap.get(moduleName);
		if (need != null && need.length > 0) {
			NEED: for (String module : need) {
				module = module.trim();
				if (!"".equals(module) && !resourceMap.containsKey(module) && jsonObj.containsKey(module)) {
					if (exclude != null && exclude.length > 0) {
						for (String ex : exclude) {
							if (module.equals(ex.trim())) {
								resourceMap.put(module, 1);
								continue NEED;
							}
						}
					}
					resourceMap.put(module, 1);
					JSONObject resourceObj = jsonObj.getJSONObject(module);
					String className = (resourceObj.containsKey("className") ? resourceObj.getString("className") : null);
					if (resourceObj.containsKey("depend")) {
						if (resourceObj.get("depend") instanceof JSONArray) {
							JSONArray dependList = resourceObj.getJSONArray("depend");
							String[] dependneed = new String[dependList.size()];
							for (int i = 0; i < dependList.size(); i++) {
								dependneed[i] = dependList.getString(i);
							}
							load(moduleName, dependneed, exclude, resourceMap, jsBuilder, cssBuilder, cssPrintBuilder);
						} else {
							load(moduleName, new String[] { resourceObj.getString("depend") }, exclude, resourceMap, jsBuilder, cssBuilder, cssPrintBuilder);
						}
					}
					if (resourceObj.containsKey("css")) {
						if (resourceObj.get("css") instanceof JSONArray) {
							JSONArray cssList = resourceObj.getJSONArray("css");
							for (int i = 0; i < cssList.size(); i++) {
								if (!"".equals(cssList.getString(i))) {
									String css = cssList.getString(i);
									css = transferByCookie(cookies, css);
									cssBuilder.append("<link " + (className != null ? "class=\"" + className + "\"" : "") + " type=\"text/css\" rel=\"stylesheet\" href=\"" + contextPath + css + "?" + System.currentTimeMillis() + "\"/>\n");
								}
							}
						} else {
							if (!"".equals(resourceObj.getString("css"))) {
								String css = resourceObj.getString("css");
								css = transferByCookie(cookies, css);
								cssBuilder.append("<link " + (className != null ? "class=\"" + className + "\"" : "") + " type=\"text/css\" rel=\"stylesheet\" href=\"" + contextPath + css + "?" + System.currentTimeMillis() + "\"/>\n");
							}
						}
					}

					if (resourceObj.containsKey("css-print")) {
						if (resourceObj.get("css-print") instanceof JSONArray) {
							JSONArray cssList = resourceObj.getJSONArray("css-print");
							for (int i = 0; i < cssList.size(); i++) {
								if (!"".equals(cssList.getString(i))) {
									String css = cssList.getString(i);
									css = transferByCookie(cookies, css);
									cssBuilder.append("<link " + (className != null ? "class=\"" + className + "\"" : "") + " type=\"text/css\" rel=\"stylesheet\" media=\"print\" href=\"" + contextPath + css + "?" + System.currentTimeMillis()+ "\"/>\n");
								}
							}
						} else {
							if (!"".equals(resourceObj.getString("css-print"))) {
								String css = resourceObj.getString("css-print");
								css = transferByCookie(cookies, css);
								cssBuilder.append("<link " + (className != null ? "class=\"" + className + "\"" : "") + " type=\"text/css\" rel=\"stylesheet\" media=\"print\" href=\"" + contextPath + css + "?" + System.currentTimeMillis()+ "\"/>\n");
							}
						}
					}
					if (resourceObj.containsKey("js")) {
						if (resourceObj.get("js") instanceof JSONArray) {
							JSONArray jsList = resourceObj.getJSONArray("js");
							for (int i = 0; i < jsList.size(); i++) {
								if (!"".equals(jsList.getString(i))) {
									String js = jsList.getString(i);
									js = transferByCookie(cookies, js);
									if (resourceObj.containsKey("translate") && resourceObj.getBoolean("translate")) {
										jsBuilder.append("<script " + (className != null ? "class=\"" + className + "\"" : "") + " type=\"text/javascript\" src=\"" + contextPath + "/jstranslator/get?path=" + js + "&locale=" + locale + "&t=" + System.currentTimeMillis()+ "\"></script>\n");
									} else {
										if (resourceObj.containsKey("nocache") && resourceObj.getBoolean("nocache")) {
											jsBuilder.append("<script " + (className != null ? "class=\"" + className + "\"" : "") + " type=\"text/javascript\" src=\"" + contextPath + js + "?" + System.currentTimeMillis() + "\"></script>\n");
										} else {
											jsBuilder.append("<script " + (className != null ? "class=\"" + className + "\"" : "") + " type=\"text/javascript\" src=\"" + contextPath + js + "?" + System.currentTimeMillis()+ "\"></script>\n");
										}
									}
								}
							}
						} else {
							if (!"".equals(resourceObj.getString("js"))) {
								String js = resourceObj.getString("js");
								js = transferByCookie(cookies, js);
								if (resourceObj.containsKey("translate") && resourceObj.getBoolean("translate")) {
									jsBuilder.append("<script " + (className != null ? "class=\"" + className + "\"" : "") + " type=\"text/javascript\" src=\"" + contextPath + "/jstranslator/get?path=" + js + "&locale=" + locale + "&t=" + System.currentTimeMillis()+ "\"></script>\n");
								} else {
									if (resourceObj.containsKey("nocache") && resourceObj.getBoolean("nocache")) {
										jsBuilder.append("<script " + (className != null ? "class=\"" + className + "\"" : "") + " type=\"text/javascript\" src=\"" + contextPath + js + "?" + System.currentTimeMillis() + "\"></script>\n");
									} else {
										jsBuilder.append("<script " + (className != null ? "class=\"" + className + "\"" : "") + " type=\"text/javascript\" src=\"" + contextPath + js + "?" + System.currentTimeMillis()+ "\"></script>\n");
									}
								}
							}
						}
					}

				}
			}
		}
	}

}
