var whitetheme = {
	colors : ['#4572A7', 
	          '#AA4643', 
	          '#89A54E', 
	          '#80699B', 
	          '#3D96AE', 
	          '#DB843D', 
	          '#92A8CD', 
	          '#A47D7C', 
	          '#B5CA92'],
	chart : {
		backgroundColor : '#FFFFFF',
		borderRadius : 5,
		plotShadow: true
	},
	title : {
		style : {
			color: '#3E576F',
			fontSize: '16px'
		}
	},
	subtitle : {
		style : {
			color : '#DDD',
			font : '12px Lucida Grande, Lucida Sans Unicode, Verdana, Arial, Helvetica, sans-serif'
		}
	},
	xAxis : {
		lineColor : '#C0D0E0',
		tickColor : '#C0D0E0',
		labels : {
			style : {
				color: '#6D869F',
				fontWeight: 'bold'
			}
		},
		title : {
			style : {
				color: '#6D869F',
				fontWeight: 'bold'
			}
		}
	},
	yAxis : {
		gridLineColor : '#C0C0C0',
		minorGridLineColor : '#E0E0E0',
		labels : {
			style : {
				color: '#6D869F',
				fontWeight: 'bold'
			}
		},
		title : {
			style : {
				color: '#6D869F',
				fontWeight: 'bold'
			}
		}
	},
	legend : {
		itemStyle : {
			 cursor: 'pointer',
			   color: '#274b6d',
			   fontSize: '12px'
		},
		itemHoverStyle : {
			color: '#000'
		},
		itemHiddenStyle : {
			color: '#CCC'
		}
	},
	labels : {
		style : {
			color: '#3E576F'
		}
	},
	tooltip : {
		backgroundColor : 'rgba(255, 255, 255, 0.85)',
		borderWidth : 1,
		style : {
			color: '#333333',
			fontSize: '12px',
			padding: '8px'
		}
	},

	plotOptions : {
		series : {
			shadow : false
		},
		line : {
			dataLabels : {
				color : null
			},
			marker : {
				lineColor :  "#FFFFFF"
			}
		},
		spline : {
			marker : {
				lineColor : '#FFFFFF'
			}
		},
		scatter : {
			marker : {
				lineColor : '##FFFFFF'
			}
		},
		candlestick : {
			lineColor : '#000000'
		}
	},

	toolbar : {
		itemStyle : {
			color : '#FFFFFF'
		}
	},

	navigation : {
		buttonOptions : {
			symbolStroke : ' #666',
			hoverSymbolStroke : '#FFFFFF',
			theme : {
				fill : {
					linearGradient : {
						x1 : 0,
						y1 : 0,
						x2 : 0,
						y2 : 1
					},
					stops : [ [ 0.4, '#606060' ], [ 0.6, '#333333' ] ]
				},
				stroke : '#000000'
			}
		}
	},

	// scroll charts
	rangeSelector : {
		buttonTheme : {
			fill : {
				linearGradient : {
					x1 : 0,
					y1 : 0,
					x2 : 0,
					y2 : 1
				},
				stops : [ [ 0.4, '#888' ], [ 0.6, '#555' ] ]
			},
			stroke : '#000000',
			style : {
				color : '#CCC',
				fontWeight : 'bold'
			},
			states : {
				hover : {
					fill : {
						linearGradient : {
							x1 : 0,
							y1 : 0,
							x2 : 0,
							y2 : 1
						},
						stops : [ [ 0.4, '#BBB' ], [ 0.6, '#888' ] ]
					},
					stroke : '#000000',
					style : {
						color : 'white'
					}
				},
				select : {
					fill : {
						linearGradient : {
							x1 : 0,
							y1 : 0,
							x2 : 0,
							y2 : 1
						},
						stops : [ [ 0.1, '#000' ], [ 0.3, '#333' ] ]
					},
					stroke : '#000000',
					style : {
						color : 'yellow'
					}
				}
			}
		},
		inputStyle : {
			backgroundColor : '#333',
			color : 'silver'
		},
		labelStyle : {
			color : 'silver'
		}
	},

	navigator : {
		handles : {
			backgroundColor : '#666',
			borderColor : '#AAA'
		},
		outlineColor : '#CCC',
		maskFill : 'rgba(16, 16, 16, 0.5)',
		series : {
			color : '#7798BF',
			lineColor : '#A6C7ED'
		}
	},

	scrollbar : {
		barBackgroundColor : {
			linearGradient : {
				x1 : 0,
				y1 : 0,
				x2 : 0,
				y2 : 1
			},
			stops : [ [ 0.4, '#888' ], [ 0.6, '#555' ] ]
		},
		barBorderColor : '#CCC',
		buttonArrowColor : '#CCC',
		buttonBackgroundColor : {
			linearGradient : {
				x1 : 0,
				y1 : 0,
				x2 : 0,
				y2 : 1
			},
			stops : [ [ 0.4, '#888' ], [ 0.6, '#555' ] ]
		},
		buttonBorderColor : '#CCC',
		rifleColor : '#FFF',
		trackBackgroundColor : {
			linearGradient : {
				x1 : 0,
				y1 : 0,
				x2 : 0,
				y2 : 1
			},
			stops : [ [ 0, '#000' ], [ 1, '#333' ] ]
		},
		trackBorderColor : '#666'
	},

	// special colors for some of the demo examples
	legendBackgroundColor : 'rgba(48, 48, 48, 0.8)',
	legendBackgroundColorSolid : 'rgb(70, 70, 70)',
	dataLabelsColor : '#444',
	textColor : '#E0E0E0',
	maskColor : 'rgba(255,255,255,0.3)'
};

