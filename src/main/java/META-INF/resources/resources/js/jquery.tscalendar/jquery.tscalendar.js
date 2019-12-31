;
(function($) {
//	init***calendar方法负责渲染每个日历的外层元素，update***calendar方法负责调用render***calendar（里层不含数据的渲染）方法+调接口回传数据（接口回调跟里层框架分开渲染避免由于接口回调时间太长导致的页面加载过渡太明显）
	var tscalendar = function(target,config) {
		var that = this;
		$(target).append('<div class="jquery-tscalendar-outer"></div>')
		that.$outer = $(target);
		that.$target = that.$outer.find('.jquery-tscalendar-outer');
		that.weekdate =['星期天','星期一','星期二','星期三','星期四','星期五','星期六'];
		that.typename ={'month':'月','week':'周','threeDay':'三天','day':'日','overview':'日程'};
		that.config = config;
		// 日历的日期，如果不指定则默认为当天
		if(config.date){
			that.datetime = new Date(Date.parse(config.date.replace(/-/g,  "/")) );	// 2018-12-12 12:30字符串格式的转为日期格式
		}else{
			that.datetime = new Date();
		}
		that.year = parseInt(that.datetime.getFullYear());								// 年
		that.month = parseInt(that.datetime.getMonth())+1;								// 月（从0开始的，要+1）
		that.date = parseInt(that.datetime.getDate());									// 日
		that.day = that.datetime.getDay();												// 星期 0(周日)～6(周六)
		that.hours = that.datetime.getHours();											// 小时
		that.minutes = that.datetime.getMinutes();										// 分
		that.seconds = that.datetime.getSeconds();										// 秒（that.datetime.getSeconds() 距1970-1-1 8:00之间的毫秒数）
		that.timetype = that.config.timetype ? that.config.timetype :['month','day'];
		that.defaulttype = that.config.defaultType ? that.config.defaultType : 'month';
		that.initActionbar();
	};
	this.tscalendar = tscalendar;
	tscalendar.prototype = {
		initActionbar:function(){
			var that = this;
			that.$outer.prepend('<div class="jquery-tscalendar-actionbar"></div>');
			that.actionbar = that.$outer.find('.jquery-tscalendar-actionbar');
			for(var i=0;i<that.timetype.length;i++){
				var mytype = that.timetype[i];
				if(that.defaulttype == mytype){
					that.actionbar.append('<span class="btn btn-primary" data-type="'+mytype+'">'+that.typename[mytype]+'</span>');
				}else{
					that.actionbar.append('<span class="btn btn-items" data-type="'+mytype+'">'+that.typename[mytype]+'</span>');
				}
			}
			that.type = that.defaulttype;
			that.updataCalendar(that.defaulttype,that.year,that.month,that.date);
			that.actionbar.find('.btn').click(function(){
				var type = $(this).data('type');
				that.type = type;
				that.updataCalendar(type,that.year,that.month,that.date);
			});
		},
		updataCalendar:function(type,year,month,date){
			var that = this;
			that.actionbar.find('.btn[data-type="'+type+'"]').addClass('btn-primary').removeClass('btn-items').siblings().addClass('btn-items').removeClass('btn-primary');
			$(window).scrollTop(0);
			if(type =='month'){
				that.initMonthcalendar(year,month,date);
			}else if(type =='week'){
				that.initWeekcalendar(year,month,date);
			}else if(type =='day'){
				that.initDaycalendar(year,month,date);
			}else if(type =='overview'){
				that.initOverviewcalendar(year,month,date);
			}
		},
		initMonthcalendar:function(year,month,date){											//月模式下的日历外壳
			var that = this;
			// 拼接外层
			var innertxt ='';
			innertxt +='<div class="jquery-tscalendar-month">';
			innertxt +='<div class="jquery-tscalendar-header month-header">';
			innertxt +='<a class="btn_prev_m btn btn-icon ts-angle-left btn-items" href="javascript:void(0);"></a>';
			innertxt +='<h2 class="nowdate"></h2>';
			innertxt +='<a class="btn_next_m btn btn-icon ts-angle-right btn-items" href="javascript:void(0);"></a>';
			innertxt +='</div>';			
			innertxt +='<div class="jquery-tscalendar-container">';		
			innertxt +='<div class="jquery-tscalendar-container-thead">';		
			innertxt +='<table>';
			innertxt +='<thead>';
			innertxt +='<tr>';
			innertxt +='<th class="weekend">'+that.weekdate[0]+'</th>';
			for(var i =1; i<that.weekdate.length-1; i++){
				innertxt +='<th>'+that.weekdate[i]+'</th>';
			}
			innertxt +='<th class="weekend">'+that.weekdate[that.weekdate.length-1]+'</th>';
			innertxt +='</tr>';
			innertxt +='</thead>';
			innertxt +='</table>';
			innertxt +='</div>';
			innertxt +='<table>';			
			innertxt +='<tbody class="jquery-tscalendar-tbody">';
			innertxt +='</tbody>';
			innertxt +='</table>';
			innertxt +='</div>';	
			innertxt +='</div>';	
			innertxt +='</div>';	
			that.$target.empty().append(innertxt);
			that.month_outer = that.$target.find('.jquery-tscalendar-month');
			that.month_header = that.$target.find('.month-header');
			that.month_showdate = that.month_header.find('.nowdate');
			that.month_btnprevm = that.month_header.find('.btn_prev_m');
			that.month_btnnextm = that.month_header.find('.btn_next_m');
			that.month_container = that.month_header.siblings('.jquery-tscalendar-container');
			that.month_tbody = that.month_container.find('.jquery-tscalendar-tbody');
			// 填充tbody
			that.updateMonthcalendar(that.year,that.month,that.date);
			//点击切换上下月
			that.month_btnprevm.click(function(){
				var prev = that.getPrevcalendar('month',that.year,that.month,that.date);
				that.updateDate(prev);
				that.updateMonthcalendar(that.year,that.month,that.date);
				prev = null;
			});
			that.month_btnnextm.click(function(){
				var next = that.getNextcalendar('month',that.year,that.month,that.date);
				that.updateDate(next);
				that.updateMonthcalendar(that.year,that.month,that.date);
				next = null ;
			});
			innertxt = null;
			//窗口滚动或者发生尺寸变化时判断是否要固定表头
			$(window).on('scroll resize', function() {
				if($(window).scrollTop()>that.month_outer.offset().top){
					that.month_outer.addClass('fixtop');
				}else{
					that.month_outer.removeClass('fixtop');					
				}
			});
		},
		updateMonthcalendar:function(year,month,date){
			var that= this;
			that.month_tbody.html(that.renderMonthcalendar(year,month,date));
			that.month_tbody.find('.monthday-time').click(function(){
				that.date = $(this).data('date');
				that.updataCalendar('day',year,month,$(this).data('date'));				
			});
			//小于最小日期的日历不可以搜索，如果无设置最小日期则默认是1970-1-1
			var mindate = that.config.minDate ? new Date(Date.parse(that.config.minDate.replace(/-/g,  "/"))):new Date('1970/1/1');
			var monthmindate = new Date(year+'/'+month+'/1');
			var monthajaxdata =that.config.datalist? that.config.datalist :{};
			if(monthmindate.getTime() <= mindate.getTime()){
				that.month_btnprevm.addClass('disabled').attr("disabled",true);
			}else{
				that.month_btnprevm.removeClass('disabled').attr("disabled",false);
			}
			//如果有设置最大日期，则大于该日期的日历不可以搜索
			if(that.config.maxDate &&isNaN(that.config.maxDate)&&!isNaN(Date.parse(that.config.maxDate))){
				var maxdate = new Date(Date.parse(that.config.maxDate.replace(/-/g,  "/")));
				var monthmaxdate = new Date(year+'/'+month+'/'+that.getMonthday(year,month));
				if(monthmaxdate.getTime() >= maxdate.getTime()){
					that.month_btnnextm.addClass('disabled').attr("disabled",true);
				}else{
					that.month_btnnextm.removeClass('disabled').attr("disabled",false);
				}				
			}
			//设置同步当两个接口都数据太多的时候会导致页面特别卡，由于同步异步只涉及到布局的所以在两个接口返回同时反复判断高度那些的参数

			if(that.config.datasource){
				$.post(that.config.datasource+'?dateType=month&endTime='+year+'-'+month+'-'+that.getMonthday(year,month),monthajaxdata).success(function(data){
					if(data.monthData && data.monthData.length>0){
						for(var i =0;i<data.monthData.length;i++){
							(function(ind){
								if(that.config.onRendermonth && typeof that.config.onRendermonth == 'function'){
									that.month_tbody.find(".schedulelist-month[data-datestamp='"+data.monthData[ind].dayTime+"']").each(function(){
										//当月历的日个数太多时更多的跳转到日模式下查看全部
										that.config.onRendermonth($(this).find('.monthdata-container'),data.monthData[ind].dayData);
										var viewdate = data.monthData[ind].dayTime.split('-');
										$(this).append('<div class="btn-showallmonth"><a class="btn-viewdaydetail fz12 text-href" href="javascript:void(0);" data-date="'+viewdate[2]+'" data-datalength="'+ data.monthData[ind].dayData.length+'">查看全部'+ data.monthData[ind].dayData.length+'个</a></div>');
										$(this).find('.btn-viewdaydetail').click(function(){
												that.date = $(this).data('date');
												that.updataCalendar('day',that.year,that.month,$(this).data('date'));			
										});
									})
								}	
							})(i);
						}
					}
					if(that.config.scheduleresource){
						$.post(that.config.scheduleresource+'?dateType=month&endTime='+year+'-'+month+'-'+that.getMonthday(year,month),monthajaxdata).success(function(data){
							if(data.monthData &&data.monthData.length>0){
								for(var i =0;i<data.monthData.length;i++){
									(function(ind){
										if(that.config.onRenderschedule && typeof that.config.onRenderschedule == 'function'){
											that.month_tbody.find(".schedulelist-month[data-datestamp='"+data.monthData[ind].dayTime+"']").each(function(){
												that.config.onRenderschedule($(this).find('.month-schedulelist'),data.monthData[ind].dayData);
												if($(this).find('.btn-showallmonth').length>0){
													$(this).find('.monthdata-container').css('height','auto');
													if($(this).find('.month-schedulelist').height()>200){
														$(this).find('.month-schedulelist').css('overflow','auto');
														$(this).find('.month-schedulelist').css('height',200);
													}
													if($(this).find('.month-schedulelist').height()+$(this).find('.monthdata-container').height()>220){
														$(this).css('height',220);
														$(this).find('.monthdata-container').css('height',220-$(this).find('.month-schedulelist').height());
														$(this).find('.btn-showallmonth').show();
													}else{
														$(this).css('height',$(this).find('.month-schedulelist').height()+$(this).find('.monthdata-container').height());
														$(this).find('.btn-showallmonth').hide();
													}
													
												}
											})
										}								
									})(i);
								}
							}					
						}).error(function(data){
							showPopMsg.error('${tk:lang("异常","")}：<br>' + data.Message);
						});
					}
				}).error(function(data){
					showPopMsg.error('${tk:lang("异常","")}：<br>' + data.Message);
				});			
			}
			$(window).resize(function(){
				that.month_tbody.find(".schedulelist-month").each(function(){
					if($(this).find('.btn-showallmonth').length>0){
						$(this).find('.monthdata-container').css('height','auto');
						if($(this).find('.month-schedulelist').height()+$(this).find('.monthdata-container').height()>220){
							$(this).css('height',220);
							$(this).find('.monthdata-container').css('height',220-$(this).find('.month-schedulelist').height());
							$(this).find('.btn-showallmonth').show();
						}else{
							$(this).css('height',$(this).find('.month-schedulelist').height()+$(this).find('.monthdata-container').height());
							$(this).find('.btn-showallmonth').hide();
						}
						
					}
				})
			})
		},
		renderMonthcalendar:function(year,month,date){
			var that= this;
			var this_max_date = that.getMonthday(year,month);
			// 计算星期数
			var firstdate = new Date(year+','+month+',1');
			var firstday = firstdate.getDay(); // 每月1号的星期数
			var trAmount = (firstday + this_max_date)%7 == 0 ? (firstday + this_max_date)/7 : (firstday + this_max_date - (firstday + this_max_date)%7)/7 +1;   // 总共有多少行
			// 显示当前日期
			that.month_showdate.html(year+'年'+((month < 10) ? '0'+parseInt(month,10) : month)+'月');
			// 拼接tbody字段
			var html ='';
			for(var i=0;i<trAmount;i++){
				if(i == 0){	// 第一行
					html +='<tr class="first-tr">';
					for(var j = 1;j < firstday+1;j ++){
						html +='<td class="nomonthday"></td>';	// 第一行中1号之前的是没有的
					}
					var calenda_i = 1;
					for(var j=firstday+1;j<=7;j++){	// 这一天的日期year+'-'+month+'-'+日;
						var mydate = 7*i+calenda_i <10 ? '0'+parseInt(7*i+calenda_i) : 7*i+calenda_i;
						html +='<td><h6 class="monthday"><div class="monthday-text" data-datestamp="'+year+'-'+((month < 10) ? '0'+parseInt(month,10) : month)+'-'+mydate+'"></div><span class="monthday-time" data-date="'+mydate+'">'+(7*i+calenda_i)+'</span></h6><div class="schedulelist-month" data-datestamp="'+year+'-'+((month < 10) ? '0'+parseInt(month,10) : month)+'-'+mydate+'"><div class="month-schedulelist"></div><div class="monthdata-container"></div></div></td>';
						calenda_i++;
					}
					html +='</tr>';
				}else if(i==trAmount-1){	// 最后一行
					html +='<tr class="last-tr">';
					var calenda_i = 8-firstday;
					for(var j=1;j<=7;j++){	// 这一天的日期year+'-'+month+'-'+日;
						if((7*(i-1)+calenda_i) > this_max_date){
							html +='<td class="nomonthday"></td>';	// 最后一行中最大日期之后是没有的
						}else{
							html +='<td><h6 class="monthday"><div class="monthday-text" data-datestamp="'+year+'-'+((month < 10) ? '0'+parseInt(month,10) : month)+'-'+(7*(i-1)+calenda_i)+'"></div><span class="monthday-time" data-date="'+(7*(i-1)+calenda_i)+'">'+(7*(i-1)+calenda_i)+'</span></h6><div class="schedulelist-month" data-datestamp="'+year+'-'+((month < 10) ? '0'+parseInt(month,10) : month)+'-'+(7*(i-1)+calenda_i)+'"><div class="month-schedulelist"></div><div class="monthdata-container"></div></div></td>';
						}
						calenda_i++;
					}
					html +='</tr>';
				}else{	// 中间每一行
					html +='<tr class="middle-tr">';
					var calenda_i = 8 - firstday;
					for(var j=1;j<=7;j++){// 这一天的日期是： year+'-'+month+'-'+日;
						var mydate = 7*(i-1)+calenda_i <10 ? '0'+parseInt(7*(i-1)+calenda_i) : 7*(i-1)+calenda_i;
						html +='<td><h6 class="monthday"><div class="monthday-text" data-datestamp="'+year+'-'+((month < 10) ? '0'+parseInt(month,10) : month)+'-'+mydate+'"></div><span class="monthday-time" data-date="'+mydate+'">'+(7*(i-1)+calenda_i)+'</span></h6><div class="schedulelist-month" data-datestamp="'+year+'-'+((month < 10) ? '0'+parseInt(month,10) : month)+'-'+mydate+'"><div class="month-schedulelist"></div><div class="monthdata-container"></div></div></td>';
						calenda_i++;
					}
					html +='</tr>';
				}
			}
			firstdate = null;
			firstday = null;
			trAmount = null;
			return html;
		},
		initDaycalendar:function(year,month,date){											//日模式下的日历外壳
			var that = this;
			// 拼接外层
			var innertxt ='';
			innertxt +='<div class="jquery-tscalendar-day">';
			innertxt +='<div class="jquery-tscalendar-header day-header">';
			innertxt +='<a class="btn_prev_d btn btn-icon ts-angle-left btn-items" href="javascript:void(0);"></a>';
			innertxt +='<h2 class="nowdate"></h2>';
			innertxt +='<a class="btn_next_d btn btn-icon ts-angle-right btn-items" href="javascript:void(0);"></a>';
			innertxt +='</div>';
			if(that.config.dayresource){
				innertxt +='<div class="jquery-tscalendar-hasleft">';
				innertxt +='<div class="jquery-tscalendar-left">';
				innertxt +='<div class="jquery-tscalendar-container-thead">';
				innertxt +='<table>';
				innertxt +='<thead>';
				innertxt +='<tr>';			
				innertxt +='<th>编排</th>';			
				innertxt +='</tr>';	
				innertxt +='</thead>';
				innertxt +='</table>';
				innertxt +='</div>';
				innertxt +='<table>';
				innertxt +='<tbody>';			
				innertxt +='</tbody>';			
				innertxt +='</table>';
				innertxt +='</div>';				
			}else{
				innertxt +='<div>';
			}
			innertxt +='<div class="jquery-tscalendar-container">';		
			innertxt +='<div class="jquery-tscalendar-container-thead day-table">';
			innertxt +='<div class="xscroll-container"><span class="xscroll-bar"></span></div>';
			innertxt +='<table>';
			innertxt +='<thead>';
			innertxt +='<tr>';
			for(var i =0; i<24; i++){
				innertxt +='<th>'+i+':00</th>';
			}
			innertxt +='</tr>';
			innertxt +='</thead>';
			innertxt +='</table>';
			innertxt +='</div>';
			innertxt +='<div class="jquery-tscalendar-container-tbody day-table">';
			innertxt +='<table>';
			innertxt +='<tbody class="jquery-tscalendar-tbody">';
			innertxt +='</tbody>';
			innertxt +='</table>';
			innertxt +='</div>';
			innertxt +='</div>';	
			innertxt +='</div>';	
			innertxt +='</div>';	
			innertxt +='</div>';	
			that.$target.empty().append(innertxt);
			that.day_header = that.$target.find('.day-header');
			that.day_showdate = that.day_header.find('.nowdate');
			that.day_btnprevm = that.day_header.find('.btn_prev_d');
			that.day_btnnextm = that.day_header.find('.btn_next_d');
			that.day_container = that.day_header.siblings().find('.jquery-tscalendar-container');
			that.day_scroll = that.day_container.find('.xscroll-container');
			that.day_scrollbar = that.day_scroll.find('.xscroll-bar');
			if(that.config.dayresource){
				that.day_resourcecontainer = that.day_header.siblings().find('.jquery-tscalendar-left');
				that.day_resource = that.day_resourcecontainer.find('tbody');
				that.day_parentcontainer = that.day_resourcecontainer.parent();
			}
			that.day_tbody = that.day_container.find('.jquery-tscalendar-tbody');
			that.day_outer = that.$target.find('.jquery-tscalendar-day');

			//点击切换前后天
			that.day_btnprevm.click(function(){
				var prev = that.getPrevcalendar('day',that.year,that.month,that.date);
				that.updateDate(prev);
				if(that.config.dayresource){
					that.updateDaycalendarwithsource(that.year,that.month,that.date,that.day);
				}else{
					that.updateDaycalendar(that.year,that.month,that.date,that.day);
				}
				prev = null;
			});
			that.day_btnnextm.click(function(){
				var next = that.getNextcalendar('day',that.year,that.month,that.date);
				that.updateDate(next);
				if(that.config.dayresource){
					that.updateDaycalendarwithsource(that.year,that.month,that.date,that.day);
				}else{
					that.updateDaycalendar(that.year,that.month,that.date,that.day);
				}
				next = null;
			});
			innertxt = null;
			// 填充tbody
			if(that.config.dayresource){
				that.updateDaycalendarwithsource(that.year,that.month,that.date,that.day);
			}else{
				that.updateDaycalendar(that.year,that.month,that.date,that.day);
			}
			//窗口滚动或者发生尺寸变化时判断是否要固定表头
			$(window).on('scroll resize', function() {
				if($(window).scrollTop()>that.day_outer.offset().top){
					that.day_outer.addClass('fixtop');
				}else{
					that.day_outer.removeClass('fixtop');					
				}
				that.resizeResourcewidth();
			});			
		},
		updateDaycalendar:function(year,month,date,day){
			var that= this;
			that.day_tbody.html(that.renderDaycalendar(year,month,date,day));
			//小于最小日期的日历不可以搜索，如果无设置最小日期则默认是1970-1-1
			var mindate = that.config.minDate ? new Date(Date.parse(that.config.minDate.replace(/-/g,  "/"))):new Date('1970/1/1');
			var daydate = new Date(year+'/'+month+'/'+date);
			if(daydate.getTime() <= mindate.getTime()){
				that.day_btnprevm.addClass('disabled').attr("disabled",true);
			}else{
				that.day_btnprevm.removeClass('disabled').attr("disabled",false);
			}
			//如果有设置最大日期，则大于该日期的日历不可以搜索
			if(that.config.maxDate &&isNaN(that.config.maxDate)&&!isNaN(Date.parse(that.config.maxDate))){
				var maxdate = new Date(Date.parse(that.config.maxDate.replace(/-/g,  "/")));
				if(daydate.getTime() >= maxdate.getTime()){
					that.day_btnnextm.addClass('disabled').attr("disabled",true);
				}else{
					that.day_btnnextm.removeClass('disabled').attr("disabled",false);
				}				
			}
			if(that.config.datasource){
				$.post(that.config.datasource+'?dateType=day&endTime='+year+'-'+month+'-'+date,that.config.datalist? that.config.datalist :{}).success(function(data){
					if(data.dayData && data.dayData.length>0){
						for(var i =0;i<data.dayData.length;i++){
							(function(ind){
								if(that.config.onRenderday && typeof that.config.onRenderday == 'function'){
									that.day_tbody.find(".schedulelist-day[data-datestamp='"+data.dayData[ind].hourTime+"']").each(function(){
										that.config.onRenderday($(this),data.dayData[ind].hourData);
									})
								}								
							})(i);
						}						
					}
					that.resizeResourcewidth();
				}).error(function(data){
					showPopMsg.error('${tk:lang("异常","")}：<br>' + data.Message);
				});			
			}
		},
		renderDaycalendar:function(year,month,date,day){
			var that= this;
			// 显示当前日期
			that.day_showdate.html(year+'年'+((month < 10) ? '0'+parseInt(month,10) : month)+'月'+((date < 10) ? '0'+parseInt(date,10) : date)+'日<small>('+that.weekdate[parseInt(day)]+')</small>');
			// 拼接tbody字段
			var html ='';
			html +='<tr class="day-tr">';
			for(var i =0; i<24; i++){
				html +='<td><div class="schedulelist-day" data-datestamp="'+i+'"></div></td>';
			}
			html +='</tr>';
			return html;
		},
		updateDaycalendarwithsource:function(year,month,date,day){
			var that= this;
			that.renderDaycalendarwithsource(year,month,date,day);
			//小于最小日期的日历不可以搜索，如果无设置最小日期则默认是1970-1-1
			var mindate = that.config.minDate ? new Date(Date.parse(that.config.minDate.replace(/-/g,  "/"))):new Date('1970/1/1');
			var daydate = new Date(year+'/'+month+'/'+date);
			if(daydate.getTime() <= mindate.getTime()){
				that.day_btnprevm.addClass('disabled').attr("disabled",true);
			}else{
				that.day_btnprevm.removeClass('disabled').attr("disabled",false);
			}
			//如果有设置最大日期，则大于该日期的日历不可以搜索
			if(that.config.maxDate &&isNaN(that.config.maxDate)&&!isNaN(Date.parse(that.config.maxDate))){
				var maxdate = new Date(Date.parse(that.config.maxDate.replace(/-/g,  "/")));
				if(daydate.getTime() >= maxdate.getTime()){
					that.day_btnnextm.addClass('disabled').attr("disabled",true);
				}else{
					that.day_btnnextm.removeClass('disabled').attr("disabled",false);
				}				
			}
			if(that.config.datasource){
				$.post(that.config.datasource+'?dateType=day&endTime='+year+'-'+month+'-'+date,that.config.datalist? that.config.datalist :{}).success(function(data){
					if(data.dayData && data.dayData.length>0){
						for(var i =0;i<data.dayData.length;i++){
							(function(ind){
								if(that.config.onRenderday && typeof that.config.onRenderday == 'function'){
									that.day_tbody.find(".schedulelist-day[data-datestamp='"+data.dayData[ind].hourTime+"']").each(function(){
										var myitem = $(this);
										for(var j=0;j<data.dayData[ind].hourData.length;j++){
											if(myitem.data('flowid')== 'none'){
												that.config.onRenderday(myitem,data.dayData[ind].hourData);
											}else if(myitem.data('flowid')== 'flow_'+data.dayData[ind].hourData[j].id){
												var mydata = {};
												mydata.data=data.dayData[ind].hourData[j];
												mydata.datatype='hasresource';
												that.config.onRenderday(myitem,mydata);
											}								
										}
									})
								}								
							})(i);
						}						
					}
					that.resizeResourcewidth();
					if(that.day_tbody.find('.day-hourlylist').length>0){
						$(window).scrollTop(that.day_tbody.find('.day-hourlylist').eq(0).offset().top-that.day_tbody.offset().top);
					}
				}).error(function(data){
					showPopMsg.error('${tk:lang("异常","")}：<br>' + data.Message);
				});			
			}
		},
		renderDaycalendarwithsource:function(year,month,date,day){
			var that = this;
			// 显示当前日期
			that.day_showdate.html(year+'年'+((month < 10) ? '0'+parseInt(month,10) : month)+'月'+((date < 10) ? '0'+parseInt(date,10) : date)+'日<small>('+that.weekdate[parseInt(day)]+')</small>');
			$.ajax({type:'post',url:that.config.dayresource,data:that.config.datalist? that.config.datalist :{},async:false,success:function(data){ 
				if(data && data.length>0){
					var html ='';
					var righthtml ='';
					for(var i =0;i<data.length;i++){
						(function(ind){
							if(data[ind].children.length>0){
								html +='<tr data-id="'+data[ind].id+'">';
								html +='<td><div data-id="'+data[ind].id+'" class="ts ts-angle-down hasnextsource resource-data overflow">'+data[ind].title+'</div></td>';
								html +='</tr>';	
								righthtml +='<tr>';
								for(var r =0; r<24; r++){
									righthtml +='<td><div class="schedulelist-day" data-datestamp="'+r+'" data-flowid="'+data[ind].id+'"></div></td>';
								}
								righthtml +='</tr>';
								for(var j=0;j<data[ind].children.length;j++){
									html +='<tr data-parent="'+data[ind].id+'">';
									html+='<td><div data-id="'+data[ind].children[j].id+'" class="resource-data overflow" style="padding-left:25px;">'+data[ind].children[j].title+'</div></td>';
									html +='</tr>';
									righthtml +='<tr data-parent="'+data[ind].id+'">';
									for(var s =0; s<24; s++){
										righthtml +='<td><div class="schedulelist-day" data-datestamp="'+s+'" data-flowid="'+data[ind].children[j].id+'"></div></td>';
									}
									righthtml +='</tr>';
								}									
							}else{
								html +='<tr>';
								html+='<td><div data-id="'+data[ind].id+'" class="resource-data overflow">'+data[ind].title+'</div></td>';
								html +='</tr>';	
								righthtml +='<tr>';
								for(var t =0; t<24; t++){
									righthtml +='<td><div class="schedulelist-day" data-datestamp="'+t+'" data-flowid="'+data[ind].id+'"></div></td>';
								}
								righthtml +='</tr>';
							}
						})(i);
					}
					that.day_resource.html(html);
					that.day_tbody.html(righthtml);
					that.resizeResourcewidth();
					that.day_resource.find('.hasnextsource').on('click',function(){
						var myid = $(this).data('id');
						if($(this).hasClass('ts-angle-right')){
							$(this).removeClass('ts-angle-right').addClass('ts-angle-down');
							$(this).parents('tr').siblings('[data-parent="'+myid+'"]').show();
							that.day_tbody.find('tr[data-parent="'+myid+'"]').show();
						}else{
							$(this).removeClass('ts-angle-down').addClass('ts-angle-right');
							$(this).parents('tr').siblings('[data-parent="'+myid+'"]').hide();
							that.day_tbody.find('tr[data-parent="'+myid+'"]').hide();
						}	
						that.resizeResourcewidth();
					});
				}else{
					var html ='';
					var righthtml ='';
					html +='<tr>';
					html+='<td><div class="resource-data overflow" data-id="none">暂无数据</div></td>';
					html +='</tr>';	
					righthtml +='<tr>';
					for(var t =0; t<24; t++){
						righthtml +='<td><div class="schedulelist-day" data-datestamp="'+t+'" data-flowid="none"></div></td>';
					}
					righthtml +='</tr>';
					that.day_resource.html(html);
					that.day_tbody.html(righthtml);
					that.resizeResourcewidth();
				}	
			},error:function(data){
				showPopMsg.error('${tk:lang("异常","")}：<br>' + data.Message);
			}});	
		},
		reloadSource:function(datalist){
			var that = this;
			console.log(that);
			that.config.datalist = datalist;
			$(window).scrollTop(0);
			if(that.type =='month'){
				that.updateMonthcalendar(that.year,that.month,that.date);
			}else if(that.type =='day'){
				if(that.config.dayresource){
					that.updateDaycalendarwithsource(that.year,that.month,that.date,that.day);
				}else{
					that.updateDaycalendar(that.year,that.month,that.date,that.day);
				}
			}	
			
			
		},
		resizeResourcewidth:function(){
			var that =this;
			that.isDragx = false;
			that.offsetLeft = 0;
			if(that.config.dayresource){
				var leftWidth = that.day_resourcecontainer.find('tbody').width();
				that.offsetLeft = leftWidth;
				that.day_resourcecontainer.find('.jquery-tscalendar-container-thead').width(leftWidth+1);
				that.day_resourcecontainer.find('.jquery-tscalendar-container-thead').find('table').width(leftWidth+1);
				that.day_parentcontainer.find('.day-table').css('padding-left',leftWidth);	
				that.day_resource.find('.resource-data').each(function(){
					var resourceid = $(this).data('id');
					$(this).parents('tr').height($('.schedulelist-day[data-flowid="'+resourceid+'"]').parents('tr').height());
					
				});
				if(that.day_outer.hasClass('fixtop')){
					that.day_scroll.css('cssText','left:'+ (leftWidth+32) +'px !important');
				}else{
					if(that.scrollLeft){
						that.day_scroll.css({
							'left' : leftWidth+that.scrollLeft,
							'right': that.offsetLeft-leftWidth-that.scrollLeft
						});
					}else{
						that.day_scroll.css({
							'left' : leftWidth
						});
					}
				}
			}
			that.day_scrollbar.css({
				'left' : Math.min(that.scrollLeft*(that.day_scroll.width()-that.day_scrollbar.width())/(that.day_tbody.width()-that.day_scroll.width()),(that.day_scroll.width()-that.day_scrollbar.width())),
				'width': Math.max(40, (that.day_container.width()-that.offsetLeft)*(that.day_container.width()-that.offsetLeft)/that.day_tbody.width())
			});
			if(that.day_tbody.width()+that.offsetLeft>that.day_container.width()){
				that.day_scroll.show();
			}else{
				that.day_scroll.hide();
			}
			if(that.scrollLeft){
				that.day_container.find('.jquery-tscalendar-container-thead').scrollLeft(that.scrollLeft);
			}
			if(that.day_container.scrollLeft() > (that.day_tbody.width()+that.offsetLeft-that.day_container.width())){
				that.day_container.scrollLeft(that.day_tbody.width()+that.offsetLeft-that.day_container.width());
			}
			
			that.day_scrollbar.on('mousedown', function(e) {
				that.goScrollx(e);
			});
		},
		goScrollx:function(e){
			var that = this;
			e.stopPropagation();
			if (!that.isDragx) {
				that.isDragx = true;
				var formalL = that.day_scrollbar.position().left;
				var outerW = that.day_tbody.width()-that.day_scroll.width();
				var innerW = that.day_scroll.width()-that.day_scrollbar.width();
				var deltaX = e.clientX;
				that.day_scroll.addClass('active');
				$(document).bind('mousemove', function(e) {
					e.preventDefault();
					if (that.isDragx) {
						if (!e) {
							e = window.event;
						}
						var nx = e.clientX ;
						var scrollL = Math.min(Math.max(formalL + nx - deltaX ,0), innerW);
						that.day_scroll.addClass('active');
						that.day_scrollbar.css({'left' : scrollL});
						that.scrollLeft = outerW* scrollL/innerW;
						that.day_container.scrollLeft(that.scrollLeft);
						if(that.day_outer.hasClass('fixtop')){
							that.day_scroll.css('cssText','left:'+ (that.offsetLeft+32) +'px !important;');
							that.day_container.find('.jquery-tscalendar-container-thead').scrollLeft(that.scrollLeft);
						}else{
							that.day_scroll.css({
								'left' : outerW * scrollL/innerW + that.offsetLeft,
								'right': -outerW * scrollL/innerW
							});
							that.day_container.find('.jquery-tscalendar-container-thead').scrollLeft(0);
						}
					}
				});
				$(document).bind('mouseup mouseleave', function(e) {
					if(that.isDragx){
						that.isDragx = false;
						that.day_scroll.removeClass('active');
					}
				});
			}
		},
		initWeekcalendar:function(year,month,date){											//周模式下的日历外壳
			var that = this;
			// 拼接外层
			var innertxt ='';
			innertxt +='<div class="jquery-tscalendar-header week-header">';
			innertxt +='<a class="btn_prev_w btn btn-icon ts-angle-left btn-items" href="javascript:void(0);"></a>';
			innertxt +='<h2 class="nowdate"></h2>';
			innertxt +='<a class="btn_next_w btn btn-icon ts-angle-right btn-items" href="javascript:void(0);"></a>';
			innertxt +='</div>';			
			innertxt +='<div class="jquery-tscalendar-container">';		
			innertxt +='<table>';
			innertxt +='<thead>';
			innertxt +='<tr>';
			innertxt +='<th class="weekend">'+that.weekdate[0]+'</th>';
			for(var i =1; i<that.weekdate.length-1; i++){
				innertxt +='<th>'+that.weekdate[i]+'</th>';
			}
			innertxt +='<th class="weekend">'+that.weekdate[that.weekdate.length-1]+'</th>';
			innertxt +='</tr>';
			innertxt +='</thead>';
			innertxt +='<tbody class="jquery-tscalendar-tbody">';
			innertxt +='</tbody>';
			innertxt +='</table>';
			innertxt +='</div>';	
			innertxt +='</div>';	
			that.$target.empty().append(innertxt);
			that.week_header = that.$target.find('.week-header');
			that.week_showdate = that.week_header.find('.nowdate');
			that.week_btnprevm = that.week_header.find('.btn_prev_w');
			that.week_btnnextm = that.week_header.find('.btn_next_w');
			that.week_container = that.week_header.siblings('.jquery-tscalendar-container');
			that.week_tbody = that.week_container.find('.jquery-tscalendar-tbody');
			// 填充tbody
			that.updateWeekcalendar(that.year,that.month,that.date);
			//点击切换上下月
			that.week_btnprevm.click(function(){
				that.updateWeekcalendar(that.year,that.month,that.date);
			});
			that.week_btnnextm.click(function(){
				that.updateWeekcalendar(that.year,that.month,that.date);
			});
			innertxt = null;			
		},
		updateWeekcalendar:function(year,month,date){
			var that= this;
			that.week_tbody.html(that.renderWeekcalendar(year,month,date));
		},
		renderWeekcalendar:function(year,month,date){
			var that= this;
			var html ='';
			//当前日期星期几
			var nowdate = new Date(year+','+month+','+date);
			var nowday = nowdate.getDay();
			//当前月份第一天星期几
			var firstdate = new Date(year+','+month+',1');
			var firstday = firstdate.getDay(); 
			//当前月份最后一天星期几
			var lastdate = new Date(year+','+month+','+that.getMonthday(year,month));
			var lastday = lastdate.getDay(); 
			html +='<tr class="week-tr">';
			if(date<8-firstday){	//该周是当月第一周有上一个月的日期
				for(var i=1;i<=7;i++){
					if(i<firstday+1){
						html +='<td><h6 class="weekday">'+(that.getMonthday(year,month)-firstday+i)+'</h6></td>';
					}else{
						html +='<td><h6 class="weekday" data-timestamp="'+year+'-'+month+'-'+(i - firstday)+'">'+(i - firstday)+'</h6></td>';
					}
				}
			}else if(that.getMonthday(year,month)-date<6){	//该周有下一个月的日期
				for(var i=1;i<=7;i++){
					if(i>lastday+1){
						html +='<td><h6 class="weekday">'+(i - lastday -1)+'</h6></td>';
					}else{
						html +='<td><h6 class="weekday" data-timestamp="'+year+'-'+month+'-'+(that.getMonthday(year,month)-lastday + i - 1)+'">'+(that.getMonthday(year,month)-lastday + i - 1)+'</h6></td>';
					}
				}				
			}else{
				for(var i=1;i<=7;i++){
					html +='<td><h6 class="weekday" data-timestamp="'+year+'-'+month+'-'+(date - nowday + i - 1)+'">'+(date - nowday + i - 1)+'</h6></td>';
				}				
			}
			html +='</tr>';
			// 显示当前日期
			//that.month_showdate.html(year+'年'+((month < 10) ? '0'+parseInt(month,10) : month)+'月'+((date < 10) ? '0'+parseInt(date,10) : date)+'日');
			return html;
		},
		getMonthday:function(year,month){			// 计算每月最大天数
		    var d = new Date(year, month, 0);
		    return d.getDate();
		},	
		getPrevcalendar:function(type,year,month,date){
			var that =this;
			var myDate = new Date(year+'/'+month+'/'+date);
			var prevDay;
			if(type=="month"){
				if(month==1){
					month = 12;
					year = year - 1;
				}else{
					month = month - 1;
				}
				myDate = new Date(year, month-1, 1);
			}else if(type=="week"){
				prevDay = myDate.getDate() - 7;
				myDate.setDate(prevDay);	
			}else if(type=="threeDay"){
				prevDay = myDate.getDate() - 3;
				myDate.setDate(prevDay);					
			}else if(type=="day"){
				prevDay = myDate.getDate() - 1;
				myDate.setDate(prevDay);			
			}else if(type=="overview"){
				
			}
			return myDate;
		},
		getNextcalendar:function(type,year,month,date){
			var that =this;
			var myDate = new Date(year+'/'+month+'/'+date);
			var nextDay;
			if(type=="month"){
				nextDay = new Date(year, month, 1);
				myDate = nextDay;
			}else if(type=="week"){
				nextDay = myDate.getDate() + 7;
				myDate.setDate(nextDay);	
			}else if(type=="threeDay"){
				nextDay = myDate.getDate() + 3;
				myDate.setDate(nextDay);					
			}else if(type=="day"){
				nextDay = myDate.getDate() + 1;
				myDate.setDate(nextDay);			
			}else if(type=="overview"){
				
			}
			return myDate;
		},
		updateDate:function(date){
			var that = this;
			that.month=parseInt(date.getMonth())+1;
			that.year=parseInt(date.getFullYear());
			that.date=parseInt(date.getDate());
			that.day =parseInt(date.getDay());
		}
	};
	$.fn.tscalendar = function(config) {
		var $target = $(this);
		var d = null;
		if (!$target.attr('bind-tscalendar')) {
			d = new tscalendar($target,config);
			$target.attr('bind-tscalendar', true);
			$target.data('tscalendar', d);
		} else {
			d = $target.data('tscalendar');
		}
		return d;
	};
})(jQuery);
