var kingwolfofsky = {
	/**
	 * 获取输入光标在页面中的坐标
	 * 
	 * @param {HTMLElement}
	 *            输入框元素
	 * @return {Object} 返回left和top,bottom
	 */
	getInputPositon : function(elem) {
		if (document.selection) { // IE Support
			elem.focus();
			var Sel = document.selection.createRange();
			var sh = document.documentElement.scrollTop;
			return {
				left : Sel.offsetLeft,
				top : Sel.offsetTop + sh,
				bottom : Sel.offsetTop + Sel.boundingHeight + sh
			};
		} else {
			var that = this;
			var cloneDiv = '{$clone_div}', cloneLeft = '{$cloneLeft}', cloneFocus = '{$cloneFocus}', cloneRight = '{$cloneRight}';
			var none = '<span style="white-space:pre-wrap;"> </span>';
			var div = elem[cloneDiv] || document.createElement('div'), focus = elem[cloneFocus] || document.createElement('span');
			var text = elem[cloneLeft] || document.createElement('span');
			var offset = that._offset(elem), index = this._getFocus(elem), focusOffset = {
				left : 0,
				top : 0
			};

			if (!elem[cloneDiv]) {
				elem[cloneDiv] = div, elem[cloneFocus] = focus;
				elem[cloneLeft] = text;
				div.appendChild(text);
				div.appendChild(focus);
				document.body.appendChild(div);
				focus.innerHTML = '|';
				focus.style.cssText = 'display:inline-block;width:0px;overflow:hidden;z-index:-100;word-wrap:break-word;word-break:break-all;';
				div.className = this._cloneStyle(elem);
				div.style.cssText = 'visibility:hidden;display:inline-block;position:absolute;z-index:-100;word-wrap:break-word;word-break:break-all;overflow:hidden;';
			}
			;
			div.style.left = this._offset(elem).left + "px";
			div.style.top = this._offset(elem).top + "px";
			var strTmp = elem.value.substring(0, index).replace(/</g, '<').replace(/>/g, '>').replace(/\n/g, '<br/>').replace(/\s/g, none);
			text.innerHTML = strTmp;

			focus.style.display = 'inline-block';
			try {
				focusOffset = this._offset(focus);
			} catch (e) {
			}
			;
			focus.style.display = 'none';

			return {
				left : focusOffset.left,
				top : focusOffset.top,
				bottom : focusOffset.bottom
			};
		}
	},

	// 克隆元素样式并返回类
	_cloneStyle : function(elem, cache) {
		if (!cache && elem['${cloneName}'])
			return elem['${cloneName}'];
		var className, name, rstyle = /^(number|string)$/;
		var rname = /^(content|outline|outlineWidth)$/; // Opera: content;
		// IE8:outline &&
		// outlineWidth
		var cssText = [], sStyle = elem.style;

		for (name in sStyle) {
			if (!rname.test(name)) {
				val = this._getStyle(elem, name);
				if (val !== '' && rstyle.test(typeof val)) { // Firefox 4
					name = name.replace(/([A-Z])/g, "-$1").toLowerCase();
					cssText.push(name);
					cssText.push(':');
					cssText.push(val);
					cssText.push(';');
				}
				;
			}
			;
		}
		;
		cssText = cssText.join('');
		elem['${cloneName}'] = className = 'clone' + (new Date).getTime();
		this._addHeadStyle('.' + className + '{' + cssText + '}');
		return className;
	},

	// 向页头插入样式
	_addHeadStyle : function(content) {
		var style = this._style[document];
		if (!style) {
			style = this._style[document] = document.createElement('style');
			document.getElementsByTagName('head')[0].appendChild(style);
		}
		;
		style.styleSheet && (style.styleSheet.cssText += content) || style.appendChild(document.createTextNode(content));
	},
	_style : {},

	// 获取最终样式
	_getStyle : 'getComputedStyle' in window ? function(elem, name) {
		return getComputedStyle(elem, null)[name];
	} : function(elem, name) {
		return elem.currentStyle[name];
	},

	// 获取光标在文本框的位置
	_getFocus : function(elem) {
		var index = 0;
		if (document.selection) {// IE Support
			elem.focus();
			var Sel = document.selection.createRange();
			if (elem.nodeName === 'TEXTAREA') {// textarea
				var Sel2 = Sel.duplicate();
				Sel2.moveToElementText(elem);
				var index = -1;
				while (Sel2.inRange(Sel)) {
					Sel2.moveStart('character');
					index++;
				}
				;
			} else if (elem.nodeName === 'INPUT') {// input
				Sel.moveStart('character', -elem.value.length);
				index = Sel.text.length;
			}
		} else if (elem.selectionStart || elem.selectionStart == '0') { // Firefox
			// support
			index = elem.selectionStart;
		}
		return (index);
	},

	// 获取元素在页面中位置
	_offset : function(elem) {
		var box = elem.getBoundingClientRect(), doc = elem.ownerDocument, body = doc.body, docElem = doc.documentElement;
		var clientTop = docElem.clientTop || body.clientTop || 0, clientLeft = docElem.clientLeft || body.clientLeft || 0;
		var top = box.top + (self.pageYOffset || docElem.scrollTop) - clientTop, left = box.left + (self.pageXOffset || docElem.scrollLeft) - clientLeft;
		return {
			left : left,
			top : top,
			right : left + box.width,
			bottom : top + box.height
		};
	}
};

