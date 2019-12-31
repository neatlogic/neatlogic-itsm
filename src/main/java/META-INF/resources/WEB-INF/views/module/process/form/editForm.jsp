<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<!DOCTYPE HTML>
<html lang="en">
<head>
<title>编辑表单</title>
<jsp:include page="/resources/res-include-new.jsp?module=process&exclude=bootstrap,skincss,tsicon&need=base,jquery,bootstrap,bootstrap-validation,tsicon,uploader,customcheckbox,slidedialog,sheet,xdot,util,title,snapmessage,form,json,tojson,checkselect,select,scrollbar,wdatepicker,inputselect" />
<script type="text/javascript">
	$(function() {
		var formbuilder = $('#divContent').SheetBuilder({
			"sheets" : [ {
				"cols" : 5,
				"rows" : 5,
				"width" : 1409,
				"head" : [ {
					"width" : 531,
					"index" : 0
				}, {
					"width" : 112,
					"index" : 1
				}, {
					"width" : 165,
					"index" : 2
				} ],
				"body" : [ {
					"cols" : [ {
						"index" : 0
					}, {
						"index" : 1
					}, {
						"index" : 2
					}, {
						"index" : 3
					}, {
						"index" : 4
					} ],
					"index" : 0
				}, {
					"cols" : [ {
						"index" : 0
					}, {
						"text" : "阿斯顿发",
						"colspan" : "2",
						"index" : 1
					}, {
						"text" : "地方",
						"rowspan" : "3",
						"index" : 2
					}, {
						"index" : 3
					} ],
					"index" : 1
				}, {
					"cols" : [ {
						"text" : "阿斯顿发",
						"colspan" : "3",
						"rowspan" : "3",
						"index" : 0
					}, {
						"rowspan" : "3",
						"index" : 1
					} ],
					"index" : 2
				}, {
					"index" : 3
				}, {
					"cols" : [ {
						"index" : 0
					} ],
					"index" : 4
				} ]
			} ]
		});

		$('#btnSave').on('click', function() {
			formbuilder.fromJson({
				
			});
		});

		$('#btnToJson').on('click', function() {
			console.log(formbuilder.toJson());
		});

	});
</script>
</head>
<body class="bg-grey">
	<div class="block-main form-container step-panel">
		<div id="divContent"></div>
	</div>
	<button type="button" id="btnSave">fromJson</button>
	<button type="button" id="btnToJson">toJson</button>
</body>
</html>