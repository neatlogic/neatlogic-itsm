/**
 * 针对formbuilder使用的表单预览插件 2016-05-31 kong
 */
(function($) {
	var getSetJson = function(jsonUrl,selector,setId) {
		var selectText = '<option value="">请选择...</option>';
		var isMultiple = !(selector.attr('multiple') == 'multiple');// 判断是否多选,是：false
		$.getJSON(jsonUrl, function(data) {
			if (data.Status == "OK") {
				selector.children().remove();
				if (isMultiple) {
					selector.append(selectText);
				}
				for ( var i in data.Data) {
					var option = '<option setId="' + setId + '" value="' + data.Data[i].value + '">' + data.Data[i].text + '</option>';
					selector.append(option);
				}
			} else {
				selector.children().remove();
				selector.append('<option value="">数据源加载失败</option>');
			}
		});
	}
	var getContextPath = Global.getContextPath();
	// 异步根据父数据源添加子下拉框
	var ajaxGetSonBind = function(selector, setId) {
		var pName = selector.attr('parentbindname');// 绑定的parentName
		var pNameVals = "";
		// 获取该下拉框所有父的“name=value”
		if (pName && pName != "undefined") {
			var pNames = pName.split(',');

			for (var j = 0; j < pNames.length; j++) {
				var pVal = $('.container-fluid').find('select[name="' + pNames[j] + '"]').val();
				if (pVal) {
					pNameVals = pNameVals + "&" + pNames[j] + '=' + pVal;
				}
			}
		}
		var jsonUrl = getContextPath + "/prop/set/" + setId + "?1=1" + pNameVals;
		//jsonUrl = jsonUrl.replace("1=1&", "");
		//jsonUrl = jsonUrl.replace("1=1", "");
		if (typeof setId != 'undefined' && setId != '') {
			getSetJson(jsonUrl,selector,setId);
		}
	}
	// 联动select change事件
	var selectChangeLinkage = function() {
		var $this = $(this);
		var parentName = $this.attr('name');
		$('.container-fluid').find('select').each(function() {// 找到所有select
			var parentNames = $(this).attr('parentbindname');
			parentNames = parentNames.split(',');
			for (var i = 0; i < parentNames.length; i++) {
				if (parentNames[i] == parentName) {
					var setId = $(this).attr('controltypeid');
					ajaxGetSonBind($(this), setId);
				}
			}
		});
	}

	var initSelectSet = function(sender) {
		var $this = $(sender);
		var setId = $this.attr("controltypeid");
		if (setId) {
			var jsonUrl = getContextPath + "/prop/set/" + setId;
			getSetJson(jsonUrl,$this,setId);
		}
	}
	$.fn.formpreview = function() {
		$('.container-fluid').find('select').each(function() {
			// 重新绑定联动事件
			$(this).on('change', selectChangeLinkage);
			// 重新初始化不是子下拉框的属性集
			if($(this).attr("parentbindname")){
				initSelectSet(this);
			}
		});
		$('.badge').hide();
	};

})(jQuery);
