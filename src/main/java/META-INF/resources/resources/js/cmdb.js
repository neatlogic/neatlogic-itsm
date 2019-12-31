var ciTypeAttrId = '';
var maxOcu = 1;
var clearCiInfo = function() {
	ciTypeAttrId = '';
	maxOcu = 1;
};

/**
 * 外部调用选择CMDB列表
 *
 * @param _ciTypeAttrId
 * @param ciTypeId
 * @param maxOcu
 */
function chooseCIInfo(_ciTypeAttrId, ciTypeId, maxNum) {
	if (_ciTypeAttrId != '') {
		ciTypeAttrId = _ciTypeAttrId;
	}
	maxOcu = maxNum;

	$.ajax({
		type : 'GET',
		dataType : 'html',
		url : '/balantflow/module/balantcmdb/cmdb/getCmdbChooseListAjax.do',
		data : {
			ciTypeId : ciTypeId
		},
		async : false,
		success : function(data) {
			$('body').append(data);
			// addCI或者updateCI时使用
			$('#chooseCiTypeID').val(ciTypeId);

			$.ajax({
				type : 'GET',
				dataType : 'html',
				url : '/balantflow/module/balantcmdb/cmdb/getCmdbChooseListAjaxForType.do',
				data : {
					ciType : ciTypeId,
					pageSize : 10,
					container : 'modal-body'
				},
				async : false,
				success : function(data) {
					$('#modal-body').html(data);
					if (maxOcu == 1) {
						$('.chkCi').click(function() {
							$('.chkCi').removeAttr("checked");
							$(this).attr("checked", "checked");
						});
					}

					$('.modal-backdrop').click(function() {
						$('#chooseCiInfoModal').remove();
					});
				}
			});

			var dialogTop = $(window).scrollTop() + 100;
			/*
			 * if(self != top){ dialogTop =
			 * $(window.parent.document).scrollTop() + dialogTop; }else{
			 * dialogTop = $(window).scrollTop() + dialogTop; }
			 */

			var width = $(window).width() > 1000 ? 1000 : $(window).width();
			var height = $(window).height() - 200;
			var left = ($(window).width() - width) / 2;
			$('#chooseCiInfoModal').css({
				'margin' : '0px',
				'width' : width + 'px',
				'left' : left + 'px',
				'position' : 'absolute',
				'top' : dialogTop + 'px'
			});
			$('#chooseCiInfoModal').show();
			$('#modal_body').css({
				'max-height' : height + 'px',
				'overflow' : 'auto'
			});

		}
	});

}
var removeDiv = function(item) {
	item = $(item);

	var ciAttrRefCiId = item.parent().attr('ciAttrRefCiId');
	var valueType = item.parent().attr('valueType');
	var ciTypeAttrId = item.parent().attr('ciTypeAttrId');

	var tempRefCiIds = '';
	var ciIds = $('#ciId_' + ciTypeAttrId + '_' + valueType).val();
	var ciIdsArray = ciIds.split(",");
	if (ciIdsArray.length > 1) {
		for (var i = 0; i < ciIdsArray.length; i++) {
			var refCiId = ciIdsArray[i];
			if (refCiId != ciAttrRefCiId) {
				if (i != ciIdsArray.length - 1) {
					tempRefCiIds = tempRefCiIds + refCiId + ',';
				} else {
					tempRefCiIds = tempRefCiIds + refCiId;
				}

			}
		}
	}

	$('#ciId_' + ciTypeAttrId + '_' + valueType).val(tempRefCiIds);
	item.parent().remove();
};

/**
 * 从列表中选择checkbox 配置项
 */
