var p = location.pathname.toString().split('/');
var contextPath = "/" + p[1];
//

function getTop() {
	return $(window).scrollTop() + 150;
}

function getWidth() {
	return $(window).width();
}

var showPopMsgT = null;

function closePopMsg(id) {
	var div;
	if (self != top) {
		try {
			div = $(top.document.body).find('#' + id);
		} catch (e) {
			div = $('#' + id);
		}
	} else {
		div = $('#' + id);
	}
	div.fadeOut(function() {
		div.remove();
		if (self != top) {
			try {
				if (top.showPopMsgT != null) {
					top.clearTimeout(top.showPopMsgT);
					top.showPopMsgT = null;
				}
			} catch (e) {
			}
		} else {
			if (showPopMsgT != null) {
				clearTimeout(showPopMsgT);
				showPopMsgT = null;
			}
		}
	});
}

/**
 * 消息提示框 msg 消息显示内容 type 类型（info、ok、error） time 可以不传，默认是5秒
 */
var showPopMsg = {
	info : function() {
		var msg = null, time = null, callback = null;
		for (var i = 0; i < arguments.length; i++) {
			if (typeof arguments[i] == 'string' || typeof arguments[i] == 'object') {
				msg = arguments[i];
			} else if (typeof arguments[i] == 'number') {
				time = arguments[i];
			} else if (typeof arguments[i] == 'function') {
				callback = arguments[i];
			}
		}
		if (msg != null) {
			if (self != top) {
				try {
					toastr = top.toastr;
				} catch (e) {
				}
			}
			toastr.options = {
				"closeButton" : true,
				"debug" : false,
				"newestOnTop" : false,
				"progressBar" : true,
				"positionClass" : "toast-top-right",
				"preventDuplicates" : false,
				"onclick" : null,
				"showDuration" : "300",
				"hideDuration" : "1000",
				"timeOut" : (time * 1000) || 3000,
				"extendedTimeOut" : "1000",
				"showEasing" : "swing",
				"hideEasing" : "linear",
				"showMethod" : "fadeIn",
				"hideMethod" : "fadeOut",
				"onShown" : callback
			};
			toastr.info(msg);
		}
	},
	success : function() {
		var msg = null, time = null, callback = null;
		for (var i = 0; i < arguments.length; i++) {
			if (typeof arguments[i] == 'string') {
				msg = arguments[i];
			} else if (typeof arguments[i] == 'number') {
				time = arguments[i];
			} else if (typeof arguments[i] == 'function') {
				callback = arguments[i];
			}
		}
		if (msg == null) {
			msg = '${tk:lang("操作成功")}';
		}
		if (msg != null) {
			if (self != top) {
				try {
					toastr = top.toastr;
				} catch (e) {
				}
			}
			toastr.options = {
				"closeButton" : true,
				"debug" : false,
				"newestOnTop" : true,
				"progressBar" : true,
				"positionClass" : "toast-top-right",
				"preventDuplicates" : false,
				"onclick" : null,
				"showDuration" : "300",
				"hideDuration" : "1000",
				"timeOut" : (time * 1000) || 2000,
				"extendedTimeOut" : "1000",
				"showEasing" : "swing",
				"hideEasing" : "linear",
				"showMethod" : "fadeIn",
				"hideMethod" : "fadeOut",
				"onShown" : callback
			};
			toastr.success(msg);
		}
	},
	error : function() {
		var msg = null, time = null, callback = null, errorMsg = "";
		for (var i = 0; i < arguments.length; i++) {
			if (typeof arguments[i] == 'string') {
				msg = arguments[i];
			} else if (typeof arguments[i] == 'number') {
				time = arguments[i];
			} else if (typeof arguments[i] == 'function') {
				callback = arguments[i];
			} else if (typeof arguments[i] == 'object') {
				errorMsg = arguments[i].Message;
			}
		}
		if (msg == null) {
			msg = '${tk:lang("操作失败，异常")}：<br>' + errorMsg;
		}
		if (msg != null) {
			if (self != top) {
				try {
					toastr = top.toastr;
				} catch (e) {
				}

			}
			toastr.options = {
				"closeButton" : true,
				"debug" : false,
				"newestOnTop" : false,
				"progressBar" : true,
				"positionClass" : "toast-top-right",
				"preventDuplicates" : false,
				"onclick" : null,
				"showDuration" : "300",
				"hideDuration" : "1000",
				"timeOut" : (time * 1000) || 5000,
				"extendedTimeOut" : "1000",
				"showEasing" : "swing",
				"hideEasing" : "linear",
				"showMethod" : "fadeIn",
				"hideMethod" : "fadeOut",
				"onShown" : callback
			};
			toastr.error(msg);
		}
	},
	warn : function() {
		var msg = null, time = null, callback = null;
		for (var i = 0; i < arguments.length; i++) {
			if (typeof arguments[i] == 'string' || typeof arguments[i] == 'object') {
				msg = arguments[i];
			} else if (typeof arguments[i] == 'number') {
				time = arguments[i];
			} else if (typeof arguments[i] == 'function') {
				callback = arguments[i];
			}
		}
		if (msg != null) {
			if (self != top) {
				try {
					toastr = top.toastr;
				} catch (e) {
				}
			}
			toastr.options = {
				"closeButton" : true,
				"debug" : false,
				"newestOnTop" : false,
				"progressBar" : true,
				"positionClass" : "toast-top-right",
				"preventDuplicates" : false,
				"onclick" : null,
				"showDuration" : "300",
				"hideDuration" : "1000",
				"timeOut" : (time * 1000) || 5000,
				"extendedTimeOut" : "1000",
				"showEasing" : "swing",
				"hideEasing" : "linear",
				"showMethod" : "fadeIn",
				"hideMethod" : "fadeOut",
				"onShown" : callback
			};
			toastr.warning(msg);
		}
	}
};
var MASKFULLSCREEN = false;
function loadingMask() {
	if (self != top) {
		var div;
		if (MASKFULLSCREEN) {
			if ($('#loading-box').length == 0) {
				$('body').append('<div id="loading-box"><div class="loader"></div></div>');
			}
			div = $('#loading-box');
			div.stop().fadeIn();
		} else {
			try {
				var loadingInd = $(self.frameElement).attr('index');
				if ($(top.document.body).find('#ul-tab').find('.actived#tab-item-' + loadingInd).length > 0) {
					$(top.document.body).find('#ul-tab').find('.actived#tab-item-' + loadingInd).addClass('loading');
				}
			} catch (e) {
			}
		}
	}
}
/* 取消加载效果方法 */
function removeMask() {
	if (self != top) {
		if (MASKFULLSCREEN) {
			$('#loading-box').stop().fadeOut();
		} else {
			try {
				var loadingInd = $(self.frameElement).attr('index');
				if ($(top.document.body).find('#ul-tab').find('#tab-item-' + loadingInd).length > 0) {
					$(top.document.body).find('#ul-tab').find('#tab-item-' + loadingInd).find('.loadingbar').css({
						'left' : '100%'
					});
					$(top.document.body).find('#ul-tab').find('#tab-item-' + loadingInd).removeClass('loading');
					$(top.document.body).find('#ul-tab').find('#tab-item-' + loadingInd).find('.loadingbar').css({
						'left' : '-100%'
					});
				}
			} catch (e) {
			}
		}
	}
}
function encodeHtml(str) {
	var s = "";
	if (typeof str != 'undefined' && str != null) {
		if (str.length == 0)
			return "";
		s = str.replace(/&/g, "&amp;");
		s = s.replace(/</g, "&lt;");
		s = s.replace(/>/g, "&gt;");
		s = s.replace(/\'/g, "&#39;");
		s = s.replace(/\"/g, "&quot;");
	}
	return s;
}

function decodeHtml(str) {
	var s = "";
	if (typeof str != 'undefined' && str != null) {
		if (str.length == 0)
			return "";
		s = str.replace(/&quot;/g, "\"");
		s = s.replace(/&#39;/g, "\'");
		s = s.replace(/&gt;/g, ">");
		s = s.replace(/&lt;/g, "<");
		s = s.replace(/&amp;/g, "&");
	}
	return s;
}

function randomBgColor() {
	var r, g, b;
	r = decToHex(randomNumber(256) - 1);
	g = decToHex(randomNumber(256) - 1);
	b = decToHex(randomNumber(256) - 1);
	return "#" + r + g + b;
}

function resetIframeHeight(ifm) {
	var ifrm = (typeof ifm == 'string' ? document.getElementById(ifm) : ifm);
	$(ifrm).css('height', '100%');
}

function setContentIframeHeight(ifm) {
	var ifrm = (typeof ifm == 'string' ? document.getElementById(ifm) : ifm);
	var oldTimego = $(ifrm).data("timego");
	if (oldTimego) {// 先停止再开始，否则会可能有多个计时器
		clearInterval(oldTimego);
	}
	var timego = setTimeout(function() {
		try {
			var scrolltop = $(window).scrollTop();
			ifrm.style.height = '0px';
			ifrm.style.height = $(ifrm).contents().height() + 'px';
			$(window).scrollTop(scrolltop);
			// console.log(timego)
		} catch (e) {

		}
		if (!$(ifrm).hasClass('fullContent')) {
			// 如果弹出框全屏的，只做一次高度计算
			setTimeout(arguments.callee, 1000);
		}
	}, 1000);
	$(ifrm).data("timego", timego);
}

function setContentIframeHeightDelay(ifrm) {
	timego = setTimeout(function() {
		try {
			var scrolltop = $(window).scrollTop();
			ifrm.style.height = '0px';
			ifrm.style.height = $(ifrm).contents().height() + 'px';
			$(window).scrollTop(scrolltop);
		} catch (e) {

		}
		setTimeout(arguments.callee, 500);
	}, 500);
}

function timeinfo() {
	$(".timeSpan").each(function() {
		var rowid = $(this).attr("index");
		var stoptime = $(this).attr("time");
		var date = new Date();
		countTime(stoptime, rowid);
	});
}

function getDateTime(dateStr) {
	if (dateStr) {
		var newstr = dateStr.replace(/-/g, '/');
		var date = new Date(newstr);
		return date.getTime();
	} else {
		return dateStr;
	}
}

function getDateStr(date) {
	if (date) {
		var date = new Date(date);
		Y = date.getFullYear() + '-';
		M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '-';
		D = date.getDate() < 10 ? '0' + date.getDate() + ' ' : date.getDate() + ' ';
		h = date.getHours() < 10 ? '0' + date.getHours() + ':' : date.getHours() + ':';
		m = date.getMinutes() < 10 ? '0' + date.getMinutes() + ':' : date.getMinutes() + ':';
		s = date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds();
		return Y + M + D + h + m + s;
	} else {
		return date;
	}
}

/**
 * 设置Ajax全局，请求超时跳转到当前窗口链接
 */
var REQUEST_LIST = new Array();
var originalXhr = $.ajaxSettings.xhr;
jQuery.ajaxSettings.traditional = true;

$.ajaxSetup({
	contentType : "application/x-www-form-urlencoded;charset=utf-8",
	context : document.body,
	complete : function(XMLHttpRequest, textStatus) {
		var isLoad = true;
		REQUEST_LIST.forEach(function(xml, index) {
			if (xml == XMLHttpRequest) {
				REQUEST_LIST.splice(index, 1);
				isLoad = false;
			}
		});
		//20190815 zqp reload过滤上传文件的接口（上传取消的时候不需要刷新页面）
		//20191030 zqp 去掉 !XMLHttpRequest.status 判断（部分接口返回0会刷新页面）
		if (!isLoad && (XMLHttpRequest.status == 403 || XMLHttpRequest.status == 302 || (XMLHttpRequest.status == 200 && XMLHttpRequest.statusText=="parsererror"))) {
			try{
			    top.location.reload();
			}catch(ev){
			}
		}
		if (XMLHttpRequest.getResponseHeader("redirect_url")) {
			window.location.href = XMLHttpRequest.getResponseHeader("redirect_url");
		}

		if (REQUEST_LIST.length == 0) {
			removeMask();
		}
	},
	beforeSend : function(XMLHttpRequest, url) {
		if (url.enctype != "multipart/form-data") {
			REQUEST_LIST.push(XMLHttpRequest);
			loadingMask();
		}
	},
	error : function(xhr, status, e) {
		REQUEST_LIST.forEach(function(xml, index) {
			if (xml == xhr) {
				REQUEST_LIST.splice(index, 1);
			}
		});
		if (REQUEST_LIST.length == 0) {
			removeMask();
		}
	}
});

// 检查当前窗口是否可见
function checkCurrentWindowIsVisible() {
	try {
		var arrFrames = top.document.getElementsByTagName("IFRAME");
		for (var i = 0; i < arrFrames.length; i++) {
			if (arrFrames[i].contentWindow === window) {
				return $(arrFrames[i]).is(':visible');
			}
		}
	} catch (e) {
	}
	return false;
}

// 全屏切换
function toggleFullScreen(id) {
	var elm = document.getElementById(id);
	if ((document.fullScreenElement && document.fullScreenElement !== null) || (!document.mozFullScreen && !document.webkitIsFullScreen)) {
		if (document.documentElement.requestFullScreen) {
			elm.requestFullScreen();
		} else if (document.documentElement.mozRequestFullScreen) {
			elm.mozRequestFullScreen();
		} else if (document.documentElement.webkitRequestFullScreen) {
			elm.webkitRequestFullScreen();
		}
	} else {
		if (document.cancelFullScreen) {
			document.cancelFullScreen();
		} else if (document.mozCancelFullScreen) {
			document.mozCancelFullScreen();
		} else if (document.webkitCancelFullScreen) {
			document.webkitCancelFullScreen();
		}
	}
}

function generateUuid() {
	return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
		var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
		return v.toString(16);
	});
}

function getMaxZ() {
	var index_highest = 1;
	$('div,span,table,input,button,ul,i,th').filter(':visible').each(function() {
		if ($(this).css("zIndex")) {
			var index_current = parseInt($(this).css("zIndex"), 10);
			if (index_current > index_highest) {
				index_highest = index_current;
			}
		}
	});
	return index_highest;
}

var filehandler = (function() {
	return {
		getSizeText : function(size) {
			if (size) {
				if (size < 1024) {
					return size + " Byte";
				} else if (size < 1024 * 1024) {
					return (size / 1024).toFixed(2) + " KB";
				} else {
					return (size / 1048576).toFixed(2) + " MB";
				}

			} else {
				return size;
			}
		}
	}
}());

var pagination = (function() {
	return {
		callPageFuc : function(item, pageFuc) {
			pageFuc = unescape(pageFuc);
			var index = pageFuc.indexOf(':');
			index = (index == -1 ? 0 : index);
			pageFuc = pageFuc.substring(index + 1, pageFuc.length);
			pageFuc = pageFuc.replace(/\$pagesize/igm, item.value).replace(/\$page/igm, 1);
			eval(pageFuc);
		},
		number : function(obj, pageFuc, needpagesize) {
			var pageFucEncode = escape(pageFuc);
			var str = '';
			if (obj.currentPage && obj.pageCount > 1 && pageFuc) {
				str = '<div class="pagination-page"><ul class="pagination pagination-sm">';
				if (obj.currentPage <= 1) {
					str += '<li class="prev disabled"><a href="javascript:void(0)"><i class="ts-angle-double-left"></i></a></li>'
					str += '<li class="pageno disabled"><a href="javascript:void(0)"><i class="ts-angle-left"></i></a></li>'
				} else {
					str += '<li class="prev"><a  href="javascript:void(0)" onclick="' + pageFuc.replace(/\$pagesize/igm, obj.pageSize).replace(/\$page/igm, 1) + '"><i class="ts-angle-double-left"></i></a></li>'
					str += '<li class="pageno"><a  href="javascript:void(0)" onclick="' + pageFuc.replace(/\$pagesize/igm, obj.pageSize).replace(/\$page/igm, obj.currentPage - 1) + '"><i class="ts-angle-left"></i></a></li>';
				}

				var maxpage = obj.currentPage + 3 <= obj.pageCount ? obj.currentPage + 3 : obj.pageCount;
				var minpage = obj.currentPage - 3 >= 1 ? obj.currentPage - 3 : 1;
				if (minpage > 1) {
					str += '<li class="disabled"><a href="javascript:void(0)">...</a></li>';
				}

				for (var k = minpage; k <= maxpage; k++) {
					if (obj.currentPage == k) {
						str += '<li class="active"><a  href="javascript:void(0)">' + k + '</a></li>';
					} else {
						str += '<li class="pageno"><a  href="javascript:void(0)" onclick="' + pageFuc.replace(/\$pagesize/igm, obj.pageSize).replace(/\$page/igm, k) + '">' + k + '</a></li>';
					}
				}

				if (maxpage < obj.pageCount) {
					str += '<li class="disabled"><a>...</a></li>';
				}
				if (obj.currentPage >= obj.pageCount) {
					str += '<li class="pageno disabled"><a href="javascript:void(0)"><i class="ts-angle-right"></i></a></li>';
					str += '<li class="next disabled"><a href="javascript:void(0)"><i class="ts-angle-double-right"></i></a></li>';
				} else {
					str += '<li class="pageno"><a  href="javascript:void(0)" onclick="' + pageFuc.replace(/\$pagesize/igm, obj.pageSize).replace(/\$page/igm, obj.currentPage + 1) + '"><i class="ts-angle-right"></i></a></li>';
					str += '<li class="next"><a href="javascript:void(0)" onclick="' + pageFuc.replace(/\$pagesize/igm, obj.pageSize).replace(/\$page/igm, obj.pageCount) + '"><i class="ts-angle-double-right"></i></a></li>';
				}
				str += '</ul></div>';
				if (maxpage < obj.pageCount || minpage > 1) {
					str += '<div class="row-num">';
					str += '${tk:lang("跳转到")}：<input style="border:0px;width:' + (obj.currentPage.toString().length * 7 + 25)
							+ 'px" min="1" max="10000"  type="number" onchange="if(parseInt(this.value,10) != this.value){this.value = parseInt(this.value,10);}if(this.value < 1){this.value = 1;}else if(this.value > ' + obj.rowNum
							+ '){this.value = ' + obj.rowNum + ';}' + pageFuc.replace(/\$pagesize/igm, obj.pageSize).replace(/\$page/igm, 'this.value') + '" value="' + obj.currentPage + '">';
					str += '</div>';
				}
			}
			if (needpagesize) {
				str += '<div class="row-num">';
				if (obj.rowNum) {
					str += '${tk:lang("总数")}：' + obj.rowNum + '&nbsp;&nbsp;';
				}
				str += '${tk:lang("每页显示")}：<input min="1" style="border:0px;width:'
						+ (obj.pageSize.toString().length * 7 + 25)
						+ 'px" max="10000"  type="number" onchange="if(parseInt(this.value,10) != this.value){this.value = parseInt(this.value,10);}if(this.value < 1){this.value = 1;}else if(this.value > 10000){this.value = 10000;}pagination.callPageFuc(this,\''
						+ pageFucEncode + '\')" value="' + obj.pageSize + '" name="pageSize">';
				str += '</div>';
			}
			if (str != '') {
				str = '<div class="pagination-contain">' + str + '</div>';
			}
			return str;
		},
		icon : function(obj, pageFuc, icontype) {
			var pageFucEncode = escape(pageFuc);
			var icontype = icontype || false;
			var str = '';
			if (obj.currentPage && obj.pageCount > 1 && pageFuc) {
				var maxpage = obj.currentPage + 3 <= obj.pageCount ? obj.currentPage + 3 : obj.pageCount;
				var minpage = obj.currentPage - 3 >= 1 ? obj.currentPage - 3 : 1;
				if (icontype) {
					str = '<div class="pagination-contain" style="text-align: center;"><ul class="pagination pagination-circle">';
				} else {
					str = '<div class="pagination-contain" style="text-align: center;"><ul class="pagination pagination-circle-hollow">';
				}
				if (obj.pageCount > 5) {
					if (obj.currentPage > 1) {
						str += '<li class="prev" onclick="' + pageFuc.replace(/\$pagesize/igm, obj.pageSize).replace(/\$page/igm, 1) + '"></li>'
						str += '<li class="pageno prevno" onclick="' + pageFuc.replace(/\$pagesize/igm, obj.pageSize).replace(/\$page/igm, obj.currentPage - 1) + '" style="margin-right:20px;"></li>';
					} else {
						str += '<li class="prev disabled"></li>'
						str += '<li class="pageno disabled prevno" style="margin-right:20px;"></li>'
					}
				}
				if (minpage > 1) {
					str += '<li class="more">...</li>';
				}
				for (var k = minpage; k <= maxpage; k++) {
					if (obj.currentPage == k) {
						str += '<li class="active amount"><div class="acount">' + k + '</div></li>';
					} else {
						str += '<li class="amount" onclick="' + pageFuc.replace(/\$pagesize/igm, obj.pageSize).replace(/\$page/igm, k) + '"><div class="acount">' + k + '</div></li>';
					}
				}
				if (maxpage < obj.pageCount) {
					str += '<li class="more">...</li>';
				}
				if (obj.pageCount > 5) {
					if (obj.currentPage < obj.pageCount) {
						str += '<li class="pageno nextno" onclick="' + pageFuc.replace(/\$pagesize/igm, obj.pageSize).replace(/\$page/igm, obj.currentPage + 1) + '" style="margin-left:20px;"></li>';
						str += '<li class="next"  onclick="' + pageFuc.replace(/\$pagesize/igm, obj.pageSize).replace(/\$page/igm, obj.pageCount) + '"></li>';
					} else {
						str += '<li class="pageno disabled nextno" style="margin-left:20px;"></li>';
						str += '<li class="next disabled"></li>';
					}
				}
				str += '</ul>';
				str += '</div>';
			}
			return str;
		}
	}
}());

// 处理键盘事件 禁止后退键（Backspace）密码或单行、多行文本框除外
function forbidKey(e) {
	var ev = e || window.event; // 获取event对象
	var obj = ev.target || ev.srcElement; // 获取事件源
	var t = obj.type || obj.getAttribute('type'); // 获取事件源类型
	// 获取作为判断条件的事件类型
	var vReadOnly = obj.readOnly;
	var vDisabled = obj.disabled;
	// 处理undefined值情况
	vReadOnly = (vReadOnly == undefined) ? false : vReadOnly;
	vDisabled = (vDisabled == undefined) ? true : vDisabled;
	// 当敲Backspace键时，事件源类型为密码或单行、多行文本的，
	// 并且readOnly属性为true或disabled属性为true的，则退格键失效
	var flag1 = ev.keyCode == 8 && (t == "password" || t == "text" || t == "textarea" || t == "number") && (vReadOnly == true || vDisabled == true);
	// 当敲Backspace键时，事件源类型非密码或单行、多行文本的，则退格键失效
	var flag2 = ev.keyCode == 8 && t != "password" && t != "text" && t != "textarea" && t != "number";
	// 屏蔽回车
	var flag3 = ev.keyCode == 13 && t != "password" && t != "text" && t != "textarea";
	// 判断
	if (flag2 || flag1 || flag3) {
		ev.stopPropagation();
		ev.returnValue = false;// IE
		return false;
	}
}
function getRandomId(len) {
	var str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	var res = "";
	for (var i = 0; i < len; i++) {
		var number = Math.floor(Math.random() * 62);
		res += str.substr(number, 1);
	}
	return res;
}

function changeLeftWidth($this) {// 在body为body-column data-drag="true"
	// 的情况下面可以拖动左边改变距离
	var $divLeft = $this.children('.body-left');
	var $divMain = $this;
	var path = window.location.pathname;
	var width = $divLeft.width() + $divLeft.offset().left;
	var isMove = false, isDown = false;
	if (localStorage && localStorage.getItem(path)) {
		$divMain.css('padding-left', localStorage.getItem(path) + 'px');
		$divLeft.css('width', localStorage.getItem(path) + 'px');
		width = parseInt(localStorage.getItem(path)) + $divLeft.offset().left;
	}
	$this.mousemove(function(event) {
		var clientX = event.clientX;
		if (!isDown) {
			if ((width - 4) < clientX && clientX < (width + 4) && !isMove) {
				$(this).css('cursor', 'w-resize').addClass('noselect-box');
				isMove = true;
			} else if (isMove && !((width - 6) < clientX && clientX < (width + 6))) {
				$(this).css('cursor', 'unset').removeClass('noselect-box');
				isMove = false;
			}
		} else {
			var padding = clientX - $divLeft.offset().left;
			if (padding < 400 && padding > 240) {
				width = padding + $divLeft.offset().left
				$divMain.css('padding-left', padding);
				$divLeft.css('width', padding);
			}
		}
	});

	$this.mousedown(function(event) {
		if (isMove) {
			isDown = true;
		}
	});
	$this[0].addEventListener('mousedown', function(evt) {
		var e = (evt) ? evt : window.event;
		if (isMove) {
			isDown = true;
		}
	}, true);

	$this[0].addEventListener('click', function(evt) {// 防止在可以拖动的情况一下操作其他
		var e = (evt) ? evt : window.event;
		if (isMove) {
			window.event ? e.cancelBubble = true : e.stopImmediatePropagation();
		}
	}, true);

	$this.mouseup(function(event) {
		if (isMove && isDown) {
			isDown = false;
			localStorage.setItem(path, width);
		}
	});
}

// 获取url中指定name的参数
function getUrlParams(name) {
	var reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)', 'i');
	var paramStr = window.location.search;
	var r = window.location.search.substr(1).match(reg);
	if (r != null) {
		return unescape(r[2]);
	}
	return null;
}

