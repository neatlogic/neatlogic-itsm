(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {})));
}(this, (function (exports) {
    'use strict';
    var opts = {
        type: 'tip',
        nodeinterval: 160,
        linelength: 200
    };

    // 处理tip功能
    var Tag = exports[opts.type] = exports.Base.extend();
    Tag.inherit = 'Base';
    Tag.inject({
        init: function (vm) {
            var that = this;
            this
                .inherit(vm)
                .subset(vm)
                .instanceInject({
                    options: Object.assign({}, vm.options, opts),
                    container: vm.container
                });

            this.el = this.container.append('xhtml:div').attr('class', 'tip hide').style('opacity', 0);
            this.content = this.el.append('xhtml:div').attr('class', 'tip-con');
            this.arrow = this.el.append('xhtml:div').attr('class', 'tip-arrow');

            this.Svg = this.getVm('svg');

            this.Svg.el.on('afterClick', function () {
                that.hide();
            });
            // this.Svg.el.on('beforeDrag', function () {
            //     that.hide();
            // })
            this.el
                .on('mouseover', function () {
					that.el.interrupt('hide');
					that.el.style('opacity', 1).classed('hide', false);
				})
				.on('mouseout', function () {
                    that.options.nodeMouseout(that.current);
				})

        },
        tip: function (node, actionData) {
            if (node.hasOwnProperty('node')) node = node.datum();
            if (!node) return;

            this.nodeData = node;
            this.actionData = actionData;

            if (this.actionData.action === 'ru') {
                this.position = {
                    top: this.Svg.getZoomInvert({
                        x: node.x + this.actionData.x,
                        y: node.y - node.height / 2
                    }),
                    right: this.Svg.getZoomInvert({
                        x: node.x + node.width / 2,
                        y: node.y - node.height / 4
                    }),
                    left: this.Svg.getZoomInvert({
                        x: node.x - node.width / 2,
                        y: node.y - node.height / 4
                    })
                };
            } else if (this.actionData.action === 'rd') {
                this.position = {
                    bottom: this.Svg.getZoomInvert({
                        x: node.x + this.actionData.x,
                        y: node.y + node.height / 2
                    }),
                    right: this.Svg.getZoomInvert({
                        x: node.x + node.width / 2,
                        y: node.y + node.height / 4
                    }),
                    left: this.Svg.getZoomInvert({
                        x: node.x - node.width / 2,
                        y: node.y + node.height / 4
                    })
                };
            } else if (this.actionData === 'node') {
				this.position = {
					bottom: this.Svg.getZoomInvert({
						x: node.x,
						y: node.y + node.height / 2
					}),
					right: this.Svg.getZoomInvert({
						x: node.x + node.width / 2,
						y: node.y
					}),
					left: this.Svg.getZoomInvert({
						x: node.x - node.width / 2,
						y: node.y
					}),
                    top: this.Svg.getZoomInvert({
						x: node.x,
						y: node.y - node.height / 2
					})
				};
            }
        },
        show: function (content) {
            var that = this;
            if (!content) this.hide();
            else that.el.classed('hide', false);

            if (content.nodeType === 1) {
                this.content.html('').append(function () {
                    return content;
                })
            } else this.content.html(content);

            var position = this.position;

            var eWidth = that.el.node().offsetWidth;
            var eHeight = that.el.node().offsetHeight;

            var conWidth = that.container.node().offsetWidth;
            var conHeight = that.container.node().offsetHeight;

            var left, top, isRight, isTop, isBottom;
            var pos;

            isRight = (position.right[0] + eWidth + 12) < conWidth;
            isTop = !isRight && (this.actionData.action === 'ru') && ((position.right[1] - eHeight - 12) > 0);
            isBottom = !isRight && (this.actionData.action === 'rd') && ((position.right[1] + eHeight + 12) < conHeight);

            if (isRight) {
                pos = position.right;
                left = position.right[0];
                top = position.right[1] - eHeight / 2;
                that.el.attr('class', 'tip right');
            } else if (isTop) {
                pos = position.top;
                left = position.top[0] - eWidth / 2;
                top = position.top[1] - eHeight;
                that.el.attr('class', 'tip top');
            } else if (isBottom) {
                pos = position.bottom;
                left = position.bottom[0] - eWidth / 2;
                top = position.bottom[1];
                that.el.attr('class', 'tip bottom');
            } else { // 左边
                pos = position.left;
                left = position.left[0] - eWidth;
                top = position.left[1] - eHeight / 2;
                that.el.attr('class', 'tip left');
            }

            // 边界判断
            // top
            if (top < 0) top = 0;
            // right
            if (left + eWidth > conWidth) left = conWidth - eWidth;
            // left
            if (left < 0) left = 0;
            // bottom
            if (top + eHeight > conHeight) top = conHeight - eHeight;

            that.el
                .style('left', left + 'px')
                .style('top', top + 'px');

            that.arrow
                .style('left', null)
                .style('top', null);

            if (isTop || isBottom) {
                var x = pos[0] - left < 10 ? 10 : (pos[0] - left);
                that.arrow
                    .style('left', x + 'px')
            } else {
                var y = pos[1] - top < 10 ? 10 : (pos[1] - top);
                that.arrow
                    .style('top', pos[1] - top + 'px')
            }


            var transition = that.el
                .transition('show')
                .duration(300)

            if (that.el.classed('hide')) {
                transition
                    .style('left', left + 'px')
                    .style('top', top + 'px');
            } else {

            }
			that.el.interrupt('hide');
            transition
				.ease(d3.easeLinear)
                .style('opacity', 1)
                .on('start', function () {

                })
                .on('interrupt', function () {
					that.el.style('opacity', 1)
				})


        },
        hide: function () {
            var that = this;
            this.el
                .transition('hide')
                .duration(500)
                .ease(d3.easeLinear)
                .style('opacity', 0)
                .on('end', function () {
                    that.el.classed('hide', true);
                })

        }

    });


})));