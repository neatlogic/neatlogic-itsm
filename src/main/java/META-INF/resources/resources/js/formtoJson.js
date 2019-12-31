/*******************************************************************************
 * 
 * form 表单元素转Json 格式数据 json 的 key 为元素的 name
 * 
 * 支持text/radio/checkbox/textarea
 * 
 * 多选 对应的是个json 数组 ['1','2']
 */

(function($) {
	$.fn.toJson = function(dropblank) {
		var that = this;
		var serializeObj = {};
		if(that[0] && that[0].tagName!='FORM'){
			that=that.clone(true).wrap("<form class='wrap hide'></form>").closest('form');
		}
		var array = that.serializeArray();
		// console.info(array);
		that.find('input').each(function() {
			if ($(this).attr('type') == 'checkbox') {
				var name = $(this).attr('name');
				/* 没有 name 的 input 跳过，避免出现key为 undefined */
				if (!name) {
					return false;
				}
				
				var isExists = false;
				$(array).each(function() {
					if (this.name == name) {
						isExists = true;
					}
				});
				if (!isExists) {
					array.push({
						name : name,
						value : []
					});
				}
			}
		});
		// console.info(this.html());
		// console.info(this.serialize());

		$(array).each(function() {
			if (serializeObj[this.name] != undefined) {
				if ($.isArray(serializeObj[this.name])) {
					serializeObj[this.name].push(this.value);
				} else {
					serializeObj[this.name] = [ serializeObj[this.name], this.value ];
				}
			} else {
				if (!dropblank || this.value != '') {
					serializeObj[this.name] = this.value;
				}
			}
		});
		/*
		 * for(var s in serializeObj){ alert(s + "<>" + serializeObj[s]); }
		 */
		//console.log(serializeObj);
		that=null;
		return serializeObj;
	};
})(jQuery);