(function($) {
	$.fn.clearCache = function() {
		var RELATION_VALUE = $.cookie('RELATION_VALUE');
		if (RELATION_VALUE) {
			$('.J-relation-list').remove();
			var json = eval('(' + RELATION_VALUE + ')');
			for (k in json) {
				var value = json[k];
				$('#batchAdd_' + value.id).parent().attr('disabled', false);
			}
			$.cookie("RELATION_VALUE", '', {
				path : '/'
			})
			$('.J-relation-num').text(0);
			showPopMsg.success('cache清除完毕。');
		}
	};

	$.fn.batchOpt = function() {
		var RELATION_VALUE = $.cookie('RELATION_VALUE');
		if (RELATION_VALUE) {
			var json = eval('(' + RELATION_VALUE + ')');
			var $mylen = 0;
			var jsonArray = new Array();
			for (k in json) {
				var value = json[k];
				$mylen = $mylen + 1;
				jsonArray.push({
					'id' : value.id
				});
			}
			if ($mylen == 0) {
				showPopMsg.error('请先选择要操作的流程。');
			} else {
				var url = "/balantflow/relation/toBatchOptTaskList.do";
				var form = $("<form></form>");
				form.attr('style', 'display:none');
				form.attr('method', 'post');
				form.attr('action', url);
				var input = $('<input>');
				input.attr('type', 'hidden');
				input.attr('name', 'query');
				input.attr('value', JSON.stringify(jsonArray));
				form.append(input);
				$('body').append(form);
				form.submit();
				form.remove();
			}
		} else {
			showPopMsg.info('请先选择关联流程。');
		}
	};

	$.fn.handle = function() {
		var RELATION_VALUE = $.cookie('RELATION_VALUE');
		if (RELATION_VALUE) {
			var json = eval('(' + RELATION_VALUE + ')');
			var $mylen = 0;
			var jsonArray = new Array();
			for (k in json) {
				var value = json[k];
				$mylen = $mylen + 1;
				jsonArray.push({
					'id' : value.id
				});
			}
			if ($mylen < 2) {
				showPopMsg.error('请选择两个以上流程实例。');
			} else {
				var url = "/balantflow/relation/toRelation.do";
				var form = $("<form></form>");
				form.attr('style', 'display:none');
				form.attr('method', 'post');
				form.attr('action', url);
				var input = $('<input>');
				input.attr('type', 'hidden');
				input.attr('name', 'query');
				input.attr('value', JSON.stringify(jsonArray));
				form.append(input);
				$('body').append(form);
				form.submit();
				form.remove();
			}
		} else {
			showPopMsg.info('请先选择关联流程。');
		}
	};

	$.extend($.fn, {
		relation : function(options) {
			var self = this, $shop = $('.J-relation'), $title = $('.J-relation-title'), $body = $('.J-relation-body'), $num = $('.J-relation-num'), $close = $('.J-relation-close');
			var S = {
				init : function() {
					$(document).on('click', '.J-relation-title', this.clickOnTitle);
					$(document).on('click', '.J-relation-close', this.removeList);
					$(self).data('click', true).on('click', this.addRelation);
					$(document).on('click', S.slideBoxMini);
					$body.on('click', this.clickOnBody);
				},
				clickOnBody : function(e) {
					if (!$(e.target).hasClass('J-relation-close')) {
						e.stopPropagation(); // 阻止冒泡
					}
					;
				},
				addRelation : function(e) {
					e.stopPropagation();
					var $target = $(e.target), id = $target.attr('taskId'), title = $target.attr('taskTitle'), bg = $target.attr('bg'), cutType = $target.attr('cutType'), dis = $target.data('click'), x = $target.offset().left + 30, y = $target.offset().top + 10, X = $shop.offset().left + $shop.width() / 2 - $target.width() / 2 + 10, Y = $shop.offset().top;
					if (dis) {
						if ($('#floatOrder').length <= 0) {
							var tr = $target.parent().parent().parent();
							var html = '<table class="table"><tr style="background:#eee;">' + '<td>' + tr.find('td').eq(1).html() + '</td>' + '<td>' + tr.find('td').eq(2).html() + '</td>' + '<td>' + tr.find('td').eq(3).html() + '</td>' + '<td>' + tr.find('td').eq(4).html() + '</td>' + '<td>' + tr.find('td').eq(5).html() + '</td>' + '<td>' + tr.find('td').eq(6).html() + '</td>' + '<td>' + tr.find('td').eq(7).html() + '</td>' + '</tr></table>';
							$('body').append('<div id="floatOrder">' + html + '</div');
						}
						;
						var $obj = $('#floatOrder');
						if (!$obj.is(':animated')) {
							$obj.css({
								'left' : x,
								'top' : y
							}).animate({
								'left' : X,
								'top' : Y - 80
							}, 500, function() {
								$obj.stop(false, false).animate({
									'top' : Y - 20,
									'opacity' : 0
								}, 500, function() {
									$obj.fadeOut(300, function() {
										$obj.remove();
										$target.data('click', false).addClass('dis-click').parent().attr('disabled', true);
										var l = $('.J-relation-list').length, num = Number($num.text());
										if (l < 20) {
											var canDo = true;
											var RELATION_VALUE = $.cookie('RELATION_VALUE');
											if (RELATION_VALUE) {
												var json = eval('(' + RELATION_VALUE + ')');
												for (k in json) {
													if (k == ('id_' + id)) {
														canDo = false;
													}
												}
												if (canDo) {
													json['id_' + id] = {
														'id' : id,
														'bg' : bg,
														'cutType' : cutType,
														'title' : title
													};
													$.cookie('RELATION_VALUE', JSON.stringify(json), {
														path : '/'
													});
												}
											} else {
												var json = {};
												json['id_' + id] = {
													'id' : id,
													'bg' : bg,
													'cutType' : cutType,
													'title' : title
												};
												$.cookie('RELATION_VALUE', JSON.stringify(json), {
													path : '/'
												});
											}
											if (canDo) {
												$body.prepend('<div class="J-relation-list" data-id="' + id + '"> ' + '<div class="J-relation-list-a">' + '<span class="channelType" style="background-color:' + bg + '">' + cutType + '</span>' + '<a href="/balantflow/task/getTaskDetail.do?taskId=' + id + '" >[' + id + ']' + title + '</a></div>' + '<div class="baseBg J-relation-close"></div></div>');
												$num.text(num + 1);
											}
										}
										;
									});
								});
							});
						}
						;
					}
					;
				},
				clickOnTitle : function(e) {
					e.stopPropagation();
					var length = $('.J-relation-list').length;
					if (length > 0) {
						if (!$shop.hasClass('J-relation-small')) {
							$body.slideToggle();
						} else {
							$('.J-relation-mx').hide();
							$('.J-relation-px').show();
							$shop.animate({
								'width' : 289
							}, 100, function() {
								$shop.removeClass('J-relation-small');
								$body.slideDown();
							});
						}
						;
					}
					;
				},
				slideBoxMini : function() {
					$('.J-relation-px,.J-relation-body').hide();
					$('.J-relation-mx').show();
					$shop.animate({
						'width' : 119
					}, 100, function() {
						$shop.addClass('J-relation-small');
					});
				},
				removeList : function(e) {
					e.stopPropagation();
					var $target = $(e.target), $parent = $target.parents('.J-relation-list'), id = $parent.attr('data-id');
					$parent.addClass('J-relation-list-remove').fadeOut(300, function() {
						$('#batchAdd_' + id).data('click', true).removeClass('dis-click').parent().attr('disabled', false);
						;
						$parent.remove();
						S.hideBody();
						if (options && options.callback) {
							options.callback($(self));
						}
						;
						var json = eval('(' + $.cookie('RELATION_VALUE') + ')');
						delete json['id_' + id];
						$.cookie('RELATION_VALUE', JSON.stringify(json), {
							path : '/'
						});
					});
				},
				hideBody : function() {
					var length = $('.J-relation-list').length;
					$num.text(length);
					if (length == 0) {
						$('.J-relation-px,.J-relation-body').hide();
						$('.J-relation-mx').show();
						$shop.animate({
							'width' : 119
						}, 100, function() {
							$shop.addClass('J-relation-small');
						});
					}
					;
				}
			};
			S.init();
		},
		initRelationDiv : function() {
			var html = '<div class="J-relation J-relation-small" id="J-relation-div">'
					+ '<div class="J-relation-item"><div class="baseBg J-L-ico J-relation-pos"></div>'
					+ '<div class="J-relation-main">' 
					+ '<div class="J-relation-title">' 
					+ '<a href="#" title="" style="white-space:nowrap" class="J-go"><i class="pic-icon-small pic-icon-batch"></i>批量选择池<span style="padding:2px" class="label label-danger J-relation-num">0</span></a>' 
					+ '</div>' 
					+ '<div class="baseBg J-relation-mx"></div>' 
					+ '<div class="J-relation-px"></div>'
					+ '<div class="J-relation-body">' 
					+ '<div class="J-relation-buy"><span>最多处理<strong>20</strong>条数据</span></div>' 
					+ '<div style="text-align:right;width:100%;">' 
					+ '<button class="btn btn-success btn-xs J-relation-Opt" type="button"><i class=" icon-retweet icon-white"></i>批量操作</button>' 
					+ '<button class="btn btn-info btn-xs J-relation-handle" type="button"><i class=" icon-retweet icon-white"></i>批量关联</button>' 
					+ '<button class="btn btn-warning btn-xs J-relation-clearCache" type="button"><i class=" icon-trash icon-white"></i>清空</button></div>	' 
					+ '</div>' 
					+ '</div>' 
					+ '<div class="baseBg J-R-ico J-relation-pos"></div>'
					+ '</div>' 
					+ '</div>';
			var J_length = $('#J-relation-div').length;
			if (J_length == 0) {
				$('body').append(html);
				var RELATION_VALUE = $.cookie('RELATION_VALUE');
				if (RELATION_VALUE) {
					var json = eval('(' + RELATION_VALUE + ')');
					var $_length = 0;
					for (k in json) {
						var value = json[k];
						if (value) {
							$('.J-relation-body').prepend('<div class="J-relation-list" data-id="' + value.id + '"> ' + '<div class="J-relation-list-a">' + '<span class="channelType" style="background-color:' + value.bg + '">' + value.cutType + '</span>' + '<a href="/balantflow/task/getTaskDetail.do?taskId=' + value.id + '" >[' + value.id + ']' + value.title + '</a></div>' + '<div class="baseBg J-relation-close"></div></div>');
							$_length = $_length + 1;
							$('#batchAdd_' + value.id).parent().attr('disabled', true);
						}
					}
					$('.J-relation-num').text($_length);
				}
				$clearCache = $('.J-relation-clearCache');
				$clearCache.on('click', this.clearCache);
				$hanlde = $('.J-relation-handle');
				$hanlde.on('click', this.handle);
				$opt = $('.J-relation-Opt');
				$opt.on('click', this.batchOpt);
			}
		}
	});

	$(function() {
		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			var exist = $('.batchOperateAdd').length;
			if (exist > 0) {
				$(this).initRelationDiv();
				$('.batchOperateAdd').relation();
			}
		});
		var exist = $('.batchOperateAdd').length;
		if (exist > 0) {
			$(this).initRelationDiv();
			$('.batchOperateAdd').relation();
		}
	});
})(jQuery);