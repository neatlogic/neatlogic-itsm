(function($) {
	'namespace sumo';
	$.fn.checkselect = function(options) {

		// var is_visible_default = false;
		// $(document).click(function () { is_visible_default = false; });

		// This is the easiest way to have default options.
		var settings = $.extend({
			placeholder : '${tk:lang("请选择")}...', // Dont change it here.
			csvDispCount : 4, // display no. of items in multiselect. 0 to
			// display all.
			captionFormat : '{0} ${tk:lang("选择项")}', // format of caption
			//当所有的选项都被选中，而且个数大于csvDispCount时显示selectedAllTitle名称（比如"全部告警"）
			selectedAllTitle:null,
			// text. you can set
			// your locale.
			floatWidth : 400, // Screen width of device at which the list is
			// rendered in floating popup fashion.
			forceCustomRendering : false, // force the custom modal on all
			// devices below floatWidth
			// resolution.
			nativeOnDevice : [ 'Android', 'BlackBerry', 'iPhone', 'iPad', 'iPod', 'Opera Mini', 'IEMobile', 'Silk' ], //
			outputAsCSV : false, // true to POST data as csv ( false for Html
			// control array ie. deafault select )
			csvSepChar : ',', // seperation char in csv mode
			okCancelInMulti : false, // display ok cancel buttons in desktop
			// mode multiselect also.
			triggerChangeCombined : true, // im multi select mode wether to
			// trigger change event on
			// individual selection or combined
			// selection.
			selectAll : false, // to display select all button in multiselect
			// mode.|| also select all will not be available
			// on mobile devices.
			selectAlltext : '全选', // the text to display for select all.
			searchAble : true,
			width : null,
			url : null,
			sourceroot : null,
			textkey : 'text',
			valuekey : 'value',
			position : null,
			moreKey: false , 
			appendDefault : false  
		// 是否允许搜索

		}, options);

		var ret = this.each(function() {
			var selObj = this; // the original select object.
			if (this.checkselect || !$(this).is('select'))
				return; // already initialized

			this.checkselect = {
				E : $(selObj), // the jquery object of original select element.
				is_multi : $(selObj).attr('multiple'), // if its a mmultiple
				// select
				select : '',
				caption : '',
				placeholder : '',
				optDiv : '',
				CaptionCont : '',
				is_floating : false,
				is_opened : false,
				// backdrop: '',
				mob : false, // if to open device default select
				Pstate : [],
				moreKey: settings.moreKey,
				isUpdate: true,//多选顺序是否改变
				createElems : function() {
					var O = this;
					O.E.wrap('<div class="SumoSelect" tabindex="0">');
					O.select = O.E.parent();
					O.caption = $('<span></span>');
					O.CaptionCont = $('<p class="CaptionCont noneed"><label><i></i></label></p>').addClass('SlectBox').attr('style', O.E.attr('style')).prepend(O.caption);
					if (settings.width) {
						O.CaptionCont.css('width', settings.width);
					}
					O.select.append(O.CaptionCont);

					if (O.E.attr('disabled') || O.E.attr('readonly'))
						O.select.addClass('disabled').removeAttr('tabindex');

					// if output as csv and is a multiselect.
					if (settings.outputAsCSV && O.is_multi && O.E.attr('name')) {
						// create a hidden field to store csv value.
						O.select.append($('<input class="HEMANT123" type="hidden" />').attr('name', O.E.attr('name')).val(O.getSelStr()));

						// so it can not post the original select.
						O.E.removeAttr('name');
					}

					// break for mobile rendring.. if forceCustomRendering is
					// false
					if (O.isMobile() && !settings.forceCustomRendering) {
						O.setNativeMobile();
						return;
					}else{
						O.E.change(function() {
							O.updateOpt();
						});
					}

					// hide original select
					O.E.css("display","none");

					// ## Creating the list...
					O.optDiv = $('<div class="optWrapper noneed">');

					// create search input
					if (settings.searchAble) {
						sdiv = $('<div style="padding:5px;position:relative"></div>');
						sdiv.append('<i class="ts-search" style="color:#555;position:absolute;right:10px;top:12px"></i>');
						O.searcher = $('<input placeholder="${tk:lang("请输入关键字")}" type="text" style="width:100%;padding-right: 20px;" class="form-control">');
						sdiv.append(O.searcher);
						O.optDiv.append(sdiv);
						O.searcher.on('keyup input', function() {
							var v = $.trim($(this).val());
							if (v != '') {
								var fc = 0;
								O.optDiv.find('li').each(function() {
									var li = $(this);
									var label = li.find('label');
									label.find('.match').each(function() {
										var v = $(this).html();
										$(this).replaceWith(v);
									});
									var matchMorekey = false;
									if(O.moreKey){
										if(li[0].dataset){
											for(var keys in li[0].dataset){
												if(li[0].dataset[keys] && li[0].dataset[keys].toLowerCase().indexOf(v.toLowerCase()) > -1){
													matchMorekey = true;
												}
											}
										}						
									}									
									
									if (label.text().toLowerCase().indexOf(v.toLowerCase()) > -1 || matchMorekey) {
										var txt = label.find('.txt').html();
										txt = txt.replace(new RegExp("(" + v + ")", "ig"), '<font class="match" style="display:inline-block;color:red">$1</font>');
										label.find('.txt').html(txt);
										fc += 1;
										var diff = li.position().top + ul.scrollTop() - ul.height();
										if (fc == 1) {
											ul.scrollTop(diff + ul.height() / 2);
										}
										li.addClass('found');
										li.removeClass('unfound');
									} else {
										li.removeClass('found');
										li.addClass('unfound');
									}
								});
							} else {
								O.optDiv.find('li.found').removeClass('found');
								O.optDiv.find('li.unfound').removeClass('unfound');
								O.optDiv.find('.match').each(function() {
									var v = $(this).text();
									$(this).replaceWith(v);
								});
							}
						});
					}
					// branch for floating list in low res devices.
					O.floatingList();

					// Creating the markup for the available options
					ul = $('<ul class="options">');
					O.optDiv.append(ul);

					// Select all functionality
					if (settings.selectAll)
						O.selAll();

					$(O.E.children('option')).each(function(i, opt) { // parsing
						// options
						// to li
						opt = $(opt);
						O.createLi(opt);
					});

					// if multiple then add the class multiple and add OK /
					// CANCEL button
					if (O.is_multi)
						O.multiSelelect();

					O.select.append(O.optDiv);
					O.basicEvents();
					O.selAllState();
					
					//当有滚动条时阻止滚动继承
					ul.bind('mousewheel DOMMouseScroll', function(event, delta, deltaX, deltaY) {
						if($(this)[0].scrollHeight>$(this)[0].offsetHeight){
							event.stopPropagation();
						}
					});
					
				},

				// ## Creates a LI element from a given option and binds events
				// to it
				// ## Adds it to UL at a given index (Last by default)
				createLi : function(opt, i) {
					var O = this;
					if (!opt.attr('value'))
						opt.attr('value', opt.val());
					var txtspan = '<div class="txt" style="margin:0px;display:inline-block">' + opt.text() + '</div>';
					if (opt.attr('icon-url')) {
						var iconspn = '<span style="display:inline-block"><img style="margin-right:5px;width: 25px;height: 25px;white-space: nowrap;" src="' + opt.attr('icon-url') + '"></span>';
						li = $('<li title="' + opt.text() + '"><label style="margin:0px">' + iconspn + txtspan + '</label></li>');
						li.data('val', opt.val());
					} else {
						li = $('<li title="' + opt.text() + '"><label style="margin:0px">' + txtspan + '</label></li>');
						li.data('val', opt.val());
					}
					if (O.is_multi)
						li.prepend('<span><i></i></span>');

					if (opt[0].disabled || opt[0].readonly)
						li = li.addClass('disabled');

					O.onOptClick(li);

					if (opt[0].selected){
						li.addClass('selected');
					}

					if (opt.attr('class')){
						li.addClass(opt.attr('class'));
					}
					// exclude the null dataset value
					if(O.moreKey){
						if(opt[0].dataset){
							for(var keys in opt[0].dataset){
								if(opt[0].dataset[keys]){
									li.data(keys,opt[0].dataset[keys]);
									li.attr('data-'+keys,opt[0].dataset[keys]);									
								}
							}
						}						
					}
					ul = O.optDiv.children('ul.options');
					if (typeof i == "undefined")
						ul.append(li);
					else
						ul.children('li').eq(i).before(li);

					return li;
				},

				// ## Returns the selected items as string in a Multiselect.
				getSelStr : function() {
					// get the pre selected items.
					sopt = [];
					this.E.children('option:selected').each(function() {
						sopt.push($(this).val());
					});
					return sopt.join(settings.csvSepChar);
				},

				// ## THOSE OK/CANCEL BUTTONS ON MULTIPLE SELECT.
				multiSelelect : function() {
					var O = this;
					O.optDiv.addClass('multiple');
					O.okbtn = $('<p class="btnOk">确定</p>').click(function() {

						// if combined change event is set.
						if (settings.triggerChangeCombined) {

							// check for a change in the selection.
							changed = false;
							if (O.E.children('option:selected').length != O.Pstate.length) {
								changed = true;
							} else {
								O.E.children('option:selected').each(function() {
									if (O.Pstate.indexOf($(this).val()) < 0)
										changed = true;
								});
							}

							if (changed) {
								O.E.trigger('change').trigger('click');
							}
						}
						O.hideOpts();
					});
					O.cancelBtn = $('<p class="btnCancel">取消</p>').click(function() {
						O._cnbtn();
						O.hideOpts();
					});
					O.optDiv.append($('<div class="MultiControls">').append(O.okbtn).append(O.cancelBtn));
				},

				_cnbtn : function() {
					var O = this;
					// remove all selections
					O.E.children('option:selected').each(function() {
						this.selected = false;
					});
					O.optDiv.find('li.selected').removeClass('selected')

					// restore selections from saved state.
					for (i = 0; i < O.Pstate.length; i++) {
						// O.E.children('option[value="' + O.Pstate[i] +
						// '"]')[0].selected = true;
						O.E.children('option').each(function() {
							if ($(this).val() == O.Pstate[i]) {
								$(this).attr('selected', true);
							}
						});
						O.optDiv.find('li').each(function() {
							if ($(this).data('val') == O.Pstate[i]) {
								$(this).addClass('selected');
							}
						});
					}
					O.selAllState();
				},

				selAll : function() {
					var O = this;
					if (!O.is_multi)
						return;
					O.chkAll = $('<i>');
					O.selAll = $('<p class="select-all"><label>' + settings.selectAlltext + '</label></p>').prepend($('<span></span>').append(O.chkAll));
					O.chkAll.on('click', function() {
						// O.toggSelAll(!);
						O.selAll.toggleClass('selected');
						O.optDiv.find('ul.options li').each(function(ix, e) {
							e = $(e);
							if (O.selAll.hasClass('selected')) {
								if (!e.hasClass('selected'))
									e.trigger('click');
							} else if (e.hasClass('selected'))
								e.trigger('click');
						});
					});

					O.optDiv.prepend(O.selAll);
				},

				selAllState : function() {
					var O = this;
					if (settings.selectAll) {
						var sc = 0, vc = 0;
						O.optDiv.find('ul.options li').each(function(ix, e) {
							if ($(e).hasClass('selected'))
								sc++;
							if (!$(e).hasClass('disabled'))
								vc++;
						});
						// select all checkbox state change.
						if (sc == vc)
							O.selAll.removeClass('partial').addClass('selected');
						else if (sc == 0)
							O.selAll.removeClass('selected partial');
						else
							O.selAll.addClass('partial')// .removeClass('selected');
					}
				},

				showOpts : function() {
					var O = this;			
					if (O.E.attr('disabled')||O.E.attr('readonly'))
						return; // if select is disabled then retrun
					O.is_opened = true;
					// O.backdrop.show();
					
					//多选选中的提到前面
					var $ul = O.optDiv.find('ul.options');	
					if(O.is_multi && O.isUpdate) {
						O.E.children('option').each(function() {
							if(typeof($(this).attr("selected"))!="undefined") {
								$(this).prependTo(O.E);
							}
						});
						O.optDiv.find('ul.options li').each(function() {
							if($(this).hasClass('selected')) {
								$(this).prependTo($ul);
							}
						});
						O.isUpdate = false;
					}
					
					O.optDiv.addClass('open');
					if(settings.position){
						O.optDiv.addClass('open-'+settings.position);
					}
					if(O.select.offsetParent().hasClass('tsscroll-container')){
						if(O.optDiv.offset().top+O.optDiv.height()+10 > O.select.offsetParent().offset().top+O.select.offsetParent().height()){
							O.select.offsetParent().height(O.optDiv.offset().top+O.optDiv.height()+10-O.select.offsetParent().offset().top);
						}else{
							O.select.offsetParent().height('');
						}						
					}
					
					
					// hide options on click outside.
					$(document).on('click.checkselect', function(e) {
						if (!O.select.is(e.target) // if the target of the
								// click isn't the
								// container...
								&& O.select.has(e.target).length === 0) { // ...
							// nor
							// a
							// descendant
							// of
							// the
							// container
							// if (O.is_multi && settings.okCancelInMulti)
							// O._cnbtn();
							// O.hideOpts();
							if (!O.is_opened)
								return;
							O.hideOpts();
							if (O.is_multi && settings.okCancelInMulti)
								O._cnbtn();
						}
						
					});

					if (O.is_floating) {
						H = O.optDiv.children('ul').outerHeight() + 2; // +2 is
						// clear
						// fix
						if (O.is_multi)
							H = H + parseInt(O.optDiv.css('padding-bottom'));
						O.optDiv.css('height', H);
					}

					// maintain state when ok/cancel buttons are available.
					if (O.is_multi && (O.is_floating || settings.okCancelInMulti)) {
						O.Pstate = [];
						O.E.children('option:selected').each(function() {
							O.Pstate.push($(this).val());
						});
					}
				},
				hideOpts : function() {
					var O = this;
					O.is_opened = false;
					O.optDiv.find('ul').removeClass('focus');
					O.optDiv.find('li.active-class').removeClass('active-class');
					O.optDiv.removeClass('open').find('ul li.sel').removeClass('sel');
					O.optDiv.find('li.found').removeClass('found');
					O.optDiv.find('li.unfound').removeClass('unfound');
					O.optDiv.find('.match').each(function() {
						var v = $(this).text();
						$(this).replaceWith(v);
					});
					if (O.searcher) {
						O.searcher.val('');
					}
					if(O.select.offsetParent().hasClass('tsscroll-container')){
						if(O.optDiv.offset().top+O.optDiv.height()+10 > O.select.offsetParent().offset().top+O.select.offsetParent().height()){
							O.select.offsetParent().height(O.optDiv.offset().top+O.optDiv.height()+10-O.select.offsetParent().offset().top);
						}else{
							O.select.offsetParent().height('');
						}						
					}
					$(document).off('click.checkselect');
				},
				setOnOpen : function() {
					var O = this;
					var li = O.optDiv.find('ul li').eq(O.E[0].selectedIndex!=-1?O.E[0].selectedIndex:0);
					li.addClass('sel');
					O.showOpts();
				},
				nav : function(up) {
					var O = this, c;
					var sel = O.optDiv.find('ul li.sel');
					!O.optDiv.find('ul').hasClass('focus')?O.optDiv.find('ul').addClass('focus'):'';
					if (O.is_opened && sel.length) {
						if (up)
							c = sel.prevAll('li:not(.disabled):not([class*="unfound"])');
						else
							c = sel.nextAll('li:not(.disabled):not([class*="unfound"])');
						if (!c.length)
							return;
						sel.removeClass('sel');
						sel = c.first().addClass('sel');
					}else{
						O.setOnOpen();
						sel = O.optDiv.find('ul li.sel');
					}	
					// setting sel item to visible view.
					var ul = O.optDiv.find('ul');
					ul.scrollTop(sel.position().top+ul.scrollTop()-ul.outerHeight()/2);
				},

				basicEvents : function() {
					var O = this;
					O.CaptionCont.click(function(evt) {
						O.E.trigger('click');
						if (O.is_opened){
							O.hideOpts();
						}else{
							O.showOpts();
						}
						evt.stopPropagation();
					});

					/*
					 * O.select.on('blur focusout', function () {
					 * if(!O.is_opened)return; //O.hideOpts(); O.hideOpts();
					 * 
					 * if (O.is_multi && settings.okCancelInMulti) O._cnbtn(); })
					 */
					O.select.on('keydown', function(e) {
						switch (e.which) {
						case 38: // up
							O.nav(true);
							break;

						case 40: // down
							O.nav(false);
							break;

						case 32: // space
						case 13: // enter
							if (O.is_opened)
								O.optDiv.find('ul li.sel').trigger('click');
							else
								O.setOnOpen();
							break;
						case 9: // tab
						case 27: // esc
							if (O.is_multi && settings.okCancelInMulti)
								O._cnbtn();
							O.hideOpts();
							return;

						default:
							return; // exit this handler for other keys
						}
						e.preventDefault(); // prevent the default action
						// (scroll / move caret)
					});

					$(window).on('resize.checkselect', function() {
						O.floatingList();
					});
				},

				onOptClick : function(li) {
					var O = this;
					li.click(function() {
						O.isUpdate = true;
						var li = $(this);
						if (li.hasClass('disabled'))
							return;
						txt = "";
						if (O.is_multi) {
							li.toggleClass('selected');
							// O.E.children('option[value="' + li.data('val') +
							// '"]')[0].selected = li.hasClass('selected');
							O.E.children('option').each(function() {
								if ($(this).val() == li.data('val')) {
									$(this).attr('selected', li.hasClass('selected'));
									$(this).prop('selected', li.hasClass('selected'));
								}
							});
							O.selAllState();
						} else {
						//单选模式下优化样式和重新赋值原生select值 20181224
							li.addClass('selected').siblings().removeClass('selected');
							O.E.val(li.data('val')); // set the value of
							O.E.children('option').each(function() {
								if ($(this).val() == li.data('val')) {
									$(this).attr('selected','selected');
									$(this).selected = li.hasClass('selected');
								}else{
									$(this).removeAttr('selected');
								}
							});
							// select element
						}
						// branch for combined change event.
						if (!(O.is_multi && settings.triggerChangeCombined && (O.is_floating || settings.okCancelInMulti))) {
							O.E.trigger('change').trigger('click');
						}

						if (!O.is_multi){
							O.hideOpts(); // if its not a multiselect then
						}
							
						// hide on single select.
							
					});
				},
				updateOpt:function(){
					var O =this;
					O.setText();
					$(O.E.children('option')).each(function(i, opt) { // parsing
						if($(this).prop('selected')){
							O.optDiv.find('ul.options li').eq(i).addClass('selected');
						}else{
							O.optDiv.find('ul.options li').eq(i).removeClass('selected');
						}
					});
				},
				setText : function() {
					var O = this;
					O.placeholder = "";
					if (O.is_multi) {
						sels = O.E.children(':selected').not(':disabled'); // selected
						// options.
                       
						for (i = 0; i < sels.length; i++) {
							if (settings.csvDispCount && i >= settings.csvDispCount ) {
								O.placeholder = settings.captionFormat.replace('{0}', sels.length);
								// O.placeholder = i + '+ Selected';
								break;
							} else{
								O.placeholder += $(sels[i]).text() + ", ";
							}	
						}
						O.placeholder = O.placeholder.replace(/,([^,]*)$/, '$1'); // remove
						// unexpected
						// ","
						// from
						// last.
						//当所有的选项(不包括disabled选项)都被选中，而且个数大于csvDispCount时显示selectedAllTitle名称（比如"全部告警"）
						if(settings.selectedAllTitle && settings.csvDispCount){
							var optionLength=O.optDiv.find('ul.options li').not(':disabled').length;
							if(sels.length>settings.csvDispCount && optionLength==sels.length){
								O.placeholder = settings.selectedAllTitle; 
							}	
						}
					} else {
						if(O.E.children(':selected').length>0){
							O.placeholder = O.E.children(':selected').not(':disabled').text();
						}else{
							O.placeholder = O.E.attr('placeholder');
						}
					}

					is_placeholder = false;

					if (!O.placeholder) {

						is_placeholder = true;

						O.placeholder = O.E.attr('placeholder');
						if (!O.placeholder) // if placeholder is there then set
						// it
						{
							O.placeholder = O.E.children('option:disabled:selected').text();
							// if (!O.placeholder && settings.placeholder ===
							// 'Select Here')
							// O.placeholder = O.E.val();
						}
					}

					O.placeholder = O.placeholder ? O.placeholder : settings.placeholder

					// set display text
					O.caption.text(O.placeholder).attr('title',O.placeholder);

					
					
					// set the hidden field if post as csv is true.
					csvField = O.select.find('input.HEMANT123');
					if (csvField.length)
						csvField.val(O.getSelStr());

					// add class placeholder if its a placeholder text.
					if (is_placeholder)
						O.caption.addClass('placeholder');
					else
						O.caption.removeClass('placeholder');
					return O.placeholder;
				},

				isMobile : function() {

					// Adapted from http://www.detectmobilebrowsers.com
					var ua = navigator.userAgent || navigator.vendor || window.opera;

					// Checks for iOs, Android, Blackberry, Opera Mini, and
					// Windows mobile devices
					for (var i = 0; i < settings.nativeOnDevice.length; i++)
						if (ua.toString().toLowerCase().indexOf(settings.nativeOnDevice[i].toLowerCase()) > 0)
							return settings.nativeOnDevice[i];
					return false;
				},

				setNativeMobile : function() {
					var O = this;
					O.E.addClass('SelectClass')// .css('height',
					// O.select.outerHeight());
					O.mob = true;
					O.E.change(function() {
						O.setText();
					});
				},

				floatingList : function() {
					var O = this;
					// called on init and also on resize.
					// O.is_floating = true if window width is < specified float
					// width
					O.is_floating = $(window).width() <= settings.floatWidth;

					// set class isFloating
					O.optDiv.toggleClass('isFloating', O.is_floating);

					// remove height if not floating
					if (!O.is_floating)
						O.optDiv.css('height', '');

					// toggle class according to okCancelInMulti flag only when
					// it is not floating
					O.optDiv.toggleClass('okCancelInMulti', settings.okCancelInMulti && !O.is_floating);
				},

				// HELPERS FOR OUTSIDERS
				// validates range of given item operations
				vRange : function(i) {
					var O = this;
					opts = O.E.children('option');
					if (opts.length <= i || i < 0)
						throw "index out of bounds"
					return O;
				},

				// toggles selection on c as boolean.
				toggSel : function(c, i) {
					var O = this.vRange(i);
					if (O.E.children('option')[i].disabled)
						return;
					O.E.children('option')[i].selected = c;
					if (!O.mob)
						O.optDiv.find('ul.options li').eq(i).toggleClass('selected', c);
					O.setText();
				},

				// toggles disabled on c as boolean.
				toggDis : function(c, i) {
					var O = this.vRange(i);
					O.E.children('option')[i].disabled = c;
					if (c)
						O.E.children('option')[i].selected = false;
					if (!O.mob)
						O.optDiv.find('ul.options li').eq(i).toggleClass('disabled', c).removeClass('selected');
					O.setText();
				},

				// toggle disable/enable on complete select control
				toggSumo : function(val) {
					var O = this;
					O.enabled = val;
					O.select.toggleClass('disabled', val);

					if (val) {
						O.E.attr('disabled', 'disabled');
						O.select.removeAttr('tabindex');
					} else {
						O.E.removeAttr('disabled');
						O.select.attr('tabindex', '0');
					}

					return O;
				},

				// toggles alloption on c as boolean.
				toggSelAll : function(c) {
					var O = this;
					O.E.find('option').each(function(ix, el) {
						if (O.E.find('option')[$(this).index()].disabled)
							return;
						O.E.find('option')[$(this).index()].selected = c;
						if (!O.mob)
							O.optDiv.find('ul.options li').eq($(this).index()).toggleClass('selected', c);
						O.setText();
					});
					if (!O.mob && settings.selectAll)
						O.selAll.removeClass('partial').toggleClass('selected', c);
				},

				/*
				 * outside accessibility options which can be accessed from the
				 * element instance.
				 */
				reload : function() {
					var elm = this.unload();
					return $(elm).checkselect(settings);
				},

				unload : function() {
					var O = this;
					if (!O.select) {
						O.select = O.E.parent('.SumoSelect');
					}
					O.select.before(O.E);
					O.E.css("display","block");

					if (settings.outputAsCSV && O.is_multi && O.select.find('input.HEMANT123').length) {
						O.E.attr('name', O.select.find('input.HEMANT123').attr('name')); // restore
						// the
						// name;
					}
					O.select.remove();
					$(selObj).attr('data-init', null);
					delete selObj.checkselect;
					return selObj;
				},

				// ## add a new option to select at a given index.
				add : function(val, txt, i) {
					if (typeof val == "undefined")
						throw "No value to add"

					var O = this;
					opts = O.E.children('option')
					if (typeof txt == "number") {
						i = txt;
						txt = val;
					}
					if (typeof txt == "undefined") {
						txt = val;
					}

					opt = $("<option></option>").val(val).html(txt);

					if (opts.length < i)
						throw "index out of bounds"

					if (typeof i == "undefined" || opts.length == i) { // add
						// it to
						// the
						// last
						// if
						// given
						// index
						// is
						// last
						// no or
						// no
						// index
						// provides.
						O.E.append(opt);
						if (!O.mob)
							O.createLi(opt);
					} else {
						opts.eq(i).before(opt);
						if (!O.mob)
							O.createLi(opt, i);
					}

					return selObj;
				},

				// ## removes an item at a given index.
				remove : function(i) {
					var O = this.vRange(i);
					O.E.children('option').eq(i).remove();
					if (!O.mob)
						O.optDiv.find('ul.options li').eq(i).remove();
					O.setText();
				},

				// ## Select an item at a given index.
				selectItem : function(i) {
					this.toggSel(true, i);
				},

				// ## UnSelect an iten at a given index.
				unSelectItem : function(i) {
					this.toggSel(false, i);
				},

				// ## Select all items of the select.
				selectAll : function() {
					this.toggSelAll(true);
				},

				// ## UnSelect all items of the select.
				unSelectAll : function() {
					this.toggSelAll(false);
				},

				// ## Disable an iten at a given index.
				disableItem : function(i) {
					this.toggDis(true, i)
				},

				// ## Removes disabled an iten at a given index.
				enableItem : function(i) {
					this.toggDis(false, i)
				},

				// ## New simple methods as getter and setter are not working
				// fine in ie8-
				// ## variable to check state of control if enabled or disabled.
				enabled : true,
				// ## Enables the control
				enable : function() {
					return this.toggSumo(false)
				},

				// ## Disables the control
				disable : function() {
					return this.toggSumo(true)
				},

				init : function() {
					var O = this;
					O.createElems();
					O.setText();
					return O
				}

			};
			if ($(selObj).attr('data-init')) {
				selObj.checkselect.reload();
			} else {
				if (settings.url) {
					$(selObj).attr('data-url', settings.url);
					$(selObj).attr('data-sourceroot', settings.sourceroot ? settings.sourceroot : '');
					$(selObj).attr('data-textkey', settings.textkey ? settings.textkey : 'text');
					$(selObj).attr('data-valuekey', settings.valuekey ? settings.valuekey : 'value');
					$(selObj).checkselectInit($(selObj));
				}
				selObj.checkselect.init();
				$(selObj).attr('data-init', 'true');
			}
		});

		return ret.length == 1 ? ret[0] : ret;
	}, $.fn.checkselectInit = function(item) {
		if (item.data('url') && !item.attr('data-checkselect-init')) {
			var textkey = item.data('textkey') || 'text';
			var valuekey = item.data('valuekey') || 'value';
			var defaultValue = new Array();
			if(item.data('value')&&item.data('value')!=""){
				if(item.data('value').indexOf('.') > -1){
					defaultValue = item.data('value').split(',');
				}else{
					defaultValue = item.data('value') ; 
				}
			}
			textkey = textkey.toLowerCase();
			valuekey = valuekey.toLowerCase();
			var ismultiple = $(item).attr('multiple') ? true : false;
			
			/*
			 当声明不清理对象的option时，追加option
			 默认初始化，先清掉option
			*/
			var appendDefault = $(item).data('appenddefault');
			appendDefault = appendDefault ? appendDefault : false; 
			var tmpOptArray = new Array();
			if(!appendDefault){
				$(item).find('option').remove();
			}else{
				$(item).find('option').each(function(){
					var tmpObj = {};
					tmpObj.text = $(this).text();
					tmpObj.value = $(this).attr('value');
					tmpOptArray.push(tmpObj);
				});
				$(item).find('option').remove();
			}
			
			$.ajax({
				url : item.data('url'),
				async : false,
				contentType : 'application/json',
				dataType : 'json',
				success : function(rs) {
					if (item.data("sourceroot")) {
						var newRs = [];
						var sr = item.data("sourceroot").split('.');
						for (var ri = 0; ri < sr.length; ri++) {
							if (sr[ri]) {
								newRs = rs[sr[ri]];
							}
						}
						if (!ismultiple) {
							$(item).append($('<option value="">${tk:lang("请选择")}...</option>'));
						}
						$.each(newRs, function(index, optionData) {
							var option = $('<option></option>');
							for ( var i in optionData) {
								if (i.toLowerCase() == textkey) {
									option.text(optionData[i]);
								} else if (i.toLowerCase() == valuekey) {
									option.val(optionData[i]);
									if(defaultValue!=null && defaultValue.length > 0){
										for(var j=0; j<defaultValue.length;j++){
											if(optionData[i] == defaultValue[j]){
												option.attr("selected", "selected");
												break;
											}
										}
									}
								} else if (i.toLowerCase() == 'isselected') {
									if (optionData[i] || optionData[i] == 1) {
										option.attr("selected", "selected");
									}
								} else {
									option.attr(i, optionData[i]);
								}
							}
							$(item).append(option);
						});
					} else {
						if (!ismultiple) {
							$(item).append($('<option value="">${tk:lang("请选择")}...</option>'));
						}
						$.each(rs, function(index, optionData) {
							var option = $('<option></option>');
							for ( var i in optionData) {
								if (i.toLowerCase() == textkey) {
									option.text(optionData[i]);
								} else if (i.toLowerCase() == valuekey) {
									option.val(optionData[i]);
									if(defaultValue!=null && defaultValue.length > 0){
										for(var j=0; j<defaultValue.length;j++){
											if(optionData[i] == defaultValue[j]){
												option.attr("selected", "selected");
												break;
											}
										}
									}
								} else if (i.toLowerCase() == 'isselected') {
									if (optionData[i] || optionData[i] == 1) {
										option.attr("selected", "selected");
									}
								} else {
									option.attr(i, optionData[i]);
								}
							}
							$(item).append(option);
						});
					}
					//补上默认参数
					if(appendDefault){
						$.each(tmpOptArray , function(i , k ){
							$(item).append($('<option value="'+ k.value+'">'+ k.text +'</option>'));
						});
					}
					// 新增参数防重复加载
					item.attr('data-checkselect-init', true);
				},
				error : function(data) {
					console.error(data);
				},
				type : 'GET'
			});
		}
	}

	$(function() {
		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('select[plugin-checkselect]').each(function() {
				var item = $(this);
				pluginCheckselect(item);
			});
		});

		$('select[plugin-checkselect]').each(function() {
			var item = $(this);
			pluginCheckselect(item);
		});

		$(document).bind('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('select[plugin-checkselect]').each(function() {
				var item = $(this);
				pluginCheckselect(item);
			});
		});
	});
	function pluginCheckselect(item){
		var searchable = item.data('searchable');
		var json = {};
		if (typeof searchable != 'undefined') {
			json.searchAble = searchable;
		}
		if (item.data('width')) {
			json.width = item.data('width');
		}
		if (item.data('position')) {
			json.position = item.data('position');
		}
		if (item.data('placeholder')) {
			json.placeholder = item.data('placeholder');
		}
		if(item.hasClass('hasMorekey')){
			json.moreKey = true;
		}
		if(item.data('csvdispcount')){
			json.csvDispCount = parseInt(item.data('csvdispcount'));
		}
		if(item.data('selectedalltitle')){
			json.selectedAllTitle =item.data('selectedalltitle');
		}
		if (!item.data('bind')) {
			item.checkselectInit(item);
			item.checkselect(json);
			item.data('bind', true);
			item.attr('data-init', true);
		}		
	}
}(jQuery));
