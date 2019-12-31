var formbuilder = (function() {
	return {
		replacePattern: function(template, conf){
			var tempFn = doT.template(template);
			var resultText = tempFn(conf);
			//console.log(JSON.stringify(conf, null, 2));
			return $(resultText);
		},
		replacePattern_bak : function(json, conf) {
			var jsonStr = JSON.stringify(json, null, 2);
			if (conf != null) {
				for ( var c in conf) {
					var regex = new RegExp("#" + c + "\\|[^#]*?#", "ig");
					jsonStr = jsonStr.replace(regex, conf[c]);
				}
			}
			jsonStr = jsonStr.replace(/\#[^\|]*?\|([^\#]*?)\#/ig, "$1");
			return JSON.parse(jsonStr);
		},
		generateNode : function(json) {
			var node = $('<' + json.tag + '>');
			for ( var attr in json.attr) {
				node.attr(attr, json.attr[attr]);
			}
			if (json.text) {
				node.html(json.text);
			}
			if (json.children) {
				for ( var n = 0; n < json.children.length; n++) {
					node.append(arguments.callee(json.children[n]));
				}
			}
			return node;
		},
		generateEditor : function(json, target) {
			json = $.extend(true, {}, json, translateDomToJson(target));
			var popoverConf = {};
			popoverConf.title = json.title;
			var form = $('<form role="form"></form>');
			for ( var t in json.fields) {
				var type = t;
				var field = json.fields[t];
				if (field) {
					var div = $('<div class="form-group"></div>');
					var label = $('<label class="control-label" style="display:block">' + field.label + '</label>');
					var control = null;
					if (field.type == 'input' || field.type == 'textarea') {
						control = $('<input maxlength="30" type="text" name="' + type + '" class="form-control input-sm" value="' + field.value + '">');
					} else if (field.type == 'checkbox') {
						control = $('<input name="' + type + '" type="checkbox" ' + (field.value == true ? "checked" : "") + '>');
					} else if (field.type == 'textarea-split') {
						control = $('<textarea name="' + type + '" class="form-control" style="height:120px"></textarea>');
						var val = '';
						for ( var v in field.value) {
							val += field.value[v] + '\r\n';
						}
						val = val.substring(0, val.length - 2);
						control.val(val);
					} else if (field.type == 'select') {
						var defval = field.selected;
						control = $('<select name="' + type + '" class="form-control input-sm"></select>');
						for ( var i = 0; i < field.value.length; i++) {
							var option = $('<option></option>');
							for ( var v in field.value[i]) {
								var vv = field.value[i][v];
								if (v == 'label') {
									option.text(vv);
								} else if (v == 'value') {
									option.val(vv);
									if (vv == defval) {
										option.prop('selected', 'selected');
									}
								}
							}
							control.append(option);
						}
					}
					if (control != null) {
						div.append(label).append(control);
					}
					form.append(div);
				}
			}
			var btnOk = $('<button type="button" class="btn btn-info btn-sm btnOK" style="margin-right:10px">保存</button>');
			var btnDel = $('<button type="button" class="btn btn-danger btn-sm btnDel" style="margin-right:10px">删除</button>');
			var btnCancel = $('<button type="button" class="btn btn-sm btn-default btnCancel">取消</button>');
			form.append(btnOk).append(btnDel).append(btnCancel);
			popoverConf.container = 'body';
			popoverConf.content = form;
			popoverConf.html = true;
			popoverConf.trigger = 'click';
			popoverConf.placement = 'auto';
			return popoverConf;
		},
		translateJsonToDom : function(node, type, form) {
			var conf = form.toJson();
			var newConf = {}
			for(var c in conf){
				if(conf[c].indexOf('\r\n') > -1){
					newConf[c] = conf[c].split('\r\n');
				}else{
					newConf[c] = conf[c];
				}
			}
			var newNode = formbuilder.initNode(type, newConf);
			node.popover('hide');
			$('.popover').remove();
			node.replaceWith(newNode);
		},
		initNode : function(type, conf) {
			var node = null;
			$.ajax({
				type : "GET",
				url : '/balantflow/resources/js/form-builder/template_old/' + type + '.html',
				async : false,
				success : function(template) {
					try{
						node = formbuilder.replacePattern(template, conf);
						node.attr('controltype', type);
						node.addClass('autoGenControl');
					}catch(e){
						showPopMsg.error(e.message);
					}
				}
			});
			$.ajax({
				type : "GET",
				url : '/balantflow/resources/js/form-builder/editor_old/' + type + '.js',
				async : false,
				dataType : "json",
				success : function(data) {
					var popoverConf = formbuilder.generateEditor(data, node);
					node.addClass('labPopover').popover(popoverConf);
					node.on('shown.bs.popover', function() {
						var that = $(this);
						$('.labPopover').each(function(){
							if(!$(this).is(that)){
								$(this).popover('hide');
							}
						});
						
						$('.popover').find('.btnCancel').click(function() {
							node.popover('hide');
							$('.popover').remove();
						});

						$('.popover').find('.btnOK').click(function() {
							formbuilder.translateJsonToDom(node, type, popoverConf.content);
						});

						$('.popover').find('.btnDel').click(function() {
							node.popover('hide');
							node.remove();
							$('.popover').remove();
						});
					});
				}
			});
			return node;
		},
		rebindNode : function(node) {
			var type = node.attr('controltype');
			if(type){
				$.ajax({
					type : "GET",
					url : '/balantflow/resources/js/form-builder/editor/' + type + '.js',
					async : false,
					dataType : "json",
					success : function(data) {
						var popoverConf = formbuilder.generateEditor(data, node);
						node.addClass('labPopover').popover(popoverConf);
						node.on('shown.bs.popover', function() {
							var that = $(this);
							$('.labPopover').each(function(){
								if(!$(this).is(that)){
									$(this).popover('hide');
								}
							});
							
							$('.popover').find('.btnCancel').click(function() {
								node.popover('hide');
								$('.popover').remove();
							});

							$('.popover').find('.btnOK').click(function() {
								formbuilder.translateJsonToDom(node, type, popoverConf.content);
							});

							$('.popover').find('.btnDel').click(function() {
								node.popover('hide');
								node.remove();
								$('.popover').remove();
							});
						});
					}
				});
			}
			return node;
		}
	}

	function translateDomToJson(node) { //把已生成的控件dom重新转换为json
		var json = {};
		var fields = {};
		var label = node.find('label:first');
		fields.label = {
			"value" : label.text()
		};
		var controls = node.find('input');
		if (controls.length <= 0) {
			controls = node.find('textarea');
		}
		if (controls.length <= 0) {
			controls = node.find('select');
		}
		controls.each(function() {
			var control = $(this);

			if(control.attr('type') == 'radio'){//单选框组装数据
				if(fields.radios && fields.radios.value){
					fields.radios.value.push(control.attr('value') + (control.prop('checked')?"|checked" : ""));
				}else{
					var radioList = new Array();
					radioList.push(control.attr('value') + (control.prop('checked')?"|checked" : ""));
					fields.radios = {};
					fields.radios.value = radioList;
				}
			}else if(control.attr('type') == 'checkbox'){//复选框组装数据
				if(fields.checkboxs && fields.checkboxs.value){
					fields.checkboxs.value.push(control.attr('value')+ (control.prop('checked')?"|checked" : ""));
				}else{
					var checkboxList = new Array();
					checkboxList.push(control.attr('value')+ (control.prop('checked')?"|checked" : ""));
					fields.checkboxs = {};
					fields.checkboxs.value = checkboxList;
				}
			}else if(control[0].tagName.toLowerCase() == 'select'){//下拉框组装数据
				fields.options = {};
				fields.options.value = new Array();
				control.children().each(function(){
					fields.options.value.push($(this).text() + ($(this).prop('selected')?"|selected" : ""));
				});
			}

			if (control.attr('name')) {
				fields.id = {
					"value" : control.attr('name')
				};
			} else {
				fields.id = {
					"value" : ""
				};
			}

			if (control.attr('placeholder')) {
				fields.placeholder = {
					"value" : control.attr('placeholder')
				};
			}

			if(control.attr('timeformat')){
				fields.timeformat = {
					"value" : control.attr('timeformat')
				}
			}

			if (control.attr('check-type') == 'required') {
				fields.required = {
					"selected" : "required"
				};
			}

			if(control.attr('inputheight')){
				fields.inputheight = {
					"value" : control.attr('inputheight')
				}
			}
			/*if (control.hasClass('input-sm')) {
				fields.inputheight = {
					"selected" : "input-sm"
				};
			} else if (control.hasClass('input-df')) {
				fields.inputheight = {
					"selected" : "input-df"
				};
			} else if (control.hasClass('input-lg')) {
				fields.inputheight = {
					"selected" : "input-lg"
				};
			}*/

			if (control.hasClass('input-mini')) {
				fields.inputsize = {
					"selected" : "input-mini"
				};
			} else if (control.hasClass('input-small')) {
				fields.inputsize = {
					"selected" : "input-small"
				};
			} else if (control.hasClass('input-medium')) {
				fields.inputsize = {
					"selected" : "input-medium"
				};
			} else if (control.hasClass('input-large')) {
				fields.inputsize = {
					"selected" : "input-large"
				};
			} else if (control.hasClass('input-xlarge')) {
				fields.inputsize = {
					"selected" : "input-xlarge"
				};
			} else if (control.hasClass('input-xxlarge')) {
				fields.inputsize = {
					"selected" : "input-xxlarge"
				};
			}
		});
		json.fields = fields;
		return json;
	}

}());