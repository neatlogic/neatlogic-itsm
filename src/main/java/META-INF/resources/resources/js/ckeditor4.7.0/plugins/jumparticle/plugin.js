/**
 * 自定义文章链接跳转 by kong 2016/4/18
 */

CKEDITOR.plugins.add('jumparticle', {
	requires : [ 'dialog' ],
	init : function(editor) {
		var pluginName = 'jumparticle';
		// 加载自定义窗口
		CKEDITOR.dialog.add('myDialog', this.path + "/article/topage.js");

		// 给自定义插件注册一个调用命令
		editor.addCommand(pluginName, new CKEDITOR.dialogCommand('myDialog'));

		// 注册一个按钮，来调用自定义插件
		editor.ui.addButton('MyButton', {
			// editor.lang.mine是我在zh-cn.js中定义的一个中文项，
			// 这里可以直接写英文字符，不过要想显示中文就得修改zh-cn.js
			label : '引用文档',
			command : pluginName,
			icon : this.path + '/img/pageLink.png'
		});
		// 添加自定义右键“编辑文章链接”菜单事件
		if (editor.contextMenu) {
			// 获取选中文本对象
			getSelectElement = CKEDITOR.tools.addFunction(function() {
				var mySelection = CKEDITOR.instances.editor.getSelection();// 获取selection
				var element = editor.getSelection().getStartElement();// 获取选择文本对应的对象
				return element;
			});
			// 获取选中文本a对象
			getSelectedLabel = CKEDITOR.tools.addFunction(function() {
				var selection = editor.getSelection();
				var selectedElement = selection.getSelectedElement();
				if ( selectedElement && selectedElement.is( 'a' ) )
					return selectedElement;

				var range = selection.getRanges()[ 0 ];

				if ( range ) {
					range.shrink( CKEDITOR.SHRINK_TEXT );
					return editor.elementPath( range.getCommonAncestor() ).contains( 'a', 1 );
				}
				return null;
			});
			// 获取选中文本对象
			getSelectText = CKEDITOR.tools.addFunction(function() {
				var mySelection = CKEDITOR.instances.editor.getSelection();// 获取selection
				var text = editor.getSelection().getSelectedText();// 获取选择文本
				return text;
			});
			// 添加移除选中对象，并插入html内容方法
			removeAddHtml = CKEDITOR.tools.addFunction(function(text, element) {
				var type = element.$.tagName;
				if (type.toLowerCase() == 'a'&&element.getAttribute( 'data-target' )) {// 选中的对象是a才移除
					element.remove();
					CKEDITOR.instances.editor.insertHtml(text);// 插入新a标签
				}
				
			});
			// 添加自定义右键“编辑/取消文章链接”菜单事件
			editor.addCommand('cancelLink',
					{
						exec : function() {
							var element = CKEDITOR.tools
									.callFunction(getSelectElement);
							CKEDITOR.tools.callFunction(removeAddHtml,
									element.$.innerHTML, element);
						}
					});
			editor.addMenuGroup('linkGroup', 2);// 添加两个右键子菜单项
			editor.addMenuItems({
				pageLinkItem_1 : {
					label : '编辑引用文档',
					command : 'jumparticle',
					icon : this.path + '/img/pageLink.png',
					group : 'linkGroup',
					order : 1
				},
				pageLinkItem_2 : {
					label : '取消引用文档',
					command : 'cancelLink',
					icon : this.path + '/img/cancelPageLink.png',
					group : 'linkGroup',
					order : 1
				}
			});
			// 添加右键菜单监听（显示pageLinkItem菜单项）
			editor.contextMenu.addListener(function(element) {
				if (element.getAscendant('a', true)) {
					if(element.getAttribute( 'data-target' )){
						return {
							pageLinkItem_1 : CKEDITOR.TRISTATE_OFF,
							pageLinkItem_2 : CKEDITOR.TRISTATE_OFF
						};// 右键显示pageLinkItem菜单项
					}
				}
			});
		}

	}
});