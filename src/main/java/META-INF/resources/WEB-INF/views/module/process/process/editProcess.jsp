<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<!DOCTYPE HTML>
<html lang="en">
<head>
<title><c:choose>
		<c:when test="${empty flowVo }">
创建流程
</c:when>
		<c:otherwise>"编辑编排"：${flowVo.name }</c:otherwise>
	</c:choose></title>
<jsp:include page="/resources/res-include-new.jsp?module=process&exclude=bootstrap,skincss,tsicon&need=base,jquery,bootstrap,bootstrap-validation,sheet,tsicon,uploader,customcheckbox,ckeditor,treeselect,codemirror,slidedialog,xdot,util,title,snapmessage,form,json,quartz,tojson,d3arrange,draggable,checkselect,select,scrollbar,wdatepicker,inputselect" />
<c:forEach items="${componentList }" var="component">
	<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/process/component/${component.type}.js"></script>
</c:forEach>

<script type="text/javascript">
	var Paper = null;
	function initPaper() {
		var height = $('#divPaper').height();
		var width = $('#divPaper').width();
		if (!Paper) {
			Paper = TsFlowChart($("#divPaper")[0], {
				uuidmode : 32
			});
		}
	}

	$(function() {
		initPaper();

		if ($.trim($('#txtConfig').val())) {
			var json = JSON.parse($.trim($('#txtConfig').val()));
			Paper.fromJson(json);
			//console.log(Paper.getUserData());
			var nodeList = Paper.getAllNode();
			for (var i = 0; i < nodeList.length; i++) {
				var node = nodeList[i];
				if (node.getUserData() && node.getUserData()['isStartNode'] == '1') {
					node.setStyle({
						fill : '#70BC82',
						stroke : '#009688',
						fillopacity : 1,
						fontcolor : '#ffffff',
						fontsize : 12,
						iconcolor : '#ffffff',
						strokewidth : 1,
						strokedasharray : 0
					});
					node.setIsStart(true);
					node.setNeedIn('optional');
					break;
				}
			}
		}

		$('.flowItem').each(function() {
			var type = $(this).data('type');
			$(this).draggable({
				dropHolder : $('#divPaper'),
				onDrop : function(x, y) {
					x = x - $(window).scrollLeft() - $('#tdMain').offset().left;
					y = y - $(window).scrollLeft() - $('#tdMain').offset().top;
					var config = {
						x : x,
						y : y,
						type : type
					};
					Paper.addNode(config);
				}
			});
		});

		$('#btnForm').on('click', function() {
			var userData = Paper.getUserData();
			var html = xdoT.render('process.process.editform', userData || {});
			createSlideDialog({
				title : "选择表单",
				content : html,
				width : 1000,
				successFuc : function() {
					if ($('#formProcessForm').valid()) {
						userData['formId'] = $('#sltForm').val();
						Paper.setUserData(userData);
					}
				}
			});
		});

		$('#btnAttribute').on('click', function() {
			//var attr = $.trim($('#txtProcessAttribute').val());
			var attrData = Paper.getUserData();
			console.log(JSON.stringify(attrData));
			/*if (attr) {
				attr = JSON.parse(attr);
				attrData['attributeList'] = attr;
			}*/
			var html = xdoT.render('process.process.editprocessattribute', attrData || {});
			createSlideDialog({
				title : "选择属性",
				content : html,
				width : 700,
				successFuc : function() {
					if ($('#formProcessAttribute').valid()) {
						var attributes = new Array();
						$('.trProcessAttribute').each(function(i, k) {
							var attr = {};
							attr.uuid = $('.hidProcessAttributeUuid', $(this)).val();
							attr.width = $('.sltProcessAttributeWidth', $(this)).val();
							attr.label = $('.txtProcessAttributeLabel', $(this)).val();
							attr.typeName = $('.hidProcessAttributeTypeName', $(this)).val();
							attr.handler = $('.hidProcessAttributeHandler', $(this)).val();
							attr.handlerName = $('.hidProcessAttributeHandlerName', $(this)).val();
							attributes.push(attr);
						});
						Paper.setUserData({
							"attributeList" : attributes
						});
						if (attributes.length > 0) {
							$('#spnAttributeCount').text(attributes.length);
						} else {
							$('#spnAttributeCount').text('');
						}
					}
				}
			});
		});

		$('#btnSaveArrange').on('click', function() {
			var startNode = Paper.getElementByType('start');
			/*if (!startNode) {
				showPopMsg.info("请创建开始节点");
				return;
			}*/
			var endNode = Paper.getElementByType('end');
			if (!endNode) {
				showPopMsg.info("请创建结束节点");
				return;
			}
			var omnipotentList = Paper.getElementByType('omnipotent');
			if (omnipotentList == null) {
				showPopMsg.info("请至少创建一个处理节点");
				return;
			}
			if (Paper.valid()) {
				var nodeList = Paper.getAllNode();
				var hasStartNode = false;
				for (var i = 0; i < nodeList.length; i++) {
					var userData = nodeList[i].getUserData();
					if (userData && userData['isStartNode'] == '1') {
						hasStartNode = true;
						break;
					}
				}
				if (!hasStartNode) {
					showPopMsg.info('请设置一个处理节点为开始节点');
					return false;
				}
				$('#txtConfig').val(JSON.stringify(Paper.toJson()));
				console.log(JSON.stringify(Paper.toJson()));
				if ($('#mainForm').valid()) {
					$('#mainForm').ajaxSubmit({
						url : '${pageContext.request.contextPath}/module/process/process/save',
						dataType : 'json',
						type : 'POST',
						success : function(data) {
							if (data.Status == 'OK') {
								showPopMsg.success("操作成功", function() {
									window.location.href = '${pageContext.request.contextPath}/module/process/process/editProcess.do?uuid=' + data.processId;
								});
							} else {
								showPopMsg.error(data);
							}
						}
					});
				}
			}
		});

		$(window).resize(function() {
			//Paper.setSize();
		});
	});
