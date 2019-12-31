var formbuilder = (function() {
	return {
		replacePattern : function(template, conf) { // 拼凑标签html
			var tempFn = doT.template(template);
			var resultText = tempFn(conf);
			return $(resultText);
		},
		ajaxGetSonBind : function(selector, sonId, type, pValue) {// 异步根据父数据源添加子下拉框
			var isMultiple = !(selector.attr('multiple') == 'multiple');// 判断是否多选,是：false
			var selectText = '<option value="">请选择...</option>';
			var pName = selector.attr('parentbindname');// 绑定的parentName
			var pNameVals = "";
			// 获取该下拉框所有父的“name=value”
			if (pName && pName != "undefined") {
				var pNames = pName.split(',');

				for (var j = 0; j < pNames.length; j++) {
					var pVal = $('.htmleaf-content').find('select[name="' + pNames[j] + '"]').val();
					if (pVal) {
						pNameVals = pNameVals + "&" + pNames[j] + '=' + pVal;
					}
				}
			}
			var jsonUrl = "/balantflow/prop/set/" + sonId + "?1=1" + pNameVals;
			jsonUrl = jsonUrl.replace("1=1&", "");
			jsonUrl = jsonUrl.replace("1=1", "");
			if (typeof sonId != 'undefined' && sonId != '') {
				$.getJSON(jsonUrl, function(data) {
					if (data.Status == "OK") {
						selector.children().remove();
						if (isMultiple) {
							selector.append(selectText);
						}
						for ( var i in data.Data) {
							var option = '<option setid="' + sonId + '" value="' + data.Data[i].value + '">' + data.Data[i].text + '</option>';
							selector.append(option);
						}
					} else {
						selector.append('<option value="">数据源加载失败</option>');
					}
				});
			}
		},
		sourceSonBind : function(selector, sonId) {// selector,sonId:初始化node下拉框;undefined,undefined:初始化子选择selecttype
			if (typeof sonId != 'undefined' && sonId != 'undefined') {
				formbuilder.ajaxGetSonBind(selector, sonId);
			} else {
				$('.sourceson').remove();
			}
		},
		getSelectNames : function(form, target, parentName, nodeName) {// 获取所有能绑定的Select的name
			var nodeSelector = form.find("select[name='parentbindname']");
			var selected = '';
			nodeSelector.children().remove();
			if ($('.htmleaf-content').find('select').length == 0) {// 新增
				nodeSelector.append('<option value="">没有父数据源!</option>');
			} else {
				if ($('.htmleaf-content').find('select').length == 1) {
					if ($('.htmleaf-content').find('select').attr("name") == target.find('select').attr("name")) {
						nodeSelector.append('<option value="">没有父数据源!</option>');
					}
				}
			}

			$('.htmleaf-content').find('select').each(function() {
				var selected = '';
				var $thisName = $(this).attr('name');
				var parentNames = parentName.split(',');
				if (nodeName != $thisName) {
					if (parentNames.length > 1) {
						for (var i = 0; i < parentNames.length; i++) {
							if (parentNames[i] == $thisName) {// 初始化选中项
								selected = 'selected="selected"';
								break;
							}
						}
					} else {
						if (parentName == $thisName) {// 初始化选中项
							selected = 'selected="selected"';
						}
					}

					var option = '<option ' + selected + ' value="' + $thisName + '">' + $thisName + '</option>';
					nodeSelector.append(option);
				}
			})
			nodeSelector.change(formbuilder.sourceSonBind);
		},
		sourceExistBind : function(form, target, parentName, nodeName) {// 异步添加下拉框绑定数据源(flow_prop_option_set)
			var div = $('<div class="form-group sourceexist"></div>');
			var label = $('<label class="control-label" style="display:block">绑定数据集</label>');
			var selector = $('<select plugin-checkselect name="resources" class="form-control input-sm"></select>');
			selector.append('<option value="">请选择...</option>');
			$.getJSON('/balantflow/prop/getProSets.do', function(data) {
				if (data.Status == 'OK') {
					for ( var i in data.Data) {
						if (target.find("select").attr('controltypeid') == data.Data[i].id) {// 初始化选中项
							var selected = 'selected="selected"';
						} else {
							var selected = '';
						}
						var option = '<option ' + selected + ' value="' + data.Data[i].id + '">' + data.Data[i].name + '</option>';
						selector.append(option);
					}
				} else {
					selector.children().remove();
					selector.append('<option value="">数据源加载失败</option>');
				}
				div.append(label);
				div.append(selector);
				$('.sourceexist').remove();
				form.find("input[name='reselect']:first").parents(".form-group").after(div);
				formbuilder.checkBindShow(form);
				formbuilder.sourceSonBind();
				formbuilder.getSelectNames(form, target, parentName, nodeName);
				form.find('select[name="parentbindname"]').closest(".form-group").show();
			});

		},
		initSourceTextarea : function(SourceTextarea, sender, selectElement, form, target) {// 点击node初始化自定义选项
			var sourceTextarea = form.find(SourceTextarea);// 自定义数据源
			var val = "";
			target.find(sender).each(function() {
				if (sender == "option") {
					val += $(this).val() +($(this).prop('selected')?'|selected':'')+ '\r\n';
				} else {
					if (sender.indexOf('checkbox')) {
						val += $(this).find('input').val() +($(this).find('input').prop('checked')?'|checked':'') +'\r\n';
					} else {
						val += $(this).find(selectElement).val() +($(this).find(selectElement).prop('checked')?'|checked':'') +'\r\n';
					}
				}
			});
			val = val.substring(0, val.length - 2);
			sourceTextarea.val(val);
		},
		generateEditor : function(json, target) {// 编辑标签

			json = $.extend(true, {}, json, formbuilder.translateDomToJson(target));
			var popoverConf = {};
			var nodeSelector = target.find("select");
			var sourceid = nodeSelector.attr("controltypeid");
			var parentName = nodeSelector.attr("parentbindname");
			var nodeName = nodeSelector.attr("name");
			var isSave = (target.attr('class') == 'grid-stack-item');// 判断是点击node还是save
			// editform
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
						control = $('<select ' + field.multi + ' name="' + type + '" class="form-control input-sm"></select>');
						for (var i = 0; i < field.value.length; i++) {
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
					} else if (field.type == 'radio') {
						controlText = "";
						for (var i = 0; i < field.value.length; i++) {
							var labelR = "";
							var valueR = "";
							for ( var v in field.value[i]) {
								var vv = field.value[i][v];
								if (v == 'label') {
									labelR = vv;
								} else if (v == 'value') {
									valueR = vv;
								}
							}
							controlText += "<label class='radio-inline'><input type='radio' name='" + type + "' value='" + valueR + "' >" + labelR + "</label>";
						}
						control = $(controlText);
					}

					if (control != null) {
						div.append(label).append(control);
					}
					form.append(div); 
				}
			}
			var selectChangeLinkage = function() {// 联动select change事件
				var $this = $(this);
				var parentName = $this.attr('name');
				var parentVal = $this.val();
				$('.htmleaf-content').find('select').each(function() {// 找到所有select
					var parentNames = $(this).attr('parentbindname');
					parentNames = parentNames.split(',');
					for (var i = 0; i < parentNames.length; i++) {
						if (parentNames[i] == parentName) {
							var sourceid = $(this).attr('controltypeid');
							formbuilder.ajaxGetSonBind($(this), sourceid, 'linkage', parentVal);
						}
					}
				});
			}
			var sourceRadioClick = function() {// 数据源单选框点击选择事件
				if ($(this).val() == "exist") {
					formbuilder.sourceExistBind(form, target, parentName, nodeName);

					form.find("textarea[name='options']").parent().addClass('hide');
				} else {
					form.find("textarea[name='options']").parent().removeClass('hide');
					form.find("select[name='resources']").parent().remove();
				}
				formbuilder.checkBindShow(form);
			}
			if (json.title == "单选下拉框" || json.title == "多选下拉框") {

				// 如果没有初始化选中数据源，则初始化自定义选项
				var sourceSelect = form.find("select[name='resources']");// 数据源下拉框
				var sourceTextarea = form.find("textarea[name='options']");// 自定义数据源
				if (typeof sourceid == "undefined") { // 数据源是否为已存在数据集
					formbuilder.initSourceTextarea("textarea[name='options']", "option", '', form, target);
					form.find("input[type='radio'][value='custom']").prop("checked", true);
					// 清空绑定关系
					nodeSelector.attr("parentbindname", "");
					var nodeName = nodeSelector.attr("name");
					formbuilder.removeColor($('.grid-stack-item-content'));

					sourceSelect.parent().addClass('hide');
					sourceTextarea.parent().removeClass('hide');
				} else {
					formbuilder.sourceExistBind(form, target, parentName, nodeName);
					form.find("input[type='radio'][value='exist']").prop("checked", true);
					sourceTextarea.parent().addClass('hide');
					sourceSelect.parent().removeClass('hide');
				}
				if (form.find('select[name="parentbindname"]').val()) {
					formbuilder.sourceSonBind();
				}
				$('.htmleaf-content').find('select').each(function() {
					$(this).unbind();
					$(this).bind('change', formbuilder.selectChangeLinkage);
				})
				formbuilder.getSelectNames(form, target, parentName, nodeName);// 获取所有能绑定的Select的name，初始化下拉框

				formbuilder.checkBindShow(form);
				// 隐藏
				form.find("input[type='radio']").bind("click", sourceRadioClick);// 绑定单选框点击事件
			} else if (json.title == "多行单选框") {
				formbuilder.initSourceTextarea("textarea[name='radios']", ".radio", 'input[type="radio"]', form, target);
			} else if (json.title == "单行单选框") {
				formbuilder.initSourceTextarea("textarea[name='radios']", ".radio-inline", 'input[type="radio"]', form, target);
			} else if (json.title == "单行复选框") {
				formbuilder.initSourceTextarea("textarea[name='checkboxs']", ".checkbox-inline", 'input[type="checkbox"]', form, target);
			} else if (json.title == "多行复选框") {
				formbuilder.initSourceTextarea("textarea[name='checkboxs']", ".checkbox", 'input[type="checkbox"]', form, target);
			}
			var buttonGroup = $('<div class="editBtn"></div>');
			var btnOk = $('<button type="button" class="btn btn-info btn-sm btnOK" style="margin-right:5px">保存</button>');
			var btnDel = $('<button type="button" class="btn btn-danger btn-sm btnDel" style="margin-right:5px">删除</button>');
			var btnCancel = $('<button type="button" class="btn btn-sm btn-default btnCancel">取消</button>');

			buttonGroup.append(btnOk).append(btnDel).append(btnCancel);
			form.append(buttonGroup);
			popoverConf.container = '#popoverContent';
			popoverConf.content = form;
			popoverConf.html = true;
			popoverConf.trigger = 'click';
			popoverConf.placement = 'false';
			return popoverConf;
		},
		translateJsonToDom : function(node, type, form) {
			var conf = form.toJson();
			var flag = true;
			if (type == 'select' || type == 'mselect') {
				var controlName = conf.id;
				var controlNameOrigin = node.find('select').attr('name');
				if (controlName != controlNameOrigin) { // 判断改的name是否和原来的name一样
					if ($('select[name="' + controlName + '"]').length > 0) {
						showPopMsg.error('name 已存在');
						flag = false;
					} else {
						// 重新绑定联动关系
						$('.htmleaf-content').find('select').each(function() {
							var parentName = $(this).attr('parentbindname');
							var parentNames = parentName.split(',');
							var parentNameNew = '';
							for (var i = 0; i < parentNames.length; i++) {
								if (parentNames[i] == controlNameOrigin) {
									parentNameNew = parentNameNew + controlName + ",";
								} else {
									parentNameNew = parentNameNew + parentNames[i] + ",";
								}
							}
							// console.log(parentNameNew.substring(0,parentNameNew.length-1))
							$(this).attr('parentbindname', parentNameNew.substring(0, parentNameNew.length - 1));
						})
						/*
						 * $('.htmleaf-content').find('select[parentbindname="' +
						 * controlNameOrigin + '"]').each(function() {
						 * $(this).attr('parentbindname', controlName); });
						 */
					}
				}
				var resourceCheck = form.find('input[type="radio"][name="reselect"]:checked');
				var resourceSelect = form.find('.sourceexist').find('select').val();
				if (resourceCheck.val() == 'exist' && resourceSelect == '') {
					showPopMsg.error('如果选择数据集，请绑定数据集');
					flag = false;
				}
			}
			if (flag) {

				var newConf = {}
				for ( var c in conf) {
					if (conf[c].indexOf('\r\n') > -1) {
						newConf[c] = conf[c].split('\r\n');
					} else {
						newConf[c] = conf[c];
					}
				}
				formbuilder.initNode(type, newConf, function(newNode) {
					$('.popover').remove();
					var grid = $('#simpleContent').data('gridstack');
					grid.remove_widget(node, true);// 移除
					grid.add_widget($(newNode), node.attr("data-gs-x"), node.attr("data-gs-y"), node.attr("data-gs-width"), node.attr("data-gs-height"), false, true);
					var $selectNode = $(newNode).children().first();
					$selectNode.addClass('select-node');
					formbuilder.addParentSonColor($selectNode);
				});
			}
		},
		nodeClick : function(data, node, type) {
			if (!node.children().first().hasClass('select-node')) {
				// 选中的node添加的背景颜色
				formbuilder.removeColor($('.grid-stack-item-content'));
				popoverConf = formbuilder.generateEditor(data, node);// 获取最新的editForm
				$("#popoverContent").html(popoverConf.content);
				$("#popoverContent").removeClass('hide');
				var $selectNode = $(node).children().first();
				formbuilder.addParentSonColor($selectNode);
				$selectNode.addClass("select-node");
				// 初始化绑定确定按钮事件
				$('#popoverContent').find('.btnOK').bind('click', function() {
					formbuilder.translateJsonToDom(node, type, popoverConf.content);// 根据最新editForm内容替换node
				});
				// 初始化绑定删除按钮事件
				$('#popoverContent').find('.btnDel').bind('click', function() {
					var grid = $('#simpleContent').data('gridstack');
					grid.remove_widget(node, true);// 移除
					$("#popoverContent").toggleClass('hide');

				});

			}
			// 新增绑定节点点击切换编辑模块
			if($('#popoverBtn')){
				$('#popoverBtn').tab('show');
			}
		},
		initNode : function(type, conf, callback) { // 以下两种情况下会执行initNode方法：1.拖动新增标签(conf
			// = "undefined"),2.保存编辑
			if (!conf) {
				conf = {};
			}
			var node = null;
			var typeJson;
			var templateHtml = '';
			var callArray = new Array();
			callArray.push($.getJSON('/balantflow/resources/js/form-builder/editor/' + type + '.js', function(json) {
				typeJson = json;
				if (!conf.id) {
					for ( var f in json.fields) {
						var field = json.fields[f];
						if (field.value == 'selectbasic') {
							field.value = 'selectbasic' + selectId;
							selectId++;
						}
						if (field.type == 'select') {
							conf[f] = field.selected;
						} else {
							conf[f] = field.value;
						}
					}
				}
			}));

			callArray.push($.get('/balantflow/resources/js/form-builder/template/' + type + '.html', function(template) {
				templateHtml = template;
			}));

			$.when.apply($, callArray).done(function() {
				try {
					node = formbuilder.replacePattern(templateHtml, conf);
					node.attr('controltype', type);
					// 如果是下拉框同时异步初始化数据源
					selector = node.find("select");
					sourceid = selector.attr("controltypeid");
					if (((type == "select" || type == "mselect") && conf.resources)) {// 保存edit，才执行下面代码
						selector.attr('controltypeid', conf.resources);// 点击标签初始化editForm
						formbuilder.sourceSonBind(selector, conf.resources); // 初始化node的下拉框选项
					}
					// --------------------------------------------
					popoverConf = formbuilder.generateEditor(typeJson, node);
					node.addClass('labPopover');
					$("#popoverContent").html(popoverConf.content);
					// 初始化绑定确定按钮事件
					$('#popoverContent').find('.btnOK').bind('click', function() {
						formbuilder.translateJsonToDom(node, type, popoverConf.content);
					});
					// 初始化绑定删除按钮事件
					$('#popoverContent').find('.btnDel').bind('click', function() {
						var grid = $('#simpleContent').data('gridstack');
						grid.remove_widget(node, true);// 移除
						$("#popoverContent").toggleClass('hide');

					});
					node.on('click', function() {
						formbuilder.nodeClick(typeJson, node, type);
					});
					$('#popoverContent').removeClass('hide');

					if (callback) {
						callback(node);
					}
				} catch (e) {
					showPopMsg.error(e.message);
				}
			});

			// 新增绑定节点点击切换编辑模块
			if($('#popoverBtn')){
				$('#popoverBtn').tab('show');
			}			
			// return node;

		},
		rebindNode : function(node) {
			var type = node.attr('controltype');
			if (type) {
				$.getJSON('/balantflow/resources/js/form-builder/editor/' + type + '.js', function(data) {
					node.on('click', function() {
						formbuilder.nodeClick(data, node, type);
					});

				});
			}
			return node;
		},
		addParentSonColor : function(node) { // 设置选择选择框的父框和子框的颜色
			var $select = node.find('select');
			if ($select.length > 0) {
				var name = $select.attr('name');
				var parentName = $select.attr('parentbindname');
				var gridstackContentParent = '';
				formbuilder.removeColor($('.grid-stack-item-content'));
				node.addClass('select-node');
				var parentNames = parentName.split(',');
				var nameReg = new RegExp(name);
				$('.htmleaf-content').find('select').each(function() { // 设置所有子选择框的颜色
					if (nameReg.test($(this).attr('parentbindname')) > 0) {
						$(this).parents('.grid-stack-item-content').addClass('select-node-son');
						$(this).parents('.grid-stack-item-content').append('<span class="badge" aria-hidden="true">子</span>');
					}
				});
				if (parentNames.length > 1) {
					for (var i = 0; i < parentNames.length; i++) {
						gridstackContentParent = $('.htmleaf-content').find('select[name="' + parentNames[i] + '"]').parents('.grid-stack-item-content');
						gridstackContentParent.addClass('select-node-parent'); // 设置父选择框的颜色
						gridstackContentParent.append('<span class="badge" aria-hidden="true">父</span>');
					}
				} else {
					gridstackContentParent = $('.htmleaf-content').find('select[name="' + parentName + '"]').parents('.grid-stack-item-content');
					gridstackContentParent.addClass('select-node-parent'); // 设置父选择框的颜色
					gridstackContentParent.append('<span class="badge" aria-hidden="true">父</span>');
				}
			}

		},
		checkBindShow : function(form) { // 设置选择父控件和匹配type 显示or隐藏
			var $resources = form.find('select[name="resources"]');
			var $parentbindname = form.find('select[name="parentbindname"]');
			// var $selecttype = form.find('select[name="selecttype"]');
			if ($resources.length == 0) { // 隐藏 选择父控件和匹配type
				$parentbindname.closest(".form-group").hide();
				// $selecttype.parent().hide();
			}
		},
		removeColor : function(node) {
			node.removeClass('select-node');
			node.removeClass('select-node-son');
			node.removeClass('select-node-parent');
			$('.badge').remove();
		},
		translateDomToJson : function(node) { // 把已生成的控件dom重新转换为json
			var json = {};
			var fields = {};
			var label = node.find('label:first');
			fields['form-label'] = {// label标签组装数据
				"value" : label.text().replace('：', '')
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
				if (control.attr('type') == 'radio') {// 单选框组装数据
					if (fields.radios && fields.radios.value) {
						fields.radios.value.push(control.attr('value') + (control.prop('checked') == true ? "|checked" : "")); 
					} else {
						var radioList = new Array();
						radioList.push(control.attr('value') + (control.prop('checked') == true ? "|checked" : ""));
						fields.radios = {};
						fields.radios.value = radioList;
					}
				} else if (control.attr('type') == 'checkbox') {// 复选框组装数据
					if (fields.checkboxs && fields.checkboxs.value) {
						fields.checkboxs.value.push(control.attr('value') + (control.prop('checked') == true ? "|checked" : ""));
					} else {
						var checkboxList = new Array();
						checkboxList.push(control.attr('value') + (control.prop('checked') == true ? "|checked" : ""));
						fields.checkboxs = {};
						fields.checkboxs.value = checkboxList;
					}
				} else if (control[0].tagName.toLowerCase() == 'select') {// 下拉框组装数据
					fields.options = {};
					fields.options.value = new Array();
					control.children().each(function() {
						fields.options.value.push($(this).text() + ($(this).prop('selected') ? "|selected" : ""));
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

				if (control.attr('timeformat')) {
					fields.timeformat = {
						"value" : control.attr('timeformat')
					}
				}

				if (control.attr('x-check-type') == 'required') {
					fields.required = {
						"selected" : "required"
					};
				}

				if (control.attr('x-check-type') == 'needselect') {
					fields.required = {
						"selected" : "needselect"
					};
				}
				if (control.attr('x-check-type') == 'radio') {
					fields.required = {
						"selected" : "radio"
					};
				}
				if (control.attr('inputheight')) {
					fields.inputheight = {
						"value" : control.attr('inputheight')
					}
				}
				if (control.attr('x-check-type') == 'checkbox') {
					fields.required = {
						"selected" : "checkbox"
					};
				}
				fields['form-group'] = {
					"value" : control.attr('form-group')
				};
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
	}
}());