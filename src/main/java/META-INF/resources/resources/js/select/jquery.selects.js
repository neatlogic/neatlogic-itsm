(function($) {
	$.fn.emptySelect = function() {
		return this.each(function() {
			if (this.tagName == 'SELECT')
				this.options.length = 0;
		});
	};
	/*
	 * add: wangtc,2014/04/19 缺省值属性: isSelected:0/1 || true/false 
	 * example: [{text:选项1 value:value1 isSelect:1 }]
	 * 
	 * add new rule : config 内传递默认值（多个逗号分隔），可选中
	 * 	{"textkey" : 'name' , "valuekey" : 'id' , "defaultValue" : "1,2"} 
	 * 调用示例：
	 * 	$('#slttest').loadSelectWithDefault(test , {'defaultValue':"值1,值1"});
	 */
	$.fn.loadSelectWithDefault = function(optionsDataArray, config) {
		return this.emptySelect().each(function() {
			if (this.tagName == 'SELECT') {
				var selectElement = this;
				if (optionsDataArray.length > 0) {
					var option = new Option();
					option.value = '';
					option.text = '${tk:lang("请选择")}...';
					selectElement.add(option);
					var textkey = 'text';
					var valuekey = 'value';
					var defaultValue;
					if (config) {
						textkey = config['textkey'] || 'text';
						valuekey = config['valuekey'] || 'value';
						defaultValue = config['defaultValue'] || '' ; 
					}
					textkey = textkey.toLowerCase();
					valuekey = valuekey.toLowerCase();
					$.each(optionsDataArray, function(index, optionData) {
						var option = $('<option></option>');
						for ( var i in optionData) {
							if (i.toLowerCase() == textkey) {
								option.text(optionData[i]);
							} else if (i.toLowerCase() == valuekey) {
								option.val(optionData[i]);
								if(defaultValue){//add by baron 20190413
									var values = defaultValue.split(','); 
									if(values && values.length > 0 ){
										for (x in values){
										   if($.trim(values[x]) == optionData[i]){
											   option.attr("selected", "selected");
										   }
										}
									}
								}
							} else if (i.toLowerCase() == 'isselected') {
								if (optionData[i] || optionData[i] == 1) {
									option.attr("selected", "selected");
								}
							} else {
								if(!config || !config.disableAttr){
									option.attr(i, optionData[i]);
								}
							}
							
						}
						$(selectElement).append(option);
					});
					if ($(selectElement).data('value')) {
						$(selectElement).val($(selectElement).data('value'));
					}
				} else {
					var option = new Option();
					option.value = '';
					option.text = '${tk:lang("没有任何数据")}';
					selectElement.add(option);
				}
			}
		});
	};
	$.fn.loadSelect = function(optionsDataArray, config) {
		return this.emptySelect().each(function() {
			if (this.tagName == 'SELECT') {
				var selectElement = this;
				if (optionsDataArray.length > 0) {
					/*
					 * var option = new Option(); option.value = ''; option.text =
					 * '请选择...'; selectElement.add(option);
					 */
					// var layer = -1;
					var textkey = 'text';
					var valuekey = 'value';
					var defaultValue ; 
					if (config) {
						textkey = config['textkey'] || 'text';
						valuekey = config['valuekey'] || 'value';
						defaultValue = config['defaultValue'] || '' ; 
					}
					textkey = textkey.toLowerCase();
					valuekey = valuekey.toLowerCase();
					$.each(optionsDataArray, function(index, optionData) {
						var option = $('<option></option>');
						for ( var i in optionData) {
							if (i.toLowerCase() == textkey) {
								option.text(optionData[i]);
							} else if (i.toLowerCase() == valuekey) {
								option.val(optionData[i]);
								if(defaultValue){//add by baron 20190413
									var values = defaultValue.split(','); 
									if(values && values.length > 0 ){
										for (x in values){
										   if($.trim(values[x]) == optionData[i]){
											   option.attr("selected", "selected");
										   }
										}
									}
								}
							} else {
								if(!config || !config.disableAttr){
									option.attr(i, optionData[i]);
								}
							}
						}
						$(selectElement).append(option);
					});
					if ($(selectElement).data('value')) {
						$(selectElement).val($(selectElement).data('value'));
					}
				} else {
					var option = new Option();
					option.value = '';
					option.text = '${tk:lang("没有加载到任何数据")}';
					selectElement.add(option);
				}
			}
		});
	};
	$.fn.appendSelect = function(optionsDataArray, config) {
		return this.each(function() {
			if (this.tagName == 'SELECT') {
				var selectElement = this;
				if (optionsDataArray.length > 0) {
					var textkey = 'text';
					var valuekey = 'value';
					var defaultValue ;
					if (config) {
						textkey = config['textkey'] || 'text';
						valuekey = config['valuekey'] || 'value';
						defaultValue = config['defaultValue'] || '' ; 
					}
					textkey = textkey.toLowerCase();
					valuekey = valuekey.toLowerCase();
					$.each(optionsDataArray, function(index, optionData) {
						var option = $('<option></option>');
						for ( var i in optionData) {
							if (i.toLowerCase() == textkey) {
								option.text(optionData[i]);
							} else if (i.toLowerCase() == valuekey) {
								option.val(optionData[i]);
								if(defaultValue){//add by baron 20190413
									var values = defaultValue.split(','); 
									if(values && values.length > 0 ){
										for (x in values){
										   if($.trim(values[x]) == optionData[i]){
											   option.attr("selected", "selected");
										   }
										}
									}
								}
							} else {
							    if(!config || !config.disableAttr){
								    option.attr(i, optionData[i]);
							    }
							}
						}
						$(selectElement).append(option);
					});
					if ($(selectElement).data('value')) {
						$(selectElement).val($(selectElement).data('value'));
					}
				} else {
					var option = new Option();
					option.value = '';
					option.text = '${tk:lang("没有加载到任何数据")}';
					selectElement.add(option);
				}
			}
		});
	};
})(jQuery);
