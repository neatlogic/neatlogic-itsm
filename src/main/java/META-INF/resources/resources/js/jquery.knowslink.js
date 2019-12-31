(function($) {
	$.fn.knowslink = function(options) {
		var item = $(this);
		if (typeof options != 'undefined' && typeof options.check != 'undefined') {
			var readed = true;
			var pageList = item.data('pageList');
			if (typeof pageList != 'undefined' && pageList != null) {

				for (var i = 0; i < pageList.length; i++) {
					var link = pageList[i];
					$.ajax({
						async : false,
						type : "GET",
						dataType : "json",
						url : '/balantflow/module/balantknows/page/getPageReadAudioJson.do?n=' + encodeURIComponent(link.text()),
						success : function(data) {
							if (data.readcount == 0) {
								link.next().html('<img src="/balantflow/resources/images/icons/new.gif" >');
								readed = false;
							} else {
								link.next().html('');
							}
						}
					});
				}
			}
			return readed;
		} else {
			var text_org = $.trim(item.text());
			if (text_org != '') {
				text_org = text_org.replace(/，/ig, ',');
				var texts = text_org.split(',');
				item.html('');
				var pageList = new Array();
				$.each(texts, function(k, v) {
					if (v != '') {
						var link = $('<a href="javascript:void(0)" class="aLnkKeyword">' + v + '</a>');
						var msg = $('<span class="spnNewPage"></span>');
						pageList.push(link);
						link.click(function() {
							if (hasKnows) {
								// showKnowsPage(v);
								top.addTab('/balantflow/module/balantknows/page/view/' + v);
							} else {
								showPopMsg.info('知识库模块不可用，请先安装');
							}
						});
						item.append(link).append(msg).append('<br>');
					}
				});
				item.data('pageList', pageList);
			}
		}
		return this;
	};

	var hasKnows = null;
	$(function() {
		$.getJSON('/balantflow/module/check/KNOWS', function(data) {
			if (data.status == 1) {
				hasKnows = true;
			}
		});
	});

	function showKnowsPage(pagename) {
		createModalDialog({
			msgwidth : '95%',
			msgtitle : pagename,
			checkBtn : false,
			zIndex : 3000,
			msgcontent : '<iframe class="iframe" src="/balantflow/module/balantknows/page/view/' + pagename + '" style="width:100%" frameborder="0" scroll="no" onload="setContentIframeHeight(this);"></iframe>'
		});
	}
	
	$(function() {
		$('.spnKeyword').each(function() {
			var item = $(this);
			if(!item.data('bind')){
				$(this).knowslink();
				item.data('bind',true);
			}
		});
		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('.spnKeyword').each(function() {
				var item = $(this);
				if(!item.data('bind')){
					$(this).knowslink();
					item.data('bind',true);
				}
			});
		});
		$(document).bind('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('.spnKeyword').each(function() {
				var item = $(this);
				if(!item.data('bind')){
					$(this).knowslink();
					item.data('bind',true);
				}
			});
		});
	});
})(jQuery);