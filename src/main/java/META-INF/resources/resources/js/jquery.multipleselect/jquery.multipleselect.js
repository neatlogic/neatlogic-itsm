;
(function($) {
	var defaultopts = {
		zindex : 3334,
		confirm : null,
		url : null,
		width : '500',
		height : '250',
		firstText : 'text',
		firstValue : 'value',
		firstDisName : null,
		secondText : 'text',
		secondValue : 'value',
		secondListName : null,
		initTrigger : true,
		secondDisName : null
	};

	var multipleselect = function(target, options) {
		var that = this;
		this.$config = $.extend(true, {}, defaultopts, options);
		this.$parent = $('<div class="jquery-multipleselect form-control"></div>');
		this.$target = $(target);
		this.flag = true;
		this.$target.parent().append(this.$parent);
		this.$target.hide();
		this.$parent.data('config', this.$config);
		this.$container = $('<div class="jquery-multipleselect-container"></div>');
		this.$containerBody = $('<div></div>');
		this.$containerFoot = $('<div style="background:#f0f0f0;text-align:right;margin-top:5px;padding:5px;border-top:1px solid #ccc"></div>');
		this.$btnConfirm = $('<button type="button" class="btn btn-primary" style="margin:0px 6px;">${tk:lang("确认")}</button>');
		this.$btnCancel = $('<button type="button" class="btn btn-default">${tk:lang("取消")}</button>');
		this.$parent.data('container', this.$container);
		this.$parent.data('containerbody', this.$containerBody);
		this.$parent.css({
			'width' : that.$target.width() < that.$config.width ? (parseInt(that.$config.width) + 2) : (parseInt(that.$target.width()) + 2) + 'px'
		});
		this.$btnConfirm.on('click', function() {
			that.$target.find('option').remove();
			var outHtml = '';
			that.$container.find('.jquery_multipleselect_checkbox').each(function() {
				if ($(this).attr('checked')) {
					var value = $(this).val();
					var text = $(this).attr('it_' + that.$config.secondText);
					that.$target.append('<option value="' + value + '" text="' + text + '" selected="selected">' + text + '</option>');
					outHtml = outHtml + text + '、';
				}
			});
			that.$target.trigger('change');
			if (outHtml != '') {
				outHtml = outHtml.substr(0, outHtml.length - 1);
			}
			that.$parent.html(outHtml);
			that.$container.slideUp(function() {
				that.$containerBody.empty();
			});
		});

		this.$btnCancel.on('click', function() {
			that.$container.slideUp(function() {
				that.$containerBody.empty();
			});
		});

		this.$container.css('z-index', this.$config.zindex);
		this.$parent.after(this.$container);
		this.$containerFoot.append(this.$btnConfirm).append(this.$btnCancel);

		this.$parent.on('click', function() {
			if (!that.$container.is(':visible')) {
				var top = that.$parent.position().top + that.$parent.outerHeight(true) - 1;
				var left = that.$parent.position().left;

				that.$container.css({
					'top' : top,
					'left' : left,
					'width' : that.$parent.outerWidth(true)
				});

				that.initValue();
				that.$container.slideDown();
			} else {
				that.$container.slideUp(function() {
					that.$containerBody.empty();
				});
			}
		});
		that.initValue();
	}

	this.multipleselect = multipleselect;

	multipleselect.prototype = {
		initValue : function() {
			var that = this;
			var config = that.$config;
			var target = that.$target;
			var containerBody = that.$containerBody;
			var containerFoot = that.$containerFoot;
			var container = that.$container;
			var parent = that.$parent;
			if (config.url && config.secondListName && container.find('.jquery-multipleselect-table').length == 0) {

				$.getJSON(config.url, function(data) {

					var $main = $('<div class="jquery-multipleselect-div clearfix"></div>');
					var $left = $('<div class="jquery-multipleselect-left d_f"></div>');
					var $right = $('<div class="jquery-multipleselect-right d_f_r"></div>')
					$left.css({
						'width' : '30%'
					});
					$right.css({
						'width' : '70%'
					});
					var $inputItem = $('<div class="jquery-multipleselect-input"><div>');
					var $input = $('<input type="text" style="width:100%" placeholder="${tk:lang("请输入关键字")}" class="form-control input-sm" />')
					var $ul = $('<ul class="jquery-multipleselect-nav-list navlist-ul" style="max-height:'+config.height+'px;overflow:auto;"></ul>');
					var $view = $('<div class="jquery-multipleselect-view"></div>');
					var $viewList = $('<div class="jquery-multipleselect-viewlist clearfix"></div>')
					$view.append($viewList);
					if (config.secondDisName) {
						$right.append($('<h6 style="padding:2px;border-bottom:1px dashed #eee;">' + config.secondDisName + '</h6>'));
					}
					$main.prepend($view);
					var checkArray = new Array();
					for ( var i in data) {
						var item = data[i];
						valuekey = config.firstValue;
						textkey = config.firstText;
						var first_value = '', first_text = '', list = null;
						for ( var j in data[i]) {
							if (valuekey.toLowerCase() == j.toLowerCase()) {
								first_value = data[i][j];
							} else if (textkey.toLowerCase() == j.toLowerCase()) {
								first_text = data[i][j];
							} else if (config.secondListName.toLowerCase() == j.toLowerCase()) {
								list = data[i][j];
							}
						}
						var active = i == 0 ? 'active' : '';
						var display = i == 0 ? '' : 'none';
						var $li = $('<li class="navlist-li jquery-multipleselect-left-item ' + active + '" title="'+ first_text +'" it_' + valuekey + '="' + first_value + '"  it_' + textkey + '="' + first_text + '"  ><a href="javascript:void(0)">' + first_text + '</a></li>');
						$ul.append($li);
						var $content = $('<div class="jquery-multipleselect-content" style="width: 100%;display:' + display + ';max-height:'+config.height+'px;overflow:auto;" id="jquery_multipleselect_content' + first_value + '">');
						var $table = $('<table class="table table-striped table-hover table-condensed jquery-multipleselect-table"></table>');

						var second_value = '', second_text = '';
						for ( var k in list) {
							var that = list[k];
							for ( var j in that) {
								if (config.secondValue.toLowerCase() == j.toLowerCase()) {
									second_value = that[j];
								} else if (config.secondText.toLowerCase() == j.toLowerCase()) {
									second_text = that[j];
								}
							}

							var ischeck = false;
							target.find('option').each(function() {
								if ($(this).attr('value') == second_value) {
									ischeck = true;
								}
							});

							var $tr = $('<tr><td style="width: 60px; text-align: left;"><input data-makeup="checkbox" type="checkbox" class="jquery_multipleselect_checkbox jquery_multipleselect_checkbox' + first_value + '"  it_' + valuekey + '="'
									+ first_value + '"  name="jquery_multipleselect_checkbox' + first_value + '" value="' + second_value + '" it_' + config.secondText + '="' + second_text + '">' + second_text + '</td></tr>');
							$table.append($tr);

							if (ischeck) {
								checkArray.push(second_value);
							}
						}
						$content.append($table);
						$right.append($content);
					}

					$inputItem.append($input);
					if (config.firstDisName) {
						$left.append($('<h6 style="padding:2px;border-bottom:1px dashed #eee;">' + config.firstDisName + '</h6>'));
					}
					$left.append($inputItem).append($ul);
					$main.append($left).append($right);
					containerBody.append($main);
					container.append(containerBody).append(containerFoot);

					$input.keyup(function() {
						var value = $(this).val();
						if (value) {
							var textkey = config.firstText;
							container.find('.jquery-multipleselect-left-item').each(function() {
								if ($(this).attr('it_' + textkey).toLowerCase().indexOf(value.toLowerCase()) == -1) {
									$(this).hide();
								} else {
									$(this).show();
								}
							});
						} else {
							container.find('.jquery-multipleselect-left-item').each(function() {
								$(this).show();
							});
						}
					});

					container.find('.jquery-multipleselect-left-item').click(function() {
						var value = $(this).attr('it_' + config.firstValue);
						container.find('.jquery-multipleselect-left-item').removeClass('active');
						container.find('.jquery-multipleselect-content').hide();
						$(this).addClass('active');
						container.find('#jquery_multipleselect_content' + value).show();
					});

					container.find('.jquery_multipleselect_checkbox').click(
							function() {
								var value = $(this).attr('it_' + config.firstValue);
								var text = $(this).attr('it_' + config.secondText);
								if ($(this).attr('ischeck') == '0') {
									$(this).attr('checked', false);
									$(this).attr('ischeck', "1");
								} else {
									container.find('.jquery_multipleselect_checkbox' + value).attr('checked', false);
									$(this).attr('checked', true);
									$(this).attr('ischeck', "0");

									$('.jquery-multipleselect-view-item').each(function() {
										if ($(this).attr('it_' + config.secondValue) == value) {
											$(this).remove();
										}
									});

									var $viewItem = $('<div class="jquery-multipleselect-view-item btn btn-xs btn-default" it_' + config.secondValue + '="' + value + '">' + text
											+ ' <a href="javascript:void(0);" class="jquery-multipleselect-item-close close ts-remove" ></a></div>');
									$viewList.append($viewItem);
								}
							});
					$(document).off('click', '.jquery-multipleselect-item-close');
					$(document).on('click', '.jquery-multipleselect-item-close', function() {
						var value = $(this).parent().attr('it_' + config.secondValue);
						container.find('.jquery_multipleselect_checkbox').each(function() {
							if ($(this).attr('it_' + config.secondValue) == value) {
								$(this).attr('checked', false);
								$(this).attr('ischeck', "1");
							}
						});
						$(this).parent().remove();
					});
					if (checkArray.length > 0) {
						for ( var v in checkArray) {
							var _isLast = false;
							if (v == checkArray.length - 1) {
								_isLast = true;
							}
							container.find('.jquery_multipleselect_checkbox').each(function() {
								if ($(this).val() == checkArray[v]) {
									$(this).attr('ischeck', '1').trigger('click');
									if (_isLast) {
										var _tv = $(this).attr('it_' + config.firstValue);
										container.find('.jquery-multipleselect-left-item').each(function() {
											if ($(this).attr('it_' + config.firstValue) == _tv) {
												$(this).trigger('click');
											}
										});
									}
								}
							});
						}
						var outHtml = '';
						container.find('.jquery_multipleselect_checkbox').each(function() {
							if ($(this).attr('checked')) {
								var value = $(this).val();
								var text = $(this).attr('it_' + config.secondText);
								target.append('<option value="' + value + '" text="' + text + '" selected="selected">' + text + '</option>');
								outHtml = outHtml + text + '、';
							}
						});
						if (config.initTrigger)// 是否需要触发
							target.trigger('change');
						if (outHtml != '') {
							outHtml = outHtml.substr(0, outHtml.length - 1);
						}
						parent.html(outHtml);
					}
					that.flag = false;
				});
			}
		}
	};

	$.fn.multipleselect = function(options) {
		var $target = $(this);
		if (!$target.attr('bind')) {
			var c = new multipleselect($target, options);
			$target.attr('bind', true);
		}
		return this;
	};

})(jQuery);