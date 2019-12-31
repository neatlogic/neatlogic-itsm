var showSnapMessage = function(message, mesgtype, x, y,speed) {
	var option = {};
	option.message = message;
	option.x = x;
	option.y = y;
	option.speed = speed || 500;
	option.mesgtype = mesgtype || 'notice'; // notice, warning, error, success
	var snapMessage = new SnapMessage(option);
	snapMessage.show();
	return snapMessage;
};

var SnapMessage = function(options) {
	this.distance = 50;
	$.extend(this, options);
};

SnapMessage.prototype.show = function() {
	var x = this.x || window.event.clientX + $(window).scrollLeft();
	var y = this.y || window.event.clientY + $(window).scrollTop();
	var that = this;
	var left = x;
	that.$container = $('<div class="jquery-snapmessage-container snapmessage-' + that.mesgtype + '"></div>');
	that.$container.text(that.message);
	$('body').append(that.$container);
	that.$container.css({
		top : (y-that.$container.height()) > $(document).height() ? ($(document).height()-that.$container.height()) + 'px' : (y-that.$container.height()) + 'px',
		left : (that.$container.width()+left )> $(document).width() ? (left - 5 - that.$container.width()) + 'px' : left + 'px'
	});	
	if(y - $(window).scrollTop() - that.distance - that.distance * 3/5 > 10){
		that.$container.animate({
			top : (y - that.distance) + 'px'
		}, that.speed).animate({
			opacity : 0,
			top : (y - that.distance - that.distance * 3/5) + 'px'
		}, that.speed*0.6, function() {
			that.$container.remove();
		})		
	}else{
		that.$container.animate({
			top : (y + that.distance) + 'px'
		}, that.speed).animate({
			opacity : 0,
			top : (y + that.distance + that.distance * 3/5) + 'px'
		}, that.speed*0.6, function() {
			that.$container.remove();
		})		
	}
};