var checkCi = function(item) {
	if (maxOcu != 1) {

		var div;
		var hid;
		div = $('#divChooseCiDivABC');
		hid = $('#hidCiIDABC');
		if (div.index() <= 0 && hid.index() <= 0) {
			div = $('<div class=\"d_f\" id=\"divChooseCiDivABC\"></div>');
			hid = $('<input type=\"hidden\" id=\"hidCiIDABC\"/>');
			$('#choseCiName').append(div).append('<div class=\"clear\"></div>').append(hid);
		}
		// 选择 添加
		var ciIds = hid.val();
		var ciNames = div.text();
		var array = ciIds.split(',');
		var ciId = $(item).attr('ciId');
		var ciName = $(item).attr('ciName');

		var ciIdSelectedsArray = ciIds.split(",");
		var selectedNum = ciIdSelectedsArray.length;
		var ciIdSelectedsArray = ciIds.split(",");
		var nowSelectedRefCiIdNum = ciIdSelectedsArray.length;
		var oldSelectedRefCiIdNum = 0;
		$('.refClass' + ciTypeAttrId).each(function() {
			oldSelectedRefCiIdNum++;
		});

		if ((maxOcu != -1) && (nowSelectedRefCiIdNum + oldSelectedRefCiIdNum > maxOcu)) {
			showPopMsg.error('当前属性选择的总的配置项(包括之前已经选择的)的个数超过了' + maxOcu + '个,请重新选择');
		}
		// if((maxOcu>1) && (selectedNum>maxOcu)){
		// showPopMsg('当前属性只能引用' + maxOcu + '个配置项','info');
		// }

		if ($(item).attr('checked')) {// 选中
			if (ciIds.indexOf(ciId) == -1) {
				ciIds = ciIds + ciId + ',';
				ciNames = ciNames + ciName + ',';
			}
		} else {// 取消选中
			ciIds = ciIds.replace(ciId + ',', '');
			ciNames = ciNames.replace(ciName + ',', '');
		}

		hid.val(ciIds);
		div.text(ciNames);
	} else {
		$('.chkCi').click(function() {
			$('.chkCi').removeAttr("checked");
			$(this).attr("checked", "checked");
		});
	}

};

/**
 * 从列表中选择了ci之后进行的操作
 *
 */
function chooseCISure() {
	var chooseCiType = $('#chooseCiTypeID').val();

	var ciNames = '';
	var ciIds = '';
	// 只选择一个ci实例时
	if (maxOcu == 1) {
		var ciIds_ = '';
		var ciNames_ = '';
		$('.chkCi:checked').each(function(i) {
			var ciID = $(this).val();
			var ciName = $(this).next().val();

			ciIds_ += ciID + ',';
			ciNames_ += ciName + ',';
		});
		ciNames = ciNames_.substring(0, ciNames_.length - 1);
		ciIds = ciIds_.substring(0, ciIds_.length - 1);

		$('.refClass' + ciTypeAttrId).each(function() {
			$(this).remove();
		});

		var $newRefCiNameDiv = $('<div class="refClassBox refClass' + ciTypeAttrId + '" ciTypeAttrId="' + ciTypeAttrId + '" valueType="' + chooseCiType + '" ciAttrRefCiId="' + ciIds + '" ciAttrRefCiName="' + ciNames + '"><font style="color:#0088cc;"><span class="tooltips">' + ciNames + '</span></font><a title="删除引用" class="removeRefCi" onclick="removeDiv(this);"><i class="icon-remove"></i></a></div>');
		$('#refDiv' + ciTypeAttrId).append($newRefCiNameDiv);
	} else {
		if ($('#divChooseCiDivABC').length > 0) {
			var html = $('#divChooseCiDivABC').html();
			var value = $('#hidCiIDABC').val();
			ciNames = html.substring(0, html.length - 1);
			ciIds = value.substring(0, value.length - 1);
		}

		var ciIdSelectedsArray = ciIds.split(",");
		var nowSelectedRefCiIdNum = ciIdSelectedsArray.length;
		var oldSelectedRefCiIdNum = 0;
		$('.refClass' + ciTypeAttrId).each(function() {
			oldSelectedRefCiIdNum++;
		});
		if ((maxOcu != -1) && (nowSelectedRefCiIdNum + oldSelectedRefCiIdNum > maxOcu)) {
			showPopMsg.error('当前属性选择的总的配置项(包括之前已经选择的)的个数超过了' + maxOcu + '个,请重新选择');
			return false;
		} else {

			if (ciTypeAttrId != '') {
				var ciNamesArray = ciNames.split(",");
				var ciIdsArray = ciIds.split(",");
				for (var i = 0; i < ciNamesArray.length; i++) {
					var ciName = ciNamesArray[i];
					var ciId = ciIdsArray[i];
					var ciNameExist = false;
					$('.refClass' + ciTypeAttrId).each(function() {
						var ciAttrRefCiName = $(this).attr('ciAttrRefCiName');
						if (ciAttrRefCiName == ciName) {
							ciNameExist = true;
						}
					});
					if (!ciNameExist) {
						var $newRefCiNameDiv = $('<div style="margin-left:10px;" class="refClassBox refClass' + ciTypeAttrId + '" ciTypeAttrId="' + ciTypeAttrId + '" valueType="' + chooseCiType + '" ciAttrRefCiId="' + ciId + '" ciAttrRefCiName="' + ciName + '"><font style="color:#0088cc;"><span class="tooltips">' + ciName + '</span></font><a title="删除引用" class="removeRefCi" onclick="removeDiv(this);"><i class="icon-remove"></i></a></div>');
						$('#refDiv' + ciTypeAttrId).append($newRefCiNameDiv);
					}
				}

				$('.tooltips').each(function() {
					var tooltips = $(this).text();
					if (tooltips.length > 30) {
						$(this).attr('title', tooltips);
						$(this).text(tooltips.substring(0, 30) + '...');
					}
				});
				$('.tooltips').tooltip();
			}
		}

	}

	var tempRefCiIds = '';
	var tempRefCiNames = '';
	$('.refClass' + ciTypeAttrId).each(function() {
		var ciAttrRefCiId = $(this).attr('ciAttrRefCiId');
		var ciAttrRefCiName = $(this).attr('ciAttrRefCiName');
		tempRefCiIds = tempRefCiIds + ciAttrRefCiId + ',';
		tempRefCiNames = tempRefCiNames + ciAttrRefCiName + ',';
	});

	tempRefCiIds = tempRefCiIds.substring(0, tempRefCiIds.length - 1);
	tempRefCiNames = tempRefCiNames.substring(0, tempRefCiNames.length - 1);

	$('#ciId_' + ciTypeAttrId + '_' + chooseCiType).val(tempRefCiIds);
	$('#ciAttr' + ciTypeAttrId).val(ciNames);

	$('#chooseCiInfoModal').hide();
	$('#chooseCiInfoModal').remove();
	clearCiInfo();

}

