;
(function($) {
	var tmp_panel = '<div class="jquery-ciinfo-panel"></div>';
	var tmp_panel_box = '<div class="jquery-ciinfo-panelbox"></div>';
	var tmp_panel_header = '<div class="jquery-ciinfo-panelheader"></div>';
	var tmp_close_btn = '<i class="jquery-ciinfo-closebtn fa fa-close"></i>';
	var tmp_panel_body = '<div class="jquery-ciinfo-panelbody"></div>';
	var tmp_panel_title = '<div class="jquery-ciinfo-paneltitle"></div>';
	var tmp_panel_main = '<div class="jquery-ciinfo-panelmain"></div>';
	var tmp_panel_arrow = '<span class="arrow"></span>';
	var $currentopenpanel = null;
	var formalciId;

	var ciinfo = function(target, options) {
		this.$target = $(target);
		this.panelWidth = 500;
		this.panelHeight = 400;
		this.offset = 15;
		this.parentContainer = options.parentContainer ? $(options.parentContainer) : null;
		this.updateFunctionName = options.updatefunction;
		this.$panel = null;
		this.init();
	};

	this.ciinfo = ciinfo;
	ciinfo.prototype = {
		init : function() {
			var that = this;
			if (!that.$target.data('hasouter')) {
				that.$target.wrap('<div class="jquery-ciinfo-panelcontainer"></div>');
				that.$wraper = that.$target.closest('.jquery-ciinfo-panelcontainer');
			}
			that.$target.append('<i class="ciinfo-icon ts-cube"></i>');
			if(!that.$target.data('cientityid') || that.$target.data('cientityid') =='undefined'){
				that.$target.css('cursor','not-allowed');
				that.$target.addClass('nocientity');
			}
			that.$target.on('click', function(e) {
				e.stopPropagation();
				if($(this).hasClass('nocientity')){
					return;
				}
				var needopen = true;
				var myciId = that.$target.data('cientityid');
				if (!that.$target.data('hasouter')) {
					if (that.$panel) {
						needopen = false;
						that.close();
					} else {
						$('.jquery-ciinfo-panel').each(function() {
							if ($(this).data('plugin')) {
								$(this).data('plugin').close();
							}
						});
						formalciId = that.$target.data('cientityid');
					}
				}
				if (needopen) {
					if (!that.$target.data('hasouter')) {
						var $panel = that.generateContainer();
						that.$wraper.append($panel);
						that.$target.data('panel', that);
						that.$panel = $panel;
					} else {
						that.$panel = that.$target.closest('.jquery-ciinfo-panel');
						that.$wraper = that.$target.closest('.jquery-ciinfo-panelcontainer');
						that.$target.data('panel', that);
					}
					var cientity, sourceCiList, canEdit = false, isOwner = false;
					var callArray = new Array();
					callArray.push($.getJSON('/balantflow/module/balantecmdb/cientity/' + myciId + '/source', function(data) {
						sourceCiList = data;
					}));
					callArray.push($.getJSON('/balantflow/module/balantecmdb/cientity/' + myciId, function(data) {
						cientity = data;
					}));
					callArray.push($.getJSON('/balantflow/module/balantecmdb/cientity/' + myciId + '/canedit', function(data) {
						canEdit = data.canEdit;
						isOwner = data.isOwner;
					}));
					$.when.apply($, callArray).done(function() {
						cientity.canEdit = canEdit;
						cientity.isOwner = isOwner;
						if ((cientity.attrEntityList && cientity.attrEntityList.length > 0) || (cientity.relEntityList && cientity.relEntityList.length > 0)) {
							if (that.$target.data('hasouter')) {
								if (formalciId == myciId) {
									that.$panel.find('.jquery-ciinfo-paneltitle').find('.title').empty().append('<span class="ciInfo now" data-cientityid="' + myciId + '" data-hasouter="1" style="max-width:100%;">' + cientity.name + '</span>');
								} else {
									var repeatid = false;
									that.$panel.find('.jquery-ciinfo-paneltitle').find('.ciInfo').each(function(ind) {
										if ($(this).data('cientityid') == myciId) {
											$(this).addClass('now').siblings().removeClass('now');
											$(this).nextAll('.ciInfo').remove();
											that.$panel.find('.jquery-ciinfo-paneltitle').find('.title').scrollLeft();
											repeatid = true;
										}
									})
									if (!repeatid) {
										that.$panel.find('.jquery-ciinfo-paneltitle').find('.ciInfo').removeClass('now');
										that.$panel.find('.jquery-ciinfo-paneltitle').find('.title').append('<span class="ciInfo now" data-cientityid="' + myciId + '" data-hasouter="1" title="' + cientity.name + '">' + cientity.name + '</span>');
									}
									that.$panel.find('.jquery-ciinfo-paneltitle').find('.ciInfo').css('max-width', 100 / (that.$panel.find('.jquery-ciinfo-paneltitle').find('.ciInfo').length) + '%');
								}
								that.$panel.data('plugin').getinnercontent(cientity, sourceCiList);
								that.$panel.data('plugin').updatePosition();
							} else {
								if(that.$panel){
									that.$panel.find('.jquery-ciinfo-paneltitle').find('.title').empty().append('<span class="ciInfo now" data-cientityid="' + myciId + '" data-hasouter="1" style="max-width:100%;">' + cientity.name + '</span>');
									that.getinnercontent(cientity, sourceCiList);
									that.updatePosition();									
								}
							}
							if(that.$target.parents('.jquery-scrolltable').length>0){
								var tablecontainer=that.$target.parents('.jquery-scrolltable-container');
								tablecontainer.height(Math.max(tablecontainer.height(),that.$wraper.position().top+that.$panel.outerHeight()+that.$panel.position().top+10));
								that.$target.parents('.jquery-scrolltable').trigger('resize');
							}
							that.updateTableheight();
						}
					});
				}
			});
			that.$target.attr('data-bind', true);
		},
		close : function() {
			var that = this;
			if (that.$panel) {
				that.$target.data('panel').$panel.remove();
				that.$target.data('panel', null);
				that.$panel = null;
				if(that.$wraper.offsetParent().hasClass('tsscroll-container')){
					that.$wraper.offsetParent().height('');
				}
				if(that.$target.parents('.jquery-scrolltable').length>0){
					var tablecontainer=that.$target.parents('.jquery-scrolltable-container');
					tablecontainer.css('height','auto');
					that.$target.parents('.jquery-scrolltable').trigger('resize');
				}
			}
			formalciId = null;
		},
		updateTableheight: function(){
			var that = this;
			if(that.$panel){
				if(that.$wraper.offsetParent() && that.$wraper.offsetParent().hasClass('tsscroll-container')){
					that.$wraper.offsetParent().height('');
					if(that.$panel.offset().top+that.$panel.height()+10 > that.$wraper.offsetParent().offset().top+that.$wraper.offsetParent().height()){
						that.$wraper.offsetParent().height(that.$panel.offset().top+that.$panel.height()+10-that.$wraper.offsetParent().offset().top);
					}else{
						that.$wraper.offsetParent().height('');
					}								
				}				
			}
		},
		generateContainer : function() {
			var that = this;
			var $panelbox = $(tmp_panel_box);
			var $panelheader = $(tmp_panel_header);
			var $panelmain = $(tmp_panel_main);
			var $paneltitle = $(tmp_panel_title);
			var $panelbody = $(tmp_panel_body);
			var $panel = $(tmp_panel);
			$panel.data('plugin', this);
			var $arrow = $(tmp_panel_arrow);
			var $closebtn = $(tmp_close_btn);
			$closebtn.on('click', function() {
				if (that.$panel) {
					that.close();
				}
			});
			$panelheader.append($closebtn);
			$paneltitle.append('<div class="title"></div>');
			$panelmain.append($paneltitle);
			$panelmain.append($panelbody);
			$panelbox.append($arrow).append($panelheader).append($panelmain);
			$panel.append($panelbox);
			$panel.css('visibility', 'hidden');
			return $panel;
		},
		getinnercontent : function(cientity, sourceCiList) {
			var that = this;
			var randomid = new Date().getTime();
			var $cientityContainer = $('<div role="tabpanel" class="tab-pane active" id="detail' + cientity.id + '_' + randomid + '"></div>');
			var $sourceContainer = $('<div role="tabpanel" class="tab-pane" style="text-align:left" id="source' + cientity.id + '_' + randomid + '"></div>');
			var $tableouter = $('<div class="jquery-ciinfo-citableouter" plugin-scrollbar></div>');
			var table = $('<table class="jquery-ciinfo-citable"></table>');
			var $tab = $('<ul class="nav-bdbottom nav-tabs" role="tablist"></ul>');
			var $tabbody = $('<div class="tab-content" style="padding:10px 5px;"></div>');
			if (cientity.attrEntityList) {
				for (var i = 0; i < cientity.attrEntityList.length; i++) {
					var attr = cientity.attrEntityList[i];
					var tr = $('<tr></tr>');
					var tdLabel = $('<td nowrap class="jquery-ciinfo-citable-tdtitle"></td>');
					var tdValue = $('<td class="jquery-ciinfo-citable-tdcontent"></td>');
					tdLabel.html(attr.label + '：');
					var value = '';
					for ( var v in attr.valueList) {
						if (value != '') {
							value += ';';
						}
						value += attr.valueList[v];
					}
					tdValue.append(value);
					tr.append(tdLabel).append(tdValue);
					table.append(tr);
				}
			}
			if (cientity.relEntityList) {
				for (var i = 0; i < cientity.relEntityList.length; i++) {
					var rel = cientity.relEntityList[i];
					var tr = $('<tr></tr>');
					var tdLabel = $('<td nowrap class="jquery-ciinfo-citable-tdtitle"></td>');
					var tdValue = $('<td class="jquery-ciinfo-citable-tdcontent"></td>');
					tdLabel.html(rel.label + '：');
					var value = '';
					for ( var v in rel.targetCiEntityList) {
						value += '<div><span class="ciInfo" data-cientityid="' + rel.targetCiEntityList[v].id + '" data-hasouter="1" >' + rel.targetCiEntityList[v].name + '</span></div>';
					}
					tdValue.html(value);
					tr.append(tdLabel).append(tdValue);
					table.append(tr);
				}
			}

			var lici = '<li role="presentation"  class="active">' + '<a href="#detail' + cientity.id + '_' + randomid + '" class="lnkCiInfo" role="tab" data-toggle="tab">配置项信息</a>' + '</li>';
			$tab.append(lici);
			$tableouter.append(table);
			$cientityContainer.append($tableouter);
			$tabbody.append($cientityContainer);
			if (sourceCiList && sourceCiList.length > 0) {
				for ( var s in sourceCiList) {
					var sourceCi = sourceCiList[s];
					var sourceDiv = $('<div class="divCi" style="color:#666"></div>');
					var sourceName = $('<div style="padding: 5px 0px;"><i class="ts ts-caret-down" style="cursor:pointer;"></i><b>' + sourceCi.name + '(' + sourceCi.ciEntityList.length + ')</b></div>');
					var sourceUlouter = $('<div class="divCiEntity"></div>');
					var sourceUl = $('<ul style="padding:0 25px 5px"></ul>');
					for ( var sc in sourceCi.ciEntityList) {
						var scientity = sourceCi.ciEntityList[sc];
						sourceUl.append('<li style="font-size:95%;"><span class="ciInfo" data-cientityid="' + scientity.id + '" data-hasouter="1">' + scientity.name + '</span></li>');
					}
					sourceUlouter.append(sourceUl);
					sourceDiv.append(sourceName).append(sourceUlouter);
					$sourceContainer.append(sourceDiv);
					sourceName.find('.ts').on('click', function() {
						if ($(this).hasClass('ts-caret-right')) {
							$(this).removeClass('ts-caret-right').addClass('ts-caret-down');
							$(this).parent().siblings('.divCiEntity').stop().show();
						} else {
							$(this).removeClass('ts-caret-down').addClass('ts-caret-right');
							$(this).parent().siblings('.divCiEntity').stop().hide();
						}
					});
				}
				var lisource = '<li role="presentation" >' + '<a href="#source' + cientity.id + '_' + randomid + '" class="lnkCiInfo" role="tab" data-toggle="tab">关联配置项</a>' + '</li>';
				$tab.append(lisource);
				$tabbody.append($sourceContainer);
			}
			if (cientity.canEdit && that.updateFunctionName && typeof window[that.updateFunctionName] == 'function') {
				var btn = $('<a style="width:100%" href="javascript:void(0)" data-ciid="' + cientity.ciId + '" data-cientityid="' + cientity.id + '" data-isowner="' + cientity.isOwner + '"><i class="ts-pencil"></i>&nbsp;编辑配置项</a>');
				btn.on('click', function() {
					var cientityid = $(this).data('cientityid');
					var isOwner = $(this).data('isowner');
					var ciId = $(this).data('ciid');
					var callArray = new Array();
					var allowTransaction = true;
					var html = null;
					var form = null;
					callArray.push($.getJSON('/balantflow/module/balantecmdb/ci/' + ciId + '/rolecheck/3', function(data) {
						allowTransaction = data.allowed;
					}));

					callArray.push($.getJSON('/balantflow/module/balantecmdb/cientity/' + cientityid + '/edit', function(data) {
						var json = {};
						json.ci = data;
						json.ciEntityId = cientityid;
						html = xdoT.render('balantecmdb.cientity.edit.editcientity', json);
						form = html.find('form');
					}));

					$.when.apply($, callArray).done(function() {
						var flag = true;
						var btnList = new Array();
						btnList.push({
							text : '保存事务',
							id : 'btnSaveTransaction',
							classname : 'btn-primary',
							click : function() {
								$(this).attr('disabled', 'disabled');
								form.find('#hidSaveMode').val(0);
								var flag = window[that.updateFunctionName](form);
								$(this).removeAttr('disabled');
								return flag;
							}
						});
						if (isOwner || allowTransaction) {
							btnList.push({
								text : '保存并提交事务',
								id : 'btnSaveAndCommitTransaction',
								classname : 'btn-success',
								click : function() {
									$(this).attr('disabled', 'disabled');
									form.find('#hidSaveMode').val(1);
									var flag = window[that.updateFunctionName](form);
									$(this).removeAttr('disabled');
									return flag;
								}
							});
						}
						createSlideDialog({
							title : '编辑配置项',
							content : html,
							width : '90%',
							customButtons : btnList
						});
					});
				});
				$cientityContainer.append(btn);
			}
			// 修复下拉菜单点击展开后弹窗空白区域和tab点击会收起下拉bug
			$(document).on('click.bs.dropdown.data-api', '.jquery-ciinfo-panelbody', function(e) {
				e.stopPropagation();
			})
			$tab.data('panel', that);
			that.$panel.find('.jquery-ciinfo-panelbody').empty().append($tab).append($tabbody);
		},
		updatePosition : function() {
			var that = this;
			if (that.$panel) {
				that.$panel.removeClass('top').removeClass('top-left').removeClass('top-right').removeClass('bottom-left').removeClass('bottom-right').removeClass('bottom');
				that.$panel.addClass(this.getPlacement());
				that.$panel.css(this.getPosition());
				that.$panel.css('visibility', 'visible');
			}
		},
		getpos : function() {
			var that = this;
			var pos = {
				top : that.$wraper.position().top - that.$wraper.offsetParent().scrollTop(),
				left : that.$wraper.position().left - that.$wraper.offsetParent().scrollLeft(),
				right : that.$target.outerWidth() + that.$wraper.position().left,
				bottom : that.$target.outerHeight() + that.$wraper.position().top - that.$wraper.offsetParent().offset().top,
				roRight : that.$wraper.offsetParent().width() - (that.$target.outerWidth() + that.$wraper.position().left)
			};
			return pos;
		},
		getPosition : function() {
			var that = this;
			var returnPos = {};
			var pos = that.getpos();
			if ((that.$panel.height() + that.offset) > pos.top) {// 飘下面
				returnPos.top = 5 + that.$target.outerHeight();
			} else {
				returnPos.top = -(5 + that.$panel.outerHeight());// 飘上面
				// returnPos.bottom = 10 + that.$target.outerHeight();
			}
			
			if (that.$panel.width() > pos.right) {// 飘右边或中间
				if (that.$panel.width() * 0.5 >= pos.left + that.$target.outerWidth() * 0.5) {
					returnPos.left = that.$target.outerWidth() * 0.5 - 21;// 飘右边,21是箭头到左边边框距离
				} else {
					returnPos.left = -Math.max(that.$panel.width() * 0.5 - that.$target.outerWidth() * 0.5,pos.left-2);// 飘中间
				}
			} else {// 飘左边
				if (this.$panel.width() * 0.5 <= pos.roRight) {
					returnPos.left = -(that.$panel.width() * 0.5 - that.$target.outerWidth() * 0.5);
				} else {
					returnPos.left = that.$target.outerWidth() - that.$panel.width() + (30 - that.$target.outerWidth() * 0.5);// 30是箭头到右边边框距离
				}
			}
			return returnPos;
		},
		getPlacement : function() {
			var that = this;
			var pos = that.getpos();
			var pv = 'top';
			var ph = 'left';
			if ((that.$panel.height() + that.offset) > pos.top) {
				pv = 'bottom';
			} else {
				pv = 'top';
			}
			if (that.$panel.width() > pos.right) {
				if (that.$panel.width() * 0.5 > pos.left + that.$target.outerWidth() * 0.5) {// 飘中间
					ph = 'right';
				} else {
					ph = '';
				}
			} else {
				if (that.$panel.width() / 2 <= pos.roRight) {
					ph = '';
				} else {
					ph = 'left';
				}
			}
			if (ph) {
				return pv + '-' + ph;
			} else {
				return pv;
			}
		}
	};

	$.fn.ciinfo = function(options) {
		var $target = $(this);
		if (!$target.data('bind') && $target.data('cientityid')) {
			new ciinfo($target, options);
		}
		return this;
	};

	$(function() {

		$(document).on('shown.bs.tab', '.lnkCiInfo', function() {
			if($(this).closest('.nav-tabs').data('panel')){
				$(this).closest('.nav-tabs').data('panel').updatePosition();
				$(this).closest('.nav-tabs').data('panel').updateTableheight();				
			}
		});

		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('.ciInfo').each(function() {
				var config = {};
				if ($(this).data('parentcontainer')) {
					config['parentContainer'] = $(this).data('parentcontainer');
				}
				if ($(this).data('updatefunction')) {
					config['updatefunction'] = $(this).data('updatefunction');
				}
				$(this).ciinfo(config);
			});
		});

		$(document).bind('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('.ciInfo').each(function() {
				var config = {};
				if ($(this).data('parentcontainer')) {
					config['parentContainer'] = $(this).data('parentcontainer');
				}
				if ($(this).data('updatefunction')) {
					config['updatefunction'] = $(this).data('updatefunction');
				}
				$(this).ciinfo(config);
			});
		});

		$('.ciInfo').each(function() {
			var config = {};
			if ($(this).data('parentcontainer')) {
				config['parentContainer'] = $(this).data('parentcontainer');
			}
			if ($(this).data('updatefunction')) {
				config['updatefunction'] = $(this).data('updatefunction');
			}
			$(this).ciinfo(config);
		});
	});

})(jQuery);