;
(function($) {
	var dragTable = function($target, option) {
		// 定义变量
		var that = this;
		that.O = $.extend(true, {}, $.fn.initTable.defaultopts, option); // 接受配置参数并设定默认值
		that.$target = $target;
		that.targetEl = null;// 点击拖动元素值
		that.$cloneTarget = null; // 鼠标拖动的元素
		that.$parent = that.$target.find(that.O.moveParentEl);
		that.mouseDown = false; // 是否有元素被点击
		that.mouseMove = false; // 鼠标是否拖动元素移动
		that.to = null;// 最终得到的排序值
		that.downTime = null;// 鼠标暗下的时间
		that.timeEnd = null; // 记录鼠标按下的时间

		// 初始化数据
		that.$parent.on("mousedown", that.O.moveEl, function(e) {
			that.targetEl = this, that.mouseDown = true;
			that.downTime = new Date();
			return false;
		}).on("mouseenter", that.O.moveEl, function(e) {
			that.timeEnd = new Date();
			if (that.mouseDown && that.timeEnd - that.downTime > 300) {
				// var mouseTop = e.pageX || e.offsetX;
				var mouseTop = e.clientY;
				var eTop = $(this).offset().top;
				if (mouseTop > eTop + 3 && that.targetEl != this) { // 从下面进去元素
					that.to = $(this).data("from");
					$(this).before(that.targetEl);
				} else if (that.targetEl != this) { // 从上面进入元素
					that.to = $(this).data("from");
					$(this).after(that.targetEl);
				}
			}
		}).on("mousemove", that.O.moveEl, function(e) {
			if (that.mouseDown) { // 移动鼠标
				$(this).css("cursor", "move");
				that.timeEnd = new Date();
				if (that.$cloneTarget == null && that.timeEnd - that.downTime > 300) { // 如果鼠标被按下的状态有1秒
					that.$cloneTarget = $(this).clone();
					that.$cloneTarget.css({
						"position" : "absolute",
						"background" : "#fff",
						"opacity" : "0.4"
					});
					$(this).after(that.$cloneTarget);
					$(that.targetEl).attr('style','opacity:0.4;background-color:#fffae8;');
				}
				if (that.timeEnd - that.downTime > 300) {
					that.$cloneTarget.css({
						"left" : e.pageX + 10,
						"top" : e.pageY + 5
					});
				}
				$(this).css("cursor", "move");

				//$(that.targetEl).addClass('tr-selected');
			}
			return false;
		}).on("mouseup", that.O.moveEl, function(e) {
			that.upkey();
		});

		that.$parent.closest('table').mouseleave(function(e) { // 鼠标离开表格时,释放所有事件
			that.upkey();
		});

		return that.$target;
	}

	dragTable.prototype = {
		upkey : function() {
			var that = this;
			that.timeEnd = new Date();
			if (that.mouseDown) {
				// 清楚数据
				if (that.targetEl && that.timeEnd - that.downTime > 300) {
					$(that.targetEl).attr("data-to", that.to);
					if (that.O.successFuc != null && typeof that.O.successFuc == "function") { // 移动之后进行的操作
						that.O.successFuc($(that.targetEl));
					}
				}
				$(that.targetEl).attr('style','opacity:1;');
				that.targetEl = null;
				if (that.$cloneTarget != null) {
					that.$cloneTarget.remove();
					that.$cloneTarget = null; // 鼠标拖动的元素
				}
				that.mouseDown = false; // 是否有元素被点击
				that.to = null;
				that.downTime = null;
				that.timeEnd = null; // 记录鼠标按下的时间
				that.$target.find(that.O.moveEl).css("cursor", "default");
			}
		}
	}

	$.fn.initTable = function(option) {
		var $target = $(this);
		if (!$target.attr('bind-dragtable')) {
			var c = new dragTable($target, option);
			$target.attr('bind-dragtable', true);
		}
	}

	$.fn.initTable.defaultopts = {
		moveEl : "tr",
		moveParentEl : "tbody",
		inbeforFuc : null, // 显示移动之前执行的方法，如果存在着会把移动的那一个jquery对象传递进去
		successFuc : null, // 移动进去成功之后调用的方法，如果存在着会把移动的那一个jquery对象传递进去
	};

})(jQuery)