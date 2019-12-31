(function() {
	var xdoT = {
		render : undefined,
		basepath:'/codedriver/xdottemplate/get?t=&templatename='
	};
	var _globals;
	var templateMap = {};
	var f = {};

	f.include = function(name, url) {
		var html = "";
		var json;
		$.ajax({
			url : xdoT.basepath + name,
			async : false,
			success : function(data) {
				html = data;
			}
		});
		if(url && typeof url == 'string'){
			$.ajax({
				dataType:'json',
				async:false,
				url : url,
				success : function(data) {
					json = data;
				}
			});
			var dd = doT.template(html);
			html = dd(json);
		}else if(url && typeof url == 'object'){
			var dd = doT.template(html);
			html = dd(url);
		}
		return '<!-- '+name+' start -->'+html+'<!-- '+name+' end -->';;
	}
	
	xdoT.renderWithHtml = function(html, data){
		if(data){
			html =  doT.template(html, null, f)(data);
		}
		var $html = $(html);
		var tmp = $('<span></span>');
		tmp.html($html);
		var scriptList = new Array();
		tmp.find('script.xdotScript').each(function(){
			scriptList.push($(this).html());
		});
		tmp.find('script.xdotScript').each(function(){
			$(this).empty();
			$(this).remove();
		});
		for(var s = 0; s < scriptList.length; s++){
			try{
				var fn = null, loadFn = null;
				eval(scriptList[s]);
				if(typeof(customFn) == 'object'){
					for(var fnname in customFn){
						window[fnname] = (function(t,f){
							return function(){
								return customFn[f].apply(t, arguments);
							}
						}($html,fnname));
					}
				}
				if(typeof(fn) == 'object'){
					for(var dom in fn){
						var fucList = fn[dom];
						for(var fuc in fucList){
							if(dom != 'this'){
								var fuclist = fucList[fuc];
								tmp.find(dom).off(fuc).on(fuc,(function(h,fuclist){
									  return function(){
										  this.root = h;
										  fuclist.apply(this,arguments); }
									  })($html,fuclist));
							}else{
								$html.off(fuc).on(fuc,fucList[fuc]);
							}
						}
					}
				}
				if(typeof(loadFn) == 'function'){
					tmp.ready((function($html,loadFn){
						return function(){
							loadFn.call($html,$html);
						}
					}($html,loadFn)));
				}
				fn = null;
			}catch(e){
				console.error(e);
			}
		};
		$(document).trigger('xdoTRender', $html);
		return $html;
	}

	xdoT.render = function(templatename, data) {
		if (!templateMap[templatename]) {
			var path = xdoT.basepath + templatename;
			$.ajax({
				url:path,
				async:false,
				success:function(tmp){
					try{
						//if(data){
							doT.template(tmp, null, f)
							templateMap[templatename] = doT.template(tmp, null, f);
						//}else{
						//	templateMap[templatename] = tmp;
						//}
					}catch(e){
						showPopMsg.error('模板<b>' + templatename +'</b>语法异常：<br>'+e);
					}
				}
			}).fail(function(){
				showPopMsg.error('模板<b>' + templatename +'</b>加载失败');
			});
		}
		var html = '';
		//if(data){
			html = templateMap[templatename](data);
		//}else{
		//	html = templateMap[templatename];
		//}
		var $html = $(html);
		var tmp = $('<span></span>');
		tmp.html($html);
		var scriptList = new Array();
		tmp.find('script.xdotScript').each(function(){
			scriptList.push($(this).html());
		});
		tmp.find('script.xdotScript').each(function(){
			if(!$(this).data('keep')){
				$(this).empty();
				$(this).remove();
			}
		});
		for(var s = 0; s < scriptList.length; s++){
			try{
				var fn = null, loadFn = null;
				eval(scriptList[s]);
				if(typeof(customFn) == 'object'){
					for(var fnname in customFn){
						window[fnname] = (function(t,f){
							return function(){
								return customFn[f].apply(t, arguments);
							}
						}($html,fnname));
					}
				}
				if(typeof(fn) == 'object'){
					for(var dom in fn){
						var fucList = fn[dom];
						for(var fuc in fucList){
							if(dom != 'this'){
								var fuclist =fucList[fuc];
								tmp.find(dom).off(fuc).on(fuc,(function(h,fuclist){
									  return function(){
										  this.root = h;
										  fuclist.apply(this,arguments); }
									  })($html,fuclist));
							}else{
								$html.off(fuc).on(fuc,fucList[fuc]);
							}
							
						}
					}
				}
				if(typeof(loadFn) == 'function'){
					tmp.ready((function($html,loadFn){
						return function(){
							loadFn.call($html,$html);
						}
					}($html,loadFn)));
				}
				
				fn = null;
			}catch(e){
				console.error(e);
			}
		}
		$(document).trigger('xdoTRender', $html);
		// var $html = tmp.contents();
		// return $('<div>').append($html.clone()).html();
		if($html.length ==1){
			$html.prepend('<!-- '+templatename+' start -->');
			$html.append('<!-- '+templatename+' end -->');
			return $html;
		}else if($html.length > 1){
			var returnList = new Array();
			returnList.push($('<!-- '+templatename+' start -->')[0]);
			returnList = returnList.concat($html);
			returnList.push($('<!-- '+templatename+' end -->')[0]);
			return returnList;			
		}

	};
	
	xdoT.renderstr = function(templatename, data) {
		if (!templateMap[templatename]) {
			var path = xdoT.basepath + templatename;
			$.ajax({
				url:path,
				async:false,
				success:function(tmp){
					try{
						if(data){
							templateMap[templatename] = doT.template(tmp, null, f);
						}else{
							templateMap[templatename] = tmp;
						}
						
					}catch(e){
						showPopMsg.error('模板<b>' + templatename +'</b>语法异常：<br>'+e);
					}
				}
			}).fail(function(){
				showPopMsg.error('模板<b>' + templatename +'</b>加载失败');
			});
		}
		var html ='';
		if(data){
			html = templateMap[templatename](data);
		}else{
			html = templateMap[templatename];
		}
		return '<!-- '+templatename+' start -->'+html+'<!-- '+templatename+' end -->';
	};

	_globals = (function(){
				return this || (0,eval)("this");
				}());
	_globals.xdoT = xdoT;
}());