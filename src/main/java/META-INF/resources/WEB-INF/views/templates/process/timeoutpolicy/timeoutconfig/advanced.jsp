<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form class="form_timeoutpolicy">
	<div>
		<input type="text" value="{{?it.config}}{{=it.config['expression']||''}}{{?}}" class="txtExpression" style="width: 100%">
	</div>
	<div class="help-block divAttrItem">表达式使用javascript语法，只能返回true或false，例如：true或${param1}=='ABC' && ${param2}== 1，${tk:lang("点击以下自定义属性进行复制","") }：</div>
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			var userData = Paper.getUserData();
			if (userData && userData['attributeList']) {
				for (var i = 0; i < userData['attributeList'].length; i++) {
					var attr = userData['attributeList'][i];
					var span = $('<span style=\"text-decoration:underline;margin:0px 5px;cursor:pointer\" title=\"${tk:lang("点击复制","")}\" data-uuid="' + attr['uuid'] + '">' + attr['label'] + '</span>');
					span.on('click', function() {
						var $temp = $('<input>');
						$("body").append($temp);
						$temp.val('\\${' + $(this).data('uuid') + '}').select();
						document.execCommand("copy");
						$temp.remove();
						showSnapMessage('${tk:lang("复制成功","")}');
					});
					$('.divAttrItem', that).append(span);
				}
			}
		};
		var fn = {
			'this' : {
				'getConfig' : function() {
					var that = this.root;
					var returnValue = {};
					returnValue['expression'] = $('.txtExpression', that).val();
					return returnValue;
				}
			}
		}
	</script>
</form>