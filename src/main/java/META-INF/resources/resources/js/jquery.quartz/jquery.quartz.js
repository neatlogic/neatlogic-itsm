;
(function($) {
	var quartz = function(target, options) {
		this.$target = $(target);
		if (options) {
			this.option = options;
		} else {
			this.option = {};
		}
		this.$target.wrap('<div class="jquery-quartz-container"></div>');
		this.$container = this.$target.closest('.jquery-quartz-container');
		this.cronexpression = $(target).val();
		this.$currentdropdown = null;
		this.SECOND = null;
		this.MINUTE = null;
		this.HOUR = null;
		this.DAYOFMONTH = null;
		this.MONTH = null;
		this.DAYOFWEEK = null;
		this.YEAR = null;
		this.init();
	}

	this.quartz = quartz;
	quartz.prototype = {
		controllers : [ "minute", "hour", "dayofmonth", "dayofweek", "month", "year" ],
		seconds : [ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
				"40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" ],
		minutes : [ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
				"40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" ],
		hours : [ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" ],
		dayofmonths : [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" ],
		months : [ "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" ],
		dayofweeks : [ "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" ],
		dict : {
			"minute" : "分钟",
			"hour" : "小时",
			"dayofmonth" : "天",
			"dayofweek" : "星期",
			"month" : "月",
			"year" : "年",
			"JAN" : "一月",
			"FEB" : "二月",
			"MAR" : "三月",
			"APR" : "四月",
			"MAY" : "五月",
			"JUN" : "六月",
			"JUL" : "七月",
			"AUG" : "八月",
			"SEP" : "九月",
			"OCT" : "十月",
			"NOV" : "十一月",
			"DEC" : "十二月",
			"SUN" : "星期日",
			"MON" : "星期一",
			"TUE" : "星期二",
			"WED" : "星期三",
			"THU" : "星期四",
			"FRI" : "星期五",
			"SAT" : "星期六",
			"*" : "所有",
			"?" : "不指定"
		},
		controllerList : {
			minute : [ {
				type : 'label',
				text : '每'
			}, {
				type : 'controller',
				get : 'type',
				datasource : 'controllers',
				width : 'auto'
			} ],
			hour : [ {
				type : 'label',
				text : '每'
			}, {
				type : 'controller',
				get : 'type',
				datasource : 'controllers',
				width : 'auto'
			}, {
				type : 'label',
				text : ':'
			}, {
				type : 'controller',
				get : 'value',
				datasource : 'minutes',
				target : 'MINUTE',
				width : 380
			}, {
				type : 'label',
				text : '分'
			} ],
			dayofmonth : [ {
				type : 'label',
				text : '每'
			}, {
				type : 'controller',
				get : 'type',
				datasource : 'controllers',
				width : 'auto'
			}, {
				type : 'label',
				text : ''
			}, {
				type : 'controller',
				get : 'value',
				datasource : 'hours',
				target : 'HOUR',
				width : 200
			}, {
				type : 'label',
				text : '时'
			}, {
				type : 'controller',
				get : 'value',
				datasource : 'minutes',
				target : 'MINUTE',
				width : 380
			}, {
				type : 'label',
				text : '分'
			} ],
			dayofweek : [ {
				type : 'label',
				text : '每'
			}, {
				type : 'controller',
				get : 'type',
				datasource : 'controllers',
				width : 'auto'
			}, {
				type : 'label',
				text : ''
			}, {
				type : 'controller',
				get : 'value',
				datasource : 'dayofweeks',
				target : 'DAYOFWEEK',
				width : 65
			}, {
				type : 'label',
				text : ''
			}, {
				type : 'controller',
				get : 'value',
				datasource : 'hours',
				target : 'HOUR',
				width : 200
			}, {
				type : 'label',
				text : '时'
			}, {
				type : 'controller',
				get : 'value',
				datasource : 'minutes',
				target : 'MINUTE',
				width : 380
			}, {
				type : 'label',
				text : '分'
			} ],
			month : [ {
				type : 'label',
				text : '每'
			}, {
				type : 'controller',
				get : 'type',
				datasource : 'controllers',
				width : 'auto'
			}, {
				type : 'label',
				text : ''
			}, {
				type : 'controller',
				get : 'value',
				datasource : 'dayofmonths',
				target : 'DAYOFMONTH',
				width : 200
			}, {
				type : 'label',
				text : '日'
			}, {
				type : 'controller',
				get : 'value',
				datasource : 'hours',
				target : 'HOUR',
				width : 200
			}, {
				type : 'label',
				text : '时'
			}, {
				type : 'controller',
				get : 'value',
				datasource : 'minutes',
				target : 'MINUTE',
				width : 380
			}, {
				type : 'label',
				text : '分'
			} ],
			year : [ {
				type : 'label',
				text : '每'
			}, {
				type : 'controller',
				get : 'type',
				datasource : 'controllers',
				width : 'auto'
			}, {
				type : 'label',
				text : ''
			}, {
				type : 'controller',
				get : 'value',
				datasource : 'months',
				target : 'MONTH',
				width : 65
			}, {
				type : 'label',
				text : ''
			}, {
				type : 'controller',
				get : 'value',
				datasource : 'dayofmonths',
				target : 'DAYOFMONTH',
				width : 200
			}, {
				type : 'label',
				text : '日'
			}, {
				type : 'controller',
				get : 'value',
				datasource : 'hours',
				target : 'HOUR',
				width : 200
			}, {
				type : 'label',
				text : '时'
			}, {
				type : 'controller',
				get : 'value',
				datasource : 'minutes',
				target : 'MINUTE',
				width : 380
			}, {
				type : 'label',
				text : '分'
			} ]
		},
		init : function() {
			var that = this;
			var times = this.cronexpression.split(' ');
			if (times[0] && that.validField('second', times[0])) {
				this.SECOND = times[0];
			} else {
				this.SECOND = 0;
			}

			if (times[0] && that.validField('minute', times[1])) {
				this.MINUTE = times[1];
			} else {
				this.MINUTE = '*';
			}

			if (times[0] && that.validField('hour', times[2])) {
				this.HOUR = times[2];
			} else {
				this.HOUR = '*';
			}

			if (times[0] && that.validField('dayofmonth', times[3])) {
				this.DAYOFMONTH = times[3];
			} else {
				this.DAYOFMONTH = '*';
			}

			if (times[0] && that.validField('month', times[4])) {
				this.MONTH = times[4];
			} else {
				this.MONTH = '*';
			}

			if (times[0] && that.validField('dayofweek', times[5])) {
				this.DAYOFWEEK = times[5];
			} else {
				if (this.MONTH == '?') {
					this.DAYOFWEEK = '*';
				} else {
					this.DAYOFWEEK = "?";
				}
			}

			if (times[0] && that.validField('year', times[6])) {
				this.YEAR = times[6];
			} else {
				this.YEAR = '*';
			}

			this.generateController(this.getFitType());
			$(document).on('click', function() {
				if (that.isEntered && !that.isEnter && that.$currentdropdown) {
					that.$currentdropdown.slideUp('fast');
					that.$currentdropdown = null;
				}
			});
		},
		getFitType : function() {
			var that = this;
			if (this.MONTH != '*') {
				return 'year';
			} else if (this.DAYOFMONTH != '*' && this.DAYOFMONTH != '?') {
				return 'month';
			} else if (this.DAYOFWEEK != '*' && this.DAYOFWEEK != '?') {
				return 'dayofweek';
			} else if (this.HOUR != '*') {
				return 'dayofmonth';
			} else if (this.MINUTE != '*') {
				return 'hour';
			} else {
				return 'minute';
			}
		},
		checkIsSelected : function(field, value) {
			var currentValue = this[field];

			if (currentValue) {
				var checkValueList = new Array();
				if (currentValue.indexOf(',') > -1 || currentValue.indexOf('-') > -1) {
					var vs = new Array();
					if (currentValue.indexOf(',') > -1) {
						vs = currentValue.split(',');
					} else {
						vs.push(currentValue);
					}
					for (var i = 0; i < vs.length; i++) {
						if (vs[i].indexOf('-') > -1) {
							var s = parseInt(vs[i].split('-')[0], 10);
							var e = parseInt(vs[i].split('-')[1], 10);
							for (var d = s; d <= e; d++) {
								checkValueList.push(d);
							}
						} else {
							checkValueList.push(vs[i]);
						}
					}
				} else {
					checkValueList.push(currentValue);
				}

				for (var d = 0; d < checkValueList.length; d++) {
					if (checkValueList[d] == value) {

						return true;
					}
				}
			}
			return false;
		},
		validField : function(field, value) {
			if (this[field + "s"]) {
				var checkValueList = new Array();
				if (value && (value.indexOf(',') > -1 || value.indexOf('-') > -1)) {
					var vs = new Array();
					if (value.indexOf(',') > -1) {
						vs = value.split(',');
					} else {
						vs.push(value);
					}
					for (var i = 0; i < vs.length; i++) {
						if (vs[i].indexOf('-') > -1) {
							var s = parseInt(vs[i].split('-')[0], 10);
							var e = parseInt(vs[i].split('-')[1], 10);
							checkValueList.push(s);
							checkValueList.push(e);
						} else {
							checkValueList.push(vs[i]);
						}
					}
				} else {
					checkValueList.push(value);
				}
				for (var i = 0; i < this[field + "s"].length; i++) {
					for (var d = 0; d < checkValueList.length; d++) {
						if (checkValueList[d] == this[field + "s"][i]) {
							return true;
						}
					}
				}
			}
			return false;
		},
		translate : function(text) {
			text = text.toString();
			var finaltext = this.dict[text] || text;
			if (text.indexOf(',') > -1) {
				var texts = text.split(',');
				for (var t = 0; t < texts.length; t++) {
					finaltext = finaltext.replace(texts[t], this.dict[texts[t]] || texts[t]);
				}
			}
			if (text.indexOf('-') > -1) {
				var texts = text.split('-');
				for (var t = 0; t < texts.length; t++) {
					finaltext = finaltext.replace(texts[t], this.dict[texts[t]] || texts[t]);
				}
			}
			return finaltext;
		},
		setValue : function(type) {
			var that = this;
			if (type == 'minute') {
				that.MINUTE = '*';
				that.HOUR = '*';
				that.DAYOFMONTH = '*';
				that.MONTH = '*';
				that.DAYOFWEEK = '?';
				that.YEAR = '*';
			} else if (type == 'hour') {
				that.MINUTE = that.$container.find('.MINUTE').data('value');
				that.HOUR = '*';
				that.DAYOFMONTH = '*';
				that.MONTH = '*';
				that.DAYOFWEEK = '?';
				that.YEAR = '*';
			} else if (type == 'dayofmonth') {
				that.MINUTE = that.$container.find('.MINUTE').data('value');
				that.HOUR = that.$container.find('.HOUR').data('value');
				that.DAYOFMONTH = '*';
				that.MONTH = '*';
				that.DAYOFWEEK = '?';
				that.YEAR = '*';
			} else if (type == 'dayofweek') {
				that.MINUTE = that.$container.find('.MINUTE').data('value');
				that.HOUR = that.$container.find('.HOUR').data('value');
				that.DAYOFMONTH = '?';
				that.MONTH = '*';
				that.DAYOFWEEK = that.$container.find('.DAYOFWEEK').data('value') == "?" ? "*" : that.$container.find('.DAYOFWEEK').data('value');
				that.YEAR = '*';
			} else if (type == 'month') {
				that.MINUTE = that.$container.find('.MINUTE').data('value');
				that.HOUR = that.$container.find('.HOUR').data('value');
				that.DAYOFMONTH = that.$container.find('.DAYOFMONTH').data('value') == "?" ? "*" : that.$container.find('.DAYOFMONTH').data('value');
				that.MONTH = "*";
				that.DAYOFWEEK = '?';
				that.YEAR = '*';
			} else if (type == 'year') {
				that.MINUTE = that.$container.find('.MINUTE').data('value');
				that.HOUR = that.$container.find('.HOUR').data('value');
				that.DAYOFMONTH = that.$container.find('.DAYOFMONTH').data('value');
				that.MONTH = that.$container.find('.MONTH').data('value');
				that.DAYOFWEEK = '?';
				that.YEAR = "*";
			}
			that.$target.val(that.SECOND + ' ' + that.MINUTE + ' ' + that.HOUR + ' ' + that.DAYOFMONTH + ' ' + that.MONTH + ' ' + that.DAYOFWEEK + ' ' + that.YEAR);
		},
		getValue : function() {
			alert('getValue');
		},
		toggleDropdownlist : function() {
			alert('download');
		},
		formatValue : function(value) {
			if (value.indexOf(',') == -1) {
				return value;
			}
			var vs = value.split(',');
			for (var i = 0; i < vs.length; i++) {
				if (isNaN(parseInt(vs[i], 10))) {
					return value;
				}
			}
			var newvalue = '';
			var max = 0;

			vs.sort(function(a, b) {
				if (!isNaN(parseInt(a, 10)) && !isNaN(parseInt(b, 10))) {
					return parseInt(a, 10) - parseInt(b, 10);
				} else {
					return 0;
				}
			});

			for (var i = 0; i < vs.length; i++) {
				var cur = parseInt(vs[i], 10);
				if (newvalue == '') {
					newvalue += cur;
				} else {
					if (cur - max == 1) {
						if (newvalue.substring(newvalue.length - 1) != '-') {
							newvalue += '-';
						}
					} else {
						if (newvalue.substring(newvalue.length - 1) == '-' && i > 1) {
							newvalue += parseInt(vs[i - 1], 10);
						}
						newvalue += ',' + cur;
					}
				}
				max = cur;
			}
			if (newvalue.substring(newvalue.length - 1) == '-') {
				newvalue += cur;
			}
			return newvalue;
		},
		generateController : function(type) {
			var that = this;
			if (!type) {
				type = 'minute';
			}
			var json = that.controllerList[type];
			that.$container.find('.dynamic-item').remove();
			for (var i = 0; i < json.length; i++) {
				var conf = json[i];
				if(!that.option.readonly){
					if (conf.type == 'label') {
						that.$container.append('<label class="jquery-quartz-controller-label dynamic-item">' + conf.text + '</label>');
					} else if (conf.type == 'controller') {
						var c = $('<div class="jquery-quartz-controller-container dynamic-item"></div>');
						var ct = $('<div class="jquery-quartz-controller-text"></div>');
						if (conf.target) {
							ct.text(that.translate(that[conf.target]) || '-所有-');
							ct.data('value', that[conf.target]);
							ct.addClass(conf.target);
						}
						if (conf['get'] == 'type') {
							ct.text(that.translate(type));
						}
						var dropdown = $('<div class="jquery-quartz-dropdown-container"></div>');
						if (!that.option.direction || that.option.direction == 'down') {
							dropdown.css({
								'top' : '30px'
							});
						} else {
							dropdown.css({
								'bottom' : '30px'
							});
						}
						dropdown.data('target', ct);
						dropdown.width(conf.width);
						c.data('dropdownlist', dropdown);
						c.on('click', function() {
							that.isEntered = false;
							if ($(this).data('dropdownlist')) {
								if ($(this).data('dropdownlist').is(':visible')) {
									$(this).data('dropdownlist').slideUp('fast');
									that.$currentdropdown = null;
								} else {
									if (that.$currentdropdown) {
										that.$currentdropdown.slideUp('fast');
									}
									$(this).data('dropdownlist').slideDown('fast');
									that.$currentdropdown = $(this).data('dropdownlist');
									that.$currentdropdown.on('mouseenter', function() {
										that.isEnter = true;
										that.isEntered = true;
									});

									that.$currentdropdown.on('mouseleave', function() {
										that.isEnter = false;
									});
								}
							}
						});
						if (that[conf.datasource]) {
							for (var d = 0; d < that[conf.datasource].length; d++) {
								var item = $('<div class="jquery-quartz-dropdown-item" data-value="' + that[conf.datasource][d] + '">' + that.translate(that[conf.datasource][d]) + '</div>');
								if (conf['get'] == 'type') {
									if (type == that[conf.datasource][d]) {
										item.addClass('selected');
									}
									item.on('click', function() {
										that.generateController($(this).data('value'));
									});
								} else if (conf['get'] == 'value') {
									if (that.checkIsSelected(conf.target, that[conf.datasource][d])) {
										item.addClass('selected');
									}
									item.on('click', function(e) {
										e.stopPropagation();
										if (!$(this).hasClass('selected')) {
											$(this).addClass('selected');
										} else {
											$(this).removeClass('selected');
										}
										var targettext = $(this).closest('.jquery-quartz-dropdown-container').data('target');
										var text = '';
										var val = '';
										$(this).closest('.jquery-quartz-dropdown-container').find('.jquery-quartz-dropdown-item.selected').each(function() {
											if (text) {
												text += ',';
												val += ',';
											}
											text += that.translate($(this).data('value'));
											val += $(this).data('value');
										});
										if (text != '') {
											targettext.text(that.formatValue(text));
											targettext.data('value', that.formatValue(val));
										} else {
											targettext.text('-所有-');
											targettext.data('value', '*');
										}
										that.setValue(type);
									});
								}
								dropdown.append(item);
							}
						}
						c.append(ct).append(dropdown);
						that.$container.append(c);
					}					
				}else{

					if (conf.type == 'label') {
						that.$container.append(conf.text);
					} else if (conf.type == 'controller') {
						var text='';
						var ct = '';
						if (conf.target) {
							text += that.translate(that[conf.target])? that.translate(that[conf.target]) :'-所有-';
						}
						if (conf['get'] == 'type') {
							text +=that.translate(type);
						}
						that.$container.append(text);
					}						
					
				}

			}
			if(!that.option.readonly){
				that.setValue(type);
			}
		}
	};

	$.fn.quartz = function(options) {
		var $target = $(this);
		if (!$target.data('bind') && $target.attr('type').toLowerCase() == 'hidden') {
			new quartz($target, options);
		}
		return this;
	};

	$(function() {

	});

})(jQuery);