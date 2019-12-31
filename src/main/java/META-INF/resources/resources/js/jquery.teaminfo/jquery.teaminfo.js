;
(function($) {
	var teaminfo = function(target) {
		this.$target = $(target);
		this.panelWidth = 500;
		this.panelHeight = 400;
		this.offset = 15;
		this.teamids = this.$target.attr('teamid');
		this.teamname = this.$target.attr('teamname');
		this.position = this.$target.data('position') || 'absolute';
		this.parentContainer = null;//外层指定的滚动容器
		this.$target.addClass('teaminfo-text');
		this.init();
	}
	this.teaminfo = teaminfo;
	teaminfo.prototype = {
		init : function() {
			var that = this;
			that.$target.wrap('<div class="teaminfo-container" tabindex="-1"></div>');
			that.$target.append('<i class="ts-team teaminfo-icon"></i>');
			that.$container = that.$target.closest('.teaminfo-container');
			that.$target.click(function(e){
				e.stopPropagation();
				if(that.$dropdown){
					that.toggleShow('hide');
				}else{
					that.toggleShow('show');
					
				}
			});
			that.$container.on('blur', function(e){
				if(that.$dropdown){
					that.toggleShow('hide');
				}
			});	
			$(window).on('resize', function() {
				if(that.$dropdown){
					that.toggleShow('hide');
				}
			});
			
			that.windowscrolltop=$(window).scrollTop();//工单中表单改变高度时会触发这个事件，需要过滤掉
			$(window).on('scroll', function() {
				if(that.windowscrolltop!=$(window).scrollTop()){
					that.windowscrolltop=$(window).scrollTop();
					if(that.$dropdown){
						that.toggleShow('hide');
					}
				}
			});
		},
		toggleShow: function(type){
			var that =this;
			if(type == 'hide'){
				if(that.$dropdown){
					that.$dropdown.remove();
					that.$dropdown = null;
				}
			}else{
				that.$container.append('<div class="teaminfo-dropdown" style="opacity:0;z-index:999;"></div>');
				that.$dropdown = that.$container.find('.teaminfo-dropdown');
				$.getJSON('/balantflow/user/getUserListByTeamsPage.json?pageSize=16&teamids='+that.teamids,function(data){
					if(data && that.$dropdown){
						data.teamname = that.teamname;
						data.teamids = that.teamids;
						that.$dropdown.html(xdoT.render('common.plugin.teaminfo.showteamlist',data));	
						that.updatePosition();
						that.$dropdown.click(function(e){
							e.stopPropagation();
						});						
					}
				});
			}
		},
		updatePosition: function(){
			var that = this;
			var result = this.getPosition();
			that.$dropdown.css(result.returnPos);
			that.$dropdown.addClass(result.directionClass);
			that.$dropdown.css('opacity','1');
		},
		getPosition:function(){
			var that = this;
			if(!this.parentContainer){
				that.$target.parents().each(function(){
					if($(this).css('overflow') == 'auto' ||$(this).css('overflow-y') == 'auto'){
						that.parentContainer = $(this);
						return false;
					}
				});
			}
			
			//相对于window的滚动判断
			var pos= {
					top:that.$target.offset().top - $(window).scrollTop(),
					left:that.$target.offset().left - $(window).scrollLeft(),
					outerWidth:that.$dropdown.outerWidth(),
				    outerHeight:that.$dropdown.outerHeight()
			};
			pos.right=$(window).outerWidth()- pos.left;
			pos.bottom=$(window).outerHeight()- pos.top;
			
			if(that.parentContainer && that.position != 'fixed'){//如果外层有滚动的容器，需要通过滚动容器判断对应的值
				pos.top=pos.top - that.parentContainer.offset().top;
				pos.left=pos.left - that.parentContainer.offset().left;
				pos.bottom=that.parentContainer.outerHeight() - pos.top;
				pos.right=that.parentContainer.outerWidth() -pos.left;
			};
			
			
			//不能通过class来控制，通过top left来控制
			var returnPos={},direction='';
            if(!that.position || that.position == 'absolute'){
            	if(pos.bottom>pos.outerHeight+10){//往下飘
            		returnPos.top = that.$target.outerHeight();
            		direction=direction + ' bottom';
            	}else{//往上飘
            		returnPos.top = 0-pos.outerHeight-10;
            		direction=direction + ' top';
            	}
            	
            	if((pos.left>pos.outerWidth/2) && (pos.right>pos.outerWidth/2)){//往中间飘
            		returnPos.left =-pos.outerWidth/2;
            		direction=direction + ' center';
            	}else if(pos.left+10<pos.outerWidth){//往右飘 
            		returnPos.left =0;
            		direction=direction + ' right';
            	}else{//往左飘
            		returnPos.left =pos.outerWidth+that.$target.outerWidth()/2 +10;
            		direction=direction + ' left';
            	}
			}else if(that.position == 'fixed'){
				that.$dropdown.addClass('fixed');

            	if(pos.bottom>pos.outerHeight+10){//往下飘
            		returnPos.top = that.$target.offset().top +that.$target.outerHeight() - $(window).scrollTop();
            		direction=direction + ' bottom';
            	}else{//往上飘
            		returnPos.top = that.$target.offset().top-pos.outerHeight - $(window).scrollTop()-10;
            		direction=direction + ' top';
            	}
            	
            	if((pos.left>pos.outerWidth/2) && (pos.right>pos.outerWidth/2)){//往中间飘
            		returnPos.left =pos.left-pos.outerWidth/2;
            		direction=direction + ' center';
            	}else if(pos.left<pos.outerWidth+10){//往右飘 
            		returnPos.left =pos.left;
            		direction=direction + ' right';
            	}else{//往左飘
            		returnPos.left =pos.left- pos.outerWidth + that.$target.outerWidth()/2 +10;
            		direction=direction + ' left';
            	}
			}
			
			return {returnPos:returnPos,directionClass:direction};
		}
	};

	$.fn.teaminfo = function() {
		var $target = $(this);
		if (!$target.attr('bind-teaminfo') && $target.attr('teamid')) {
			new teaminfo($target);
			$target.attr('bind-teaminfo', true);
		}
		return this;
	};

	$(function() {
		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('.teamInfo').each(function() {
				$(this).teaminfo();
			});
		});

		$(document).on('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('.teamInfo').each(function() {
				$(this).teaminfo();
			});
		});

		$('.teamInfo').each(function() {
			$(this).teaminfo();  
		});
	});
})(jQuery);