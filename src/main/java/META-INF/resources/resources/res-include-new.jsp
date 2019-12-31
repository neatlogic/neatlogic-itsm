<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@page import="codedriver.module.process.controller.ResourceLoader"%>
<%@include file="/WEB-INF/views/commons/taglibs.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<%
	String module = request.getParameter("module") == null ? "" : request.getParameter("module");
	ResourceLoader rl = new ResourceLoader(module);
	String need = request.getParameter("need") == null ? "" : request.getParameter("need");
	String exclude = request.getParameter("exclude") == null ? "" : request.getParameter("exclude");
	String[] needs = need.split(",");
	String[] excludes = exclude.split(",");
%>
<%=rl.getResources(module, needs, excludes)%>