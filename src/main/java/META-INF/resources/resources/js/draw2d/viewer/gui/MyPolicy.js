draw2d.policy.canvas.MyPolicy = draw2d.policy.canvas.ReadOnlySelectionPolicy.extend({
	isDrag : false,
	onMouseDown : function() {
		if (!this.isDrag) {
			this.isDrag = true;
			$('svg').css('cursor', 'move');
		}
	},
	onMouseDrag : function(canvas, dx, dy, dx2, dy2) {
		if (this.isDrag) {
			canvas.getFigures().each($.proxy(function(i, f) {
				if (f.getCssClass() != 'draw2d_shape_composite_Group') {
					f.translate(dx2, dy2);
				}
			}, this));
		}
	},
	onMouseUp : function() {
		if (this.isDrag) {
			this.isDrag = false;
			$('svg').css('cursor', 'crosshair');
		}
	}
});
