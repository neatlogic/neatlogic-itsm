<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" deferredSyntaxAllowedAsLiteral="true"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<!DOCTYPE HTML>
<html lang="en">
<head>
<title>${tk:lang("修改密码","") }</title>
<jsp:include page="/resources/res-include-new.jsp?exclude=bootstrap,skincss&need=base,bootstrap-validation,util,customcheckbox" />
<style>
.deadline-tips {
	padding-bottom: 30px;
	border-bottom: 1px solid #ddd;
	padding-top: 20px;
}

#remainedTime {
	margin-left: 10px;
	font-weight: normal;
}

#formUpdatePwd {
	padding-top: 40px;
}

.skiplabel {
	margin-left: 2px;
	cursor: pointer;
	margin-top: 0px;
	vertical-align: middle;
}
</style>
<script type="text/javascript" src="/balantflow/resources/login/js/psw.js"></script>
</head>
<body class="bg-grey">
	<input type="hidden" id="hidExpireLeftDay" value="${expireLeftDay }" />
	<div class="block-main" style="min-height: calc(100vh - 82px);">
		<c:choose>
			<c:when test="${isExpired && !isAdmin && !isExpiredModify}">
				<h4 class="text-center" style="line-height: 6;">
					<i class="ts-info-s text-danger fz32" style="vertical-align: middle; margin-right: 10px;"></i>${tk:lang("当前密码已过期，请联系管理员重置","") }
				</h4>
			</c:when>
			<c:otherwise>
				<div class="deadline-tips">
					<table class="table noborder">
						<tbody>
							<tr>
								<td width="80" class="text-right">
									<i class="ts-info-s text-danger" style="font-size: 36px; line-height: 2;"></i>${checkMenu}</td>
								<td>
									<h4>
										<c:choose>
											<c:when test="${isExpired && (isAdmin || isExpiredModify)}">
												<span class="text-danger fz24">${tk:lang("密码已过期","") }</span>
											</c:when>
											<c:otherwise>${tk:lang("距离密码到期还有","") }
												<span class="text-danger fz32" id="remainedTime">
													<c:choose>
														<c:when test="${expireDayLeft == 0}">
															${tk:lang("不到1天","") }
														</c:when>
														<c:otherwise>
															${expireDayLeft }${tk:lang("天","") }
														</c:otherwise>
													</c:choose>
												</span>
											</c:otherwise>
										</c:choose>
									</h4>
									<h6>${tk:lang("请及时修改密码","") }，${tk:lang("以免影响正常使用","") }</h6>
									<div>${tk:lang("密码要求","") }：<span class="text-danger">${tk:lang("长度在8~20之间的字符串","") }，${tk:lang("至少有字母、数字、特殊字符其中2种组合","") }。</span>
									</div>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
				<form class="form-detail" method="POST" id="formUpdatePwd">
					<div class="form-block">
						<label for="txtOldPwd" class="block-left">${tk:lang("当前密码","") }：</label>
						<div class="block-right">
							<input class="form-control input-large" id="txtOldPwd" type="password" check-type="required" required-message="请输入当前使用的密码" value="">
						</div>
					</div>
					<div class="form-block">
						<label for="txtNewPwd" class="block-left">${tk:lang("新密码","") }：</label>
						<div class="block-right">
							<input class="form-control input-large" id="txtNewPwd" type="password" check-type="passcode" required-message="请输入新密码" value="">
						</div>
					</div>
					<div class="form-block">
						<label for="txtNewPwd2" class="block-left">${tk:lang("确认新密码","") }：</label>
						<div class="block-right">
							<input class="form-control input-large" id="txtNewPwd2" type="password" check-type="passcode" required-message="请输入确认密码" value="">
						</div>
					</div>
				</form>
				<div class="btn-bar">
					<div class="btn-group">
						<button type="button" class="btn btn-primary" id="btnUpdatePwd">${tk:lang("确认修改","") }</button>
						<c:if test="${expireDayLeft > 0 || isAdmin}">
							<a class="btn btn-default" href="/balantflow/passwordValidity/ignoreWarning">${tk:lang("跳过此次提醒","") }</a>
						</c:if>
					</div>
				</div>
				<script>
				$(function(){
						if(self!=top){
							top.window.location.reload();
						}
					});
					$('#txtNewPwd2').change(function() {//校验密码是否一致
						var newPwd = $('#txtNewPwd').val();
						var newPwd2 = $('#txtNewPwd2').val();
						if (newPwd != newPwd2) {
							showPopMsg.info('${tk:lang("密码前后输入不一致","")}');
						}
					});
					$("#btnUpdatePwd").click(function() {//修改密码
						if ($('#formUpdatePwd').valid()) {
							var newPwd = $('#txtNewPwd').val();
							var newPwd2 = $('#txtNewPwd2').val();
							var oldPwd = $('#txtOldPwd').val();
							if (newPwd != newPwd2) {
								showPopMsg.info('${tk:lang("密码前后输入不一致","")}');
								return false;
							}
							if (newPwd == oldPwd) {
								showPopMsg.info('${tk:lang("新密码跟旧密码不能一样","")}');
								return false;
							} else {
								$.ajax({
									type : "POST",
									url : "/balantflow/user/updateUserPwd.do",
									data : {
										'txtOldPwd' : '{ENCRYPED}' + $.md5(oldPwd),
										'txtNewPwd' : '{ENCRYPED}' + $.md5(newPwd),
										'txtNewPwd2' : '{ENCRYPED}' + $.md5(newPwd2)
									},
									success : function(data, status) {
										if (data.Status == 'OK') {
											createModalDialog({
												msgtitle : '${tk:lang("修改成功","")}',
												msgcontent : '${tk:lang("当前密码修改成功","")}，${tk:lang("请重新登录","")}。',
												blurclose : false,
												checkBtn : false,
												hideClose : true,
												customButtons : [ {
													'text' : '${tk:lang("确定","")}',
													'class' : 'btn-primary',
													'click' : function() {
														location.href = '${pageContext.request.contextPath}/';
													}
												} ]
											});
										} else {
											showPopMsg.error(data);
										}
									}
								});
							}
						}
					});
				</script>
			</c:otherwise>
		</c:choose>
	</div>
</body>
</html>