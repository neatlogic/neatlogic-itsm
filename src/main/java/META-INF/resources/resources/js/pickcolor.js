var colorPanelCurrent = null;
var isinit = false;
function initColorTable() {
	var ColorHex = new Array('00', '33', '66', '99', 'CC', 'FF');
	var SpColorHex = new Array('FF0000', '00FF00', '0000FF', 'FFFF00', '00FFFF', 'FF00FF');
	var colorTable = [];
	for (var i = 0; i < 2; i++) {
		for (var j = 0; j < 6; j++) {
			colorTable[colorTable.length] = '<tr height=12><td width=11 style="background-color:#000000">';
			if (i == 0) {
				colorTable[colorTable.length] = '<td width=11 style="background-color:#';
				colorTable[colorTable.length] = ColorHex[j];
				colorTable[colorTable.length] = ColorHex[j];
				colorTable[colorTable.length] = ColorHex[j];
				colorTable[colorTable.length] = '">';
			} else {
				colorTable[colorTable.length] = '<td width=11 style="background-color:#';
				colorTable[colorTable.length] = SpColorHex[j];
				colorTable[colorTable.length] = '">';
			}
			colorTable[colorTable.length] = '<td width=11 style="background-color:#000000">';
			for (var k = 0; k < 3; k++) {
				for (var l = 0; l < 6; l++) {
					colorTable[colorTable.length] = '<td width=11 style="background-color:#';
					colorTable[colorTable.length] = ColorHex[k + i * 3];
					colorTable[colorTable.length] = ColorHex[l] + ColorHex[j];
					colorTable[colorTable.length] = '">';
				}
			}
		}
	}
	var colorTableStr = '<div id="colorInnerPanel" style="position:absolute;z-index:1060 ;width:230px;">'
			+ '<table width="100%" border="0" cellspacing="0" cellpadding="0" style="border:1px #000000 solid;border-bottom:none;border-collapse: collapse" bordercolor="000000">' + '<tr height=30><td bgcolor=#cccccc>'
			+ '<table cellpadding="0" cellspacing="1" border="0" style="border-collapse: collapse">'
			+ '<tr><td width="3"></td><td><input type="text" id="DisColor" size="6"  disabled style="width:60px;border:solid 1px #000000;background-color:#ffff00"></td>'
			+ '<td width="3"></td><td><input type="text" id="HexColor" size="7" style="width:140px;border:inset 1px;font-family:Arial;" value="#000000"></td><td width="3"></td>'
			+ '<td onclick="closeColorPanel();" style="cursor:pointer;">&nbsp;X<br/>&nbsp;</td></tr></table></td></tr></table>'
			+ '<table id="colorTable" border="1" cellspacing="0" cellpadding="0" style="border-collapse: collapse" bordercolor="000000"  style="cursor:hand;">' + colorTable.join("") + '</table></div>';
	return colorTableStr;
};

function pickColor(config) {
	var json = {
		successFuc : null,// 回调方法
		target : null, // this
		container : null,// 要写到的对象
		resouce : null
	// 输出对象
	};
	$.extend(json, config);

	if (!isinit) {
		$("#colorpanel").append(initColorTable());
		target = json.target;
		container = json.container;

		$("#colorTable").mouseover(function(event) {
			if ((event.target.tagName == "TD") && (colorPanelCurrent != event.target)) {
				if (colorPanelCurrent != null) {
					colorPanelCurrent.style.backgroundColor = colorPanelCurrent._background;
				}
				event.target._background = event.target.style.backgroundColor;
				$("#DisColor").css("backgroundColor", event.target.style.backgroundColor);
				$("#HexColor").val(event.target.style.backgroundColor.toUpperCase());
				event.target.style.backgroundColor = "white";
				colorPanelCurrent = event.target;
			}
		}).mouseout(function(event) {
			if (colorPanelCurrent != null) {
				colorPanelCurrent.style.backgroundColor = colorPanelCurrent._background.toUpperCase();
			}
		}).click(function(event) {
			if (event.target.tagName == "TD") {
				var clr = event.target._background;
				if (!container) {
					$("#color").val(clr.toUpperCase());
				} else {
					container.val(clr.toUpperCase());
				}
				json.resouce.css("background", clr.toUpperCase());
				closeColorPanel();
				if (json.successFuc)
					json.successFuc();
				return clr;
			}
			event.cancelBubble = true;
			return false;
		});

		isinit = true;
	}
	$("#colorpanel").css("left", $(target).position().left + 150);
	$("#colorpanel").css("top", $(target).position().top + 120);
	$("#colorpanel").css("display", "block");

	return false;
}

function closeColorPanel() {
	$("#colorpanel").css("display", "none").html('');
	isinit = false;
	colorPanelCurrent = null;
}