<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ include file="WEB-INF/views/common/taglibs.jsp"%>
<html> 
<head> 
<%@ include file="./WEB-INF/views/common/meta.jsp"%>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> 
<title>Drone::Hive Workbench</title>
<link rel="shortcut icon" type="image/x-icon" href="<c:url value='/resources/images/icons/favicon.ico'/>" />

<script>
	<c:if test="${param.errorCode != null}">
		var errorCode = Number("${param.errorCode}");
		switch(errorCode){
			case 100:
				alert("UserID or Password require.");				
				break;
			case 101:
				alert("Wrong User Id or Password");				
				break;
			default:
				break;
		}
	</c:if>
</script>
<style type="text/css">
  td {font-size:9pt;}
  th {font-size:9pt;}
  body {font-size:9pt;}
  p {font-size:9pt;} 
</style>
</head> 
<body> 
<table width="100%" height="100%"> 
<tr valign="middle">
	<td align="center">
		<form id="loginForm" name="loginForm" method="post" action="j_spring_security_check">
			<table border=0>
				<tr>
					<td align="right">User : </td><td><input type="text" id="userId" name="userId"/></td>
				</tr>
				<tr>	
					<td>Password : </td><td><input type="password" id="password" name="password"/></td>
				</tr>
				<tr>
					<td colspan="2" align="right" height="25"><input type="submit" value="LOGIN"></td>
				</tr>
				<tr>
					<td colspan="2" align="center" height="30"></td>
				</tr>
				<tr>
					<td colspan="2" align="center">Copyright Gruter 2012<br/>www.gruter.com
					<br /><a href="mailto:contact@gruter.com">Contact Us</a></td>
				</tr>
			</table>
		</form>
	</td>
</tr>
</table> 
</body> 
</html> 