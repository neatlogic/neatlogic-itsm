draw2d.shape.node.FlowServiceRest = draw2d.shape.node.FlowBaseService.extend({
	NAME : "draw2d.shape.node.FlowServiceRest",
	CNNAME : 'RESTFul接口',
	isValid : function() {
		var connections = this.getConnections();
		var color = new draw2d.util.Color("#ff0000");
		var d = this.getUserData();
		if (connections.getSize() > 0) {
			var sourcecount = 0;
			var targetcount = 0;
			var hasgradestep = false;
			for (var c = 0; c < connections.getSize(); c++) {
				var conn = connections.get(c);
				if (conn.getTarget().getParent().getId() == this.getId()) {
					sourcecount += 1;
				} else {
					targetcount += 1;
				}
			}

			if (sourcecount > 1) {
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return 'RESTFul接口节点只能关联一个前置节点。';
			}

			if (targetcount > 0) {
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return 'RESTFul接口节点不能关联后置节点。';
			}
		}

		if (d == null || d.name == '' || !d.url) {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '请设置节点信息。';
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
				case "base":
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowServiceRest' ? this : this.getParent());
					var name = that.CNNAME, url = '', key = '', expression = '', value = '', trigger = 'finish', paramName = null, paramKey = null, customParamName = null, customParamKey = null;
					var userData = that.getUserData();
					if (userData != null) {
						name = userData.name || that.CNNAME;
						url = userData.url;
						expression = userData.expression;
						key = userData.key;
						value = userData.value;
						paramName = userData.paramName;
						paramKey = userData.paramKey;
						trigger = userData.trigger;
						customParamName = userData.customParamName;
						customParamKey = userData.customParamKey;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					$.ajax({
						type : "GET",
						url : '/balantflow/resources/js/flow-component/template/rest/base.html',
						async : false,
						success : function(template) {
							form.html(template);
						}
					});
					var txtUrl = form.find('#txtUrl');
					var txtValue = form.find('#txtValue');
					var txtKey = form.find('#txtKey');
					var sltExpression = form.find('#sltExpression');
					var txtStepName = form.find('#txtStepName');
					var tabParam = form.find('#tabParam');
					var tabCustomParam = form.find('#tabCustomParam');
					var btnAdd = form.find('#btnAdd');
					var btnAddCustom = form.find('#btnAddCustom');
					var sltTrigger = form.find('#sltTrigger');
					var connections = that.getConnections();
					var formid = '', propid = '';
					for (var i = 0; i < connections.getSize(); i++) {
						var conn = connections.get(i);
						if (conn.getTarget().getParent().getId() == that.getId()) {// 前置步骤
							var preNode = conn.getSource().getParent();
							if (preNode.getComposite() != null) {// 如果是组合节点，则取组合节点的信息
								var cl = preNode.getComposite().getAssignedFigures();
								for (var d = 0; d < cl.getSize(); d++) {
									var dn = cl.get(d);
									if (dn.getName() != 'draw2d.shape.node.FlowStart') {
										preNode = dn;
										break;
									}
								}
							}
							var preData = preNode.getUserData();
							if (preData != null) {
								if (preData.form && preData.form != '') {
									formid = preData.form;
								}
								if (preData.propid && preData.propid != '') {
									propid = preData.propid;
								}
							}
						}
					}

					var it = {};
					it.paramName = '';
					it.paramKey = '';
					$.ajax({
						type : "GET",
						url : '/balantflow/flow/getAllParameterJson.do?formId=' + formid + '&propId=' + propid,
						async : false,
						dataType : 'json',
						success : function(data) {
							it.params = data;
						}
					});

					var paramFn = doT.template($('#tmpParam').html());
					if (paramName && paramKey) {
						if (typeof paramName != 'string') {
							for (var i = 0; i < paramName.length; i++) {
								if (paramName[i] != '' && paramKey[i] != '') {
									it.paramName = paramName[i];
									it.paramKey = paramKey[i];
									var html = paramFn(it);
									tabParam.append(html);
								}
							}
						}
					}
					
					btnAdd.click(function() {
						var html = paramFn(it);
						tabParam.append(html);
					});
					
					var cit = {};
					var customParamFn = doT.template($('#tmpCustomParam').html());
					if(customParamName && customParamKey){
						if (typeof customParamName != 'string') {
							for (var i = 0; i < customParamName.length; i++) {
								if (customParamName[i] != '' && customParamKey[i] != '') {
									cit.paramName = customParamName[i];
									cit.paramKey = customParamKey[i];
									var html = customParamFn(cit);
									tabCustomParam.append(html);
								}
							}
						}
					}
					
					btnAddCustom.click(function() {
						var html = customParamFn({});
						tabCustomParam.append(html);
					})

					txtStepName.val(name);
					txtUrl.val(url);
					txtValue.val(value);
					txtKey.val(key);
					sltExpression.val(expression);
					sltTrigger.val(trigger);
					break;
				default:
					break;
				}
				;
			}, this),
			x : x,
			y : y,
			items : {
				'base' : {
					name : '基本设置'
				}
			}
		});
	}
});