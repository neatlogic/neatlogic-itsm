<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<%@page import="java.util.Enumeration,com.techsure.balantflow.util.Toolkit"%>

<%
	String uri = request.getAttribute("javax.servlet.forward.request_uri").toString();
	//String queryString = request.getQueryString() == null ? "" : request.getQueryString();
	String queryString = "";
	Enumeration<String> paraNames = request.getParameterNames();
	while (paraNames.hasMoreElements()) {
		String p = paraNames.nextElement();
		if (queryString.length() > 0)
	queryString += "&";
		if (!p.equals("currentPage")) {
	queryString += p + "=" + Toolkit.encodeURIComponent(request.getParameter(p));
		}
	}
	int pageCount = (Integer) request.getAttribute("pageCount");
	int pageSize = Integer.valueOf(request.getParameter("pageSize") == null ? "10" : request.getParameter("pageSize"));
	int currentPage = Integer.valueOf(request.getParameter("currentPage") == null ? "1" : request.getParameter("currentPage"));
	String container = request.getParameter("container") == null ? "" : request.getParameter("container");
	/*if (queryString.indexOf("currentPage") > -1) {
		queryString = queryString.replaceAll("^currentPage=[^&]+[&]?", "");
		queryString = queryString.replaceAll("[&]currentPage=[^&]+", "");
	}*/
	if (!queryString.equals("")) {
		uri = uri + "?" + queryString;
	}
	if (pageCount > 1) {
%>
<script type="text/javascript">
	var pageajax = (function() {
		return {
			changePageAjax : function(uri, container, page) {
				uri = '<%=uri%>';
				if (uri == null) {
					showPopMsg.error('换页失败，参数：uri不能为空！');
					return;
				}
				if (container == null) {
					showPopMsg.error('换页失败，参数：container不能为空！');
					return;
				}
				if (page) {
					if (uri && uri.indexOf('?') > -1) {
						uri += '&currentPage=' + page;
					} else {
						uri += '?currentPage=' + page;
					}
					if (uri.indexOf('container') == -1) {
						uri = uri + '&container=' + container;
					}
				}


				$.get(uri, function(data) {
					$('#' + container).html(data);
				}).fail(function() {
					showPopMsg.error('查询数据失败:<br/>请检查服务端或者网络是否存在问题。', 10);
				});
			}
		}
	}());
</script>
<div style="text-align: right; padding: 0 5px 0 5px">
	<ul class="pagination pagination-sm" style="margin: 0px">
		<%
			if (currentPage <= 1) {
		%>
		<li class="prev disabled"><a href="#">&lt;上一页</a></li>
		<%
			} else {
		%>
		<li class="prev"><a href="javascript:pageajax.changePageAjax('<%=uri%>', '<%=container%>', <%=currentPage - 1%>);">&lt;${tk:lang('上一页','') }</a></li>
		<%
			}
		%>
		<%
			int maxpage = currentPage + 3 <= pageCount ? currentPage + 3 : pageCount;
				int minpage = currentPage - 3 >= 1 ? currentPage - 3 : 1;
				if (minpage > 1) {
		%>
		<li class="disabled"><a href="#">……</a></li>
		<%
			}
				for (int k = minpage; k <= maxpage; k++) {
					if (currentPage == k) {
		%>
		<li class="active"><a href="#"><%=k%></a></li>
		<%
			} else {
		%>
		<li class="pageno"><a href="javascript:pageajax.changePageAjax('<%=uri%>', '<%=container%>', <%=k%>);"><%=k%></a></li>
		<%
			}
				}
				if (maxpage < pageCount) {
		%>
		<li class="disabled"><a>……</a></li>
		<%
			}
		%>
		<%
			if (currentPage >= pageCount) {
		%>
		<li class="next disabled"><a href="#">下一页&gt;</a></li>
		<%
			} else {
		%>
		<li class="next"><a href="javascript:pageajax.changePageAjax('<%=uri%>', '<%=container%>', <%=currentPage + 1%>);">${tk:lang('下一页','') }&gt;</a></li>
		<%
			}
		%>
	</ul>
</div>
<%
	}
%>
