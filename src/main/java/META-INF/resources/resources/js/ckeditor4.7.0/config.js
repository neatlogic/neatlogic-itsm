/**
 * @license Copyright (c) 2003-2015, CKSource - Frederico Knabben. All rights
 *          reserved. For licensing, see LICENSE.md or
 *          http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function(config) {
	config.toolbar = [ [ 'Maximize', 'Undo', 'Redo', 'Bold', 'Italic', 'TextColor', 'BGColor', 'Underline', 'Strike', 'Font', 'FontSize', 'Smiley', 'Link' ] ]// ,'MyButton'
	config.extraPlugins = "uploadimage,pastefromword,jumparticle";// ,devtools";
	config.pasteFromWordRemoveStyles = true;
	config.pasteFromWordRemoveFontStyles = true;
	config.toolbarCanCollapse = true;
	config.toolbarStartupExpanded = false;
	config.allowedContent = {
		$1 : {
			// Use the ability to specify elements as an object.
			elements : CKEDITOR.dtd,
			attributes : true,
			styles : true,
			classes : true
		}
	};
	config.removePlugins = 'elementspath';
	config.disallowedContent = 'link; script; *[on*]';
	config.font_names = '宋体/SimSun;新宋体/NSimSun;仿宋/FangSong;楷体/KaiTi;仿宋_GB2312/FangSong_GB2312;' + '楷体_GB2312/KaiTi_GB2312;黑体/SimHei;华文细黑/STXihei;华文楷体/STKaiti;华文宋体/STSong;华文中宋/STZhongsong;'
			+ '华文仿宋/STFangsong;华文彩云/STCaiyun;华文琥珀/STHupo;华文隶书/STLiti;华文行楷/STXingkai;华文新魏/STXinwei;' + '方正舒体/FZShuTi;方正姚体/FZYaoti;细明体/MingLiU;新细明体/PMingLiU;微软雅黑/Microsoft YaHei;微软正黑/Microsoft JhengHei;' + 'Arial Black/Arial Black;'
			+ config.font_names;
	config.menu_groups = 'clipboard,form,tablecell,tablecellproperties,tablerow,tablecolumn,table,anchor,link,image,flash,checkbox,radio,textfield,hiddenfield,imagebutton,button,select,textarea,label';
};

CKEDITOR.on('dialogDefinition', function(ev) {
	// Take the dialog window name and its definition from the event data.
	var dialogName = ev.data.name;
	var dialogDefinition = ev.data.definition;

	if (dialogName == 'image') {
		dialogDefinition.removeContents('Upload');
		dialogDefinition.removeContents('advanced');
		dialogDefinition.removeContents('Link');
		var infoTab = dialogDefinition.getContents('info');
		infoTab.remove('browse');
		infoTab.remove('txtBorder');
		infoTab.remove('txtHSpace');
		infoTab.remove('txtVSpace');
		infoTab.remove('cmbAlign');
		var url = infoTab.get('txtUrl');
		url.style = 'display:none';
	} else if (dialogName == 'link') {
		dialogDefinition.removeContents('upload');
		dialogDefinition.removeContents('advanced');
		var infoTab = dialogDefinition.getContents('info');
		infoTab.remove('browse');
	}
});
