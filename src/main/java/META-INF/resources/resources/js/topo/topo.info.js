(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {}), global));
}(this, (function (exports, global) {
    var Info = function (base) {
        this.base = base;
        this.init();
    };
    Info.prototype = {
        constructor: Info,
        init: function () {
            var _that = this.base;
            var that = this;
            this.base.getVm('svg').container.classed('right-block', true);
            this.container = this.base.container.append('xhtml:div').attr('class', 'detailpannel');

            // 降低color picker渲染时间
            var assistDiv = this.container.append('xhtml:div');
            var assistPicker = this.colorPicker({el: assistDiv.node()});
            d3.select(assistPicker.getRoot().root).remove();

            // 绑定触发事件
            var svg = _that.getVm('svg');
            svg.el.on('afterSelected', function (d) {
                var detail = d3.event.detail;
                var selectAll = _that.getAll('condition', 'selected');
                var infoArr = [];
                if (!selectAll.size()) { // 显示svg
                    infoArr.push(d);
                } else if (selectAll.size() === 1) {
                    infoArr.push(detail);
                } else { // 节点信息归并

                }

                // 渲染
                var pannel = that.container.selectAll('.pannel').data(infoArr, function (d) {
                    return d.uuid;
                });
                var pannelEnter = pannel.enter().append('xhtml:div').attr('class', 'pannel');
                var pannelExit = pannel.exit()
                    .each(function (d) {
                        var item = d3.select(this).selectAll('.item');
                        item.data().forEach(function (o) {
                            if (o.hasOwnProperty('colorPicker')) o.colorPicker.destroyAndRemove();
                        });
                    })
                    .remove();

                pannelEnter.each(function (d) {
                    var pel = d3.select(this);
                    var title = pel.append('xhtml:div').attr('class', 'pannel-title').text(d.info.label);
                    var block = pel.append('xhtml:div').attr('class', 'block-container');
                    var itemData = Object.keys(d.info).filter(function (key) {
                        return typeof d.info[key] === 'object';
                    }).map(function (key) {
                        return Object.assign({}, d.info[key], {key: key});
                    });
                    var item = block.selectAll('.item').data(itemData, function (o) {
                        return o.key;
                    });
                    var itemEnter = item.enter().append('xhtml:div').attr('class', 'item')
                        .html(function (o) {
                            return that.appendItem(d.info[o.key]);
                        })
                        .each(function (o) {
                            if (o.type === 'color') {
                                var picker = d3.select(this).select('.pickr');
                                picker.size() && (o.colorPicker = that.colorPicker({
                                    el: picker.node(),
                                    default: o.value
                                }));
                                o.colorPicker && o.colorPicker.on('save', function (HSVaColorObject, PickrInstance) {
                                    d.hasOwnProperty(o.key) && (d[o.key] = d.info[o.key].value = HSVaColorObject.toHEXA().toString());
                                });
                            } else if (o.type === 'input' || o.type === 'mulInput') {
                                var input = d3.select(this).selectAll('input.input');
                                var once;
                                input
                                    .on('blur', function () {
                                        if (o.type === 'mulInput' && o.key === 'widthHeight') {
                                            var inputVal = input.nodes().map(function (el) {
                                                return parseFloat(el.value);
                                            });
                                            d.info[o.key].value = inputVal.join('-');
                                            d['width'] = inputVal[0];
                                            d['height'] = inputVal[1];
                                            d.whChange = d.info[o.key].value;

                                        } else {
                                            d.hasOwnProperty(o.key) && (d[o.key] = d.info[o.key].value = this.value);
                                        }
                                        svg.data.status.inputting = false;
                                    })
                                    .on('focus', function () {
                                        var _this = this;
                                        svg.data.status.inputting = true;
                                        once = _.once(function () {
                                            var val = _this.value;
                                            _this.setSelectionRange(val.length, val.length)
                                        })
                                    })
                                    .on('keydown', function () {
                                        if (event.keyCode == 13) {
                                            this.blur();
                                        }
                                    })
                                    .on('click', function () {
                                        // once && once();
                                    })

                            }

                        });

                    var itemExit = item.exit().remove();

                    // 添加colorpicker
                });
            });

            svg.el.on('fromJsonEnd', function () {
                svg.el.dispatch('afterSelect');
            });

            // this.container.html(function () {
            //     return `
            //         <div>
            //             <div data-status="node-selected" class="pannel" style="display: none;">
            //                 <div class="pannel-title">节点</div>
            //
            //             </div>
            //             <div data-status="edge-selected" class="pannel" style="display: none;">
            //                 <div class="pannel-title">线</div>
            //             </div>
            //             <div data-status="group-selected" class="pannel" style="display: none;">
            //                 <div class="pannel-title">组</div>
            //             </div>
            //             <div data-status="canvas-selected" class="pannel" style="display: block;">
            //                 <div class="pannel-title">画布</div>
            //                 <div class="block-container">
            //                     <div class="item">
            //                         名称：<input class="name-input input" type="text" value="模型节点">
            //                     </div>
            //                     <div class="item">
            //                         尺寸：<input type="text" class="width-input input"><input class="height-input input" type="text">
            //                     </div>
            //                     <div class="item">
            //                         颜色：<div class="pickr"></div>
            //                     </div>
            //                 </div>
            //             </div>
            //             <div data-status="multi-selected" class="pannel" style="display: none;">
            //                 <div class="pannel-title">多选</div>
            //             </div>
            //         </div>
            //     `
            // })
        },
        appendItem: function (obj) {
            var that = this;
            var item;
            switch (obj.type) {
                case 'input':
                    item = `
                        ${obj.label}：<input class="name-input input" type="text" value="${obj.value}">
                    `;
                    break;
                case 'color':
                    item = `
                        ${obj.label}：<div class="pickr"></div>
                    `;
                    break;
                case 'mulInput':
                    var valArr = obj.value.split('-');
                    item = `
                        ${obj.label}：<input class="width-input input" type="text" value="${valArr[0]}"><input class="height-input input" type="text" value="${valArr[1]}">
                    `;
            }

            return item;
        },
        colorPicker: function (obj) {
            return new Pickr(Object.assign({
                swatches: ['#F44336', '#E91E63', '#9C27B0', '#673AB7'],
                components: {

                    // Defines if the palette itself should be visible.
                    // Will be overwritten with true if preview, opacity or hue are true
                    palette: false,

                    preview: true, // Left side color comparison
                    opacity: true, // Opacity slider
                    hue: true,     // Hue slider

                    interaction: {
                        hex: true,  // hex option  (hexadecimal representation of the rgba value)
                        rgba: true, // rgba option (red green blue and alpha)
                        input: true, // input / output element
                        save: true,  // Save button,
                    }
                },
                strings: {
                    save: '确定',  // Default for save button
                }
            }, obj));
        }
    };
    exports.info = Info;
})));