function closeChooseCiInfoModal() {
	$('#chooseCiInfoModal').hide();
	$('#chooseCiInfoModal').remove();
}

function showTopology(ciId, deep) {
	if ($('#ciInfoModal').length > 0) {
		return;
	}
	if (deep === undefined) {
		deep = 2;
	}
	window.location.href = '/balantflow/module/balantcmdb/cmdb/topology.do?topoType=ci&deep=' + deep + '&ownerId=' + ciId;
}

function showCi(ciId) {
	var url = '/balantflow/module/balantcmdb/cmdb/showCiAjax.do?ciId=' + ciId;
	createModalDialog({
		msgwidth : 600,
		msgheight : 400,
		checkBtn : false,
		url : url,
		msgtitle : '配置信息'
	});
}

function showCIInfo(ciId) {
	if ($('#ciInfoModal').length > 0) {
		$('#ciInfoModal').remove();
	}

	$.ajax({
		type : 'GET',
		dataType : 'html',
		url : '/balantflow/module/balantcmdb/cmdb/showCiInfo.do',
		data : {
			ciId : ciId
		},
		async : false,
		success : function(data) {
			$('body').append(data);
			$('body').css('position', 'relative');
			var winWidth = $(window).width();
			var scrollTop = $(window.parent).scrollTop();
			$('#ciInfoModal').css({
				'width' : (winWidth * 0.6) + 'px',
				'position' : 'absolute',
				'margin-top' : '0px',
				'top' : (scrollTop + 100) + 'px',
				'margin-left' : -((winWidth * 0.6) / 2) + 'px'
			});
			$('#ciInfoModal').show();
			$('.modal-backdrop').click(function() {
				$('#ciInfoModal').remove();
			});
		}
	});
}

function closeCiJitChart(item) {
	item = $(item);
	var ciId = item.parent().parent().attr('ciId');
	item.parent().parent().remove();
	$('#ciInfoCiRelationChartCi' + ciId).attr('rel', '0');
}

