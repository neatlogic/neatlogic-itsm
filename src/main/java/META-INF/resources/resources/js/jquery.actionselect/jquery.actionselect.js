;(function($){
	var ActionSelect = function(target,config){
		this.defaultconfig={
			textkey : 'text',
			valuekey : 'value',
			multiple:false,//多选参数，这个功能暂时支持单选
			data:[],
			url:null,//如果data存在的话则url使用无效
			sourceroot:null,//通过url获取的的数时，拿去数据的根元素
			placeholder:'请选择...',
			deletText:null,//如果删除需要有提示框，显示提示内容
			defaultvalue:[],//默认选中的数据，之后是选中的数据
			saveFn:null,//保存数据时调用的方法，返回值true | false
			deletFn:null//删除数据时调用的方法，返回值true | false
		}
		this.config = $.extend({}, this.defaultconfig, config);
		this.$target = target;
		
		var config = this.config;
		//获取默认选中数据
		this.$target.find("option:selected").each(function(i,item){
			config.defaultvalue.push({value:$(item).attr('value'),text:$(item).html().trim()});
			if(!config.multiple && config.defaultvalue.length>=1){//目前暂时支持单选
				return false;
			}
		});
		
		this.$target.hide();
		this.init();
		this.$target.data('actionSelect',this);
	};
	
	ActionSelect.prototype.init=function(){
		var that = this;
		var html = xdoT.render('common.plugin.actionselect.actionselect', that.config);
		that.$target.wrap(html); 
		that.$wrap=that.$target.parent('.actionselect-container');
		that.$opContent=that.$wrap.find('.op-content');
		
		that.$wrap.on('input','.iptCheckTrim',function(){//输入框的为空处理校验
			var $this = $(this);
			var value = $this.val().trim();
			if(!value){
				$this.next('button').addClass('disabled');
			}else{
				$this.next('button').removeClass('disabled');
			}
		});
		
		that.$wrap.on('keydown','.iptCheckTrim',function(event){//输入框输入enter键的操作
			var $this = $(this);
			var value = $this.val().trim();
			if(value){
				var $nextButton =$this.next('button');
				if(event.keyCode==13){
					$nextButton.trigger('click');
					event.stopPropagation(); 
				}
			}
			event.stopPropagation();
		});
		
		that.$wrap.on('keydown',function(event){//输入框输入enter键的操作
				var $preSelect = that.$opContent.find('li.preSelect');
				var isopen = that.$opContent.hasClass('open');
				switch(event.keyCode){
					case 38://up
						if(!isopen){
							break;
						}
						$preSelect.length<=0 && ($preSelect=that.$opContent.find('li.selected').first());
						var $upSelect=null;
						if($preSelect.length>0){
							$upSelect =  $preSelect.prev('li');
							$upSelect.length>0 && $upSelect.addClass('preSelect') && $preSelect.removeClass('preSelect');
						}else{
							$upSelect = that.$opContent.find('li').last().addClass('preSelect');
						}
						$upSelect && $upSelect[0] && that.$opContent.find('ul.ul-content').scrollTop($upSelect[0].offsetTop-50);
						break;
					case 40: //down
						if(!isopen){
							break;
						}
						$preSelect.length<=0 && ($preSelect=that.$opContent.find('li.selected').first());
						var $downSelect=null;
						if($preSelect.length>0){
							$downSelect =  $preSelect.next('li');
							$downSelect.length>0 && $downSelect.addClass('preSelect') && $preSelect.removeClass('preSelect');
						}else{
							$downSelect=that.$opContent.find('li').first().addClass('preSelect');
						}
						$downSelect && $downSelect[0] && that.$opContent.find('ul.ul-content').scrollTop($downSelect[0].offsetTop-50);
						break;
					case 13: //enter
						if(!isopen){
							that.open();
							break;
						}else if($preSelect.length>0 && isopen){
							$preSelect.trigger('click');
						}else{
							that.close();
						}
					default:return;
			     }
			    event.preventDefault();
		});
		
		that.$opContent.on('click','.editSpanAction',function(event){//编辑按钮
			 $(this).closest('li').addClass('edit');
			 var obj=$(this).closest('li').find('input')[0];
			 that.focus(obj,obj.value.length);
			 event.stopPropagation();
		});
		
		that.$opContent.on('click','.delSpanAction',function(event){//删除按钮
			var $delItem = $(this);
			var returnVal=true;
			var $li=$delItem.closest('li');
			if(that.config.deletText){//使用内部弹出框的目的是为了让删除的接口返回的数据可以被接收
				createModalDialog({
					msgtitle : '删除确认',
					msgcontent : that.config.deletText,
					blurclose:false,
					type:'del',
					successFuc : function(event) {
						if(that.config.deletFn && typeof that.config.deletFn=='function'){
			            	returnVal = that.config.deletFn($li.data('value'));
			            }
						returnVal && that.removeItem($li);
						event.stopPropagation();
					},
					cancelFuc:function(event){
						event.stopPropagation();
					}
				});
			}else{
				if(that.config.deletFn && typeof that.config.deletFn=='function'){
	            	returnVal = that.config.deletFn($li.data('value'));
	            }
				returnVal && that.removeItem($li);
			} 
			event.stopPropagation();
		});
		
		
		that.$opContent.on('click','.btnAddAction',function(event){//保存时调用的方法
			var $this = $(this);
            if($this.hasClass('disbled')){
            	return false;
            }
            var $input=$this.prev('input');
            var text = encodeHtml($input.val().trim());
            var value = $input.data('value') || null;
            var returnVal = text;
            
            if(that.config.saveFn && typeof that.config.saveFn=='function'){
            	returnVal = that.config.saveFn(text,value);
            }
            
            if(returnVal!=false && value!=null){//编辑
        		that.editItem($this.closest('li'),text,value);
        	}else if(returnVal!=false && value==null){//新增
        		that.addItem(text,returnVal);
        	}
            that.$wrap.focus();
            event.stopPropagation(); 
		});
		
		that.$opContent.on('click','li',function(){
			var $this = $(this);
			if($this.hasClass('selected') || $this.hasClass('edit')){
				return false;
			}else{
				that.$opContent.find('li.selected').removeClass('selected');
				$this.addClass('selected');
				var value={value:$this.data('value'),text:$this.find('label>span').html().trim()};
				that.$wrap.find('.caption').empty().html('<span class="option-item '+(value.value==''?'empty':'')+'" data-value="'+value.value+'">'+value.text+'</span>');
				that.config.defaultvalue=[value];
				var optionSelected = '';
				that.config.defaultvalue.forEach(function(item){
					optionSelected=optionSelected+'<option value="'+item.value +'" selected>'+item.text+'</option>';
				});
				that.$target.empty().html(optionSelected);
				that.close();
			}
		});
		
		that.$wrap.on('click','.caption',function(event){//打开和关闭下拉框
			if(that.$opContent.hasClass('open')){
				that.close();
			}else{
				that.open();
			}
		});
		
		that.$wrap.on('click',function(event){//阻止事件传播
			 event.stopPropagation();  
		});
		
		$(document).on('click',function(){
		   $('body>.tsModalDialog').length<=0 && that.$opContent.hasClass('open') && that.close();
		});	
	};
	
	
	ActionSelect.prototype.close=function(){//关闭弹出框
		var that = this;
		that.$opContent.removeClass('open');
		that.$opContent.find('.ul-content').empty();
		that.$opContent.find('.add-content>input').val('');
	};
	
	ActionSelect.prototype.open=function(){//每次打开都会调用一次接口渲染数据
		var that = this;
		if(that.config.url){
			$.ajaxSetup({async:false});
			$.getJSON(that.config.url,function(data){
				that.config.data=(that.config.sourceroot?data[that.config.sourceroot]:data);
			});
			that.config.mustinput=true;
			if(!that.config.multiple && !that.$target.hasClass('mustinput')){//目前暂时支持单选
				that.config.mustinput=false;
			}
			$.ajaxSetup({async:true});
		}
		that.config.actiontype='init';
		var html = xdoT.render('common.plugin.actionselect.actionselectul', that.config);
		var $ulContent=that.$opContent.find('.ul-content').empty().html(html);
		that.$opContent.addClass('open');
		var $liSelected = that.$opContent.find('li.selected');
		if($liSelected.length>0){
			var scrollTop = $liSelected[0].offsetTop-50;	
			that.$opContent.find('.ul-content').scrollTop(scrollTop);
		}
	};
	
	ActionSelect.prototype.addItem=function(text,value){//添加的时候调用的方法
		var that = this;
		var item={};
		item[that.config.textkey]=text;
		item[that.config.valuekey]=value;
		var itemJson = {data:[item],textkey :that.config.textkey,valuekey : that.config.valuekey,actiontype:'add'};
		var html = xdoT.render('common.plugin.actionselect.actionselectul', itemJson);
		var $ul=that.$opContent.find('ul.ul-content');
		$ul.append(html);
		$ul.scrollTop($ul[0].scrollHeight);
		that.$opContent.find('.add-content>input').val('');
	};
	
	ActionSelect.prototype.editItem=function($li,text,value){//编辑时调用的方法
		var that = this;
		that.$wrap.find('.caption>.option-item').each(function(){
			if($(this).data('value')==value){
				$(this).html(text);
				that.changeDefaultvalue(value,text);
				return false;
			} 
		});
		$li.find('label>span').html(text);
		$li.find('.hideInputContent>input').data('oldvalue',text);
		$li.removeClass('edit');
	};
	
	ActionSelect.prototype.removeItem=function($li){//移除时调用的方法
		var that = this;
		var value=$li.data('value');
		that.$wrap.find('.caption>.option-item').each(function(){
			$(this).data('value')==value && $(this).remove();
			that.config.defaultvalue.forEach(function(item,i){
				if(item.value==value){
					that.config.defaultvalue.splice(i,1);
					that.$target.find('option[value='+value+']').remove();
					return false;
				}
			});
			return false;
		});
		$li.remove();
	};
	
	ActionSelect.prototype.changeDefaultvalue=function(value,text){//更改选中值
		var that = this;
		that.config.defaultvalue.forEach(function(item,i){
			if(item.value==value){
				that.config.defaultvalue[i].text=text;
			}
		});
	};
	
	
	ActionSelect.prototype.focus=function(obj,end){
		if(obj.createTextRange){
            var range = obj.createTextRange();
		        range.collapse(true);
		        range.moveEnd('character', end);
		        range.moveStart('character', end);
             range.select();
		}else{
	        obj.focus();
	        obj.setSelectionRange(end, end);
		};				
	};
	
	this.ActionSelect=ActionSelect;
	$.fn.actionSelect = function(config){
		var $target = $(this);
		if (!$target.attr('bind-actionselect')) {
			var c = new ActionSelect($target, config);
			$target.attr('bind-actionselect', true);
		}
		return this;
	}
})(jQuery);