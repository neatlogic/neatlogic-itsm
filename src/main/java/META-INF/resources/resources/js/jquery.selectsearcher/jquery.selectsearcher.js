;
(function($) {
	var SelectSearcher = function(target, config) {
		this.defaultconfig = {
			width : null, // 宽度，默认自适应，如设置此属性必须带单位
			height : null, // 高度，默认不设置，需要固定高度时一般为32px
			url : null, // *调用地址，必须有
			minLength : 0, // 输入最少几个字符串长度才触发查询事件，默认0
			valueKey : 'value', // 存的值的key
			textKey : 'text', // 显示文字的key
			titleKey : 'text', // 下拉显示title的key
			param : 'k',	//接口的关键词对应的key，默认为k
			strict : false, // true标示严格匹配，不匹配的值不能添加，false可以输入不匹配的值
			multiple : true, // 是否支持多选，默认多选
			placeholder : '输入关键字后回车或者点击下拉选项进行搜索', // 提示语
			tooltip: false,										//选中区域的分组提示为移上再展示的tooltip，默认无，为内嵌到展示的
			initFn : null, // 初始化执行事件，如果需要设置默认选中等信息时调用
			updateFn : null,  // 输入框内容更新后的方法，如果需要对输入内容做特殊处理时此方法可以覆盖默认的数据封装事件
			searchFn :null,
			showAmount: 4,
			isAllkey:false,	//是否需要封装所有标签上的data，默认只有text和value
			root:null		//兼容返回数据不是标准的，例如有效数据在data.list,root则为list
		};
		this.config = $.extend({}, this.defaultconfig, config);
		
		if(!this.config.url){
			return;
		}
		this.$target = target;
		this.$target.addClass('selectsearcher-input').attr('contenteditable',true);
		this.init();
		this.request = null;
	};
	this.SelectSearcher = SelectSearcher;
	SelectSearcher.prototype = {
		init : function() { // 初始化外层和输入框和下拉外层，如为只读模式不初始化外层
			var that = this ,config = that.config;
			that.$target.wrap('<div class="selectsearcher-container"'+(config.width?' style="'+ config.width +'"':' ')+'></div>');
			that.$container = that.$target.closest('.selectsearcher-container');
			that.$container.prepend('<div class="selectsearcher-ul"></div>');
			that.$container.append('<span class="selectsearcher-searchbtn ts-search"></span>');
			that.$searchbtn = that.$target.next('.selectsearcher-searchbtn');
			that.$container.append('<span class="selectsearcher-clearbtn ts-remove"></span>');
			that.$clearbtn = that.$searchbtn.next('.selectsearcher-clearbtn');
			that.$selectedul = that.$target.prev('.selectsearcher-ul');
			that.$container.wrap('<div class="selectsearcher-wrapper"></div>');
			that.$wrapper = that.$container.closest('.selectsearcher-wrapper'); 
			that.$wrapper.append('<div class="selectsearcher-dropdown"></div>');
			that.$dropdown = that.$container.next('.selectsearcher-dropdown');
			//如果有初始化事件，执行该事件（用于需要设置默认选中值）
			if(config.initFn && typeof config.initFn =="function"){
				config.initFn(that.$selectedul);
			}	
			//点击自动聚焦
			that.$wrapper.click(function(){
				that.$target.focus();
			});
			$(document).on('click',function(e){
				var ishide = true;
				if(e.target){
					if(($(e.target).parents('.selectsearcher-wrapper').length>0 && $(e.target).parents('.selectsearcher-wrapper').is(that.$wrapper)) || ($(e.target).is(that.$wrapper))){
						ishide =false;
					}
				}
				if(ishide){
					that.$wrapper.addClass('hidedrop');
				}
			});
			$(window).blur(function(){
				that.$wrapper.removeClass('on').addClass('hidedrop');
				that.$target.blur();
			});
			that.$target.focus(function(){
				that.$wrapper.addClass('on').removeClass('hidedrop');
			});
			that.$target.blur(function(){
				that.$wrapper.removeClass('on');
			});
			//如果点击区域在选中区域内不聚焦
			that.$selectedul.click(function(e){
				e.stopPropagation();
			});		
			//输入内容搜索
			that.$target.on('input',function(){
				var val = $.trim($(this).text());
				if((val && val.length>=config.minLength) || val ==''){
					that.url=config.url+'?'+config.param+'='+val;
					that.delaySearch(that.url,val);
				}
			});
			that.$target.keydown(function(e){
				e.stopPropagation();
				that.$wrapper.removeClass('hidedrop');
				if(e.keyCode == "13"){			//enter
					e.preventDefault();
					$(this).text($.trim($(this).text()));
					if(that.$dropdown.find('.selected').length>0){
						that.$dropdown.find('.selected').trigger('click');
					}else if(that.$dropdown.find('.result-fuzzy').length>0){
						that.$dropdown.find('.result-fuzzy').trigger('click');
					}else if($.trim($(this).text()) && $.trim($(this).text())!='' && that.$dropdown.find('.result-li').length==0){
						var val = $.trim($(this).text());
						that.$selectedul.append('<div class="selectsearcher-li li-fuzzy" data-value="" data-text="'+val+'">“'+val+'”<i class="btn-remove ts-remove"></i>'+ (that.config.tooltip ? '<i class="selectsearcher-litips">文本</i>':'')+'</div>');
						$(this).html('');
						that.$target.trigger('change');
					}
				}else if(e.keyCode == "8"){		//delete
					if($(this).html() == ''){
						var selected = that.$selectedul.find('.readytodelete');
						var last = that.$selectedul.find('.selectsearcher-li:last');
						if (selected.length > 0) {
							selected.find('.btn-remove').trigger('click');
						} else {
							last.addClass('readytodelete');
						}
					}else{
						that.$selectedul.find('.readytodelete').removeClass('readytodelete');
					}
				}else if(e.keyCode == "38"){	//up
					e.preventDefault();
					if(that.$dropdown.find('.selected:visible').length>0){
						var selectedIndex = that.$dropdown.find('.result-li:visible').index(that.$dropdown.find('.selected'));
						selectedIndex = selectedIndex<1 ? that.$dropdown.find('.result-li:visible').index(that.$dropdown.find('.result-fuzzy')) : selectedIndex-1;
						that.$dropdown.find('.result-li:visible').removeClass('selected');
						that.$dropdown.find('.result-li:visible').eq(selectedIndex).addClass('selected');
					}else{
						that.$dropdown.find('.result-fuzzy').addClass('selected');
					}
					that.$dropdown.data('selected',that.$dropdown.find('.selected.result-accurate').data('text'));
				}else if(e.keyCode == "40"){	//down
					e.preventDefault();
					if(that.$dropdown.find('.selected:visible').length>0){
						var selectedIndex = that.$dropdown.find('.result-li:visible').index(that.$dropdown.find('.selected'));
						selectedIndex = selectedIndex >that.$dropdown.find('.result-li:visible').length-2 ? 0 : selectedIndex+1;
						that.$dropdown.find('.result-li:visible').removeClass('selected');
						that.$dropdown.find('.result-li:visible').eq(selectedIndex).addClass('selected');
					}else{
						that.$dropdown.find('.result-fuzzy').addClass('selected');
					}	
					that.$dropdown.data('selected',that.$dropdown.find('.selected.result-accurate').data('text'));
				}
				if(e.keyCode != 8){
					that.$selectedul.find('.readytodelete').removeClass('readytodelete');
				}
			});
			that.$selectedul.on('click','.btn-remove',function(){
				if($(this).parent().prev('.li-intersect')){
					if($(this).parent().prev('.li-intersect').hasClass('contact')){
						$(this).parent().prev('.li-intersect').prev('.selectsearcher-li').removeClass('contactprev');
					}
					$(this).parent().prev('.li-intersect').remove();
				}

				$(this).parent().next('.li-intersect') && $(this).parent().next('.li-intersect').remove();
				if($(this).parent().hasClass('li-intersect')){
					$(this).parent().prev('.selectsearcher-li').removeClass('.contactprev');
				}
				$(this).parent().remove();
				that.$target.trigger('change');
			});
			that.$dropdown.on('click','.result-li',function(){
				that.selectOpt($(this));
			});
			that.$clearbtn.click(function(){
				that.$selectedul.empty();
				that.$target.empty();
				that.$dropdown.empty();
				that.$target.trigger('change');
			});
			//搜索执行事件
			that.$searchbtn.click(function(){
				that.$target.trigger('change');
			});
			that.$target.change(function(){
	 			if(that.$selectedul.find('.li-intersect').length>0){
	 				that.$selectedul.find('.li-intersect').removeClass('contactprev');
	 				that.$selectedul.find('.li-intersect').each(function(ind,item){
	 					if($(item).prev('.selectsearcher-li').length>0 && $(item).next('.selectsearcher-li').length>0){
	 						$(item).addClass('contact');
	 						$(item).prev('.selectsearcher-li').addClass('contactprev');
	 					}else{
	 						$(item).removeClass('contact');
	 						$(item).prev('.selectsearcher-li').removeClass('contactprev');
	 					}
	 				})
	 			}
	 			
	 			if(that.$selectedul.find('.selectsearcher-li').length>0){
	 				that.$clearbtn.show();
	 			}else{
	 				that.$clearbtn.hide();
	 			}
	 			
	 			//执行搜索事件
				if(config.searchFn && typeof config.searchFn =="function"){
					config.searchFn(that.getList());
				}
	 			
			});
		},
		getList : function(){
			var that =this;
			var lilist = [],lidata=[];
			var datalist='[';
			if(that.$selectedul.find('.selectsearcher-li').length>0 && !that.$selectedul.find('.selectsearcher-li:last').hasClass('li-intersect')){
				if(that.$selectedul.find('.selectsearcher-li').length>0){
					for(var i =0; i< that.$selectedul.find('.selectsearcher-li').length;i++){
						var selectedli = that.$selectedul.find('.selectsearcher-li').eq(i);
						if(!selectedli.hasClass('li-intersect')){
							if(selectedli.next('.li-intersect').length>0 && selectedli.prev('.li-intersect').length>0 && i< that.$selectedul.find('.selectsearcher-li').length-2){
								datalist += i;
							}else if(selectedli.next('.li-intersect').length>0 && i< that.$selectedul.find('.selectsearcher-li').length-2){
								datalist += '['+i;
							}else if(selectedli.prev('.li-intersect').length>0){
								datalist += i+']';
							}else{
								datalist += '['+i+']';
							}
							if(i<that.$selectedul.find('.selectsearcher-li').length-1){
								datalist += ',';
							}
						}
					}					
				}
			}else{
				if(that.$selectedul.find('.selectsearcher-li').length>0){
					for(var i =0; i< that.$selectedul.find('.selectsearcher-li').length-1;i++){
						var selectedli = that.$selectedul.find('.selectsearcher-li').eq(i);
						if(!selectedli.hasClass('li-intersect')){
							if(selectedli.next('.li-intersect').length>0 && selectedli.prev('.li-intersect').length>0 && i< that.$selectedul.find('.selectsearcher-li').length-2){
								datalist += i;
							}else if(selectedli.next('.li-intersect').length>0 && i< that.$selectedul.find('.selectsearcher-li').length-2){
								datalist += '['+i;
							}else if(selectedli.prev('.li-intersect').length>0){
								datalist += i+']';
							}else{
								datalist += '['+i+']';
							}
							if(i<that.$selectedul.find('.selectsearcher-li').length-2){
								datalist += ',';
							}
						}
					}					
				}
			}
			datalist += ']';
			datalist= JSON.parse(datalist);
			if(datalist && datalist.length>0){
				for(var s =0;s <datalist.length;s++){
					var li = datalist[s];
					var arr =[],darr=[];
					for(var k=0;k<li.length;k++){
						var kli = that.$selectedul.find('.selectsearcher-li').eq(li[k]);
						arr.push(kli);
						if(that.config.isAllkey){
							var allarr = {};
							var li_data=kli.data();
							for(var idx in li_data){
								allarr[idx]=li_data[idx];
							}
							darr.push(allarr);
						}else{
							darr.push({
								value:kli.data('value') ||'',
								text:kli.data('text') ||''
							});							
						}

					}
					lilist.push(arr);
					lidata.push(darr);
				}				
			}
			return {
				data:lidata,
				list:lilist
			};
		},
		delaySearch : function(url,val) {
			var that = this;
			if (that.searchtimer != null) {
				clearTimeout(that.searchtimer);
			}
			that.searchtimer = setTimeout(function() {
				var config = that.config;
				that.doSearch(url,val);
			}, 200);
		},
		doSearch : function(url,val) {
			var that = this;
			var config = that.config;
			if(that.request != null){
				that.request.abort();
			}
			that.$dropdown.empty();
			that.request =$.getJSON(url,function(data){
				var resultData;
				if(val.toUpperCase() =="OR" && that.$selectedul.find('.selectsearcher-li').length>0 && !that.$selectedul.find('.selectsearcher-li:last').hasClass('li-intersect')){
					that.$dropdown.append('<div class="result-li result-intersect"><b>逻辑</b> “<i>'+val+'</i>”</div>');
				}
				if(config.root){
					data=data[config.root];
				}
				// 数据格式如果是{data:[]}的就是有分组的，如果是[]则是不带分组的
				if(data.data){
					resultData = data.data;
					that.hasClassify = true;
				}else{
					resultData = data;
					that.hasClassify = false;					
				}
				if(resultData && resultData.length>0){
					if (that.hasClassify) {
						that.$dropdown.addClass('group');
						for(var i=0;i<resultData.length;i++){
							var datas = resultData[i]; 
							if(datas.data.length>0){
								that.$dropdown.append('<h6 class="result-title">'+datas[config.textKey]+'</h6>');
								var $dropul = $('<ul class="result-ul">');
								for(var j=0;j< datas.data.length;j++){
									var datali = datas.data[j];
									var newtext = datali[config.textKey];
									if (datali[config.textKey]) {
										newtext = datali[config.textKey].replace(new RegExp('(' + val + ')', 'ig'), '<b style="color:blue">$1</b>');
									}
									var $dropli =$('<li class="result-li result-accurate '+((that.$dropdown.data('selected') && datali[config.textKey]==that.$dropdown.data('selected'))?'selected':'')+' '+(j>config.showAmount?'result-more':'')+'" title="'+datali[config.titleKey]+'">'+newtext+'</li>');
									for ( var ct in datali) {
										$dropli.data(ct, datali[ct]);
									}
									$dropli.data('group', datas[config.textKey]);
									datas[config.valueKey] && $dropli.data('type', datas[config.valueKey]);
									$dropul.append($dropli);
								}
								if(datas.data.length>config.showAmount){
									var moreBtn = $('<li class="btn-moreresult"><i class="ts icon-more"></i></li>');
									$dropul.append(moreBtn);
									moreBtn.click(function(e){
										e.stopPropagation();
										$(this).parents('.result-ul').toggleClass('showmore');
									});
								}
								that.$dropdown.append($dropul);								
							}
						}
					}else{
						var $dropul = $('<ul class="result-ul"></ul>');
						for(var j=0;j< resultData.length;j++){
							var datali =resultData[j];
							var newtext = datali[config.textKey];
							if (datali[config.textKey]) {
								newtext = datali[config.textKey].replace(new RegExp('(' + val + ')', 'ig'), '<b style="color:blue">$1</b>');
							}
							var $dropli =$('<li class="result-li result-accurate '+((that.$dropdown.data('selected') && datali[config.textKey]==that.$dropdown.data('selected'))?'selected':'')+'" title="'+datali[config.titleKey]+'">'+(datali.icon_url?'<img src="'+datali.icon_url+'">':'')+newtext+'</li>');
							for ( var ct in datali) {
								$dropli.data(ct, datali[ct]);
							}
							$dropul.append($dropli);							
						}
						that.$dropdown.append($dropul);
					}
				}
				if(val){
					var $fuzzyli = $('<div class="result-li result-fuzzy"><b>搜索</b> “<i>'+val+'</i>”</div>');
					$fuzzyli.data('text',val);
					$fuzzyli.data('value','');
					that.$dropdown.append($fuzzyli);
				}
				that.request = null;
			});
		},
		selectOpt : function(obj){
			var that = this;
			if(obj.hasClass('result-fuzzy')){				//关键字搜索
				//过滤掉单引号等会影响字符串转义
				var val = obj.data('text').replace(/\"/g," ");
				var $selectedli =$('<div class="selectsearcher-li li-fuzzy">“'+val+'”<i class="btn-remove ts-remove"></i>'+ (that.config.tooltip ? '<i class="selectsearcher-litips">文本</i>':'')+'</div>');
				$selectedli.data('text',val);
				$selectedli.data('value','');
				that.$selectedul.append($selectedli);
			}else if(obj.hasClass('result-intersect')){		//逻辑运算符
				if(that.$selectedul.find('.selectsearcher-li').length>0 && !that.$selectedul.find('.selectsearcher-li:last').hasClass('li-intersect')){
					that.$selectedul.append('<span class="selectsearcher-li li-intersect">|<i class="btn-remove ts-remove"></i></span>');
				}else{
					return;
				}				
			}else{	//精确字
				var dataarr = obj.data();
				var $selectedli =$('<div class="selectsearcher-li li-accurate">'+( obj.data('group') && !that.config.tooltip ?'<b>'+obj.data('group')+'</b>:':'' )+''+ obj.data('text')+'<i class="btn-remove ts-remove"></i>'+ (that.config.tooltip ? '<i class="selectsearcher-litips">'+obj.data('group')+'</i>':'')+'</div>');	
				if(dataarr){
					for(var arr in dataarr){
						$selectedli.data(arr, dataarr[arr]);
					}					
				}
				that.$selectedul.append($selectedli);
			}
			that.$dropdown.empty();
			that.$target.empty();
			that.$target.trigger('change');	
		}
	};
	$.fn.selectsearcher = function(config) {
		var $target = $(this);
		if (!$target.data('bind')) {
			var c = new SelectSearcher($target, config);
			$target.data('bind',true).data('selectsearcher', c);
			
		}
		return this;
	};
})(jQuery);