function closeRelyCiJitChart(item) {
	item = $(item);
	var ciId = item.parent().parent().attr('ciId');
	item.parent().parent().remove();
	$('#ciInfoCiRelationChartRelyCi' + ciId).attr('rel', '0');
}

function showCiTopoChart(item, ciId) {
	createDialog({
		msgtitle : '配置项上下游拓扑图',
		drag : true,
		checkBtn : false,
		msgcontent : '<iframe frameborder="no" scrolling="no" style="height:500px;width:100%" src="/balantflow/module/balantcmdb/cmdb/CITopology.do?ciId=' + ciId + '"></iframe>',
		msgheight : 520,
		zIndex : 2000
	});
}

function showCiTopoChart222(item, ciId) {
	// var $ciRelationChart =
	// $(item).parent().parent().parent().parent().parent().parent().next('div');
	if ($('#ciInfoModal').length > 0) {
		$('#ciInfoModal').remove();
	}
	/*
	 * var modalContain = $('<div class="modal hide fade in" id="ciInfoModal"></div>');
	 * $('body').append(modalContain); var url =
	 * '/balantflow/module/balantcmdb/cmdb/CITopology.do?ciId=' + ciId;
	 * $('#ciInfoModal').load(url).show(); console.info($('#ciInfoModal'));
	 */

	$.ajax({
		type : 'GET',
		dataType : 'html',
		url : '/balantflow/module/balantcmdb/cmdb/CITopology.do',
		data : {
			ciId : ciId
		},
		async : false,
		success : function(data) {
			$('body').append(data);
			$('body').css('position', 'relative');
			var winWidth = $(window.parent.document).find('#rightContent').width();
			winWidth = winWidth ? winWidth : $(window).width();
			if (winWidth > 1000) {
				winWidth = 1000;
			}
			var winOffset = $(window.parent.document).find('#rightContent').offset();
			var scrollTop = $(window.parent).scrollTop();
			$('#ciInfoModal').css({
				'width' : (winWidth * 0.8) + 'px',
				'position' : 'absolute',
				'margin-top' : '0px',
				'top' : (scrollTop + 100) + 'px',
				'margin-left' : -((winWidth * 0.8) / 2) + 'px'
			});
			$('#ciInfoModal').show();
			$('.modal-backdrop').click(function() {
				$('#ciInfoModal').remove();
			});
		}
	});

}

function showCiHistoryDetailChart(item, ciId) {
	if ($('#ciInfoModal').length > 0) {
		$('#ciInfoModal').remove();
		$(".popover").remove();
	}
	$.ajax({
		type : 'GET',
		dataType : 'html',
		url : '/balantflow/module/balantcmdb/cmdbcihistory/getCmdbCiHistoryListAjax.do',
		data : {
			ciId : ciId
		},
		async : false,
		success : function(data) {
			$('body').append(data);
			$('body').css('position', 'relative');
			var winWidth =  $(window).width();
			var scrollTop = $(window).scrollTop();
			var width = ($(window).width() - 100) > 1000 ? 900 : $(window).width();
			var height = $(window).height() - 200;
			var left = ($(window).width() - width) / 2;
			$('#ciInfoModal').css({
				'width' : (winWidth * 1.0) + 'px',
				'position' : 'absolute',
				'margin-top' : '0px',
				'top' : (scrollTop + 100) + 'px',
				'margin-left' : ((((winWidth - $('#ciInfoModal').width())) / 2)) + 'px'
			});
			$('#ciInfoModal').show();
			$('.modal-backdrop').click(function() {
				$('#ciInfoModal').remove();
			});
		}
	});

}

function showCiRelationCiChart(item, ciId) {

	var $ciRelationChart = $(item).parent().parent().parent().parent().parent().parent().next('div');
	if ($('#ciInfoModal').length > 0) {
		$('#ciInfoModal').remove();
	}

	$.ajax({
		type : 'GET',
		dataType : 'html',
		url : '/balantflow/module/balantcmdb/cmdb/showCiRelationCiChart.do',
		data : {
			ciId : ciId
		},
		async : false,
		success : function(data) {
			$('body').append(data);
			$('body').css('position', 'relative');
			var winWidth = $(window.parent.document).find('#rightContent').width();
			winWidth = winWidth ? winWidth : $(window).width();
			var winOffset = $(window.parent.document).find('#rightContent').offset();
			var scrollTop = $(window.parent).scrollTop();
			$('#ciInfoModal').css({
				'width' : (winWidth * 0.8) + 'px',
				'position' : 'absolute',
				'margin-top' : '0px',
				'top' : (scrollTop + 100) + 'px',
				'margin-left' : -((winWidth * 0.8) / 2) + 'px'
			});
			$('#ciInfoModal').show();
			$('.modal-backdrop').click(function() {
				$('#ciInfoModal').remove();
			});
		}
	});
}

