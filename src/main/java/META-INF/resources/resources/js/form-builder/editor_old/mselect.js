{
    "title": "多选下拉框",
    "fields": {
      "id": {
        "label": "ID/Name",
        "type": "input",
        "value": "selectbasic"
      },
      "form-label": {
        "label": "标签",
        "type": "input",
        "value": "多选下拉框"
      },
      "form-group": {
          "label": "分组",
          "type": "input",
          "value": ""
        },
      "reselect": {
        	"label": "数据源:",
        	"type": "radio",
        	"value": [
        	  {
        		  "value":"exist",
        		  "label":"数据集"
        	  },
        	  {
        		  "value":"custom",
        		  "label":"自定义"
        	  }
        	]
          },
          "parentbindname": {
              "label": "选择父控件",
              "type": "select",
              "multi": "multiple plugin-checkselect",
              "selected": "",
              "value": [
                  {
                      "value": "",
                      "label": "没有父数据源!"
                  }
              ]
          },
      "options": {
        "label": "选项",
        "type": "textarea-split",
        "value": [
          "选项一",
          "选项二"
        ]
      },
      "inputheight": {
        "label": "高度(像素)",
        "type": "input",
        "value" : "40"
      },
      "inputsize": {
        "label": "宽度",
        "type": "select",
        "selected": "input-medium",
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
          "label": "必选？",
          "type": "select",
          "selected": "",
          "value": [
            {
              "value": "",
              "label": "否"
            },
             {
              "value": "needselect",
              "label": "是"
            }
          ]
        }
    }
  }

