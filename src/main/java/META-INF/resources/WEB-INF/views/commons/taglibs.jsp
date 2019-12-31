<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="/WEB-INF/tld/toolkit.tld" prefix="tk"%>
<%@ taglib uri="/WEB-INF/tld/tag.tld" prefix="auth"%>
<%-- <c:choose>
	<c:when test="${empty cookie.locale || cookie.locale.value == 'zh_CN' }">
		<fmt:setLocale value="zh_CN" />
		<fmt:setBundle basename="simplechinese" var="lang" />
	</c:when>
	<c:when test="${cookie.locale.value == 'en_US'}">
		<fmt:setLocale value="en_US" />
		<fmt:setBundle basename="english" var="lang" />
	</c:when>
</c:choose>
--%>
