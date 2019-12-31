var SLIDEDIALOG_LIST = new Array();

var createSlideDialog = function(options) {
	var slideDialog = new SlideDialog(options);
	slideDialog.show();
	return slideDialog;
}

var SlideDialog = function(options) {
	this.$backdrop = null;
	this.$container = null;
	this.width = 500;
	this.showclose = true;
	this.allowmore = true;
	this.allowscroll = true;
	this.blurclose = false;
	this.position = 'right';
	this.buttonList = new Array();
	this.widthList ={	//新增小中大三个通用尺寸
		'small':600,
		'medium':800,
		'large':1000
	};
	$.extend(this, options);
}

SlideDialog.prototype.show = function() {
	var that = this;
	if (!that.allowmore) {
		for (var i = 0; i < SLIDEDIALOG_LIST.length; i++) {
			if (SLIDEDIALOG_LIST[i]) {
				SLIDEDIALOG_LIST[i].hide();
			}
		}
	}
	var zindex = getMaxZ()>10?getMaxZ():10;
	this.$backdrop = $('<div class="slidedialog_backdrop"></div>');
	if (this.blurclose) {
		this.$backdrop.on('click', function(e) {
			e.stopPropagation();
			that.hide();
		});
	}
	zindex += 1;
	this.$backdrop.css('z-index', zindex);
	$('body').append(this.$backdrop);

	this.$container = $('<div  tabindex="-1" style="opacity:0" class="slidedialog_container"></div>');
	if (this.id) {
		this.$container.attr('id', this.id);
	}
	zindex += 1;
	this.$container.css('z-index', zindex);
	if(!/[0-9]/.test(this.width)){	//参数不带数字的即为标准宽度
		this.$container.css('width', this.widthList[this.width]);
	}else if (parseFloat(this.width) != this.width) {
		this.$container.css('width', this.width);
	} else {
		this.$container.css('width', this.width + 'px');
	}
	var w = this.$container.width();
	var css = {};
	css[that.position] = (-w) + 'px';
	css['border-radius'] = that.position == 'left' ? '0 5px 5px 0' : '5px 0 0 5px';
	this.$container.css(css);
	var $wraper = $('<div class="slidedialog_main" ></div>');
	if (this.title) {
		var $header = $('<div class="slidedialog_header"></div>');
		$header.append(this.title);
		var $headerclose = $('<button type="button" class="close">&times;</button>');
		$headerclose.on('click', function() {
			that.hide();
		});
		$header.append($headerclose);
		$wraper.append($header);
	}else{
		$wraper.css('padding-top', '10px');
	}
	var $bodydiv = $('<div class="slidedialog_body body"></div>');
	if (this.allowscroll) {
		$bodydiv.css({
			'overflow-y' : 'auto',
			'overflow-x' : 'hidden'
		});
	}
	$bodydiv.append(this.content);
	$wraper.append($bodydiv);

	var $footer = $('<div class="slidedialog_footer"></div>');
	// var $footerdiv = $('<div class="btn-group2"></div>');
	var needfooter = false;

	if (this.customButtons) {
		for (var i = 0; i < this.customButtons.length; i++) {
			var btnopt = this.customButtons[i];
			var $btn = $('<button class="btn btn-sm ' + btnopt.classname + '" type="button"></button>&nbsp;');
			if (!!btnopt.id) {
				$btn.attr("id", btnopt.id);
			}
			this.buttonList.push($btn);
			if (btnopt.ishide) {
				$btn.css('display', 'none');
			}
			if (btnopt.icon) {
				$btn.append('<i class="' + btnopt.icon + '"></i>');
			}
			$btn.append(btnopt.text);
			$btn.data('click', btnopt.click);
			$btn.on('click', function() {
				var returnVal = false;
				that.disable();
				if (that.checkreturn) {// 关闭异步
					$.ajaxSetup({
						async : false
					});
				}
				var returnVal = $(this).data('click')(that);
				if (that.checkreturn) {// 重新打开异步
					$.ajaxSetup({
						async : true
					});
				}
				if (that.checkreturn) {
					if (returnVal == true) {
						that.hide();
					}
				} else {
					if (typeof returnVal == 'undefined' || returnVal == true) {
						that.hide();
					}
				}
				that.enable();
			});
			$footer.append($btn);
		}
		needfooter = true;
	}

	if (this.successFuc && typeof this.successFuc == 'function') {
		this.$okbtn = $('<button  class="btn btn-sm btn-primary" type="button">确认</button>');
		this.buttonList.push(this.$okbtn);
		this.$okbtn.on('click', function() {
			var returnVal = false;
			that.disable();
			if (that.checkreturn) {// 关闭异步
				$.ajaxSetup({
					async : false
				});
			}
			try {
				var r = true;
				if (that.valid && typeof that.valid == 'function') {
					r = false;
					r = that.valid();
				}
				if (r) {
					returnVal = that.successFuc();
				}
			} catch (e) {
				console.error(e);
				that.enable();
			}
			if (that.checkreturn) {// 重新打开异步
				$.ajaxSetup({
					async : true
				});
			}

			if (that.checkreturn) {
				if (returnVal) {
					that.hide();
				} else {
					that.enable();
				}
			} else {
				if (typeof returnVal == 'undefined' || returnVal == true) {
					that.hide();
				}
			}
			that.enable();
		});
		$footer.append(this.$okbtn);
		needfooter = true;
	}

	if (this.showclose) {
		this.$closebtn = $('<button class="btn btn-sm btn-cancel" type="button">取消</button>');
		this.buttonList.push(this.$closebtn);
		this.$closebtn.on('click', function() {
			that.hide();
		});
		$footer.append(this.$closebtn);
		needfooter = true;
	}
	if (needfooter) {
		// $footer.append($footerdiv);
		$wraper.append($footer);
		$wraper.css('padding-bottom', '50px');
	} else {
		$wraper.css('padding-bottom', '0px');
	}
	this.$container.append($wraper);
	$('body').append(this.$container);
	var style = {};
	style[that.position] = 0;
	style['opacity'] = 1;
	this.$container.animate(style, 'fast', function() {
		if (that.shownFuc && typeof that.shownFuc == 'function') {
			that.shownFuc();
		}
	});
	$('body').addClass('modal-open');
	SLIDEDIALOG_LIST.push(that);
};

SlideDialog.prototype.enable = function() {
	var that = this;
	if (that.buttonList && that.buttonList.length > 0) {
		for (var i = 0; i < that.buttonList.length; i++) {
			var btn = that.buttonList[i];
			btn.removeAttr('disabled');
		}
	}
};

SlideDialog.prototype.disable = function() {
	var that = this;
	if (that.buttonList && that.buttonList.length > 0) {
		for (var i = 0; i < that.buttonList.length; i++) {
			var btn = that.buttonList[i];
			btn.attr('disabled', 'disabled');
		}
	}
};

SlideDialog.prototype.hide = function() {
	var that = this;
	var w = this.$container.width();
	var style = {};
	style[that.position] = (-w) + 'px';
	style['opacity'] = 0;
	this.$container.animate(style, 'fast', function() {
		that.$container.remove();
		that.$backdrop.remove();
		if (that.closeFuc && typeof that.closeFuc == 'function') {
			that.closeFuc();
		}
		that = null;
		if ($('.slidedialog_container ,.modal-dialog').length <= 1) {
			$('body').removeClass('modal-open');
		}
		for (var i = 0; i < SLIDEDIALOG_LIST.length; i++) {
			if (SLIDEDIALOG_LIST[i] && SLIDEDIALOG_LIST[i] == that) {
				delete SLIDEDIALOG_LIST[i];
			}
		}
	});
}
