;
(function($) {
	var timeSelect = function(target, config) {
		this.defaultconfig = {
			'width':null,
			'valueKey' : 'value', // 存的值的key
			'textKey' : 'text', // 显示文字的key
			'placeholder' : '请选择...',
			'defaultValue' :null, //{'timeRange':'360','startTime':'2019-11-12 10:20','endTime':'2019-11-12 12:20'}
			'valueList':[{
				'value':'0',
				'text':'实时',
				'icon':'ts-pulse'
			},{
				'value':'60',
				'text':'最近1小时',
				'icon':'ts-timer'
			},{
				'value':'180',
				'text':'最近3小时',
				'icon':'ts-timer'
			},{
				'value':'360',
				'text':'最近6小时',
				'icon':'ts-timer'
			},{
				'value':'720',
				'text':'最近12小时',
				'icon':'ts-timer'
			},{
				'value':'1440',
				'text':'最近一天',
				'icon':'ts-timer'
			},{
				'value':'10080',
				'text':'最近一周',
				'icon':'ts-timer'
			},{
				'value':'43200',
				'text':'最近一月',
				'icon':'ts-timer'
			}],
			'url' : null,	//下拉获取url，数据格式为{status:'ok',data:[...valueList]}
			'switchRange': false	//左右切换时间段
		};
		this.config = $.extend({}, this.defaultconfig, config);
		this.$target = target;
		if(this.$target.val()){
			this.config.defaultValue = this.$target.val();
		}
		this.$target.addClass('tstimeselect-input');
		this.$target.wrap('<div class="tstimeselect-wrapper"></div>');
		this.$wrap = this.$target.closest('.tstimeselect-wrapper');
		if(this.config.switchRange){
			this.$wrap.addClass('isSwitch');
		}
		this.$wrap.prepend('<div class="tstimeselect-shower"><div class="tstimeselect-txt">'+ (!this.config.defaultValue ? '<span style="opacity:.8;">'+ this.config.placeholder +'</span>':'') +'</div></div>');
		this.$shower = this.$target.siblings('.tstimeselect-shower');
		this.$showtxt = this.$shower.find('.tstimeselect-txt');
		this.init();
	};
	this.timeSelect = timeSelect;
	timeSelect.prototype = {
		init : function() {
			var that = this;
			if(that.$target.attr('readonly')){
				that.$wrap.addClass('readonly');
			}else{
				if(this.config.switchRange){
					that.$shower.append('<div class="tstimeselect-icon ts-angle-left prevbtn disabled"></div><div class="tstimeselect-icon ts-angle-right nextbtn disabled"></div>');
					that.$prevbtn = that.$shower.find('.prevbtn');
					that.$nextbtn = that.$shower.find('.nextbtn');
					that.$prevbtn.click(function(e){
						e.stopPropagation();
						that.switchVal();
					});
					that.$nextbtn.click(function(e){
						e.stopPropagation();
						if(!$(this).hasClass('disabled')){
							that.switchVal('next');
						}
					});
				}
				that.$wrap.append('<div class="tstimeselect-dropdown"></div>');
				that.$dropdown = that.$target.siblings('.tstimeselect-dropdown');
				if(that.config.url){
					$.get(url,function(data){
						if(data && data.status=="OK"){
							that.initValue(data.data);
						}else{
							console.error('获取时间设置失败！');
						}
					})
				}else{
					that.initValue(that.config.valueList);
				}
				that.$target.change(function(){
					if(that.config.switchRange){
						if($(this).val()){
							var myVal = JSON.parse($(this).val());
							if(myVal.timeRange ==0){
								that.$prevbtn.addClass('disabled');
								that.$nextbtn.addClass('disabled');									
							}else{
								that.$prevbtn.removeClass('disabled');
							}
						}else{
							that.$prevbtn.addClass('disabled');
							that.$nextbtn.addClass('disabled');							
						}
					}
				});
				that.$shower.click(function(){
					that.$wrap.toggleClass('ondrop');
					if(that.$wrap.offset().top+30>$(window).height() && that.$wrap.offset().top>300){
						that.$dropdown.addClass('ontop');
					}else{
						that.$dropdown.removeClass('ontop');
					}
					if(that.$wrap.offset().left+400 > $(window).width()){
						that.$dropdown.addClass('onleft');
					}else{
						that.$dropdown.removeClass('onleft');
					}
				});
				$(document).on('click',function(e){
					var ishide = true;
					if(e.target){
						if(($(e.target).parents('.tstimeselect-wrapper').length>0 && $(e.target).parents('.tstimeselect-wrapper').is(that.$wrap)) || ($(e.target).is(that.$wrap))){
							ishide =false;
						}
					}
					if(ishide){
						that.$wrap.removeClass('ondrop');
					}
				});
			}
		},
		initValue : function(val){
			var that = this;
			that.timeid = new Date().getTime();
			if(val && val.length>0){
				var html ='';
				html +='<div class="tstimeselect-ul">';
				for(var i =0;i<val.length;i++){
					var valArr = val[i];
					html +='<div class="tstimeselect-li tstimeselect-option'+ (that.config.defaultValue === valArr[that.config.valueKey]? 'selected' :'') +'"';
					for(var k in valArr){
						html +=' data-'+ k +'="'+ valArr[k] +'"';
					}
					html +='>'+(valArr.icon?'<i class="'+valArr.icon+'" style="margin-right:3px;"></i>':'') + valArr[that.config.textKey] +'</div>';
				}
				html +='</div>';
				html +='<div class="tstimeselect-li tstimeselect-range">';
				html +='<span class="timer-wrapper"><input type="text" class="Wdate tstimeselect-timer timer-start" id="timerstart_'+that.timeid+'"/><i class="timer-txt">开始时间</i></span>';
				html +='<span style="margin:0 4px;">-</span>';
				html +='<span class="timer-wrapper"><input type="text" class="Wdate tstimeselect-timer timer-end" id="timerend_'+that.timeid+'"/><i class="timer-txt">结束时间</i></span>';
				html +='<span class="btn btn-primary btnConfirm">确定</span></div>';
				that.$dropdown.html(html);
				that.$dropdown.find('.timer-start').focus(function(){
					WdatePicker({
						isShowClear:false,
						isShowOK:false,
						qsEnabled:false,
						isShowToday:false,
						autoPickDate:true,
						dateFmt:'yyyy-MM-dd HH:mm'
					})
				});
				that.$dropdown.find('.timer-end').focus(function(){
					WdatePicker({
						isShowClear:false,
						isShowOK:false,
						qsEnabled:false,
						isShowToday:false,
						autoPickDate:true,
						dateFmt:'yyyy-MM-dd HH:mm',
						minDate:'#F{$dp.$D(\'timerstart_'+that.timeid+'\')}'
					})
				});
			}else{
				that.$dropdown.html('<div class="emptydatatips">暂无时间选项</div>');
			}
			that.$dropdown.find('.timer-txt').click(function(){
				$(this).siblings('input').focus();
			});
			
			that.$dropdown.find('input').on('change input',function(){
				if($(this).val()){
					$(this).siblings('.timer-txt').addClass('on');
				}else{
					$(this).siblings('.timer-txt').removeClass('on');
				}				
			});
			that.$dropdown.find('input').on('focus',function(){
				$(this).siblings('.timer-txt').addClass('on');
			});
			
			that.$dropdown.find('.tstimeselect-option').click(function(){
				var myVal = $(this).data('value');
				var myTxt = $(this).html();
				var valString = {'timeRange':myVal};
				$(this).addClass('selected').siblings('.tstimeselect-option').removeClass('selected');
				that.$showtxt.html(myTxt);
				if($(this).data()){
					var datal = $(this).data();
					for(var k in datal){
						that.$target.data(k,datal[k]);
					}
				}
				that.$target.val(JSON.stringify(valString));
				that.$target.trigger('change');
				that.$wrap.removeClass('ondrop');
				myVal = null;
				myTxt = null;
				valString = null;
			});	
			that.$dropdown.find('.btnConfirm').click(function(){
				var valString = {'startTime':$(this).parent().find('.timer-start').val(),'endTime':$(this).parent().find('.timer-end').val()};
				var showtxt = '<span class="small">'+ (valString.startTime ? valString.startTime :'任意开始时间') + '</span><span style="margin:0 4px;">-</span><span class="small">' + (valString.endTime ? valString.endTime :'任意结束时间') +'</span>';
				that.$dropdown.find('.tstimeselect-option.selected').removeClass('selected');
				that.$showtxt.html(showtxt);
				that.$target.data('timerange',null);
				that.$target.val(JSON.stringify(valString));
				that.$target.trigger('change');
				that.$wrap.removeClass('ondrop');
				valString = null;
				showtxt = null;
			});
			
		},
		switchVal:function(type){
			var type = type ||'prev';
			var that = this;
			var timer = JSON.parse(that.$target.val());
			if(timer){
				that.$dropdown.find('.tstimeselect-option.selected').removeClass('selected');
				if(type == 'prev'){
					if(timer.timeRange){
						var range = timer.timeRange*60*1000;
						var startTime = that.getDate(new Date().getTime() - range);
						var endTime = that.getDate(new Date().getTime());
					}else{
						var range = that.$target.data('timerange')? that.$target.data('timerange')*60*1000 : 86*60*60*1000;		//不是范围的默认86小时
						var startTime = that.getDate(new Date(timer.startTime).getTime() - range);
						var endTime = that.getDate(new Date(timer.endTime).getTime() - range);
					}	
					if(new Date(endTime).getTime() < new Date().getTime()){
						that.$nextbtn.removeClass('disabled');
					}
					var valString = {'startTime':startTime,'endTime':endTime};
					that.$target.val(JSON.stringify(valString));
					that.$showtxt.html('<span class="small">'+ (startTime ? startTime :'任意开始时间') + '</span><span style="margin:0 4px;">-</span><span class="small">' + (endTime ? endTime :'任意结束时间') +'</span>');
					range = null;
					startTime = null;
					endTime = null;
					valString = null;
				}else{
					var nowdate= new Date().getTime();
					var oldendTime = new Date(timer.endTime).getTime();
					var range = that.$target.data('timerange') ? that.$target.data('timerange')*60*1000: 86*60*60*1000;		//不是范围的默认86小时
					var endTime = (oldendTime + range) > nowdate ? that.getDate(nowdate) : that.getDate(oldendTime + range);	
					var startTime = (oldendTime + range) > nowdate ? that.getDate(nowdate - range) : that.getDate(oldendTime);	
					if(oldendTime + range >= nowdate || that.getDate(oldendTime + range)== that.getDate(nowdate)){
						that.$nextbtn.addClass('disabled');
					}
					var valString = {'startTime':startTime,'endTime':endTime};
					that.$target.val(JSON.stringify(valString));
					that.$showtxt.html('<span class="small">'+ (startTime ? startTime :'任意开始时间') + '</span><span style="margin:0 4px;">-</span><span class="small">' + (endTime ? endTime :'任意结束时间') +'</span>');
					nowdate = null;
					oldendTime = null;
					range = null;
					startTime = null;
					endTime = null;
					valString = null;					
				}
				that.$target.trigger('change');
				
			}			
			
		},
		getDate:function(date){
			if (date) {
				var date = new Date(date);
				Y = date.getFullYear() + '-';
				M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '-';
				D = date.getDate() < 10 ? '0' + date.getDate() + ' ' : date.getDate() + ' ';
				h = date.getHours() < 10 ? '0' + date.getHours() + ':' : date.getHours() + ':';
				m = date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes();
				//s = date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds();
				return Y + M + D + h + m;
			} else {
				return date;
			}
		}
	};

	$.fn.timeselect = function(config) {
		var $target = $(this);
		if (!$target.attr('bind-timeselect')) {
			var c = new timeSelect($target,config);
			$target.attr('bind-timeselect', true);
		}
		return this;
	};
//
//	$(function() {
//			$('[plugin-timeselect]').each(function() {
//				$(this).timeselect();
//			});
//
//			$(document).bind("ajaxComplete", function(e, xhr, settings) {
//				$('[plugin-timeselect]').each(function() {
//					$(this).timeselect();
//				});
//			});
//
//			$(document).on('xdoTRender', function(e, content) {
//				var $content = $(content);
//				$content.find('[plugin-timeselect]').each(function() {
//					$(this).timeselect();
//				});
//			});			
//	});
})(jQuery);



