;
(function($) {
	var tmp_panel = '<div class="jquery-titleinfo-panel"></div>';
	var tmp_panel_header = '<div class="jquery-titleinfo-panelheader"><span class="title"></span></div>';
	var tmp_close_btn = '<i class="jquery-titleinfo-closebtn fa fa-close"></i>';
	var tmp_panel_body = '<div class="jquery-titleinfo-panelbody"></div>';
	var tmp_panel_arrow = '<span class="arrow"></span>';
	var $currentopenpanel = null;
	var closetimer = null;
	var closetimeout = 500;
	var titleInfo = function(target, config) {
		this.$target = $(target);
		this.config = config;
		this.trigger = config.trigger || 'hover';
		this.url = config.url || '';
		this.title = config.title || '';
		this.offset = 15;
		this.arrowoffset = 30;
		this.parentContainer = config.parentContainer ? $(config.parentContainer) : null;
		this.$panel = null;
		this.init();
		this.$target.data('titleinfo', this);
	}

	this.titleInfo = titleInfo;
	titleInfo.prototype = {
		init : function() {
			var that = this;
			that.$target.wrap('<div tabindex="-1" class="jquery-titleinfo-panelcontainer"></div>');
			that.$wraper = that.$target.closest('.jquery-titleinfo-panelcontainer');
			if (that.config.width) {
				if (!isNaN(parseFloat(that.config.width)) && isFinite(that.config.width)) {
					that.$wraper.css('width', that.config.width + 'px');
				} else {
					that.$wraper.css('width', that.config.width);
				}
			}
			var event = '';
			if (that.trigger == 'hover') {
				event = 'mouseenter';
				that.$wraper.on('mouseleave', function(e) {
					if ($currentopenpanel && !$currentopenpanel.data('in')) {
						if (closetimer) {
							window.clearTimeout(closetimer);
							closetimer = null;
						}
						closetimer = window.setTimeout(function() {
							if ($currentopenpanel && !$currentopenpanel.data('in')) {
								/*
								 * $currentopenpanel.hide();
								 * $currentopenpanel.detach(); $currentopenpanel =
								 * null;
								 */
								that.hide();
							}
						}, closetimeout);
					}
				});
			} else {
				event = 'click';
				$(document).on('click', function(event) {
					if ($currentopenpanel && $currentopenpanel.find(event.target).length == 0) {
						$currentopenpanel.data('plugin').hide();
						/*
						 * $currentopenpanel.hide(); $currentopenpanel.detach();
						 * $currentopenpanel = null;
						 */
					}
				});
			}
			that.$target.on(event, function(e) {
				e.stopPropagation();
				$(this).focus();
				var needopen = true;
				if ($currentopenpanel) {
					if ($currentopenpanel.data('trigger') != this) {
						if (closetimer) {
							window.clearTimeout(closetimer);
							closetimer = null;
						}
						that.hide();
						/*
						 * $currentopenpanel.stop(); $currentopenpanel.hide();
						 * $currentopenpanel.detach(); $currentopenpanel = null;
						 */
					} else {
						needopen = false;
						if (event == 'click') {
							that.hide();
							/*
							 * $currentopenpanel.stop();
							 * $currentopenpanel.hide();
							 * $currentopenpanel.detach(); $currentopenpanel =
							 * null;
							 */
						}
					}
				}
				if (needopen) {
					var $panel = that.generateContainer();
					that.$wraper = $(this).closest('.jquery-titleinfo-panelcontainer');
					that.$wraper.append($panel);
					that.$target.data('panel', $panel);
					that.$panel = $panel;
					if (that.title) {
						if (typeof that.title != 'function') {
							var span = $('<span>' + that.title + '</span>');
							var w = null;
							if (that.config.width) {
								if (!isNaN(parseFloat(that.config.width)) && isFinite(that.config.width)) {
									w = that.config.width + 'px';
								} else {
									w = that.config.width;
								}
							} else {
								$('body').append(span);
								span.hide();
								var w = span.width() + 25 + 'px';
								span.remove();
							}
							$panel.css('width', w);
							that.$panel.find('.jquery-titleinfo-panelbody').empty().html(that.title);
							that.$panel.css('min-width', '50px');
							that.updatePosition();
							$currentopenpanel = $panel;
							$currentopenpanel.data('trigger', this);
						} else {
							that.title(function(content) {
								var span = $('<span>' + content + '</span>');
								var w = null;
								if (that.config.width) {
									if (!isNaN(parseFloat(that.config.width)) && isFinite(that.config.width)) {
										w = that.config.width + 'px';
									} else {
										w = that.config.width;
									}
								} else {
									$('body').append(span);
									span.hide();
									var w = span.width() + 25 + 'px';
									span.remove();
								}
								$panel.css('width', w);
								that.$panel.find('.jquery-titleinfo-panelbody').empty().html(content);
								that.$panel.css('min-width', '50px');
								that.updatePosition();
								$currentopenpanel = $panel;
								$currentopenpanel.data('trigger', this);
							});
						}
					} else if (that.url) {
						that.$panel.css('width', '50px');
						that.$panel.find('.jquery-titleinfo-panelbody').empty().html('<img src="/balantflow/resources/images/loading.gif">');
						/*
						 * that.$panel.find('.jquery-titleinfo-panelbody').load(that.url,
						 * function() { that.$panel.css('width', 'auto');
						 * that.updatePosition(); });
						 */
						$.get(that.url, (function(target) {
							return function(html) {
								var $html = $(html);
								var tmp = $('<span></span>');
								tmp.html($html);
								var scriptList = new Array();
								tmp.find('script.xdotScript').each(function() {
									scriptList.push($(this).html());
								});
								tmp.find('script.xdotScript').each(function() {
									$(this).empty();
									$(this).remove();
								});
								for (var s = 0; s < scriptList.length; s++) {
									try {
										var fn = null, loadFn = null;
										eval(scriptList[s]);
										if (typeof (customFn) == 'object') {
											for ( var fnname in customFn) {
												window[fnname] = (function(t, f) {
													return function() {
														customFn[f].apply(t, arguments);
													}
												}($html, fnname));
											}
										}
										if (typeof (fn) == 'object') {
											for ( var dom in fn) {
												var fucList = fn[dom];
												for ( var fuc in fucList) {
													tmp.find(dom).off(fuc).on(fuc, fucList[fuc]);
												}
											}
										}
										if (typeof (loadFn) == 'function') {
											tmp.ready((function($html) {
												return function() {
													loadFn($html);
												}
											}($html)));
										}

										fn = null;
									} catch (e) {
										console.error(e);
									}
								}
								$(document).trigger('xdoTRender', $html);
								target.$panel.find('.jquery-titleinfo-panelbody').empty().append($html);
								target.$panel.css('width', 'auto');
								target.updatePosition();
								that.updatePosition();
								$currentopenpanel = $panel;
								$currentopenpanel.data('trigger', this);
							};
						}(that)));
					}
				} else {
					if (closetimer) {
						window.clearTimeout(closetimer);
						closetimer = null;
					}
				}
			});
			that.$target.attr('data-bind', true);
			that.$target.bind('hide-title', function() {
				that.hide(this);
			});
		},
		hide : function(trigger) {
			// if ($currentopenpanel && $currentopenpanel.data('trigger') ==
			// trigger) {

			if (this.config.closeFuc && typeof this.config.closeFuc == 'function') {
				this.config.closeFuc(this.$panel);
			}
			$currentopenpanel.stop();
			$currentopenpanel.hide();
			$currentopenpanel.detach();
			$currentopenpanel = null;
			// }
		},
		generateContainer : function() {
			var that = this;
			var $panelheader = $(tmp_panel_header);
			var $panelbody = $(tmp_panel_body);
			var $panel = $(tmp_panel);
			var $arrow = $(tmp_panel_arrow);
			var $closebtn = $(tmp_close_btn);
			$closebtn.on('click', function(e) {
				if ($currentopenpanel) {
					$currentopenpanel.detach();
					$currentopenpanel = null;
				}
				e.stopPropagation();
				return false;
			});
			// $panelheader.append($closebtn);
			$panel.append($arrow).append($panelbody);
			$panel.css('visibility', 'hidden');
			if (that.trigger == 'hover') {
				$panel.mouseenter(function(e) {
					$(this).data('in', true);
				});

				$panel.mouseleave(function(e) {
					$(this).data('in', false);
				});
			}
			$panel.bind('close', function() {
				if ($currentopenpanel) {
					$currentopenpanel.detach();
					$currentopenpanel = null;
				}
			});
			$panel.data('plugin', that);
			return $panel;
		},
		updatePosition : function() {
			if (this.$panel) {
				this.$panel.removeClass('top').removeClass('top-left').removeClass('top-right').removeClass('bottom-left').removeClass('bottom-right').removeClass('bottom');
				this.$panel.addClass(this.getPlacement());
				this.$panel.css(this.getPosition());
				this.$panel.css('visibility', 'visible');
			}
		},
		getPosition : function() {
			var that = this;
			var returnPos = {};
			var parentContainer = that.parentContainer || null;
			var pos = {
				top : that.$target.offset().top - $(window).scrollTop(),
				left : that.$target.offset().left - $(window).scrollLeft(),
				right : that.$target.outerWidth() + that.$target.offset().left,
				bottom : that.$target.outerHeight() + that.$target.offset().top,
				roRight : $(window).width() - (this.$target.outerWidth() + that.$target.offset().left)
			};

			if (!parentContainer) {
				that.$target.parents().each(function() {
					if ($(this).css('overflow') == 'auto' || $(this).css('overflow-y') == 'auto') {
						parentContainer = $(this);
						return false;
					}
				});
			}
			if (parentContainer) {
				pos.top = pos.top - parentContainer.offset().top;
				pos.left = pos.left - parentContainer.offset().left;
				pos.right = pos.right - parentContainer.offset().left;
				pos.roRight = parentContainer.offset().left + parentContainer.width() - (that.$target.outerWidth() + that.$target.offset().left);
			}

			if ((that.$panel.height() + that.offset) > pos.top) {// 飘下面
				returnPos.top = 10 + that.$target.outerHeight();
			} else {
				returnPos.top = -(10 + this.$panel.outerHeight());// 飘上面
				// returnPos.bottom = 10 + that.$target.outerHeight();
			}
			if (that.$panel.width() > pos.right) {// 飘右边或中间
				if (that.$panel.width() * 0.5 > pos.left + that.$target.outerWidth() * 0.5) {// 飘中间
					returnPos.left = that.$target.outerWidth() * 0.5 - 21;// 飘右边,21是箭头到左边边框距离
				} else {
					returnPos.left = -(that.$panel.width() * 0.5 - that.$target.outerWidth() * 0.5);
					// 飘右边
				}
			} else {// 飘左边
				if (this.$panel.width() * 0.5 <= pos.roRight) {
					returnPos.left = -(that.$panel.width() * 0.5 - that.$target.outerWidth() * 0.5);
				} else {
					returnPos.left = that.$target.outerWidth() - that.$panel.width() + (30 - that.$target.outerWidth() * 0.5);// 30是箭头到右边边框距离
				}
			}
			return returnPos;
		},
		getPlacement : function() {
			var that = this;
			var parentContainer = that.parentContainer || null;
			var pos = {
				top : that.$target.offset().top - $(window).scrollTop(),
				left : that.$target.offset().left - $(window).scrollLeft(),
				right : that.$target.outerWidth() + that.$target.offset().left,
				bottom : that.$target.outerHeight() + that.$target.offset().top,
				roRight : $(window).width() - (that.$target.outerWidth() + that.$target.offset().left)
			};

			if (!parentContainer) {
				that.$target.parents().each(function() {
					if ($(this).css('overflow') == 'auto' || $(this).css('overflow-y') == 'auto') {
						parentContainer = $(this);
						return false;
					}
				});
			}
			if (parentContainer) {
				pos.top = pos.top - parentContainer.offset().top;
				pos.left = pos.left - parentContainer.offset().left;
				pos.right = pos.right - parentContainer.offset().left;
				pos.roRight = parentContainer.offset().left + parentContainer.width() - (that.$target.outerWidth() + that.$target.offset().left);
			}
			var pv = 'top';
			var ph = 'left';
			if ((that.$panel.height() + that.offset) > pos.top) {
				pv = 'bottom';
			} else {
				pv = 'top';
			}
			if (that.$panel.width() > pos.right) {
				if (that.$panel.width() * 0.5 > pos.left + that.$target.outerWidth() * 0.5) {// 飘中间
					ph = 'right';
				} else {
					ph = '';
				}
			} else {
				if (that.$panel.width() * 0.5 <= pos.roRight) {
					ph = '';
				} else {
					ph = 'left';
				}
			}
			if (ph) {
				return pv + '-' + ph;
			} else {
				return pv;
			}
		}
	};

	$.fn.titleInfo = function(options) {
		var $target = $(this);
		if (!$target.data('bind')) {
			var titleinfo = new titleInfo($target, options);
		}
		return this;
	};

	$(function() {
		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('*[plugin-title]').each(function() {
				var config = {};
				if ($(this).data('url')) {
					config.url = $(this).data('url');
				}
				if ($(this).attr('title')) {
					config.title = $(this).attr('title');
					$(this).attr("title","");
				}
				if ($(this).data('width')) {
					config.width = $(this).data('width');
				}
				if ($(this).data('parentcontainer')) {
					config.parentContainer = $(this).data('parentcontainer');
				}
				if ($(this).data('trigger')) {
					config.trigger = $(this).data('trigger');
				}
				$(this).titleInfo(config);
			});
		});

		$(document).bind('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('*[plugin-title]').each(function() {
				var config = {};
				if ($(this).data('url')) {
					config.url = $(this).data('url');
				}
				if ($(this).attr('title')) {
					config.title = $(this).attr('title');
					$(this).attr("title","");
				}
				if ($(this).data('width')) {
					config.width = $(this).data('width');
				}
				if ($(this).data('parentcontainer')) {
					config.parentContainer = $(this).data('parentcontainer');
				}
				if ($(this).data('trigger')) {
					config.trigger = $(this).data('trigger');
				}
				$(this).titleInfo(config);
			});
		});

		$('*[plugin-title]').each(function() {
			var config = {};
			if ($(this).data('url')) {
				config.url = $(this).data('url');
			}
			if ($(this).attr('title')) {
				config.title = $(this).attr('title');
				$(this).attr("title","");
			}
			if ($(this).data('width')) {
				config.width = $(this).data('width');
			}
			if ($(this).data('parentcontainer')) {
				config.parentContainer = $(this).data('parentcontainer');
			}
			if ($(this).data('trigger')) {
				config.trigger = $(this).data('trigger');
			}
			$(this).titleInfo(config);
		});
	});
})(jQuery);