function showCiRelationRelyThisCiChart(item, ciId) {

	var $ciRelationChart = $(item).parent().parent().parent().parent().parent().parent().next('div');

	if ($('#ciInfoModal').length > 0) {
		$('#ciInfoModal').remove();
	}

	$.ajax({
		type : 'GET',
		dataType : 'html',
		url : '/balantflow/module/balantcmdb/cmdb/showCiRelationRelyThisCiChart.do',
		data : {
			ciId : ciId
		},
		async : false,
		success : function(data) {
			$('body').append(data);
			$('body').css('position', 'relative');
			var winWidth = $(window.parent.document).find('#rightContent').width();
			winWidth = winWidth ? winWidth : $(window).width();
			var winOffset = $(window.parent.document).find('#rightContent').offset();
			var scrollTop = $(window.parent).scrollTop();
			$('#ciInfoModal').css({
				'width' : (winWidth * 0.8) + 'px',
				'position' : 'absolute',
				'margin-top' : '0px',
				'top' : (scrollTop + 100) + 'px',
				'margin-left' : -((winWidth * 0.8) / 2) + 'px'
			});
			$('#ciInfoModal').show();
			$('.modal-backdrop').click(function() {
				$('#ciInfoModal').remove();
			});
		}
	});
}

function closeChooseCiInfoModal() {
	$('#chooseCiInfoModal').hide();
	if ($('#hidCiIDABC').length > 0) {
		$('#divChooseCiDivABC').remove();
		$('#hidCiIDABC').remove();
	}

}

function showMonCiInfo(ciId, scheduleId, pluginId) {
	if ($('#ciMonInfoModal').length > 0) {
		return;
	}
	$.get('/balantflow/module/balantcmdb/cmdb/showMonCiInfo.do', {
		ciId : ciId,
		scheduleId : scheduleId,
		pluginId : pluginId
	}, function(data) {
		$('body').append(data);
		$('#ciMonInfoModal').modal('show');
		$('.modal-backdrop').click(function() {
			$('#ciMonInfoModal').remove();
		});
	});
}

function closeCommonModal(modal) {
	$('#' + modal).modal('hide');
	$('#' + modal).remove();
}

function closeModal(item) {
	item = $(item);
	item.parent().parent().remove();

}

function closeCiMonInfoModal() {
	$('#ciMonInfoModal').modal('hide');
	$('#ciMonInfoModal').remove();
}

