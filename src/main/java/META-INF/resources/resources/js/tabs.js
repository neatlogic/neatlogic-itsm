var tabMap = {};
var tabIndex = 1;
$(function() {
	$(document).on('click', 'a[rel=tab]', function(e) {
		if (self == top) {
			addTab(this);
		} else {
			top.addTab(this);
		}
		return false;
	});

	if ($('#contentTab').length == 1) {
		$('#contentTab').tabs({
			onBeforeClose : function(title, index) {
				var id = null;
				$('.tabTitle').each(function(k, v) {
					if (index == k) {
						id = $(this).attr('index');
					}
				});
				if (id != null) {
					for ( var k in tabMap) {
						if (tabMap[k] == id) {
							delete tabMap[k];
							break;
						}
					}
				}
			}
		});
	}
});

function selectTab(id) {
	var idx = null;
	$('.tabTitle').each(function(k, v) {
		if ($(this).attr('index') == id) {
			idx = k;
		}
	});
	if (idx != null) {
		$('#contentTab').tabs('select', idx);
	}
}

function closeTab(id) {
	var idx = null;
	$('.tabTitle').each(function(k, v) {
		if ($(this).attr('index') == id) {
			idx = k;
		}
	});
	if (idx != null) {
		$('#contentTab').tabs('close', idx);
	}
}

function closeCurrent(win) {
	var arrFrames = top.document.getElementsByTagName("IFRAME");
	for (var i = 0; i < arrFrames.length; i++) {
		if (arrFrames[i].contentWindow === win) {
			var idx = $(arrFrames[i]).attr("index");
			closeTab(idx);
		}
	}
}

function updateTitleByIndex(idx, title) {
	if (idx && title) {
		$('#tab-item-' + idx).find('.tabs-title').text(title);
	}
}

function addBlankTab() {
	$('#contentTab').tabs('add', {
		id : tabIndex,
		title : '加载中...',
		selected : true,
		content : '<iframe class="tabFrame" name="ifrm' + tabIndex + '" index="' + tabIndex + '" frameborder="no" style="width: 100%; height: 800px" onload="setIframeHeight(this);updateTabTitle(this);" scrolling="auto"></iframe>',
		closable : true
	});
	tabIndex += 1;
	return 'ifrm' + tabIndex;
}

function addTab(linkitem) {
	var url = null;
	var closable = true;
	var title;
	var id;
	if (typeof linkitem == 'string') {
		url = linkitem;
	} else {
		url = $(linkitem).attr('href');
		closable = typeof $(linkitem).attr('closable') == 'undefined' ? true : ($(linkitem).attr('closable') == '1' ? true : false);
		title = $(linkitem).attr('title');
	}
	if (tabMap[url] && tabMap[url] != null) {
		var idx = null;
		$('.tabTitle').each(function(k, v) {
			if ($(this).attr('index') == tabMap[url]) {
				idx = k;
			}
		});
		if (idx != null) {
			$('#contentTab').tabs('select', idx);
		}
	} else {
		$('#contentTab').tabs('add', {
			id : tabIndex,
			title : title || '加载中...',
			selected : true,
			content : '<iframe class="tabFrame" name="ifrm' + tabIndex + '" index="' + tabIndex + '" src="' + url + '" frameborder="no" style="width: 100%; height: 800px" onload="setIframeHeight(this);updateTabTitle(this);" url="' + url + '" scrolling="auto"></iframe>',
			closable : closable,
			tools : [ {
				iconCls : 'easyui-icon-mini-refresh',
				title : '刷新',
				handler : function() {
					var idx = $(this).closest('li').attr('index');
					var ifm = $('#' + idx).find('iframe');
					if (ifm.length == 1) {
						var src = ifm.attr('src');
						ifm.attr('src', src);
					}
				}
			} ]
		});
		tabMap[url] = tabIndex;
		tabIndex += 1;
	}
	return false;
}

function updateTabTitle(frm) {
	var title = $(frm).contents().find('title').text();
	var index = $(frm).attr('index');
	if ($('#tabTitle' + index).find('.tabs-title').text() == '加载中...') {
		$('#tabTitle' + index).find('.tabs-title').text(title);
	}
}