/**
 * 针对formbuilder使用的表单验证插件 2016-05-20 kong
 */
;
(function($) {
	var globalOptions = {
		validRules : [
				{
					name : 'required',
					validate : function(el) {
						var value = el.val();
						return ($.trim(value) == '');
					},
					defaultMsg : '请输入内容'
				},
				{
					name : 'needselect',
					validate : function(el) {
						var value = el.val();
						return ($.trim(value) == '');
					},
					defaultMsg : '请选择'
				},
				{
					name : 'number',
					validate : function(el) {
						var value = el.val();
						if (value == '') {
							return false;
						}
						return (!/^[0-9]\d*$/.test(value));
					},
					defaultMsg : '请输入数字'
				},
				{
					name : 'mail',
					validate : function(el) {
						var value = el.val();
						if (value == '') {
							return false;
						}
						return (!/^[_a-zA-Z0-9-]{1}([\._a-zA-Z0-9-]+)(\.[_a-zA-Z0-9-]+)*@[_a-zA-Z0-9-]+(\.[_a-zA-Z0-9-]+){1,3}$/
								.test(value));
					},
					defaultMsg : '请输入邮箱地址'
				},
				{
					name : 'char',
					validate : function(el) {
						var value = el.val();
						return (!/^[a-z\_\-A-Z]*$/.test(value));
					},
					defaultMsg : '请输入英文字符'
				},
				{
					name : 'chinese',
					validate : function(el) {
						var value = el.val();
						return (!/^[\u4e00-\u9fff]$/.test(value));
					},
					defaultMsg : '请输入汉字'
				},
				{
					name : 'dbindex',
					validate : function(el) {
						var value = el.val();
						return (!/^[\d]+,[\d]+$/.test(value));
					},
					defaultMsg : '格式不正确'
				},
				{
					name : 'smtp',
					validate : function(el) {
						var value = el.val();
						return (!/^smtp\.[a-zA-Z0-9]+(\.[a-zA-Z0-9]+){1,3}(\:[0-9]+){0,1}$/
								.test(value));
					},
					defaultMsg : '请输入正确的SMTP服务器地址'
				},
				{
					name : 'pop',
					validate : function(el) {
						var value = el.val();
						return (!/^pop\.[a-zA-Z0-9]+(\.[a-zA-Z0-9]+){1,3}(\:[0-9]+){0,1}$/
								.test(value));
					},
					defaultMsg : '请输入正确的POP服务器地址'
				},
				{
					name : 'imap',
					validate : function(el) {
						var value = el.val();
						return (!/^imap\.[a-zA-Z0-9]+(\.[a-zA-Z0-9]+){1,3}(\:[0-9]+){0,1}$/
								.test(value));
					},
					defaultMsg : '请输入正确的IMAP服务器地址'
				},
				{
					name : 'ip',
					validate : function(el) {
						var value = el.val();
						if (value != '') {
							return (!/^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$/
									.test(value));
						} else {
							return false;
						}
					},
					defaultMsg : '请输入正确的IP地址'
				},
				{
					name : 'maxNum',
					validate : function(el) {
						var value = el.val();
						return (!/^[0-9]\d*|(\-1)$/.test(value));
					},
					defaultMsg : '请输入-1或正数'
				},
				{
					name : 'PeriodOfTime',
					validate : function(el) {
						var value = el.val();
						return (!/^(?:[0-1]?[0-9]|2[0-3]):[0-5]?[0-9]-(?:[0-1]?[0-9]|2[0-3]):[0-5]?[0-9](?: +(?:[0-1]?[0-9]|2[0-3]):[0-5]?[0-9]-(?:[0-1]?[0-9]|2[0-3]):[0-5]?[0-9])*$/
								.test(value));
					},
					defaultMsg : '请输正确的时间段,格式为10:00-12:00,多个用空格隔开.'
				},
				{
					name : 'port',
					validate : function(el) {
						var value = el.val();
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
					defaultMsg : '请输入0至65535之间的整数'
				},
				{
					name : 'integer_p',
					validate : function(el) {
						var value = el.val();
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
					defaultMsg : '请输入正整数'
				},
				{
					name : 'integer',
					validate : function(el) {
						var value = el.val();
						if (isNaN(value))
							return true;
						if (parseInt(value) != value)
							return true;
						return false;
					},
					defaultMsg : '请输入整数'
				},
				{
					name : 'range',
					validate : function(el) {
						var value = el.val();
						if (value.indexOf('-') == -1) {
							return true;
						} else {
							var vs = value.split('-');
							if (vs.length != 2) {
								return true;
							}
							if (isNaN(parseFloat(vs[0]))
									|| isNaN(parseFloat(vs[1]))) {
								return true;
							}
							if (parseFloat(vs[0]) > parseFloat(vs[1])) {
								return true;
							}
						}
						return false;
					},
					defaultMsg : '请按照格式要求输入'
				},
				{
					name : 'stepindex',
					validate : function(el) {
						var value = el.val();
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
										if ((cc >= 65 && cc <= 90)
												|| (cc >= 97 && cc <= 122)) {

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
					defaultMsg : '请输入序号,格式范例：1或1.A或1.A.1'
				}, {
					name : 'enchar',
					validate : function(el) {
						var value = el.val();
						return (!/^[a-z\_\-A-Z\d\.\_]*$/.test(value));
					},
					defaultMsg : '请输入英文字符或数字'
				},{
					name : 'radio',
					validate : function(el){
						var name = el.attr("name");
						var chklen=$('input:radio[name="'+name+'"]:checked').length;
			            if(chklen<=0){
			                return true;
			            }
					},
					defaultMsg : '请选择'
				},{
					name : 'checkbox',
					validate : function(el){
						var name = el.attr("name");
						var chklen=$('input:checkbox[name="'+name+'"]:checked').length;
			            if(chklen<=0){
			                return true;
			            }
					},
					defaultMsg : '请选择'
				} ]
	};
	// input/textarea/time error
	var inputErrorPosition = function(el,fine_tune,name) {
		el.XPositionLeft = el.offset().left + el.width() - fine_tune;
		el.XPositionTop = el.offset().top;
		if(name == 'mselect'){
			el.XHeight = el.find('option').outerHeight(true)+9;
		}else{
			if(name == 'textarea'){
				el.XHeight = 34;
			}else{
				el.XHeight = el.outerHeight(true);
			}
			el.XPositionLineHeight = -5;
		}
		
	}
	// checkbox or checkbox error
	var radiocheckErrorPosition = function(el,fine_tune) {
		var elParent = el.parents('.grid-stack-item-content').find('label:last');
		el.XPositionTop = elParent.offset().top;
		el.XPositionLeft = elParent.offset().left+ elParent.width()+fine_tune;
		el.XHeight = elParent.outerHeight(true);
	}
	//  mradio or mcheckbox error
	var mradiocheckErrorPosition = function(el,fine_tune,className) {
		var elParent = el.parents('.grid-stack-item-content').find('label:last');
		el.XPositionTop = elParent.offset().top;
		el.XPositionLeft = elParent.offset().left+ elParent.width()+fine_tune;
		el.XHeight = el.innerHeight();
		el.XPositionLineHeight = -11;
	}

	var validateField = function(field) { // 验证字段
		var el = $(field), error = false, errorMsg = '';
		if (el.is(':hidden') && !el.hasClass('mustinput')) {// 无需验证
			return true;
		}
		var valid = '';
		if(typeof el.attr('x-check-type') != 'undefined'){
			valid = el.attr('x-check-type').split(' ');// valid 需要的所有验证方式
		}
		
	
		for (i = 0; i < valid.length; i++) {
			var x = true, flag = valid[i], msg = el.attr(flag + '-message'); // flag
																				// 需要的验证方式
			if (flag.substr(0, 1) == '!') {
				x = false;
				flag = flag.substr(1, flag.length - 1); // x
														// 如果有!(表示非这种验证方式，如!integer,表示非整数,这时x
														// = fasle)
			}

			var rules = globalOptions.validRules;
			for (j = 0; j < rules.length; j++) {
				var rule = rules[j];
				if (rule && flag == rule.name) {
					if (rule.validate.call(field, el) == x) {
						error = true;
						errorMsg = msg || rule.defaultMsg;
						break;
					}
				}
			}

			if (error) {
				break;// 验证到不通过的点
			}
		}
		elcontent = el.parents('.grid-stack-item-content');
		if (error) {
			var elParent = el.parents('.grid-stack-item');
			var elType = elParent.attr("controltype");
			var iserror = elcontent.data("error-validation");// 标识错误
			if (iserror || typeof iserror == "undefined") {
				switch(elType){
				case 'input_new':inputErrorPosition(el,10);break;
				case 'textarea_new':inputErrorPosition(el,5,'textarea');break;
				case 'time_new':inputErrorPosition(el,20);break;
				case 'select_new':inputErrorPosition(el,15,'select');break;
				case 'mselect_new':inputErrorPosition(el,15,'mselect');break;
				case 'radio_new':radiocheckErrorPosition(el,15);break;
				case 'mradio_new':mradiocheckErrorPosition(el,15,'.radio');break;
				case 'checkbox_new':radiocheckErrorPosition(el,15);break;
				case 'mcheckbox_new': mradiocheckErrorPosition(el,15,'.checkbox');break;
				}
				elcontent.addClass("has-error");// el														// 对应的label和文本框变红，表示错误
				elcontent.append('<span class="glyphicon glyphicon-question-sign form-control-feedback" style="pointer-events: auto;" aria-hidden="true"  data-toggle="tooltip" data-placement="left" title="Tooltip on left"></span>');
				var $tips = elcontent.find(".glyphicon");
				$tips.offset({
					left : el.XPositionLeft,
					top : el.XPositionTop
				}); // 设置X的位置
				$tips.height(el.XHeight); // 设置X的高度
				if(el.XPositionLineHeight !=0){
					var orag = $tips.css("line-height");
					$tips.css("line-height",parseInt(orag.replace("px","")) + el.XPositionLineHeight+"px");
					el.XPositionLineHeight = 0;
				}
				$tips.attr("title", errorMsg);
				$('[data-toggle="tooltip"]').tooltip();// 初始化提示框
				elcontent.data("error-validation", false);// 标识错误,true表示没错
			}
			
		} else {
			if (!iserror || typeof iserror == "undefined") {
				elcontent.removeClass("has-error");// el
				elcontent.find(".tooltip").remove();	//防止提示不消失														// 对应的label和文本框去掉变红样式
				elcontent.find(".glyphicon")
						.remove();
				elcontent.data("error-validation", true);// 标识错误,true表示没错
				
			}
		}
		return !error; // 有错返回false
	};
	// 失去焦点改变内容时判断是否符合规则
	$(function() {
		$(document).on('blur change', '[x-check-type]', function() {
			validateField(this);
		});
	});

	var getLastChildValid = function(type,struct,sender){
		var validationError = false;
		$('input[x-check-type]',sender).each(function(){
			
			if($(this).attr('type') == type){
				var name = $(this).attr('name');
				if(name){
					if(!struct[name]){
						struct[name] = new Array();
					}
					struct[name].push(this);
				}
			}
		});
		$.each(struct,function(){  //将每个数组的最后一个元素送去验证
			var el = $(this)[$(this).length-1];
			if (!validateField(el)){
				validationError = true;
				
			}
		});
		return validationError;;
	}
	$.fn.formvalid = function(options) {
		var validationError = false;
		this.each(function() {
			var checkboxs = {};
			var radios = {};
			var i= 0;
			validationCeckboxError = getLastChildValid("checkbox",checkboxs,this); //验证checkbox
			validationRadioError = getLastChildValid("radio",radios,this); //验证radio
			if(validationRadioError == true || validationCeckboxError == true){
				validationError = true;
			}
			
			
			$('input[type="text"],textarea[x-check-type], select[x-check-type]', //验证text、textarea、select
					this).each(function() {
				var el = $(this);
				if (!validateField(this)) {
					validationError = true;
				}
			});
			
		});
		return validationError ? false : true; // 没错返回true
	};
})(jQuery);