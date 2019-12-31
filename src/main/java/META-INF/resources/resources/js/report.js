$(function() {
	$('.btnExport').click(function() {
		$('#formMain').attr('action', 'exportReport.do?id=' + $(this).attr('reportId') + '&type=' + $(this).attr('type')).attr('target', '_blank').submit();
		$('#formMain').attr('action', 'showReport.do?id=' + $(this).attr('reportId')).attr('target', '_self');
	});
	$('#btnEmail').click(function() {
		reportEmail(this);
	});
	$('#btnRight').click(function() {
		var s = $('#divContainer').scrollLeft();
		$('#divContainer').animate({
			scrollLeft : s + 200
		}, 500);
	});

	$('#btnLeft').click(function() {
		var s = $('#divContainer').scrollLeft();
		$('#divContainer').animate({
			scrollLeft : s - 200
		}, 500);
	});
});

// 获取收件人邮箱  
function getReceiverEmail() {
	var email=['#receiverEmail','#copyEmail'];
	var select=['#receiverSelect','#copySelect'];
	for(var i=0;i<email.length;i++){
		var receiverEmailJson = $(email[i]).data("arrEmail");
		if (receiverEmailJson) {
			var emailString = "";
			var receiver = $(select[i]).val();
			for ( var t in receiverEmailJson) {
				for ( var r in receiver) {
					if (t.toLowerCase() == receiver[r].toLowerCase()) {
						emailString += "," + receiverEmailJson[t];
					}
				}
			}
			emailString = emailString.substring(1, emailString.length);
			$(email[i]).val(emailString);
		}
	}
}
// 处理收件人
function showUserInfo(userId,selectId) {
	var $this= $("#"+userId);
	var $next= $this.next(".inputselect-selector");
	var value = $this.val();
	var arrEmail = null;
	if (value.length > 0) {
		$.get('/balantflow/user/getUserInfo.json?userId=' + value[value.length - 1], function(json) {
			if (json.email == "") {// 如果收件人邮件不存在
				showPopMsg.error("用户：" + json.userid + "邮件不存在");
				$next.find('span[data-value="' + json.userid + '"]').remove();
				$this.find('option[value="' + json.userid + '"]').remove();
				$next.find('.inputselect-inputer').focus();
			} else {
				var receiverVar = $('#'+selectId).data("arrEmail");
				if (typeof receiverVar == "undefined") {
					arrEmail = JSON.parse("{}");
				} else {
					arrEmail = receiverVar;
				}
				arrEmail[json.userid] = json.email;
				$('#'+selectId).data("arrEmail", arrEmail);
			}
		}, 'json');
	}
}

// 发送报表邮件
function reportEmail(sender) {
	var json = {};
	$.getJSON('/balantflow/mailBox/getMailBoxList.do', function(mailBoxList) {
		json.mailBoxList = mailBoxList.mailBoxVoList;
		json.emailName = $(sender).attr('reportname');
		json.reportId = $(sender).attr('reportid');
		var html = xdoT.render('balantreport.report.reportemail', json);
		createModalDialog({
			msgtitle : '发送报表邮件',
			msgwidth : 700,
			checkReturn : true,
			msgcontent : html,
			successFuc : function() {
				var flag = true;
				if ($('#repotEmail').valid()) {
					if ($('#receiverSelect').val() == '' || $('#receiverSelect').val() == null) {
						showPopMsg.error("请填写收件人邮箱");
						return false;
					} else {
						getReceiverEmail();
						if ($('.techsurePlugin').length > 0) {
							$('.techsurePlugin').remove();
						}
						var repotEmailJson = $("#repotEmail").toJson(true);
						var repotEmailJsonString = JSON.stringify(repotEmailJson);
						$(".caret").after("<input type=\"hidden\" class=\"techsurePlugin\" name=\"techsureReportEmailForm\" value='" + repotEmailJsonString + "'/>");
						$("#formMain").ajaxSubmit({
							type : 'post',
							dataType : 'json',
							url : '/balantflow/module/balantreport/report/sendReportEmail.do',
							success : function(data) {
								if (data.Status == "OK") {
									showPopMsg.success();
									flag = true;
								} else {
									showPopMsg.error(data);
									flag = false;
								}
							}

						});
					}
				} else {
					flag = false;
				}
				return flag;
			},
			showFuc : function() {
				$('#receiverSelect').inputselect({
					url : '/balantflow/user/searchUserByNameJson.do'
				});
				$('#copySelect').inputselect({
					url : '/balantflow/user/searchUserByNameJson.do'
				});

				$('#receiverSelect,#copySelect').change(function() {
					var id =$(this).attr("id");
					if ($(this).val() && $(this).val() != '') {
						showUserInfo(id,$(this).data("id"));
					}
				});
			}
		});

	});
}