draw2d.shape.node.FlowCondition = draw2d.shape.node.FlowBaseImage.extend({
	NAME : 'draw2d.shape.node.FlowCondition',
	CNNAME : '条件分流',
	allowBack : false,
	allowRefire : false,
	allowAssign : false,
	allowEoa : false,
	allowGrade : false,
	allowCondition : false,
	isValid : function() {
		var color = new draw2d.util.Color("#ff0000");
		var connections = this.getConnections();
		if (connections.getSize() > 0) {
			var sourcecount = 0;
			for (var i = 0; i < connections.getSize(); i++) {
				var conn = connections.get(i);
				if (conn.getTarget().getParent().getId() == this.getId()) {// 前置步骤
					sourcecount += 1;
				}
			}

			if (sourcecount > 1) {
				// this.addFigure(this.stateFigure, new
				// draw2d.layout.locator.RightLocator(this));
				// return '条件分流节点只允许关联一个前置节点。';
			}
		}
		var d = this.getUserData();
		if (d == null || d.name == '' || d.rule == '') {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '请设置节点信息。';
		}
		if (typeof d.returnvalue == 'object' && d.returnvalue.length > 0) {
			for (var i = 0; i < d.returnvalue.length; i++) {
				if ($.trim(d.returnvalue[i]) == '') {
					this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
					return '返回值不能为空。';
				}
				for (var j = i + 1; j < d.returnvalue.length; j++) {
					if (d.returnvalue[i] == d.returnvalue[j] && d.nextstepid[i] == d.nextstepid[j]) {
						this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
						return '返回值：' + d.returnvalue[i] + '至跳转步骤：' + d.nextstepid[i] + '已存在。';
					}
				}
			}
		}

		for (var c = 0; c < this.getChildren().getSize(); c++) {
			if (this.getChildren().get(c).NAME == 'draw2d.shape.basic.Image') {
				this.removeFigure(this.stateFigure);
			}
		}
		return true;
	},
	onContextMenu : function(x, y) {
		$.contextMenu({
			selector : 'body',
			events : {
				hide : function() {
					$.contextMenu('destroy');
				}
			},
			callback : $.proxy(function(key, options) {
				switch (key) {
				case 'info':
					var that = (this.NAME == 'draw2d.shape.node.FlowCondition' ? this : this.getParent());
					var userData = that.getUserData();
					var name = that.CNNAME, rule = '', targetid, returnvalue, nextstepid;
					if (userData != null) {
						name = userData.name || that.CNNAME;
						rule = userData.rule;
						returnvalue = userData.returnvalue;
						nextstepid = userData.nextstepid;
						targetid = userData.targetid;
					}

					var form = $('#dialogForm');
					var editor = null;
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);

					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.condition.base'));
					var txtStepName = form.find('#txtStepName');
					var txtRule = form.find('#txtRule');
					var divParam = form.find('#divParam');
					var divNextStep = form.find('#divNextStep');
					var btnTest = form.find('#btnTest');
					var sltTargetId = form.find('#sltTargetId');
					var loadPropFn = function(tid){
						var preNode = null;
						// var formid = '', propid = '';
						var formIdList = new Array();
						var propIdList = new Array();
						if(tid && typeof tid == 'object'){
							for(var ti = 0; ti < tid.length; ti++){
								if(tid[ti] != ''){
									preNode = that.getCanvas().getFigure(tid[ti]);
									if(preNode != null && preNode.getComposite() != null){// 如果是组合节点，则取组合节点的信息
										var cl = preNode.getComposite().getAssignedFigures();
										for (var di = 0; di < cl.getSize(); di++) {
											var dn = cl.get(di);
											if (dn.getName() != 'draw2d.shape.node.FlowStart') {
												preNode = dn;
												break;
											}
										}
									}
									var preData = preNode.getUserData();
									if (preData != null) {
										if (preData.form) {
											formIdList.push(preData.form);
										}
										if (preData.propid && preData.propid != '') {
											propIdList.push(preData.propid);
										}
									}
								}
							}
						}

                            var param = {};
                            param.formId = [];

                            if(formIdList.length > 0){
                                for(var f = 0; f < formIdList.length; f++){
                                    /*if(param != ''){
                                        param += '&';
                                    }
                                    param += 'formId=' + formIdList[f];*/
                                    param.formId.push(formIdList[f]);
                                }
                            }
                            param.propId = [];
                            if (propIdList.length>0) {
                                for(var p = 0; p < propIdList.length; p++){
                                    /*if(param != ''){
                                        param += '&';
                                    }
                                    param += 'propId=' + propIdList[p];*/
                                    param.propId.push(propIdList[p]);
                                }
                            }



                            var d = that.getUserData();
                            var prevStepList = new draw2d.util.ArrayList();
                            var groupIdList = new draw2d.util.ArrayList();
                            that.findAllPrevNode(prevStepList, that);
                            var prevList = [];
                            for (var f = 0; f < prevStepList.getSize(); f++) {
                                var figure = prevStepList.get(f);
                                prevList.push({name: figure.getName(), targetid: figure.id});
                            }
                            var selectedParam = $('#sltTargetId').val();
                            param.stepTypes = [];
                            for (j = 0; j < prevList.length; j++) {
                                for (i = 0; i < selectedParam.length; i++) {
                                    if (selectedParam[i] == prevList[j].targetid) {
                                        param.stepTypes.push(prevList[j].name);
                                    }
                                }
                            }
                            $.getJSON('getNewAllParameterJson.do?', param, function (data) {
                                var count = 0;
                                var table, tr;
                                for (var i in data) {
                                    if (count == 0) {
                                        table = $('<table class="table table-hover"></table>');
                                        th = $('<thead><tr class="noborder"><th>变量名称</th><th>说明</th><th></th></tr></thead>');
                                        table.append(th);
                                    }
                                    if (i != 'techsure_status' && i != 'techsure_message') {
                                       // tr = $('<tr style="text-align:left"><td>${' + i + '}</td><td>' + (data[i].split(','))[0] + '</td></tr>');
                                        tr = $('<tr style="text-align:left"><td nowrap>${' + i + '}</td><td>' + data[i] + '</td></tr>');

                                        table.append(tr);
                                        count += 1;
                                    }
                                }
                                if (count > 0) {
                                    divParam.html(table);
                                } else {
                                    divParam.html('没有找到任何表单参数和自定义属性。');
                                }
                            });

                        };

					txtStepName.val(name);
					txtRule.val(rule);

					editor = CodeMirror.fromTextArea($('#txtRule')[0], {
						mode : "javascript",
						lineNumbers : true
					});

					$('#divDialog').bind('beforeSave', function() {
						txtRule.val(editor.getValue());
					});
					// editor.setSize('100%', 200);

					var prevStepList = new draw2d.util.ArrayList();
					that.findAllPrevNode(prevStepList, that);
					for ( var f = 0; f < prevStepList.getSize(); f++) {
						var figure = prevStepList.get(f);
						if(figure.allowCondition){
							var opt = $('<option value="' + figure.id + '">' + figure.getLabelText() + '</option>');
							sltTargetId.append(opt);
						}
					}
					sltTargetId.val(targetid);
					sltTargetId.checkselect();

					if(targetid){
						loadPropFn(targetid);
					}
					sltTargetId.change(function(){
						var tid = $(this).val();
						loadPropFn(tid);
					});

					var nextStepList = new draw2d.util.ArrayList();
					that.findNextNode(nextStepList, that);

					if (nextStepList.getSize() > 0) {
						var nextStepTab, nextStepTh = '';
						nextStepTab = $('<table class="table table-hover"></table>');
						nextStepTh = $('<thead><tr class="noborder"><th>返回值</th><th>跳转节点</th><th></th></tr></thead>');
						nextStepTab.append(nextStepTh);

						var nextStepTr = $('<tr  class="returnItem"></tr>');
						var returnTd = $('<td></td>');
						var stepIdTd = $('<td></td>');
						var btnTd = $('<td style="text-align:right" nowrap></td>');
						var txtReturnValue = $('<input type="text" name="returnvalue" class="txtReturnValue form-control input-sm input-small">');
						// var hidStepId = $('<input type="hidden" value="' +
						// nextUid + '" name="nextstepid">');
						var sltStepId = $('<select name="nextstepid" style="width:100%" class="sltStepId form-control input-sm"></select');
						for ( var s = 0; s< nextStepList.getSize(); s++) {
							var nn = nextStepList.get(s);
							sltStepId.append('<option value="' +nn.id + '">' + nn.getLabelText() + '</option>');
						}
						var btnAdd = $('<button type="button" class="btn btn-noborder"><i class="ts-plus"></i></button>');
						var btnDel = $('<button type="button" class="btn btn-noborder"><i class="ts-minus"></i></button>');
						btnDel.on('click', function() {
							if ($('.returnItem').length <= 1) {
								showPopMsg.info('最后一个不能删除');
							} else {
								$(this).closest('.returnItem').remove();
							}
						});
						btnAdd.on('click', function() {
							var tr = $(this).closest('.returnItem').clone(true);
							nextStepTab.append(tr);
						});

						returnTd.append(txtReturnValue);
						stepIdTd.append(sltStepId);
						btnTd.append(btnAdd).append(btnDel);
						nextStepTr.append(returnTd).append(stepIdTd).append(btnTd);
						if (returnvalue && nextstepid) {// 有返回值
							if (typeof (returnvalue) == 'string') {
								nextStepTr.find('.txtReturnValue').val(returnvalue);
								nextStepTr.find('.sltStepId').val(nextstepid);
								nextStepTab.append(nextStepTr);
							} else {
								for ( var s in nextstepid) {
									var tr = nextStepTr.clone(true);
									tr.find('.txtReturnValue').val(returnvalue[s]);
									tr.find('.sltStepId').val(nextstepid[s]);
									nextStepTab.append(tr);
								}
							}
						} else {// 没有返回值
							nextStepTr.append(returnTd).append(stepIdTd).append(btnTd);
							nextStepTab.append(nextStepTr);
						}
						divNextStep.html(nextStepTab);
					} else {
						divNextStep.html('没有找到任何后置节点。');
					}

					btnTest.click(function() {
						if (formid != '' && editor.getValue() != '') {
							var iframe = $('<iframe style="width:100%;height:100%" frameborder="0" scroll="auto" src="getFormContentById.do?formId=' + formid + '">');
							createModalDialog({
								msgwidth : 680,
								msgheight : 400,
								msgtitle : "模拟测试",
								msgcontent : iframe,
								checkReturn : true,
								successFuc : function() {
									var formContent = iframe[0].contentWindow.getFormValue();
									$.post('checkRuleAjax.do', {
										'content' : formContent,
										'rule' : editor.getValue()
									}, function(data) {
										if (data.Status == 'OK') {
											showPopMsg.success("流转规则验证成功，返回结果：" + data.Message);
										} else {
											showPopMsg.error("流转规则验证失败，存在语法错误：" + data.Message, 10);
										}
									}, 'json');
								}
							});
						} else if (formid == '') {
							showPopMsg.info('请设置前置步骤的关联表单。');
						} else if (editor.getValue() == '') {
							showPopMsg.info('请填写流转规则。');
						}
					});
					break;
				default:
					break;
				}
				;
			}, this),
			x : x,
			y : y,
			items : {
				'info' : {
					name : '编辑信息'
				}
			}
		});
	}
});