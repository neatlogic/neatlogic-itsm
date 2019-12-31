(function($) {
	$.fn.uploader = function(options) {
		var config = $.extend(true, {}, $.fn.uploader.defaultopts, options);
		var $target = $(this);
		var fileList = new Array();
		if (!$target.data('bind') && !$target.attr('bind')) {
			$target.addClass('uploader-main');
			var $dropbox = $('<div class="uploader-dropbox"></div>');
			var $icon = $('<i class="ts-upload uploader-uploadicon"></i>');
			var $file = $('<input' + (config.allowMult ? ' multiple ' : ' ') + ' type="file"' + (config.isKeepValue && config.paramName ? ' name="' + config.paramName + '"' : '') + ' style="display:none" '
					+ (config.allowFolder ? 'webkitdirectory' : '') + ' >');
			var $span = $('<div class="uploader-help">拖动或点击上传</div>');
			var $filetype = $('<span class="uploader-filetype"></span>');
			// var $previewlist = $('<ul class="uploader-previewlist"></ul>')
			$dropbox.append($icon).append($span);

			getConfigName();

			$target.append($dropbox).append($file);
			// $target.append($previewlist);

			$dropbox.on('dragover', function(e) {
				e.preventDefault();
				e.stopPropagation();
			});

			$dropbox.on('mouseenter', function(e) {
				e.preventDefault();
				e.stopPropagation();
			});

			$dropbox.on('mouseleave', function(e) {
				e.preventDefault();
				e.stopPropagation();
			});

			$dropbox.on('dragenter', function(e) {
				e.preventDefault();
				e.stopPropagation();
			});
			$dropbox.on('drop', function(e) {
				e.preventDefault();
				e.stopPropagation();
				if (e.originalEvent.dataTransfer) {
					if (e.originalEvent.dataTransfer.files.length > 0) {
						for (var i = 0; i < e.originalEvent.dataTransfer.files.length; i++) {
							if (e.originalEvent.dataTransfer.files[i].type || e.originalEvent.dataTransfer.files[i].name.indexOf('.') > 0) {
								upload(e.originalEvent.dataTransfer.files[i]);
							}
						}
						if (!config.isKeepValue) {
							filecontrol.val('');
						}
					}
				}
			});
			$dropbox.on('click', function() {
				$file.click();
			});
			$file.on('change', function() {
				var filecontrol = $(this);
				if (filecontrol[0].files.length > 0) {
					for (var i = 0; i < filecontrol[0].files.length; i++) {
						upload(filecontrol[0].files[i]);
					}
				}
				if (!config.isKeepValue) {
					filecontrol.val('');
				}
			});

			function upload(f) {
				var file = f;
				if (config.allowed == 'image') {
					if (!/image\/\w+/.test(file.type)) {
						return false;
					}
				} else if (config.allowed == 'file') {
					if (/image\/\w+/.test(file.type)) {
						return false;
					}
				}
				if (config.fnBeforeSubmit) {
					var t = config.fnBeforeSubmit();
					if (t == false) {
						return false;
					}
				}

				var $previewbox = $('<div class="uploader-previewbox"></div>');
				var $processbar = $('<div class="progress uploader-process" style="border-radius:0px"><div>');
				var $process = $(' <div class="progress-bar progress-bar-success" style="border-radius:0px;"><div class="progress-filename" title="' + file.name + '">' + file.name
						+ '</div><div class="progress-state"><div class="inner-state"></div></div><span class="state-text">上传中</span></div>');
				var $filename = $('<div class="uploader-filename" title="' + file.name + '"></div>');
				var $cancel = $('<i class="ts-remove uploader-cancel"></i>');
				var $download = $('<i class="ts-m-import uploader-download"></i>');
				var $remove = $('<i class="ts-remove uploader-remove"></i>');
				var $size = $('<span class="uploader-size"></span>');
				// var $filetype = $('<span class="uploader-filetype"></span>');
				var $fileicon = null;
				if (/image\/\w+/.test(file.type) && window.FileReader) {
					var fr = new FileReader();
					$fileicon = $('<div class="uploader-imagediv"><img class="uploader-image"></div>');
					fr.onloadend = function(e) {
						$fileicon.find('img').attr('src', e.target.result);
					};
					fr.readAsDataURL(file);
				} else {
					$fileicon = $('<span class="ts-file uploader-fileicon"></span>');
				}
				$processbar.append($process);
				$previewbox.append($processbar);
				$previewbox.append($filename);
				$previewbox.append($fileicon);
				if (config.downloadUrl) {
					$previewbox.append($download);
				}
				if (config.showRemove) {
					$previewbox.append($remove);
				}

				$previewbox.append($size);
				// $previewbox.append($filetype);

				$target.prepend($previewbox);
				if (!config.allowMult) {
					$dropbox.hide();
				}
				$filename.text(file.name);
				if (config.upload && config.url && config.paramName) {
					var fd = new FormData();
					fd.append(config.paramName, file);
					if (config.param) {
						var pm = null;
						if (typeof config.param == 'function') {
							pm = config.param();
						} else if (typeof config.param == 'object') {
							pm = config.param;
						} else {
							pm = {};
						}
						for ( var k in pm) {
							fd.append(k, pm[k]);
						}
					}
					$.ajax({
						// beforeSend: function(request) {
						// request.setRequestHeader("Pragma", "no-cache");
						// request.setRequestHeader("Cache-Control",
						// "no-cache");
						// },
						type : 'post',
						url : config.url + '?belong=' + config.belong + '&filetype=' + file.type + '&paramName=' + config.paramName,
						data : fd,
						enctype : 'multipart/form-data',
						dataType : 'json',
						processData : false,
						contentType : false,
						async : true,
						xhr : function() {
							var myXhr = $.ajaxSettings.xhr();
							$process.append($cancel);
							$cancel.on('click', function() {
								myXhr.abort();
								$(this).closest('.uploader-previewbox').remove();
								if (!config.allowMult) {
									$dropbox.show();
								}
							});
							if (myXhr.upload) {
								myXhr.upload.addEventListener('progress', function(ev) {
									if (ev.lengthComputable) {
										var p = (ev.loaded / ev.total * 100).toFixed(0);
										$process.find(".inner-state").css('width', p + "%");
									}
								}, false);
							}
							return myXhr;
						}
					}).done(function(data) {
						$cancel.remove();
						if (data.state == 'SUCCESS' || data.Status == 'OK') {
							$process.html('<div class="ts-check state-success"></div>');
							$previewbox.append('<input type="hidden" name="' + config.fileIdName + '" value="' + data.fileid + '">');
							if (config.needFileName) {
								$previewbox.append('<input type="hidden" name="' + config.fileNameName + '"  value="' + data.name + '">');
							}
							$size.text(data.size);
							// $filetype.text(data.typename);
							config.isUploaded = true;
							fileList.push(data);
						} else {
							$previewbox.tooltip({
								html : true,
								placement : "auto",
								title : data.state,
								trigger : 'hover'
							});
							if (config.showRemove) {
								$size.html('<span style="color:red">上传失败</span>');
							} else {
								$size.html('<span style="color:red">上传失败</span><i class="ts-remove btn-delerror"></i>');
								$size.find('.btn-delerror').on('click', function() {
									var c = $(this).closest('.uploader-previewbox');
									c.tooltip('destroy');
									c.remove();
								});
							}
						}
						$processbar.delay(1000).fadeOut();

						$download.on('click', function() {
							var url = config.downloadUrl;
							if (url) {
								if (config.downloadUrl.indexOf('?') > -1) {
									url = url + '&fileid=' + data.fileid
								} else {
									url = url + '?fileid=' + data.fileid
								}
								window.location.href = url;
							}
						});

						if (config.showRemove) {
							$remove.on('click', function(e) {
								e.stopPropagation();
								if (config.fnBeforeRemoved) {
									var r = config.fnBeforeRemoved(data.fileid);
									if (!r) {
										return;
									}
								}
								$file.val('');
								var c = $(this).closest('.uploader-previewbox');
								c.tooltip('destroy');
								c.remove();
								if (config.fnAfterRemoved) {
									config.fnAfterRemoved(data.fileid);
								}
								if (!config.allowMult) {
									$dropbox.show();
								}
								for (var f = 0; f < fileList.length; f++) {
									if (fileList[f].fileid == data.fileid) {
										fileList.splice(f, 1);
									}
								}
							});
						}

						$previewbox.on('mouseenter', function() {
							$remove.show();
							if (config.downloadUrl && config.isUploaded) {
								$download.show();
							}
						});

						$previewbox.on('mouseleave', function() {
							$remove.hide();
							$download.hide();
						});

						if (config.fnAfterSubmit)
							config.fnAfterSubmit(data);
					});
				}
			}

			this.setBelong = function(_belong) {
				config.belong = _belong;
			}

			this.getUploadedCount = function() {
				return $target.find('input[name=' + config.fileIdName + ']').length;
			};

			this.getUploaded = function() {
				return fileList;
			};

			function getConfigName() {
				this.filetype = '', $.getJSON("/balantflow/file/getFileTypeByBelong.do?belong=" + config.belong, function(data) {
					if (data.Status == "OK") {
						this.filetype = data.name;
					}
					$filetype.html(this.filetype);
					$dropbox.append($filetype);
				})
			}
			;

			this.addUploaded = function(file) {
				var $previewbox = $('<div class="uploader-previewbox"></div>');
				var $filename = $('<div class="uploader-filename" title="' + file.name + '">' + file.name + '</div>');
				var $download = $('<i class="ts-m-import uploader-download" title="下载" data-fileid="' + (file.fileid || '') + '"></i>');
				var $remove = $('<i class="ts-remove uploader-remove" title="删除"></i>');
				var $size = $('<span class="uploader-size">' + file.size + '</span>');
				// 如果取消注释，很多页面都需要修改
				// $previewbox.append('<input type="hidden" name="' +
				// config.fileIdName + '" value="' + file.fileid + '">');
				var $fileicon = null;
				if (file.type && (file.type == 'image' || /image\/\w+/.test(file.type))) {
					$fileicon = $('<div class="uploader-imagediv"><img class="uploader-image" src="/balantflow/file/getFile.do?fileid=' + file.fileid + '"></div>');
				} else {
					$fileicon = $('<span class="ts-file uploader-fileicon uploader-fileicon-success"></span>');
				}
				$previewbox.append($filename);
				$previewbox.append($fileicon);
				if (config.downloadUrl) {
					$previewbox.append($download);
				}
				$previewbox.append($remove);
				$previewbox.append($size);
				if (file.name.lastIndexOf(".") > -1) {
					file.extension = file.name.substring(file.name.lastIndexOf(".") + 1);
				}
				fileList.push(file);
				$download.on('click', function() {
					if ($(this).data('fileid')) {
						var url = config.downloadUrl;
						if (url) {
							if (config.downloadUrl.indexOf('?') > -1) {
								url = url + '&fileid=' + $(this).data('fileid');
							} else {
								url = url + '?fileid=' + $(this).data('fileid')
							}
							window.location.href = url;
						}
					}
				});

				$remove.on('click', function(e) {
					e.stopPropagation();
					var c = $(this).closest('.uploader-previewbox');
					c.tooltip('destroy');
					c.remove();
					if (config.fnAfterRemoved) {
						config.fnAfterRemoved(file.fileid);
					}
					for (var f = 0; f < fileList.length; f++) {
						if (fileList[f].fileid == file.fileid) {
							fileList.splice(f, 1);
						}
					}
					$dropbox.show();
				});

				$previewbox.on('mouseenter', function() {
					$remove.show();
					if (config.downloadUrl) {
						$download.show();
					}
				});

				$previewbox.on('mouseleave', function() {
					$remove.hide();
					if (config.downloadUrl) {
						$download.hide();
					}
				});
				$target.prepend($previewbox);
				if (!config.allowMult) {
					$dropbox.hide();
				}
			};

			$target.data('bind', true);
			$target.attr('bind', true);
		}
		return this;
	};

	$.fn.uploader.defaultopts = {
		paramName : 'upfile',
		belong : 'FLOW',
		fileIdName : 'fileId',
		fileNameName : 'fileName',
		dropzoneText : '拖动文件到此或点击上传',
		url : '/balantflow/file/uploadFile.do',
		upload : true,
		allowed : 'all',
		showRemove : true,
		fnAfterRemoved : null,
		fnBeforeRemoved : null,
		fnBeforeSubmit : null,
		isKeepValue : false,
		param : null,
		downloadUrl : null,
		fnAfterSubmit : null,
		isUploaded : false,
		needFileName : true,
		allowMult : true,
		allowFolder : false
	};

})(jQuery);