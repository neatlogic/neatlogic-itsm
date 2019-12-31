/*!
 * cxColor 1.2
 * http://code.ciaoca.com/
 * https://github.com/ciaoca/cxColor
 * E-mail: ciaoca@gmail.com
 * Released under the MIT license
 * Date: 2014-12-10
 */
(function(factory){
    if (typeof define === 'function' && define.amd) {
        define(['jquery'], factory);
    } else {
        factory(jQuery);
    };
    
}(function($){
	$.cxColor = function(){
		var obj;
		var settings;
		var callback;
		var colorPanel = {
			dom: {},
			api: {}
		};

		// 检测是否为 DOM 元素
		var isElement = function(o){
			if(o && (typeof HTMLElement === 'function' || typeof HTMLElement === 'object') && o instanceof HTMLElement) {
				return true;
			} else {
				return (o && o.nodeType && o.nodeType === 1) ? true : false;
			};
		};

		// 检测是否为 jQuery 对象
		var isJquery = function(o){
			return (o && o.length && (typeof jQuery === 'function' || typeof jQuery === 'object') && o instanceof jQuery) ? true : false;
		};

		// 分配参数
		for (var i = 0, l = arguments.length; i < l; i++) {
			if (isJquery(arguments[i])) {
				obj = arguments[i];
			} else if (isElement(arguments[i])) {
				obj = $(arguments[i]);
			} else if (typeof arguments[i] === 'function') {
				callback = arguments[i];
			} else if (typeof arguments[i] === 'object') {
				settings = arguments[i];
			};
		};

		if (obj.length < 1) {return};

		colorPanel.init = function(){
			var _this = this;

			_this.dom.el = obj;
			_this.settings = $.extend({}, $.cxColor.defaults, settings);

			if (_this.dom.el.val().length > 0) {
				_this.settings.color = _this.dom.el.val();
			};

			_this.build();

			_this.api = {
				show: function(){
					_this.show();
				},
				hide: function(){
					_this.hide();
				},
				color: function(){
					return _this.setColor.apply(_this, arguments);
				},
				reset: function(){
					_this.reset();
				},
				clear: function(){
					_this.clear();
				}
			};

			if (typeof callback === 'function') {
				callback(_this.api);
			};
		};

		// 创建面板
		colorPanel.build = function(){
			var _this = this;
	
			// 显示面板事件
			_this.dom.el.on('click', function(){
				_this.show();
			});

	
			// 第一次初始化
			//_this.change(_this.settings.color);
		};

		colorPanel.show = function(){
			var _this = this;
			var _colorHex = ['00','33','66','99','cc','ff'];
			var _spcolorHex = ['ff0000','00ff00','0000ff','ffff00','00ffff','ff00ff'];
			var _html = '';

			//_html = '<div class="panel_hd"><a class="reset" href="javascript:void(0)" rel="reset">默认颜色</a><a class="clear" href="javascript:void(0)" rel="clear">清除</a></div>';
			_this.dom.colorPane = $('<div></div>', {'class':'cxcolor'}).appendTo('body');//.html(_html);

			_html = '';
			for (var i = 0; i < 2; i++) {
				for(var j = 0; j < 6; j++) {
					_html += '<tr>';
					_html += '<td title="#000000" style="background-color:#000000">';

					if (i == 0) {
						_html += '<td title="#' + _colorHex[j] + _colorHex[j] + _colorHex[j] + '" style="background-color:#' + _colorHex[j] + _colorHex[j] + _colorHex[j] + '">';
					} else {
						_html += '<td title="#' + _spcolorHex[j] + '" style="background-color:#' + _spcolorHex[j]+'">';
					};

					_html += '<td title="#000000" style="background-color:#000000">';

					for (var k = 0; k < 3; k++){
						for(var l = 0; l < 6; l++){
							_html += '<td title="#' + _colorHex[k + i * 3] + _colorHex[l] + _colorHex[j] + '" style="background-color:#' + _colorHex[k + i * 3] + _colorHex[l] + _colorHex[j] + '">';
						};
					};
				};
			};

			_this.dom.colorTable = $('<table></table>').html(_html).appendTo(_this.dom.colorPane);
			_this.dom.lockBackground = $('<div></div>', {'class':'cxcolor_lock'}).appendTo('body');
			
			// 监听事件
			_this.dom.colorPane.delegate('a', 'click', function(){
				if (!this.rel) {return};
	
				switch (this.rel) {
					case 'reset':
						_this.reset();
						return false;
						break
					case 'clear':
						_this.clear();
						return false;
						break
				};
			});
	
			// 选择颜色事件
			_this.dom.colorTable.on('click', 'td', function(){
				_this.change(this.title);
			});
			
			// 关闭面板事件
			_this.dom.lockBackground.on('click', function(){
				_this.hide();
			});
			
			var _docWidth = document.body.clientWidth;
			var _docHeight = document.body.clientHeight;
			var _paneWidth = _this.dom.colorPane.outerWidth();
			var _paneHeight = _this.dom.colorPane.outerHeight();
			var _paneTop = _this.dom.el.offset().top;
			var _paneLeft = _this.dom.el.offset().left;
			var _elWidth = _this.dom.el.outerWidth();
			var _elHeight = _this.dom.el.outerHeight();
			
			_paneTop = ((_paneTop + _paneHeight + _elHeight) > _docHeight) ? _paneTop - _paneHeight : _paneTop + _elHeight;
			_paneLeft = ((_paneLeft + _paneWidth) > _docWidth) ? _paneLeft - (_paneWidth - _elWidth) : _paneLeft;

			_this.dom.colorPane.css({'top': _paneTop, 'left': _paneLeft}).show();
			_this.dom.lockBackground.css({width: _docWidth, height: _docHeight}).show();
		};

		// 关闭日期函数
		colorPanel.hide = function(){
			this.dom.colorPane.remove();
			this.dom.lockBackground.remove();
		};

		// 更改颜色函数
		colorPanel.change = function(c){
			this.colorNow = c;
			this.dom.el.val(c).css('backgroundColor', c);
			this.dom.el.trigger('change');
			this.hide();
		};

		// 设置或获取颜色
		colorPanel.setColor = function(c){
			if (!c) {
				return this.colorNow;
			} else {
				this.change(c);
			};
		};

		// 还原默认颜色
		colorPanel.reset = function(){
			this.change(this.settings.color);
		};

		// 清除颜色
		colorPanel.clear = function(){
			this.change('');
		};

		colorPanel.init();
		
		return this;
	};

	// 默认值
	$.cxColor.defaults = {
		color: '#000000'	// 默认颜色
	};

	$.fn.cxColor = function(settings, callback){
		this.each(function(i){
			$.cxColor(this, settings, callback);
		});
		return this;
	};
}));