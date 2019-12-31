(function($) {

	$.fn.tags = function(options) {
		var defaults = {
			url : null,// ajax地址
			type : "GET", // ajax方式
			parameterName : "hidTags", // 生成的<input type='hidden' />的name属性
			textkey : 'text', // 显示label的值
			valuekey : 'value', // 显示value的值
			paramName : 'term', // ajax 送往后台的参数名
			fnAfterRemoved : null, // 删除标签回调函数
			fnBeforeSave : null
		// 新增标签回调函数,此方法如有后台操作必须用同步（async : false）
		};
		options = options || {};
		$.extend(defaults, options);

		var $div = this;
		var $buttons = $div.find("div:nth-child(1)");
		var $input = $div.find("input:last");
		var menuOpend = false;

		$div.click(function() {
			$(this).find("input").focus();
			return true;
		});

		// 当鼠标离开输入框，本点击document的时候
		$(document).click(function() {
			var term = $input.val();
			if ($.trim(term)) {
				var $selectedItem = $('<li></li>');
				$selectedItem.text($.trim(term));
				inputTagLabel($selectedItem, defaults);
				$input.val("");
				$(".tags-menu").remove();
			}
		});

		$input.keyup(function(event) {
			var keyCode = event.keyCode;
			var $meun = null;
			var $item = null;
			var isMenuPopuped = $(".tags-menu").length == 1;

			if (keyCode == 27) { // esc
				$(".tags-menu").remove();
			} else if (keyCode == 40) { // down
				if (!isMenuPopuped) {
					return false;
				}
				$menu = $(".tags-menu");
				var $highlight = $(".tags-menu .tags-highlight");
				if ($highlight.length == 0) {
					$menu.find("li:eq(0)").addClass("tags-highlight");
				} else {
					$highlight.removeClass("tags-highlight").next().addClass("tags-highlight");
				}

			} else if (keyCode == 38) { // up
				if (!isMenuPopuped) {
					return false;
				}
				$menu = $(".tags-menu");
				var $highlight = $(".tags-menu .tags-highlight");
				if ($highlight.length == 0) {
					$menu.find("li:last").addClass("tags-highlight");
				} else {
					$highlight.removeClass("tags-highlight").prev().addClass("tags-highlight");
				}
			} else if (keyCode == 13) {
				$menu = $(".tags-menu");
				if ($menu.is(':visible')) {
					if ($menu.length == 0) {
						return false;
					}
					var $selectedItem = $menu.find(".tags-highlight");
					if ($selectedItem.length == 0) {
						return false;
					} else {
						selectTagLabel($selectedItem, defaults);
						$input.val("");
						$menu.remove();
					}
				} else {
					var term = $input.val();
					if ($.trim(term)) {
						var $selectedItem = $('<li></li>');
						$selectedItem.text($.trim(term));
						inputTagLabel($selectedItem, defaults);
						$input.val("");
						$(".tags-menu").remove();
					}
				}
			} else if (keyCode == 8) { // 删除

				/*if ($input.val().length == 0) {
					var tag = $("input[name='" + defaults.parameterName + "']:last");
					if (tag) {
						var value = tag.val();
						var label = tag.data('label');
						if (defaults.fnAfterRemoved) {
							defaults.fnAfterRemoved(value);
						}
						tag.remove();
						$('.btnTags:last').remove();
					}
				}*/

			} else {
				$(".tags-menu").remove();
				var term = $input.val();
				if (term.length == 0) {
					$(".tags-menu").remove();
					return false;
				} else {
					var myurl = defaults.url;
					if (myurl.indexOf('?') > -1) {
						myurl = myurl + '&' + defaults.paramName + '=' + encodeURIComponent(term);
					} else {
						myurl = myurl + '?' + defaults.paramName + '=' + encodeURIComponent(term);
					}
					// 发送ajax请求
					$.ajax({
						url : myurl,
						type : defaults.type,
						dataType : "json",
						success : function(data) {
							if (data && data.length != 0) {
								// 每次搜索重置
								$(".tags-menu").remove();

								$menu = $('<ul class="tags-menu"></ul>');
								var textkey = 'text';
								var valuekey = 'value';
								if (defaults) {
									textkey = defaults['textkey'] || 'text';
									valuekey = defaults['valuekey'] || 'value';
								}
								textkey = textkey.toLowerCase();
								valuekey = valuekey.toLowerCase();

								for (i = 0; i < data.length; i++) {
									$item = $('<li class="tags-item">' + data[i][textkey] + '</li>').attr("data-value", data[i][valuekey]);
									$item.appendTo($menu);
								}

								$menu.appendTo("body").css("top", $input.position().top + 33).css("left", $input.position().left + 3).show();

								$menu.find("li").mouseover(function() {
									$menu.find("li.tags-highlight").removeClass("tags-highlight");
									$(this).addClass("tags-highlight");
								}).mouseout(function() {
									$menu.find("li").removeClass("tags-highlight");
								});

								$menu.find("li").click(function() {
									selectTagLabel($(this), defaults);
									$menu.remove();
									$input.val("");
								});
							} else {
								$(".tags-menu").remove();
								return false;
							}
						}
					});

				}
			}
		});

		var initTagLabel = function($input) {
			var values = $input.data('tags');
			var divSelector = $div;
			if (values && values.length > 0) {
				values = eval('(' + values + ')');
				for (var i = 0, l = values.length; i < l; i++) {
					for ( var key in values[i]) {
						var value = key;
						var label = values[i][key];
						var appendToSelector = divSelector.find("div:eq(0)");
						var $btn = $('<button type="button" class="btn btn-info btn-xs btnTags" data-value="' + value + '" style="margin-right: 1px;"><i class="icon-tag icon-white"></i> ' + label + '&nbsp;&nbsp;<i class="close">&times;</i></button>');
						$btn.appendTo(appendToSelector).click(function() {
							var value = $(this).attr("data-value");
							divSelector.find("input[type='hidden']").each(function() {
								if ($(this).val() == value || $(this).data('label') == label) {
									$(this).remove();
									if (defaults.fnAfterRemoved) {
										defaults.fnAfterRemoved(value);
									}
									return false;
								}
							});
							$(this).remove();
						});
						var $hidden = $("<input type='hidden' name='" + defaults.parameterName + "' data-label='" + label + "' />").val(value);
						$hidden.appendTo(divSelector);
					}
				}
			}
		}

		var inputTagLabel = function(selectedItem, defaults) {
			var label = selectedItem.text();
			var isExits = false;
			var divSelector = $div;
			$("input[name='" + defaults.parameterName + "']").each(function() {
				if ($(this).data('label') == label) {
					isExits = true;
					return;
				}
			});
			if (!isExits) {
				if (defaults.fnBeforeSave) {
					var r = defaults.fnBeforeSave(label);
					if (r) {
						var value = r.id;
						if (r.Status == 'OK') {
							var appendToSelector = divSelector.find("div:eq(0)");
							var $btn = $('<button type="button" class="btn btn-info btn-xs btnTags" data-value="' + value + '" style="margin-right: 1px;"><i class="icon-tag icon-white"></i> ' + label + '&nbsp;&nbsp;<i class="close">&times;</i></button>');
							$btn.appendTo(appendToSelector).click(function() {
								var value = $(this).attr("data-value");
								divSelector.find("input[type='hidden']").each(function() {
									if ($(this).val() == value || $(this).data('label') == label) {
										$(this).remove();
										if (defaults.fnAfterRemoved) {
											var r = defaults.fnAfterRemoved(value);
											if (!r) {
												return;
											}
										}
										return false;
									}
								});
								$(this).remove();
							});
							var $hidden = $("<input type='hidden' name='" + defaults.parameterName + "' data-label='" + label + "' />").val(value);
							$hidden.appendTo(divSelector);
						}
					}
				}
			}
		}

		var selectTagLabel = function(selectedItem, defaults) {
			var label = selectedItem.text();
			var value = selectedItem.attr("data-value");
			var divSelector = $div;
			var isExits = false;
			$("input[name='" + defaults.parameterName + "']").each(function() {
				if ($(this).val() == value) {
					isExits = true;
					return;
				}
			});
			if (!isExits) {
				var appendToSelector = divSelector.find("div:eq(0)");
				var $btn = $('<button type="button" class="btn btn-info btn-xs btnTags" data-value="' + value + '" style="margin-right: 1px;"><i class="icon-tag icon-white"></i> ' + label + '&nbsp;&nbsp;<i class="close">&times;</i></button>');
				$btn.appendTo(appendToSelector).click(function() {
					var value = $(this).attr("data-value");
					divSelector.find("input[type='hidden']").each(function() {
						if ($(this).val() == value || $(this).data('label') == label) {
							$(this).remove();
							if (defaults.fnAfterRemoved) {
								var r = defaults.fnAfterRemoved(value);
								if (!r) {
									return;
								}
							}
							return false;
						}
					});
					$(this).remove();
				});
				var $hidden = $("<input type='hidden' name='" + defaults.parameterName + "' data-label='" + label + "' />").val(value);
				$hidden.appendTo(divSelector);
			}
		}

		// 初始化数据
		initTagLabel($input);
	};
})(jQuery);