;(function () {
    var TsFlowChart = window.TsFlowChart || {};
    var flowChartOpts = {
        zoomExtent: [0.2, 10],
        iconPath: './image/',
        readOnly: false,
        durationTime: 500,
		uuidmode: 5 // 默认uuid位数
    };
    var nodeOpts = {
        startNodeWidth: 93,
        startNodeHeight: 37,
        nodeWidth: 93,
        nodeHeight: 37,
        nodeHeight1: 50,
        anchorPointRadius: 4,
        anchorPointOffset: 4 + 3,
        anchorPointHoverRadius: 4 + 6,
        nodeBtnWidth: 17,
        nodePadding: 8,
        nodeIconWidth: 20,
        nodeFontIconWidth: 30,
        nodeStatusBtnHeight: 25,
        nodeStatusIconWidth: 13,
    };
    var linkOpts = {
        anchorPathMinLength: 20,
        linkDragAnchorWidth: 12,
        linkDragAnchorHeight: 8,
        linkPathRadius: 10,
        linkLabelWidth: 36,
        linkLabelHeight: 16,
        linkHoverWidth: 5
    };
    var flowChartCb = {};
    var theme1 = {
        node: {
            textColor: '#ffffff',
            bg: '#87b2ec',
            bd: '#87b2ec',
            textFont: 12,
        }
    };

    // 主体
    TsFlowChart = function (container, options) {
        return new TsFlowChart.prototype.init(container, options);
    };

    TsFlowChart.prototype = {
        init: function (container, opts) {
            var that = this;
            if (this.consoleLog(container, '容器container不存在！')) return false;
            // uuid arr
            this.uuidArr = [];
            this.uuidArr.add = function (ele) {
                if (!that.include(that.uuidArr, ele)) that.uuidArr.push(ele);
            };
            this.uuidArr.remove = function (ele) {
                var index = that.uuidArr.indexOf(ele);
                if (index !== -1) {
                    that.uuidArr.splice(index, 1);
                }
            };
            this.svgId = this.createId(8);
            this.container = container;
            this.options = Object.assign({}, flowChartOpts, nodeOpts, linkOpts, flowChartCb, opts);
            this.svg = d3.select(this.container).classed('flow-chart', true).attr('onselectstart', 'return false')
                .append('svg').attr('class', 'svg').attr('width', '100%').attr('height', '100%').attr('text-rendering', 'geometricPrecision');
            this.calculateText = this.svg.append('text').attr('y', -100).attr('fill', 'none').attr('stroke', 'none');
			this.defs = this.svg.append('defs').attr('class', 'defs');
            this.zoomG = this.svg.append('g').attr('class', 'zoomG');
            this.linksG = this.zoomG.append('g').attr('class', 'linksG');
            this.nodesG = this.zoomG.append('g').attr('class', 'nodesG');
            this.assistG = this.zoomG.append('g').attr('class', 'assistG');
            this.tooltipG = this.zoomG.append('g').attr('class', 'tooltipG');
            this.tooltipDiv = d3.select(this.container).style('overflow', 'hidden').style('position', 'relative')
                .append('xhtml:div').attr('class', 'tooltipDiv').style('position', 'absolute').style('top', 0).style('left', 0);

            // 事件管理器
            this.dispatch = d3.dispatch('link', 'node', 'svg');
            // 添加zoom
            this.zoomFn();
            // 添加拖动
            this.dragFn();
            // 添加defs
            this.appendDefs();
            // 添加dispatch
            this.dispatchFn();

            // 画布添加事件
            this.svg.on('click', function () {
                that.linksG.selectAll('.linkG.active').dispatch('reSelected');
            });

            // 添加键盘事件
            document.onkeydown = function (e) {
                if (e.keyCode === 46 || e.keyCode === 8) {
                    that.linksG.selectAll('.linkG.active').dispatch('remove');
                }
            };
            // 主题
            that.options.theme = theme1;
            // 对外的接口
            this.SVG = TsFlowChart.SVG;
            this.NODE = TsFlowChart.NODE;
            this.LINK = TsFlowChart.LINK;
            // 实例化画布
            this.canvas = new this.SVG(this);
            return this.canvas;
        },
        extend: function (obj) {
            for (var attr in obj) {
                this[attr] = obj[attr];
            }
        }
    };

    TsFlowChart.extend = function (obj) {
        for (var attr in obj) {
            this[attr] = obj[attr];
        }
    };

    // 方法
    TsFlowChart.prototype.extend({
        fromJson: function (json) {
            if (this.consoleLog(json, '无效json数据')) return false;
            this.Json = json;
            this.elementList = json['elementList'];
            if (this.elementList && this.elementList.length > 0) {
                this.nodesData = this.nodeDataFn(this.elementList);
                this.drawNode(this.nodesData);
            }
            this.connectionList = json['connectionList'];
            if (this.connectionList && this.connectionList.length > 0) {
                this.linksData = this.linksDataFn(this.connectionList);
                this.drawLink(this.linksData, 'init');
            }

            var startNode = this.nodesData.find(function (d) {
                return d.data.type === 'start';
            });
            if (startNode && startNode.data.zoom) {
                var zoomIdentity = d3.zoomIdentity;
                zoomIdentity.k = startNode.data.zoom.k;
                zoomIdentity.x = startNode.data.zoom.x;
                zoomIdentity.y = startNode.data.zoom.y;
                this.svg.call(this.zoom.transform, zoomIdentity);
            }
            this.userData = this.Json.userData;
        },
        toJson: function () {
            var that = this;
            var nodesData = that.nodesG.selectAll('.nodeG').data();
            nodesData = nodesData.map(function (d) {
                return that.getNodeData(d);
            });
            var linksData = that.linksG.selectAll('.linkG').data();
            linksData = linksData.map(function (d) {
                return that.getLinkData(d);
            });

            // 添加zoom数据
            var startNode = nodesData.find(function (d) {
                return d.type === 'start';
            });
            if (startNode) {
                startNode.zoom = d3.zoomTransform(this.svg.node());
            }
            if (!this.Json) this.Json = {};
            this.Json['elementList'] = nodesData;
            this.Json['connectionList'] = linksData;
            this.userData && (this.Json.userData = this.userData);
            return that.Json;
        },
        getIcon: function (iconName) {
            var path = this.options.iconPath;
            switch (iconName) {
                case 'start':
                    return 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAYAAACM/rhtAAAAAXNSR0IArs4c6QAABxVJREFUWAnFWAtsFFUUfW/27VZETMGEyk+MWkwAJfzBb5CYKCbdtkhjERJ/fHfrN3wUQQwKQYMx7BaoRCFEKbZCuyUpRKIkkCjK1ygfEcRARD5KbFGgO2/nee5sp93OzkynEnSS3bnv3nPPvXPn/eZxdhVXcVwNSTEZBsX9jKlejPGeaTp1GvKvkHcGmEhsivJ9/zYM76ijUoqH46nHOTfeUor1s/w55zqSPJNu85uBC7ba2FGltNcT0cBnwClL7+feoQQLK9TtKinXK6ZGMARCqG2Qa3JCwfqqqeyUFZweouQD1qcpqY/jjBcpzh5m0EH+lofExNpp/Lif5AjjO8HCuD4WFatCQt2QyPYAV3M2RUK7/QQqLk8OTym+FImPQZIXkGlJbTT4hR9fXwlScoZiW/E4yIvNT5QF3/ZDbseEY/o8VHMRUyylcfaInyTbTXD8KpWvS/kNguWicqWJiPjUHrgj7cK4LDGY2gCfP4NCjNw4nf/k5a95GakvSalXov90BXDe1SZHsWqjooq4iJO4KYZXDp7GcExOVMz4BF31y7qy4Fgvoo7aCmI6+qB6iDPtyUSZWO/m71pBejLFjUU0WgUXs9wI/q3e5AQ3xfCqomsFw/HkSIzaXajeFlRvnFciBbHkMM61p1CRYWkc36OUsbauLLTH20+vh8+jGNWjEtEQ9fOsy7WCTPFCE63xmiyvDEU4rr+JwbMLCUUwA48wf5DxYF+TLQOaLVrcVqxsBHNPkLF7CS9CgS0OfqaqcIWchNezAJX+nXGttEuuyKUfyQD8QTbCuPlncJuxnHDurziWPIb+d0ttROSgQlnL08LtSuw7KM/B0lkTYmjNDP5DZoCilWqgIeVejNG/hwwQ3ReO4TLTTjL1vcJy2QThZKIsdIfdTm2PCtLCr844JUeOBw6xgTRVAFNjT47saZ2qIYyJJaXtIm5cv6E7NG8ybAA03RPkLIV+2CXbJa0xjFQ+SYrxo24Yy2ZhnXBY+m7ECpVyspHOPUGmTmDdzS1erXo7OWPVO9KsH+BkJx1GZ3/z3optAyVuioHHPNHGkNHwSFDbQTijKXVPBr5FDOWxQ3hF56AooGmmxdAskA6DJ0wYwtrt1G7lTsdywrgmGAioSnJAH5ni5FhdwvFa+HQgBF7TtnB5chpVhH4kk45shEljs1ksbitWNgLeTkpLVxBP7gVkMBb1O90W9YK4/jI64mIkk2P5pe+8Ceyv1UWD77XVp1vNm5Af4be/Lhoa6oQhnWsFTSPXYighT0l9rhsBJRDQxGC8ysWo2ufmDzLp3JIjLmwUXiVujWJ4XJ4VLKtXOSePy2Pw76kFxZCa6fw7Dy7fpqJVapChS/pOOd31BpG/9ml+xc3Zs4KxcbwJG/VZGGkaJt1lbiQd1RMXcRK3V3LE65kgAeqiYgNe20506LG0/SLd1VzmFg5cxEnc7XG1myARcE08D0KDcbW8dLXKa4/UzW76goO4iNMNl6n3lWBthB+A0zuo4k2XmuTKTIKOyORLHMTVzNmuu68EiSWUJ97AynAAAYrCcflcu8w2APmQL3EQl83s2vQcxXYv2qGkpNwDJwM74uEbo/ygHePUHh9XA6SSu7El0gJCDHPaXDj5kc53BQlMxNiAzMYI7CSVXjV5nepMeq+LMIQlH/LtSHLE65iguVzF9FOF5fp8e3BszZdjhUBA1r+hUf/Ibre3GxrkGsKSj+lrA2Al2oyd91dTK1qPSjIhjgmmmvSleOLeymCXM8GWnMODz0I+giWuBORzLL39nrapCYRt9rFDsNKxy+ibo88m5QvZRocKFsX10djHlaIz/4JZPu7kVB3hfwW0YDEmoEZahwtWyMfsOFMHG5bAi4QlHzuG2jnXiTnYl11BhRcUV6gedkybCuJJOI443qc1EqdRs71m+ZoIP4zg9O2BWU1VhmPqLoucZNKl2/wJwlo2+716Cj+hKUbnNl1Suv6u3d5mFONYYrKhjHUIvCMRDT5oBzu107sZtQwVP4UTt1GEwUkcvvJYHwiveG0YLL4JVapT8px+CD638gB/IDEzuNOytVSQRhvOTJbQLK8C4kUL0N6dEsA3cYWZEKPvXL2eZNL5SY74sV+8zDXtJZKVoeJIOEAyXS0JXmyUc/Fqe2FwrKmbwfenzf7+Q90DEVR9M9CD6Ecy6fx5p1G1M0UtKr4Vffpu/bw+w/I1XzGOcvumlDyMjiqvzxH5lVP4WQvg906vqems/JjwOXliElXFr6+FK4qrfoaS36N9KdRZ9Kt+hp83beFYcgO+IZTXlGGRXOs7zhCXUC6F8eSHFIsXlev3pQy1E9X7ue9twf60B7zWSXjx01hobJBH8MHfizM1GgfiNOLUSAyOC+iiZ7yc/ysbNrI9MB5wKMB3CXRKsx9icHRDAvT7/y9MA3QhMe0fq5cg683m78gAAAAASUVORK5CYII=';
                // return path + 'start.png'
                case 'end':
                    return 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACIAAAAqCAYAAADWFImvAAAAAXNSR0IArs4c6QAAAo9JREFUWAntl8trFEEQh6umZwl6ElQUc1E8CD5AD4J4i4Qc9rCzEXJVlhjEZOPfsAcF78mikbCuEBTEKKMgSg4+rh4EYUFExCiIj4Oeguz0TFk9Mx2HoQUPmZ6A25eqedC/b6pqqrsR0uG1+9MUQVtdVrByeLmJPf3MhnVsiPyLhmt6SZKs19vyuOlZEfccEq+NIAR0iSIqQtM4Z+TQqfJTg/DOn3afGSPiOE4DIvpgxN+Am4QwTkQX1VQIuIiIZAQRJF4uzxb313jzweXke1BuHXK7yreeGu8aHeRonExA4MHtKfxaCggE8lwKAY7AxXVfOzbs7CMa4qI4k2p9PHpBPNG6xhrRDzfarr4Px4Foezyvg50WYqQ1rIIwxJQS5j+FAdyOhlDWWrHWF2g/E4zE4giP/Rn8VApIFMhJjggm4n+KVMNYiUjradyvGkk08MuxQ+KhBtDWCsirXjjG0ditRFnwVmsEpQbQ1gqIANFLChSAq/SIFs9aKyD3mrjKhbqSCo/Wr9LeLITyrYDEopgWqCrYkAs3N6yB7HKFz6vsN6XP6WlM3CGRZbEGcv08Bix8MxYnGu5/D6ulgChRB9z1RU53WQ1jLSJK8H4T33KDf56KV2tztKcUECWql37ekwgEmTQ5dV8T2bLbtoi7gPhD6fEmfZKBUPnWQboN/MXNbUmJ89h3ui1HlWPcBgQgb9TmgjX1QhGDINqh5w0J1NZgxQjCFV3s4SpzZOK8eBMd2mkE4cbzmTPY19TFWgK5FlaNIC64Y//tIdz6X/O3NA9A8pEZRGQQkXwE8tebpkawNt/vxnSEB7itn4h9RJ/9n9zq3/gzlSt5+iKuXd4UnE0mzqxERF4q9oKtFZBNk5rftDy44ejjSuEAAAAASUVORK5CYII=';
                default:
                    return path + 'default.png';
            }
        },
        valid: function () {
            var that = this;
            that.nodesG.selectAll('.nodeG').dispatch('valid');
            var nodes = this.nodesG.selectAll('.nodeG');
            var b = true;
            var errorNode = [];

            nodes.each(function (d) {
                var _this = this;
                if (d.status === 'error') {
                    d3.select(this).dispatch('default');
                }
                d.tipData.error.splice(0);

                if (typeof d.valid === 'object') {
                    Object.keys(d.valid).forEach(function (k) {
                        if (d.behavior[k] === false || d.behavior[k] === true) {
                            var msg = '';
                            if (d.behavior[k] === false && d.valid[k] === true) {
                                if (k === 'needIn') {
                                    msg = '不能有线连入';
                                } else if (k === 'needOut') {
                                    msg = '不能有线连出';
                                }
                            } else if (d.behavior[k] === true && d.valid[k] === false) {
                                if (k === 'needIn') {
                                    msg = '没有线连入';
                                } else if (k === 'needOut') {
                                    msg = '没有线连出';
                                }
                            } else {

                            }
                            if (msg) {
                                if (!that.include(d.tipData.error, msg)) d.tipData.error.push(msg);
                                if (!that.include(errorNode, d)) errorNode.push(d);
                            }
                        }
                    });
                }
                // 判断孤岛
                if (d.data.type !== 'start' && d.data.type !== 'end') {
                    var msg = '';
                    if (d.behavior.needIn) {
                        var isStart = that.isConnectStart(_this);
                        if (isStart === false) {
                            msg = '不能被起始节点孤立';
                        }
                    }
                    if (d.behavior.needOut) {
                        var isEnd = that.isConnectEnd(_this);
                        if (isEnd === false) {
                            msg = '不能被结束节点孤立';
                        }
                    }
                    if (msg) {
                        if (!that.include(d.tipData.error, msg)) d.tipData.error.push(msg);
                        if (!that.include(errorNode, d)) errorNode.push(d);
                    }
                }
                if (typeof d.data.onValid === 'function') {
                    var oEle = that.getNodeObject(this);
                    var is = d.data.onValid(oEle);
                    if (is === false) {
                        b = is;
                        // errorNode.push({error: 'onValid', node: d});
                    }
                }
            });

            if (errorNode.length > 0) b = false;

            errorNode.forEach(function (d1) {
                d1.tipData.msg = '该节点：\n' + d1.tipData.error.join('\n');
            });
            that.tooltipFn(errorNode);
            return b;
        },
        include: function (array, target) {
            if (Array.isArray(array)) {
                return array.some(function (d) {
                    return d === target;
                })
            } else return false
        },
        getBodyOffset: function () {
            var el = this.container;
            var offsetTop = 0;
            var offsetLeft = 0;
            while (el && el.tagName !== 'BODY') {
                offsetTop += el.offsetTop;
                offsetLeft += el.offsetLeft;
                el = el.offsetParent;
            }
            return {dx: offsetLeft, dy: offsetTop};
        },
        tooltipFn: function (nodeData) {
            if (!Array.isArray(nodeData)) nodeData = [nodeData];
            var that = this;
            var maxWidth = 200;
            var padding = 18;
            var trgWidth = 8;
            var trgHeight = 5;
            var rx = 6;
            var delayTime = 2000;
            var tipG = this.tooltipG.selectAll('.tipG').data(nodeData, function (d) {
                return d.data.id;
            });
            var tipEnterG = tipG.enter().append('g').attr('class', 'tipG').attr('filter', `url(#filter1${this.svgId})`);

            tipG = tipG.merge(tipEnterG);
            tipG.each(function (d) {
                var width = 0;
                var height = 20;
                var cTipG = d3.select(this);
                cTipG.select('.delay').interrupt();
                cTipG.html('');
                var rect = cTipG.append('rect')
                    .attr('rx', rx)
                    .attr('ry', rx)
                    .attr('fill', d.tipData.fill)
                    .attr('stroke', d.tipData.stroke);

                var triangle = cTipG.append('path')
                    .attr('class', 'trg')
                    .attr('fill', d.tipData.fill)
                    .attr('class', 'trg')
                    .attr('d', `M0,0L${trgWidth},${trgHeight}L${trgWidth * 2},0`);
                if (d.tipData.icon) {
                    cTipG.append('text')
                        .attr('class', 'icon')
                        .text(d.tipData.icon)
                        .attr('font-size', d.tipData.iconSize)
                        .attr('font-family', d.tipData.fontFamily)
                        .attr('fill', d.tipData.iconColor)
                        .attr('x', padding)
                        .attr('text-anchor', 'middle')
                        .attr('y', height / 2)
                        .attr('dy', '0.37em');
                    width += padding * 2;
                }

                var deleteCircle = cTipG.append('circle')
                    .attr('class', 'del-cir')
                    .attr('text-anchor', 'middle')
                    .attr('cursor', 'pointer')
                    .attr('r', 8)
                    .attr('fill', d.tipData.fill);
                var deleteIcon = cTipG.append('text')
                    .attr('class', 'del')
                    .attr('text-anchor', 'middle')
                    .attr('cursor', 'pointer')
                    .text(d.tipData.deleteIcon)
                    .attr('font-size', d.tipData.deleteIconSize)
                    .attr('fill', d.tipData.deleteIconColor)
                    .attr('font-family', d.tipData.fontFamily)
                    .attr('dy', '0.37em');

                var delay = cTipG.append('path')
                    .attr('class', 'delay')
                    .attr('fill', 'none')
                    .attr('stroke', d.tipData.delay)
                    .attr('stroke-opacity', d.tipData.delayOpacitty)
                    .attr('stroke-dashoffset', 0)
                    .attr('stroke-dasharray', 0)
                    .attr('stroke-width', 1);

                var msg = cTipG.append('text')
                    .attr('class', 'msg')
                    .attr('fill', d.tipData.fontColor)
                    .attr('font-size', d.tipData.fontSize)
                    .text(d.tipData.msg);

                var lineClamp = 1;
                var strArr = d.tipData.msg.split('\n');
                if (strArr.length === 1) {
                    var msgWidth = msg.node().getBBox().width;
                    width += msgWidth;
                    var str = d.tipData.msg;
                    if (width > maxWidth) {
                        lineClamp = Math.ceil(msgWidth / maxWidth);
                    }
                    width -= msgWidth;
                    var strLen = d.tipData.msg.length / lineClamp;
                    strArr.pop();
                    for (var i = 0; i < lineClamp; i++) {
                        strArr.push(str.substring(strLen * i, strLen * (i + 1)));
                    }
                }

                var tspan = msg.html('').selectAll('tspan').data(strArr);
                tspan.enter().append('tspan')
                    .text(function (d1) {
                        return d1;
                    })
                    .attr('x', function (d1, i1) {
                        if (i1 === 0 && d.tipData.icon) return width;
                        else return 10;
                    })
                    .attr('y', function (d1, i1) {
                        return i1 * 20 + 16;
                    })
                    .attr('dy', '0.37em');
                var msgBox = msg.node().getBBox();
                width += msgBox.width + 10 * 2;
                height = msgBox.height + 18;
                deleteIcon
                    .attr('y', 4)
                    .attr('x', width - 4);

                deleteCircle
                    .attr('cx', width - 4)
                    .attr('cy', 4);

                rect.attr('width', width)
                    .attr('height', height);

                deleteIcon.on('click', function () {
                    cTipG.dispatch('remove');
                });
                cTipG
                    .on('mouseenter', function () {
                        delay.dispatch('add').raise()
                    })
                    .on('mouseleave', function () {
                        delay.dispatch('minus')
                    })
                    .on('remove', function () {
                        cTipG
                            .attr('opacity', 1)
                            .transition('remove')
                            .duration(700)
                            .attr('opacity', 0)
                            .on('end', function () {
                                cTipG.remove();
                            });
                    });
                if (d.x === undefined) {
                    var fp = that.linkPathFilterDir(d.path);
                    var linePath = that.filterLabelPosition(fp);
                    var cx = (linePath.x0 + linePath.x1) / 2;
                    var cy = (linePath.y0 + linePath.y1) / 2;
                    var dy = 0.5;
                    delay.attr('d', `M${dy},${height - dy - rx}A${rx},${rx},0,0,0,${rx},${height - dy}L${rx},${height - dy}L${width / 2 - trgWidth},${height - dy}L${width / 2},${height + trgHeight - dy},L${width / 2 + trgWidth},${height - dy},L${width - rx},${height - dy}A${rx},${rx},0,0,0,${width - dy},${height - rx - dy}`);
                    triangle.attr('transform', `translate(${width / 2 - trgWidth},${height - 0.1})`);
                    cTipG.attr('transform', `translate(${cx - width / 2},${cy - height - 10})`);
                } else {
                    var dy = 0.5;
                    delay.attr('d', `M${dy},${height - dy - rx}A${rx},${rx},0,0,0,${rx},${height - dy}L${rx},${height - dy}L${d.width / 2 - trgWidth},${height - dy}L${d.width / 2},${height + trgHeight - dy},L${d.width / 2 + trgWidth},${height - dy},L${width - rx},${height - dy}A${rx},${rx},0,0,0,${width - dy},${height - rx - dy}`);
                    triangle.attr('transform', `translate(${d.width / 2 - trgWidth},${height - 0.1})`);
                    cTipG.attr('transform', `translate(${d.x - rx},${d.y - height - 10})`);
                }

                // 延时关闭动画
                var totalLength = delay.node().getTotalLength();
                var stepTime = delayTime / totalLength;
                delay
                    .attr('stroke-dasharray', totalLength)
                    .on('add', function () {
                        delay.interrupt();
                        var gth = Number(delay.attr('stroke-dashoffset'));
                        delay
                            .transition()
                            .duration(stepTime * gth)
                            .ease(d3.easeLinear)
                            .attr('stroke-dashoffset', 0)
                    })
                    .on('minus', function () {
                        delay.interrupt();
                        var gth = totalLength - Number(delay.attr('stroke-dashoffset'));
                        delay
                            .transition()
                            .duration(stepTime * gth)
                            .ease(d3.easeLinear)
                            .attr('stroke-dashoffset', totalLength)
                            .on('end', function () {
                                cTipG.dispatch('remove');
                            })
                    })
                    .dispatch('minus');
            });
        },
        appendDefs: function () {
            var that = this;
            var arrowMarkerS = that.defs.append('marker')
                .attr('id', 'arrow_r_s' + that.svgId)
                .attr('markerUnits', 'strokeWidth')
                .attr('markerWidth', '12')
                .attr('markerHeight', '12')
                .attr('viewBox', '0 0 12 12')
                .attr('refX', '8')
                .attr('refY', '6')
                .attr('orient', 'auto');
            var arrowMarkerF = that.defs.append('marker')
                .attr('id', 'arrow_r_f' + that.svgId)
                .attr('markerUnits', 'strokeWidth')
                .attr('markerWidth', '12')
                .attr('markerHeight', '12')
                .attr('viewBox', '0 0 12 12')
                .attr('refX', '8')
                .attr('refY', '6')
                .attr('orient', 'auto');
            var arrowMarkerE = that.defs.append('marker')
                .attr('id', 'arrow_r_e' + that.svgId)
                .attr('markerUnits', 'strokeWidth')
                .attr('markerWidth', '12')
                .attr('markerHeight', '12')
                .attr('viewBox', '0 0 12 12')
                .attr('refX', '8')
                .attr('refY', '6')
                .attr('orient', 'auto');
            var arrow_path_right = 'M0,2 L8,6 L0,10 L4,6 L0,2';

            arrowMarkerS.append('path')
                .attr('d', arrow_path_right)
                .attr('fill', '#18a15f')
                .attr('stroke', '#18a15f');
            arrowMarkerF.append('path')
                .attr('d', arrow_path_right)
                .attr('fill', '#9f9f9f')
                .attr('stroke', '#9f9f9f');
            arrowMarkerE.append('path')
                .attr('d', arrow_path_right)
                .attr('fill', '#e42332')
                .attr('stroke', '#e42332');
            // 水波
            var symbol = that.defs.append('symbol')
                .attr('id', 'ware' + that.svgId);
            symbol.append('path')
                .attr('d', 'M 420 20 c 21.5 -0.4 38.8 -2.5 51.1 -4.5 c 13.4 -2.2 26.5 -5.2 27.3 -5.4 C 514 6.5 518 4.7 528.5 2.7 c 7.1 -1.3 17.9 -2.8 31.5 -2.7 c 0 0 0 0 0 0 v 20 H 420 Z');
            symbol.append('path')
                .attr('d', 'M 420 20 c -21.5 -0.4 -38.8 -2.5 -51.1 -4.5 c -13.4 -2.2 -26.5 -5.2 -27.3 -5.4 C 326 6.5 322 4.7 311.5 2.7 C 304.3 1.4 293.6 -0.1 280 0 c 0 0 0 0 0 0 v 20 H 420 Z');
            symbol.append('path')
                .attr('d', 'M 140 20 c 21.5 -0.4 38.8 -2.5 51.1 -4.5 c 13.4 -2.2 26.5 -5.2 27.3 -5.4 C 234 6.5 238 4.7 248.5 2.7 c 7.1 -1.3 17.9 -2.8 31.5 -2.7 c 0 0 0 0 0 0 v 20 H 140 Z');
            symbol.append('path')
                .attr('d', 'M 140 20 c -21.5 -0.4 -38.8 -2.5 -51.1 -4.5 c -13.4 -2.2 -26.5 -5.2 -27.3 -5.4 C 46 6.5 42 4.7 31.5 2.7 C 24.3 1.4 13.6 -0.1 0 0 c 0 0 0 0 0 0 l 0 20 H 140 Z');

            var clipPath1 = that.defs.append('clipPath')
                .attr('id', 'clipPath1' + that.svgId)
                .append('rect')
                .attr('x', 0.5)
                .attr('y', 0.5)
                .attr('ry', 6 - 0.5)
                .attr('rx', 6 - 0.5)
                .attr('width', that.options.nodeWidth - 1)
                .attr('height', that.options.nodeHeight - 1);
            var clipPath2 = that.defs.append('clipPath')
                .attr('id', 'clipPath2' + that.svgId)
                .append('rect')
                .attr('x', 0.5)
                .attr('y', 0.5)
                .attr('ry', 6 - 0.5)
                .attr('rx', 6 - 0.5)
                .attr('width', that.options.nodeWidth - 1)
                .attr('height', that.options.nodeHeight1 - 1);

            var clipPath3 = that.defs.append('clipPath')
                .attr('id', 'clipPath3' + that.svgId)
                .append('rect')
                .attr('x', 0.5)
                .attr('y', 0.5)
                .attr('ry', 6 - 0.5)
                .attr('rx', 6 - 0.5)
                .attr('width', that.options.nodeWidth - 1)
                .attr('height', that.options.nodeStatusBtnHeight + 6 - 1);
            var clipPath4 = that.defs.append('clipPath')
                .attr('id', 'clipPath4' + that.svgId)
                .append('rect')
                .attr('x', 0.5)
                .attr('y', 0.5)
                .attr('ry', 6 - 0.5)
                .attr('rx', 6 - 0.5)
                .attr('width', that.options.nodeWidth - 1)
                .attr('height', that.options.nodeStatusBtnHeight + that.options.nodeHeight + 6 - 1);

            var clipPath5 = that.defs.append('clipPath')
                .attr('id', 'clipPath5' + that.svgId)
                .append('rect')
                .attr('x', 0.5)
                .attr('y', 0.5)
                .attr('ry', 6 - 0.5)
                .attr('rx', 6 - 0.5)
                .attr('width', that.options.nodeWidth - 1)
                .attr('height', that.options.nodeStatusBtnHeight + that.options.nodeHeight1 + 6 - 1);

            var clipPath6 = that.defs.append('clipPath')
                .attr('id', 'clipPath6' + that.svgId)
                .append('rect')
                .attr('x', 0.5)
                .attr('y', 0.5)
                .attr('ry', 6 - 0.5)
                .attr('rx', 6 - 0.5)
                .attr('width', that.options.nodeWidth - 1)
                .attr('height', that.options.nodeStatusBtnHeight + that.options.nodeHeight - 1);
            var clipPath7 = that.defs.append('clipPath')
                .attr('id', 'clipPath7' + that.svgId)
                .append('rect')
                .attr('x', 0.5)
                .attr('y', 0.5)
                .attr('ry', 6 - 0.5)
                .attr('rx', 6 - 0.5)
                .attr('width', that.options.nodeWidth - 1)
                .attr('height', that.options.nodeStatusBtnHeight + that.options.nodeHeight1 - 1);
            var clipPath8 = that.defs.append('clipPath')
                .attr('id', 'clipPath8' + that.svgId)
                .append('rect')
                .attr('x', -8)
                .attr('y', 3)
                .attr('ry', 6 - 0.5)
                .attr('rx', 6 - 0.5)
                .attr('width', 18)
                .attr('height', 8);
            var clipPath9 = that.defs.append('clipPath')
                .attr('id', 'clipPath9' + that.svgId)
                .append('rect')
                .attr('x', -8)
                .attr('y', 3)
                // .attr('ry', 6 - 0.5)
                // .attr('rx', 6 - 0.5)
                .attr('width', 18)
                .attr('height', 8);
            // 添加图标
            var icon1 = that.defs.append('symbol')
                .attr('id', 'iconPaused' + that.svgId)
                .attr('viewBox', '0 0 1024 1024')
                .html(function () {
                    return `<path d="M346.8 791.8c-12.1 0-21.9-9.8-21.9-21.9V258.5c0-12.1 9.8-21.9 21.9-21.9s21.9 9.8 21.9 21.9v511.3c0 12.2-9.8 22-21.9 22zM675.9 791.8c-12.1 0-21.9-9.8-21.9-21.9V258.5c0-12.1 9.8-21.9 21.9-21.9s21.9 9.8 21.9 21.9v511.3c0.1 12.2-9.8 22-21.9 22z"  /><path d="M511.3 977.8c-62.6 0-123.3-12.3-180.5-36.4C275.6 918 226 884.6 183.5 842s-76-92.2-99.4-147.4c-24.2-57.2-36.4-117.9-36.4-180.5 0-62.6 12.3-123.3 36.4-180.5 23.4-55.2 56.8-104.8 99.4-147.4s92.2-76 147.4-99.4C388 62.7 448.7 50.5 511.3 50.5s123.3 12.3 180.5 36.4c55.2 23.4 104.8 56.8 147.4 99.4s76 92.2 99.4 147.4c24.2 57.2 36.4 117.9 36.4 180.5 0 62.6-12.3 123.3-36.4 180.5-23.4 55.2-56.8 104.8-99.4 147.4s-92.2 76-147.4 99.4c-57.2 24.1-117.9 36.3-180.5 36.3z m0-890.7c-235.5 0-427.1 191.6-427.1 427.1s191.6 427.1 427.1 427.1 427.1-191.6 427.1-427.1S746.8 87.1 511.3 87.1z"  />`;
                });
            var icon2 = that.defs.append('symbol')
                .attr('id', 'iconWait' + that.svgId)
                .attr('viewBox', '0 0 1024 1024')
                .html(function () {
                    return `<path d="M724.096 360.128l-185.6 175.936C538.112 536.384 537.6 536.512 537.216 536.832 526.912 547.968 510.208 551.808 495.36 544.64L325.248 445.632C307.2 436.928 299.52 415.68 307.904 398.464c8.384-17.344 29.888-24.448 47.808-15.616l150.528 87.68 170.432-161.664c14.656-13.568 37.248-13.184 50.368 1.024C739.968 323.968 738.88 346.432 724.096 360.128zM960 512c0 247.36-200.576 448-448 448s-448-200.64-448-448c0-247.424 200.576-448 448-448S960 264.576 960 512zM896.704 512c0-212.48-172.224-384.768-384.768-384.768S127.168 299.52 127.168 512s172.224 384.768 384.768 384.768S896.704 724.48 896.704 512z" />`;
                });
            var icon3 = that.defs.append('symbol')
                .attr('id', 'iconAborted' + that.svgId)
                .attr('viewBox', '0 0 1024 1024')
                .html(function () {
                    return `<path d="M512 64.7C265.3 64.7 64.7 265.3 64.7 512c0 118.8 46.6 226.8 122.5 307 0.6 0.7 1 1.4 1.7 2 0.4 0.4 0.8 0.7 1.3 1 81.4 84.5 195.6 137.3 322 137.3 246.7 0 447.3-200.7 447.3-447.3S758.7 64.7 512 64.7z m0 55.9c94.3 0 180.9 33.5 248.5 89.2L209.8 760.5c-55.7-67.6-89.2-154.2-89.2-248.5 0-215.8 175.6-391.4 391.4-391.4z m0 782.8c-101.5 0-194-38.8-263.7-102.4L801 248.3C864.6 318 903.4 410.5 903.4 512c0 215.8-175.6 391.4-391.4 391.4z" />`;
                });
            var icon4 = that.defs.append('symbol')
                .attr('id', 'iconLocked' + that.svgId)
                .attr('viewBox', '0 0 1024 1024')
                .html(function () {
                    return `<path d="M815.407407 381.402074V274.962963C815.407407 123.354074 679.291259 0 512 0S208.592593 123.354074 208.592593 274.962963v106.439111c-64.493037 11.112296-113.777778 67.318519-113.777778 134.959407v370.574223C94.814815 962.503111 156.311704 1024 231.898074 1024h560.203852C867.688296 1024 929.185185 962.503111 929.185185 886.916741V516.342519c0-67.621926-49.284741-123.828148-113.777778-134.940445zM246.518519 274.962963C246.518519 144.251259 365.605926 37.925926 512 37.925926s265.481481 106.325333 265.481481 237.037037V379.259259H246.518519v-104.296296z m644.74074 611.953778A99.271111 99.271111 0 0 1 792.101926 986.074074H231.898074A99.271111 99.271111 0 0 1 132.740741 886.916741V516.342519A99.271111 99.271111 0 0 1 231.898074 417.185185h560.203852A99.271111 99.271111 0 0 1 891.259259 516.342519v370.574222z"  /><path d="M512 530.962963c-41.832296 0-75.851852 34.019556-75.851852 75.851852v113.777778c0 41.832296 34.019556 75.851852 75.851852 75.851851s75.851852-34.019556 75.851852-75.851851v-113.777778c0-41.832296-34.019556-75.851852-75.851852-75.851852z m37.925926 189.62963c0 20.916148-17.009778 37.925926-37.925926 37.925926s-37.925926-17.009778-37.925926-37.925926v-113.777778c0-20.916148 17.009778-37.925926 37.925926-37.925926s37.925926 17.009778 37.925926 37.925926v113.777778z"  />`;
                });
            var icon5 = that.defs.append('symbol')
                .attr('id', 'iconEdit' + that.svgId)
                .attr('viewBox', '0 0 1024 1024')
                .html(function () {
                    return `<path d="M512 0c-281.6 0-512 230.4-512 512 0 281.6 230.4 512 512 512 281.6 0 512-230.4 512-512C1024 230.4 793.6 0 512 0zM268.8 537.6l320-320 89.6 89.6-320 320-89.6 0L268.8 537.6zM768 768l-512 0 0-64 512 0L768 768z" p-id="1727" data-spm-anchor-id="a313x.7781069.0.i2" class="selected"></path>`;
                    //return `<path d="M851.924923 394.452817 701.265598 207.521381c-5.54632-6.881734-15.398712-7.786337-22.016433-2.009773L266.585591 565.683666c-1.89721 1.653663-3.387144 3.751441-4.355191 6.123464l-91.602278 225.288023c-4.915963 12.089339 5.257747 24.955368 17.609053 22.249747l232.283344-50.854225c2.51324-0.555655 4.858658-1.736551 6.825452-3.455705L851.924923 394.452817 851.924923 394.452817 851.924923 394.452817zM200.577225 789.746792l73.473385-164.5548c4.819772-10.799974 18.794042-12.722766 26.145473-3.594875l79.157851 98.229209c7.43432 9.228175 2.992147 23.362081-8.241708 26.240641l-170.535001 43.675732L200.577225 789.746792 200.577225 789.746792 200.577225 789.746792zM424.705841 724.876342 314.230728 587.781964c-5.545296-6.878664-4.676509-17.139356 1.933025-22.910802l263.244496-229.764987c6.614651-5.776563 16.476253-4.877077 22.020526 2.001587l100.42727 124.631532c5.54939 6.886851 4.685719 17.144472-1.928932 22.914896L424.705841 724.876342 424.705841 724.876342 424.705841 724.876342zM615.368252 303.727513l59.986209-52.360532c6.617721-5.780657 16.47523-4.871961 22.024619 2.00568l100.431364 124.636648c5.545296 6.877641 4.685719 17.138332-1.932002 22.909779l-48.013527 41.903366c-6.618744 5.776563-16.476253 4.877077-22.020526-2.00568L615.368252 303.727513 615.368252 303.727513 615.368252 303.727513zM422.179298 620.945271l191.799304-167.418011c6.617721-5.76633 7.478322-16.022928 1.927909-22.909779l0 0c-5.545296-6.887874-15.406898-7.78122-22.01541-2.006703l-191.799304 167.412894c-6.618744 5.76633-7.482415 16.028045-1.938142 22.905686l0 0.004093C405.699975 625.816209 415.561577 626.719788 422.179298 620.945271L422.179298 620.945271zM422.179298 620.945271" />`
                });
            var icon6 = that.defs.append('symbol')
                .attr('id', 'iconDelete' + that.svgId)
                .attr('viewBox', '0 0 1024 1024')
                .html(function () {
                    return `<path d="M512 992C246.896 992 32 777.104 32 512 32 246.896 246.896 32 512 32c265.104 0 480 214.896 480 480 0 265.104-214.896 480-480 480z m216.832-696.832c-12.128-12.128-31.808-12.128-43.936 0l-172 172-172.032-172.032c-12.08-12.08-31.664-12.08-43.744 0-12.064 12.08-12.064 31.664 0 43.744l172.048 172.032L297.104 682.96c-12.128 12.128-12.128 31.792 0 43.936 12.144 12.128 31.808 12.128 43.936 0l172.048-172.064L683.2 724.944c12.08 12.064 31.648 12.064 43.728 0 12.08-12.08 12.08-31.664 0-43.744L556.832 511.104l172-172c12.128-12.128 12.128-31.808 0-43.936z" p-id="1948"></path>`;
                });

            var filterEffect = this.defs
                .append('filter')
                .attr('x', '-100%')
                .attr('y', '-100%')
                .attr('width', '300%')
                .attr('height', '300%')
                .attr('id', `filter${this.svgId}`)
                .html(function () {
                    var html = `<feOffset result="offOut" in="SourceGraphic" dx="2" dy="2" />
                      <feGaussianBlur result="blurOut" in="offOut" stdDeviation="5" />
                      <feBlend in="SourceGraphic" in2="blurOut" mode="normal" />`;
                    return html;
                });
            var filterEffect1 = this.defs
                .append('filter')
                .attr('x', '-100%')
                .attr('y', '-100%')
                .attr('width', '300%')
                .attr('height', '300%')
                .attr('id', `filter1${this.svgId}`)
                .html(function () {
                    var html = `<feOffset result="offOut" in="SourceGraphic" dx="2" dy="2" />
                      <feColorMatrix result = "matrixOut" in = "offOut" type = "matrix" values = "0.2 0 0 0 0 0 0.2 0 0 0 0 0 0.2 0 0 0 0 0 0.6 0"/>
                      <feGaussianBlur result="blurOut" in="matrixOut" stdDeviation="5" />
                      <feBlend in="SourceGraphic" in2="blurOut" mode="normal" />`;
                    return html;
                });
        },
        getArrowColor: function (color) {
            var that = this;
            color = d3.color(color).toString();
            var arrowMarker = that.defs.selectAll('marker.arrow').filter(function (d) {
                return d === color;
            });
            if (arrowMarker.size()) return arrowMarker.attr('id');
            else {
                var colorId = that.createId(6);
                var arrow_path_right = 'M0,2 L8,6 L0,10 L4,6 L0,2';
                var arrowMarkerS = that.defs.append('marker').datum(color)
                    .attr('class', 'arrow')
                    .attr('id', `arrow${this.svgId}-${colorId}`)
                    .attr('markerUnits', 'strokeWidth')
                    .attr('markerWidth', '12')
                    .attr('markerHeight', '12')
                    .attr('viewBox', '0 0 12 12')
                    .attr('refX', '8')
                    .attr('refY', '6')
                    .attr('orient', 'auto');
                arrowMarkerS.append('path')
                    .attr('d', arrow_path_right)
                    .attr('fill', color)
                    .attr('stroke', color);
                return `arrow${this.svgId}-${colorId}`;
            }
        },
        getNodeFilter: function (path) {
            var that = this;
            if (!path) return false;
            var pathStr = path;
            var filterClip = that.defs.selectAll('clipPath.clipPath').filter(function (d) {
                return d === pathStr;
            });
            if (filterClip.size()) return filterClip.attr('id');
            else {
                var filterId = that.createId(6);
                var clipPath = that.defs.append('clipPath').datum(pathStr)
                    .attr('id', 'clipPath' + that.svgId + '-' + filterId)
                    .attr('class', 'clipPath')
                    .append('path')
                    .attr('d', pathStr);
                return 'clipPath' + that.svgId + '-' + filterId;
            }
        },
        dispatchFn: function () {
            var that = this;
            // 添加画布地dispatch
            this.dispatch.on('svg.click', function () {
                that.dispatch.call('link.activeRemove', that);
            });
            // 添加link事件
            this.dispatch.on('link.activeRemove', function () {
                that.linksG
                    .selectAll('g.active').classed('active', false)
                    .selectAll('.v-line').attr('stroke-width', 1);
            });
        },
        zoomFn: function () {
            var that = this;
            var startX = 0, startY = 0;
            if (!this.zoom) {
                this.zoom = d3.zoom()
                    .scaleExtent(that.options.zoomExtent)
                    .filter(function () {
                        return d3.event.type !== 'dblclick';
                    })
                    .on('start', function () {
                        startX = d3.event.transform.x;
                        startY = d3.event.transform.y;
                    })
                    .on('zoom', function () {
                        // var dx = d3.event.transform.x - startX;
                        // var dy = d3.event.transform.y - startY;
                        var x = d3.event.transform.x;
                        var y = d3.event.transform.y;
                        var k = d3.event.transform.k;
                        that.zoomG
                            .attr('transform', `translate(${x},${y})scale(${k})`);
                        that.tooltipDiv
                            .style('transform', `translate(${x}px,${y}px)scale(${k})`);
                    })
                    .on('end', function () {

                    });
            }
            this.svg.call(this.zoom);
        },
        getPlaceholderLen: function (label) {
            if (!label && typeof label !== 'string') return false;
            var Eleng = label.replace(/[\u4e00-\u9fa5]/gi, '').length;
            return (label.length * 2 - Eleng) * 7;
        },
        getStringChartLen: function (text) {
            if (typeof text !== 'string') return false;
            return text.length * 2 - text.replace(/[\u4e00-\u9fa5]/gi, '').length;
        },
        getTextLabelLen: function (data) {
            var that = this;
            if (!this.defs) return false;
			var textBBox = this.calculateText;
            if (!data) return false;
            if (data.fontSize !== undefined) textBBox.attr('font-size', data.fontSize);
            else textBBox.attr('font-size', null);
            if (data.fontFamily !== undefined) textBBox.attr('font-family', data.fontFamily);
            else textBBox.attr('font-family', null);
            var getbbox = textBBox.text(data.text).node().getBBox();
            data.width = getbbox.width;
            data.height = getbbox.height;
            return data;
        },
        getCutStr: function (str, len) {
            var str_length = 0;
            var str_len = str.length;
            var str_cut = new String();
            for (var i = 0; i < str_len; i++) {
                var a = str.charAt(i);
                str_length++;
                if (escape(a).length > 4) {
                    //中文字符的长度经编码之后大于4
                    str_length++;
                }
                str_cut = str_cut.concat(a);
                if (str_length >= len) {
                    str_cut = str_cut.concat('...');
                    return str_cut;
                }
            }
            //如果给定字符串小于指定长度，则返回源字符串；
            if (str_length < len) {
                return str;
            }
        },
        dragFn: function () {
            var that = this;
            // 节点地拖动处理逻辑
            var nodesData;
            var alignData = {
                horizon: '', // 水平
                vertical: '', // 垂直
                mark: true
            };
            var alignLine = this.assistG.selectAll('.alignLine').data(['level', 'vertical']);
            var alignLineEnter = alignLine.enter().append('path').attr('class', 'alignLine').attr('stroke-dasharray', '2 3').attr('stroke', '#ddd').attr('fill', 'none');
            alignLine = alignLine.merge(alignLineEnter);
            if (!this.drag) {
                this.drag = d3.drag()
                    .on('start', function () {
                        // that.svg.dispatch('click');
                        nodesData = that.getAllNodeData();
                    })
                    .on('drag', function (d) {
                        if (alignData.mark) {
                            setTimeout(function () {
                                that.nodeAlignLine(d, nodesData, alignData, alignLine);
                            }, 0);
                        }
                        d.x = d.x + d3.event.dx;
                        d.y = d.y + d3.event.dy;
                        d3.select(this).attr('transform', `translate(${d.x},${d.y})`);
                        var linkG = that.getAllLinkGsByNodeG(this);
                        linkG.dispatch('update');
                    })
                    .on('end', function (d) {
                        // 对齐
                        if (alignData.horizon || alignData.vertical) {
                            if (alignData.horizon) {
                                d.y = alignData.horizon - d.height / 2;
                            }
                            if (alignData.vertical) {
                                d.x = alignData.vertical - d.width / 2;
                            }
                            d3.select(this).attr('transform', `translate(${d.x},${d.y})`);
                            var linkG = that.getAllLinkGsByNodeG(this);
                            linkG.dispatch('update');
                            alignData.horizon = '';
                            alignData.vertical = '';
                            setTimeout(function () {
                                alignLine.attr('d', '');
                            }, 0);
                        }
                    });
            }
            // 线label drag
            if (!this.linkLabelDrag) {
                // this.linkLabelDrag = d3.drag()
            }
            // 节点锚点地拖动处理逻辑
            if (!this.anchorDrag) {
                var anchorConnectLine = this.assistG
                    .attr('stroke', '#9f9f9f').attr('stroke-width', 1).attr('fill', 'none')
                    .selectAll('.anchorConnectLine').data(['anchorConnectLine']);
                anchorConnectLine.enter().append('path').attr('class', 'anchorConnectLine');
                anchorConnectLine.exit().remove();
                // var anchorCircle = this.assistG
                //     .attr('stroke', '#8c8c8c').attr('stroke-width', 1).attr('fill', 'none')
                //     .selectAll('.anchorConnectLine').data(['anchorConnectLine']);
                // anchorConnectLine.enter().append('circle').attr('class', 'anchorConnectLine');
                // anchorConnectLine.exit().remove();
                var line = d3.line()
                    .x(function (d) {
                        return d.x;
                    })
                    .y(function (d) {
                        return d.y;
                    });
                var start, parentData, end, endCircle, mousePos, offset = 2, allNodeData, alignValue = {};
                this.anchorDrag = d3.drag()
                    .on('start', function (d) {
                        anchorConnectLine = that.assistG.selectAll('.anchorConnectLine');
                        allNodeData = that.getAllNodeData();
                        if (d.select.length) {
                            var parentG = that.getParents(this, 'nodeG');
                            parentData = d3.select(parentG).datum();
                            start = {x: parentData.x, y: parentData.y};
                            parentData.tmp.x = parentData.x;
                            parentData.tmp.y = parentData.y;
                        } else start = that.getAnchorPointPosition(this);
                    })
                    .on('drag', function (d) {

                        if (d.select.length) {
                            start.x = start.x + d3.event.dx;
                            start.y = start.y + d3.event.dy;
                            d.select.forEach(function (d1) {
                                var link = d3.select(d1);
                                // console.log(parentData);
                                parentData.x = start.x;
                                parentData.y = start.y;
                                link.dispatch('update')
                            })
                        } else {
                            mousePos = d3.mouse(that.zoomG.node());
                            end = {
                                x: mousePos[0],
                                y: mousePos[1]
                            };
                            if (start.x > end.x) {
                                end.x = end.x + offset;
                            } else if (start.x < end.x) {
                                end.x = end.x - offset;
                            }
                            if (start.y > end.y) {
                                end.y = end.y + offset;
                            } else if (start.y < end.y) {
                                end.y = end.y - offset;
                            }
                            anchorConnectLine.attr('d', line([start, end]));
                        }
                    })
                    .on('end', function (d) {
                        var anchor = d3.select(this);
                        var _this = this;
                        var anchorActive = that.nodesG.selectAll('.anchor-point.active');
                        if (d.select.length) {
                            parentData.x = parentData.tmp.x;
                            parentData.y = parentData.tmp.y;
                            // var anchorActiveParentData = d3.select();
                            if (anchorActive.size()) {
                                var endNode = that.getParents(anchorActive.node(), 'nodeG');
                                var endNodeD = d3.select(endNode).datum();
                                var anData = anchorActive.datum();

                                var tmpArr = d.select.map(function (d) { return d; });
                                tmpArr.forEach(function (ele) {
                                    var link = d3.select(ele);
                                    var linkD = link.datum();
                                    var isOut = false, isIn = false;

                                    if (_this === anchorActive.node()) { // 同一个
                                        link.dispatch('update');
                                        return false;
                                    }

                                    if (linkD.data.from === parentData.id) { // 始端
                                        isOut = true;
                                    } else if(linkD.data.to === parentData.id) { // 末端
                                        isIn = true;
                                    }
                                    var linkData1;
                                    if (isOut || isIn) {
                                        if (isOut) {
                                            linkData1 = {
                                                id: linkD.id,
                                                from: endNodeD.id,
                                                to: linkD.target.id,
                                                out: anData.id,
                                                in: linkD.data.in
                                            }
                                        } else if (isIn) {
                                            linkData1 = {
                                                id: linkD.id,
                                                from: linkD.source.id,
                                                to: endNodeD.id,
                                                out: linkD.data.out,
                                                in: anData.id
                                            }
                                        }
                                        var is = that.drawLinkOperate(linkData1, 'switch');
                                        if (!is) {
                                            link.dispatch('update');
                                            return false;
                                        }
                                    }
                                    var isChange = false;
                                    var original = Object.assign({}, linkD);
                                    if (linkData1 && linkD.data && ((linkD.data.from !== linkData1.from) || (linkD.data.to !== linkData1.to))) isChange = true;
                                    if (isOut) {
                                        linkD.out = anData.data;
                                        linkD.data.out = linkD.out.id;
                                        linkD.source = endNodeD;
                                        linkD.data.from = endNodeD.id;
                                    } else if (isIn) {
                                        linkD.in = anData.data;
                                        linkD.data.in = linkD.in.id;
                                        linkD.target = endNodeD;
                                        linkD.data.to = endNodeD.id;
                                    } else return false;

                                    var index = d.select.indexOf(ele);
                                    if (index !== -1) d.select.splice(index, 1);
                                    if (isOut) {
                                        var allLinks = that.getAllLinkData();
                                        var is1 = allLinks.some(function (d1) {
                                            return (d1.id !== linkD.id) && (d1.source === original.source) && (d1.out.position === original.out.position);
                                        });
                                        if (!is1) {
                                            anchor.dispatch('style').dispatch('hide', {detail: {show: false}});
                                            anchorActive.attr('stroke', 'rgb(0, 102, 153)')
                                                .dispatch('show', {detail: {show: true}});
                                        } else {
                                            anchor.dispatch('style').dispatch('hide');
                                            anchorActive.attr('stroke', 'rgb(0, 102, 153)').dispatch('show');
                                        }
                                    } else {
                                        anchor.dispatch('style').dispatch('hide');
                                        anchorActive.attr('stroke', 'rgb(0, 102, 153)').dispatch('show');
                                    }
                                    if (isChange) {
                                        delete linkD.data.userData;
                                        linkD.label = {};
                                        linkD.onEdit = linkD.source.data.onLinkEdit;
                                        linkD.onDelete = linkD.source.data.onLinkDelete;
                                        that.appendLinkLabel(link);
                                    }
                                    link.dispatch('update').dispatch('selected');
                                    // console.log(linkD);
                                });
                            } else {
                                d.select.forEach(function (ele) {
                                    var link = d3.select(ele);
                                    link.dispatch('update')
                                })
                            }
                            return false;
                        }

                        if (anchorActive.size()) {
                            endCircle = anchorActive.node();
                            var startNodeG = that.getParents(this, 'nodeG');
                            var endNodeG = that.getParents(endCircle, 'nodeG');
                            if (startNodeG === endNodeG) {
                                // console.log('起始节点和结束节点相同！');
                                anchorConnectLine.attr('d', '');
                                return false;
                            } else {
                                var currDom = d3.select(this);
                                var startNodeData = d3.select(startNodeG).datum();
                                var endNodeData = d3.select(endNodeG).datum();
                                var startData = currDom.datum();
                                var endData = d3.select(endCircle).datum();

                                end = that.getAnchorPointPosition(endCircle);
                                anchorConnectLine.attr('d', line([start, end]));
                                var linkData = {
                                    id: that.createId(that.options.uuidmode),
                                    from: startNodeData.id,
                                    to: endNodeData.id,
                                    out: startData.id,
                                    in: endData.id
                                };
                                var is = that.drawLinkOperate(linkData);
                                if (is) {
                                    currDom.dispatch('show');
                                }
                                anchorConnectLine.attr('d', '');
                            }
                        } else {
                            anchorConnectLine.attr('d', '');
                        }
                    });
            }
            // 线条上的锚点拖动处理逻辑
            if (!this.linkDrag) {
                var minDis = that.options.anchorPathMinLength;
                var prevData, nextData, firstData, lastData, linkG;
                this.linkDrag = d3.drag()
                    .on('start', function (d) {
                        linkG = that.selectAll(that.getParents(this, 'linkG'));
                        var path = linkG.datum();
                        if (path) path = path.path;
                        firstData = path[0];
                        lastData = path[path.length - 1];
                        var index = path.indexOf(d);
                        prevData = path[index - 1];
                        nextData = path[index + 1];
                        if ((prevData.x0 === prevData.x1) && (prevData.y0 === prevData.y1)) {
                            prevData = path[index - 2];
                        }
                        if ((nextData.x0 === nextData.x1) && (nextData.y0 === nextData.y1)) {
                            nextData = path[index + 2];
                        }
                    })
                    .on('drag', function (d) {
                        // console.log('link drag');
                        var event = d3.event;
                        if (d.x0 === d.x1) { // 垂直
                            if (prevData.y0 === prevData.y1 && nextData.y0 === nextData.y1) {
                                d.x1 += event.dx;
                                if (prevData === firstData) {
                                    if ((firstData.dir === -2) && (d.x1 > prevData.x0 - minDis)) {
                                        d.x1 = prevData.x0 - minDis;
                                    } else if ((firstData.dir === 2) && (d.x1 < prevData.x0 + minDis)) {
                                        d.x1 = prevData.x0 + minDis;
                                    }
                                } else if ((prevData.y0 === prevData.y1) && (firstData.y0 === firstData.y1) && (prevData.y0 === firstData.y1)) {
                                    if (((firstData.dir === -2) && (d.x1 > prevData.x0)) || ((firstData.dir === 2) && (d.x1 < prevData.x0))) {
                                        d.x1 = prevData.x0;
                                    }
                                }
                                if (nextData === lastData) {
                                    if ((lastData.dir === -2) && (d.x1 > nextData.x1 - minDis)) {
                                        d.x1 = nextData.x1 - minDis;
                                    } else if ((lastData.dir === 2) && (d.x1 < nextData.x1 + minDis)) {
                                        d.x1 = nextData.x1 + minDis;
                                    }
                                } else if ((lastData.y0 === lastData.y1) && (nextData.y0 === nextData.y1) && (lastData.y0 === nextData.y1)) {
                                    if (((lastData.dir === -2) && (d.x1 > nextData.x1)) || ((lastData.dir === 2) && (d.x1 < nextData.x1))) {
                                        d.x1 = nextData.x0;
                                    }
                                }
                                d.x0 = d.x1;
                                prevData.x1 = d.x0;
                                nextData.x0 = d.x0;
                                // d.fixed = ['x', d.x0];
                                linkG.dispatch('update', {detail: {drag: true}});
                            }
                        } else if (d.y0 === d.y1) { // 水平
                            if (prevData.x0 === prevData.x1 && nextData.x0 === nextData.x1) {
                                d.y1 += event.dy;
                                if (prevData === firstData) {
                                    if ((firstData.dir === -1) && (d.y1 > prevData.y0 - minDis)) {
                                        d.y1 = prevData.y0 - minDis;
                                    } else if ((firstData.dir === 1) && (d.y1 < prevData.y0 + minDis)) {
                                        d.y1 = prevData.y0 + minDis;
                                    }
                                } else if ((prevData.x0 === prevData.x1) && (firstData.x0 === firstData.x1) && (prevData.x0 === firstData.x1)) {
                                    if (((firstData.dir === -1) && (d.y1 > prevData.y0)) || ((firstData.dir === 1) && (d.y1 < prevData.y0))) {
                                        d.y1 = prevData.y0;
                                    }
                                }
                                if (nextData === lastData) {
                                    if ((lastData.dir === -1) && (d.y1 > nextData.y1 - minDis)) {
                                        d.y1 = nextData.y1 - minDis;
                                    } else if ((lastData.dir === 1) && (d.y1 < nextData.y1 + minDis)) {
                                        d.y1 = nextData.y1 + minDis;
                                    }
                                } else if ((lastData.x0 === lastData.x1) && (nextData.x0 === nextData.x1) && (lastData.x0 === nextData.x1)) {
                                    if (((lastData.dir === -1) && (d.y1 > nextData.y1)) || ((lastData.dir === 1) && (d.y1 < nextData.y1))) {
                                        d.y1 = nextData.y0;
                                    }
                                }
                                d.y0 = d.y1;
                                prevData.y1 = d.y0;
                                nextData.y0 = d.y1;
                                // d.fixed = ['y', d.y0];
                                linkG.dispatch('update', {detail: {drag: true}});
                            }
                        }
                    })
                    .on('end', function () {
                        // console.log('drag end');
                    });
            }
        },
        getParents: function (ele, classname) {
            var parent = ele.parentNode;
            if (ele === document.documentElement) return undefined;
            if (d3.select(parent).classed(classname)) return parent;
            else return this.getParents(parent, classname);
        },
        getNodeObject: function (ele) {
            if (!ele) return false;
            if (typeof ele.node === 'function') ele = ele.node();
            if (ele && ele.nodeType !== 1) return false;
            var o = new this.NODE(this);
            o.node = ele;
            if (this.canvas) o.svg = this.canvas;
            return o;
        },
        getLinkObject: function (ele) {
            if (!ele) return false;
            if (typeof ele.node === 'function') ele = ele.node();
            if (ele && ele.nodeType !== 1) return false;
            var o = new this.LINK(this);
            o.link = ele;
            if (this.canvas) o.svg = this.canvas;
            return o;
        },
        createId: function (len, radix) {
            var chars = '0123456789abcdefghijklmnopqrstuvwxyz'.split('');
            var uuid = [], i;
            radix = radix || chars.length;

            if (len) {
                // Compact form
                for (i = 0; i < len; i++) uuid[i] = chars[0 | Math.random() * radix];
            } else {
                // rfc4122, version 4 form
                var r;
                // rfc4122 requires these characters
                uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
                uuid[14] = '4';

                // Fill in random data.  At i==19 set the high bits of clock sequence as
                // per rfc4122, sec. 4.1.5
                for (i = 0; i < 36; i++) {
                    if (!uuid[i]) {
                        r = 0 | Math.random() * 16;
                        uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
                    }
                }
            }
            var uuidStr = uuid.join('');
            if (this.include(this.uuidArr, uuidStr)) return this.createId(len, radix);
            this.uuidArr.add(uuidStr);
            return uuidStr;
        },
        getAllNodeData: function () {
            return this.nodesG.selectAll('.nodeG').data();
        },
        getAllLinkData: function () {
            return this.linksG.selectAll('.linkG').data();
        },
        isConnectStart: function (node) {
            var that = this;
            if (!node) return false;
            if (node.nodeType === 1) node = this.selectAll(node);
            var nodeData = node.datum();

            if (nodeData.data) {
                if (nodeData.isStart) return true;
                var linkDatas = this.getAllLinkData(),
                    prevLinkDatas = this.getLinksByTo(nodeData.data.id).data(),
                    pathNode = [], //防死循环
                    b = false;
                prevLinkDatas.forEach(function (d) {
                    pathNode = [];
                    findStart(d);
                });

                function findStart(linkData) {
                    // if (pathNode.includes(linkData) || b) return false;
                    if (that.include(pathNode, linkData) || b) return false;
                    else pathNode.push(linkData);
                    if (linkData.source.isStart) {
                        b = true;
                        return false;
                    } else {
                        var prev = linkDatas.filter(function (d1) {
                            return d1.target === linkData.source;
                        });
                        if (prev.length) {
                            prev.forEach(function (d) {
                                findStart(d);
                            });
                        }
                    }
                }

                return b;
            }
        },
        isConnectEnd: function (node) {
            var that = this;
            if (!node) return false;
            if (node.nodeType === 1) node = this.selectAll(node);
            var nodeData = node.datum();
            if (nodeData.data) {
                if (nodeData.isEnd) return true;
                var linkDatas = this.getAllLinkData(),
                    prevLinkDatas = this.getLinksByFrom(nodeData.data.id).data(),
                    pathNode = [], //防死循环
                    b = false;
                prevLinkDatas.forEach(function (d) {
                    pathNode = [];
                    findStart(d);
                });

                function findStart(linkData) {
                    // if (pathNode.includes(linkData) || b) return false;
                    if (that.include(pathNode, linkData) || b) return false;
                    else pathNode.push(linkData);
                    if (linkData.target.isEnd) {
                        b = true;
                        return false;
                    } else {
                        var prev = linkDatas.filter(function (d1) {
                            return d1.source === linkData.target;
                        });
                        if (prev.length) {
                            prev.forEach(function (d) {
                                findStart(d);
                            });
                        }
                    }
                }

                return b;
            }
        },
        selectAll: function (ele) {
            if (ele.nodeType === 1) {
                if (Array.isArray(ele)) return d3.selectAll(ele);
                else return d3.select(ele);
            } else return ele;
        },
        dataComparison: function (obj, obj1) {
            var isArr = [];
            if (typeof obj === 'object' && !Array.isArray(obj)) {
                obj = [obj];
            }
            if (typeof obj1 === 'object' && !Array.isArray(obj1)) {
                obj1 = [obj1];
            }
            if (Array.isArray(obj) && Array.isArray(obj1) && (obj.length === obj1.length)) {
                obj.forEach(function (d, i) {
                    var keys = Object.keys(d);
                    var is = keys.filter(function (key) {
                        var value = d[key];
                        return typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean';
                    }).every(function (key) {
                        var value = d[key];
                        var value1 = obj1[i][key];
                        return value === value1;
                    });
                    isArr.push(is);
                });
            } else return false;
            return isArr.every(function (d) {
                return d;
            });
        },
        consoleLog: function (vb, text) {
            if (!vb) {
                console.log(text);
                return true;
            } else return false;
        },
    });
    TsFlowChart.prototype.init.prototype = TsFlowChart.prototype;
    window.TsFlowChart = TsFlowChart;
    if (typeof window.flowChartNode !== "object") window.flowChartNode = {};
})(window);