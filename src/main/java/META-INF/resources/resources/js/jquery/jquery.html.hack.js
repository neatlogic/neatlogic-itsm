(function($) {
  var oldHTML = $.fn.html;
 
  $.fn.formhtml = function() {
    if (arguments.length) return oldHTML.apply(this,arguments);
    $("input,button", this).each(function() {
      this.setAttribute('value',this.value);
    });
    $("textarea", this).each(function() {
      $(this).text(this.value);
    });
    $("input:radio,input:checkbox", this).each(function() {
      if (this.checked) this.setAttribute('checked', 'checked');
      else this.removeAttribute('checked');
    });
    $("option", this).each(function() {
      if (this.selected) this.setAttribute('selected', 'selected');
      else this.removeAttribute('selected');
    });
    return oldHTML.apply(this);
  };
 
  //这里可以选择是否覆盖jquery原来的html()方法，自己看着办。
  $.fn.html = $.fn.formhtml;
})(jQuery);