<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<script type="text/javascript">
var pageajax = (function(){
	return {changePageAjax: function(uri, container, page) {
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
	
		loadingMask();
		uri = encodeURI(uri);
		$.get(uri, function(data) {
			$('#' + container).html(data);
			removeMask();
		}).fail(function() {
			showPopMsg.error('查询数据失败:<br/>请检查服务端或者网络是否存在问题。', 10);
			removeMask();
		});
	}
	}
}());
</script>
<%
	String uri = request.getAttribute("javax.servlet.forward.request_uri").toString();
	String queryString = request.getQueryString() == null ? "" : request.getQueryString();
	int pageCount = (Integer) request.getAttribute("pageCount");
	int pageSize = Integer.valueOf(request.getParameter("pageSize") == null ? "10" : request.getParameter("pageSize"));
	int currentPage = Integer.valueOf(request.getParameter("currentPage") == null ? "1" : request.getParameter("currentPage"));
	String container = request.getParameter("container") == null ? "" : request.getParameter("container");
	if (queryString.indexOf("currentPage") > -1) {
		queryString = queryString.replaceAll("^currentPage=[^&]+[&]?", "");
		queryString = queryString.replaceAll("[&]currentPage=[^&]+", "");
	}
	if (!queryString.equals("")) {
		uri = uri + "?" + queryString;
	}
	if (pageCount > 1) {
%>
<div style="text-align: right;padding:0 5px 0 5px">
	<ul class="pager" style="margin:5px">
		<%
			if (currentPage <= 1) {
		%>
		<li class="previous disabled"><a href="#">&lt;上一页</a></li>
		<%
			} else {
		%>
		<li class="previous"><a href="javascript:pageajax.changePageAjax('<%=uri%>', '<%=container%>', <%=currentPage - 1%>);">&lt;上一页</a></li>
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
		<li class="next"><a href="javascript:pageajax.changePageAjax('<%=uri%>', '<%=container%>', <%=currentPage + 1%>);">下一页&gt;</a></li>
		<%
			}
		%>
	</ul>
</div>
<%
	}
%>