;
(function($) {
	var InputSelect = function(target, config) {
		this.defaultconfig = {
			width : 'auto', // 宽度自适应
			height : 32, // 高度，与其他输入框一致
			url : null, // 调用地址
			minLength : 2, // 输入最少几个字符串长度才触发查询事件
			defaultvalue : [], // 下拉固定值
			limit : 15,
			zindex : 5000, // 图层顺序
			enable : target.attr("disabled") ? false : true, // 是否可操作,默认是
			valueKey : 'value', // 存的值的key
			textKey : 'text', // 显示文字的key
			param : 'k',
			delimiter : 188, // 分割符keycode，输入自动换词
			strict : true, // true标示严格匹配，不匹配的值不能添加，false可以输入不匹配的值
			mode : 0, // 0表示模糊匹配，1表示精确匹配
			root : null, // 其他模式数据
			hasClassify : false,// 是否需要分组
			multiple : target.attr("multiple") ? true : false, // 是否支持多选
			placeholder : '请输入关键字', // 提示语
			limitList : false,
			theme : null, // 主题，默认无，可设置为：block
			titleKey : 'text' // 显示下拉title的key，默认为text
		};

		this.config = $.extend({}, this.defaultconfig, config);
		this.$target = target;
		this.$target.hide();
		this.init();

	};
	this.InputSelect = InputSelect;
	InputSelect.prototype = {
		init : function() { // 初始化外层和输入框和下拉外层，如为只读模式不初始化外层
			var that = this;
			var config = that.config;
			that.$target.wrap('<div class="inputselect-container"></div>');
			// 外层容器初始化
			that.$wrapper = that.$target.closest('.inputselect-container');
			that.$wrapper.css('width', typeof config.width == 'number' ? config.width + 'px' : config.width);

			if (config.theme) {
				that.$wrapper.addClass('theme-' + config.theme);
			}
			that.$wrapper.prepend('<span class="inputselect-content" style="line-height:' + (config.height - 2) + 'px;min-height:' + config.height + 'px;'
					+ ((config.width && typeof config.width == 'number') ? 'min-width:' + config.width + 'px;' : ((config.width && config.width.indexOf("%") > 0) ? 'min-width: 100%;' : ''))
					+ '"><span class="jquery-inputselect-valuespan"><input type="text" class="inputselect-inputer" autocomplete="off" autocomplete="new-password"></span><i class="inputselect-arrow ts"></i></span>');
			that.$content = that.$wrapper.find('.inputselect-content');
			that.$target.data('width') && that.$content.css({
				'width' : '100%',
				'overflow' : 'hidden'
			});

			that.$arrow = that.$content.find('.inputselect-arrow ');
			that.$input = that.$content.find('.inputselect-inputer');
			that.$selectedspan = that.$content.find('.jquery-inputselect-valuespan');
			that.$target.data('config', config);
			that.$target.bind('inputselect:reload', function() {
				var $target = that.$target;
				var config = $.extend({}, that.config, $target.data('config'), true);
				that.$selectedspan.find('span').remove();
				that.$target.find('option').each(
						function() {
							var selectedThat = $(this);
							if ($(this).prop('selected') && $(this).val() != '') {
								if (!$(this).text()) {
									var url = config.url;
									var mode = config.mode;
									var limit = 1;
									var keyword = $(this).val();
									if (url.indexOf('?') > -1) {
										url += '&' + config.param + '=' + encodeURIComponent(keyword) + '&limit=1&mode=1';
									} else {
										url += '?' + config.param + '=' + encodeURIComponent(keyword) + '&limit=1&mode=1';
									}
									$.getJSON(url, (function(selectedThat) {
										return function(data) {
											if (config.root) {
												var rs = config.root.split('\\.');
												for (var ri = 0; ri < rs.length; ri++) {
													if (rs[ri]) {
														data = data[rs[ri]];
														if (!data) {
															break;
														}
													}
												}
											}
											if (data.length > 0) {
												if (config.enable) {
													that.$input.before('<span class="jquery_inputselect_item" data-value="' + data[0][config.valueKey] + '"><i class="jquery_inputselect_text">' + data[0][config.textKey]
															+ '</i><i class="ts-remove remove_item"></i></span>');
												} else {
													that.$input.before('<span class="jquery_inputselect_item" data-value="' + data[0][config.valueKey] + '"><i class="jquery_inputselect_text">' + data[0][config.textKey] + '</i></span>');
												}
												that.$input.attr('placeholder', '').css('width', '24px');
												selectedThat.text(data[0][config.textKey]);
												$target.trigger('change');
											}
										}
									}(selectedThat)));
									if (!config.multiple) {
										return false;
									}
								} else {
									if (config.enable) {
										that.$input.before('<span class="jquery_inputselect_item" data-value="' + $(this).val() + '"><i class="noneed jquery_inputselect_text">' + $(this).text() + '</i><i class="ts-remove remove_item"></i></span>');
									} else {
										that.$input.before('<span class="jquery_inputselect_item" data-value="' + $(this).val() + '"><i class="noneed jquery_inputselect_text">' + $(this).text() + '</i></span>');
									}
									that.$input.attr('placeholder', '').css('width', '24px');
									if (!config.multiple) {
										return false;
									}
								}
							}
						});
				if ($target.find('option').length == 0) {
					$target.append('<option value=""></option>');
				} else {
					that.$input.attr('placeholder', '');
				}
			});
			that.$target.bind('inputselect:clear', function() {
				that.$input.prevAll('.jquery_inputselect_item').remove();
				that.$target.find('option').remove();
			});
			that.$target.bind('inputselect:destory', function() {
				if (that.$target.attr('bind-inputselect')) {
					that.$target.find('option').remove();
					that.$dropdown && that.$dropdown.remove();
					that.$content && that.$content.remove();
					that.$target.removeAttr('bind-inputselect');
					that.$target.unwrap();
					that.$target.show();
					that.$target.removeClass('inputselect-select');
					that.$target.unbind('inputselect:destory');
					that.$target.unbind('inputselect:reload');
					that.$target.unbind('inputselect:clear');
				} else {

				}
			});
			that.$target.trigger('inputselect:reload');// 填充数据

			if (config.defaultvalue && config.defaultvalue.length > 0) {
				for ( var i in config.defaultvalue) {
					if (!config.defaultvalue[i].text) {
						var config = that.$target.data('config');
						var url = config.url;
						var keyword = config.defaultvalue[i].value;
						if (url.indexOf('?') > -1) {
							url += '&' + config.param + '=' + encodeURIComponent(keyword) + '&limit=1&mode=1';
						} else {
							url += '?' + config.param + '=' + encodeURIComponent(keyword) + '&limit=1&mode=1';
						}
						$.getJSON(url, (function(i) {
							return function(data) {
								if (config.root) {
									var rs = config.root.split('\\.');
									for (var ri = 0; ri < rs.length; ri++) {
										if (rs[ri]) {
											data = data[rs[ri]];
											if (!data) {
												break;
											}
										}
									}
								}
								if (data.length > 0) {
									config.defaultvalue[i].text = data[0][config.textKey];
								}
							}
						}(i)));
					}

				}
			}
			if (that.$target.find('option[selected]').length > 0) {
				that.$input.attr('placeholder', '');
				that.$input.css({
					'width' : '24px'
				});
			} else {
				that.$input.attr('placeholder', config.placeholder);
			}
			if (config.enable) {
				that.$wrapper.append('<div class="inputselect-dropdown bottom tsscroll-container"></div>');
				that.$dropdown = that.$wrapper.find('.inputselect-dropdown');

				that.searchtimer = null;
				// select变化时，重新初始化绑定数据（存储显示内容）
				that.$target.on('change', function() {
					if (that.$target.find('option').length == 0) {
						that.$target.append('<option value=""></option>');
					}

					var showtext = [];
					for (var i = 0; i < that.$target.find('option:selected').length; i++) {
						showtext.push(that.$target.find('option:selected').eq(i).text());
					}
					that.$target.data('text', showtext.join(';'));
					if (that.$target.val()) {
						that.$input.attr('placeholder', '');
						that.$input.css({
							'width' : '24px'
						});
					} else {
						that.$input.attr('placeholder', config.placeholder);
						that.$input.css({
							'width' : 'auto'
						});
					}
					if (config.hasClassify) {
						var dataArray = [];
						for (var s = 0; s < that.$target.find('option:selected').length; s++) {
							var listArr = {};
							listArr["value"] = that.$target.find('option:selected').eq(s).val();
							listArr["text"] = that.$target.find('option:selected').eq(s).text();
							var seopt = that.$target.find('option:selected').eq(s).data();
							if (seopt) {
								for ( var x in seopt) {
									listArr[x] = seopt[x];
								}
							}
							dataArray.push(listArr);
						}
						that.$target.data('grouplist', dataArray);
					}

				});
				// 内容尺寸变化时，重新计算下拉的位置
				that.$content.on('sizechange', function() {
					that.$dropdown.css({
						'left' : that.$content.offset().left + that.$dropdown.width() < $(window).width() ? that.$content.position().left + 'px' : (that.$content.width() - that.$dropdown.width() + 10) + 'px'
					});
				});
				that.selectedIndex = that.$dropdown.find('.option.selected').length > 0 ? that.$dropdown.find('.option.selected').index() : -1;
				that.$input.on('keydown', function(e) {
					e.stopPropagation();
					if (e.keyCode == 38) {// up
						that.selectedIndex = (that.selectedIndex <= 0) ? that.$dropdown.find('.option').length - 1 : that.selectedIndex - 1;
						that.$dropdown.find('.option').removeClass('selected');
						that.$dropdown.find('.option').eq(that.selectedIndex).addClass('selected');
						if (that.selectedIndex == 0) {
							that.$dropdown.scrollTop(0);
						} else if ((that.$dropdown.find('.option.selected').outerHeight() * that.selectedIndex < that.$dropdown.scrollTop()) || (that.selectedIndex == that.$dropdown.find('.option').length - 1)) {
							that.$dropdown.scrollTop(that.$dropdown.find('.option.selected').outerHeight() * that.selectedIndex);
						}

					} else if (e.keyCode == 40) {// down
						that.selectedIndex = (that.selectedIndex == that.$dropdown.find('.option').length - 1) ? 0 : that.selectedIndex + 1;
						that.$dropdown.find('.option').removeClass('selected');
						that.$dropdown.find('.option').eq(that.selectedIndex).addClass('selected');
						if (that.selectedIndex == 0) {
							that.$dropdown.scrollTop(0);
						} else if (that.$dropdown.find('.option.selected').outerHeight() * that.selectedIndex > that.$dropdown.height()) {
							that.$dropdown.scrollTop(that.$dropdown.find('.option.selected').outerHeight() * that.selectedIndex);
						}
					} else if (e.keyCode == 8) {// backspace
						if ($(this).val() == '') {
							var selected = that.$selectedspan.find('.inputselect-readytodelete');
							var last = that.$selectedspan.find('.jquery_inputselect_item:last');
							if (selected.length > 0) {
								selected.find('.remove_item').trigger('click');
								if (that.$selectedspan.find('.jquery_inputselect_item').length == 0) {
									that.$target.empty();
								}
							} else {
								last.addClass('inputselect-readytodelete');
							}
						}
					} else if (e.keyCode == 13) {// enter
						if (that.$dropdown.find('.option.selected').length > 0 && that.$wrapper.hasClass('on')) {
							// 下拉有选中的，默认回车执行选中点击事件
							that.$dropdown.find('.option.selected').trigger('click');
							return false;
						} else {
							// 如果输入内容与返回结果的某一结果一致，执行选中事件
							var hasHit = false;
							that.$dropdown.find('.option').each(function() {
								if ($(this).text() == that.$input.val() || $(this).val() == that.$input.val()) {
									$(this).trigger('click');
									hasHit = true;
									return false;
								}
							});
							if (!hasHit && !config.strict) {
								if ($.trim(that.$input.val()) != '') {
									var inputval = that.$input.val();
									if (config.addFn && typeof config.addFn == 'function') {
										var returnvalue = config.addFn(inputval);
										returnvalue != false && that.addUndefinedValue(returnvalue, inputval);
									} else {
										that.addUndefinedValue(inputval);
									}
								}
							}
							return false;
						}
					} else if (e.keyCode == config.delimiter) {
						if ($.trim(that.$input.val()) != '') {
							var hasHit = false;
							that.$dropdown.find('.option').each(function() {
								if ($(this).text() == that.$input.val() || $(this).val() == that.$input.val()) {
									$(this).trigger('click');
									hasHit = true;
									return false;
								}
							});
							if (!hasHit && !config.strict) {
								that.addUndefinedValue(that.$input.val());
							}
						}
						return false;
					}
					// 不是删除按钮时需要清除等待删除的选中状态
					if (e.keyCode != 8) {
						that.$selectedspan.find('.inputselect-readytodelete').removeClass('inputselect-readytodelete');
					}
				});
				that.$input.on('blur', function() {
					var keyword = $(this).val();
					if (keyword) {
						var text_length = 0;
						for (var i = 0; i < keyword.length; i++) {
							if (keyword.charCodeAt(i) > 256) {
								text_length = text_length + 1;
							} else {
								text_length = text_length + 0.5;
							}
						}
						$(this).css('width', text_length * 14 + 30);
					} else {
						if ($(this).siblings().length == 0) {
							$(this).attr('placeholder', config.placeholder);
							var text_length = 0;
							for (var i = 0; i < config.placeholder.length; i++) {
								if (config.placeholder.charCodeAt(i) > 256) {
									text_length = text_length + 1;
								} else {
									text_length = text_length + 0.5;
								}
							}
							$(this).css('width', text_length * 14 + 30);
						} else {
							$(this).css({
								'width' : '24px'
							});
						}
					}
				});

				$(document).on('click', function(e) {
					var ishide = true;
					if (e.target) {
						if (($(e.target).parents('.inputselect-container').length > 0 && $(e.target).parents('.inputselect-container').is(that.$wrapper)) || ($(e.target).is(that.$wrapper))) {
							ishide = false;
						}
					}
					if (ishide) {
						that.toggleShow('hide');
					}
				});
				that.$wrapper.on('click', function(e) {
					e.stopPropagation();
				});

				that.$input.on('focus', function(e) {
					var keyword = $(this).val();
					if (!keyword && config.url && that.$selectedspan.find('.jquery_inputselect_item').length == 0) {
						if (config.url.indexOf('?') > -1) {
							url = config.url + '&' + config.param + '=' + encodeURIComponent(keyword) + '&limit=' + config.limit + '&mode=' + config.mode;
						} else {
							url = config.url + '?' + config.param + '=' + encodeURIComponent(keyword) + '&limit=' + config.limit + '&mode=' + config.mode;
						}
						that.doSearch(url, that.$input);
					}
				});
				that.$content.on('click', function(e) {
					that.$input.focus();
				});
				that.$content.on('click', '.jquery_inputselect_item', function(e) {
					e.stopPropagation();
				});
				that.$input.on('input', function() {
					var keyword = $(this).val();
					var text_length = 0;
					for (var i = 0; i < keyword.length; i++) {
						if (keyword.charCodeAt(i) > 256) {
							text_length = text_length + 1;
						} else {
							text_length = text_length + 0.5;
						}
					}
					$(this).css('width', text_length * 14 + 30).attr('placeholder', '');
					var url = '';
					if (keyword.length >= config.minLength && config.url) {
						if (config.url.indexOf('?') > -1) {
							url = config.url + '&' + config.param + '=' + encodeURIComponent(keyword) + '&limit=' + config.limit + '&mode=' + config.mode;
						} else {
							url = config.url + '?' + config.param + '=' + encodeURIComponent(keyword) + '&limit=' + config.limit + '&mode=' + config.mode;
						}
						that.delaySearch(url, that.$input);
					} else {
						that.toggleShow('hide');
					}
					that.$content.trigger('sizechange');
				});
				that.$content.on('click', '.remove_item', function() {
					var $item = $(this).parent();
					if (config.deleteFn && typeof config.deleteFn == 'function') {
						if (config.deleteFn($item.data('value')) == false) {
							return;
						}
					}
					that.$target.children().each(function() {
						if ($(this).val() == $item.data('value')) {
							$(this).remove();
						}
					});
					$item.remove();
					that.$content.trigger('sizechange');
					that.$target.trigger('change');
					that.$input.focus();
				});

			} else {
				that.$content.addClass('disabled');
			}
		},
		toggleShow : function(type) {
			if (type == "hide") {
				this.$wrapper.removeClass('on');
				if(this.$wrapper.offsetParent().hasClass('tsscroll-container')){
					this.$wrapper.offsetParent().height('');
				}				
			} else {
				this.$wrapper.addClass('on');
				this.$wrapper.siblings('.inputselect-container').removeClass('on');
				if(this.$wrapper.offsetParent().hasClass('tsscroll-container')){
					if(this.$dropdown.offset().top+this.$dropdown.height()+10 > this.$wrapper.offsetParent().offset().top+this.$wrapper.offsetParent().height()){
						this.$wrapper.offsetParent().height(this.$dropdown.offset().top+this.$dropdown.height()+10-this.$wrapper.offsetParent().offset().top);
					}else{
						this.$wrapper.offsetParent().height('');
					}						
				}				
			}
			
		},
		addUndefinedValue : function(val, text) {
			var that = this;
			val = $.trim(val);
			text = text || val;
			if (that.config.multiple) {
				var isExists = false;
				that.$target.find('option').each(function() {
					if ($(this).val().toLowerCase() == val.toLowerCase() && $(this).prop('selected')) {
						isExists = true;
					}
				});
				if (!isExists) {
					that.$target.append('<option selected value="' + val + '">' + text + '</option>');
					that.$input.before('<span class="jquery_inputselect_item" data-value="' + val + '"><i class="jquery_inputselect_text">' + text + '</i><i class="ts-remove remove_item"></i></span>');
				}
				that.toggleShow('hide');
				that.$target.trigger('change');
			} else {
				if (that.$target.val() != val) {
					that.$target.children().remove();
					that.$target.append('<option value="' + val + '">' + text + '</option>');
					that.$target.val(val);
					that.$target.trigger('change');
					that.$selectedspan.find('span').remove();
					that.$input.before('<span class="jquery_inputselect_item" data-value="' + val + '"><i class="jquery_inputselect_text">' + text + '</i><i class="ts-remove remove_item"></i></span>');
				}
				that.toggleShow('hide');
			}
			that.$input.val('').css('width', '24px');
		},
		delaySearch : function(url, input) {
			var that = this;
			if (that.searchtimer != null) {
				clearTimeout(that.searchtimer);
			}
			that.searchtimer = setTimeout(function() {
				var config = that.config;
				var keyword = $.trim(input.val());
				if (keyword.length >= config.minLength) {
					that.doSearch(url, input);
				}
			}, 250);
		},
		doSearch : function(url, input) {
			var that = this;
			var config = that.config;
			var keyword = $.trim(input.val());
			$.getJSON(url, function(data) {
				that.selectedIndex = -1;
				var resultData = data;
				// 兼容其他格式的数据
				if (config.root) {
					var rs = config.root.split('\\.');
					for (var ri = 0; ri < rs.length; ri++) {
						if (rs[ri]) {
							resultData = resultData[rs[ri]];
							if (!resultData) {
								break;
							}
						}
					}
				}
				// 数据格式如果是{data:[]}的就是有分组的，如果是[]则是不带分组的
				if (resultData.data) {
					resultData = resultData.data;
					config.hasClassify = true;
				} else {
					config.hasClassify = false;
					// 当不是分组同时，固定值存在时添加固定的下拉数据
					if (config.defaultvalue && config.defaultvalue.length > 0) {
						$.each(config.defaultvalue, function(i, d) {
							resultData.unshift(d);
						});
					}
				}
				if (resultData && resultData.length > 0) {
					that.$dropdown.empty();
					if (config.hasClassify) { // 有分类情况下，纯文字展示，不考虑图片等
						that.$dropdown.addClass('group');
						for (var i = 0; i < resultData.length; i++) {
							var nowList = resultData[i].data;
							var groupid = resultData[i].value;
							if (nowList.length > 0) {
								that.$dropdown.append('<div class="title">' + resultData[i].text + '</div>');
								/*
								 * if (config.defaultvalue &&
								 * config.defaultvalue.length > 0) {
								 * $.each(config.defaultvalue, function(i, d) {
								 * resultData.unshift(d); }); }
								 */
								that.$dropdown.append('<div class="list list-' + i + ' ' + (config.limitList ? 'tsscroll-container' : '') + '"></div>');
								var listouter = that.$dropdown.find('.list-' + i);
								$.each(nowList, function(i, d) {
									var icon_url = d.icon_url || '';
									var icon_class = d.icon_class || 'inputselect-icon-class-default';
									var $newopt = $('<div class="option" data-group="' + groupid + '"></div>');
									for ( var ct in d) {
										$newopt.data(ct, d[ct]);
									}
									var newtext = '';
									if (d[config.textKey]) {
										newtext = d[config.textKey].replace(new RegExp('(' + keyword + ')', 'ig'), '<b style="color:blue">$1</b>');
									}
									if (icon_url != '') {
										var $iconspn = $('<div class="d_f" style="width:25px;height:25px" ><img class="' + icon_class + '" src="' + icon_url + '"></div>');
										$newopt.append($iconspn);
										var $txtspn = $('<div class="option-txt" style="margin-left:30px;padding-top: 5px;" title="' + d[config.titleKey] + '">' + newtext + '</div>');
										$newopt.append($txtspn);
									} else {
										var $txtspn = $('<div class="option-txt" title="' + d[config.textKey] + '"></div>');
										$txtspn.append(newtext);
										$newopt.append($txtspn);
									}
									listouter.append($newopt);
									$newopt.on('click', function() {
										that.selectOption($(this));
									});
								});
							}
						}
					} else {

						$.each(resultData, function(i, d) {
							var icon_url = d.icon_url || '';
							var icon_class = d.icon_class || 'inputselect-icon-class-default';
							var $newopt = $('<div class="option"></div>');
							for ( var ct in d) {
								$newopt.data(ct, d[ct]);
							}
							var newtext = '';
							if (d[config.textKey]) {
								newtext = d[config.textKey].replace(new RegExp('(' + keyword + ')', 'ig'), '<b style="color:blue">$1</b>');
							}
							if (icon_url != '') {
								var $iconspn = $('<div class="d_f" style="width:25px;height:25px" ><img class="' + icon_class + '" src="' + icon_url + '"></div>');
								$newopt.append($iconspn);
								var $txtspn = $('<div class="option-txt" style="margin-left:30px;padding-top: 5px;" title="' + d[config.titleKey] + '">' + newtext + '</div>');
								$newopt.append($txtspn);
							} else {
								var $txtspn = $('<div class="option-txt" title="' + d[config.textKey] + '"></div>');
								$txtspn.append(newtext);
								$newopt.append($txtspn);
							}
							that.$dropdown.append($newopt);
							$newopt.on('click', function() {
								that.selectOption($(this));
							});
						});
					}
					if (that.$dropdown.find('.option').length > 0) {
						that.$wrapper.addClass('on');
						that.toggleShow('show');
					} else {
						that.$wrapper.removeClass('on');
						that.toggleShow('hide');
					}
					that.$content.trigger('sizechange');

				} else {
					that.$dropdown.empty();
					that.toggleShow('hide');
				}
			});
		},
		selectOption : function(obj) {
			var that = this, config = that.config, current_opt = obj, current_val = '';
			that.$input.val('').css('width', '24px').attr('placeholder', '');
			if (current_opt.data('group')) {
				current_val = current_opt.data('group') + "." + current_opt.data(config.valueKey);
			} else {
				current_val = current_opt.data(config.valueKey);
			}
			current_val = current_val.toString();
			if (config.multiple) {
				var isExists = false;
				that.$target.find('option').each(function() {
					if ($(this).val() && $(this).val().toLowerCase() == current_val.toLowerCase() && $(this).attr('selected')) {
						isExists = true;
					}
				});
				if (!isExists) {
					var $opt = $('<option selected></option>');
					for ( var d in current_opt.data()) {
						if (d == config.valueKey) {
							$opt.val(current_val);
						} else if (d == config.textKey) {
							$opt.text(current_opt.data(d));
						} else {
							$opt.data(d, current_opt.data(d));
						}
					}
					that.$target.append($opt);
					that.$input.before('<span class="jquery_inputselect_item" data-value="' + current_val + '"><i class="jquery_inputselect_text ' + (current_opt.data('icon') ? current_opt.data('icon') : '') + '">' + current_opt.data(config.textKey)
							+ '</i><i class="ts-remove remove_item"></i></span>');
				}

			} else {
				if (!that.$target.val() || that.$target.val().toLowerCase() != current_val.toLowerCase()) {
					that.$target.children().remove();
					var $opt = $('<option></option>');
					for ( var d in current_opt.data()) {
						if (d == config.valueKey) {
							$opt.val(current_val);
						} else if (d == config.textKey) {
							$opt.text(current_opt.data(d));
						} else {
							$opt.data(d, current_opt.data(d));
						}
					}
					that.$target.val(current_opt.data(config.valueKey));
					that.$selectedspan.find('span').remove();
					that.$target.append($opt);
					that.$input.before('<span class="jquery_inputselect_item" data-value="' + current_val + '"><i class="jquery_inputselect_text ' + (current_opt.data('icon') ? current_opt.data('icon') : '') + '">' + current_opt.data(config.textKey)
							+ '</i><i class="ts-remove remove_item"></i></span>');
				}
			}
			that.toggleShow('hide');
			that.$content.trigger('sizechange');
			that.$target.trigger('change');
			that.$input.focus();
			that.$dropdown.empty();
			that.selectedIndex = -1;
		}

	};
	$.fn.inputselect = function(config) {
		var $target = $(this);
		if (!$target.attr('bind-inputselect')) {
			var c = new InputSelect($target, config);
			$target.attr('bind-inputselect', true);
		}
		return this;
	};
	// 初始化插件
	function initInputSelect(item) {
		// 默认值处理
		var defaultVal = [];
		if (item.data('defaultvalue')) {
			if (typeof item.data('defaultvalue') == 'string') {
				var ddlist = item.data('defaultvalue').split(',');
				ddlist.forEach(function(dd, index) {
					var dds = dd.split('|');
					if (dds.length == 2) {
						defaultVal.push({
							value : dds[0],
							text : dds[1]
						});
					}
				});
			} else if (typeof item.data('defaultvalue') == 'object') {
				defaultVal = item.data('defaultvalue');
			}
		}

		item.inputselect({
			url : item.data('url'),
			width : item.data('width'),
			height : item.data('height') || 32,
			root : item.data('root') || '',
			textKey : item.data('textkey') || 'text',
			valueKey : item.data('valuekey') || 'value',
			placeholder : item.attr('placeholder') || '请输入关键字',
			enable : item.attr("disabled") ? false : true,
			defaultvalue : defaultVal,
			multiple : item.attr("multiple") ? true : false,
			theme : item.data('theme') || '',
			param : item.data('param') || 'k'
		});
	}
	;
	$(function() {
		$('select[plugin-inputselect]').each(function() {
			var item = $(this);
			// 这里不能判断bind，需要在页面重新加载后重新初始化插件
			if (item.data('url')) {
				initInputSelect(item);
			}
		});
		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('select[plugin-inputselect]').each(function() {
				var item = $(this);
				if (!item.attr('bind-inputselect') && item.data('url')) {
					initInputSelect(item);
				}
			});
		});
		$(document).bind('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('select[plugin-inputselect]').each(function() {
				if (!$(this).attr('bind-inputselect') && $(this).data('url')) {
					initInputSelect($(this));
				}
			});
		});
	});
})(jQuery);
