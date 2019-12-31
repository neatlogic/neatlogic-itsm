(function ($) {
    //插件的默认值属性
    var defaults = {
        data : {}
    };


    //
    $.fn.buildPropertiesDom = function (options) {
        if(options.propTemplateId == null){
            return;
        }
        //合并用户自定义属性，默认属性
        var opt = $.extend(defaults, options);
        return this.each(function () {
            loadProp( options.propTemplateId, this );
        });
    };

    //
    var loadProp = function ( propTemplateId ,container) {
        $.getJSON('/balantflow/prop/selectPropByTemplateId.json?propTemplateId=' + propTemplateId, function(d){
            for(var i=0; i<d.length; i++){
                var dom = buildPropDom(d[i]);
                $(container).append(dom);
            }
            initProp();
        });
    };

    var initProp = function(){
        $(".sltOptionSet").each(function(){
            var setId = $(this).attr("setid");
            var selector = 'select[setid="' + setId + '"]';
            $.getJSON('/balantflow/prop/getPropOption.do?setId=' + setId, function(data){
                $(selector).appendSelect(data.Data);
            });
        });

        $(".propInput").each(function(){
            var dom = $(this);
            var opt = {
                setId : dom.attr("setid"),
                defaultValue : dom.attr("default"),
                propId : dom.attr("propid"),
                propName : dom.attr("propName"),
                type : dom.attr("type"),
                propNeed : dom.attr("propneed"),
                propCmdb : dom.attr("propCmdb")
            };

            $.getJSON('/balantflow/prop/getPropOption.do?setId=' + opt.setId, function(obj){
                for(var i=0; i<obj.Data.length; i++){
                    opt.name = obj.Data[i].text;
                    opt.value = obj.Data[i].value;
                    var html = template.render('propInputTemplate', opt);
                    dom.append(html);
                }
            });
        });

        //initUserSelector();
        initUidControl();
        initCmdbSelector();
    };

    var  buildPropDom = function(prop){
        var dom = '';
        switch (prop.propType){
            case 'text':
                //dom = template.render('propTextTemplate', prop);
            	dom = getCatalogComponent(prop.propType, prop);
                break;
            case 'calendar':
                //dom = template.render('propCalendarTemplate', prop);
                dom = getCatalogComponent(prop.propType, prop);
                break;
            case 'user' :
                //dom = template.render('propUserTemplate',prop);
            	 dom = getCatalogComponent(prop.propType, prop);
                break;
            case 'select':
               // dom = template.render('propSelectTemplate',prop);
                dom = getCatalogComponent(prop.propType, prop);
                break;
            case 'radio':
                //dom = template.render('propRadioTemplate',prop);
                dom = getCatalogComponent(prop.propType, prop);
                break;
            case 'checkbox':
                //dom = template.render('propCheckboxTemplate',prop);
                dom = getCatalogComponent(prop.propType, prop);
                break;
            case 'ci':
               // dom = template.render('propCiTemplate',prop);
                dom = getCatalogComponent(prop.propType, prop);
                break;
            default:
                break;
        }
        return dom;
    }
    
    var getCatalogComponent = function (type, param){
    	var html = '';
    	$.ajax({
    		url: '/balantflow/resources/js/catalog-component/view/'+type+'.html',
    		async: false,
    		success: function(data){
    			var fn = doT.template(data);
    			html = fn(param);
    		}
    	});
    	return html;
    }

    var initCmdbSelector = function(){
        $(".ciSelector").each(function(){
            var cmdbDef = $(this).data("cmdb");
            var cmdbObj;
            if(cmdbDef!=null && cmdbDef!=''){
                cmdbObj = eval('[' + cmdbDef + ']')[0];
            }
            var ciTypeId = '';
            if(typeof cmdbObj == 'object') {
                ciTypeId = cmdbObj.ciType;
            }
            $(this).data("ciTypeId",ciTypeId);

            $(this).ciSelector({
                selectType : 'multi',
                ciTypeId : ciTypeId,
                param : [
                        {
                            name : "hidCurrentPropId",
                            value : name
                        }
                    ]
                ,
                callbackFun : function(){
                    var  val = $("#hidCiID").val();
                    var  text = $("#hidCiText").val();
                    var ciArray = val.split(',');
                    var ciNameArray = text.split(',');
                    var selectorId = $("#hidSelector").val();
                    var propId = $("#hidCurrentPropId").val();
                    $('#' + selectorId).next().empty();
                    $.each(ciArray,function(i,v){
                        if(v!='') {
                            var opt = {
                                id:"",
                                propId:propId,
                                ciId:v,
                                ciName:ciNameArray[i]
                            };
                            var rst = template.render('propCiCheckboxTemplate',opt);
                            $('#' + selectorId).next().append( rst );
                        }
                    });
                }
            });
        });

        /*
        $(".ciSelector").click(function(){
            var name = $(this).data("name");
            var cmdbDef = $(this).data("cmdb");
            var cmdbObj;
            if(cmdbDef!=null && cmdbDef!=''){
                cmdbObj = eval('[' + cmdbDef + ']')[0];
            }
            $("#hidCurrentPropId").val(name);
            var ciTypeId = '';
            if(typeof cmdbObj == 'object') {
                ciTypeId = cmdbObj.ciType;
            }
            var width = ( ciTypeId == '' || ciTypeId == null ) ? 600 : 450;
            createDialog({
                msgwidth : width,
                url : '/balantflow/module/balantcmdb/cmdb/ciSelectorAjax.do?ciTypeId=' + ciTypeId,
                msgtitle : '配置项选择',
                successFuc : function(){
                    var  val = $("#hidCiID").val();
                    var  text = $("#hidCiText").val();
                    var ciArray = val.split(',');
                    var ciNameArray = text.split(',');
                    var propId = $("#hidCurrentPropId").val();
                    var ciSpan = $("#spanCi" + propId);
                    ciSpan.empty();
                    $.each(ciArray,function(i,v){
                        if(v!='') {
                            var opt = {
                                id:"",
                                propId:propId,
                                ciId:v,
                                ciName:ciNameArray[i]
                            };
                            ciSpan.append( template.render('propCiCheckboxTemplate',opt) );
                        }
                    });
                }
            });
        });*/

    };


    var initUidControl = function(){
        var uidControler = $('.iptUserSelect');
        if( uidControler.size() > 0) {
            uidControler.each(function () {
                $(this).uidTypehead({});
            });
        }
    };


    var initUserSelector = function(){
        $('.iptUserSelect').each(function(){
            var url = '/balantflow/prop/getUserJson.do';
            var opt = {
                marcoPolo: {
                    url: url,
                    param : 'uid',
                    minChars: 2,
                    formatItem: function (data) {
                        return data.userName + '[' + data.userId + ']';
                    },
                    formatMinChars: function (minChars) {
                        return '<em>至少输入 <strong>' + minChars + '</strong> 个字母进行查询。</em>';
                    },
                    formatNoResults: function (q) {
                        return '<em>关键字 <strong>' + q + '</strong> 查无结果。</em>';
                    }
                },
                formatValue: function (data, $value, $item, $mpItem) {
                    return data.userId;
                },
                formatDisplay: function (data, $item, $mpItem) {
                    return data.userName;
                },
                required: true
            };
            $(this).manifest(opt);
            $(this).on('manifestchange', function (event, type, data, $item) {
                $.each( $(this).manifest('values') , function(i,v){
                    console.info(v);
                });
            });
        });
    };

    var initOrgSelector = function(){
        $('.divCiSelector').each(function(){
            $(this).treeselect({
                url:'/balantflow/module/balantcmdb/cmdb/getCiTypeListJson.do',
                extendall: false,
                chkstyle:'radio',
                selectmode : 'child'
            });
        });
    }

})(jQuery);
