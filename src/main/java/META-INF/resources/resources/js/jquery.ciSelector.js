/**
 * ci选择器，确认函数需要自定义
 * 支持单选和多选
 * 选中的值放在$("#hidCiID")和$("#hidCiText")里
 * $("#hidSelector")存放选择器的ID，比如触发选择的按钮
   实例（showReportParamForm.jsp，单选的情况）：
   var opt = {
        ciTypeId : '',
        selectType : 'single',
        callbackFun: function () {
            var val = $("#hidCiID").val();
            var text = $("#hidCiText").val();
            var selectorId = $("#hidSelector").val();
            var data = {
                id : val,
                name : text
            };
            var dom = template.render("ciTemplate",data);
            if(selectorId != ''){
                if( $('#' + selectorId).next().hasClass("ciLabelContainer")) {
                    $('#' + selectorId).next().remove();
                }
                $('#' + selectorId).after(dom);
            }
        }
    };

    $('.ciSelector').ciSelector( opt );
 */
(function ($) {
    var defaults = {
        selectType : 'single',
        ciTypeId : '',
        param : [],
        initFun : function(){},
        callbackFun : function(){}
    };

    $.fn.ciSelector = function (options) {
        var opt = $.extend(defaults, options);
        return this.each(function () {
            initCmdbSelector( this ,opt );
        });
    };

    var initCmdbSelector = function( item , opt  ){
        if(typeof opt.initFun == 'function'){
            opt.initFun;
        }
        var selectorId = $(item).prop("id");
        var ciTypeId = $(item).data("ciTypeId");
        $(item).click(function(){
            ciTypeId = ( typeof ciTypeId == 'undefined' ) ? opt.ciTypeId : ciTypeId;
            var width = ( ciTypeId == '' || ciTypeId == null ) ? 610 : 450;
            //附加中间参数，给successFun调用
            if(opt.param!=null && opt.param.length > 0){
                $.each(opt.param,function(i,v){
                    var obj = opt.param[i];
                    var id = opt.param[i].name;
                    var value = opt.param[i].value;
                    if( $('#' + id).size() > 0){
                        $('#' + id).val(value);
                    }else{
                        var ipt = $('<input type="hidden" id="' + id + '" value="' + value + '">');
                        $('body').append(ipt);
                    }
                });
            }

            createModalDialog({
                msgwidth : width,
                url : '/balantflow/module/balantcmdb/cmdb/ciSelectorAjax.do?selectorId=' + selectorId + '&selectType=' + opt.selectType + '&ciTypeId=' + ciTypeId,
                msgtitle : '配置项选择',
                successFuc : opt.callbackFun
            });
        });

    };

})(jQuery);