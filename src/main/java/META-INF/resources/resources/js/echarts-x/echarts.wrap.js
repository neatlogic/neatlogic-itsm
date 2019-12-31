;
(function() {
	var tsechart = function(_config) {
		this.url = _config.url;
		this.type = _config.type
		this.container = _config.container;
		this.theme = _config.theme;
		this.option = _config.option;
		if (_config.interval) {
			this.interval = Math.max(1000 * 60, 1000 * 60 * parseInt(_config.interval, 10));
		}
		// this.chart = echarts.init(_config.container, _config.theme);
		// console.log(_config.theme)
		this.startInterval();
	};
	this.tsechart = tsechart;
	tsechart.prototype = {
		getEChart : function() {
			return this.chart;
		},
		startInterval : function(needClear) {
			var that = this;
			if (this.url) {
				$.getJSON(this.url, function(data) {
					if (that.type && that.type == 'scatter') {
						var chartList = [];
						if(!needClear){
							var tmpcontainer = $(that.container).empty()[0];
						}
						for (var i = 0; i < 5; i++) {
							if (needClear) {
								that.chart[i].clear();
								that.chart[i].setOption(data.returnJsonList[i]);
							} else {
								var dom = '<div class="multipleCompo"></div>';
								that.container = $(tmpcontainer).append(dom).find('div:last')[0];
								that.chart = echarts.init(that.container,that.theme);
								that.chart.setOption(data.returnJsonList[i]);
								chartList.push(that.chart);
							}
						}
						if (chartList.length != 0) {
							that.chart = chartList;
						}
						if (that.interval) {
							that.TIMEOUT_HANDLER = window.setTimeout(function() {
								that.startInterval(true);
							}, that.interval);
						}
					} else {
						if (needClear) {
							that.chart.clear();
							that.chart.setOption(data);
						} else {
							that.chart = echarts.init(that.container, that.theme);
							that.chart.setOption(data);
						}

						if (that.interval) {
							clearTimeout(that.TIMEOUT_HANDLER);
							that.TIMEOUT_HANDLER = window.setTimeout(function() {
								that.startInterval(true);
							}, that.interval);
						}
					}

				}).fail(function(jqxhr, textStatus, error) {
					// alert('failed');
					console.info(error);
				});
			} else if (that.option) {
				if (needClear) {
					that.chart.clear();
					that.chart.setOption(that.option);
				} else {
					that.chart = echarts.init(that.container, that.theme);
					that.chart.setOption(that.option);
				}
			}
		},
		refresh : function(_config) {
			if (this.TIMEOUT_HANDLER) {
				window.clearTimeout(this.TIMEOUT_HANDLER);
				this.TIMEOUT_HANDLER = null;
			}
			this.url = _config.url || this.url;
			if (_config.interval) {
				Math.max(1000 * 60, 1000 * 60 * parseInt(_config.interval, 10));
			} else {
				this.interval = 0;
			}
			this.startInterval(true);
		},
		destory : function() {
			if (this.TIMEOUT_HANDLER) {
				window.clearTimeout(this.TIMEOUT_HANDLER);
				this.TIMEOUT_HANDLER = null;
			}
			$(this.container).empty();
		}
	}

}());