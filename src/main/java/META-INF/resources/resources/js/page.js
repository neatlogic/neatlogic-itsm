function changePageAjax(uri, container, page){
	if(uri == null){
		showPopMsg.error('换页失败，参数：uri不能为空！');
		return;
	}
	if(container == null){
		showPopMsg.error('换页失败，参数：container不能为空！');
		return;
	}
	if(page){
		if(uri && uri.indexOf('?') > -1){
			uri += '&currentPage=' + page;
		}else{
			uri += '?currentPage=' + page;
		}
		if(uri.indexOf('container') == -1){
			uri = uri + '&container=' + container ;
		}
	}

	loadingMask();
	$.get(uri, function(data) {
			 $('#' + container).html(data);
			 removeMask();
		  }).fail(function() {
			showPopMsg.error('查询数据失败:<br/>请检查服务端或者网络是否存在问题。', 10);
			 removeMask();
		  }
}
