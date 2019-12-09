<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="formConfigOmnipotent" class="form-horizontal formMain">
	<input type="hidden" value="{{=it.uuid}}" id="hidProcessStepUuid">
	<div role="tabpanel">
		<ul class="nav-bdbottom nav-tabs" role="tablist" id="ul-tablist">
			<li role="presentation" class="active">
				<a href="#base" role="tab" data-toggle="tab">${tk:lang("基本设置","") }</a>
			</li>
			<li role="presentation">
				<a href="#attribute" role="tab" data-toggle="tab">${tk:lang("属性设置","") }</a>
			</li>
			<li role="presentation" id="lnkFormConfig" style="display: none">
				<a href="#form" role="tab" data-toggle="tab">${tk:lang("表单设置","") }</a>
			</li>
			<li role="presentation" id="lnkAssignConfig">
				<a href="#worker" role="tab" data-toggle="tab">${tk:lang("指派设置","") }</a>
			</li>
			<li role="presentation" id="lnkExpireConfig">
				<a href="#expire" role="tab" data-toggle="tab">${tk:lang("超时设置","") }</a>
			</li>
		</ul>
		<div class="tab-content">
			<div role="tabpanel" class="tab-pane active" id="base">
				<div class="form-block form-block-12">
					<span class="block-left">
						<i style="color: red">*</i>${tk:lang("步骤名称","") }：</span>
					<div class="block-right">
						<input type="text" class="input-large" maxlength="20" name="name" value="{{=it.userData.name||''}}" check-type="required">
						<input type="hidden" value="{{=it.prevNodeIds||''}}" id="hidPrevNodeIds">
					</div>
				</div>
				<div class="form-block form-block-12">
					<span class="block-left">${tk:lang("设为开始节点","") }：</span>
					<div class="block-right">
						<input type="checkbox" data-makeup="checkbox" id="chkIsStartNode" {{?it.userData&&it.userData.isStartNode=='1'}} checked{{?}}>
						<span class="help-block">${tk:lang("帮助","") }：${tk:lang("每个流程只能有一个开始节点","") }</span>
						<input id="hidIsStartNode" type="hidden" value="{{?it.userData}}{{=it.userData.isStartNode||0}}{{??}}0{{?}}" name="isStartNode">
					</div>
				</div>
				<div class="form-block form-block-12">
					<span class="block-left">${tk:lang("允许暂存","") }：</span>
					<div class="block-right">
						<input type="checkbox" data-makeup="checkbox" id="chkAllowSave" {{?it.userData&&it.userData.allowSave=='1'}} checked{{?}}>
						<span class="help-block">${tk:lang("帮助","") }：${tk:lang("是否显示暂存按钮","") }</span>
						<input id="hidAllowSave" type="hidden" value="{{?it.userData}}{{=it.userData.allowSave||0}}{{??}}0{{?}}" name="allowSave">
					</div>
				</div>
				<div class="form-block form-block-12">
					<span class="block-left">${tk:lang("允许终止","") }：</span>
					<div class="block-right">
						<input type="checkbox" data-makeup="checkbox" id="chkAllowCancel" {{?it.userData&&it.userData.allowCancel=='1'}} checked{{?}}>
						<span class="help-block">${tk:lang("帮助","") }：${tk:lang("是否显示终止流程按钮","") }</span>
						<input id="hidAllowCancel" type="hidden" value="{{?it.userData}}{{=it.userData.allowCancel||0}}{{??}}0{{?}}" name="allowCancel">
					</div>
				</div>
				<div class="form-block form-block-12">
					<span class="block-left">${tk:lang("说明","") }：</span>
					<div class="block-right">
						<textarea id="txtDescription" name="description">{{?it.userData}}{{=it.userData.description||''}}{{?}}</textarea>
					</div>
				</div>
			</div>
			<div role="tabpanel" class="tab-pane" id="attribute">
				<div>
					<button type="button" id="btnAddAttribute" class="btn btn-xs btn-default">
						<i class="ts-plus"></i>${tk:lang("添加属性","") }</button>
				</div>
				<div id="divProcessStepAttribute" style="padding-top: 8px" class="clearfix"></div>
			</div>
			<div role="tabpanel" class="tab-pane" id="form">
				<div id="divProcessStepFormAttribute" style="padding-top: 8px" class="clearfix"></div>
			</div>
			<div role="tabpanel" class="tab-pane" id="worker">
				<div class="form-block form-block-12">
					<span class="block-left"> ${tk:lang("自动开始","") }：</span>
					<div class="block-right">
						<input type="checkbox" data-makeup="checkbox" id="chkAutoStart">
						<input type="hidden" name="isAutoStart" id="hidAutoStart" value="{{?it.userData}}{{=it.userData.isAutoStart||0}}{{??}}0{{?}}">
						<div class="help-block">${tk:lang("帮助","") }：${tk:lang("如果分派结果只有一个处理人，自动开始当前步骤","") }</div>
					</div>
				</div>
				<div class="form-block form-block-12">
					<span class="block-left"> ${tk:lang("执行方式","") }：</span>
					<div class="block-right">
						<select plugin-checkselect id="sltAssignPolicy" name="assignPolicy" data-searchable="false">
							<option value="parallel" {{?it.userData && it.userData.assignPolicy=='parallel'}}selected{{?}}>${tk:lang("全部执行","") }</option>
							<option value="serial" {{?it.userData && it.userData.assignPolicy=='serial'}}selected{{?}}>${tk:lang("顺序执行","") }</option>
						</select>
						<div class="help-block">${tk:lang("全部执行","") }：${tk:lang("指派策略会全部执行","") }，${tk:lang("顺序执行","") }：${tk:lang("指派策略按顺序执行，找不到任何处理人则会执行下一个策略，找到处理人则不再执行余下策略","") }。</div>
					</div>
				</div>
				<div class="form-block form-block-12">
					<span class="block-left">
						<i style="color: red">*</i>${tk:lang("指派策略","") }：</span>
					<div class="block-right">
						<div class="btn-group open">
							<button type="button" class="btn btn-lesser dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
								${tk:lang("添加指派策略","") }
								<span class="ts-arrow"></span>
							</button>
							<ul class="dropdown-menu">
								<%-- <li>
									<a class="lnkWorkerPolicy" data-policy="assign" href="javascript:void(0)">${tk:lang("管理员指定","") }</a>
								</li>--%>
								<li>
									<a class="lnkWorkerPolicy" data-policy="manual" href="javascript:void(0)">${tk:lang("处理人抢单","") }</a>
								</li>
								<li>
									<a class="lnkWorkerPolicy" data-policy="automatic" href="javascript:void(0)">${tk:lang("分配器分派","") }</a>
								</li>
								<li>
									<a class="lnkWorkerPolicy" data-policy="attribute" href="javascript:void(0)">${tk:lang("属性值","") }</a>
								</li>
								<li>
									<a class="lnkWorkerPolicy" data-policy="form" href="javascript:void(0)">${tk:lang("表单值","") }</a>
								</li>
								<li>
									<a class="lnkWorkerPolicy" data-policy="copy" href="javascript:void(0)">${tk:lang("复制前置步骤","") }</a>
								</li>
							</ul>
						</div>
						<table class="table" id="tabWorkerPolicy" style="display: none; margin-top: 10px">
							<thead>
								<tr class="noborder">
									<th style="width: 100px">${tk:lang("策略","") }</th>
									<th>${tk:lang("配置","") }</th>
									<th style="width: 60px">${tk:lang("","") }</th>
								</tr>
							</thead>
							<tbody id="tbWorkerPolicy">
							</tbody>
						</table>
					</div>
				</div>
			</div>
			<div role="tabpanel" class="tab-pane" id="expire">
				<div class="form-block form-block-12">
					<span class="block-left">${tk:lang("超时策略","") }：</span>
					<div class="block-right">
						<div class="btn-group open">
							<button type="button" class="btn btn-lesser dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="true">
								${tk:lang("添加超时策略","") }
								<span class="ts-arrow"></span>
							</button>
							<ul class="dropdown-menu dropdown-menu-right">
								<li>
									<a class="lnkTimeoutPolicy" data-policy="simple" href="javascript:void(0)">${tk:lang("简易策略","") }</a>
								</li>
								<li>
									<a class="lnkTimeoutPolicy" data-policy="advanced" href="javascript:void(0)">${tk:lang("高级策略","") }</a>
								</li>
							</ul>
						</div>
						<table class="table" id="tabTimeoutPolicy" style="display: none; margin-top: 10px">
							<thead>
								<tr class="noborder">
									<th style="width: 100px">${tk:lang("策略","") }</th>
									<th style="width: 80%">${tk:lang("配置","") }</th>
									<th>${tk:lang("时效（分钟）","") }</th>
									<th style="width: 60px">${tk:lang("","") }</th>
								</tr>
							</thead>
							<tbody id="tbTimeoutPolicy">
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			var nodeUuid = $('#hidProcessStepUuid', that).val();
			var editor = $('#txtDescription', that).ckeditor({
				imageUploadUrl : '${pageContext.request.contextPath}/file/uploadFile.do?belong=FLOW'
			});
			var processStepAttributeList, processAttributeList, workerPolicyList, timeoutPolicyList;
			if (Paper.getUserData()) {
				processAttributeList = Paper.getUserData()['attributeList'];
			}
			var node = Paper.getNodeById(nodeUuid);
			if (node && node.getUserData()) {
				processStepAttributeList = node.getUserData()['attributeList'];
				workerPolicyList = node.getUserData()['workerPolicyList'];
				timeoutPolicyList = node.getUserData()['timeoutPolicyList'];
			}
			if (processStepAttributeList && processAttributeList) {
				for (var i = 0; i < processStepAttributeList.length; i++) {
					var stepAttrObj = processStepAttributeList[i];
					var isExists = false;
					for (var j = 0; j < processAttributeList.length; j++) {
						var processAttrObj = processAttributeList[j];
						if (stepAttrObj.uuid == processAttrObj.uuid) {
							isExists = true;
							stepAttrObj.width = processAttrObj.width;
							stepAttrObj.name = processAttrObj.name;
							stepAttrObj.label = processAttrObj.label;
							stepAttrObj.handler = processAttrObj.handler;
							stepAttrObj.handlerName = processAttrObj.handlerName;
							break;
						}
					}
					if (isExists) {
						var html = xdoT.render('process.process.processstepattributeitem', stepAttrObj);
						$('#divProcessStepAttribute', that).append(html);
					}
				}
			}

			if (workerPolicyList) {
				for (var i = 0; i < workerPolicyList.length; i++) {
					var workerPolicyObj = workerPolicyList[i];
					var data = {};
					data['policy'] = workerPolicyObj.policy;
					data['policyName'] = workerPolicyObj.policyName;
					data['config'] = workerPolicyObj.config;
					data['prevNodeIds'] = $('#hidPrevNodeIds', that).val();
					var html = xdoT.render('process.workerpolicy.workerpolicytr', data);
					$('#tbWorkerPolicy', that).append(html);
					$('#tabWorkerPolicy', that).show();
				}
			}

			if (timeoutPolicyList) {
				for (var i = 0; i < timeoutPolicyList.length; i++) {
					var timeoutPolicyObj = timeoutPolicyList[i];
					var html = xdoT.render('process.timeoutpolicy.timeoutpolicytr', timeoutPolicyObj);
					$('#tbTimeoutPolicy', that).append(html);
					$('#tabTimeoutPolicy', that).show();
				}
			}

			$('#sltForm', that).inputselect({
				url : "${pageContext.request.contextPath}/module/process/form/searchactiveform",
				param : "name",
				valueKey : "uuid",
				textKey : "name"
			});
			$('#chkIsStartNode', that).trigger('change');
			if (Paper.getUserData() && Paper.getUserData()['formId']) {
				$('#lnkFormConfig', that).show();
			}

			if (Paper.getUserData() && Paper.getUserData()['formId']) {
				var formId = Paper.getUserData()['formId'];
				var processStepFormAttributeList = node.getUserData()['formAttributeList'];
				$.getJSON('${pageContext.request.contextPath}/module/process/form/' + formId + '/listattribute', function(data) {
					if (data && data.attributeList && data.attributeList.length > 0) {
						for (var i = 0; i < data.attributeList.length; i++) {
							var obj = data.attributeList[i];
							if (processStepFormAttributeList && processStepFormAttributeList.length > 0) {
								for (var j = 0; j < processStepFormAttributeList.length; j++) {
									if (processStepFormAttributeList[j]['uuid'] == obj['uuid']) {
										obj['isEditable'] = processStepFormAttributeList[j]['isEditable'];
										obj['config'] = processStepFormAttributeList[j]['config'];
										obj['data'] = processStepFormAttributeList[j]['data'];
										break;
									}
								}
							}
							var html = xdoT.render('process.process.processstepattributeitem', obj);
							$('#divProcessStepFormAttribute', that).append(html);
						}
					}
				});
			}
		};
		var fn = {
			'#chkAutoStart' : {
				'change' : function() {
					var that = this.root;
					if ($(this).prop('checked')) {
						$('#hidAutoStart', that).val(1);
					} else {
						$('#hidAutoStart', that).val(0);
					}
				}
			},
			'#chkIsStartNode' : {
				'change' : function() {
					var that = this.root;
					if ($(this).prop('checked')) {
						$('#hidIsStartNode').val('1');
						$('#lnkAssignConfig', that).hide();
						$('#lnkExpireConfig', that).hide();
					} else {
						$('#hidIsStartNode').val('0');
						$('#lnkAssignConfig', that).fadeIn();
						$('#lnkExpireConfig', that).fadeIn();
					}
				}
			},
			'#chkAllowSave' : {
				'change' : function() {
					if ($(this).prop('checked')) {
						$('#hidAllowSave').val('1');
					} else {
						$('#hidAllowSave').val('0');
					}
				}
			},
			'#chkAllowCancel' : {
				'change' : function() {
					if ($(this).prop('checked')) {
						$('#hidAllowCancel').val('1');
					} else {
						$('#hidAllowCancel').val('0');
					}
				}
			},
			'#btnAddAttribute' : {
				'click' : function() {
					var that = this.root;
					var html = xdoT.render('process.process.listprocessattribute', Paper.getUserData() || {});
					var dialog = createSlideDialog({
						title : '${tk:lang("选择流程属性","")}',
						content : html,
						width : 800,
						successFuc : function() {
							var processStepAttributeList = [], processAttributeList = [], newProcessStepAttributeList = [];
							if (Paper.getUserData()) {
								processAttributeList = Paper.getUserData()['attributeList'] || [];
							}
							var node = Paper.getNodeById($('#hidProcessStepUuid', that).val());
							if (node && node.getUserData()) {
								processStepAttributeList = node.getUserData()['attributeList'] || [];
							}
							$('.chkProcessAttribute:checked').each(function() {
								var newuuid = $(this).val();
								var isExists = false;
								for (var i = 0; i < processStepAttributeList.length; i++) {
									if (processStepAttributeList[i].uuid == newuuid) {
										isExists = true;
										break;
									}
								}
								if (!isExists) {
									newProcessStepAttributeList.push({
										"uuid" : newuuid
									});
								}
							});

							if (newProcessStepAttributeList.length > 0 && processAttributeList.length > 0) {
								for (var i = 0; i < newProcessStepAttributeList.length; i++) {
									var stepAttrObj = newProcessStepAttributeList[i];
									var isExists = false;
									for (var j = 0; j < processAttributeList.length; j++) {
										var processAttrObj = processAttributeList[j];
										if (stepAttrObj.uuid == processAttrObj.uuid) {
											isExists = true;
											stepAttrObj.width = processAttrObj.width;
											stepAttrObj.name = processAttrObj.name;
											stepAttrObj.label = processAttrObj.label;
											stepAttrObj.handler = processAttrObj.handler;
											stepAttrObj.handlerName = processAttrObj.handlerName;
											break;
										}
									}
									if (isExists) {
										var html = xdoT.render('process.process.processstepattributeitem', stepAttrObj);
										$('#divProcessStepAttribute', that).append(html);
									}
								}
							}
						}
					});
				}
			},
			'.lnkWorkerPolicy' : {
				'click' : function() {
					var that = this.root;
					var policy = $(this).data('policy');
					var isExist = false;
					$('.hidWorkerPolicy').each(function() {
						if ($(this).val() == policy) {
							isExist = true;
							return false;
						}
					});
					if (!isExist) {
						var policyName = $(this).text();
						var data = {};
						data['policy'] = policy;
						data['policyName'] = policyName;
						data['prevNodeIds'] = $('#hidPrevNodeIds', that).val();
						var html = xdoT.render('process.workerpolicy.workerpolicytr', data);
						$('#tbWorkerPolicy').append(html);
						$('#tabWorkerPolicy').show();
					}
				}
			},
			'.lnkTimeoutPolicy' : {
				'click' : function() {
					var that = this.root;
					var policy = $(this).data('policy');
					var policyName = $(this).text();
					var data = {};
					data['policy'] = policy;
					data['policyName'] = policyName;
					var html = xdoT.render('process.timeoutpolicy.timeoutpolicytr', data);
					$('#tbTimeoutPolicy').append(html);
					$('#tabTimeoutPolicy').show();
				}
			}
		};
	</script>
</form>


