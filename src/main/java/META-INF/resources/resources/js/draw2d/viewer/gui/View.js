viewer.View = draw2d.Canvas.extend({
	init : function(id) {
		this._super(id);
		this.setScrollArea("#" + id);
		$('svg').css('cursor', 'crosshair');
	},
	onDoubleClick : function(x, y, shiftKey, ctrlKey) {
		var midw = this.getWidth();
		var midh = this.getHeight();
		var dx = 480 - x;
		var dy = 250 - y;
		this.getFigures().each($.proxy(function(i, f) {
			if (f.getCssClass() != 'draw2d_shape_composite_Group') {
				f.translate(dx, dy);
			}
		}, this));
	}
});
