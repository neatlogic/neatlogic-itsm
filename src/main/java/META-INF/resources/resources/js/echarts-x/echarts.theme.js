(function(root, factory) {
	if (typeof define === 'function' && define.amd) {
		// AMD. Register as an anonymous module.
		define([ 'exports', 'echarts' ], factory);
	} else if (typeof exports === 'object' && typeof exports.nodeName !== 'string') {
		// CommonJS
		factory(exports, require('echarts'));
	} else {
		// Browser globals
		factory({}, root.echarts);
	}
}(this, function(exports, echarts) {
	var log = function(msg) {
		if (typeof console !== 'undefined') {
			console && console.error && console.error(msg);
		}
	};
	if (!echarts) {
		log('ECharts is not Loaded');
		return;
	}

	echarts.registerTheme('dark', {
		"color" : [ "#dd6b66", "#759aa0", "#e69d87", "#8dc1a9", "#ea7e53", "#eedd78", "#73a373", "#73b9bc", "#7289ab", "#91ca8c", "#f49f42" ],
		"backgroundColor" : "#272b30",
		"textStyle" : {},
		"title" : {
			"textStyle" : {
				"color" : "#FFFFFF",
				"fontSize" : 16,
				"fontWeight" : "normal"
			},
			"subtextStyle" : {
				"color" : "#aaaaaa"
			}
		},
		"line" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 1
				}
			},
			"lineStyle" : {
				"normal" : {
					"width" : 2
				}
			},
			"symbolSize" : 4,
			"symbol" : "circle",
			"smooth" : false
		},
		"radar" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 1
				}
			},
			"lineStyle" : {
				"normal" : {
					"width" : 2
				}
			},
			"symbolSize" : 4,
			"symbol" : "circle",
			"smooth" : false
		},
		"bar" : {
			"itemStyle" : {
				"normal" : {
					"barBorderWidth" : 0,
					"barBorderColor" : "#ccc"
				},
				"emphasis" : {
					"barBorderWidth" : 0,
					"barBorderColor" : "#ccc"
				}
			}
		},
		"pie" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"scatter" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"boxplot" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"parallel" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"sankey" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"funnel" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"gauge" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"candlestick" : {
			"itemStyle" : {
				"normal" : {
					"color" : "#fd1050",
					"color0" : "#0cf49b",
					"borderColor" : "#fd1050",
					"borderColor0" : "#0cf49b",
					"borderWidth" : 1
				}
			}
		},
		"graph" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			},
			"lineStyle" : {
				"normal" : {
					"width" : 1,
					"color" : "#aaaaaa"
				}
			},
			"symbolSize" : 4,
			"symbol" : "circle",
			"smooth" : false,
			"color" : [ "#dd6b66", "#759aa0", "#e69d87", "#8dc1a9", "#ea7e53", "#eedd78", "#73a373", "#73b9bc", "#7289ab", "#91ca8c", "#f49f42" ],
			"label" : {
				"normal" : {
					"textStyle" : {
						"color" : "#eeeeee"
					}
				}
			}
		},
		"map" : {
			"itemStyle" : {
				"normal" : {
					"areaColor" : "#eeeeee",
					"borderColor" : "#444444",
					"borderWidth" : 0.5
				},
				"emphasis" : {
					"areaColor" : "rgba(255,215,0,0.8)",
					"borderColor" : "#444444",
					"borderWidth" : 1
				}
			},
			"label" : {
				"normal" : {
					"textStyle" : {
						"color" : "#000000"
					}
				},
				"emphasis" : {
					"textStyle" : {
						"color" : "rgb(100,0,0)"
					}
				}
			}
		},
		"geo" : {
			"itemStyle" : {
				"normal" : {
					"areaColor" : "#323c48",
					"borderColor" : "#444444",
					"borderWidth" : 0.5
				},
				"emphasis" : {
					"areaColor" : "#2a333d",
					"borderColor" : "#444444",
					"borderWidth" : 1
				}
			},
			"label" : {
				"normal" : {
					"textStyle" : {
						"color" : "#000000"
					}
				},
				"emphasis" : {
					"show": false,
					"textStyle" : {
						"color" : "rgb(227,210,214)"
					}
				}
			}
		},
		"categoryAxis" : {
			"axisLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : "#aaaaaa"
				}
			},
			"axisTick" : {
				"show" : true,
				"lineStyle" : {
					"color" : "#aaaaaa"
				}
			},
			"axisLabel" : {
				"show" : true,
				"textStyle" : {
					"color" : "#cccccc"
				}
			},
			"splitLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : [ "#444444" ]
				}
			},
			"splitArea" : {
				"show" : false,
				"areaStyle" : {
					"color" : [ "#eeeeee" ]
				}
			}
		},
		"valueAxis" : {
			"axisLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : "#aaaaaa"
				}
			},
			"axisTick" : {
				"show" : true,
				"lineStyle" : {
					"color" : "#aaaaaa"
				}
			},
			"axisLabel" : {
				"show" : true,
				"textStyle" : {
					"color" : "#cccccc"
				}
			},
			"splitLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : [ "#444444" ]
				}
			},
			"splitArea" : {
				"show" : false,
				"areaStyle" : {
					"color" : [ "#eeeeee" ]
				}
			}
		},
		"logAxis" : {
			"axisLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : "#aaaaaa"
				}
			},
			"axisTick" : {
				"show" : true,
				"lineStyle" : {
					"color" : "#aaaaaa"
				}
			},
			"axisLabel" : {
				"show" : true,
				"textStyle" : {
					"color" : "#cccccc"
				}
			},
			"splitLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : [ "#444444" ]
				}
			},
			"splitArea" : {
				"show" : false,
				"areaStyle" : {
					"color" : [ "#eeeeee" ]
				}
			}
		},
		"timeAxis" : {
			"axisLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : "#aaaaaa"
				}
			},
			"axisTick" : {
				"show" : true,
				"lineStyle" : {
					"color" : "#aaaaaa"
				}
			},
			"axisLabel" : {
				"show" : true,
				"textStyle" : {
					"color" : "#cccccc"
				}
			},
			"splitLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : [ "#444444" ]
				}
			},
			"splitArea" : {
				"show" : false,
				"areaStyle" : {
					"color" : [ "#eeeeee" ]
				}
			}
		},
		"toolbox" : {
			"iconStyle" : {
				"normal" : {
					"borderColor" : "#999999"
				},
				"emphasis" : {
					"borderColor" : "#666666"
				}
			}
		},
		"legend" : {
			"textStyle" : {
				"color" : "#eeeeee"
			}
		},
		"tooltip" : {
			"axisPointer" : {
				"lineStyle" : {
					"color" : "#eeeeee",
					"width" : "1"
				},
				"crossStyle" : {
					"color" : "#eeeeee",
					"width" : "1"
				}
			}
		},
		"timeline" : {
			"lineStyle" : {
				"color" : "#eeeeee",
				"width" : 1
			},
			"itemStyle" : {
				"normal" : {
					"color" : "#dd6b66",
					"borderWidth" : 1
				},
				"emphasis" : {
					"color" : "#a9334c"
				}
			},
			"controlStyle" : {
				"normal" : {
					"color" : "#eeeeee",
					"borderColor" : "#eeeeee",
					"borderWidth" : 0.5
				},
				"emphasis" : {
					"color" : "#eeeeee",
					"borderColor" : "#eeeeee",
					"borderWidth" : 0.5
				}
			},
			"checkpointStyle" : {
				"color" : "#e43c59",
				"borderColor" : "rgba(194,53,49,0.5)"
			},
			"label" : {
				"normal" : {
					"textStyle" : {
						"color" : "#eeeeee"
					}
				},
				"emphasis" : {
					"textStyle" : {
						"color" : "#eeeeee"
					}
				}
			}
		},
		"visualMap" : {
			"color" : [ "#bf444c", "#d88273", "#f6efa6" ]
		},
		"dataZoom" : {
			"backgroundColor" : "rgba(47,69,84,0)",
			"dataBackgroundColor" : "rgba(255,255,255,0.3)",
			"fillerColor" : "rgba(167,183,204,0.4)",
			"handleColor" : "#a7b7cc",
			"handleSize" : "100%",
			"textStyle" : {
				"color" : "#eeeeee"
			}
		},
		"markPoint" : {
			"label" : {
				"normal" : {
					"textStyle" : {
						"color" : "#eeeeee"
					}
				},
				"emphasis" : {
					"textStyle" : {
						"color" : "#eeeeee"
					}
				}
			}
		}
	});
	
	echarts.registerTheme('light', {
		"color" : [ "#2ec7c9","#e5cf0d","#b6a2de", "#5ab1ef", "#ffb980","#d87a80","#8d98b3","#97b552","#95706d","#dc69aa"],
		"backgroundColor" : "rgba(255,255,255,0)",
		"textStyle" : {},
		"title" : {
			"textStyle" : {
				"color" : "#666666",
				"fontSize" : 16,
				"fontWeight" : "normal"
			},
			"subtextStyle" : {
				"color" : "#999999"
			}
		},
		"line" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : "1"
				}
			},
			"lineStyle" : {
				"normal" : {
					"width" : "2"
				}
			},
			"symbolSize" : "4",
			"symbol" : "emptyCircle",
			"smooth" : false
		},
		"radar" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : "2"
				}
			},
			"lineStyle" : {
				"normal" : {
					"width" : "3"
				}
			},
			"symbolSize" : "8",
			"symbol" : "emptyCircle",
			"smooth" : false
		},
		"bar" : {
			"itemStyle" : {
				"normal" : {
					"barBorderWidth" : 0,
					"barBorderColor" : "#ccc"
				},
				"emphasis" : {
					"barBorderWidth" : 0,
					"barBorderColor" : "#ccc"
				}
			}
		},
		"pie" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"scatter" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"boxplot" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"parallel" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"sankey" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"funnel" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"gauge" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				},
				"emphasis" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			}
		},
		"candlestick" : {
			"itemStyle" : {
				"normal" : {
					"color" : "#d0648a",
					"color0" : "transparent",
					"borderColor" : "#d0648a",
					"borderColor0" : "#22c3aa",
					"borderWidth" : "1"
				}
			}
		},
		"graph" : {
			"itemStyle" : {
				"normal" : {
					"borderWidth" : 0,
					"borderColor" : "#ccc"
				}
			},
			"lineStyle" : {
				"normal" : {
					"width" : "1",
					"color" : "#cccccc"
				}
			},
			"symbolSize" : "8",
			"symbol" : "emptyCircle",
			"smooth" : false,
			"color" :  [ "#2ec7c9","#e5cf0d","#b6a2de", "#5ab1ef", "#ffb980","#d87a80","#8d98b3","#97b552","#95706d","#dc69aa"],
			"label" : {
				"normal" : {
					"textStyle" : {
						"color" : "#ffffff"
					}
				}
			}
		},
		"map" : {
			"itemStyle" : {
				"normal" : {
					"areaColor" : "#eeeeee",
					"borderColor" : "#999999",
					"borderWidth" : 0.5
				},
				"emphasis" : {
					"areaColor" : "rgba(34,195,170,0.25)",
					"borderColor" : "#22c3aa",
					"borderWidth" : 1
				}
			},
			"label" : {
				"normal" : {
					"textStyle" : {
						"color" : "#28544e"
					}
				},
				"emphasis" : {
					"textStyle" : {
						"color" : "rgb(52,158,142)"
					}
				}
			}
		},
		"geo" : {
			"itemStyle" : {
				"normal" : {
					"areaColor" : "#eeeeee",
					"borderColor" : "#999999",
					"borderWidth" : 0.5
				},
				"emphasis" : {
					"areaColor" : "rgba(34,195,170,0.25)",
					"borderColor" : "#22c3aa",
					"borderWidth" : 1
				}
			},
			"label" : {
				"normal" : {
					"textStyle" : {
						"color" : "#28544e"
					}
				},
				"emphasis" : {
					"textStyle" : {
						"color" : "rgb(52,158,142)"
					}
				}
			}
		},
		"categoryAxis" : {
			"axisLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : "#cccccc"
				}
			},
			"axisTick" : {
				"show" : false,
				"lineStyle" : {
					"color" : "#333"
				}
			},
			"axisLabel" : {
				"show" : true,
				"textStyle" : {
					"color" : "#999999"
				}
			},
			"splitLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : [ "#eeeeee" ]
				}
			},
			"splitArea" : {
				"show" : false,
				"areaStyle" : {
					"color" : [ "rgba(250,250,250,0.05)", "rgba(200,200,200,0.02)" ]
				}
			}
		},
		"valueAxis" : {
			"axisLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : "#cccccc"
				}
			},
			"axisTick" : {
				"show" : false,
				"lineStyle" : {
					"color" : "#333"
				}
			},
			"axisLabel" : {
				"show" : true,
				"textStyle" : {
					"color" : "#999999"
				}
			},
			"splitLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : [ "#eeeeee" ]
				}
			},
			"splitArea" : {
				"show" : false,
				"areaStyle" : {
					"color" : [ "rgba(250,250,250,0.05)", "rgba(200,200,200,0.02)" ]
				}
			}
		},
		"logAxis" : {
			"axisLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : "#cccccc"
				}
			},
			"axisTick" : {
				"show" : false,
				"lineStyle" : {
					"color" : "#333"
				}
			},
			"axisLabel" : {
				"show" : true,
				"textStyle" : {
					"color" : "#999999"
				}
			},
			"splitLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : [ "#eeeeee" ]
				}
			},
			"splitArea" : {
				"show" : false,
				"areaStyle" : {
					"color" : [ "rgba(250,250,250,0.05)", "rgba(200,200,200,0.02)" ]
				}
			}
		},
		"timeAxis" : {
			"axisLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : "#cccccc"
				}
			},
			"axisTick" : {
				"show" : false,
				"lineStyle" : {
					"color" : "#333"
				}
			},
			"axisLabel" : {
				"show" : true,
				"textStyle" : {
					"color" : "#999999"
				}
			},
			"splitLine" : {
				"show" : true,
				"lineStyle" : {
					"color" : [ "#eeeeee" ]
				}
			},
			"splitArea" : {
				"show" : false,
				"areaStyle" : {
					"color" : [ "rgba(250,250,250,0.05)", "rgba(200,200,200,0.02)" ]
				}
			}
		},
		"toolbox" : {
			"iconStyle" : {
				"normal" : {
					"borderColor" : "#999999"
				},
				"emphasis" : {
					"borderColor" : "#666666"
				}
			}
		},
		"legend" : {
			"textStyle" : {
				"color" : "#999999"
			}
		},
		"tooltip" : {
			"axisPointer" : {
				"lineStyle" : {
					"color" : "#cccccc",
					"width" : 1
				},
				"crossStyle" : {
					"color" : "#cccccc",
					"width" : 1
				}
			}
		},
		"timeline" : {
			"lineStyle" : {
				"color" : "#4ea397",
				"width" : 1
			},
			"itemStyle" : {
				"normal" : {
					"color" : "#4ea397",
					"borderWidth" : 1
				},
				"emphasis" : {
					"color" : "#4ea397"
				}
			},
			"controlStyle" : {
				"normal" : {
					"color" : "#4ea397",
					"borderColor" : "#4ea397",
					"borderWidth" : 0.5
				},
				"emphasis" : {
					"color" : "#4ea397",
					"borderColor" : "#4ea397",
					"borderWidth" : 0.5
				}
			},
			"checkpointStyle" : {
				"color" : "#4ea397",
				"borderColor" : "rgba(60,235,210,0.3)"
			},
			"label" : {
				"normal" : {
					"textStyle" : {
						"color" : "#4ea397"
					}
				},
				"emphasis" : {
					"textStyle" : {
						"color" : "#4ea397"
					}
				}
			}
		},
		"visualMap" : {
			"color" : [ "#d0648a", "#22c3aa", "#adfff1" ]
		},
		"dataZoom" : {
			"backgroundColor" : "rgba(255,255,255,0)",
			"dataBackgroundColor" : "rgba(222,222,222,1)",
			"fillerColor" : "rgba(114,230,212,0.25)",
			"handleColor" : "#cccccc",
			"handleSize" : "100%",
			"textStyle" : {
				"color" : "#999999"
			}
		},
		"markPoint" : {
			"label" : {
				"normal" : {
					"textStyle" : {
						"color" : "#ffffff"
					}
				},
				"emphasis" : {
					"textStyle" : {
						"color" : "#ffffff"
					}
				}
			}
		}
	});

}));