function showCiDetailInfo(obj, ciId, refCiId, ciDisplayName) {

	if ($(obj).next('div').length) {
		$(obj).next('div').remove();
	}

	if ($('#ciInfoDiv' + ciId + 'RefCi' + refCiId).length > 0) {
		var $newCiDiv = $('#ciInfoDiv' + ciId + 'RefCi' + refCiId);
		$newCiDiv.hide();
		$newCiDiv.attr('display', 'none');
		$('#ciInfoDiv' + ciId).prepend($newCiDiv);
		$newCiDiv.fadeIn(800);
		if (!$(obj).next('div').length) {
			var randomColor = $('#ciInfoDtailTd' + refCiId).css('background-color');
			$(obj).after('<div style="display:inline-block;margin-left:2px;width:10px;height:10px;background-color:' + randomColor + ';"></div>');
		}

		if ($('#switch' + ciId).hasClass('switch')) {
			// 关闭下游信息
			var $a = $(obj).parent().parent().find('td').eq(-2).find('a:first-child');
			var rel = $a.attr('rel');
			if (rel == '1') {
				$a.attr('rel', '0');
				$('#relyIconId' + ciId).attr('class', 'icon-chevron-down icon-white');
				$('#ciInfoForRelyTr' + ciId).fadeOut(800);
			}

			$('#switch' + ciId).removeClass('switch').addClass('switchOpen');
			$('#ciInfoTr' + ciId).fadeIn(800);
		}
	} else {

		$.ajax({
			type : 'POST',
			dataType : 'html',
			url : '/balantflow/module/balantcmdb/cmdb/getCiDetailInfoByCiId.do?ciId=' + refCiId,
			async : false,
			success : function(data) {
				$('#ciInfoTr' + ciId).fadeIn(800);
				var randomColor = getRandomColor();
				var $newCiDiv = $('<div id="ciInfoDiv' + ciId + 'RefCi' + refCiId + '" style="display:none;width:100%;">' + data + '</div>');
				$('#ciInfoDiv' + ciId).prepend($newCiDiv);
				$newCiDiv.fadeIn(800);
				$('#ciInfoDtailTd' + refCiId).css('background-color', randomColor);
				$('#ciInfoDtailTd' + refCiId).attr('title', '配置项显示名:' + ciDisplayName);
				$(obj).after('<div style="display:inline-block;margin-left:2px;width:10px;height:10px;background-color:' + randomColor + ';"></div>');

				if ($('#switch' + ciId).hasClass('switch')) {
					// 关闭下游信息
					var $a = $(obj).parent().parent().find('td').eq(-2).find('a:first-child');
					var rel = $a.attr('rel');
					if (rel == '1') {
						$a.attr('rel', '0');
						$('#relyIconId' + ciId).attr('class', 'icon-chevron-down icon-white');
						$('#ciInfoForRelyTr' + ciId).fadeOut(800);
					}

					$('#switch' + ciId).removeClass('switch').addClass('switchOpen');
				}

			}
		});
	}

}

function showCiDetailInfoForRelyCi(obj, ciId, refCiId, ciDisplayName) {

	if ($(obj).next('div').length) {
		$(obj).next('div').remove();
	}

	if ($('#ciInfoDivForRelyCi' + ciId + 'RefCi' + refCiId).length > 0) {
		var $newCiDiv = $('#ciInfoDivForRelyCi' + ciId + 'RefCi' + refCiId);
		$newCiDiv.hide();
		$newCiDiv.attr('display', 'none');
		$('#ciInfoDivForRelyCi' + ciId).prepend($newCiDiv);
		$newCiDiv.fadeIn(800);
		if (!$(obj).next('div').length) {
			var randomColor = $('#ciInfoDtailTdForRelyCi' + refCiId).css('background-color');
			$(obj).after('<div style="display:inline-block;margin-left:2px;width:10px;height:10px;background-color:' + randomColor + ';"></div>');
		}

		if ($('#switchForRelyCi' + ciId).hasClass('switch')) {
			$('#switchForRelyCi' + ciId).removeClass('switch').addClass('switchOpen');
			$('#ciInfoTrForRelyCi' + ciId).fadeIn(800);
		}
	} else {

		$.ajax({
			type : 'POST',
			dataType : 'html',
			url : '/balantflow/module/balantcmdb/cmdb/getCiDetailInfoByCiIdForRelyCi.do?ciId=' + refCiId,
			async : false,
			success : function(data) {
				$('#ciInfoTrForRelyCi' + ciId).fadeIn(800);
				var randomColor = getRandomColor();
				var $newCiDiv = $('<div id="ciInfoDivForRelyCi' + ciId + 'RefCi' + refCiId + '" style="display:none;width:100%;">' + data + '</div>');
				$('#ciInfoDivForRelyCi' + ciId).prepend($newCiDiv);
				$newCiDiv.fadeIn(800);
				$('#ciInfoDtailTdForRelyCi' + refCiId).css('background-color', randomColor);
				$('#ciInfoDtailTdForRelyCi' + refCiId).attr('title', '配置项显示名:' + ciDisplayName);
				$(obj).after('<div style="display:inline-block;margin-left:2px;width:10px;height:10px;background-color:' + randomColor + ';"></div>');

				if ($('#switchForRelyCi' + ciId).hasClass('switch')) {
					$('#switchForRelyCi' + ciId).removeClass('switch').addClass('switchOpen');
				}

			}
		});
	}

}

