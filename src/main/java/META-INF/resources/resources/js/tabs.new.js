(function() {
	$(document).on('click', 'a[rel=tab],a[target="_tab"] ', function(e) {
		if (self == top) {
			addTab(this);
		} else {
			try{
				top.addTab(this);
			}catch(e){
				addTab(this);
			}
		}
		return false;
	});

	$(document).on('click', '.tab-scroll-left', function() {
		var s = $('#tab-body').scrollLeft();
		$('#tab-body').animate({
			scrollLeft : s - tabs.scrollstep
		}, tabs.scrolldelay);
	});
	$(document).on('click', '.tab-scroll-right', function() {
		tabs.scrollRight();
	});
	var tabs = {
		tabPanel : '#ul-tab',
		contentPanel : '#content-panel',
		tab_item_margin_right : 10,
		scrollstep : 150,
		scrolldelay : 300,
		icon_right_padding : 27
	};

	this.tabs = tabs;
	var tabMap = this.tabMap ={},tabList =[];
	tabs.uid = function() {
		return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
			var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
			return v.toString(16);
		});
	};

	tabs.updateTabTitle = function(that) {
		if ($(that).contents().find('title').length > 0) {
			var title = $(that).contents().find('title:first').text();
			var index = $(that).attr('index');
			if ($('#tab-item-' + index).find('.tab-text').text() == 'loading...') {
				$('#tab-item-' + index).find('.tab-text').text(title);
				$('#tab-item-' + index).attr('title', title);
			}
		}
	};

	tabs.scrollRight = function() {
		var s = $('#tab-body').scrollLeft();
		var width = $('#tab-body').width();
		var swidth = 0;
		$('.tab-item').each(function() {
			swidth += $(this).outerWidth();
		});
		swidth += tabs.icon_right_padding;
		var finalScrollLeft = (s + tabs.scrollstep + width < swidth) ? (s + tabs.scrollstep) : (swidth - width);
		$('#tab-body').animate({
			scrollLeft : finalScrollLeft
		}, tabs.scrolldelay);
	};

	tabs.selectTab = function(iframe) {
		selectTab($(iframe).attr('index'));
	};

	this.selectTab = function(id) {
		if (!$('#tab-item-' + id).hasClass('actived')) {
			$('.tab-item').removeClass('actived');
			$('.panel-item').removeClass('actived');
			$('#tab-item-' + id).addClass('actived');
			$('#panel-item-' + id).addClass('actived');
		}
		var left = $('#tab-item-' + id).position().left;
		var right = left + $('#tab-item-' + id).outerWidth() + tabs.icon_right_padding * 2;
		var s = $('#tab-body').scrollLeft();
		var width = $('#tab-body').width();
		if (s + width < right) {
			$('#tab-body').animate({
				scrollLeft : right - width
			}, tabs.scrolldelay, function() {
				hideInvisibleTab();
			});

		} else if (left < s + tabs.scrollstep) {
			$('#tab-body').animate({
				scrollLeft : left - tabs.scrollstep > 0 ? left - tabs.scrollstep : 0
			}, tabs.scrolldelay, function() {
				hideInvisibleTab();
			});
		}
		for(var i=0; i<tabList.length; i++){
			if(tabList[i] == id){
				tabList.splice(i,1);
			}
		}
		tabList.push(id);
		hideInvisibleTab();
		$('#globalUl').hide();
	};

	this.closeCurrent = function(win) {
		try{
			var arrFrames = top.document.getElementsByTagName("IFRAME");
			for (var i = 0; i < arrFrames.length; i++) {
				if (arrFrames[i].contentWindow === win) {
					var idx = $(arrFrames[i]).attr("index");
					closeTab(idx);
				}
			}
		}catch(e){
		}
	};

	tabs.refreshTab = function(id,url) {
		var ifrm = $('#iframe-item-' + id);
		if (ifrm.length > 0) { 
			if(url){
				ifrm[0].contentWindow.location.href=url;
			}else{
				try{
					ifrm[0].contentWindow.location.reload();
				}catch(e){
					ifrm[0].src=ifrm[0].src;
				}
				
			}
		}			
	};
	
	this.refreshTab = function(id,url) {
		tabs.refreshTab(id,url);
	};
	
	// 新增全局方法用于刷新单独的url,如果iframe已经打开则执行刷新事件，如果没打开则直接打开
	this.reloadTab =function(url){
		if(tabMap[url]){
			tabs.refreshTab(tabMap[url],url);
		}else{
			this.addTab(url);
		}
	};

	tabs.setIframeHeight = function(ifm) {
		var ifrm = (typeof ifm == 'string' ? document.getElementById(ifm) : ifm);
		var height = $(window).height() - ($(ifrm).offset().top || 65);
		$(ifrm).css('height', height + 'px');
	};

	this.closeTab = function(id) {
		if ($('#tab-item-' + id).attr('closable') == 'true') {
			var prevtab;
			for(var i=0; i<tabList.length; i++){
				(function(it){
					if(tabList[i] == id){
						tabList.splice(i,1);
					}
					prevtab = i-1 >0? i-1 :0;					
				})(i);
			}
			if(tabList.length>0 && $('#tab-item-' + id).hasClass('actived')){
				selectTab(tabList[prevtab]);
			}
			$('#tab-item-' + id).remove();
			var iframe = $('#panel-item-' + id).find('.iframe-item');
			try{
				var ifrdoc = iframe[0].contentWindow.document;
				$(ifrdoc).unbind().die();
				$(ifrdoc).find('*').unbind().die();
				$(ifrdoc).empty();				
			}catch(e){
				
			}

			$('#panel-item-' + id).remove();
			
			for ( var u in tabMap) {
				if (tabMap[u] == id) {
					delete tabMap[u];
				}
			}
			adjustScroller();
		}
	};

	this.adjustScroller = function() {
		var s = $('#tab-body').scrollLeft();
		var width = $('#tab-body').width();
		var swidth = 0;
		$('.tab-item').each(function() {
			swidth += $(this).outerWidth();
		});
		swidth += tabs.icon_right_padding * 2;
		if (s + width > swidth) {
			$('#tab-body').animate({
				scrollLeft : swidth - width
			}, tabs.scrolldelay, function() {
				hideInvisibleTab();
			});
		} else {
			hideInvisibleTab();
		}
	};

	this.updateTitleByIndex = function(idx, title) {
		if (idx && title) {
			$('#tab-item-' + idx).find('.tab-text').text(title);
			$('#tab-item-' + idx).attr('title', title);
		}
	}

	this.addTab = function(linkitem, config) {
		var url = null;
		var closable = true;
		var forceopen = false;
		var title;
		var id;
		var more = true;
		if (typeof linkitem == 'string') {
			url = linkitem;
			if (config) {
				title = config.title;
				more = config.more ? false : true;
			}
		} else {
			url = $(linkitem).attr('href');
			closable = typeof $(linkitem).attr('closable') == 'undefined' ? true : ($(linkitem).attr('closable') == '1' ? true : false);
			title = $(linkitem).attr('title');
			more = $(linkitem).attr('more') ? false : true;
		}
		if (config) {
			if (typeof config.closable != 'undefined') {
				closable = config.closable;
			}
			forceopen = config.forceopen ? true : false;
		}
		if (tabMap[url] && !forceopen) {
				selectTab(tabMap[url]);
		}else {
			var totalurl = url.split('?')[0] || url;
			if(tabMap[totalurl] && !more){
				var ifrm = $('#iframe-item-' + tabMap[totalurl]);
				if (ifrm.length > 0) {
					ifrm.attr('src', url);
					ifrm.attr('url', url);
					refreshTab(tabMap[totalurl],url);
				}
			}else{
				var tIndex = tabs.uid();
				var tab = $('<li class="tab-item" id="tab-item-' + tIndex + '" index="' + tIndex + '"></li>');
				tab.attr('closable', closable);
				tab.on('click', function(e) {
					e.stopPropagation();
					selectTab($(this).attr('index'));
				});
				var tabtext = $('<span class="tab-text"></span>');

				tab.attr('title', title || 'loading...');
				tabtext.text(title || 'loading...');
				tab.append(tabtext);

				var iconcontainer = $('<span class="tab-icon"></span>');
				tab.append(iconcontainer);

				var refreshicon = $('<i class="icon ts ts-refresh refreshicon" title="refresh"></i>');
				refreshicon.on('click', function() {
					tabs.refreshTab($(this).closest('.tab-item').attr('index'));
				});
				iconcontainer.append(refreshicon);
				if (closable) {
					var tabicon = $('<i class="icon ts ts-remove closeicon" title="close"></i>');
					tabicon.bind('click', function(e) {
						e.stopPropagation();
						closeTab($(this).closest('.tab-item').attr('index'));
					});
					iconcontainer.append(tabicon);
					tab.addClass('two-icon');
				}
				var globalicon = $('<i class="icon ts-list globalicon" title="more"></i>');
				globalicon.on('click', function(e) {
					e.stopPropagation();
					var ulLeft = $('#tab-body').hasClass('hideother') ? $(this).parents('.tab-item').offset().left-$(tabs.tabPanel).offset().left-$('#tab-body').scrollLeft()+40 :$(this).parents('.tab-item').offset().left-$(tabs.tabPanel).offset().left-$('#tab-body').scrollLeft();
					$('#globalUl').css('left',ulLeft);
					if($('#globalUl').data('index')==tIndex){
						$('#globalUl').toggle();
					}else{
						$('#globalUl').data('index',tIndex).show();
					}
				});
				$(tabs.tabPanel).blur(function() {
					if($('#globalUl').is(':visible')){
						setTimeout(function(){
							$('#globalUl').hide();
						},250);						
					}
				});
				iconcontainer.append(globalicon);
				tab.append('<i class="loadingbar"></i>');
				$(tabs.tabPanel).append(tab);
				var panel = $('<div class="panel-item" id="panel-item-' + tIndex + '" index="' + tIndex + '"></div>');
				if(more){
					var iframe = $('<iframe allowFullScreen="true" id="iframe-item-' + tIndex + '" class="iframe-item " index="' + tIndex + '" frameborder="no" style="width: 100%; height:100%;" onload="tabs.updateTabTitle(this);tabs.selectTab(this)" url="'
							+ url + '" scrolling="auto" name="frame"></iframe>');
					panel.append(iframe);
					$(tabs.contentPanel).append(panel);
					tabMap[url] = tIndex;
					iframe.attr('src', url);					
				}else{
					var iframe = $('<iframe allowFullScreen="true" id="iframe-item-' + tIndex + '" class="iframe-item " index="' + tIndex + '" frameborder="no" style="width: 100%; height:100%;" onload="tabs.updateTabTitle(this);tabs.selectTab(this)" url="'
							+ url + '" scrolling="auto" name="frame"></iframe>');
					panel.append(iframe);
					$(tabs.contentPanel).append(panel);
					tabMap[totalurl] = tIndex;
					iframe.attr('src', url);					
				}	
				$(tabs.tabPanel).click(function() {
					$('#globalUl').hide();
				});
			}
		}
		
		return false;
	};
	this.hideInvisibleTab = function() {
		var invisibleCount = 0;
		$('#ulInvisibleList').empty();
		var hasLeft = false, hasRight = false;
		$('#ul-tab .tab-item').each(function() {
			var index = $(this).attr('index');
			var title = $(this).attr('title');
			var left = $(this).position().left;
			var right = left + $(this).outerWidth();
			if (left < $('#tab-body').scrollLeft()) {
				invisibleCount += 1;
				$('#ulInvisibleList').append('<li><a href="javascript:selectTab(\'' + index + '\')">' + title + '</a></li>');
				hasLeft = true;
			}

			if (right > $('#tab-body').width() + $('#tab-body').scrollLeft()) {
				if (hasLeft && !hasRight) {
					$('#ulInvisibleList').append('<li role="separator" class="divider"></li>');
				}
				invisibleCount += 1;
				$('#ulInvisibleList').append('<li><a href="javascript:selectTab(\'' + index + '\')">' + title + '</a></li>');
				hasRight = true;
			}

		});
		$('#spnInvisibleCount').text(invisibleCount >= 100 ? '99+' : invisibleCount);
		if (invisibleCount > 0) {
			$('#divInvisibleTab').show();
			$('#tab-body').addClass('hideother');
		} else {
			$('#divInvisibleTab').hide();
			$('#tab-body').removeClass('hideother');
		}
	};
	this.closeallTab =function(){
		$('#ul-tab .tab-item.two-icon').each(function() {
			closeTab($(this).attr('index'));
		});
	};
	this.closeotherTab =function(){
		$('#ul-tab .tab-item.two-icon').each(function() {
			if (!$(this).hasClass('actived')) {
				closeTab($(this).attr('index'));
			}
		});
		selectTab($('#ul-tab .tab-item.actived').attr('index'));
	};
	this.closeleftTab =function(){
		if($('#ul-tab .tab-item.actived').prevAll('.tab-item.two-icon').length>0){
			$('#ul-tab .tab-item.actived').prevAll('.tab-item.two-icon').each(function(){
				closeTab($(this).attr('index'));
			});
		}
		selectTab($('#ul-tab .tab-item.actived').attr('index'));
	};
	this.closerightTab =function(){
		if($('#ul-tab .tab-item.actived').nextAll('.tab-item.two-icon').length>0){
			$('#ul-tab .tab-item.actived').nextAll('.tab-item.two-icon').each(function(){
				closeTab($(this).attr('index'));
			});
		}
		selectTab($('#ul-tab .tab-item.actived').attr('index'));
	};

}());