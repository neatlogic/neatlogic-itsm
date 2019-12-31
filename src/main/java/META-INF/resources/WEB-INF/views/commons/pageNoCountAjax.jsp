<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<script type="text/javascript">
function changePageAjax(uri, container, page){
	if(uri == null){
		showPopMsg.error('换页失败，参数：uri不能为空！');
		return;
	}
	if(container == null){
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
</script>
<% 
	String uri = request.getAttribute("uri") == null ? request.getAttribute("javax.servlet.forward.request_uri").toString() : request.getAttribute("uri").toString();
	String queryString = request.getQueryString() == null ? "" : request.getQueryString();
	int hasNextPage = (Integer)request.getAttribute("hasNextPage");
	int pageSize = Integer.valueOf(request.getParameter("pageSize") == null ? "10" : request.getParameter("pageSize"));
	int currentPage = Integer.valueOf(request.getParameter("currentPage") == null ? "1" : request.getParameter("currentPage"));
	String container = request.getParameter("container") == null ? "container" : request.getParameter("container");
	int dspSize = 10;
	if(queryString.indexOf("currentPage") > -1){
		queryString = queryString.replaceAll("^currentPage=[^&]+[&]?", "");
		queryString = queryString.replaceAll("[&]currentPage=[^&]+", "");
	}
	uri = uri + "?" + queryString; 
%>
 
<div style="text-align: right;padding:0 5px 0 5px">
	<ul class="pagination" style="margin:0px">
		 <%if(currentPage <= 1){ %>
		 <li class="prev disabled"><a href="#">&lt;上一页</a></li>
		 <%}else{ %>
		 <li class="prev">
		 <a href="javascript:changePageAjax('<%=uri %>', '<%=container %>', <%=currentPage - 1 %>);">&lt;上一页</a></li>
		 <%} %>
		<li class="pageno"><a href="javascript:changePageAjax('<%=uri %>', '<%=container %>', <%=currentPage %>);">第<%=currentPage %>页</a></li>
		 <%if(hasNextPage == 0){ %>
		 <li class="next disabled"><a href="#">下一页&gt;</a></li>
		 <%}else{ %>
		 <li class="next">
		<a href="javascript:changePageAjax('<%=uri %>', '<%=container %>', <%=currentPage + 1 %>);">下一页&gt;</a>
		 </li>
		 <%} %>
	</ul>
</div>
