{
    "title": "多行单选框",
    "fields": {
        "id": {
            "label": "ID/Name",
            "type": "input",
            "value": "radios"
        },
        "form-label": {
            "label": "标签",
            "type": "input",
            "value": "多行单选框"
        },
        "form-group": {
            "label": "分组",
            "type": "input",
            "value": ""
          },
        "radios": {
            "label": "选项",
            "type": "textarea-split",
            "value": [
                "选项一",
                "选项二"
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
                "value": "radio",
                "label": "是"
              }
            ]
          }
    }
}