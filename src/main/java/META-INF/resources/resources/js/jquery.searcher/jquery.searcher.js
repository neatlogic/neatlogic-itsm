//类型分为   single  singlerequired   multi   text  date  datetime  month  minputselect  inputselect  select  mselect
(function($) {
	var selectedOption = {};

	$.fn.searcher = function(options) {
		var $target = $(this);
		var newoptions = $.extend(true, {}, $.fn.searcher.defaultopts, options);
		$target.data('config', newoptions);
		
		var appendItem = function(objData, initvalue, parentItem) {
			var opts = $target.data('config');
			var objList = new Array();
			if (objData instanceof Array) {
				objList = objData;
			} else {
				objList.push(objData);
			}
			var isFirst = true;
			for (var o = 0; o < objList.length; o++) {
				var opt = objList[o];
				if (!$.isEmptyObject(opt)) {
					var hit = false;
					var hitvalue = null;
					var hittext = null;
					if (!$.isEmptyObject(initvalue)) {
						for ( var k in initvalue) {
							if (k == opt.name) {
								hit = true;
								hitvalue = initvalue[k];
								break;
							}
						}
					}
					var activeclass = ((typeof opt.activeclass != 'undefined' && opt.activeclass != null && opt.activeclass != '') ? opt.activeclass : 'btn-primary');
					var $div = $('<div style="position: relative;padding:2px 2px 2px 11px;margin:3px" class="searcher_container"></div>');
					if (typeof opt.parent != 'undefined') {
						$div.data('opt-parent', opt.parent);
						if (o == objList.length - 1) {
							$div.css({
								'background' : 'url(/balantflow/resources/images/icons/enter.gif) no-repeat',
								'background-position' : 'center left',
								'border-top' : '1px dashed #ccc'
							});
						}
						$div.addClass('child');
					}
					var $label = $('<div class="seacher_label" title="' + opt.label + '">' + opt.label + '：</div>');
					var $cc = $('<div style="margin-left:120px;" class="itemgroup"></div>');
					$cc.data('opt-type', opt.type);
					$cc.data('opt-activeclass', activeclass);
					$cc.data('opt-label', opt.label);
					$cc.data('opt-name', opt.name);
					var needAppend = false;
					if ((opt.type == 'single' || opt.type == 'singlerequired') && opt.items != null) {
						for (var c = 0; c < opt.items.length; c++) {
							needAppend = true;
							var hitc = false;
							var control = opt.items[c];

							if (hit && hitvalue != null) {
								if (typeof hitvalue == 'object') {// 数组
									hittext = (hittext == null ? new Array() : hittext);
									for ( var h in hitvalue) {
										if (hitvalue[h] == control.value) {
											hitc = true;
											hittext.push(control.text);
										}
									}
								} else {// string or number and so on
									if (hitvalue == control.value) {
										hitc = true;
										hittext = control.text;
									}
								}
							}

							var $btn = $('<button type="button" class="searcher_' + opt.name + ' btn btn-xs btn-default item" style="margin:2px">' + control.text + '</button>');
							$btn.data('opt-value', control.value);
							$btn.data('opt-text', control.text);
							$btn.data('opt', opt);
							$cc.append($btn);
							$btn.on('click', function() {
								var $item = $(this);
								var opt = $item.data('opt');
								var returnValue;
								if (opt.type == 'single') {
									returnValue = actionSingle(this).value;
								} else {
									returnValue = actionSingleRequired(this).value;
								}
								if (typeof opt.url != 'undefined' && opt.url != null && opt.url != '') {
									var next = $item.closest('.searcher_container').next('.child');
									while (next.length > 0) {
										var tmp = next;
										next = next.next('.child');
										tmp.remove();
									}
									if (returnValue != null) {
										$.ajax({
											type : "GET",
											url : opt.url + returnValue,
											dataType : "json",
											async : true,
											success : function(data) {
												appendItem(data, initvalue, $item.closest('.searcher_container'));
											}
										});
									}
								}
							});

							if (hitc) {
								$btn.trigger('click');
							}
						}
					} else if (opt.type == 'multi' && opt.items != null) {
						for (var c = 0; c < opt.items.length; c++) {
							needAppend = true;
							var hitc = false;
							var control = opt.items[c];

							if (hit && hitvalue != null) {
								if (typeof hitvalue == 'object') {// 数组
									hittext = (hittext == null ? new Array() : hittext);
									for ( var h in hitvalue) {
										if (hitvalue[h] == control.value) {
											hitc = true;
											hittext.push(control.text);
										}
									}
								} else {// string or number and so on
									if (hitvalue == control.value) {
										hitc = true;
										hittext = control.text;
									}
								}
							}

							var $btn = $('<button type="button" class="searcher_' + opt.name + ' btn btn-xs btn-default item" style="margin:2px">' + control.text + '</button>');
							$btn.data('opt-value', control.value);
							$btn.data('opt-text', control.text);
							$btn.on('click', function() {
								var $item = $(this);
								var returnValue = actionMulti(this).value;
							});
							$cc.append($btn);
							if (hitc) {
								$btn.trigger('click');
							}
						}
					} else if (opt.type == 'text') {
						needAppend = true;
						var $txt = $('<input type="text" placeholder="' + (opt.placeholder || '') + '" class="searcher_' + opt.name + 'input-sm form-control input-large" check-type="' + (opt.checkType || '') + '">');
						$txt.on('input', function() {
							actionTxt(this);
						});
						$cc.append($txt);
						if (hit && hitvalue != null) {
							$txt.val(hitvalue);
							$txt.trigger('input');
						}
					} else if (opt.type == 'date') {
						needAppend = true;
						var $txt = $('<input type="text" class="searcher_' + opt.name
								+ ' Wdate input-sm form-control input-large" check-type="' + (opt.checkType || '') + '" id="' + (opt.id || '') + '" onfocus="WdatePicker({dateFmt:\'yyyy-MM-dd\'' + ( opt.mindate ? ',minDate:\''+ opt.mindate +'\'' : '' ) +''+ ( opt.maxdate ? ',maxDate:\''+ opt.maxdate +'\'' : '' ) +'})">');
						$txt.on('blur', function() {
							actionTxt(this);
						});
						
						
						$cc.append($txt);
						if (hit && hitvalue != null) {
							$txt.val(hitvalue);
							$txt.trigger('blur');
						}else if(opt.value !=null){
							$txt.val(opt.value);
						}
					}else if (opt.type == 'datetime') {
						needAppend = true;
						var $txt = $('<input type="text" class="searcher_' + opt.name
								+ ' Wdate  form-control input-sm input-large" onfocus="WdatePicker({dateFmt:\'yyyy-MM-dd HH:mm:ss\'})" check-type="' + (opt.checkType || '') + '">');
						$txt.on('blur', function() {
							actionTxt(this);
						});
						$cc.append($txt);
						if (hit && hitvalue != null) {
							$txt.val(hitvalue);
							$txt.trigger('blur');
						}else if(opt.value !=null){
							$txt.val(opt.value);
						}
					} else if (opt.type == 'month') {
						needAppend = true;
						var $txt = $('<input type="text" class="searcher_' + opt.name + ' Wdate  form-control input-sm input-large" onfocus="WdatePicker({dateFmt:\'yyyy-MM\'})" check-type="' + (opt.checkType || '') + '">');
						$txt.on('blur', function() {
							actionTxt(this);
						});
						$cc.append($txt);
						if (hit && hitvalue != null) {
							$txt.val(hitvalue);
							$txt.trigger('blur');
						}
					} else if (opt.type == 'minputselect' || opt.type == 'inputselect') {
						if (typeof opt.sourceurl != 'undefined' && opt.sourceurl != null && opt.sourceurl != '') {
							needAppend = true;
							var $slt = $('<select class="searcher_' + opt.name + '" ' + (opt.type == 'minputselect' ? ' multiple' : '') + '  check-type="' + (opt.checkType || '') + '"></select>');
							if (typeof opt.url != 'undefined' && opt.url != null && opt.url != '') {
								$slt.data('url', opt.url);
							}

							for ( var k in opts.initvaluetexts) {
								if (k == opt.name) {
									for (var i = 0; i < opts.initvaluetexts[k].value.length; i++) {
										$slt.append('<option selected value="' + opts.initvaluetexts[k].value[i] + '">' + opts.initvaluetexts[k].text[i] + '</option>');
									}
								}
							}

							$slt.on('change', function() {
								var $item = $(this);
								var returnValue = actionSelect(this).value;
								if (typeof $(this).data('url') != 'undefined' && $(this).data('url') != null && $(this).data('url') != '') {
									var next = $item.closest('.searcher_container').next('.child');
									while (next.length > 0) {
										var tmp = next;
										next = next.next('.child');
										tmp.remove();
									}
									if (returnValue && returnValue.length > 0) {
										$.ajax({
											type : "GET",
											url : opt.url + returnValue,
											dataType : "json",
											async : true,
											success : function(data) {
												appendItem(data, initvalue, $item.closest('.searcher_container'));
											}
										});
									}
								}
							});
							$cc.append($slt);
							if (hit && hitvalue != null) {
								$slt.trigger('change');
							}
							$slt.inputselect({
								url : opt.sourceurl
							});
						}
					} else if (opt.type == 'select' || opt.type == 'mselect') {
						needAppend = true;
						var $slt = $('<select class="searcher_' + opt.name + ' form-control ' + (opt.type == "select" ? "input-sm" : "") + '" '
								+ (opt.type == 'mselect' ? "multiple" : "") + ' check-type="' + (opt.checkType || '') + '"></select>');
						if (opt.items && opt.items.length > 0) {
							if (opt.type == 'select') {
								$slt.loadSelectWithDefault(opt.items);
							} else {
								$slt.loadSelect(opt.items);
							}
							// $slt.data('opt-value', control.value);
							// $slt.data('opt-text', control.text);
						}
						if (typeof opt.url != 'undefined' && opt.url != null && opt.url != '') {
							$slt.data('url', opt.url);
						}
						$slt.on('change', function() {
							var $item = $(this);
							var returnValue = actionSelect(this).value;
							if (typeof $(this).data('url') != 'undefined' && $(this).data('url') != null && $(this).data('url') != '') {
								var next = $item.closest('.searcher_container').next('.child');
								while (next.length > 0) {
									var tmp = next;
									next = next.next('.child');
									tmp.remove();
								}
								if (returnValue != null) {
									$item.after('<img src="/balantflow/resources/images/loading.gif">');
									$.ajax({
										type : "GET",
										url : opt.url + returnValue,
										dataType : "json",
										async : true,
										success : function(data) {
											$item.next('img').remove();
											appendItem(data, initvalue, $item.closest('.searcher_container'));
										}
									});
								}
							}
						});
						var defaultids = '';
						if (opt.trigger && opt.triggerUrl && opt.triggerEvent) {
							$('.searcher_' + opt.trigger).each(function() {
								$(this).data('target', $slt);
								$(this).data('targetdata', opt);
							});
							$('.searcher_' + opt.trigger).on(opt.triggerEvent, function() {
								// $(document).on(opt.triggerEvent,
								// '.searcher_' +
								// opt.trigger, function() {
								var item = $(this);
								var valuetexts = $(this).closest('.itemgroup').data('opt-data');
								var ids = '';
								if (valuetexts && valuetexts.value) {
									for (var v = 0; v < valuetexts.value.length; v++) {
										ids += valuetexts.value[v];
										if (v < valuetexts.value.length - 1) {
											ids += ',';
										}
									}
								}
								var targetdata = $(this).data('targetdata');
								$.getJSON(targetdata.triggerUrl + ids, function(data) {
									var selectconfig = {};
									if (targetdata.textkey) {
										selectconfig['textkey'] = targetdata.textkey;
									}
									if (targetdata.valuekey) {
										selectconfig['valuekey'] = targetdata.valuekey;
									}
									if (item.data('target').attr('multiple')) {
										item.data('target').loadSelect(data, selectconfig).trigger('change');
									} else {
										item.data('target').loadSelectWithDefault(data, selectconfig).trigger('change');
									}
									if (item.data('target').checkselect) {
										item.data('target')[0].checkselect.reload();
									}
								});
								// });
							});
							/*
							 * if (!triggerMap[opt.name]) {
							 * 
							 * triggerMap[opt.name] = true; }
							 */

							if (opts.initvalues && opts.initvalues[opt.trigger]) {
								for (var v = 0; v < opts.initvalues[opt.trigger].length; v++) {
									defaultids += opts.initvalues[opt.trigger][v];
									if (v < opts.initvalues[opt.trigger].length - 1) {
										defaultids += ',';
									}
								}
							}
						}
						if (opt.triggerUrl) {
							$.ajax({
								url : opt.triggerUrl + defaultids,
								success : function(data) {
									var selectconfig = {};
									if (opt.textkey) {
										selectconfig['textkey'] = opt.textkey;
									}
									if (opt.valuekey) {
										selectconfig['valuekey'] = opt.valuekey;
									}
									if ($slt.attr('multiple')) {
										$slt.loadSelect(data, selectconfig);
									} else {
										$slt.loadSelectWithDefault(data, selectconfig);
									}
								},
								dataType : 'json',
								async : false
							});
						}
						$cc.append($slt);
						if (hit && hitvalue != null) {
							$slt.val(hitvalue);
							$slt.trigger('change');
						}
						$slt.checkselect({
							searchAble : true
						});
					}

					if (needAppend) {
						$div.append($label).append($cc);
						if (typeof parentItem != 'undefined') {
							parentItem.after($div);
						} else {
							$containerBody.append($div);
						}
						// 初始化选择数据
						var data = {};
						data[opt.name] = {};
						if (!hit) {
							data[opt.name].value = null;
							data[opt.name].text = null;
						} else {
							data[opt.name].value = hitvalue;
							data[opt.name].text = hittext;
						}
						data[opt.name].label = opt.label;
						selectedOption = $.extend(true, selectedOption, data);
						// 初始化选择数据
					}
				}
			}
		};

		var actionSelect = function(that) {
			var $cc = $(that).closest('.itemgroup');
			var data = {};
			data.value = new Array();
			data.text = new Array();
			// var v = $.trim($(that).val());
			// var t = $(that).children(':selected').text();
			// data.value = v == '' ? null : v;
			// data.text = t == '' ? null : t;
			$(that).find('option').each(function() {
				if ($(this).prop('selected')) {
					data.value.push($(this).val());
					data.text.push($(this).text());
				}
			});
			data.label = $cc.data('opt-label');
			if (data.value.length == 0) {
				data.value = null;
				data.text = null;
			}
			$cc.data('opt-data', data);
			return data;
		};

		var actionTxt = function actionTxt(that) {
			var $cc = $(that).closest('.itemgroup');
			var data = {};
			var v = $.trim($(that).val());
			data.value = v == '' ? null : v;
			data.text = v == '' ? null : v;
			data.label = $cc.data('opt-label');
			$cc.data('opt-data', data);
			return data;
		};

		var actionMulti = function(that) {
			var $cc = $(that).closest('.itemgroup');
			var activeclass = $cc.data('opt-activeclass');
			var data = $cc.data('opt-data');
			if (typeof data == 'undefined' || data == null || data.value == null) {
				data = {};
				data.value = new Array();
				data.text = new Array();
			}
			if ($(that).attr('actived')) {
				$(that).removeClass(activeclass).addClass('btn-default').removeAttr('actived');
				for ( var v in data.value) {
					if (data.value[v] == $(that).data('opt-value')) {
						data.value.splice(v, 1);
						break;
					}
				}
				for ( var t in data.text) {
					if (data.text[t] == $(that).data('opt-text')) {
						data.text.splice(t, 1);
						break;
					}
				}
			} else {
				$(that).removeClass('btn-default').addClass(activeclass).attr('actived', 'actived');
				data.value.push($(that).data('opt-value'));
				data.text.push($(that).data('opt-text'));
			}
			data.label = $cc.data('opt-label');
			if (data.value.length == 0) {
				data.value = null;
				data.text = null;
			}
			$cc.data('opt-data', data);
			return data;
		};

		var actionSingleRequired = function(that) {
			var $cc = $(that).closest('.itemgroup');
			var activeclass = $cc.data('opt-activeclass');
			var data = {};
			if ($(that).attr('actived')) {// 不可反选
				data.value = $(that).data('opt-value');
				data.text = $(that).data('opt-text');
			} else {
				$cc.children().removeAttr('actived').removeClass(activeclass).addClass('btn-default');
				$(that).removeClass('btn-default').addClass(activeclass).attr('actived', 'actived');
				data.value = $(that).data('opt-value');
				data.text = $(that).data('opt-text');
			}
			data.label = $cc.data('opt-label');
			$cc.data('opt-data', data);
			return data;
		};

		var actionSingle = function(that) {
			var $cc = $(that).closest('.itemgroup');
			var activeclass = $cc.data('opt-activeclass');
			var data = {};
			if ($(that).attr('actived')) {
				$(that).removeClass(activeclass).addClass('btn-default').removeAttr('actived');
				data.value = null;
				data.text = null;
				// data.value = $(that).attr('opt-value');
				// data.text = $(that).attr('opt-text');
			} else {
				$cc.children().removeAttr('actived').removeClass(activeclass).addClass('btn-default');
				$(that).removeClass('btn-default').addClass(activeclass).attr('actived', 'actived');
				data.value = $(that).data('opt-value');
				data.text = $(that).data('opt-text');
			}
			data.label = $cc.data('opt-label');
			$cc.data('opt-data', data);
			return data;
		};

		var getQueryString = function() {
			var opts = $target.data('config');
			var returnval = '';
			for ( var k in opts.initvalues) {
				if (returnval != '') {
					returnval += '&';
				}
				returnval = returnval + k + '=' + opts.initvalues[k].toString();
			}
			return encodeURI(returnval);
		};

		var getQueryStringJson = function() {
			var opts = $target.data('config');
			var returnJson = {};
			for ( var k in opts.initvalues) {
				returnJson[k] = opts.initvalues[k].toString();
			}
			return returnJson;
		};

		this.getValue = function() {
			return selectedOption;
		};

		this.getInitValues = function() {
			var opts = $target.data('config');
			return opts.initvalues;
		};

		this.getQueryString = function() {
			return getQueryString();
		};

		this.getQueryStringJson = function() {
			return getQueryStringJson();
		}

		this.getInitTexts = function() {
			var opts = $target.data('config');
			return opts.inittexts;
		};

		this.getInitValueTexts = function() {
			var opts = $target.data('config');
			return opts.initvaluetexts;
		};

		this.appendItem = function(options, initvalues) {
			appendItem(options, initvalues);
		}
		var btnClear = '<i class="ts-remove searcher_clear"></i>';

		if (!$target.data('bind')) {
			$target.wrap('<div class="searcher_wrapper"></div>');
			var $wraper = $target.closest('.searcher_wrapper');
			var $container = $('<div class="searcher_main"></div>');
			var $containerBody = $('<div class="searcher_container_body"></div>');
			var $containerFoot = $('<div class="searcher_container_foot action-group" ></div>');
			var $btnConfirm = $('<button type="button" class="btn btn-sm btn-primary">确认</button>');
			var $btnCancel = $('<button type="button" class="btn btn-sm btn-default">取消</button>');
			$target.data('container', $container);
			$target.data('containerbody', $containerBody);
			$target.addClass("searcher_target");

			$container.on('keydown', function(e) {
				e.stopPropagation();
				if (e.keyCode == 13) {
					$btnConfirm.trigger('click');
				}
			});

			$btnConfirm.on('click', function() {
			    if($wraper.valid && !$wraper.valid()){
					return false;
				}
				var opts = $target.data('config');
				selectedOption = {};
				$containerBody.find('.itemgroup').each(function() {
					var optname = $(this).data('opt-name');
					var optlabel = $(this).data('opt-label');
					var data = {};
					data[optname] = {};
					data[optname] = $(this).data('opt-data');
					selectedOption = $.extend(selectedOption, data);
				});
			    if(opts.validFn && opts.validFn != null){
			    	if(!opts.validFn(selectedOption)){
			    		return false;
			    	}
			    }
				var returnValue = '';
				var returnInitData = {};
				var returnInitText = {};
				var returnInitTextData = {};
				for ( var k in selectedOption) {
					var value = selectedOption[k];
					if (value.value != null && value.value[0] != '') {
						returnValue += '<b style="margin-left:2px">' + value.label + '</b>' + '：<font style="text-decoration:underline;margin-right:2px;">' + value.text
								+ '</font>;';
						returnInitData[k] = value.value;
						returnInitText[value.label] = value.text;
						returnInitTextData[k] = {
							value : value.value,
							text : value.text
						};
					}
				}
				opts.initvalues = returnInitData;
				opts.inittexts = returnInitText;
				opts.initvaluetexts = returnInitTextData;
				$target.html(returnValue);
				if (returnValue != '') {
					var $btnClear = $(btnClear);
					$btnClear.on('click', function(e) {
						e.stopPropagation();
						var opts = $target.data('config');
						opts.initvalues = {};
						opts.inittexts = {};
						opts.initvaluetexts={};
						$target.html('');
						$container.slideUp(function() {
							$containerBody.empty();
						});
						if (opts.clear) {
							opts.clear();
						}
					});
					$target.append($btnClear);
				}
				$container.slideUp(function() {
					$containerBody.empty();
				});
				if (opts.confirm && opts.confirm != null) {
					var querystring = getQueryString();
					opts.confirm(querystring);
				}
			});

			$btnCancel.on('click', function() {
				$container.slideUp(function() {
					$containerBody.empty();
				});
			});

			$containerFoot.append($btnConfirm).append($btnCancel);
			$container.append($containerBody).append($containerFoot);

			$container.css('z-index', $target.data('config').zindex);
			$target.after($container);
			$target.on('click', function() {
				if (!$container.is(':visible')) {
					var options = $target.data('config').options;
					var initvalues = $target.data('config').initvalues;
					var top = $target.outerHeight(true);
					var left = $target.position().left;

					if (options != null && options.length > 0) {
						appendItem(options, initvalues);
						$containerFoot.show();
					} else {
						$containerBody.html('<div style="margin:5px;" class="alert alert-danger"><b>提示：</b>' + $target.data('config').emptytext + '</div>');
						$containerFoot.hide();
					}

					$container.css({
						'top' : top,
						'left' : left,
						'width' : $target.outerWidth(true)
					});
					$container.slideDown();
				} else {
					$container.slideUp(function() {
						$containerBody.empty();
					});
				}
			});

			$target.attr('data-bind', true);
		} else {
			if ($target.data('container').is(':visible')) {
				$target.data('container').slideUp('fast', function() {
					$target.data('containerbody').empty();
				});
			}
		}

		if ($target.data('config').inittexts && !$.isEmptyObject($target.data('config').inittexts)) {
			var returnValue = '';
			for ( var k in $target.data('config').inittexts) {
				var values = $target.data('config').inittexts[k];
				var val = '';
				if (typeof values == 'string') {
					val = values;
				} else {
					for (var v = 0; v < values.length; v++) {
						val += values[v];
						if (v < values.length - 1) {
							val += ',';
						}
					}
				}
				returnValue += '<b>' + k + '</b>' + '：<font style="text-decoration:underline">' + val + '</font> ; ';
			}
			$target.html(returnValue);
			if (returnValue != '') {
				var $btnClear = $(btnClear);
				$btnClear.on('click', function(e) {
					e.stopPropagation();
					var opts = $target.data('config');
					opts.initvalues = {};
					opts.inittexts = {};
					$target.html('');
					$container.slideUp(function() {
						$containerBody.empty();
					});
					if (opts.clear) {
						opts.clear();
					}
				});
				$target.append($btnClear);
			}
		} else {
			$target.empty();
		}
		return this;
	};

	$.fn.searcher.defaultopts = {
		options : [],
		initvalues : {},
		zindex : 3334,
		inittexts : {},
		initvaluetexts : {},
		emptytext : '没有定义任何搜索组件。',
		confirm : null,
		clear : null
	};

})(jQuery);