var scrollTab = (function() {
	$(function() {
		$(document).on('click', '.tab-arrow-left', function() {
			var tabbody = $(this).closest('.content-tab').find('.tab-body');
			var s = tabbody.scrollLeft();
			tabbody.animate({
				scrollLeft : s - 200
			}, 300);
		});

		$(document).on('click', '.tab-arrow-right', function() {
			var tabbody = $(this).closest('.content-tab').find('.tab-body');
			var s = tabbody.scrollLeft();
			var width = tabbody.width();
			var swidth = 0;
			tabbody.find('.tab-item').each(function() {
				swidth += $(this).outerWidth();
			});
			var finalScrollLeft = (s + 200 + width < swidth) ? (s + 200) : (swidth - width);
			tabbody.animate({
				scrollLeft : finalScrollLeft
			}, 300);
		});

		$(document).on('shown.bs.tab', '.tab-body a[data-toggle="tab"]', function(e) {
			try {
				scrollTab.selectTab($(this).closest('.tab-item'));
				
				//隐藏的table页iframe页面加载重算高度
				var target = e.target.toString() ; 
				var ifmId = target.substr(target.lastIndexOf('#') + 1 ,target.length);
				var iframe = $('#' + ifmId).find('iframe');
				if(iframe){
					var ifrm = iframe[0] ; 
					setContentIframeHeight(ifrm);
				}
				
			} catch (e) {
				console.info('selectTab error : ' + e);
			}

		});

	});

	return {
		selectTab : function(tab) {
			var tabbody = tab.closest('.tab-body');
			var left = $(tab).position().left;
			var right = left + $(tab).outerWidth() + 26 + 18;
			var s = tabbody.scrollLeft();
			var width = tabbody.width();
			if (s + width < right) {
				tabbody.animate({
					scrollLeft : right - width
				}, 300);
			} else if (left < s) {
				tabbody.animate({
					scrollLeft : left
				}, 300);
			}
		}
	};
}());