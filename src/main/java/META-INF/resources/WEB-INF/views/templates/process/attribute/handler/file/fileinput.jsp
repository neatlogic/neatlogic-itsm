<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="form_attribute_{{=it.attributeUuid}}" class="form_attribute" data-uuid="{{=it.attributeUuid}}">
	<div class="divFile"></div>
	{{?it.data && it.data.length > 0 }} {{~it.data:file:index}}
	<input type="hidden" name="fileId" class="hidUploadFileId" value="{{=file.fileid}}" data-name="{{=file.name}}" data-size="{{=file.size}}" data-type="{{=file.type}}">
	{{~}} {{?}}
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			var uploader = $('.divFile', that).uploader({
				fnAfterRemoved : function(fileid) {
					if (fileid) {
						$('.hidUploadFileId', that).each(function() {
							if ($(this).val() == fileid) {
								$(this).remove();
							}
						});
					}
				},
				downloadUrl : '${pageContext.request.contextPath}/file/getFile.do',
				belong : 'FLOW',
				paramName : 'attribute_file'
			});
			$('.divFile', that).data('uploader', uploader);
			$('.hidUploadFileId', that).each(function() {
				var file = {};
				file.fileid = $(this).val();
				file.name = $(this).data('name');
				file.size = $(this).data('size');
				file.type = $(this).data('type');
				uploader.addUploaded(file);
			});
		};
		var fn = {
			'this' : {
				'getValue' : function() {
					var that = this;
					var uploader = $('.divFile', that).data('uploader');
					var returnValue = new Array();
					if (uploader) {
						var fileList = uploader.getUploaded();
						for(var i = 0; i < fileList.length; i++){
							returnValue.push(fileList[i].fileid);
						}
					}
					return returnValue;
				},
				'getData' : function() {
					var that = this;
					var uploader = $('.divFile', that).data('uploader');
					if (uploader) {
						var fileList = uploader.getUploaded();
						console.log(JSON.stringify(fileList));
						return fileList;
					}
					return null;
				}
			}
		};
	</script>
</form>