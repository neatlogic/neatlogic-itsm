{
    "title": "时间控件",
    "fields": {
      "id": {
        "label": "ID/Name",
        "type": "input",
        "value": "time"
      },
      "form-label": {
        "label": "标签",
        "type": "input",
        "value": "时间控件"
      },
      "form-group": {
          "label": "分组",
          "type": "input",
          "value": ""
        },
      "timeformat": {
        "label": "时间格式",
        "type": "input",
        "value": "yyyy-MM-dd HH:mm"
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
