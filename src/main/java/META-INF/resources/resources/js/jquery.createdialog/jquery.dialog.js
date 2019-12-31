var createDialog = function(options) {
	var dialog = new tsDialog(options);
	dialog.show();
	return dialog;
}

var tsDialog = function(options) {
	this.$container = null;
	this.$warp = null;
	this.width = null;
	this.height = null ;
	this.blurclose = true;
	this.buttonList = new Array();
	$.extend(this, options);
}

tsDialog.prototype.show = function() {
	var that = this;
	var DIALOG_LIST = that.getModalList();
	var zindex = getMaxZ();
	zindex = zindex < 1040 ? 1040 : zindex;
	this.$container = $('<div class="tsDialog fade in popover" style="border:0px;"></div>');
	if (this.id || this.Id) {
		this.$container.attr('id', this.id ? this.id : this.Id);
	}
	zindex += 1;
	this.$container.css('z-index', zindex);
	this.$warp = $('<div class="modal-content"></div>');

	// 兼容以前的变量
	var width = this.width ? this.width : (this.msgwidth ? this.msgwidth : '380');
	var height = this.height ? this.height : (this.msgheight ? this.msgheight : '300');
	var title = this.title ? this.title :( this.msgtitle ? this.msgtitle : '${tk:lang("消息提示")}' ) ; 
	var content = this.content ? this.content :( this.msgcontent ? this.msgcontent : '${tk:lang("确认继续当前操作")}？' ) ;
	var type = this.type ? this.type : (this.msgtype ? this.msgtype : 'info');
	var appendtoform =this.appendtoform ? this.appendtoform : (this.appendToForm ? this.appendToForm : false);
	var top  = this.top ? this.top : 100 ; 
	
	var checkbtn = this.checkbtn ;
	if(!checkbtn){
		checkbtn =  this.checkBtn;
	}
	checkbtn = checkbtn == undefined || checkbtn == null ? true : checkbtn;
	
	var allowmore = this.allowmore ;
	if(!allowmore){
		allowmore =  this.allowMore;
	}
	allowmore = allowmore == undefined || allowmore == null ?  true : allowmore;
	
	var allowscroll = this.allowscroll ;
	if(!allowscroll){
		allowscroll =  this.allowScroll;
	}
	allowscroll = allowscroll == undefined || allowscroll == null ?  false : allowscroll;
	
	if (typeof width == 'string' && width.indexOf('%') > -1) {
		var bodywidth = $(window).width();
		var winwidth = bodywidth * parseFloat(width.replace('%', '')) / 100;
		width = winwidth;
	} else if(typeof width == 'string' && width.indexOf('px') > -1){
		width = width.replace('px' , '');
	}else if (parseFloat(width) != width) {
		width = width;
	}
	this.$container.css('width', width + 'px');

	if (typeof height == 'string' && height.indexOf('%') > -1) {
		var bodyheight = $(window).height();
		var winheight = bodyheight * parseFloat(height.replace('%', '')) / 100;
		height = winheight;
	}else if(typeof height == 'string' && height.indexOf('px') > -1){
		height = height.replace('px' , '');
	} else if (parseFloat(height) != height) {
		height = height;
	}
	
	if(top){
		var st = $(window).scrollTop();
		this.$container.css('top', (top + st) + 'px');
	}
	if(this.url){
		$.ajax({
			type : 'GET',
			url : this.url,
			async : false,
			success : function(data) {
				content = ($.trim(data) == "" ? content : data);
			}
		});
	}
	
	if (!allowmore) {
		for (var i = 0; i < DIALOG_LIST.length; i++) {
			if (DIALOG_LIST[i]) {
				DIALOG_LIST[i].hide();		
			}
		}
	}

	var $header = $('<div class="modal-header"></div>');
	var $header_title = $('<h5 class="modal-title"></h5>');
	$header_title.append(title);
	var $headerclose = $('<button type="button" class="close" ata-dismiss="modal" aria-hidden="true">&times;</button>');
	$headerclose.on('click', function() {
		that.hide();
	});
	$header.append($headerclose);
	$header.append($header_title);
	this.$warp.append($header);
	
	var winWidth = null;
	if($(window.parent.document).find('#rightContent').length > 0 ){
		winWidth =  $(window.parent.document).find('#rightContent').width();
	}else{
		winWidth = $(window).width();
	}
	
	this.$container.css({
		'position' : 'absolute',
		'height' : height + 'px',
		'margin-left' : '0px',
		'left' : ((winWidth - width) / 2) + 'px'
	});
	
	var $bodydiv = $('<div class="modal-body"></div>');
	if (allowscroll) {
		$bodydiv.css({
			'word-break' : 'break-all',
			'overflow-y' : 'auto',
			'overflow-x' : 'hidden',
			'height' :  (height -120) + 'px'
		});
	}else{
		$bodydiv.css({
			'word-break' : 'break-all',
			'min-height' : (height -120) + 'px'
		});
	}
	$bodydiv.append(content);
	this.$warp.append($bodydiv);
	var $footer = $('<div class="modal-footer"></div>');
	var $footerdiv = $('<div class="btn-group"></div>');
	var $okbtn = $('<a class="btn btn-sm ' + (type == 'del' ? "btn-danger" : "btn-primary") + '">${tk:lang("确定")}</a>');
	var $cancelbtn = $('<a class="btn btn-default btn-sm">${tk:lang("取消")}</a>');
	
	$okbtn.on('click', function() {
		if (that.successFuc && typeof that.successFuc == 'function') {
			var returnVal = false;
			that.disable();
			if ((that.checkreturn && that.checkreturn == true) ||( that.checkReturn &&  that.checkReturn == true)) {// 关闭异步
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
				that.enable();
			} catch (e) {
				console.error(e);
				that.enable();
			}
			if ((that.checkreturn && that.checkreturn == true) ||( that.checkReturn &&  that.checkReturn == true)) {// 重新打开异步
				$.ajaxSetup({
					async : true
				});
			}
		}
		
		if ((that.checkreturn && that.checkreturn == true) ||( that.checkReturn &&  that.checkReturn == true)) {
			if (returnVal) {
				that.hide();
			}
		} else {
			that.hide();
		}
		that.enable();
	});
	this.buttonList.push($okbtn);
	$footerdiv.append($okbtn);
	
	$cancelbtn.click(function() {
		that.disable();
		if (that.cancelFuc && typeof that.cancelFuc == 'function') {
			try {
				that.cancelFuc();
				that.enable();
			} catch (e) {
				showPopMsg.error(e.description);
				that.enable();
			}
		}
		that.hide();
		that.enable();
	});
	this.buttonList.push($cancelbtn);
	$footerdiv.append($cancelbtn);
	
	if (this.customButtons) {
		for (var i = 0; i < this.customButtons.length; i++) {
			var btn = this.customButtons[i];
			var $btn = $('<button type="button" class="btn btn-sm"></button>');
			if(btn.icon) {
				var $icon = $('<i></i>').addClass(btn.icon);
				$btn.append($icon);
			}
			if (btn.class) {
				$btn.addClass(btn.class);
			}
			if (btn.ishide) {
				$btn.css('display', 'none');
			}
			this.buttonList.push($btn);
			$btn.append(btn.text);
			$btn.data('click', btn.click);
			$btn.on('click', function() {
				that.disable();
				if ((that.checkreturn && that.checkreturn == true) ||( that.checkReturn &&  that.checkReturn == true)) {// 关闭异步
					$.ajaxSetup({
						async : false
					});
				}
				var returnVal = $(this).data('click')();
				if ((that.checkreturn && that.checkreturn == true) ||( that.checkReturn &&  that.checkReturn == true)) {// 重新打开异步
					$.ajaxSetup({
						async : true
					});
				}
				if ((that.checkreturn && that.checkreturn == true) ||( that.checkReturn &&  that.checkReturn == true)) {
					if (returnVal == true) {
						that.hide();
					}
				} else {
					that.hide();
				}
				that.enable();
			});
			$footerdiv.prepend($btn);
		}
	}
	
	this.$container.on('show.bs.popover', function() {
		if (this.showFuc && typeof this.showFuc == 'function') {
			try {
				this.showFuc();
			} catch (er) {
				showPopMsg.error(er.description);
			}
		}
	});

	this.$container.on('hide.bs.popover', function() {
		if (this.closeFuc && typeof this.closeFuc == 'function') {
			try {
				this.closeFuc();
			} catch (er) {
				showPopMsg.error(er.description);
			}
		}
		that.hide();
		$(this).remove();
	});

	if (checkbtn) {
		$footer.append($footerdiv);
	}
	this.$warp.append($footer);
	this.$container.append(this.$warp);
	
	if(appendtoform){
		$('body').find('form').append(this.$container);
	}else{
		$('body').append(this.$container);
	}
	this.$container.show()
	var DIALOG_LIST = that.getModalList();
	DIALOG_LIST.push(that);
	that.setModalList(DIALOG_LIST);
};

