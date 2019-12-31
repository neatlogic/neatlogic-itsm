;
(function($) {
	var scrolltable = function(target) {
		var that = this;
		that.minBtn = 10;
		that.$target = $(target);
		that.hasParent = that.$target.data('hasparent') ? false : true;  //如有data-hasparent属性，则有固定高度的父级标签存在，table在父级标签内滚动，默认有(当设置了data-onlyfix时，此设置无效)
		that.hasTop = that.$target.data('hastop') ? true : false;	//如有data-hastop属性，则当前table触发的固定表头的位置会以当前位置为准，用于顶部还有actionbar需固定位置的布局，默认无
		that.onlyFix = that.$target.data('onlyfix') ? true : false;  //如有data-onlyfix属性，则当前table只触发固定表头事件，没有table内部滚动条（用于减少数据量大的table，默认无
		//初始化table外层用于定位offsetparent的标签（减少插件外元素的样式和可控制定位元素的宽高等属性）
		that.$target.wrap('<div class="jquery-scrolltable-container"></div>');
		that.$container = that.$target.closest('.jquery-scrolltable-container');
		that.$container.wrap('<div class="jquery-scrolltable-container-outer"></div>');
		that.$offsetparent = that.$target.closest('.jquery-scrolltable-container-outer');
		that.$target.addClass('jquery-scrolltable');
		//新增窗口滚动单纯固定表头
		if(!that.onlyFix){
			//左右滚动条
			that.$scrollxBar = $('<div class="jquery-scrolltable-scrollxbox"></div>');
			that.$scrollxBtn = $('<div class="jquery-scrolltable-scrollxbtn"></div>');
			that.$scrollyBar = $('<div class="jquery-scrolltable-scrollybox"></div>');
			that.$scrollyBtn = $('<div class="jquery-scrolltable-scrollybtn"></div>');
			that.$scrollxBar.append(that.$scrollxBtn);
			that.$scrollyBar.append(that.$scrollyBtn);
			that.$offsetparent.append(that.$scrollxBar).append(that.$scrollyBar);	
			that.init();
			that.isLeave = false; 
			that.isWheel = false;
			that.step = 15;
		}else{
			that.initFixonly();
		}
	};
	this.scrolltable = scrolltable;
	scrolltable.prototype = {
		initFixonly: function(){
			var that = this;
			$(window).on('scroll resize', function() {
	        	that.fixThead();
			});
		},
		init : function() {
			var that = this;
			that.isDragx = false;
			that.isDragy = false;
			that.locateBtn();
			//x轴、y轴滚动条拖动
			that.$scrollxBtn.on('mousedown', function(e) {
				that.goScrollx(e);
			});
			if(that.hasParent){
				that.$scrollyBtn.on('mousedown', function(e) {
					that.goScrolly(e);
				});				
			}
			//x轴、y轴滚动条点击快速定位
			that.$scrollxBar.on('click', function(e) {
				that.gofastScrollx(e);
			});
			that.$scrollxBtn.on('click', function(e) {
				e.stopPropagation();
			});
			if(that.hasParent){
				that.$scrollyBar.on('click', function(e) {
					that.gofastScrolly(e);
				});	
				that.$scrollyBtn.on('click', function(e) {
					e.stopPropagation();
				});
			}
			//窗口变化时初始化插件相关参数
			$(window).on('resize', function() {
				that.locateBtn();
				that.scroll();
			});
			that.$target.on('resize', function() {
				that.locateBtn();
				that.scroll();
			});
			//鼠标移上时初始化定位参数
			that.$container.on('mouseenter', function() {
				that.locateBtn();
				that.scroll();
			});			
			
			//滚动事件（绑定在table外层而非offsetparent减少滚动条的跟随滚动事件和控制table本身滚动的外层）
			if(that.hasParent){
				that.$container.on('scroll', function() {
					that.scroll();
				});				
			}else{
				$(window).on('scroll', function() {
					that.scroll();
				});				
			}
			//修复由于bootstrap的dropdown插件定位导致的table宽高溢出和高度不够的问题
				that.$target.find('.dropdown-toggle').parent().on('shown.bs.dropdown', function() {
	                var $this = $(this).find('.dropdown-toggle');
	                if(that.$container.css('overflow') != 'visible'){
						if(that.$container.css('overflow-y') == 'hidden'){
							if((($this.siblings('ul').outerHeight()  + 40) > (that.$target.outerHeight() - that.$container.scrollTop() - $(this).position().top)) &&  (($this.siblings('ul').outerHeight()  + 40)>$(this).position().top)){
								if(!$(this).hasClass('dropup')){
						    		  $(this).addClass('dropup');
						    	  }					
							}else{
								$(this).removeClass('dropup'); 
							}					
						}else{
							if((($this.siblings('ul').outerHeight()  + 40) < ($(window).height() - $(window).scrollTop() - $(this).offset().top))||(($this.siblings('ul').outerHeight()  + 40)>$(this).position().top)){
								$(this).removeClass('dropup');
								that.$container.height(Math.max(that.$container.height(),$this.siblings('ul').outerHeight()+$(this).position().top+$(this).outerHeight()+6));
							}else{
								$(this).addClass('dropup');
							}
							that.locateBtn();
						}	                	
	                }
				});
				that.$target.find('.dropdown-toggle').parent().on('hidden.bs.dropdown', function() {
					 var $this = $(this).find('.dropdown-toggle');
					 if(that.$container.css('overflow') != 'visible'){
							if(that.$container.css('overflow-y') != 'hidden'){
								if(($this.siblings('ul').outerHeight()  + 40) < ($(window).height() - $(window).scrollTop() - $(this).offset().top)){
									that.$container.height('auto');
								}
								that.locateBtn();
							}						 
					 }

				});				
			//鼠标滚动事件
			if(that.hasParent){
				that.$offsetparent.bind('mousewheel DOMMouseScroll', function(event,delta) {
					event.stopPropagation();
					var barT = that.$scrollyBtn.position().top;
					if(!delta){
						delta = -event.originalEvent.wheelDelta || event.originalEvent.detail;
					}
					if (that.$container[0].scrollHeight >that.$container.innerHeight()) {
						if (!that.isWheel) {
							if (delta > 0 && that.$container.scrollTop() != 0) {
								that.isWheel = true;
								that.$scrollyBtn.css({
									'top' : Math.max(0, that.$container.scrollTop() - that.step*delta) * (that.outerHeight - that.$scrollyBtn.outerHeight() )/(that.innerHeight - that.outerHeight)
								}); 
								that.$container.scrollTop(Math.max(0, that.$container.scrollTop() - that.step*delta));
								that.isWheel = false;
							} else if (delta < 0 && (that.$container[0].scrollHeight - that.$container.innerHeight()) > that.$container.scrollTop()) {
								that.isWheel = true;
								that.$scrollyBtn.css({
									'top' : Math.min(Math.min(that.$container.scrollTop() - that.step*delta, that.$container[0].scrollHeight - that.$container.innerHeight())  * (that.outerHeight - that.$scrollyBtn.outerHeight()) / (that.innerHeight - that.outerHeight) ,that.outerHeight - that.$scrollyBtn.outerHeight())
								});
								that.$container.scrollTop(Math.min(that.$container.scrollTop() - that.step*delta, that.$container[0].scrollHeight - that.$container.innerHeight()));
								that.isWheel = false;
							}
						}
					}
					event.preventDefault();
				});				
			}

		},
		scroll : function() {
			var that = this;
			var myTop = that.$container.scrollTop()-1;
			var offTop = 0;
			if(!that.hasParent){
				myTop = $(window).scrollTop() - that.$container.offset().top-1;
				that.$scrollxBar.css({
					'bottom' : Math.max($(window).height()+myTop- that.$container.height(),4),
				});
			}
			//当有actionbar需固定位置的顶部时table固定在初始化位置而非顶部,调整为滚动超过操作栏的高度时才滚动
			if(that.hasTop){
				myTop = $(window).scrollTop()- that.$container.offset().top + $('.jquery-scrollactionbar-container').outerHeight()-1;
				offTop =$('.jquery-scrollactionbar-container').outerHeight();
			}
			var head = that.$target.children('thead').find('th');
			//滚动事件发生时table内部thead随滚动定位到offsetparent的顶部（hasfix是区别滚动table发生的标志调整thead样式）
			if (myTop > offTop) {
				head.css({
					'transform' : 'translate3d(0px,' + myTop + 'px,0px)',
					'-ms-transform' : 'translate3d(0px,' + myTop + 'px,0px)',
					'-moz-transform' : 'translate3d(0px,' + myTop + 'px,0px)',
					'-webkit-transform' : 'translate3d(0px,' + myTop + 'px,0px)',
					'-o-transform' : 'translate3d(0px,' + myTop + 'px,0px)'
				});
				that.$target.addClass('hasfix');
			} else {
				head.css({
					'transform' : 'none',
					'-ms-transform' : 'none',
					'-moz-transform' : 'none',
					'-webkit-transform' : 'none',
					'-o-transform' : 'none'
				});
				that.$target.removeClass('hasfix');
			}
		},
		fixThead :function(){
			var that = this;
			var myTop = $(window).scrollTop() - that.$container.offset().top-1;
			var offTop = that.$container.offset().top;
			var head = that.$target.children('thead').find('th');
			//当有actionbar需固定位置的顶部时table固定在初始化位置而非顶部
			if(that.hasTop){
				myTop = $(window).scrollTop()- that.$container.offset().top-2 +$('.jquery-scrollactionbar-container').innerHeight();
				offTop =0;
			}
			if (myTop > offTop) {
				head.css({
					'transform' : 'translate3d(0px,' + myTop + 'px,0px)',
					'-ms-transform' : 'translate3d(0px,' + myTop + 'px,0px)',
					'-moz-transform' : 'translate3d(0px,' + myTop + 'px,0px)',
					'-webkit-transform' : 'translate3d(0px,' + myTop + 'px,0px)',
					'-o-transform' : 'translate3d(0px,' + myTop + 'px,0px)'
				});
				that.$target.addClass('hasfix');
			} else {
				head.css({
					'transform' : 'none',
					'-ms-transform' : 'none',
					'-moz-transform' : 'none',
					'-webkit-transform' : 'none',
					'-o-transform' : 'none'
				});
				that.$target.removeClass('hasfix');
			}			
		},
		locateBtn : function() {
			var that = this;
			//初始化外层container的高度（由固定高度调整为最大高度）
			var siblingsH = 0;
			for(var i = 0; i<that.$offsetparent.siblings().length;i++){
				siblingsH = siblingsH +that.$offsetparent.siblings().eq(i).outerHeight();
			}
			that.innerWidth = that.$target.outerWidth();
			that.outerWidth = that.$container.outerWidth();
			//根据不同table的溢出情况初始化隐藏和滚动条是否显示
			if (that.innerWidth > that.outerWidth) {
				that.$container.css('overflow-x', 'hidden');
				that.$scrollxBar.show();
			} else {
				that.$container.css('overflow-x', 'visible');
				that.$scrollxBar.hide();
			}
			if(that.hasParent){
				that.$container.css({
					'max-height' : that.$offsetparent.parent().innerHeight()-siblingsH - 10
				});	
				that.innerHeight = that.$target.outerHeight();
				that.outerHeight = that.$container.outerHeight();
				if(that.innerHeight > that.outerHeight){
					that.$container.css('overflow-y','hidden');
					that.$scrollyBar.show();
				}else{
					that.$container.css('overflow-y','visible');
					that.$scrollyBar.hide();
				}	
				that.$scrollyBtn.css({
					'height' : Math.max((that.outerHeight-that.minBtn)*that.outerHeight/that.innerHeight,that.minBtn)
				});
				//that.step = Math.min(Math.max(100,(that.outerHeight-that.minBtn)*80/that.innerHeight),0.5*that.innerHeight);
			}else{
				//当滚动范围为窗口时固定x轴滚动条位置为table底部
				that.$scrollxBar.addClass('fixed');
				that.$scrollxBar.css({
					'bottom' : Math.max($(window).height()+$(window).scrollTop() - that.$container.offset().top - that.$container.height(),4),
					'width' : that.outerWidth,
					'left' : that.$container.offset().left-$(window).scrollLeft()
				});				
			}
			//初始化滚动条的不同宽高（按最小10、最大为内外container的等比例距离）
			that.$scrollxBtn.css({
				'width' : Math.max((that.outerWidth-that.minBtn)*that.outerWidth/that.innerWidth,that.minBtn)
			});
		},
		goScrollx : function(e) {
			var that = this;
			e.stopPropagation();
			//x轴方向的拖拽，限制了滚动条的范围，left由原百分比调整为数值（其一与其他计算方法统一，其二百分比到一定比例时浏览器来回折算误差比较大）
			if (!that.isDragx) {
				that.isDragx = true;
				var formalL = that.$scrollxBtn.position().left;
				var deltaX = e.clientX;
				that.$scrollxBar.addClass('active');
				$(document).bind('mousemove', function(e) {
					e.preventDefault();
					if (that.isDragx) {
						if (!e) {
							e = window.event;
						}
						var nx = e.clientX ;
						var scrollL = Math.min(Math.max(formalL + nx - deltaX , 0), that.outerWidth - that.$scrollxBtn.outerWidth());
						that.$scrollxBar.addClass('active');
						that.$scrollxBtn.css({
							'left' : scrollL
						});
						that.$container.scrollLeft((that.innerWidth - that.outerWidth) * scrollL/ (that.outerWidth-that.$scrollxBtn.outerWidth()));
						nx = null;
						scrollL = null;
					}
				});
				$(document).bind('mouseup mouseleave', function(e) {
					if(that.isDragx){
						that.isDragx = false;
						deltaX = null;
						formalL = null;	
						that.$scrollxBar.removeClass('active');
					}
				});
			} 
		},
		goScrolly : function(e) {
			var that = this;
			e.stopPropagation();
			//x轴方向的拖拽，限制了滚动条的范围，top由原百分比调整为数值（其一与鼠标滚动计算方法统一，其二百分比到一定比例时浏览器来回折算误差比较大）
			if (!that.isDragy) {
				that.isDragy = true;
				var formalT = that.$scrollyBtn.position().top;
				var deltaY = e.clientY ;
				that.$scrollyBar.addClass('active');
				$(document).bind('mousemove', function(e) {
					if (that.isDragy) {
						if (!e) {
							e = window.event;
						}
						e.preventDefault();
						var ny = e.clientY;
						var scrollT = Math.min( Math.max(formalT + ny - deltaY,0), that.outerHeight-that.$scrollyBtn.outerHeight());
						that.$scrollyBar.addClass('active');
						that.$scrollyBtn.css({
							'top' : scrollT
						});
						that.$container.scrollTop((that.innerHeight - that.outerHeight) * scrollT /(that.outerHeight-that.$scrollyBtn.outerHeight()) );
						ny = null;
						scrollT	= null;
					}
				});
				$(document).bind('mouseup mouseleave', function(e) {
					if(that.isDragy){
						that.isDragy = false;
						deltaY = null;
						formalT = null;	
						that.$scrollyBar.removeClass('active');
					}
				});
			} 
		},
		gofastScrollx :function(e){
			var that = this;
			if (!that.isDragx) {
				var deltaX = e.clientX - that.$scrollxBar.offset().left + $(window).scrollLeft();
				var halfW = that.$scrollxBtn.position().left>deltaX ? 0 : that.$scrollxBtn.outerWidth();
				var scrollL = Math.min(Math.max(deltaX-halfW, 0), that.outerWidth - that.$scrollxBtn.outerWidth());
				that.$scrollxBtn.css({
					'left' : scrollL
				});
				that.$container.scrollLeft((that.innerWidth - that.outerWidth) * scrollL/ (that.outerWidth-that.$scrollxBtn.outerWidth()));
				deltaX = null;
				scrollL = null;	
				halfW = null;
			}

		},
		gofastScrolly :function(e){
			var that = this;
			if (!that.isDragy) {
				var deltaY = e.clientY - that.$scrollyBar.offset().top + $(window).scrollTop();
				var halfH = that.$scrollyBtn.position().top>deltaY ? 0 : that.$scrollyBtn.outerHeight();
				var scrollT = Math.min( Math.max(deltaY-halfH,0), that.outerHeight-that.$scrollyBtn.outerHeight());
				that.$scrollyBtn.css({
					'top' : scrollT
				});
				that.$container.scrollTop((that.innerHeight - that.outerHeight) * scrollT /(that.outerHeight-that.$scrollyBtn.outerHeight()) );
				deltaY = null;
				scrollT = null;				
			}
		}
	};

	$.fn.scrolltable = function() {
			var $target = $(this);
			if (!$target.attr('bind-scrolltable')) {
				var c = new scrolltable($target);
				$target.attr('bind-scrolltable', true);
			}
		return this;
	};

	$(function() {
		if( 'MozTransform' in document.documentElement.style || 'WebkitTransform' in  document.documentElement.style || 'OTransform' in document.documentElement.style  || 'msTransform' in document.documentElement.style){
			$('table[plugin-scrolltable]').each(function() {
				$(this).scrolltable();
			});

			$(document).bind("ajaxComplete", function(e, xhr, settings) {
				$('table[plugin-scrolltable]').each(function() {
					$(this).scrolltable();
				});
			});

			$(document).on('xdoTRender', function(e, content) {
				var $content = $(content);
				$content.find('table[plugin-scrolltable]').each(function() {
					$(this).scrolltable();
				});
			});			
		}
	});
})(jQuery);



