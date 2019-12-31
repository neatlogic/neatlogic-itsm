;
(function($) {
	var tmp_panel = '<div class="jquery-userinfo-panel"></div>';
	var tmp_panel_header = '<div class="jquery-userinfo-panelheader"><span class="title"></span></div>';
	var tmp_close_btn = '<i class="jquery-userinfo-closebtn fa fa-close"></i>';
	var tmp_panel_body = '<div class="jquery-userinfo-panelbody"></div>';
	var tmp_panel_arrow = '<span class="arrow"></span>';
	var $currentopenpanel = null;

	var userinfo = function(target, options) {
		this.$target = $(target);
		this.panelWidth = 500;
		this.panelHeight = 400;
		this.offset = 15;
		this.position = options.position; 
		this.$panel = null;
		this.init();
	}
	this.userinfo = userinfo;
	userinfo.prototype = {
		init : function() {
			var that = this;
			if(that.$target.attr('userid') && !$.trim(that.$target.text())){
				$.getJSON('/balantflow/user/' + that.$target.attr('userid') + '/username',function(data){
					if(data && data.userName){
						that.$target.text(data.userName);
					}
				});
			}
			that.$target.wrap('<div tabindex="-1" class="jquery-userinfo-panelcontainer"></div>');
			that.$wraper = that.$target.closest('.jquery-userinfo-panelcontainer');
			that.$target.css({
				'padding' : '0px 4px 0px 0px',
				'text-decoration' : 'underline',
				'cursor' : 'pointer',
				// 'background' :
				// 'url(/balantflow/resources/images/icons/userinfo.png)
				// no-repeat',
				'background-position' : 'center right',
				'white-space' : 'nowrap'
			});
			that.$target.append('<i class="ts ts-user" style="font-size:10px;text-decoration:none;margin-left:2px;opacity:0.8;vertical-align: middle;" />');
			that.$wraper.on('blur', function(e) {
				if ($currentopenpanel) {
					$currentopenpanel.hide();
					$currentopenpanel.remove();
					$currentopenpanel = null;
				}
			});
			
			that.$target.on('click', function(e) {
				e.stopPropagation();
				var needopen = true;
				if ($currentopenpanel) {
					if ($currentopenpanel.data('trigger') != this) {
						$currentopenpanel.hide();
						$currentopenpanel.remove();
						$currentopenpanel = null;
					} else {
						needopen = false;
						$currentopenpanel.hide();
						$currentopenpanel.remove();
						$currentopenpanel = null;
					}
				}
				if (needopen) {
					var $panel = that.generateContainer();
					that.$wraper.append($panel);
					var zindex = getMaxZ();
					$panel.css('z-index', zindex);
					that.$panel = $panel;
					$currentopenpanel = $panel;
					$currentopenpanel.data('trigger', this);
					$.getJSON('/balantflow/user/getUserInfo.json?userId=' + that.$target.attr('userid'), function(user) {
						var team = '';
						if (user.teamlist && user.teamlist.length > 0) {
							for (var t = 0; t < user.teamlist.length; t++) {
								team += user.teamlist[t].teamName;
								if (t < user.teamlist.length - 1) {
									team += '、';
								}
							}
						};
						var l = '<div class="d_f" style="text-align: center;padding-left:10px;"><span class="face_bg_midsmall"><img src="/balantflow/user/getUserFace.do?uid=' + that.$target.attr('userid') + '&type=big&t=' + (new Date()).getTime() + '"></span></div>';
						var r = '<div class="d_f_r"><ul style="margin:0px 0px 10px 0px;list-style-type:none;width:290px;padding-left:10px;">';
						var level = ''
						if (user.level > 0) {
							level = '<i class="user-level user-level-' + (6-user.level) + '"></i>';
						}
						r += '<li style="text-align:left">${tk:lang("账号")}：' + user.userid + '</li>';
						r += '<li style="text-align:left">${tk:lang("姓名")}：' + level + user.username + '</li>';
						r += '<li style="text-align:left">${tk:lang("邮箱")}：' + (user.email || ' - ') + '</li>';
						r += '<li style="text-align:left">${tk:lang("座机")}：' + (user.userNumber || ' - ') + '</li>';
						r += '<li style="text-align:left">${tk:lang("手机")}：' + (user.phone || ' - ') + '</li>';
						r += '<li style="text-align:left">${tk:lang("公司")}：' + (user.company || ' - ') + '</li>';
						r += '<li style="text-align:left">${tk:lang("组织")}：' + (user.organization || ' - ') + '</li>';
						r += '<li style="text-align:left">${tk:lang("职位")}：' + (user.position || ' - ') + '</li>';
						if (team != '') {
							r += '<li style="text-align:left;word-break:break-all;white-space: pre-wrap;">${tk:lang("分组")}：' + team + '</li>';
						}
						r += '</ul></div>';
						var clear = '<div style="clear"></div>';
						var content = '<div style="padding:5px">' + l + r + clear + "</div>";
						that.$panel.find('.jquery-userinfo-panelbody').html(content);
						that.updatePosition();
						if (that.position) {
							that.$panel.css('position', that.position);
						}
					});
				}
				e.stopPropagation();
			});	
			$(window).on('resize', function() {
				if ($currentopenpanel) {
					$currentopenpanel.remove();
				}
			});
			that.windowscrolltop=$(window).scrollTop();
			$(window).on('scroll', function() {
				if(that.windowscrolltop!=$(window).scrollTop()){
					that.windowscrolltop=$(window).scrollTop();
					if ($currentopenpanel) {
						$currentopenpanel.remove();
					}
				}
			});
			that.$target.attr('data-bind', true);
		},
		generateContainer : function() {
			var that = this;
			var $panelheader = $(tmp_panel_header);
			var $panelbody = $(tmp_panel_body);
			var $panel = $(tmp_panel);
			var $arrow = $(tmp_panel_arrow);
			var $closebtn = $(tmp_close_btn);
			$closebtn.on('click', function(e) {
				e.stopPropagation();
				if ($currentopenpanel) {
					$currentopenpanel.remove();
					$currentopenpanel = null;
				}
			});
			$panelheader.append($closebtn);
			$panel.append($arrow).append($panelheader).append($panelbody);
			$panel.css('visibility', 'hidden');
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
			var parentContainer = null;
			var pos = {
				top : that.$target.offset().top - $(window).scrollTop(),
				left : that.$target.offset().left - $(window).scrollLeft(),
				right : that.$target.outerWidth() + that.$target.offset().left,
				bottom : that.$target.outerHeight() + that.$target.offset().top,
				roRight : $(window).width() - (that.$target.outerWidth() + this.$target.offset().left)
			};
			if(!that.position || that.position == 'absolute'){
				that.$target.parents().each(function(){
					if($(this).css('overflow') == 'auto' ||$(this).css('overflow-y') == 'auto'){
						that.parentContainer = $(this);
						return false;
					}
				});
			}
			
			if(that.parentContainer  && that.position != 'fixed'){ 
				pos.top = pos.top - that.parentContainer.offset().top;
				pos.left = pos.left - that.parentContainer.offset().left;
				pos.right = pos.right - that.parentContainer.offset().left;
				pos.roRight = that.parentContainer.offset().left + that.parentContainer.width() - (that.$target.outerWidth() + that.$target.offset().left);
			}
			if(!that.position || that.position == 'absolute'){
				if ((that.$panel.height() + that.offset) > pos.top) {// 飘下面
					returnPos.top = 10 + that.$target.outerHeight();
				} else {
					returnPos.bottom = 10 + that.$target.outerHeight();// 飘上面
				}
				if (that.$panel.width() > pos.right) {// 飘右边或中间
					if (that.$panel.width() / 2 > pos.left + that.$target.outerWidth() / 2) {// 飘中间
						returnPos.left = that.$target.outerWidth() / 2 - 21;// 飘右边,21是箭头到左边边框距离
					} else {
						returnPos.left = -(this.$panel.width() / 2 - that.$target.outerWidth() / 2);
						// 飘右边
					}
				} else {// 飘左边
					if(that.$panel.width() / 2 <= pos.roRight){
						returnPos.left = -(that.$panel.width() / 2 - that.$target.outerWidth() / 2);
					}else{
						returnPos.left = that.$target.outerWidth() - that.$panel.width() + (30 - that.$target.outerWidth() / 2);// 30是箭头到右边边框距离
					}
				}				
			} else if (that.position == 'fixed') {
				if ((that.$panel.height() + that.offset) > pos.top) {// 飘下面
					returnPos.top = 10 + that.$target.outerHeight() + that.$target.offset().top - $(window).scrollTop();
				} else {
					returnPos.bottom = 10 + $(window).height()-that.$target.offset().top + $(window).scrollTop();// 飘上面
				}
				if (that.$panel.width() > pos.right) {// 飘右边或中间
					if (that.$panel.width() / 2 > pos.left + that.$target.outerWidth() / 2) {// 飘中间
						returnPos.left =  that.$target.offset().left + that.$target.outerWidth() / 2 - 21;// 飘右边,21是箭头到左边边框距离
					} else {
						returnPos.left = that.$target.offset().left -(that.$panel.width() / 2 - that.$target.outerWidth() / 2);
						// 飘右边
					}
				} else {// 飘左边
					if(that.$panel.width() / 2 <= pos.roRight){
						returnPos.left = that.$target.offset().left -(that.$panel.width() / 2 - that.$target.outerWidth() / 2);
					}else{
						returnPos.left = that.$target.offset().left + that.$target.outerWidth() - that.$panel.width() + (30 - that.$target.outerWidth() / 2);// 30是箭头到右边边框距离
					}
				}				
			}
			return returnPos;
		},
		getPlacement : function() {
			var that = this;
			var pos = {
				top : this.$target.offset().top - $(window).scrollTop(),
				left : this.$target.offset().left - $(window).scrollLeft(),
				right : this.$target.outerWidth() + this.$target.offset().left,
				bottom : this.$target.outerHeight() + this.$target.offset().top,
				roRight : $(window).width() - (this.$target.outerWidth() + this.$target.offset().left)
			};
			
			that.$target.parents().each(function(){
				if($(this).css('overflow') == 'auto' ||$(this).css('overflow-y') == 'auto'){
					that.parentContainer = $(this);
					return false;
				}
			});
			
			if(that.parentContainer && that.position != 'fixed'){
				pos.top = pos.top - that.parentContainer.offset().top;
				pos.left = pos.left - that.parentContainer.offset().left;
				pos.right = pos.right - that.parentContainer.offset().left;
				pos.roRight = that.parentContainer.offset().left + that.parentContainer.width() - (this.$target.outerWidth() + this.$target.offset().left);
			}
			var pv = 'top';
			var ph = 'left';
			if ((this.$panel.height() + this.offset) > pos.top) {
				pv = 'bottom';
			} else {
				pv = 'top';
			}
			if (this.$panel.width() >  pos.right) {
				if (this.$panel.width() / 2 > pos.left + this.$target.outerWidth() / 2) {// 飘中间
					ph = 'right';
				} else {
					ph = '';
				}
			} else {
				if(this.$panel.width() / 2 <= pos.roRight){
					ph = '';	
				}else{
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

	$.fn.userinfo = function(options) {
		var $target = $(this);
		if (!$target.data('bind') && $target.attr('userid')) {
			new userinfo($target, options);
		}
		return this;
	};

	$(function() {

		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('.userInfo').each(function() {
				var options = {};
				if ($(this).data('position')) {
					options['position'] = $(this).data('position');
				}
				$(this).userinfo(options);
			});
		});

		$(document).on('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('.userInfo').each(function() {
				var options = {};
				if ($(this).data('position')) {
					options['position'] = $(this).data('position');
				}
				$(this).userinfo(options);
			});
		});

		$('.userInfo').each(function() {
			var options = {};
			if ($(this).data('position')) {
				options['position'] = $(this).data('position');
			}
			$(this).userinfo(options);  
		});
	});

	
	
	
	
})(jQuery);