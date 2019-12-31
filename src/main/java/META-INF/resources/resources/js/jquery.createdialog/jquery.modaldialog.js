var createModalDialog = function(options) {
	var modalDialog = new ModalDialog(options);
	modalDialog.show();
	return modalDialog;
}

var ModalDialog = function(options) {
	this.$container = null;
	this.$dialog = null;
	this.$warp = null;
	this.width = null;
	this.top = null;	//不带单位，不支持百分比
	this.height = null ;
	this.blurclose = null;
	this.buttonList = new Array();
	this.hideClose = false;	//隐藏标题的关闭按钮，老属性
	this.showclose = true; //隐藏关闭按钮，与slidedialog插件的showclose保持一致兼容
	this.widthList ={	//新增小中大三个通用尺寸
		'small':300,
		'medium':500,
		'large':800
	};	
	$.extend(this, options);
}

ModalDialog.prototype.show = function() {
	var that = this;
	var MODALDIALOG_LIST = that.getModalList();
	var zindex = getMaxZ();
	zindex = zindex < 1040 ? 1040 : zindex;
	this.$container = $('<div class="tsModalDialog modal"  tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true"></div>');
	if (this.id || this.Id) {
		this.$container.attr('id', this.id ? this.id : this.Id);
	}
	if(this.top){
		var st = $(window).scrollTop();
		this.$container.css('top', (this.top + st) + 'px');
	}
	zindex += 1;
	this.$container.css('z-index', zindex);
	
	this.$dialog = $('<div class="modal-dialog"></div>');
	this.$warp = $('<div class="modal-content"></div>');

	// 兼容以前的变量
	var width = this.width ? this.width : (this.msgwidth ? this.msgwidth : '380');
	var height = this.height ? this.height : (this.msgheight ? this.msgheight : '80');
	var title = this.title ? this.title :( this.msgtitle ? this.msgtitle : '${tk:lang("消息提示")}' ) ; //最终版本需改为没有为false
	var content = this.content ? this.content :( this.msgcontent ? this.msgcontent : '${tk:lang("确认继续当前操作")}？' ) ;
	var type = this.type ? this.type : (this.msgtype ? this.msgtype : 'info');
	
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
	
	var blurclose =  this.blurclose;
	blurclose = blurclose == undefined || blurclose == null ?  true : blurclose;
	
	if(typeof width == 'string'){
		if(!/[0-9]/.test(width)){	//参数不带数字的即为标准宽度
			width = this.widthList[width];
		}else if (width.indexOf('%') > -1) {
			var bodywidth = $(window).width();
			var winwidth = bodywidth * parseFloat(width.replace('%', '')) / 100;
			width = winwidth;
		}else if(width.indexOf('px') > -1){
			width = width.replace('px' , '');	
		}
	} else if (parseFloat(width) != width) {
		width = width;
	}
	this.$dialog.css('width', width + 'px');
	if (typeof height == 'string' && height.indexOf('%') > -1) {
		var bodyheight = $(window).height();
		var winheight = bodyheight * parseFloat(height.replace('%', '')) / 100;
		height = winheight;
	}else if(typeof height == 'string' && height.indexOf('px') > -1){
		height = height.replace('px' , '');
	} else if (parseFloat(height) != height) {
		height = height;
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
		for (var i = 0; i < MODALDIALOG_LIST.length; i++) {
			if (MODALDIALOG_LIST[i]) {
				MODALDIALOG_LIST[i].hide();		
			}
		}
	}

	var $header = $('<div class="modal-header"></div>');
	var $header_title = $('<h5 class="modal-title"></h5>');
	if(title){
		$header_title.append(title);
	}
	if(!this.hideClose){
		var $headerclose = $('<button type="button" class="close" ata-dismiss="modal" aria-hidden="true">&times;</button>');
		$headerclose.on('click', function() {
			that.hide();
		});
		$header.append($headerclose);		
	}
	if(title){
		$header.append($header_title);
		this.$warp.append($header);
	}
	var $bodydiv = $('<div class="modal-body"></div>');
	if (allowscroll) {
		$bodydiv.css({
			'word-break' : 'break-all',
			'position' : 'relative',
			'overflow-y' : 'auto',
			'overflow-x' : 'hidden',
			'height' : height + 'px'
		});
	}else{
		$bodydiv.css({
			'word-break' : 'break-all',
			'position' : 'relative',
			'min-height' : height + 'px'
		});
	}
	if(!title){
		$bodydiv.css({
			'padding-top' : '40px'
		});		
	}
	$bodydiv.append(content);
	this.$warp.append($bodydiv);
	var $footer = $('<div class="modal-footer"></div>');
	/*var $footerdiv = $('<div class="btn-group"></div>');*/
	var $okbtn = $('<a class="btn btn-sm ' + (type == 'del' ? "btn-delete" : "btn-primary") + '">${tk:lang("确定")}</a>');
	var $cancelbtn;
	if(this.showclose){
		$cancelbtn = $('<a class="btn btn-cancel btn-sm">${tk:lang("取消")}</a>');
		$cancelbtn.click(function(event) {
			that.disable();
			if (that.cancelFuc && typeof that.cancelFuc == 'function') {
				try {
					that.cancelFuc(event);
					that.enable();
				} catch (e) {
					showPopMsg.error(e.description);
					that.enable();
				}
			}
			that.hide();
			that.enable();
		});
	}
	
	
	$okbtn.on('click', function(event) {
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
					returnVal = that.successFuc(event);
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
	
	if (checkbtn) {
		this.buttonList.push($okbtn);
		if(this.showclose){
			this.buttonList.push($cancelbtn);
			$footer.append($okbtn).append($cancelbtn);
		}else{
			$footer.append($okbtn);
		}
	}
	
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
			$footer.append($btn);
		}
	}
	
	if (checkbtn || this.customButtons) {
		/*$footer.append($footerdiv);*/
		this.$warp.append($footer);	
	}

	this.$dialog.append(this.$warp);
	this.$container.append(this.$dialog);
	$('body').append(this.$container);
	
	this.$container.on('shown.bs.modal',function() {
		if (that.showFuc && typeof that.showFuc == 'function') {
			that.showFuc();
		}
	});
	
	
	this.$container.modal("show");
	$('body').addClass('modal-open');
	if (!blurclose) {
		this.$container.unbind('click.dismiss.modal');
	}
	
	this.$container.on('hidden.bs.modal', function() {
		if (that.closeFuc && typeof that.closeFuc == 'function') {
			try {
				that.closeFuc();
			} catch (er) {
				showPopMsg.error(er.description);
			}
		}
		that.hide();
		$(this).remove();
	});
	
	var MODALDIALOG_LIST = that.getModalList();
	MODALDIALOG_LIST.push(that);
	that.setModalList(MODALDIALOG_LIST);
};