</script>
<style type="text/css">
.flowItem {
	cursor: move;
	-webkit-touch-callout: none;
	-webkit-user-select: none;
	-khtml-user-select: none;
	-moz-user-select: none;
	-ms-user-select: none;
	user-select: none;
	-khtml-user-select: none;
	-moz-user-select: none;
	-ms-user-select: none;
	-moz-user-select: none;
	-ms-user-select: none;
	-ms-user-select: none;
	background: #fff;
	text-align: left;
	padding: 5px;
	overflow: hidden;
	text-overflow: ellipsis;
	word-break: break-all;
	white-space: nowrap;
	line-height: 28px;
	margin: 2px;
}

.flowItem:before {
	margin-right: 3px;
}

.flowItem:hover {
	box-shadow: 1px 0 4px #ccc8c8;
}

.flowItem .fa, .flowItem [class^="ts-"] {
	margin-right: 3px;
	font-size: 16px;
	width: 20px;
	display: inline-block;
}

.flowTypeItem {
	webkit-touch-callout: none;
	-webkit-user-select: none;
	-khtml-user-select: none;
	-moz-user-select: none;
	-ms-user-select: none;
	user-select: none;
	-khtml-user-select: none;
	-moz-user-select: none;
	-ms-user-select: none;
	-moz-user-select: none;
	-ms-user-select: none;
	-ms-user-select: none;
	cursor: pointer;
	padding: 5px;
	border-radius: 4px;
	text-align: left;
	vertical-align: middle;
	line-height: 28px;
}

.flowTypeItem  .state {
	width: 20px;
	font-size: 16px;
}

.spnParam {
	display: inline-block;
	cursor: pointer;
	padding: 2px 5px;
	border: 1px solid #fff;
}

.spnParam:hover {
	background: #eee;
	border-color: #ddd;
}

.divBtnItem {
	text-align: center;
	color: #666;
	margin-top: 20px;
	cursor: pointer;
	padding: 5px;
}

.divBtnItem:hover {
	background: #eaf0ff;
}

.jquery-scrollbar-container>.jquery-scrollbar {
	right: 0;
}

#tdMain {
	margin-right: 60px;
	height: calc(100vh - 130px);
}

#divPaper {
	width: 100%;
	height: 100%;
}

.row-right {
	position: relative;
}