function showRefCiDetailInfo(obj, refCiId, ciDisplayName) {

	if ($(obj).next('div').length) {
		$('#ciInfoDiv' + ciId + 'RefCi' + refCiId).fadeOut('slow');
		$(obj).next('div').remove();
	}

	var ciId = $(obj).parent().parent().parent().parent().parent().parent().parent().parent().attr('ciId');

	var $thisCiDetailDivID = $(obj).parent().parent().parent().parent().parent().parent().parent();
	if ($('#ciInfoDiv' + ciId + 'RefCi' + refCiId).length > 0) {
		var $newCiDiv = $('#ciInfoDiv' + ciId + 'RefCi' + refCiId);
		$newCiDiv.hide();
		$newCiDiv.attr('display', 'none');
		$thisCiDetailDivID.after($newCiDiv);
		$newCiDiv.fadeIn(800);
		if (!$(obj).next('div').length) {
			var randomColor = $('#ciInfoDtailTd' + refCiId).css('background-color');
			$(obj).after('<div style="display:inline-block;margin-left:2px;width:10px;height:10px;background-color:' + randomColor + ';"></div>');
		}
	} else {

		$.ajax({
			type : 'POST',
			dataType : 'html',
			url : '/balantflow/module/balantcmdb/cmdb/getCiDetailInfoByCiId.do?ciId=' + refCiId,
			async : false,
			success : function(data) {
				$('#ciInfoTr' + ciId).fadeIn(800);
				var randomColor = getRandomColor();
				var $newCiDiv = $('<div id="ciInfoDiv' + ciId + 'RefCi' + refCiId + '" style="display:none;width:100%;">' + data + '</div>');
				$thisCiDetailDivID.after($newCiDiv);
				$newCiDiv.fadeIn(800);
				$('#ciInfoDtailTd' + refCiId).css('background-color', randomColor);
				$('#ciInfoDtailTd' + refCiId).attr('title', '配置项显示名:' + ciDisplayName);
				$(obj).after('<div style="display:inline-block;margin-left:2px;width:10px;height:10px;background-color:' + randomColor + ';"></div>');

				if ($('#switch' + ciId).hasClass('switch')) {
					$('#switch' + ciId).removeClass('switch').addClass('switchOpen');
				}

			}
		});
	}

}

function showRefCiDetailInfoForRelyCi(obj, refCiId, ciDisplayName) {

	if ($(obj).next('div').length) {
		$('#ciInfoDivForRelyCi' + ciId + 'RefCi' + refCiId).fadeOut('slow');
		$(obj).next('div').remove();
	}

	var ciId = $(obj).parent().parent().parent().parent().parent().parent().parent().parent().attr('ciId');

	var $thisCiDetailDivID = $(obj).parent().parent().parent().parent().parent().parent().parent();
	if ($('#ciInfoDivForRelyCi' + ciId + 'RefCi' + refCiId).length > 0) {
		var $newCiDiv = $('#ciInfoDivForRelyCi' + ciId + 'RefCi' + refCiId);
		$newCiDiv.hide();
		$newCiDiv.attr('display', 'none');
		$thisCiDetailDivID.after($newCiDiv);
		$newCiDiv.fadeIn(800);
		if (!$(obj).next('div').length) {
			var randomColor = $('#ciInfoDtailTdForRelyCi' + refCiId).css('background-color');
			$(obj).after('<div style="display:inline-block;margin-left:2px;width:10px;height:10px;background-color:' + randomColor + ';"></div>');
		}
	} else {

		$.ajax({
			type : 'POST',
			dataType : 'html',
			url : '/balantflow/module/balantcmdb/cmdb/getCiDetailInfoByCiIdForRelyCi.do?ciId=' + refCiId,
			async : false,
			success : function(data) {
				$('#ciInfoTrForRelyCi' + ciId).fadeIn(800);
				var randomColor = getRandomColor();
				var $newCiDiv = $('<div id="ciInfoDivForRelyCi' + ciId + 'RefCi' + refCiId + '" style="display:none;width:100%;">' + data + '</div>');
				$thisCiDetailDivID.after($newCiDiv);
				$newCiDiv.fadeIn(800);
				$('#ciInfoDtailTdForRelyCi' + refCiId).css('background-color', randomColor);
				$('#ciInfoDtailTdForRelyCi' + refCiId).attr('title', '配置项显示名:' + ciDisplayName);
				$(obj).after('<div style="display:inline-block;margin-left:2px;width:10px;height:10px;background-color:' + randomColor + ';"></div>');

				if ($('#switchForRelyCi' + ciId).hasClass('switch')) {
					$('#switchForRelyCi' + ciId).removeClass('switch').addClass('switchOpen');
				}
			}
		});
	}

}

