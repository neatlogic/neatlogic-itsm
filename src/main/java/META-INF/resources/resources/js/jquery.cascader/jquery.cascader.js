;
(function($) {
	var defaultoptions = {
		targetLevel : 'all',
		width : 210,
		height : 32,
		onChange : function() {

		},
		type:0,// 0:返回结果集是array 1：返回结果集 包了一层 如自定义属性集 Data:[]
		level : [ {
			url : '',
			searchable : true,
			text : '',
			value : '',
			valueKey : '',
			textKey : ''
		} ]
	};

	var cascader = function(target, options) {
		var that = this;
		this.ison = false;
		this.config = $.extend(true, {}, defaultoptions, options);
		this.$target = $(target);
		this.$container = $('<div class="jquery-cascader-container"></div>');
		this.$container.append('<table><tr></tr></table>');
		this.$inputer = $('<div class="jquery-cascader-inputer"><span></span></div>');
		this.$closebtn = $('<i class="fa fa-remove jquery-cascader-close"></i>');
		this.$icon = $('<i class="fa fa-caret-down jquery-cascader-icon"></i>');
		this.$inputer.append(this.$closebtn);
		this.$inputer.append(this.$icon);
		this.$closebtn.hide();
		this.$closebtn.on('click', function(e) {
			e.stopPropagation();
			$(this).hide();
			that.$inputer.find('span').empty();
			that.$inputer.find('span').attr('title', '');
			that.$target.children().remove();
			if (that.config.level && that.config.level.length > 0) {
				for (var level = 0; level < that.config.level.length; level++) {
					that.config.level[level].value = null;
					that.config.level[level].text = null;
				}
			}
			if (that.$container.is(':visible')) {
				that.$container.slideUp('fast');
				that.ison = false;
			}
		});
		this.$closeicon = $('<div></div>');
		this.$tr = this.$container.find('tr');
		this.$target.addClass('jquery-cascader');
		this.$target.hide();
		this.$target.wrap('<div class="jquery-cascader-wraper"></div>');
		this.$container.css('display', 'none');
		this.$wraper = this.$target.closest('.jquery-cascader-wraper');
		if (parseFloat(this.config.width) == this.config.width) {
			this.$wraper.css('width', this.config.width + 'px');
		} else {
			this.$wraper.css('width', this.config.width);
		}
		if (parseFloat(this.config.height) == this.config.height) {
			this.$wraper.css('height', this.config.height + 'px');
		} else {
			this.$wraper.css('height', this.config.height);
		}
		this.$wraper.append(this.$inputer);

		this.$wraper.append(this.$container);
		this.valueList = new Array();
		var that = this;
		this.$inputer.on('click', function() {
			if (!that.$container.is(':visible')) {
				if (that.config.level && that.config.level[0]) {
					that.getLevelContent(0);
					that.$container.slideDown('fast');
					that.ison = true;
				}
			} else {
				that.$container.slideUp('fast');
				that.ison = false;
			}
		});
		this.$container.on('mouseenter', function() {
			that.ison = true;
		});
		this.$container.on('mouseleave', function() {
			that.ison = false;
		});
		$(document).on('click', function() {
			if (that.ison == false && that.$container.is(':visible')) {
				that.$container.slideUp('fast');
				that.ison = false;
			}
		});
		this.initValue();
	}

	this.cascader = cascader;

	cascader.prototype = {
		initValue : function() {
			var that = this;
			if (that.config.level && that.config.level.length > 0) {
				var sltValue = '';
				var sltText = '';
				var spnText = '';
				for (var l = 0; l < that.config.level.length; l++) {
					var level = that.config.level[l];
					var valueObj = {};
					if (level.text) {
						if (spnText != '') {
							spnText += ' / ';
						}
						spnText += level.text;
						sltText = level.text;
						valueObj.text = level.text;
					}
					if (level.value) {
						sltValue = level.value;
						valueObj.value = level.value;
					}
					that.valueList[l] = valueObj;
				}
				if(spnText){
					this.$inputer.find('span').text(spnText).attr('title', spnText);
				}
				if (sltValue && sltText) {
					this.$target.children().remove();
					if(!Array.isArray(sltValue)){
						this.$target.append('<option value="' + sltValue + '" selected>' + sltText + '</option>');
					}else{
						for(var i = 0; i < sltValue.length; i++){
							this.$target.append('<option value="' + sltValue[i] + '" selected>' + sltText[i] + '</option>');
						}
					}
					that.$target.trigger('change');
					this.$closebtn.show();
				}
			}
		},
		getData : function() {
			return this.valueList;
		},
		setData : function(data) {
			if (data && data.length > 0) {
				if (this.config.level && this.config.level.length > 0) {
					for (var i = 0; i < this.config.level.length; i++) {
						if (data[i]) {
							this.config.level[i].value = data[i].value;
							this.config.level[i].text = data[i].text;
						}
					}
					this.initValue();
				}
			}
		},
		setFinalValue : function() {
			var that = this;
			var sltText = '';
			var lastValue = that.config.level[that.config.level.length - 1].value;
			var lastText = that.config.level[that.config.level.length - 1].text;
			for (var l = 0; l < that.config.level.length; l++) {
				if (that.config.level[l].text) {
					if (sltText) {
						sltText += ' / ';
					}
					sltText += that.config.level[l].text.toString();
				}
			}
			that.$inputer.find('span').text(sltText).attr('title', sltText);
			that.$closebtn.show();
			that.$target.children().remove();
			if (lastValue) {
				for (var v = 0; v < lastValue.length; v++) {
					that.$target.append('<option selected value="' + lastValue[v] + '">' + lastText[v] + '</option>');
				}
			}
		},
		getLevelContent : function(level, selectAndClose) {
			var that = this;
			that.$inputer.find('span').text('').attr('title', '');
			that.$container.find('.jquery-cascader-ul').each(function() {
				if (parseInt($(this).data('level'), 10) >= level) {
					$(this).closest('td').remove();
				}
			});
			if (that.config && that.config.level && that.config.level[level]) {
				var levelConf = that.config.level[level];

				if (levelConf.url) {
					var url = levelConf.url;
					for (var i = 0; i < level; i++) {
						var param = that.config.level[i].value.toString() || '';
						if (param) {
							url = url.replace(new RegExp('\\{' + i + '\\}', "g"), param);
						} else {
							return;
						}
					}
					$.getJSON(url, function(data) {
						if(that.config.type == 1){
							data = data.Data;
						}
						if (data && data.length > 0) {
							var $ul = $('<ul class="jquery-cascader-ul" data-level="' + level + '"></ul>');
							if (!that.config.level[level].value) {
								that.config.level[level].value = new Array();
							}
							if (!that.config.level[level].text) {
								that.config.level[level].text = new Array();
							}
							if (!that.config.level[level].value instanceof Array) {
								var tmp = that.config.level[level].value;
								that.config.level[level].value = [ tmp ];
							}
							if (!that.config.level[level].text instanceof Array) {
								var tmp = that.config.level[level].text;
								that.config.level[level].text = [ tmp ];
							}
							var isHit = false;
							for (var i = 0; i < data.length; i++) {
								var text = data[i][levelConf['textKey'] || 'text'];
								var value = data[i][levelConf['valueKey'] || 'value'];
								var $li = $('<li style="cursor:pointer" class="jquery-cascader-li jquery-cascader-li-' + level + '"></li>');
								if (!levelConf.multi) {
									$li.on('click', function(e) {
										e.stopPropagation();
										var parent = $(this).closest('.jquery-cascader-ul');
										var currentLevel = parseInt(parent.data('level'), 10);
										var valueObj = {};
										valueObj.value = new Array();
										valueObj.value.push($(this).data('value'));
										valueObj.text = new Array();
										valueObj.text.push($(this).data('text'));
										that.valueList[currentLevel] = valueObj;

										that.$container.find('.jquery-cascader-li-' + level + '.selected').removeClass('selected');
										$(this).addClass('selected');
										// 单选总是清空value和text数组
										that.config.level[currentLevel].value = new Array();
										that.config.level[currentLevel].text = new Array();
										that.config.level[currentLevel].value.push($(this).data('value'));
										that.config.level[currentLevel].text.push($(this).data('text'));
										if (that.config && that.config.level && that.config.level[currentLevel + 1]) {// 下一层
											that.getLevelContent(currentLevel + 1, true);
										} else {// 返回值
											that.setFinalValue();
											that.$container.slideUp('fast');
											that.ison = false;
											that.$target.trigger('change');
										}
									});
									$li.text(text);
									if (levelConf.value && levelConf.value.length > 0) {
										// that.config.level[level].text = new
										// Array();//清空掉text，因为text有可能已经更新
										for (var v = 0; v < levelConf.value.length; v++) {
											if (levelConf.value[v] == value) {
												$li.addClass('selected');
												isHit = true;
												// that.config.level[level].text.push(text);
												break;
											}
										}
									}
									$li.attr('data-text', text);
									$li.attr('data-value', value);
								} else {
									var chk = $('<input type="checkbox" data-makeup="checkbox" class="jquery-cascader-chk">');
									chk.on('click', function(e) {
										e.stopPropagation();

										var parent = $(this).closest('.jquery-cascader-ul');
										var currentLevel = parseInt(parent.data('level'), 10);
										that.config.level[currentLevel].value = new Array();
										that.config.level[currentLevel].text = new Array();
										var valueObj = {
											value : [],
											text : []
										};
										parent.find('.jquery-cascader-chk').each(function() {
											if ($(this).prop('checked')) {
												that.config.level[currentLevel].value.push($(this).data('value'));
												that.config.level[currentLevel].text.push($(this).data('text'));
												valueObj.value.push($(this).data('value'));
												valueObj.text.push($(this).data('text'));
											}
											that.valueList[currentLevel] = valueObj;
										});

										if (that.config && that.config.level && that.config.level[currentLevel + 1]) {// 下一层
											that.getLevelContent(currentLevel + 1, true);
										} else {// 返回值
											that.setFinalValue();
											that.$target.trigger('change');
										}
									});
									var spn = $('<span style="cursor:pointer"></span>');
									spn.text(text);
									spn.on('click',function(chk){
										return function(){
											chk.trigger('click');
										}
									}(chk));
									if (levelConf.value && levelConf.value.length > 0) {
										for (var v = 0; v < levelConf.value.length; v++) {
											if (levelConf.value[v] == value) {
												chk.prop('checked', true);
												isHit = true;
												// that.config.level[level].text[v]
												// = text;
												break;
											}
										}
									}
									chk.attr('data-text', text);
									chk.attr('data-value', value);
									$li.attr('data-text', text);
									$li.attr('data-value', value);
									$li.append(chk).append(spn);
								}
								$ul.append($li);
							}
							var $td = $('<td nowrap class="jquery-cascader-td"></td>');
							if (levelConf.title) {
								$td.append('<div class="jquery-cascader-title">' + levelConf.title + '</div>');
							}
							if (levelConf.searchAble) {
								var sc = $('<div class="jquery-cascader-searchcontainer">' + levelConf.title + '</div>');
								var $searcher = $('<input type="text" style="width:100%;height:25px" placeholder="请输入关键字" class="form-control input-sm">');
								sc.html($searcher);
								$td.append(sc);
								$searcher.bind('input', function() {
									var keyword = $.trim($(this).val()).toLowerCase();
									if (keyword != '') {
										$(this).closest('.jquery-cascader-td').find('.jquery-cascader-li').each(function() {
											if ($(this).data('text').toString().toLowerCase().indexOf(keyword) > -1) {
												$(this).show();
											} else {
												$(this).hide();
											}
										});
									} else {
										$(this).closest('.jquery-cascader-td').find('.jquery-cascader-li').each(function() {
											$(this).show();
										});
									}
								});
							}
							$td.append($ul);
							that.$tr.append($td);
							if (isHit && that.config.level[level + 1]) {
								that.getLevelContent(level + 1);
							} else if (isHit && !that.config.level[level + 1]) {
								that.setFinalValue();
							}
						} else {// 返回值
							if (that.config.targetLevel == 'all') {
								that.setFinalValue();
								if (selectAndClose) {
									that.$container.slideUp('fast');
								}
							}
						}
					});
				}
			}
		}
	};

	$.fn.cascader = function(options) {
		var $target = $(this);
		if (!$target.data('bind-cascader')) {
			var c = new cascader($target, options);

			this.getData = function() {
				return c.getData();
			};
			this.setData = function(data) {
				return c.setData(data);
			};

			$target.attr('bind-cascader', true);
			$target.attr('data-bind', true);
		}
		return this;
	};

})(jQuery);