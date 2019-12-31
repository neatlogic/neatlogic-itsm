/**
 * 自定义文章链接跳转 by kong 2016/4/18
 */
CKEDITOR.dialog
		.add(
				'myDialog',
				function(editor) {
					var plugin = CKEDITOR.plugins.link;
					// 获取通过editor和data获取a属性
					var getLinkAttributes = function(editor, data) {
						var set = {};
						switch (data.type) {
						case 'target':
							var url = CKEDITOR.tools.trim(data.target);
							var className = CKEDITOR.tools.trim(data.className);

							set['class'] = className;
							set['data-target'] = url;
							set['title'] = url;
							set['href'] = 'javascript:void(0)';
							break;
						}
						return {
							set : set,
						};

					};
					return {
						title : '引用文档',
						minWidth : 400,
						minHeight : 200,
						onShow : function() {
							var editor = this.getParentEditor(), selection = editor
									.getSelection(), element = null;
							if ((element = plugin.getSelectedLink(editor))
									&& element.hasAttribute('data-target')) {
								if (!selection.getSelectedElement())
									selection.selectElement(element);
							} else
								element = null;

							// var data = plugin.parseLinkAttributes( editor,
							// element );
							var data = null;
							// Record down the selected element in the dialog.
							this._.selectedElement = element;

							this.setupContent(data);

							// 初始化a
							var element = CKEDITOR.tools
									.callFunction(getSelectElement); // 获取选择对象
							var elementLabel = CKEDITOR.tools
									.callFunction(getSelectedLabel); // 获取选择a对象
							var type = element.$.tagName;
							if (type.toLowerCase() == 'a') {
								text = element.getAttribute( 'data-target' ); 
							} else {
								text = CKEDITOR.tools
								.callFunction(getSelectText); //获取选择文本
							}
							// 获取dialog的text，并赋值
							CKEDITOR.dialog.getCurrent().getContentElement(
									'tab1', 'linkPageName').setValue(text);
						},
						contents : [ {
							id : 'tab1',
							label : 'First Tab',
							title : 'First Tab',
							elements : [ {
								id : 'linkPageName',
								type : 'text',
								label : '文档标题',
								/*
								 * validate: function() { //验证 if (
								 * !this.getValue() ) { api.openMsgDialog( '',
								 * 'Name cannot be empty.' ); return false; } },
								 */
								/*
								 * setup: function( data ) { if ( data.tab1 )
								 * this.setValue( data.tab1.linkPageName); },
								 */
								commit : function(data) {
									data.type = 'target';
									data.target = this.getValue();
									data.className = 'pagelnk'
								}
							} ]
						} ],
						onOk : function() {
							var data = {};
							// 从dialog上面控件上收集数据
							this.commitContent(data);
							var selection = editor.getSelection(), attributes = getLinkAttributes(
									editor, data);

							if (!this._.selectedElement) {
								var range = selection.getRanges()[0];
								if (range.collapsed) {//如果没有选中对象
									var text = new CKEDITOR.dom.text(data.target);//在光标处新增text对象
									range.insertNode(text);
									range.selectNodeContents(text);
									
								}

								// 新增a标签
								var style = new CKEDITOR.style({
									element : 'a',
									attributes : attributes.set
								});

								style.type = CKEDITOR.STYLE_INLINE;
								style.applyToRange(range, editor);
								range.select();
								var link = range.getEnclosedNode();
								
								
								//如果是知识链接取消双击事件
								editor.on('doubleclick',function(e){
									var element=CKEDITOR.plugins.link.getSelectedLink(editor)||e.data.element;
									if(element.is("a") &&element.$.outerHTML.indexOf("data-target") > -1){
										//e.removeListener('doubleclick');//取消这个doubleclick 方法
										e.stop(); //马上停止所有该类型事件
										//editor.removeAllListeners();//取消所有事件 ，但不能马上生效    
										//editor.fire('doubleclick');
									}
									//alert(editor.hasListeners('doubleclick')); 
								},null,null,0);
								
								//右键菜单重初始化
								editor.contextMenu&&editor.contextMenu.addListener(function(a){
									editor.contextMenu&&editor.contextMenu.removeAll(); //去掉所有右键菜单
									if(!a||a.isReadOnly())return null;
									a=CKEDITOR.plugins.link.tryRestoreFakeAnchor(editor,a);
									if(!a&&!(a=CKEDITOR.plugins.link.getSelectedLink(editor)))return null;
									var b={};
									//!a.getAttribute("data-target")   过滤知识link 右击事件 
									
									//添加超链接编辑
									!a.getAttribute("data-target")&&a.getAttribute("href")&&a.getChildCount()&&(b={link:CKEDITOR.TRISTATE_OFF,unlink:CKEDITOR.TRISTATE_OFF});
									//添加锚
									if(a&&a.hasAttribute("name")){b.anchor=b.removeAnchor=CKEDITOR.TRISTATE_OFF;}
									//添加文本链接编辑
									if(a.getAttribute("data-target")){b.pageLinkItem_1=b.pageLinkItem_2=CKEDITOR.TRISTATE_OFF;} //
									return b
								});
							} else {
								var element = this._.selectedElement;
								element.setAttributes(attributes.set);
								delete this._.selectedElement;
							}
						}
					};
				});