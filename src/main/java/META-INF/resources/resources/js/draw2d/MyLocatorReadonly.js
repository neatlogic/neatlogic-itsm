/*****************************************
 *   Library is under GPL License (GPL)
 *   Copyright (c) 2013 Techsure
 ****************************************/
draw2d.layout.locator.MyLeftLocator = draw2d.layout.locator.Locator.extend({
    NAME : "draw2d.layout.locator.MyLeftLocator",
    
    /**
     * @constructor
     * Constructs a locator with associated parent.
     * 
     * @param {draw2d.Figure} parent the parent associated with the locator
     */
    init: function(parent)
    {
      this._super(parent);
    },
    
    
    /**
     * @method
     * Relocates the given Figure.
     *
     * @param {Number} index child index of the target
     * @param {draw2d.Figure} target The figure to relocate
     **/
    relocate:function(index, target)
    {
        var parent = this.getParent();
        var boundingBox = parent.getBoundingBox();

        var targetBoundingBox = target.getBoundingBox();
        target.setPosition(-targetBoundingBox.w + 10,(boundingBox.h/2)-(targetBoundingBox.h/2));
    }
});


draw2d.layout.locator.MyRightLocator = draw2d.layout.locator.Locator.extend({
    NAME : "draw2d.layout.locator.MyRightLocator",

    /**
     * @constructor
     * Constructs a locator with associated parent.
     *
     * @param {draw2d.Figure} parent the parent associated with the locator
     */
    init: function(parent)
    {
        this._super(parent);
    },


    /**
     * @method
     * Relocates the given Figure.
     *
     * @param {Number} index child index of the target
     * @param {draw2d.Figure} target The figure to relocate
     **/
    relocate:function(index, target)
    {
        var parent = this.getParent();
        var boundingBox = parent.getBoundingBox();

        var targetBoundingBox = target.getBoundingBox();
        target.setPosition(boundingBox.w ,(boundingBox.h/2)-(targetBoundingBox.h/2));
    }
});