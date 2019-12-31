
/***
 * 带富表单，可编辑内容同步到知识库的js
 */
;(function($){
	$.fn.editorSynpage = function(options){
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
			$.ajax({
			  url: config.typeUrl,
			  dataType: 'json',
			  success: function(data){
				  
					config.popselect = $('<select name="pageType" class="form-control" check-type="required" required-message="请选择类型"></select>');
					config.popselect.append(createNode("","请选择..."));
					
				  	$.each(data,function(key,obj){
						config.popselect.append(createNode(obj.typeID,obj.typeName));
				  	})
				  
				  	config.formDiv= $('<form class="form-horizontal"  method="post" style="width:99%;"><div class="col-sm-12"></div></form>');
					config.typeFormDiv = $('<div class="form-group"></div>');
					config.typeLabel = $('<label class="col-sm-2 control-label">类型</label>');
					config.typeContentLabel = $('<div class="col-sm-10"></div>');
					config.typeFormDiv.append(config.typeLabel).append(config.typeContentLabel.append(config.popselect));
					
					config.titleFormDiv = $('<div class="form-group"></div>');
					config.titleLabel = $('<label class="col-sm-2 control-label">名称</label>');
					config.titleContentLabel = $('<div class="col-sm-10"></div>');
					config.titleContent = $('<input name="name" class="form-control input-xxlarge" check-type="required" required-message="请输入名称">');
					config.titleContent.val(config.titleName);
					config.titleFormDiv.append(config.titleLabel).append(config.titleContentLabel.append(config.titleContent));
					
					config.contentFormDiv = $('<div class="form-group"></div>');
					config.contentLabel = $('<label class="col-sm-2 control-label">内容</label>');
					config.contentContentLabel = $('<div class="col-sm-10"></div>');
					config.contentContent = $('<textarea id="txtEditorTaskContent"  name="content" style="width:100%;height:300px"></textarea>');
					config.contentContent.html(config.content);
					config.contentFormDiv.append(config.contentLabel).append(config.contentContentLabel.append(config.contentContent));
					
					config.formDiv.append(config.titleFormDiv).append(config.contentFormDiv).append(config.typeFormDiv);
					
					target.click(function(){
						//util js
						if(typeof(createModalDialog)=="undefined"){
							jQuery.getScript("/balantflow/resources/js/util.js");
						}
						
						//表单验证js
						if(typeof(valid)){
							jQuery.getScript("/balantflow/resources/js/bootstrap-validation.js");
						}
						
						createDialog({
							msgtitle : '同步到知识库',
							msgcontent : config.formDiv , 
							msgwidth : "80%",
							checkReturn : true ,
							successFuc : function(){
								config.editor.sync();
								if(config.editor.getContentTxt() == ''){
									showPopMsg.info('请填写内容！');
									return false;
								}
								if(config.formDiv.valid()){
									$.ajax({
										data:{
											 titleName:config.titleContent.val(),
											 content:config.contentContent.val(),
											 pageType:config.popselect.val(),
											 userName:config.userName
											},
										type:'POST',
										dataType : 'json',
										url : config.postUrl,
										success : function(data,status) {
											if(data.status == "OK"){
												showPopMsg.success('同步成功！');
											}else{
												showPopMsg.error("同步失败，异常：<br>" + data.Message);
											}	
										}
									});
									return true ; 
								}else{
									showPopMsg.error('待同步的信息不完整！');
									return false; 
								}
							}
						});
						
						//UE JS
						if(typeof(UE) == 'undefined'){
							$.ajaxSetup({ 
								async : false
							});
							$.getScript('/balantflow/resources/js/ueditor/ueditor.config.js');
							$.getScript('/balantflow/resources/js/ueditor/ueditor.all.js');
							$.getScript('/balantflow/resources/js/ueditor/lang/zh-cn/zh-cn.js');
							$("<link>").attr({ rel: "stylesheet",type: "text/css",href: "/balantflow/resources/js/ueditor/themes/default/css/ueditor.css"}).appendTo("head");
							
							$.ajaxSetup({ 
								async : true 
							});
						}
						
						config.editor = UE.getEditor('txtEditorTaskContent', UE.utils.extend({
							initialFrameWidth: '100%',
							initialFrameHeight: 200,
							UEDITOR_HOME_URL : '/balantflow/resources/js/ueditor/',
							serverUrl : '/balantflow/resources/js/ueditor/jsp/controller.jsp', 
							toolbars : [["", 'link','undo', 'redo', 'paragraph', 'insertimage', 'attachment',  'superscript', 'subscript'
							              ,'justifyleft', 'justifycenter', 'justifyright', 'justifyjustify', 'touppercase', 'tolowercase', 'removeformat', 'formatmatch', 'autotypeset', 'pasteplain'
							              ,'imageleft', 'imageright', 'imagecenter'],
							              ['bold', 'italic', 'horizontal', 'highlightcode', 'underline', 'strikethrough', 'fontfamily', 'fontsize', 'forecolor', 'backcolor'
							               ,'inserttable', 'deletetable', 'mergeright', 'mergedown', 'splittorows', 'splittocols', 'splittocells', 'mergecells', 'insertcol', 'insertrow', 'deletecol', 'deleterow', 'insertparagraphbeforetable']]
						}, {
							fileUrl: '/balantflow/file/uploadFile.do?belong=FLOW&filetype=file',
							imageUrl: '/balantflow/file/uploadFile.do?belong=FLOW&filetype=image'
						}, true));
						
						config.editor.ready(function(){
							config.editor.execCommand('fontsize','12px');
					    });
					});
			  },
			  error:function(){
				  config.asksign = $('<i class="icon-question-sign"></i>');
				  config.asksign.attr('title','该功能需要加载知识库模块，请加载该模块！');
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