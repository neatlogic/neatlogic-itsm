;
(function($) {
	var defaultoptions = {
		width : 210,
		height : 32,
		url : null,
		valid : false, // 此开关表示，数据必须是下拉选择的
		multiple : false, // 是否多选
		valuekey : 'value',
		textkey : 'text',
		zindex : 999,
		keyName : 'keyword',
		param : null
	};

	var inputorselect = function(target, options) {
		var that = this;
		this.$config = $.extend(true, {}, defaultoptions, options);
		this.$target = $(target);
		this.selectMod = false;
		if (this.$config.param) {
			var url = that.$config.url;
			for ( var i in this.$config.param) {
				if (url.indexOf('?') > -1) {
					url = url + '&' + i + '=' + encodeURIComponent(this.$config.param[i]);
				} else {
					url = url + '?' + i + '=' + encodeURIComponent(this.$config.param[i]);
				}
			}
			this.url = url;
			this.$config.url = url;
		} else {
			this.url = this.$config.url;
		}

		that.$container = $('<div class="jquery-inputorselect"></div>');
		that.$containerBody = $('<div></div>');

		// that.$target.data('container', that.$container);

		that.$container.css({
			'z-index' : that.$config.zindex,
			'top' : (that.$target.position().top + that.$target.outerHeight(true)> 0 ?that.$target.outerHeight(true) : this.$config.height - 1),
			'left' : that.$target.position().left,
			'width' : that.$target.outerWidth(true) > 0 ?that.$target.outerWidth(true) : this.$config.width,
			'max-height' : '300px',
			'overflow' : 'auto',
		});

		that.$target.after(that.$container);

		that.$target.on('click', function() {
			var it = that.$target.val();
			that.url = that.$config.url;
			if (it) {
				if (that.$target.multiple) {
					ext = it.substr(it.length - 1, it.length);
					if (ext != ',') {
						it = it + ',';
					}
				} else {
					if (that.url.indexOf('?') > -1) {
						that.url = that.url + '&' + that.$config.keyName + '=' + encodeURIComponent(it);
					} else {
						that.url = that.url + '?' + that.$config.keyName + '=' + encodeURIComponent(it);
					}
				}
				that.$target.val(it).focus();
			}
			if (!that.$container.is(':visible')) {
				that.initValue();
				that.$container.slideDown();
			}
		});

		that.$target.on('keyup', function(e) {
			var it = that.$target.val();
			if (e.keyCode == 8 || e.keyCode == 46) {// 删除键或回退键
				that.$target.data('target', it);
				that.$target.val(it);
				if (that.$container.is(':visible')) {
					that.$container.slideUp(function() {
						that.$containerBody.empty();
					});
				}
				e.stopPropagation();
			} else {
				var param = '';
				if (it) {
					param = it.substr(it.lastIndexOf(',') + 1, it.length);
				}
				// if (param.length >= that.$config.conditionLen) {
				that.$containerBody.empty();
				that.url = that.$config.url;
				if (that.$config.url.indexOf('?') > -1) {
					that.url = that.url + '&' + that.$config.keyName + '=' + encodeURIComponent(param);
				} else {
					that.url = that.url + '?' + that.$config.keyName + '=' + encodeURIComponent(param);
				}
				that.initValue();
				if (!that.$container.is(':visible')) {
					that.$container.slideDown();
				}
				// }
			}
		});

		that.$target.on('blur', function() {
			var it = that.$target.val();
			if (it) {
				ext = it.substr(it.length - 1, it.length);
				it = it.substr(0, it.length - 1);
				if (ext == ',') {
					that.$target.val(it);
				} else {// 有输入值
					if (!that.$config.valid) {
						that.setInputValue();
					}
				}
			}
			if (that.$container.is(':visible')) {
				that.$container.slideUp(function() {
					that.$containerBody.empty();
				});
			}
		});
	}

	this.inputorselect = inputorselect;

	inputorselect.prototype = {
		initValue : function() {
			var that = this;
			var config = this.$config;
			var len = that.$container.find('.jquery-inputorselect-nav-li').length;
			if (len > 0) {
				that.$containerBody.empty();
			}
			$.getJSON(that.url, function(data) {
				if (data && data.length > 0) {
					var $main = $('<div class="jquery-inputorselect-content"></div>')
					var $ul = $('<ul class="jquery-inputorselect-nav-list"></ul>')
					for ( var i in data) {
						var value = '', text = '';
						for ( var j in data[i]) {
							if (config.textkey.toLowerCase() == j.toLowerCase()) {
								text = data[i][j];
							} else if (config.valuekey.toLowerCase() == j.toLowerCase()) {
								value = data[i][j];
							}
						}
						var $li = $('<li class="jquery-inputorselect-nav-li" it_' + config.textkey + '="' + text + '" it_' + config.valuekey + '="' + value + '"><a>' + text + '</a></li>')
						$ul.append($li);
					}
					$main.append($ul);
					that.$containerBody.append($main);
					that.$container.append(that.$containerBody);

					that.$container.find('.jquery-inputorselect-nav-li').click(function() {
						var text = $(this).attr('it_' + config.textkey);
						var value = $.trim($(this).attr('it_' + config.valuekey));
						that.setSelectValue(value, text);
						that.$container.slideUp(function() {
							that.$containerBody.empty();
						});
						that.selectMod = true;
					});
				}
			});
		},
		setSelectValue : function(value, text) {
			var that = this;
			var config = this.$config;
			if (config.multiple) {
				if (!that.$target.data('target')) {
					that.$target.data('target', text);
					that.$target.val(text);
				} else {
					var _text = that.$target.data('target') + ',' + $.trim(text);
					var textArray = that.$target.data('target').split(',');
					var _notExist = true;
					for ( var i in textArray) {
						if (text == textArray[i]) {
							_notExist = false;
						}
					}
					if (_notExist) {
						that.$target.data('target', _text);
						that.$target.val(_text);
					} else {
						that.$target.val(that.$target.data('target'));
					}
				}
			} else {
				that.$target.val(text);
			}
		},
		setInputValue : function() {
			var that = this;
			var config = this.$config;
			var it = that.$target.val();
			var newText = $.trim(it.substr(it.lastIndexOf(',') + 1, it.length));
			if (newText) {
				if (config.multiple) {
					if (!that.$target.data('target')) {
						that.$target.data('target', newText);
						that.$target.val(newText);
					} else {
						var _text = that.$target.data('target') + ',' + newText;
						var textArray = that.$target.data('target').split(',');
						var _notExist = true;
						for ( var i in textArray) {
							if (newText == textArray[i]) {
								_notExist = false;
							}
						}
						if (_notExist) {
							that.$target.data('target', _text);
							that.$target.val(_text);
						} else {
						}
					}
				} else {
					that.$target.val(newText);
				}
			}
		}
	};

	$.fn.inputorselect = function(options) {
		var $target = $(this);
		if (!$target.attr('bind')) {
			var c = new inputorselect($target, options);
			$target.attr('bind', true);
		}
		return this;
	};

	$(function() {
		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('input[plugin-inputorselect]').each(function() {
				var $target = $(this);
				if (!$target.attr('bind') && $target.data('url')) {
					var options = {};
					options.url = $target.data('url');
					$target.inputorselect(options);
					$target.attr('bind', true);
				}
			});
		});

		$(document).on('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('input[plugin-inputorselect]').each(function() {
				var $target = $(this);
				if (!$target.attr('bind') && $target.data('url')) {
					var options = {};
					options.url = $target.data('url');
					$target.inputorselect(options);
					$target.attr('bind', true);
				}
			});
		});

		$('input[plugin-inputorselect]').each(function() {
			var $target = $(this);
			if (!$target.attr('bind') && $target.data('url')) {
				var options = {};
				options.url = $target.data('url');
				$target.inputorselect(options);
				$target.attr('bind', true);
			}
		});
	});

})(jQuery);