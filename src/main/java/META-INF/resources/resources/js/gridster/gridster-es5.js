;
(function(factory) {
    if (typeof define === 'function' && define.amd) {
        define(['jquery'], factory);
    } else {
        factory(jQuery);
    }
})(function($) {

    var Gridster = function Gridster(node, options) {

        var _this = this;
        this.node = node;
        this.options = options;
        this.childs = node.children;
        this.widthPart = parseInt(parseFloat(getStyle(node, 'width')).toFixed() / options.widget_base_dimensions[0]);
        this.heightPart = Math.ceil(this.childs.length / this.widthPart);
        this.childComponent = new Array(this.childs.length);
        this.init();

    };

    Gridster.prototype = {
        init: function() {
            this.initMatrix();
            this.initChild();
            this.sort();
            document.body.setAttribute('onselectstart', 'return false')
            setStyle(this.node, {
                'height': this.heightPart * (this.options.widget_base_dimensions[1] + this.options.widget_margin[1]) + 'px'
            })
        },
        initChild: function() {
            for (var i = 0; i < this.childs.length; i++) {
                var child = new GridsterComponent(this, this.childs[i], this.options);
                this.childComponent[i] = child;
            }

        },
        sort: function(index, array) {
            var now = 0;
            var len = this.childs.length;
            for (var i = 0; i < len; i++) {
                var child = this.childs[i];
                if (index && index == i) {
                    continue;
                } else if (child) {
                    setStyle(child, {
                        'transform': 'translate(' + ((this.matrix[now][0] - 1) * (this.options.widget_base_dimensions[0] + this.options.widget_margin[0])) + 'px,' + ((this.matrix[now][1] - 1) * (this.options.widget_base_dimensions[1] + this.options.widget_margin[1])) + 'px)',
                        //     'transition': 'all .5s linear'
                    });
                    setAttr(child, {
                        'data-col': this.matrix[i][0],
                        'data-row': this.matrix[i][1]
                    });
                    this.childComponent[i].col = this.matrix[i][0];
                    this.childComponent[i].row = this.matrix[i][1];

                    this.childComponent[i].centerX = (this.matrix[now][0] - 1) * (this.options.widget_base_dimensions[0] + this.options.widget_margin[0]) + (this.options.widget_base_dimensions[0] + this.options.widget_margin[0]) / 2;
                    this.childComponent[i].centerY = (this.matrix[now][1] - 1) * (this.options.widget_base_dimensions[1] + this.options.widget_margin[1]) + (this.options.widget_base_dimensions[1] + this.options.widget_margin[1]) / 2;
                    now++;
                }
            }
        },
        initMatrix: function() {
            var len = this.childs.length,
                heightLen = this.heightPart,
                widthLen = this.widthPart,
                matrix = new Array(heightLen * widthLen);
            for (var i = 1; i <= heightLen; i++) {
                for (var j = 1; j <= widthLen; j++) {
                    matrix[(i - 1) * widthLen + j - 1] = [j, i];
                }
            }
            this.matrix = matrix;
        },
        interchange: function(newElem, targetElem, newIdx, targetIdx) {
            if (!newElem || !targetElem) return;
            var targetNode = targetElem,
                sourceNode = newElem;
            var siblingNode = getNextElement(sourceNode);
            if (siblingNode == targetElem) {
                this.node.insertBefore(targetNode, sourceNode);
            } else {
                this.node.insertBefore(sourceNode, targetNode);
                this.node.insertBefore(targetNode, siblingNode);
            }
            this.childComponent[newIdx] = this.childComponent.splice(targetIdx, 1, this.childComponent[newIdx])[0];
        }


    }

    var GridsterComponent = function(parent, child, options) {
        this.node = child;
        this.parent = parent;
        this.startX = 0;
        this.startY = 0;
        this.sourceX = 0;
        this.sourceY = 0;
        this.centerX = 0;
        this.centerY = 0;
        this.margin = options.widget_margin;
        this.dimensions = options.widget_base_dimensions;
        this.init();
    }

    GridsterComponent.prototype = {
        constructor: GridsterComponent,
        init: function() {
            this.setDrag();
            setStyle(this.node, {
                'marginTop': this.margin[0] + 'px',
                'marginLeft': this.margin[1] + 'px',
                'width': this.dimensions[0] + 'px',
                'height': this.dimensions[1] + 'px',
                'position': 'absolute'
            })

        },
        setDrag: function() {
            var self = this.node,
                _this = this,
                index = null,
                indexnode = null,
                parent = this.parent.node,
                matrix = this.parent.matrix,
                components = this.parent.childComponent,
                move = false,
                changenode = null,
                widthPart = this.parent.widthPart,
                width = _this.dimensions[0],
                height = _this.dimensions[1],
                marginTop = _this.margin[0],
                marginLeft = _this.margin[1];
            this.node.addEventListener('mousedown', dragStart, false);

            function dragStart(event) {
                var pos = _this.getTargetPos();

                this.startX = _this.startX = event.pageX;
                this.startY = _this.startY = event.pageY;

                this.sourceX = _this.sourceX = pos.x;
                this.sourceY = _this.sourceY = pos.y;

                _this.centerX = _this.sourceX + width / 2;
                _this.centerY = _this.sourceY + height / 2;

                document.addEventListener("mousemove", drag, false);
                document.addEventListener("mouseup", dragEnd, false);
            }


            function drag(event) {
                var currentX = event.pageX,
                    currentY = event.pageY,
                    distanceX = currentX - self.startX,
                    distanceY = currentY - self.startY,

                    x = 0,
                    y = 0,
                    idxX = 0,
                    idxY = 0,

                    changeIdx = null;

                for (var i = 0; i < _this.parent.childComponent.length; i++) {
                    if (self == _this.parent.childs[i]) {
                        index = i;
                    }
                }

                if ((self.sourceX + distanceX).toFixed() < 0) {
                    x = 0;
                } else if ((self.sourceX + distanceX).toFixed() > parseInt(getStyle(parent, 'width')) - parseInt(getStyle(self, 'width'))) {
                    x = parseInt(getStyle(parent, 'width')) - parseInt(getStyle(self, 'width'));
                } else {
                    x = (self.sourceX + distanceX).toFixed();
                }

                if ((self.sourceY + distanceY).toFixed() < 0) {
                    y = 0;
                } else if ((self.sourceY + distanceY).toFixed() > parseInt(getStyle(parent, 'height')) - parseInt(getStyle(self, 'height'))) {
                    y = parseInt(getStyle(parent, 'height')) - parseInt(getStyle(self, 'height'));
                } else {
                    y = (self.sourceY + distanceY).toFixed();
                }
                var centerX = _this.centerX = parseInt(x) + width / 2;
                var centerY = _this.centerY = parseInt(y) + height / 2;

                for (var i = 0; i < components.length; i++) {
                    if ((components[i].centerX + width / 2 > centerX) && (components[i].centerX - width / 2 < centerX) && (components[i].centerY + height / 2 > centerY) && (components[i].centerY - height / 2 < centerY) && i != index && !changenode) {
                        changenode = _this.parent.childs[i];
                        changeIdx = i;
                        nx = (_this.parent.childComponent[index].col - 1) * (width + marginLeft);
                        ny = (_this.parent.childComponent[index].row - 1) * (height + marginTop);
                        move = true;
                    }
                }
                if (changenode && move) {
                    setStyle(changenode, {
                        'transform': 'translate(' + nx + 'px,' + ny + 'px)',
                    })
                    _this.parent.interchange(self, changenode, index, changeIdx);
                    _this.parent.sort();
                    row = null;
                    col = null;
                    nx = null;
                    ny = null;
                    changenode = null;
                    changeIdx = null;
                    index = null;
                    move = false;
                }


                _this.setTargetPos({
                    x: x,
                    y: y
                })

                if (Math.abs(distanceX) > (_this.dimensions[0] + _this.margin[0]) || Math.abs(distanceY) > (_this.dimensions[1] + _this.margin[1])) {
                    move = false;

                    for (var i = 0; i < _this.parent.childComponent.length; i++) {
                        if (self == _this.parent.childs[i]) {
                            index = i;
                        }
                    }
                }

            }

            function dragEnd(event) {
                _this.parent.sort();
                move = false;
                document.removeEventListener('mousemove', drag);
                document.removeEventListener('mouseup', dragEnd)
            }

        },
        setTargetPos: function(pos) {
            var transform = this.getTransform();
            if (transform) {
                this.node.style[transform] = 'translate(' + pos.x + 'px,' + pos.y + 'px)';
            } else {
                this.node.style.left = pos.x + 'px';
                this.node.style.top = pos.y + 'px';
            }
        },
        getTransform: function() {
            var transform = '',
                divStyle = document.createElement('div').style,
                transformAttr = ['transform', 'webkitTranform', 'MozTransform', 'msTransform', 'OTransform'],
                len = transformAttr.length;

            for (var j = 0; j < len; j++) {
                if (transformAttr[j] in divStyle)
                    return transform = transformAttr[j];
            }

            return transform;
        },
        getTargetPos: function() {
            var pos = { x: 0, y: 0 };
            var elem = this.node;
            var transform = this.getTransform();
            if (transform) {
                var transformValue = getStyle(elem, transform);
                if (transformValue == 'none') {
                    elem.style[transform] = 'translate(0, 0)';
                    return pos;
                } else {
                    var temp = transformValue.split(/[ ,]+/);
                    return pos = {
                        x: parseInt(temp[4].trim()),
                        y: parseInt(temp[5].trim())
                    }
                }
            } else {
                if (getStyle(elem, 'position') == 'static') {
                    elem.style.position = 'absolute';
                } else {
                    var x = parseInt(getStyle(elem, 'left') ? getStyle(elem, 'left') : 0);
                    var y = parseInt(getStyle(elem, 'top') ? getStyle(elem, 'top') : 0);
                    return pos = {
                        x: x,
                        y: y
                    }
                }
            }
        }

    }

    function getNextElement(element) {
        var e = element.nextSibling;
        if (e == null) { //测试同胞节点是否存在，否则返回空
            return null;
        }
        if (e.nodeType == 3) { //如果同胞元素为文本节点
            var two = getNextElement(e);
            if (two == null) return;
            if (two.nodeType == 1)
                return two;
        } else {
            if (e.nodeType == 1) { //确认节点为元素节点才返回
                return e;
            } else {
                return false;
            }
        }
    }


    function setStyle(elem, attr) {
        for (var property in attr) {
            elem.style[property] = attr[property];
        }
    }

    function getStyle(elem, property) {
        return document.defaultView.getComputedStyle ? document.defaultView.getComputedStyle(elem)[property] : elem.currentStyle[property];
    }


    function setAttr(elem, attr) {
        for (var property in attr) {
            elem.setAttribute(property, attr[property]);
        }
    }

    function getAttr(elem, property) {
        return elem.getAttribute(property);
    }

    $.fn.tsgridster = function(opts) {
        return this.each(function() {
            /*if (!$(this).data('gridster')) {
                $(this).data('gridster', new Gridster(this, opts));
            }*/
        	 $(this).data('gridster', new Gridster(this, opts));
        });
    };
});