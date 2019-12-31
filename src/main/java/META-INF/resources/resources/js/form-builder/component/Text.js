Text = Controller.extend({
	init : function(_prop) {
		this._super(_prop);
		this.conf = {
			"title" : "输入框",
			"fields" : {
				"id" : {
					"label" : "ID/Name",
					"type" : "input",
					"value" : "textinput"
				},
				"form-label" : {
					"label" : "标签",
					"type" : "input",
					"value" : "文本框"
				},
				"form-group" : {
					"label" : "分组",
					"type" : "input",
					"value" : ""
				},
				"placeholder" : {
					"label" : "输入提示",
					"type" : "input",
					"value" : "placeholder"
				},
				"inputsize" : {
					"label" : "宽度",
					"type" : "select",
					"selected" : "input-large",
					"value" : [ {
						"value" : "input-mini",
						"label" : "最窄"
					}, {
						"value" : "input-small",
						"label" : "窄"
					}, {
						"value" : "input-medium",
						"label" : "中"
					}, {
						"value" : "input-large",
						"label" : "宽"
					}, {
						"value" : "input-xlarge",
						"label" : "加宽"
					}, {
						"value" : "input-xxlarge",
						"label" : "最宽"
					} ]
				},
				"required" : {
					"label" : "必填？",
					"type" : "select",
					"selected" : "",
					"value" : [ {
						"value" : "",
						"label" : "否"
					}, {
						"value" : "required",
						"label" : "是"
					} ]
				}
			}
		}
	},
	create : function() {
		this.labelContainer.append('哈哈');
		this.controllerContainer.append('<input type="text" class="form-control input-sm">');
		return this.item;
	}
});