var showCiDetaiInfoTr = function(item) {
	item = $(item);
	var ciId = item.attr('ciid');

	if (item.hasClass('switchOpen')) {
		item.removeClass('switchOpen').addClass('switch');
		$('#ciInfoTr' + ciId).fadeOut("slow");
	} else {
		var $a = $(item).parent().find('td').eq(-2).find('a:first-child');
		var rel = $a.attr('rel');

		if (rel == '1') {
			$a.attr('rel', '0');
			$('#relyIconId' + ciId).attr('class', 'icon-chevron-down icon-white');
			$('#ciInfoForRelyTr' + ciId).fadeOut(800);
		}

		item.removeClass('switch').addClass('switchOpen');
		var $nextTd = item.next('td');
		// 找到属性是名称的td
		if ($nextTd.attr('name') != 'name') {
			$nextTd = $nextTd.next('td');
		}
		var $a = $nextTd.find('a:first-child');
		var ciDisplayName = $a.html();
		// 如果ci信息为空则将这个ci的信息展示出来
		if (!$('#ciInfoDiv' + ciId).has('div').length) {
			showCiDetailInfo($a, ciId, ciId, ciDisplayName);
		}

		$('#ciInfoTr' + ciId).fadeIn(800);

	}
};

var showCiDetaiInfoTrForRelyCi = function(item) {
	item = $(item);
	var ciId = item.attr('ciid');

	if (item.hasClass('switchOpen')) {
		item.removeClass('switchOpen').addClass('switch');
		$('#ciInfoTrForRelyCi' + ciId).fadeOut("slow");
	} else {
		item.removeClass('switch').addClass('switchOpen');
		var $nextTd = item.next('td');
		// 找到属性是名称的td
		if ($nextTd.attr('name') != 'name') {
			$nextTd = $nextTd.next('td');
		}
		var $a = $nextTd.find('a:first-child');
		var ciDisplayName = $a.html();
		// 如果ci信息为空则将这个ci的信息展示出来
		if (!$('#ciInfoDivForRelyCi' + ciId).has('div').length) {
			showCiDetailInfoForRelyCi($a, ciId, ciId, ciDisplayName);
		}

		$('#ciInfoTrForRelyCi' + ciId).fadeIn(800);

	}
};

var removeCiDetailInfoTableFromTd = function(item) {
	item = $(item);
	var $ciDiv = item.parent().parent().parent().parent().parent().parent().parent().parent();
	var ciId = $ciDiv.attr('ciId');
	var $thisCiDiv = item.parent().parent().parent().parent().parent().parent().parent();
	$thisCiDiv.fadeOut("slow");
	$thisCiDiv.remove();

	if (!$ciDiv.has('div').length) {
		var $switchTd = $('#switch' + ciId);
		$('#ciInfoTr' + ciId).fadeOut("slow");
		$switchTd.removeClass('switchOpen').addClass('switch');
	}
};

var removeCiDetailInfoTableFromTdForRelyCi = function(item) {
	item = $(item);
	var $ciDiv = item.parent().parent().parent().parent().parent().parent().parent().parent();
	var ciId = $ciDiv.attr('ciId');
	var $thisCiDiv = item.parent().parent().parent().parent().parent().parent().parent();
	$thisCiDiv.fadeOut("slow");
	$thisCiDiv.remove();

	if (!$ciDiv.has('div').length) {
		var $switchTd = $('#switchForRelyCi' + ciId);
		$('#ciInfoTrForRelyCi' + ciId).fadeOut("slow");
		$switchTd.removeClass('switchOpen').addClass('switch');
	}
};

var getRandomColor = function() {
	return '#' + (function(color) {
		return (color += '0123456789abcdef'[Math.floor(Math.random() * 16)]) && (color.length == 6) ? color : arguments.callee(color);
	})('');
};
