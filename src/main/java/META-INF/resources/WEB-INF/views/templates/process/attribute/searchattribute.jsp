<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="formAttribute" method="GET">
	<div class="action-group">
		<input type="text" name="keyword" class="input-xlarge" placeholder="${tk:lang('请输入属性名称','') }">
		<button class="btn btn-default" type="button" id="btnSearchAttribute">${tk:lang("搜索","") }</button>
	</div>
	<div id="divAttributeContainer"></div>
	<script>
		var searchAttribute = function(currentPage) {
			$('#formAttribute').ajaxSubmit({
				url : '${pageContext.request.contextPath}/module/process/attribute/search' + (currentPage ? '?currentPage=' + currentPage : ''),
				dataType : 'json',
				type : 'GET',
				success : function(data) {
					var html = xdoT.render('process.attribute.listattribute', data);
					$('#divAttributeContainer').empty().html(html);
				}
			});
		};
	</script>
	<script class="xdotScript">
		var fn = {
			'#btnSearchAttribute' : {
				'click' : function() {
					searchAttribute();
				}
			}
		};
		
		var loadFn = function() {
			var that = this;
			$.getJSON('${pageContext.request.contextPath}/module/process/attribute/search', function(data) {
				var html = xdoT.render('process.attribute.listattribute', data);
				$('#divAttributeContainer', that).empty().html(html);
			});
		}
	</script>
</form>