(function($) {
	$.fn.at = function(options) {
		var defaults = {
			userurl : "/balantflow/user/getUserNameJson.do?name=",
			teamurl : "/balantflow/user/getTeamNameJson.do?name="
		}
		var options = $.extend(defaults, options);
		var at_pos;
		var cursor_pos;
		var find_mode = "";
		var weibo_maxlen = 200;
		var userDiv = $('<div style="position:absolute;z-index:1000;display:none;background:#f4f4f4;border:1px solid #ccc;padding:2px;" id="divAtUser123"></div>');
		var divInfo = '<div style="border: 1px dashed #ccc; padding: 2px;margin-bottom:2px;color:#CCC;">选择昵称或轻敲空格完成输入</div>';
		var divTeam = '<div class="divUserUnSelected" style="cursor: pointer; border: 0px; padding: 2px;margin:1px 0px 1px 0px;border-bottom:1px dashed #ddd">#{teamname}</div>';
		var divUser = '<div class="divUserUnSelected" style="cursor: pointer; border: 0px; padding: 2px;margin:1px 0px 1px 0px;border-bottom:1px dashed #ddd">[#{userid}]#{username}</div>';
		var limit = 10;

		$(function() {
			if ($('#divAtUser123').length == 0) {
				$('body').append(userDiv);
			} else {
				userDiv = $('#divAtUser123');
			}
		});

		function findSth(item) {
			if (find_mode != "#") {
				findUser(item);
			}
			if (find_mode != "@") {
				findGroup(item);
			}
		}

		function findGroup(item) {
			var id = item.id;
			var text = $(item).val();
			cursor_pos = getCursorPosition(item);
			var findText = text.substring(0, cursor_pos);
			at_pos = findText.lastIndexOf("#");
			var blank_pos = findText.lastIndexOf(" ");
			if (at_pos != -1) {
				if (blank_pos == -1 || blank_pos < at_pos) {
					find_mode = "#";
					var find_gid = findText.substring(at_pos + 1, cursor_pos);
					if(find_gid.length <= 2){
						return;
					}
					$.getJSON(options.teamurl + encodeURIComponent(find_gid), function(data) {
						userDiv.attr('containerid', id);
						var p = kingwolfofsky.getInputPositon(item);
						userDiv.html(divInfo);
						if (data.length > 0) {
							for (var t = 0; t < data.length; t++) {
								var tem = divTeam;
								tem = tem.replace(/\#\{teamname\}/gi, data[t]);
								tem = $(tem);
								tem.attr('gid', data[t]);
								tem.click(function() {
									selectGroup($(this).attr('gid'));
								});
								userDiv.append(tem);
								if (t == limit) {
									break;
								}
							}
						}
						userDiv.css({
							top : p.bottom + 'px',
							left : p.left + 'px'
						}).show();
					}).fail(function() {
						alert('加载组织架构列表失败！');
					});
				} else if (blank_pos > at_pos) {
					userDiv.hide();
					find_mode = "";
				}
			} else {
				userDiv.hide();
				find_mode = "";
			}
		}

		function findUser(item) {
			var id = item.id;
			var text = $(item).val();
			cursor_pos = getCursorPosition(item);
			var findText = text.substring(0, cursor_pos);
			at_pos = findText.lastIndexOf("@");
			var blank_pos = findText.lastIndexOf(" ");
			if (at_pos != -1) {
				if (blank_pos == -1 || blank_pos < at_pos) {
					find_mode = "@";
					var find_uid = findText.substring(at_pos + 1, cursor_pos);
					if (find_uid.length <= 2) {
						return;
					}
					$.getJSON(options.userurl + encodeURIComponent(find_uid), function(data) {
						userDiv.attr('containerid', id);
						var p = kingwolfofsky.getInputPositon(item);
						userDiv.html(divInfo);
						if (data.length > 0) {
							for (var t = 0; t < data.length; t++) {
								var tem = divUser;
								tem = tem.replace(/\#\{username\}/gi, data[t].username).replace(/\#\{userid\}/gi, data[t].userid);
								tem = $(tem);
								tem.attr('uid', data[t].userid);
								tem.click(function() {
									selectUser($(this).attr('uid'));
								});
								userDiv.append(tem);
								if (t == limit) {
									break;
								}
							}
						}
						userDiv.css({
							top : p.bottom + 'px',
							left : p.left + 'px'
						}).show();
					}).fail(function() {
						alert('加载用户列表失败！');
					});
				} else if (blank_pos > at_pos) {
					userDiv.hide();
					find_mode = "";
				}
			} else {
				userDiv.hide();
				find_mode = "";
			}
		}

		function selectUser(uid) {
			var container = userDiv.attr('containerid');
			if (container) {
				var text = $('#' + container).val();
				var text_pre = text.substring(0, at_pos);
				var text_sub = text.substring(cursor_pos);
				if (text.length - cursor_pos > 0) {// �س�������
					text = text_pre + '@' + uid + ' ' + text_sub;
				} else {
					text = text_pre + '@' + uid + ' ';
				}
				$('#' + container).val(text).focus();
				userDiv.hide();
				find_mode = "";
			}
		}

		function selectGroup(gid) {
			var container = userDiv.attr('containerid');
			if (container) {
				var text = $('#' + container).val();
				var text_pre = text.substring(0, at_pos);
				var text_sub = text.substring(cursor_pos);
				if (text.length - cursor_pos > 0) {// �س�������
					text = text_pre + '#' + gid + ' ' + text_sub;
				} else {
					text = text_pre + '#' + gid + ' ';
				}
				$('#' + container).val(text).focus();
				userDiv.hide();
				find_mode = "";
			}
		}

		function getCursorPosition(textarea) {
			var rangeData = {
				text : "",
				start : 0,
				end : 0
			};
			textarea.focus();
			if (textarea.setSelectionRange) { // W3C
				rangeData.start = textarea.selectionStart;
				rangeData.end = textarea.selectionEnd;
				rangeData.text = (rangeData.start != rangeData.end) ? textarea.value.substring(rangeData.start, rangeData.end) : "";
			} else if (document.selection) { // IE
				var i, oS = document.selection.createRange(),
				// Don't: oR = textarea.createTextRange()
				oR = document.body.createTextRange();
				oR.moveToElementText(textarea);

				rangeData.text = oS.text;
				rangeData.bookmark = oS.getBookmark();

				// object.moveStart(sUnit [, iCount])
				// Return Value: Integer that returns the number of units moved.
				for (i = 0; oR.compareEndPoints('StartToStart', oS) < 0 && oS.moveStart("character", -1) !== 0; i++) {
					// Why? You can alert(textarea.value.length)
					if (textarea.value.charAt(i) == '\n') {
						i++;
					}
				}
				rangeData.start = i;
				rangeData.end = rangeData.text.length + rangeData.start;
			}
			return rangeData.end;
		}

		$(this).keydown(function(event) {
			if (event.keyCode == 13) {// 回车键
				if (!userDiv.is(':hidden')) {
					var item = $('.divUserSelected');
					if (item.length > 0) {
						if (find_mode == '@') {
							selectUser(item.attr('uid'));
						} else if (find_mode == '#') {
							selectGroup(item.attr('gid'));
						}
					}
					return false;
				}
			}
		});

		$(this).keyup(function(event) {
			var item = this;
			var word_len = $(item).val().length;
			var index = $(item).attr('index');
			$('#spnWeiboLength' + index).text(weibo_maxlen - word_len >= 0 ? weibo_maxlen - word_len : 0);
			if (word_len > weibo_maxlen) {
				$(item).val($(item).val().substring(0, weibo_maxlen));
				return false;
			}
			if ((event.keyCode >= 65 && event.keyCode <= 90) || event.keyCode == 32 || event.keyCode == 8 || event.keyCode == 37 || event.keyCode == 39 || (event.keyCode >= 48 && event.keyCode <= 57) || (event.shiftKey && event.keyCode == 50) || (event.shiftKey && event.keyCode == 51)) {
				if (event.keyCode == 51) {
					find_mode = "#";
				} else if (event.keyCode == 50) {
					find_mode = "@"
				}
				findSth(item);
			}

			if (event.keyCode == 38) {// 上按键
				if (!userDiv.is(':hidden')) {
					var it = $('.divUserSelected');
					if (it.length > 0) {
						it.removeClass('divUserSelected').css('background', 'transparent');
						it.prev('.divUserUnSelected').addClass('divUserSelected').css('background-color', '#ffffdd');
					}
				}
			}
			if (event.keyCode == 40) {// 下按键
				if (!userDiv.is(':hidden')) {
					var it = $('.divUserSelected');

					if (it.length > 0) {
						it.removeClass('divUserSelected').css('background', 'transparent');
						it.next('.divUserUnSelected').addClass('divUserSelected').css('background-color', '#ffffdd');
					} else {
						$('.divUserUnSelected').each(function() {
							it = $(this);
							return false;
						});
						if (it.length > 0) {
							it.addClass('divUserSelected').css('background-color', '#ffffdd');
						}
					}
				}
			}
		});

		$(this).click(function() {
			findSth(this);
		});

		return this;
	};
})(jQuery);