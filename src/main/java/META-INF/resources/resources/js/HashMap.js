function HashMap() {
	this.size = 0;
	this.map = new Object();
}

HashMap.prototype.put = function(key, value) {
	if (this.map[key] == null) {
		this.size++;
	}
	this.map[key] = value;
}
HashMap.prototype.get = function(key) {
	return this.isKey(key) ? this.map[key] : null;
}
HashMap.prototype.isKey = function(key) {
	return (key in this.map);
}
HashMap.prototype.remove = function(key) {
	if (this.isKey(key) && (delete this.map[key])) {
		this.size--;
	}
}

HashMap.prototype.size = function() {
	return this.size;
}

HashMap.prototype.find = function(_callback) {
	for ( var _key in this.map) {
		_callback.call(this, _key, this.map[_key]);
	}
}