#btnContain {
	position: absolute;
	width: 60px;
	right: 0px;
	border-left: 1px solid #ddd;
	top: 16px;
	bottom: 65px;
	background: #fff;
	overflow: auto;
}

#leftList {
	position: absolute;
	height: calc(100% - 50px);
}

.partlist>*[id^='divAction'] {
	display: none;
	margin-left: 18px;
}

#content .CodeMirror-scroll {
	background: #f7f7f7;
}

.step-list {
	display: inline-block;
	padding: 0 5px;
	height: 30px;
	line-height: 30px;
}

.step-seprate {
	display: inline-block;
	height: 30px;
	line-height: 30px;
	padding: 0 3px;
	color: #d6d5d5;
}

.step-container .step-title {
	cursor: pointer;
}

.step-container .step-title .ts:before {
	content: '\ea66';
}

.step-container .step-text {
	display: block;
}

.step-container.showstep .step-title .ts:before {
	content: '\ea65';
}

.step-container.showstep .step-text {
	display: none;
}

#divRunBlock .tooltip-inner {
	word-break: break-all !important;
	white-space: normal;
}

#formParam .tooltip-inner {
	max-width: none !important;
}

#formParam .jquery-titleinfo-panel {
	z-index: 108;
}
</style>
</head>
<body class="body-row">
	<form id="mainForm" class="form-horizontal row-main" method="POST">
		<textarea id="txtConfig" name="config" style="display: none">${processVo.config }</textarea>
		<div class="row-left">
			<div class="row-left-title">
				<input type="text" id="txtKeyword" style="border-radius: 0px; width: 100%; border-bottom: 1px solid #bbb; border-top-width: 0px; border-left-width: 0px; border-right-width: 0px" placeholder="关键字">
			</div>
			<div id="leftList" plugin-scrollbar>
				<c:forEach items="${componentList }" var="component">
					<div class="flowItem" data-type="${component.type }">${component.icon }${component.name}</div>
				</c:forEach>
				<div class="partlist">
					<c:forEach items="${actionTypeList }" var="actionType">
						<div class="btnActionType flowTypeItem" id="btnActionType${actionType.id }" data-typeid="${actionType.id }">
							<i class="fa fa-angle-right state" aria-hidden="true"></i>${actionType.name }</div>
						<div id="divAction${actionType.id }"></div>
					</c:forEach>
				</div>
			</div>
		</div>
		<span class="ts row-tool" data-toggle="row"></span>
		<div class="row-right step-panel">
			<div class="action-group">
				<div>
					<input class="input-large" name="name" type="text" placeholder='名称' check-type="required" value="${processVo.name }" maxlength="50">
					<input type="hidden" name="uuid" value="${processVo.uuid }">
					<input type="hidden" name="belong" value="itsm">
				</div>
				<div>
					<select name="type" data-searchable="true" data-placeholder='类型' class="mustinput" plugin-checkselect>
						<c:forEach items="${typeList}" var="type">
							<option value="${type.id }" ${flowVo!=null and type.id == flowVo.type?'selected':''}>${type.name}</option>
						</c:forEach>
					</select>
				</div>
				<input class="input-xlarge" name="description" type="text" placeholder='说明' value="${flowVo.description }" maxlength="200">
			</div>
			<div id="tdMain">
				<div id="divPaper"></div>
			</div>
			<div id="btnContain">
				<div class="divBtnItem" id="btnAttribute">
					<div>
						<i class="fa-envelope-o fz16"></i>
					</div>
					<div>属性<span id="spnAttributeCount" class="font-icon fz12"> ${processVo.attributeList.size()>0?processVo.attributeList.size():''} </span>
					</div>
				</div>
				<div class="divBtnItem" id="btnForm">
					<div>
						<i class="fa-envelope-o fz16"></i>
					</div>
					<div>表单<span id="spnFormCount" class="font-icon fz12">
							<c:if test="${!empty processVo.formUuid}">1</c:if>
						</span>
					</div>
				</div>
			</div>
			<div class="btn-bar absolute">
				<div class="btn-group">
					<button type="button" class="btn btn-primary" id="btnSaveArrange">保存</button>
				</div>
			</div>
		</div>
	</form>
</body>
</html>