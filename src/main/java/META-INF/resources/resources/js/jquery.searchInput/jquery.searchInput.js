;
(function($) {//失去焦点问题 
	var searchInput = function(target, options) {
		var that = this;// 绑定的元素
		that.$target = target.css('display','none');
		that.$targetDiv = that.$target.wrap('<div class="form-control cr serchshow" ></div>').parent();
		that.O = $.extend(true, {}, $.fn.searchInput.defaultData, options);// 参数
		that.$ulDiv = $('<ul  class="serchul" style="left:' + that.$targetDiv.offset().left + 'px;top:' + (that.$targetDiv.offset().top + 32) + 'px;display: none;min-width:' + that.$targetDiv.height() + 'px;" ></ul>');// 用来显示搜索到的数据的ul容器
		that.liHtml = "";// 显示搜索的ul中的li
		that.spanHtml = "";// 陈列选中的数据
		that.$spanContain = $('<div class="serchspandiv"></div>');// 存放选中的数据的容器
		
		that.$parentDiv = that.$targetDiv.wrap('<div class="serchdiv plugin-searchInput" ></div>').parent();
		that.$searchInput = $('<input class="serchinput" type="text" placeholder="请输入...">');
		

		if (!that.$targetDiv.hasClass("serchshow")) {
			that.$targetDiv.addClass("serchshow");
		}
		
		that.$targetDiv.append(that.$spanContain);
		that.$spanContain.append(that.$searchInput);
		that.$parentDiv.append(that.$ulDiv);
		if (that.$target.attr("data-default")) {// 如果绑定的元素上面有默认的数据着使用绑定上的数据
			that.O.defData = that.$target.attr("data-default").trim().split(";");
		}

		var spanHtml="";
		that.$target.find('option').each(function(i, item) {
			var value= $(item).val();
			if (value.trim() != '') {
				that.spanHtml = that.spanHtml + '<p class="serchp"><span class="serchValue">' + value + '</span><span class="serchdelete ts-remove"></span></p>';
			}
		});
		
		that.$spanContain.append(that.spanHtml);
		
		that.$ulDiv.on("click", "li", function(event) { // 点击ul中li新增数据
			that.setItem($(this).html().trim());
			that.$searchInput[0].focus();
			event.stopPropagation();
		});

		that.$targetDiv.click(function(event) {
			that.$searchInput[0].focus();
			event.stopPropagation();
		});

		that.$parentDiv.click(function(event) {
			event.stopPropagation();
		});

		that.$spanContain.on("click", ".serchdelete", function(event) { // 删除span的数据
			that.deleteSpan($(this));
			event.stopPropagation();
		});
		
		that.$searchInput.keyup(function(event) {
			if(event.keyCode==13){//enter
				var value = $(this).val().trim();
				if(that.$ulDiv.find('li.select').length>0){
					value=that.$ulDiv.find('li.select').html().trim();
				}
				return that.setItem(value,event);
			}else if(event.keyCode==40){//下箭头
				that.selectLi("down");
			}else if(event.keyCode==38){//上箭头
				that.selectLi("up");
			}else{
				that.keyup();
			}
			that.$searchInput.css('width',(that.$searchInput.val().length*14+30)+'px');
			event.preventDefault();
		});
		
		
		if(that.$target.data('name')){
			that.setValueString(that.$target.data('name'));
		}//初始化数据
		
		/*that.$searchInput.blur(function(event) {
			return that.setValue(that.$ulDiv,event);
		});*/
		
		that.$searchInput.focus(function(event) {
			$(document).trigger('click');
		});
		
		$(document).click(function(event){
			return that.setItem(that.$searchInput.val().trim(),event);
		});
	}

	searchInput.prototype = {
		setItem : function(value,event) {// 添加元素sitem
			var that = this;
			value = value.trim();
			that.$searchInput.val("").css('width','auto');
			that.$ulDiv.html("").css("display", "none");
			var targetValue = that.$target.val() || '';
			if(value!='' && (','+targetValue.toString()+',').indexOf(','+value+',')<0){
				that.$spanContain.append('<p class="serchp"> <span class="serchValue">' + value + '</span><span class="serchdelete ts-remove"></span></p>');
				that.$target.append('<option value="'+encodeHtml(value)+'" selected>'+value+'</option>');
				if(that.$target.data('name')){
					that.setValueString(that.$target.data('name'));
				}
			}
			if(event){
				event.stopPropagation();
			}
			
		},
		setValueString:function(name){//把值利用","分隔进行存储
		   var that = this;
		   var $valueString = that.$targetDiv.find('textarea.valueString');
		   var targetValue = that.$target.val() || '';
		   if($valueString.length>0){
			   $valueString.html(decodeHtml(targetValue.toString()));
		   }else{
			   $valueString =$('<textarea class="valueString hide mustinput" name="'+name+'"  check-type="'+that.$target.attr('check-type')+'">'+decodeHtml(targetValue.toString())+'</textarea>');
			   that.$targetDiv.append($valueString);
		   }
		   that.$target.trigger('change');
		   $valueString.trigger('change');
		},
		deleteSpan : function($this) {// 删除从span中删除item
			var that = this;
			var value = $this.parent('.serchp').find('.serchValue').html().trim();
			$this.closest('.serchp').remove();//删除显示里面的值
			that.$target.find('option[value="'+value+'"]').remove();//刪除select里面的值
			
			if(that.$target.data('name')){
				that.setValueString(that.$target.data('name'));
			}
		},
		keyup : function() {// 搜索时的操作
			var that = this;
			var k = that.$searchInput.val();
			if (k.trim() != "") {
				if (that.$target.attr("data-url")) {// 当url存在时对元素绑定实时监听搜索
					var url = that.$target.attr("data-url");
					if (url.indexOf("?") >= 0) {
						url = that.$target.attr("data-url") + "&k=" + k;
					} else {
						url = that.$target.attr("data-url") + "?k=" + k;
					}
					$.get(url, function(data) {
						that.O.defData = data;
						$.each(that.O.defData, function(i, item) {
							if (typeof item == 'string' && item.trim() != '') {
								that.liHtml = that.liHtml + '<li class="showli">' + item + '</li>';
							}
							if (typeof item == 'object') {
								that.liHtml = that.liHtml + '<li class="showli">' + item[that.O.value] + '</li>';
							}
						})
						that.$ulDiv.html(that.liHtml);
						that.liHtml = "";
						if (that.$ulDiv.find("li.showli").length > 0) {
							that.$ulDiv.css("width", that.$targetDiv.outerWidth());
							that.$ulDiv.css("display", "inline-block");
						} else {
							that.$ulDiv.css("display", "none");
						}
					});
				} else {
					that.$ulDiv.html(that.liHtml);
					that.liHtml = "";
					if (that.$ulDiv.find("li.showli").length > 0) {
						that.$ulDiv.css("width", that.$targetDiv.outerWidth());
						that.$ulDiv.css("display", "inline-block");
					} else {
						that.$ulDiv.css("display", "none");
					}
				}
			} else {
				that.$ulDiv.empty().css("display", "none");
				that.liHtml = "";
			}
		},
		selectLi:function(type){
			var that = this;
			
			if(that.$ulDiv.find('li').length>0 && type=='down'){
				var $liselect =that.$ulDiv.find('li.select').length>0 ? that.$ulDiv.find('li.select') : that.$ulDiv.find('li:first');
				if($liselect.hasClass('select')){
					$liselect.removeClass('select');
					if($liselect.next("li").length>0){
						$liselect.next("li").addClass('select');
					}else{
						that.$ulDiv.find('li:first').addClass('select');
					}
				}else{
					$liselect.addClass('select');
				}
			}else if(that.$ulDiv.find('li').length>0 && type=='up'){
				var $liselect =that.$ulDiv.find('li.select').length>0 ? that.$ulDiv.find('li.select') : that.$ulDiv.find('li:last');
				if($liselect.hasClass('select')){
					$liselect.removeClass('select');
					if($liselect.prev("li").length>0){
						$liselect.prev("li").addClass('select');
					}else{
						that.$ulDiv.find('li:last').addClass('select');
					}
				}else{
					$liselect.addClass('select');
				}
			}
		}
	}

	$.fn.searchInput = function(option) {
		if (navigator.appName != 'Microsoft Internet Explorer') {
			var $target = $(this);
			if (!$target.data('bind')) {
				var c = new searchInput($target, option);
				$target.data('bind', true);
			}
		}
		return this;
	};
	$.fn.searchInput.defaultData = {
		defData : [],
		title : "text",
		value : "text",
		containt : null
	}
	
	$(function() {
		$('*[plugin-searchInput]').each(function() {
			$(this).searchInput();
		});

		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('*[plugin-searchInput]').each(function() {
				$(this).searchInput();
			});
		});

		$(document).on('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('*[plugin-searchInput]').each(function() {
				$(this).searchInput();
			});
		});
	});
})(jQuery)