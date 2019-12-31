;(function (window) {
    // 对外的接口
    var TsFlowChart = window.TsFlowChart || {};

    TsFlowChart.extend({
        SVG: function (that) {
            this.that = that;
            this.svg = that.svg.node();
        },
        NODE: function (that) {
            this.that = that;
        },
        LINK: function (that) {
            this.that = that;
        }
    });

    TsFlowChart.SVG.prototype = {
        getElementByType: function (type) {
            var that = this.that;
            var nodeG = that.getNodesByType(type);
            var oNode = new that.NODE(that);
            oNode.node = nodeG.node();
            if (oNode.node) return oNode;
            else return oNode.node;
        },
        getAllNode: function (type) {
            var that = this.that;
            var nodeG;
            if (!type) nodeG = that.nodesG.selectAll('.nodeG');
            else nodeG = that.getNodesByType(type);
            var nodes = [];
            if (nodeG && nodeG.size()) {
                nodeG.each(function () {
                    nodes.push(that.getNodeObject(this));
                });
            }
            return nodes;
        },
        fromJson: function (json) {
            var that = this.that;
            that.fromJson(json);
            return this;
        },
        toJson: function () {
            var that = this.that;
            var json = that.toJson();
            return json;
        },
		setUserData: function (json) {
            this.that && (this.that.userData = json);
			return this;
		},
		getUserData: function () {
            return this.that && this.that.userData;
		},
        setData: function (key, value) {
            if (!this.userData) this.userData = {};
            this.userData[key] = value;
        },
        getData: function (key) {
            if (this.userData) {
                return this.userData[key];
            }
        },
        setStatus: function (uid, status, progress) { // 设置节点状态
            var that = this.that;
            if (!uid || !status) return this;
            var node = that.getNodeByUid(uid);
            if (node) {
                switch (status) {
                    case 'succeed': // 成功
                    case 'done':
                        node.dispatch('executed');
                        break;
                    case 'running': // 运行中
                    case 'doing':
                        node.dispatch('runningSuccess');
                        break;
                    case 'paused': // 暂停
                        node.dispatch('paused');
                        break;
                    case 'failed': // 失败
                        if (progress >= 0 && progress <= 1) node.dispatch('runningFailure', {detail: {progress: progress}});
                        else node.dispatch('runningFailure');
                        break;
                    case 'waitconfirm': // 等待确认
                        node.dispatch('waitConfirm');
                        break;
                    case 'aborted': // 终止
                        node.dispatch('aborted');
                        break;
                    case 'pending': // 未执行
                    case 'undo':
                        node.dispatch('nonexecution');
                        break;
                    case 'locked': // 锁定
                        node.dispatch('locked');
                        break;
                    default:
                        break;
                }
            }
            return this;
        },
        valid: function () {
            // console.log('svg Valid!');
            var that = this.that;
            return that.valid();
        },
        addNode: function (json) {
            var that = this.that;
            if (!json) return false;
            var nodeG = that.createNode(json);
            if (nodeG.size()) {
                return that.getNodeObject(nodeG.node());
            }
            return this;
        },
        getNodeById: function (uid) {
            var that = this.that;
            if (!uid) return false;
            var node = that.getNodeByUid(uid);
            var oEle = that.getNodeObject(node);
            return oEle;
        },
        getPathById: function (uid) {
            var that = this.that;
            if (!uid) return false;
            var link = that.getLinkByUid(uid);
            var oEle = that.getLinkObject(link);
            return oEle;
        },
        extend: function (obj) {
            for (var attr in obj) {
                this[attr] = obj[attr];
            }
            return this;
        }
    };

    TsFlowChart.NODE.prototype = {
        getPrevNode: function () {
            // console.log('node getPrevNode!');
            var that = this.that;
            var nodeG = that.getPrevNodes(this.node);
            var nodes = [];
            if (nodeG) {
                nodeG.each(function () {
                    nodes.push(that.getNodeObject(this));
                });
            }
            return nodes;
        },
        getSvg: function () {
            return this.svg;
        },
        getPaper: function () {
            return this.svg;
        },
        getId: function () {
            if (!this.node || (this.node && this.node.nodeType !== 1)) return null;
            return d3.select(this.node).datum().id;
        },
        getNextNode: function () {
            // console.log('node getNextNode!');
            var that = this.that;
            var nodeG = that.getNextNodes(this.node);
            var nodes = [];
            if (nodeG) {
                nodeG.each(function () {
                    nodes.push(that.getNodeObject(this));
                });
            }
            return nodes;
        },
        getAllPrevNode: function (type) {
            // console.log('node getAllPrevNode!');
            var that = this.that;
            if (typeof type !== 'string') return false;
            var nodes = [];
            if (this.node && type) {
                var nodeG = that.getAllPrevTypeNode(this.node, type);
                if (nodeG && typeof nodeG.node === 'function' && nodeG.size()) {
                    nodeG.each(function () {
                        nodes.push(that.getNodeObject(this));
                    });
                }
            }
            return nodes;
        },
        getAllNextNode: function (type) {
            // console.log('node getAllNextNode!');
            var that = this.that;
            if (typeof type !== 'string') return false;
            var nodes = [];
            if (this.node && type) {
                var nodeG = that.getAllNextTypeNode(this.node, type);
                if (nodeG && typeof nodeG.node === 'function' && nodeG.size()) {
                    nodeG.each(function () {
                        nodes.push(that.getNodeObject(this));
                    });
                }
            }
            return nodes;
        },
        getPrevPath: function () {
            // console.log('node getPrevPath!');
            var that = this.that;
            var link = [];
            if (this.node) {
                var nodeId = d3.select(this.node).datum().id;
                var linkG = that.getLinksByTo(nodeId);
                if (linkG && linkG.size()) {
                    linkG.each(function () {
                        var oLink = that.getLinkObject(this);
                        link.push(oLink);
                    });
                }
            }
            return link;
        },
        getNextPath: function () {
            // console.log('node getNextPath!');
            var that = this.that;
            var link = [];
            if (this.node) {
                var nodeId = d3.select(this.node).datum().id;
                var linkG = that.getLinksByFrom(nodeId);
                if (linkG && linkG.size()) {
                    linkG.each(function () {
                        var oLink = that.getLinkObject(this);
                        link.push(oLink);
                    });
                }
            }
            return link;
        },
        disabled: function () {
            // console.log('node Disabled!');
            var that = this.that;
            var nodeData = d3.select(this.node).data()[0];
            if (nodeData) {
                nodeData.behavior.editable = false;
                that.appendNodeBtn(this.node);
            }
            return this;
        },
        hide: function () {
            // console.log('node Hide!');
            var that = this.that;
            if (this.node) {
                d3.select(this.node).dispatch('hide');
            }
            return this;
        },
        show: function () {
            // console.log('node Show!');
            var that = this.that;
            if (this.node) {
                d3.select(this.node).dispatch('show');
            }
            return this;
        },
        isConnectStart: function () {
            // console.log('node isConnectStart!');
            var that = this.that;
            var b = that.isConnectStart(this.node);
            return b;
        },
        isConnectEnd: function () {
            // console.log('node isConnectEnd!');
            var that = this.that;
            var b = that.isConnectEnd(this.node);
            return b;
        },
        setUserData: function (json) {
            // console.log('node setUserData!');
            var that = this.that;
            if (!json || typeof json !== 'object') return false;
            var nodeData = d3.select(this.node).data()[0];
            if (nodeData) {
                nodeData.data.userData = json;
            }
            return this;
        },
        getUserData: function () {
            // console.log('node getUserData!');
            var that = this.that;
            if (this.node) return d3.select(this.node).datum().data.userData;
            else return null;
        },
        setLabel: function (text) {
            // console.log('node setLabel!');
            var that = this.that;
            if (text && this.node) {
                var nodeData = d3.select(this.node).datum();
                nodeData.label = nodeData.data.label = text;
                that.setNodeLabel(this.node, text);
            }
            return this;
        },
        getLabel: function () {
            // console.log('node getLabel!');
            var that = this.that;
            if (this.node) {
                return d3.select(this.node).datum().label;
            } else return '';
        },
        setError: function () {
            // console.log('node setError!');
            var that = this.that;
            if (this.node) {
                d3.select(this.node).dispatch('error');
            }
            return this;
        },
        clearError: function () {
            // console.log('node clearError!');
            var that = this.that;
            if (this.node) {
                d3.select(this.node).dispatch('executed');
            }
            return this;
        },
        valid: function () {
            // console.log('node Valid!');
            var that = this.that;
            if (this.node) {
                var nodeData = d3.select(this.node).datum();
                var onValid = nodeData.data.onValid;
                var oEle = that.getNodeObject(this.node);
                if (typeof onValid === 'function') return onValid(oEle);
            }
            return this;
        },
        getType: function () {
            var that = this.that;
            if (this.node) {
                var nodeData = d3.select(this.node).datum();
                return nodeData.data.type;
            }
            return '';
        },
        setStyle: function (json) {
            /* 取 null
            json: {
               fill: '#87b2ec',
               stroke: '#87b2ec',
               fillopacity: 1,
               fontcolor: '#ffffff',
               fontsize: 12,
               strokewidth: 1,
               strokedasharray: 0
            }
             */
            var that = this.that;
            if (this.node && json) {
                var node = d3.select(this.node);
                var nodeData = node.datum();
                if (nodeData.userData.nodeStyle) {
                    var is = that.dataComparison(nodeData.userData.nodeStyle, json);
                    if (is) return this;
                }

                if (!nodeData.defaultStyle) {
                    nodeData.defaultStyle = {
                        stroke: nodeData.stroke,
                        strokeWidth: nodeData.strokeWidth,
                        strokeDasharray: nodeData.strokeDasharray,
                        fill: nodeData.fill,
                        fillOpacity: nodeData.fillOpacity,
                        fontSize: nodeData.fontSize,
                        fontColor: nodeData.fontColor,
                        fontIconColor: nodeData.fontIconColor,
                        fontIconSize: nodeData.fontIconSize,
                    };
                }
                nodeData.userData.nodeStyle = json;
                nodeData.stroke = json['stroke'] || nodeData.stroke;
                nodeData.strokeWidth = json['strokewidth'] || nodeData.strokeWidth;
                nodeData.strokeDasharray = json['strokedasharray'] === undefined ? nodeData.strokeDasharray : json['strokedasharray'];
                nodeData.fill = json['fill'] || nodeData.fill;
                nodeData.fillOpacity = json['fillopacity'] || nodeData.fillOpacity;
                nodeData.fontSize = json['fontsize'] || nodeData.fontSize;
                nodeData.fontColor = json['fontcolor'] || nodeData.fontColor;
                nodeData.fontIconColor = json['iconcolor'] || nodeData.fontColor;
                nodeData.fontIconSize = json['iconsize'] || nodeData.fontIconSize;
                node.dispatch('style');
            } else if (this.node) {
                var node = d3.select(this.node);
                var nodeData = node.datum();
                if (nodeData.defaultStyle) {
                    nodeData.userData.nodeStyle = nodeData.defaultStyle;
                    Object.keys(nodeData.defaultStyle).forEach(function (k) {
                        nodeData[k] = nodeData.defaultStyle[k];
                    })
                    node.dispatch('style');
                }
            }
            return this;
        },
        setStatusIcon: function (json) {
            /*
            json: [{
                name: 'paused' // 提供则采用内部提供的icon，不提供则采用下面自定义的icon
                icon: data.icon ? eval('\'' + data.icon + '\'') : '\ue92a',
                title: data.title || '暂停',
                iconsize: data.iconsize || 14,
                iconcolor: data.iconcolor || '#336FFF',
                iconfamily: 'ts',
            }]
             */
            var that = this.that;
            if (this.node) {
                var node = d3.select(this.node);
                var nodeData = node.datum();
                var statusIcon = [];
                if (Array.isArray(json)) {
                    if (nodeData.userData.statusIcon && nodeData.userData.statusIcon.length === json.length) {
                        var is = that.dataComparison(nodeData.userData.statusIcon, json);
                        if (is) return this;
                    }
                    nodeData.userData.statusIcon = json;
                    json.forEach(function (btn) {
                        var obj = {
                            name: btn.name || false,
                            icon: btn.icon ? eval('\'' + btn.icon + '\'') : '\ue92a',
                            title: btn.title || 'icon',
                            fontIconSize: btn.iconsize || 14,
                            fontIconColor: btn.iconcolor || '#336FFF',
                            fontIconFamily: btn.iconfamily || 'ts',
                            bgFill: btn.bgcolor || 'transparent',
                            bgStroke: btn.bgstroke || '#336FFF',
                            cb: btn.cb,
                        };
                        statusIcon.push(obj);
                    });
                }
                nodeData.statusIcon = statusIcon;
                that.appendNodeStatusIcon(this.node);
            }
            return this;
        },
        setStatusBtn: function (json) {
            /*
            json: [{
                icon: '\ue92a',
                title: '暂停',
                iconsize: 14,
                iconcolor: '#336FFF',
                iconfamily: 'ts',
                bgfill: '#4E93FA',
                bgstroke: 'transparent',
                segmentation: 'red',
                bgfillopacity: 0.45,
                onclick: function (o) {
                    console.log(o);
                },
            }]
             */
            var that = this.that;
            if (this.node) {
                var node = d3.select(this.node);
                var nodeData = node.datum();
                var statusBtn = [];
                if (Array.isArray(json)) {
                    if (nodeData.userData.statusBtn && nodeData.userData.statusBtn.length === json.length) {
                        var is = that.dataComparison(nodeData.userData.statusBtn, json);
                        if (is) return this;
                    }
                    nodeData.userData.statusBtn = json;
                    json.forEach(function (btn) {
                        var obj = {
                            icon: btn.icon ? eval('\'' + btn.icon + '\'') : '\ue92a',
                            title: btn.title || 'btn',
                            fontIconSize: btn.iconsize || 12,
                            fontIconColor: btn.iconcolor || '#336FFF',
                            fontIconFamily: btn.iconfamily || 'ts',
                            bgFill: btn.bgfill || '#4E93FA',
                            bgStroke: btn.bgstroke || 'transparent',
                            bgFillOpacity: btn.bgfillopacity || 0.45,
                            segmentation: btn.segmentation || 'red',
                            split: btn.split || btn.segmentation || 'red',
                            splitDasharray: btn.splitdasharray || '2',
                            segmentationWidth: btn.segmentationwidth || 0.5,
                            onClick: btn.onclick || function (o) {
                                // console.log(o);
                            },
                        };
                        statusBtn.push(obj);
                    });
                }
                if (statusBtn.length) {
                    nodeData.height = nodeData.labelHeight + that.options.nodeStatusBtnHeight;
                } else {
                    nodeData.height = nodeData.labelHeight;
                }
                nodeData.statusBtn = statusBtn;
                // node.dispatch('resize');
                that.appendNodeStatusBtn(node);
            }
            return this;
        },
        setProgress: function (json) {
            // json = {
            //     color: 'red',
            //     opacity: 0.8,
            //     progress: 0.6,
            //     enable: true
            // };
            var that = this.that;
            if (this.node && json) {
                var node = d3.select(this.node);
                var nodeData = node.datum();
                if (nodeData.userData.progress) {
                    var is = that.dataComparison(nodeData.userData.progress, json);
                    if (is) return this;
                }
                nodeData.userData.progress = json;
                nodeData.progress.color = json.color || nodeData.progress.color;
                nodeData.progress.opacity = json.progress !== undefined ? json.progress : nodeData.progress.opacity;
                nodeData.progress.progress = json.progress !== undefined ? json.progress : nodeData.progress.progress;
                nodeData.progress.enable = json.enable !== undefined ? json.enable : nodeData.progress.enable;
                that.appendNodeWareEffect(this.node);
            }
            return this;
        },
        setProperty: function (key, value) {
            var that = this.that;
            if (this.node) {
                var node  = d3.select(this.node);
                var nodeData = node.datum();
                nodeData.data[key] = value;
            }
            return this;
        },
        getProperty: function (key) {
            var that = this.that;
            if (this.node) {
                var node  = d3.select(this.node);
                var nodeData = node.datum();
                return nodeData.data[key];
            }
            return this;
        },
        setData: function (key, value) {
            if (this.node) {
                if (!this.userData) this.userData = {};
                this.userData[key] = value;
            }
            return this;
        },
        getData: function (key) {
            if (this.node) {
                if (this.userData) {
                    return this.userData[key];
                }
            }
            return this;
        },
        showMessage: function (json) {
            // json = {
            //     msg: 'this is a message',
            //     fill: 'orange',
            //     fillopacity: 0.8,
            //     fontcolor: 'white',
            //     icon: '\ue8d1',
            //     iconcolor: 'white',
            // };
            var that = this.that;
            if (this.node && json) {
                var node = d3.select(this.node);
                var nodeData = node.datum();
                var obj = {
                    msg: json.msg || 'error！',
                    fill: json.fill || '#f4f4f4',
                    fillOpacity: json.fillopacity || 0.8,
                    fontColor: json.fontcolor || '#555',
                    icon: json.icon ?  eval('\'' + json.icon + '\'') : '',
                    iconColor: json.iconcolor || '#555',
                    deleteIcon: '\ue84d',
                    deleteIconColor: 'red',
                };
                Object.assign(nodeData.tipData, obj);
                that.tooltipFn(nodeData);
                // that.tooltipFn(json.content, json.type, nodeData.x, nodeData.y);
                // node.dispatch('style');
            }
            return this;
        },
        setIsStart: function (b) {
            if (this.node) {
                var nodeData = d3.select(this.node).datum();
                nodeData && (nodeData.isStart = b || false);
            }
            return this;
        },
        setIsEnd: function (b) {
            if (this.node) {
                var nodeData = d3.select(this.node).datum();
                nodeData && (nodeData.isEnd = b || false);
            }
            return this;
        },
        getIsStart: function () {
            if (this.node) {
                var nodeData = d3.select(this.node).datum();
                return nodeData.isStart;
            }
        },
        getIsEnd: function () {
            if (this.node) {
                var nodeData = d3.select(this.node).datum();
                return nodeData.isEnd;
            }
        },
        setNeedIn: function  (b) {
            if (this.node) {
                var nodeData = d3.select(this.node).datum();
                nodeData && (nodeData.behavior.needIn = b);
            }
            return this;
        },
        getNeedIn: function () {
            if (this.node) {
                var nodeData = d3.select(this.node).datum();
                return nodeData.behavior.needIn;
            }
        },
        setNeedOut: function  (b) {
            if (this.node) {
                var nodeData = d3.select(this.node).datum();
                nodeData && (nodeData.behavior.needOut = b);
            }
            return this;
        },
        getNeedOut: function () {
            if (this.node) {
                var nodeData = d3.select(this.node).datum();
                return nodeData.behavior.needOut;
            }
        },
        extend: function (obj) {
            for (var attr in obj) {
                this[attr] = obj[attr];
            }
            return this;
        }
    };

    TsFlowChart.LINK.prototype = {
        setStatus: function (status) {
            var that = this.that;
            if (this.link && status) {
                var link = that.selectAll(this.link);
                switch (status) {
                    case 'succeed': // 成功
                    case 'done':
                        link.dispatch('through');
                        break;
                    case 'running': // 运行中
                    case 'doing':
                        link.dispatch('running');
                        break;
                    case 'failed': // 失败
                        link.dispatch('failed');
                        break;
                    case 'pending': // 未执行
                    case 'undo':
                        link.dispatch('without');
                        break;
                    default:
                        break;
                }
            }
        },
        setStyle: function (json) {
            var that = this.that;
            if (this.link) {
                var link = d3.select(this.link);
                var linkData = link.datum();
                if (linkData.userData.style) {
                    var is = that.dataComparison(linkData.userData.style, json);
                    if (is) return this;
                }
                linkData.userData.style = json;
                linkData.stroke = json.stroke || '#ddd';
                // linkData.strokeDasharray = json.strokedasharray === undefined ? linkData.strokeDasharray : json.strokedasharray;
                linkData.strokeWidth = linkData.strokewidth || 1;
                link.dispatch('style');
            }
            return this;
        },
        setRunning: function (json) {
            // json = {
            //     color: 'red',
            //     size: 14,
            //     speed: 300,
            //     icon: '\ue8a9',
            //     iconfamily: 'ts',
            //     enable: false
            // }
            var that = this.that;
            if (this.link && json) {
                var link = d3.select(this.link);
                var linkData = link.datum();
                if (linkData.userData.runing) {
                    var is = that.dataComparison(linkData.userData.runing, json);
                    if (is) return this;
                }
                linkData.userData.runing = json;
                linkData.lightDot.color = json.color || '#9f9f9f';
                linkData.lightDot.size = json.size || 13;
                linkData.lightDot.speed = json.speed || 150;
                linkData.lightDot.icon = json.icon ?  eval('\'' + json.icon + '\'') : "\ue8d5";
                linkData.lightDot.iconFamily = json.iconfamily ?  json.iconfamily : 'ts';
                if (json.enable === false) link.dispatch('runStop');
                else link.dispatch('runningDot');
            }
            return this
        },
        getUserData: function () {
            var that = this.that;
            if (this.link) {
                return d3.select(this.link).datum().data.userData;
            } else return null;
        },
        setUserData: function (json) {
            var that = this.that;
            if (this.link && typeof json === 'object') {
                var link = d3.select(this.link);
                var linkData = link.datum();
                linkData.label = linkData.data.userData = json;
                that.appendLinkLabel(this.link);
                link.dispatch('update');
            }
        },
        setLabel: function (text) {
            var that = this.that;
            var link = d3.select(this.link);
            var linkData = link.datum();
            linkData.label.text = text;
            !linkData.data.userData && (linkData.data.userData = {})
            linkData.data.userData.text = text;
            that.appendLinkLabel(this.link);
            link.dispatch('update');
        },
        getId: function () {
            var that = this.that;
            if (this.link) {
                return d3.select(this.link).datum().id;
            } else return '';
        },
        setData: function (key, value) {
            var that = this.that;
            if (this.link) {
                if (!this.userData) this.userData = {};
                this.userData[key] = value;
            } else return this;
        },
        getData: function (key) {
            if (this.link) {
                if (this.userData) {
                    return this.userData[key];
                }
            }
            return this;
        },
        setProperty: function (key, value) {
            var that = this.that;
            if (this.link) {
                var link = d3.select(this.link);
                var linkData = link.datum();
                linkData.data[key] = value;
            }
            return this;
        },
        getProperty: function (key) {
            var that = this.that;
            if (this.link) {
                var link = d3.select(this.link);
                var linkData = link.datum();
                return linkData.data[key];
            }
            return this;
        },
        showMessage: function (json) {
            // json = {
            //     content: 'this is a message!',
            //     type: 'warning',
            //     bgfill: 'red',
            //     bgstroke: 'blue',
            //     fontcolor: 'white',
            //     iconcolor: 'white',
            // };
            var that = this.that;
            if (this.link && json) {
                var link = d3.select(this.link);
                var linkData = link.datum();
                var obj = {
                    msg: json.msg || 'error！',
                    fill: json.fill || '#f4f4f4',
                    fillOpacity: json.fillopacity || 0.8,
                    fontColor: json.fontcolor || '#555',
                    icon: json.icon ?  eval('\'' + json.icon + '\'') : '',
                    iconColor: json.iconcolor || '#555',
                    deleteIconColor: 'red',
                };
                Object.assign(linkData.tipData, obj);
                // console.log(linkData.tipData);
                that.tooltipFn(linkData);
            }
            return this;
        },
        getEndNode: function () {
            var that = this.that;
            var link = d3.select(this.link);
            var linkData = link.datum();
            if (linkData) {
                var node = that.getNodeByUid(linkData.target.id);
                var oEle = that.getNodeObject(node);
                return oEle;
            } else return false;
        },
        getStartNode: function () {
            var that = this.that;
            var link = d3.select(this.link);
            var linkData = link.datum();
            if (linkData) {
                var node = that.getNodeByUid(linkData.source.id);
                var oEle = that.getNodeObject(node);
                return oEle;
            } else return false;
        },
        extend: function (obj) {
            for (var attr in obj) {
                this[attr] = obj[attr];
            }
            return this;
        }
    };
})(window);