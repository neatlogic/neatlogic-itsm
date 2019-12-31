<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>

<% 
	String uri = request.getAttribute("javax.servlet.forward.request_uri").toString();
	String queryString = request.getQueryString() == null ? "" : request.getQueryString();
	int pageCount = (Integer)request.getAttribute("pageCount");
	int pageSize = Integer.valueOf(request.getParameter("pageSize") == null ? "10" : request.getParameter("pageSize"));
	int currentPage = Integer.valueOf(request.getParameter("currentPage") == null ? "1" : request.getParameter("currentPage"));
	if(queryString.indexOf("currentPage") > -1){
		queryString = queryString.replaceAll("^currentPage=[^&]+[&]?", "");
		queryString = queryString.replaceAll("[&]currentPage=[^&]+", "");
	}
	if(!queryString.equals("")){
		uri = uri + "?" + queryString;
	}
	if(pageCount > 1){
%>

<%if(pageCount > 1){ %>
<div style="text-align:right;padding:5px">
<ul class="pagination">
 <%if(currentPage <= 1){ %>
 <li class="prev disabled"><a href="#">&lt;上一页</a></li>
 <%}else{ %>
 <li class="prev">
 <a href="<%=uri.indexOf("?") > -1 ? (uri + "&currentPage="+ (currentPage - 1)) : (uri + "?currentPage="+ (currentPage - 1))%>">&lt;上一页</a></li>
 <%} %>
<%
	int maxpage = currentPage + 3 <= pageCount ? currentPage + 3 : pageCount;
	int minpage = currentPage - 3 >= 1 ? currentPage - 3 : 1;
	if(minpage > 1){
%>
<li class="disabled"><a href="#">……</a></li>
<%
	}
	for(int k = minpage; k <= maxpage; k++){
		if(currentPage == k){
%>
<li class="active"><a href="#"><%=k %></a></li>
<% 	
		}else{
%>
<li class="pageno"><a href="<%=uri.indexOf("?") > -1 ? (uri + "&currentPage=" + k) : (uri + "?currentPage="+ k)%>"><%=k %></a></li>
<%
		}
	}
if(maxpage < pageCount){
		%>
<li class="disabled"><a>……</a></li>
<%}%>
 <%if(currentPage >= pageCount){ %>
 <li class="next disabled"><a href="#">下一页&gt;</a></li>
 <%}else{ %>
 <li class="next">
<a href="<%=uri.indexOf("?") > -1 ? (uri + "&currentPage="+ (currentPage + 1)) : (uri + "?currentPage="+ (currentPage + 1))%>">下一页&gt;</a>
 </li>
 <%} %>
<%} %>
</ul>
</div>
<%}%>