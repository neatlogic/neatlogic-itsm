;
(function($) {
	var scrolltable = function(target,config) {
		var that = this;
		that.defaultconfig = {
            height: null,				// 默认是外层高度的100%,如需要具体高度，需要带单位
            fixedColumn: 0,				// 需要固定第几列，默认0无需固定
            hasContainer: false,    	// 默认父级容器是body，如设置此属性为true会取table的上一层标签作为父级容器（通过窗口滚动计算的top值带抖动）
            autoScroll: false,			// 是否自动滚动，默认false
            showcolumnList: [],     	// 显示的列，默认是空
            ishideColumn:false,			// 是否需要隐藏列，默认否
            columnList:[],				// 所有列
            localStorage:'tableorder',	//存储的item名字
			saveorderFn: null,			//点击保存排序弹窗后的回调方法，设为同步需返回true或者false以便保存本地缓存和关闭弹窗
			hasTop : false
		};
		that.config = $.extend({}, that.defaultconfig, config);
		that.$target = target;
		that.$target.addClass('jquery-scrolltable-target');
		if(that.config.height){
			that.config.hasContainer =true;
		}
		that.init();		
	};
	this.scrolltable = scrolltable;
	scrolltable.prototype = {
		init: function() {
			var that = this;
			that.$target.wrap('<div class="jquery-scrolltable-container tsscroll-container"'+ (that.config.height ? ' style="height:'+that.config.height+'"' :'') +'></div>');
			that.$wrapper = that.$target.closest('.jquery-scrolltable-container');
			that.$wrapper.wrap('<div class="jquery-scrolltable-wrapper"></div>');
			that.$outer = that.$wrapper.closest('.jquery-scrolltable-wrapper');
			that.draging = false;
			that.$thead = that.$target.find('thead');
			if(that.config.columnList && that.config.columnList.length>0){
				that.config.ishideColumn = true;
			}			
			if(that.config.ishideColumn){
				that.$outer.append('<div class="ts-cog-s jquery-scrolltable-setting text-primary"></div>');
				that.$settingbtn = that.$outer.find('.jquery-scrolltable-setting');
				that.$settingbtn.click(function(){
					var html = xdoT.render('common.plugin.scrolltable.editcolumn',{'columnList':that.config.columnList,'showcolumnList':that.config.showcolumnList});
					createModalDialog({
						msgtitle : '设置显示字段',
						msgcontent : html,
						msgwidth : 'large',
						checkReturn : true,
						blurclose:false,
						top: 40,
						successFuc : function() {
							if($('.jquery-scrolltable-sortol').find('li') &&$('.jquery-scrolltable-sortol').find('li').length>0){
								var sortorder =[];
								var sortli = $('.jquery-scrolltable-sortol').find('li');
								for(var j =0 ;j<sortli.length;j++){
									sortorder.push({
										'value':sortli.eq(j).data('value'),
										'defaultSelect':sortli.eq(j).data('default'),
										'canEdit':sortli.eq(j).data('canedit'),
										'isSelected':true
									});
								}
								if(that.config.saveorderFn && typeof that.config.saveorderFn == 'function'){
									localStorage.setItem(that.config.localStorage, JSON.stringify(sortorder));
									that.config.saveorderFn();
									return true;
								}
							}
						}
					});
				});
			}
			that.$outer.append('<div class="jquery-scrolltable-scrollbar"><span class="jquery-scrolltable-scrollbtn"></span></div>');
			that.$scrollbar = that.$outer.find('.jquery-scrolltable-scrollbar');
			that.$scrollbtn = that.$outer.find('.jquery-scrolltable-scrollbtn');
			that.initScrollbar();
			
			if(that.config.hasContainer){
				that.$wrapper.scroll(function(e){
					that.$thead.scrollLeft(that.$wrapper.scrollLeft());
					that.$scrollbtn.css('left',that.$wrapper.scrollLeft()* that.$wrapper.outerWidth()/that.$target.width());
					if(that.$wrapper.scrollTop()>0){
						that.$target.addClass('onfixed');
						that.transformTh(that.$thead.find('th'),(that.$wrapper.scrollTop()-2)+'px');
					}else{
						that.$target.removeClass('onfixed');
						that.transformTh(that.$thead.find('th'),'none');
					}
					that.config.ishideColumn && that.$settingbtn.css('top',1);
					that.initScrollbar();
				});
				$(window).resize(function(e){
					that.$wrapper.scrollLeft(0);
					that.$scrollbtn.css('left',that.$wrapper.scrollLeft()* that.$wrapper.outerWidth()/that.$target.width());
					if(that.$wrapper.scrollTop()>0){
						that.$target.addClass('onfixed');
						that.transformTh(that.$thead.find('th'),(that.$wrapper.scrollTop()-2)+'px');
					}else{
						that.$target.removeClass('onfixed');
						that.transformTh(that.$thead.find('th'),'none');
					}
					that.config.ishideColumn && that.$settingbtn.css('top',1);
					that.initScrollbar();
				});	
				that.$target.resize(function(e){
					that.$scrollbtn.css('left',that.$wrapper.scrollLeft()* that.$wrapper.outerWidth()/that.$target.width());
					if(that.$wrapper.scrollTop()>0){
						that.$target.addClass('onfixed');
						that.transformTh(that.$thead.find('th'),(that.$wrapper.scrollTop()-2)+'px');
					}else{
						that.$target.removeClass('onfixed');
						that.transformTh(that.$thead.find('th'),'none');
					}
					that.config.ishideColumn && that.$settingbtn.css('top',1);
					that.initScrollbar();				
				});
			}else{
				if(that.$wrapper.offset().top < $(window).scrollTop()){
					that.config.ishideColumn && that.$settingbtn.css('top', $(window).scrollTop()- that.$wrapper.offset().top+1);
				}else{
					that.config.ishideColumn && that.$settingbtn.css('top',2);
				}
				$(window).scroll(function(e){
					that.positonTop = (that.config.hasTop && $('.jquery-scrollactionbar-container').length>0) ? $('.jquery-scrollactionbar-container').outerHeight(): 0 ;
					if((that.$wrapper.offset().top-that.positonTop < $(window).scrollTop()) && $(window).scrollTop()>0){
						that.$target.addClass('onfixed');
						that.transformTh(that.$thead.find('th'),($(window).scrollTop()+that.positonTop-that.$wrapper.offset().top-2)+'px');
						that.config.ishideColumn && that.$settingbtn.css('top', $(window).scrollTop()+that.positonTop- that.$wrapper.offset().top+1);
					}else{
						that.$target.removeClass('onfixed');
						that.transformTh(that.$thead.find('th'),'none');
						that.config.ishideColumn && that.$settingbtn.css('top', 2);
					}
					that.initScrollbar();
				});
				that.$wrapper.scroll(function(e){
					that.$outer.addClass('active');
					that.$scrollbtn.css('left',that.$wrapper.scrollLeft()* that.$wrapper.outerWidth()/that.$target.width());
				});
				$(window).resize(function(e){
					that.$wrapper.scrollLeft(0);
					that.positonTop = (that.config.hasTop && $('.jquery-scrollactionbar-container').length>0) ? $('.jquery-scrollactionbar-container').outerHeight(): 0 ;
					if((that.$wrapper.offset().top-that.positonTop < $(window).scrollTop()) && $(window).scrollTop()>0){
						that.$target.addClass('onfixed');
						that.transformTh(that.$thead.find('th'),($(window).scrollTop()+that.positonTop-that.$wrapper.offset().top-2)+'px');
						that.config.ishideColumn && that.$settingbtn.css('top', $(window).scrollTop()+that.positonTop- that.$wrapper.offset().top+1);
					}else{
						that.$target.removeClass('onfixed');
						that.transformTh(that.$thead.find('th'),'none');
						that.config.ishideColumn &&that.$settingbtn.css('top', 2);
					}
					that.initScrollbar();
					
				});	
				that.$target.resize(function(e){
					that.positonTop = (that.config.hasTop && $('.jquery-scrollactionbar-container').length>0) ? $('.jquery-scrollactionbar-container').outerHeight(): 0 ;
					if((that.$wrapper.offset().top-that.positonTop < $(window).scrollTop()) && $(window).scrollTop()>0){
						that.$target.addClass('onfixed');
						that.transformTh(that.$thead.find('th'),($(window).scrollTop()+that.positonTop-that.$wrapper.offset().top-2)+'px');
						that.config.ishideColumn && that.$settingbtn.css('top', $(window).scrollTop()+that.positonTop- that.$wrapper.offset().top+1);
					}else{
						that.$target.removeClass('onfixed');
						that.transformTh(that.$thead.find('th'),'none');
						that.config.ishideColumn &&that.$settingbtn.css('top', 2);
					}
					that.initScrollbar();				
				});
			}
			that.updateThead();

			that.$scrollbtn.on('mousedown',function(e){
				if (!that.draging) {
					that.draging =true;
					e.preventDefault();
					that.mouseX = e.clientX;
					that.$outer.addClass('active');
					that.scrollLeft = that.$scrollbtn.position().left;
				}
			});
			$(document).on('mouseup mouseleave', function(e) {
				that.draging =false;
				that.$outer.removeClass('active');
				that.scrollLeft = that.$scrollbtn.position().left;
			});
			$(document).on('mousemove', function(e) {
				e.stopPropagation();
				if (that.draging) {
					e.preventDefault();
					var oldMouseX = that.mouseX;
					var offset = Math.min(Math.max(that.scrollLeft +  e.clientX - oldMouseX, 0), that.$scrollbar.width() - that.$scrollbtn.width()) ;
					that.$wrapper.scrollLeft((that.$target.width() - that.$wrapper.width()) * offset / (that.$scrollbar.width() - that.$scrollbtn.width()));
					that.$scrollbtn.css('left',offset);
				}
			});
			if(that.config.autoScroll){
				that.$wrapper.on('mousemove',function(e){
					if(that.$target.width()>that.$wrapper.outerWidth() && e.clientY){
						var remain = 0.5*(that.$target.width()-that.$wrapper.width());
						var area = 0.25*that.$wrapper.width();
						var mymouseX = e.clientX-that.$wrapper.offset().left+2;
						that.$wrapper.removeClass('onscroll');
						if((mymouseX>0)&& (mymouseX < area)&& (that.$wrapper.scrollLeft()>0)){
							that.$wrapper.scrollLeft(mymouseX*remain/area);
							that.$wrapper.addClass('onscroll');
						}else if(mymouseX>=0.75*that.$wrapper.width() && mymouseX<=(that.$wrapper.width()-that.$wrapper.offset().left) && (that.$wrapper.scrollLeft()<(that.$target.width()-that.$wrapper.width()))){
							that.$wrapper.scrollLeft(remain+(mymouseX+2-0.5*(that.$wrapper.width()-that.$wrapper.offset().left))*remain/area);
							that.$wrapper.addClass('onscroll');
							
						}
					}else{
						
					}
				})
			}
		},
		updateThead: function(){
			var that =this;
			that.positonTop = (that.config.hasTop && $('.jquery-scrollactionbar-container').length>0) ? $('.jquery-scrollactionbar-container').outerHeight(): 0 ;
			if((that.$wrapper.offset().top-that.positonTop < $(window).scrollTop()) && $(window).scrollTop()>0){
				that.$target.addClass('onfixed');
				that.transformTh(that.$thead.find('th'),($(window).scrollTop()+that.positonTop-that.$wrapper.offset().top-2)+'px');
				that.config.ishideColumn && that.$settingbtn.css('top', $(window).scrollTop()+that.positonTop- that.$wrapper.offset().top+1);
			}else{
				that.$target.removeClass('onfixed');
				that.transformTh(that.$thead.find('th'),'none');
				that.config.ishideColumn && that.$settingbtn.css('top', 2);
			}
		},
		transformTh :function(obj,top){
			if(top =='none'){
				obj.css({
					'transform': 'none',
					'-ms-transform': 'none',
					'-moz-transform': 'none', 
					'-webkit-transform': 'none',
					'-o-transform': 'none'		
				});				
			}else{
				obj.css({
					'transform': 'translate3d(0px,' + top + ',0px)',
					'-ms-transform': 'translate3d(0px,' + top + ',0px)',
					'-moz-transform': 'translate3d(0px,' + top + ',0px)', 
					'-webkit-transform': 'translate3d(0px,' + top + ',0px)',
					'-o-transform': 'translate3d(0px,' + top + ',0px)'		
				});				
			}
		},
		initScrollbar: function(){
			var that = this;
			if(that.$wrapper.width()<that.$target.width()){
				that.$scrollbtn.css('width',that.$wrapper.width()*that.$wrapper.width()/that.$target.width()+'px');
				if(that.$wrapper.height()- ($(window).scrollTop()- that.$wrapper.offset().top) - $(window).height() > 0){
					that.$scrollbar.css('top',Math.min($(window).scrollTop()- that.$wrapper.offset().top + $(window).height()-10,that.$wrapper.height()));
					that.$scrollbar.show();
				}else{
					that.$scrollbar.css('top','100%');
					that.$scrollbar.hide();
				}
			}else{
				that.$scrollbar.hide();
			}
		},
		updateColumn : function(list){
//			var that = this;
//			if(list && list.length>0){
//				for(var i in list){
//					that.$target.find('th[data-type]').each(function(index,item){
//						$(item).children('th').show();
//						$(item).children('td').show();
//						if($(item).data('type')== list[i]){
//							$(item).hide();
//							console.log(index);
//							that.$target.children('tbody').children('tr').each(function(){
//								$(this).children('td').eq(index).hide();
//							})
//						}
//					});
//				}
//			}else{
//				that.$target.find('tr').each(function(index,item){
//					$(item).children('th').show();
//					$(item).children('td').show();
//				});				
//			}			
		}
	};

	$.fn.scrolltable = function(config) {
		var $target = $(this);
		if (!$target.attr('bind-scrolltable')) {
			if(!config){
				config ={};
				if($target.data('height')){
					config.height = $target.data('height');
				}
				if($target.data('fixedColumn')){
					config.fixedColumn = $target.data('fixedColumn');
				}
				if(typeof($target.attr('autoScroll')) !="undefined"){
					config.autoScroll = true;
				}
				if(typeof($target.data('hidecolumnList')) !="undefined"){
					config.hidecolumnList = $target.data('hidecolumnList');
				}
				if(typeof($target.attr('ishideColumn')) !="undefined"){
					config.ishideColumn = true;
				}
				if($target.data('columnList')){
					config.columnList = $target.data('columnList');
				}
				if($target.data('hastop')){
					config.hasTop = $target.data('hastop');
				}
			}
			var c = new scrolltable($target,config);
			$target.attr('bind-scrolltable', true);
		}
		return this;
	};

	$(function() {
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
	});
})(jQuery);