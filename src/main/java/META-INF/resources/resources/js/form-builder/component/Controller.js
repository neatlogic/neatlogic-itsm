Controller = Class.extend({
	init : function(_prop) {
		this.item = $('<div class="form-group formbuilder-auto"><label class="col-xs-2 control-label"></label><div class="control-main col-xs-10"></div></div>');
		this.labelContainer = this.item.find('.control-label');
		this.controllerContainer = this.item.find('.control-main');
		this.item.on('click', function() {
			createSlideDialog({
				title : 'gaga',
				content : 'asdfaf'
			});
		});
	}
});