$(function() {
	// IE也能用textarea
	$("textarea[maxlength]").keyup(function() {
		var area = $(this);
		var max = parseInt(area.attr("maxlength"), 10); // 获取maxlength的值
		if (max > 0) {
			if (area.val().length > max) { // textarea的文本长度大于maxlength
				area.val(area.val().substr(0, max)); // 截断textarea的文本重新赋值
			}
		}
	});
	// 复制的字符处理问题
	$("textarea[maxlength]").blur(function() {
		var area = $(this);
		var max = parseInt(area.attr("maxlength"), 10); // 获取maxlength的值
		if (max > 0) {
			if (area.val().length > max) { // textarea的文本长度大于maxlength
				area.val(area.val().substr(0, max)); // 截断textarea的文本重新赋值
			}
		}
	});
	$(document).on("keydown", "input.unsubmit", function(event) {
		// 输入框回车阻止浏览器默认提交表单事件
		if (event.keyCode == "13") {
			event.preventDefault();
		}
	});

	// 禁止后退键、回车 作用于Firefox、Opera
	document.onkeypress = forbidKey;
	// 禁止后退键、回车 作用于IE、Chrome
	document.onkeydown = forbidKey;
	// 防止页面后退
	history.pushState(null, null, document.URL);
	window.addEventListener('popstate', function() {
		history.pushState(null, null, document.URL);
	});
	// 自动隐藏btn-bar
	var btnbar = $('.btn-bar[data-toggle]');
	if (btnbar.length == 1) {
		btnbar.hide();
		var showbarbtn = $('<button class="btn-primary btn-showbar" style="z-index:9999;font-size:12px;padding:3px 20px 0px 20px;border-top-right-radius:5px;border-top-left-radius:5px;border:0px;left:50%;position:fixed;bottom:0px;"><i class="glyphicon glyphicon-menu-hamburger"></i></button>');
		showbarbtn.on('click', function() {
			btnbar.show();
			btnbar.css({
				'bottom' : -btnbar.height(),
				left : 0
			});
			btnbar.animate({
				bottom : 0
			}, 'fast');
			$(this).hide();
			if (btnbar.data('helpblock')) {
				if ($('#' + btnbar.data('helpblock')).length > 0) {
					$('#' + btnbar.data('helpblock')).fadeOut('fast');
				}
			}
		});

		btnbar.on('mouseleave', function() {
			var timeout = $(this).data('timeout');
			if (!timeout) {
				timeout = window.setTimeout(function() {
					btnbar.animate({
						bottom : -btnbar.height()
					}, 'fast', null, function() {
						showbarbtn.fadeIn();
						btnbar.hide();
					});

				}, 2000);
				$(this).data('timeout', timeout);
			}
		});

		btnbar.on('mouseenter', function() {
			var timeout = $(this).data('timeout');
			if (timeout) {
				window.clearTimeout(timeout);
				timeout = null;
				$(this).data('timeout', null);
			}
		});
		$('body').append(showbarbtn);
	}
	$(document).on('click', '.row-tool[data-toggle="row"]', function() {
		$('body').toggleClass('hideleft');
		if (!$('body').hasClass('hideleft') && $(this).data('width')) {
			$(this).css({
				left : $(this).data('width')
			});
		}
	});

	// 当左边的位子不够时通过拖动来改变大小
	if ($('body.body-column').length > 0 && $('body.body-column').data('drag') && localStorage) {
		changeLeftWidth($('body.body-column>.body-main'));
	}
	// 拟下拉
	$(document).on('mouseenter', '.tsselect-container', function() {
		$(this).addClass('on');
	}).on('mouseleave', '.tsselect-container', function() {
		$(this).removeClass('on');
	});

	$(window).on('scroll', function() {
		if ($('.body-right-free').length > 0) {
			if ($(window).scrollTop() > 0) {
				$('.body-left').addClass('fixtop');
				$('.body-left').css('top', Math.max(0, 17 - $(window).scrollTop()));
				;
			} else {
				$('.body-left').removeClass('fixtop');
				$('.body-left').css('top', 17);
			}
		}
		if ($('.row-right-free').length > 0) {
			if ($(window).scrollTop() > 0) {
				$('.row-left').addClass('fixtop');
				$('.row-tool').addClass('fixtop');
				$('.row-left').css('top', Math.max(0, 17 - $(window).scrollTop()));
				$('.row-tool').css('top', Math.max(9, 25 - $(window).scrollTop()));
			} else {
				$('.row-left').removeClass('fixtop');
				$('.row-tool').removeClass('fixtop');
				$('.row-left').css('top', 17);
				$('.row-tool').css('top', 25);
			}
		}
	});
	$(document).on('focus', 'input:text', function() {
		$(this).attr("autocomplete", "off");
	});
	// table第一行选中的全选单选状态（需在table加一个table-checkbox）
	$(document).on('change', '.table-checkbox>thead>tr>th>.customcheckbox_square>input:checkbox', function() {
		var chkTd = $(this).closest('thead').siblings('tbody').find('tr').length > 0 ? $(this).closest('thead').siblings('tbody').find('tr') : null;
		if (chkTd) {
			if ($(this).prop('checked')) {
				chkTd.each(function(ind, item) {
					$(item).find('td:first').find('input:checkbox').prop('checked', true);
				});
			} else {
				chkTd.each(function(ind, item) {
					$(item).find('td:first').find('input:checkbox').prop('checked', false);
				});
			}
		}
	});
	$(document).on('change', '.table-checkbox>tbody>tr>td>.customcheckbox_square>input:checkbox', function() {
		var chkTh = $(this).closest('tbody').siblings('thead').find('th:first').find('input:checkbox') ? $(this).closest('tbody').siblings('thead').find('th:first').find('input:checkbox') : null;
		var chkTd = $(this).closest('tbody').find('tr');
		if (chkTh) {
			var chkAmount = 0;
			chkTd && chkTd.each(function(ind, item) {
				if ($(item).find('td:first').find('input:checkbox').prop('checked')) {
					chkAmount = chkAmount + 1;

				}
			})
			if (chkAmount == chkTd.length) {
				chkTh.prop('checked', true);
			} else {
				chkTh.prop('checked', false);
			}
		}

	});
});
