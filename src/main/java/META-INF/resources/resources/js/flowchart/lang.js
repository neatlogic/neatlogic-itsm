// --- ENGLISH ---
Flowchart.prototype.language.en = {
	c1 : 'Do you really want to delete these ',
	c2 : 'selected objects?',
	c3 : 'Edit',
	c4 : 'Delete',
	c5 : 'Move line',
	c6 : 'Change arrow direction',
	c7 : 'Change color',
	c8 : 'Connect',
	c9 : 'Remove connection',
	c10 : 'New object',
	c11 : 'This connection already exists!',
	c12 : 'Can not connect! You have to add 2 Objects first',
	c13 : 'Adjust alignment',
	c14 : 'Export',
	c15 : 'save'
};

// --- Chinese ---
Flowchart.prototype.language.zh = {
	c1 : '您确定要删除这些',
	c2 : '请选择操作对象?',
	c3 : '编辑',
	c4 : '删除',
	c5 : '移动',
	c6 : '更改箭头方向',
	c7 : '更改颜色',
	c8 : '连接',
	c9 : '删除连接',
	c10 : '创建新对象',
	c11 : '此连接已存在！',
	c12 : '暂时无法连接！你必须先添加2个对象!',
	c13 : '调整对齐',
	c14 : '导出',
	c15 : '保存' 
};

Flowchart.prototype.getText = function(id) {
	return this.language[this.lang][id] ? this.language[this.lang][id]
			: 'translate: ' + id;
}
