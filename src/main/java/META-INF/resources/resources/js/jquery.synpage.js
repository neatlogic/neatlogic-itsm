/**
 * plugin synpage
 * author by wangpf
 * date:2014-05-22
 */
;(function($){
	$.fn.synpage = function(options){
		var config = $.extend({
			titleName:null,
			content:null,
			userName:null,
			placement:'right',
			typeUrl:'/balantflow/module/balantknows/synpage/getPageTypeList.do',
			postUrl:'/balantflow/module/balantknows/synpage/synPageToKnows.do'
		},options||{});
		
		var target = this;
		
		if(config.userName != null && config.content != null && config.titleName != null){
			config.label = $('<label>选择类型:</label>');
			config.popselect = $('<select name="pageType" class="form-control"></select>');
			$.ajax({
			  url: config.typeUrl,
			  dataType: 'json',
			  success: function(data){
				  	$.each(data,function(key,obj){
						config.popselect.append(createNode(obj.typeID,obj.typeName));
				  	})
				  
				  	config.popsubmit = $('<button name="submitTxt" class="btn btn-success" id="submitTxt"><i class=" icon-folder-open icon-white"></i>同步到知识库</button>');
					config.formDiv = $('<div class="form-group"></div>');
					config.formDiv.append(config.label).append(config.popselect).append(config.popsubmit);
					
					target.popover({
						container:'body',
						html:true,
						toggle:'popover',
						content:config.formDiv,
						placement:config.placement
					});
					
					config.popsubmit.click(function(){
		
					var params = {
						titleName:config.titleName,
						content:config.content,
						pageType:config.popselect.val(),
						userName:config.userName
					}
					
					$.ajax({
						data:params,
						type:'POST',
						dataType : 'json',
						url : config.postUrl,
						success : function(data,status) {
							if(data.status == "OK"){
								showPopMsg.success('保存成功！');
								location.reload();
							}else{
								showPopMsg.error("保存失败，异常：<br>" + data.Message);
							}	
						}
					});
				});
			  },
			  error:function(){
				  config.asksign = $('<i class="icon-question-sign"></i>');
				  config.asksign.attr('title','该功能需要加载知识库模块儿，请加载该模块！');
				  target.after(config.asksign);
				  target.attr('disabled',true);  
			  }
			  
			});
		}
		
		function createNode(typeID,typeName) {
			return '<option value="'+typeID + '" >'+typeName+'</option>';
		}
		
		return this;
	};
})(jQuery);