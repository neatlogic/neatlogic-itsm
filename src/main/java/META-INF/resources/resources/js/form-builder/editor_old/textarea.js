{
    "title": "文本域",
    "fields": {
        "id": {
            "label": "ID/Name",
            "type": "input",
            "value": "textarea"
        },
        "form-label": {
            "label": "标签",
            "type": "input",
            "value": "文本域"
        },
        "form-group": {
            "label": "分组",
            "type": "input",
            "value": ""
          },
        "placeholder": {
            "label": "输入提示",
            "type": "input",
            "value": "placeholder"
          },
        "inputheight": {
	        "label": "高度(像素)",
	        "type": "input",
	        "value" : "40"
	      },
        "inputsize": {
            "label": "宽度",
            "type": "select",
            "selected": "input-large",
            "value": [
                {
                    "value": "input-mini",
                    "label": "最窄"
                },
                {
                    "value": "input-small",
                    "label": "窄"
                },
                {
                    "value": "input-medium",
                    "label": "中"
                },
                {
                    "value": "input-large",
                    "label": "宽"
                },
                {
                    "value": "input-xlarge",
                    "label": "加宽"
                },
                {
                    "value": "input-xxlarge",
                    "label": "最宽"
                }
            ]
        },
        "required": {
            "label": "必填？",
            "type": "select",
            "selected": "",
            "value": [
                {
                    "value": "",
                    "label": "否"
                },
                {
                    "value": "required",
                    "label": "是"
                }
            ]
        }
    }
}