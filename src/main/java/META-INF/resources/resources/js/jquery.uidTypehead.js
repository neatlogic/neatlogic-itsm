(function ($) {
    var defaults = {
        min : 2,
        name : 'uid',
        displayKey : 'userId',
        url : '/balantflow/prop/getUserJson.do?uid=%QUERY'
    };

    $.fn.uidTypehead = function (options) {
        var opt = $.extend(defaults, options);
        return this.each(function () {
            initCtl( this , opt );
            $('.tt-hint').removeAttr("check-type").removeAttr("required-message");
        });
    };

    var initCtl = function( item , options ){
        if( item != undefined && typeof item == 'object') {
            var users = new Bloodhound({
                datumTokenizer: Bloodhound.tokenizers.obj.whitespace,
                queryTokenizer: Bloodhound.tokenizers.whitespace,
                remote: options.url
            });
            users.initialize();
            $(item).typeahead(
                            {
                                minLength: options.min
                            },
                            {
                                name : options.name,
                                displayKey : options.displayKey ,
                                source: users.ttAdapter(),
                                templates: {
                                    empty: [
                                        '<p>查无此人</p>'
                                    ].join('\n'),
                                    suggestion: template.compile('<p>{{userName}}[{{userId}}]</p>')
                                }
                            });
        }
    };

})(jQuery);