var graytheme = {
	colors : [ "#DDDF0D", "#7798BF", "#55BF3B", "#DF5353", "#aaeeee",
			"#ff0066", "#eeaaee", "#55BF3B", "#DF5353", "#7798BF", "#aaeeee" ],
	chart : {
		backgroundColor : {
			linearGradient : {
				x1 : 0,
				y1 : 0,
				x2 : 0,
				y2 : 1
			},
			stops : [ [ 0, 'rgb(96, 96, 96)' ], [ 1, 'rgb(16, 16, 16)' ] ]
		},
		borderRadius : 15
	},
	title : {
		style : {
			color : '#FFF',
			font : '16px Lucida Grande, Lucida Sans Unicode, Verdana, Arial, Helvetica, sans-serif'
		}
	},
	subtitle : {
		style : {
			color : '#DDD',
			font : '12px Lucida Grande, Lucida Sans Unicode, Verdana, Arial, Helvetica, sans-serif'
		}
	},
	xAxis : {
		lineColor : '#999',
		tickColor : '#999',
		labels : {
			style : {
				color : '#999',
				fontWeight : 'bold'
			}
		},
		title : {
			style : {
				color : '#AAA',
				font : 'bold 12px Lucida Grande, Lucida Sans Unicode, Verdana, Arial, Helvetica, sans-serif'
			}
		}
	},
	yAxis : {
		gridLineColor : 'rgba(255, 255, 255, .1)',
		minorGridLineColor : 'rgba(255,255,255,0.07)',
		labels : {
			style : {
				color : '#999',
				fontWeight : 'bold'
			}
		},
		title : {
			style : {
				color : '#AAA',
				font : 'bold 12px Lucida Grande, Lucida Sans Unicode, Verdana, Arial, Helvetica, sans-serif'
			}
		}
	},
	legend : {
		itemStyle : {
			color : '#CCC'
		},
		itemHoverStyle : {
			color : '#FFF'
		},
		itemHiddenStyle : {
			color : '#333'
		}
	},
	labels : {
		style : {
			color : '#CCC'
		}
	},
	tooltip : {
		backgroundColor : {
			linearGradient : {
				x1 : 0,
				y1 : 0,
				x2 : 0,
				y2 : 1
			},
			stops : [ [ 0, 'rgba(96, 96, 96, .8)' ],
					[ 1, 'rgba(16, 16, 16, .8)' ] ]
		},
		borderWidth : 0,
		style : {
			color : '#FFF'
		}
	},

	plotOptions : {
		series : {
			shadow : true
		},
		line : {
			dataLabels : {
				color : '#CCC'
			},
			marker : {
				lineColor : '#333'
			}
		},
		spline : {
			marker : {
				lineColor : '#333'
			}
		},
		scatter : {
			marker : {
				lineColor : '#333'
			}
		},
		candlestick : {
			lineColor : 'white'
		}
	},

	toolbar : {
		itemStyle : {
			color : '#CCC'
		}
	},

	navigation : {
		buttonOptions : {
			symbolStroke : '#DDDDDD',
			hoverSymbolStroke : '#FFFFFF',
			theme : {
				fill : {
					linearGradient : {
						x1 : 0,
						y1 : 0,
						x2 : 0,
						y2 : 1
					},
					stops : [ [ 0.4, '#606060' ], [ 0.6, '#333333' ] ]
				},
				stroke : '#000000'
			}
		}
	},

	// scroll charts
	rangeSelector : {
		buttonTheme : {
			fill : {
				linearGradient : {
					x1 : 0,
					y1 : 0,
					x2 : 0,
					y2 : 1
				},
				stops : [ [ 0.4, '#888' ], [ 0.6, '#555' ] ]
			},
			stroke : '#000000',
			style : {
				color : '#CCC',
				fontWeight : 'bold'
			},
			states : {
				hover : {
					fill : {
						linearGradient : {
							x1 : 0,
							y1 : 0,
							x2 : 0,
							y2 : 1
						},
						stops : [ [ 0.4, '#BBB' ], [ 0.6, '#888' ] ]
					},
					stroke : '#000000',
					style : {
						color : 'white'
					}
				},
				select : {
					fill : {
						linearGradient : {
							x1 : 0,
							y1 : 0,
							x2 : 0,
							y2 : 1
						},
						stops : [ [ 0.1, '#000' ], [ 0.3, '#333' ] ]
					},
					stroke : '#000000',
					style : {
						color : 'yellow'
					}
				}
			}
		},
		inputStyle : {
			backgroundColor : '#333',
			color : 'silver'
		},
		labelStyle : {
			color : 'silver'
		}
	},

	navigator : {
		handles : {
			backgroundColor : '#666',
			borderColor : '#AAA'
		},
		outlineColor : '#CCC',
		maskFill : 'rgba(16, 16, 16, 0.5)',
		series : {
			color : '#7798BF',
			lineColor : '#A6C7ED'
		}
	},

	scrollbar : {
		barBackgroundColor : {
			linearGradient : {
				x1 : 0,
				y1 : 0,
				x2 : 0,
				y2 : 1
			},
			stops : [ [ 0.4, '#888' ], [ 0.6, '#555' ] ]
		},
		barBorderColor : '#CCC',
		buttonArrowColor : '#CCC',
		buttonBackgroundColor : {
			linearGradient : {
				x1 : 0,
				y1 : 0,
				x2 : 0,
				y2 : 1
			},
			stops : [ [ 0.4, '#888' ], [ 0.6, '#555' ] ]
		},
		buttonBorderColor : '#CCC',
		rifleColor : '#FFF',
		trackBackgroundColor : {
			linearGradient : {
				x1 : 0,
				y1 : 0,
				x2 : 0,
				y2 : 1
			},
			stops : [ [ 0, '#000' ], [ 1, '#333' ] ]
		},
		trackBorderColor : '#666'
	},

	// special colors for some of the demo examples
	legendBackgroundColor : 'rgba(48, 48, 48, 0.8)',
	legendBackgroundColorSolid : 'rgb(70, 70, 70)',
	dataLabelsColor : '#444',
	textColor : '#E0E0E0',
	maskColor : 'rgba(255,255,255,0.3)'
};