var Browser = {};
Browser.check = Class.create();

Browser.check.prototype.initialize = function() {
	this.ver = parseFloat(navigator.appVersion);
	this.appver = navigator.appVersion;
	this.agent = navigator.userAgent;
	this.name = navigator.appName;
	this.vendor = navigator.vendor ? navigator.vendor : '';
	this.vendver = navigator.vendorSub ? parseFloat(navigator.vendorSub) : 0;
	this.product = navigator.product ? navigator.product : '';
	this.platform = String(navigator.platform).toLowerCase();

	this.lang = (navigator.language) ? navigator.language.substring(0, 2)
			: (navigator.browserLanguage) ? navigator.browserLanguage.substring(0, 2)
			: (navigator.systemLanguage) ? navigator.systemLanguage.substring(0, 2) : 'zh';

	this.win = (this.platform.indexOf('win') >= 0) ? true : false;
	this.mac = (this.platform.indexOf('mac') >= 0) ? true : false;
	this.linux = (this.platform.indexOf('linux') >= 0) ? true : false;
	this.unix = (this.platform.indexOf('unix') >= 0) ? true : false;
	this.ie = (document.all) ? true : false;
	this.ie5 = (this.appver.indexOf('MSIE 5') > 0);
	this.ie6 = (this.appver.indexOf('MSIE 6') > 0);
	this.mz = (this.ver >= 5);
	this.ns = ((this.ver < 5 && this.name == 'Netscape') || (this.ver >= 5 && this.vendor
			.indexOf('Netscape') >= 0));
	this.ns6 = (this.ns && parseInt(this.vendver) == 6);
	this.ns60 = (this.ns && this.agent.indexOf('Netscape6/6.0') > 0);
	this.safari = this.agent.toLowerCase().indexOf('safari') > 0;
	this.konq = (this.agent.toLowerCase().indexOf('konqueror') > 0);
	this.opera = (window.opera) ? true : false;
	this.opera5 = (this.opera5 && this.agent.indexOf('Opera 5') > 0) ? true : false;

	if (this.opera && window.RegExp)
		this.vendver = (/opera(\s|\/)([0-9\.]+)/i.test(navigator.userAgent)) ? parseFloat(RegExp.$2) : -1;
	else if (!this.vendver && this.safari)
		this.vendver = (/safari\/([0-9]+)/i.test(this.agent)) ? parseInt(RegExp.$1) : 0;
	else if (!this.vendver && this.mz)
		this.vendver = (/rv:([0-9\.]+)/.test(this.agent)) ? parseFloat(RegExp.$1) : 0;
	else if (this.ie && window.RegExp)
		this.vendver = (/msie\s+([0-9\.]+)/i.test(this.agent)) ? parseFloat(RegExp.$1) : 0;

	// get real language out of safari's user agent
	if (this.safari && (/;\s+([a-z]{2})-[a-z]{2}\)/i.test(this.agent)))
		this.lang = RegExp.$1;

	this.vml = (this.win && this.ie && !this.opera);
	this.cookies = navigator.cookieEnabled;
}

var bw = new Browser.check();