ModalDialog.prototype.enable = function() {
	var that = this;
	if (that.buttonList && that.buttonList.length > 0) {
		for (var i = 0; i < that.buttonList.length; i++) {
			var btn = that.buttonList[i];
			btn.removeAttr('disabled');
		}
	}
};

ModalDialog.prototype.disable = function() {
	var that = this;
	if (that.buttonList && that.buttonList.length > 0) {
		for (var i = 0; i < that.buttonList.length; i++) {
			var btn = that.buttonList[i];
			btn.attr('disabled', 'disabled');
		}
	}
};

ModalDialog.prototype.getModalList = function() {
	var $body = $('body');
	var modaldialog_list = $body.data('modaldialog_list');
	if(!modaldialog_list){
		modaldialog_list = new Array();
	}
	return modaldialog_list;
};

ModalDialog.prototype.setModalList = function(_newList) {
	$('body').data('modaldialog_list' , _newList);
};


ModalDialog.prototype.hide = function() {
	var that = this;
	var MODALDIALOG_LIST = that.getModalList();
	this.$container.animate({}, 'fast', function() {
		if (that.closeFuc && typeof that.closeFuc == 'function') {
			that.closeFuc();
		}
		if($('.slidedialog_container ,.modal-dialog').length<=1){
			$('body').removeClass('modal-open');
		}
		var tmpArray =  new Array();
		for (var i = 0; i < MODALDIALOG_LIST.length; i++) {
			if (MODALDIALOG_LIST[i] && MODALDIALOG_LIST[i] == that) {
				delete MODALDIALOG_LIST[i];
			}else{
				tmpArray.push(MODALDIALOG_LIST[i]);
			}
		}
		that.setModalList(tmpArray);
		that.$container.modal("hide");
		that.$container.remove();
		that = null;
	});
}