tsDialog.prototype.enable = function() {
	var that = this;
	if (that.buttonList && that.buttonList.length > 0) {
		for (var i = 0; i < that.buttonList.length; i++) {
			var btn = that.buttonList[i];
			btn.removeAttr('disabled');
		}
	}
};

tsDialog.prototype.disable = function() {
	var that = this;
	if (that.buttonList && that.buttonList.length > 0) {
		for (var i = 0; i < that.buttonList.length; i++) {
			var btn = that.buttonList[i];
			btn.attr('disabled', 'disabled');
		}
	}
};

tsDialog.prototype.getModalList = function() {
	var $body = $('body');
	var dialog_list = $body.data('dialog_list');
	if(!dialog_list){
		dialog_list = new Array();
	}
	return dialog_list;
};

tsDialog.prototype.setModalList = function(_newList) {
	$('body').data('dialog_list' , _newList);
};


tsDialog.prototype.hide = function() {
	var that = this;
	var DIALOG_LIST = that.getModalList();
	this.$container.animate({}, 'fast', function() {
		if (that.closeFuc && typeof that.closeFuc == 'function') {
			that.closeFuc();
		}
		var tmpArray =  new Array();
		for (var i = 0; i < DIALOG_LIST.length; i++) {
			if (DIALOG_LIST[i] && DIALOG_LIST[i] == that) {
				delete DIALOG_LIST[i];
			}else{
				tmpArray.push(DIALOG_LIST[i]);
			}
		}
		that.setModalList(tmpArray);
		that.$container.hide();
		that.$container.remove();
		that = null;
	});
}
