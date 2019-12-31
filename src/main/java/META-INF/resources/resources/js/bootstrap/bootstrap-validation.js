;
(function($) {
	var globalOptions = {
		validRules : [
				{
					name : 'required',
					validate : function(value) {
						return ($.trim(value) == '');
					},
					defaultMsg : '${tk:lang("请输入内容")}'
				},
				{
					name : 'number',
					validate : function(value) {
						if (value == '') {
							return false;
						}
						return (!/^[0-9]\d*$/.test(value));
					},
					defaultMsg : '${tk:lang("请输入数字")}'
				},
				{
					name : 'mail',
					validate : function(value) {
						if (value == '') {
							return false;
						}
						return (!/^[_a-zA-Z0-9-]{1}([\._a-zA-Z0-9-]+)(\.[_a-zA-Z0-9-]+)*@[_a-zA-Z0-9-]+(\.[_a-zA-Z0-9-]+){1,3}$/.test(value));
					},
					defaultMsg : '${tk:lang("请输入邮箱地址")}'
				},
				{
					name : 'char',
					validate : function(value) {
						return (!/^[a-z\_\-A-Z]*$/.test(value));
					},
					defaultMsg : '${tk:lang("请输入英文字符")}'
				},
				{
					name : 'chinese',
					validate : function(value) {
						return (!/^[\u4e00-\u9fff]$/.test(value));
					},
					defaultMsg : '${tk:lang("请输入汉字")}'
				},
				{
					name : 'dbindex',
					validate : function(value) {
						return (!/^[\d]+,[\d]+$/.test(value));
					},
					defaultMsg : '${tk:lang("格式不正确")}'
				},
				{
					name : 'smtp',
					validate : function(value) {
						return (!/^smtp\.[a-zA-Z0-9]+(\.[a-zA-Z0-9]+){1,3}(\:[0-9]+){0,1}$/.test(value));
					},
					defaultMsg : '${tk:lang("请输入正确的SMTP服务器地址")}'
				},
				{
					name : 'pop',
					validate : function(value) {
						return (!/^pop\.[a-zA-Z0-9]+(\.[a-zA-Z0-9]+){1,3}(\:[0-9]+){0,1}$/.test(value));
					},
					defaultMsg : '${tk:lang("请输入正确的POP服务器地址")}'
				},
				{
					name : 'imap',
					validate : function(value) {
						return (!/^imap\.[a-zA-Z0-9]+(\.[a-zA-Z0-9]+){1,3}(\:[0-9]+){0,1}$/.test(value));
					},
					defaultMsg : '${tk:lang("请输入正确的IMAP服务器地址")}'
				},
				{
					name : 'ip',
					validate : function(value) {
						if (value != '') {
							return (!/^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$/
									.test(value));
						} else {
							return false;
						}
					},
					defaultMsg : '${tk:lang("请输入正确的IP地址")}'
				},{
					name : 'cidr',
					validate : function(value) {
						if (value != '' && value != '*') {
								return (!/^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])\/(\d{1}|[0-2]{1}\d{1}|3[0-2])$/
										.test(value));
						} else {
							return false;
						}
					},
					defaultMsg : '${tk:lang("请输入正确的CIDR地址")}'
				}, {
					name : 'url',
					validate : function(value) {
						if (value != '') {
							return (!/^((https|http|ftp|rtsp|mms)?:\/\/)[^\s]+$/.test(value));
						} else {
							return false;
						}
					},
					defaultMsg : '${tk:lang("请输入正确的URL地址")}'
				}, {
					name : 'maxNum',
					validate : function(value) {
						return (!/^[0-9]\d*|(\-1)$/.test(value));
					},
					defaultMsg : '${tk:lang("请输入-1或正数")}'
				}, {
					name : 'PeriodOfTime',
					validate : function(value) {
						return (!/^(?:[0-1]?[0-9]|2[0-3]):[0-5]?[0-9]-(?:[0-1]?[0-9]|2[0-3]):[0-5]?[0-9](?: +(?:[0-1]?[0-9]|2[0-3]):[0-5]?[0-9]-(?:[0-1]?[0-9]|2[0-3]):[0-5]?[0-9])*$/.test(value));
					},
					defaultMsg : '${tk:lang("请输正确的时间段,格式为10:00-12:00,多个用空格隔开.")}'
				}, {
					name : 'exclude',
					validate : function(value) {
						if (value == '') {
							return false;
						}
						if (/^(((\d)(-\d)?(,)?)*)$/.test(value)) {
							var flag = true;
							var arr = new Array();
							arr = value.trim().split(/-|,/);
							for (var i = 0; i < arr.length; i++) {
								if (arr[i] < 0 || arr[i] > 255 || arr[i] > arr[i + 1]) {
									flag = false;
								}
							}
							if (flag) {
								return false;
							}
						}
						return true;
					},
					defaultMsg : '${tk:lang("请输入正确的保留地址。")}'
				}, {
					name : 'port',
					validate : function(value) {
						if (value == '') {
							return false;
						}
						if (isNaN(value)) {
							return true;
						}

						if (parseInt(value, 10) != value) {
							return true;
						}

						if (parseInt(value, 10) < 0 || parseInt(value) > 65535) {
							return true;
						}
						return false;
					},
					defaultMsg : '${tk:lang("请输入0至65535之间的整数")}'
				}, {
					name : 'mask',
					validate : function(value) {
						if (value == '') {
							return false;
						}
						if (isNaN(value)) {
							return true;
						}

						if (parseInt(value, 10) != value) {
							return true;
						}

						if (parseInt(value, 10) < 8 || parseInt(value) > 32) {
							return true;
						}
						return false;
					},
					defaultMsg : '${tk:lang("请输入正确的掩码")}'
				}, {
					name : 'integer_p',
					validate : function(value) {
						if (value == '')
							return false;
						if (isNaN(value))
							return true;
						if (parseInt(value) != value)
							return true;
						if (parseInt(value) <= 0)
							return true;
						return false;
					},
					defaultMsg : '${tk:lang("请输入正整数")}'
				}, {
					name : 'integer',
					validate : function(value) {
						if (isNaN(value))
							return true;
						if (parseInt(value) != value)
							return true;
						return false;
					},
					defaultMsg : '${tk:lang("请输入整数")}'
				}, {
					name : 'range',
					validate : function(value) {
						if (value.indexOf('-') == -1) {
							return true;
						} else {
							var vs = value.split('-');
							if (vs.length != 2) {
								return true;
							}
							if (isNaN(parseFloat(vs[0])) || isNaN(parseFloat(vs[1]))) {
								return true;
							}
							if (parseFloat(vs[0]) > parseFloat(vs[1])) {
								return true;
							}
						}
						return false;
					},
					defaultMsg : '${tk:lang("请按照格式要求输入")}'
				}, {
					name : 'stepindex',
					validate : function(value) {
						value = $.trim(value);
						if (value != '') {
							if (value.indexOf('.') > -1) {
								var vl = value.split('.');
								for ( var v in vl) {
									if (v % 2 == 0) {
										if (isNaN(parseInt(vl[v], 10))) {
											return true;
										}
									} else {
										if (vl[v].length != 1) {
											return true;
										}
										var cc = vl[v].charCodeAt();
										if ((cc >= 65 && cc <= 90) || (cc >= 97 && cc <= 122)) {

										} else {
											return true;
										}
									}
								}
							} else {
								if (isNaN(parseInt(value, 10))) {
									return true;
								}
							}
						} else {
							return false;
						}
					},
					defaultMsg : '${tk:lang("请输入序号,格式范例：1或1.A或1.A.1")}'
				}, {
					name : 'enchar',
					validate : function(value) {
						return (!/^[a-z\_\-A-Z\d\.\_]*$/.test(value));
					},
					defaultMsg : '${tk:lang("请输入英文字符或数字")}'
				}, {
					name : 'enchar_space',
					validate : function(value) {
						return (!/^[a-z\_\-A-Z\d\.\ \_]*$/.test(value));
					},
					defaultMsg : '${tk:lang("请输入英文字符或数字,允许空格")}'
				}, {
					name : 'parameter',
					validate : function(value) {
						return (!/^[a-zA-Z_][a-zA-Z0-9_\.]*$/.test(value));
					},
					defaultMsg : '${tk:lang("变量名只能以字母、数字、下划线和.组成，且开头不能是数字")}'
				}, {
					name : 'month',
					validate : function(value) {
						if (value == '') {
							return true;
						}
						if (isNaN(value)) {
							return true;
						}
						if (parseInt(value) < 1 || parseInt(value) > 12) {
							return true;
						}
						return false;
					},
					defaultMsg : '${tk:lang("请输入1至12之间的整数")}'
				}, {
					name : 'check',
					validate : function(value) {
						if (!value) {
							return true;
						}
						return false;
					},
					defaultMsg : '${tk:lang("请选择")}'
				}, {
					name : 'non-special',
					validate : function(value) {
						if (value) {
							return (/[<>"']/.test(value));
						}
					},
					defaultMsg : '${tk:lang("请不要输入&lt;&gt;&quot;&apos;等特殊符号")}'
				}, {
					name : 'passcode',
					validate : function(value) {
						return (!/(?!.*[\u4E00-\u9FA5\s])(?!^[a-zA-Z]+$)(?!^[\d]+$)(?!^[^a-zA-Z\d]+$)^.{8,20}$/.test(value));
					},
					defaultMsg : '${tk:lang("请输入长度在8~20之间的字符串，至少有字母、数字、特殊字符其中2种组合")}'
				}, {
					name : 'regex',
					validate : function(value,reg) {
						if ($.trim(value)) {
							reg =new RegExp(reg);
							return (!reg.test(value));	
						}
					},
					defaultMsg : '${tk:lang("请输入正确格式的字符串")}'
				}]
	};
	$(function() {
		$(document).on('focus change input', '[check-type]', function() {
			validateField(this);
		});
	});

	var validateField = function(field) { // 验证字段
		var el = $(field), error = false, errorMsg = '';
		if ((el.is(':hidden') && !el.hasClass('mustinput')) || el.prop('disabled')) {// 无需验证
			return true;
		}
		var valid = el.attr('check-type').split(' ');
		var placement = el.attr('check-placement');
		if(field.type =='radio' || field.type =='checkbox'){
			for (i = 0; i < valid.length; i++) {
				var x = true, flag = valid[i], msg = el.attr(flag + '-message');
				if (flag.substr(0, 1) == '!') {
					x = false;
					flag = flag.substr(1, flag.length - 1);
				}
				
				var rules = globalOptions.validRules;
				for (j = 0; j < rules.length; j++) {
					var rule = rules[j];
					if (rule && flag == rule.name) {
						if (rule.validate.call($("[name='"+field.name+"']:last")[0], $("[name='"+field.name+"']:checked").val()) == x) {
							error = true;
							errorMsg = msg || rule.defaultMsg;
							break;
						}
					}
				}
				if (error) {
					break;
				}
			}	
			el= $("[name='"+field.name+"']:last");
		}else{
			for (i = 0; i < valid.length; i++) {
				var x = true, flag = valid[i], msg = el.attr(flag + '-message');
				if (flag.substr(0, 1) == '!') {
					x = false;
					flag = flag.substr(1, flag.length - 1);
				}
				var rules = globalOptions.validRules;
				for (j = 0; j < rules.length; j++) {
					var rule = rules[j];
					if (rule && flag == rule.name) {
						if (rule.validate.call(field, el.val(),el.attr(flag + '-rule')? el.attr(flag + '-rule'):'') == x) {
							error = true;
							errorMsg = msg || rule.defaultMsg;
							break;
						}
					}
				}

				if (error) {
					break;
				}
			}			
		}

		if (error) {
			el.addClass('onerror');
			if (el.data('error-tooltip')) {
				el.data('error-tooltip').remove();
			}
			var trickel = $('<span class="trickel noneed" style="vertical-align:middle;display:inline-block;position:relative"></span>');
			trickel.append('<div class="tooltip in" style="position:absolute;display: block;"><div class="tooltip-arrow"></div><div style="white-space:nowrap;max-width: none;" class="tooltip-inner">' + errorMsg
					+ '</div></div>');
			var tooltipWidth = 100;
			if (el[0].checkselect) {
				el.parent().after(trickel);
			} else if (el.attr('data-makeup')){
				el.parent().parent().after(trickel);
			} else if (el.data('bind') || el.attr('bind') || el.attr('isbind')) {
				el.parent().after(trickel);
			} else {
				el.after(trickel);
			}
			el.data('error-tooltip', trickel);
			var tooltip = trickel.find('.tooltip');
			if (!placement) {
				placement = 'right';
				if ($('body').width() - trickel.position().left - tooltip.outerWidth() < 0) {
					placement = 'top';
				}
			}
			tooltip.addClass(placement);
			if (placement == 'right') {
				tooltip.css({
					'top' : (-tooltip.outerHeight() / 2) + 'px',
					'left' : '0px'
				});
			} else if (placement == 'top') {
				tooltip.css({
					'top' : (-tooltip.outerHeight()) + 'px',
					'left' : (-tooltip.outerWidth() / 2) + 'px'
				});
			} else if (placement == 'bottom') {
				tooltip.css({
					'top' : '0px',
					'left' : (-tooltip.outerWidth() / 2) + 'px'
				});
			} else if (placement == 'bottom-right') {
				tooltip.css({
					'top' : '0px',
					'left' : '0px'
				});
			} else if (placement == 'top-right') {
				tooltip.css({
					'top' : (-tooltip.outerHeight()) + 'px',
					'left' : '0px'
				});
			} else if (placement == 'bottom-left') {
				tooltip.css({
					'top' : '0px',
					'left' : (-tooltip.outerWidth()) + 'px'
				});
			} else if (placement == 'top-left') {
				tooltip.css({
					'top' : (-tooltip.outerHeight()) + 'px',
					'left' : (-tooltip.outerWidth()) + 'px'
				});
			} else if (placement == 'left') {
				tooltip.css({
					'top' : (-tooltip.outerHeight() / 2) + 'px',
					'left' : (-tooltip.outerWidth()) + 'px'
				});
			}
			if(el.data('require-position') == true){
				window.scrollTo(0,el.offset().top); 
			}
		} else {
			el.removeClass('onerror');
			if (el.data('error-tooltip')) {
				el.data('error-tooltip').remove();
			}
		}
		return !error;
	};

	$.fn.valid = function(options) {
		var validationError = false;
		this.each(function() {
			$('input[check-type], textarea[check-type], select[check-type]', this).each(function() {
				if (!validateField(this)) {
					validationError = true;
				}
			});
			if($('.trickel.noneed',this).length>0){
				if($(this).parents('.modal-body').length>0){
					$(this).parents('.modal-body').scrollTop($('.trickel.noneed').eq(0).position().top-70); 
				}else if( $(this).parents('.slidedialog_body').length>0 ){
					$(this).parents('.slidedialog_body').scrollTop($('.trickel.noneed').eq(0).position().top-70); 
				}else{
					window.scrollTo(0,$('.trickel.noneed').eq(0).offset().top-30); 
				}
			}
		});
		return validationError ? false : true;
	};

})(jQuery);