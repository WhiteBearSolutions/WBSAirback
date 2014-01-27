
	var vSelectFrom = "";
	var vLang = "en";
 
function writeAppletTag(vLoginName, vPwd, vLang, vSessionID, vSelectFrom, vProductName) {
	document.writeln('<APPLET CODE = "com.ahsay.obr.restorer.class" ');
	document.writeln(' CODEBASE = "/obs/jsp/applet/obr" ARCHIVE = "obrA.jar" ');
	document.writeln(' WIDTH = "80" HEIGHT = "25" NAME="restorer">');
	document.writeln('<PARAM NAME = "cabinets" VALUE = "obrA.cab">');
	document.writeln('<PARAM NAME = "scriptable" VALUE = "false">');
	document.writeln('<PARAM NAME = "LOGIN_NAME" VALUE = "' + vLoginName + '">');
	document.writeln('<PARAM NAME = "PASSWORD" VALUE = "' + vPwd + '">');
	document.writeln('<PARAM NAME = "USING_PLUGIN" VALUE = "N">');
	document.writeln('<PARAM NAME = "LANGUAGE" VALUE = "' + vLang + '">');
	document.writeln('<PARAM NAME = "SESSION_ID" VALUE = "' + vSessionID + '">');
	document.writeln('<PARAM NAME = "SELECT_FROM" VALUE = "' + vSelectFrom + '">');
	document.writeln('<PARAM NAME = "PRODUCT_NAME" VALUE = "' + vProductName + '">');
	document.writeln('Es necesario un explorador compatible con JDK 1.1.');
	document.writeln('</APPLET>');
}
 
function writeObjectTag(vLoginName, vPwd, vLang, vSessionID, vSelectFrom, vProductName) {
	document.writeln('<OBJECT classid = "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" ');
	document.writeln(' codebase = "/obs/jsp/applet/plugin/j2re.exe#Version=1,2,0,0" ');
	document.writeln(' WIDTH = "80" HEIGHT = "25" align = "baseline">');
	document.writeln('<PARAM NAME = "code" VALUE = "com.ahsay.obr.restorer.class">');
	document.writeln('<PARAM NAME = "name" VALUE = "restorer">');
	document.writeln('<PARAM NAME = "codebase" VALUE = "/obs/jsp/applet/obr">');
	document.writeln('<PARAM NAME = "archive" VALUE = "obrA.jar">');
	document.writeln('<PARAM NAME = "type" VALUE = "application/x-java-applet;version=1.2">');
	document.writeln('<PARAM NAME = "scriptable" VALUE = "false">');
	document.writeln('<PARAM NAME = "LOGIN_NAME" VALUE = "' + vLoginName + '">');
	document.writeln('<PARAM NAME = "PASSWORD" VALUE = "' + vPwd + '">');
	document.writeln('<PARAM NAME = "USING_PLUGIN" VALUE = "Y">');
	document.writeln('<PARAM NAME = "LANGUAGE" VALUE = "' + vLang + '">');
	document.writeln('<PARAM NAME = "SESSION_ID" VALUE = "' + vSessionID + '">');
	document.writeln('<PARAM NAME = "SELECT_FROM" VALUE = "' + vSelectFrom + '">');
	document.writeln('<PARAM NAME = "PRODUCT_NAME" VALUE = "' + vProductName + '">');
	document.writeln('<COMMENT>');
	document.writeln('<EMBED');
	document.writeln('archive = "obrA.jar"');
	document.writeln('type = "application/x-java-applet;version=1.2"');
	document.writeln('JAVA_CODEBASE = "/obs/jsp/applet/obr"');
	document.writeln('CODE = "com.ahsay.obr.restorer.class"');
	document.writeln('NAME = "restorer"');
	document.writeln('WIDTH = "80"');
	document.writeln('HEIGHT = "25"');
	document.writeln('LOGIN_NAME = "' + vLoginName + '"');
	document.writeln('PASSWORD = "' + vPwd + '"');
	document.writeln('USING_PLUGIN= "Y"');
	document.writeln('LANGUAGE = "' + vLang + '"');
	document.writeln('SESSION_ID = "' + vSessionID + '"');
	document.writeln('SELECT_FROM = "' + vSelectFrom + '"');
	document.writeln('PRODUCT_NAME = "' + vProductName + '"');
	document.writeln('scriptable = "false"');
	document.writeln('pluginspage="http://java.sun.com/products/plugin/index.html#download">');
	document.writeln('<NOEMBED>');
	document.writeln('Por favor use el Plug-in Sun Java2 Plug-in para desencriptar los archivos de respaldo');
	document.writeln('</NOEMBED>');
	document.writeln('</EMBED>');
	document.writeln('</COMMENT>');
	document.writeln('</OBJECT>');
}





function writeRestorerTag() {
	if(navigator.userAgent.indexOf("Opera") != -1) {
		writeAppletTag(vLoginName, vPwd, vLang, vSessionID, vSelectFrom, vProductName);
	}
	else if(navigator.userAgent.indexOf("AOL") != -1) {
		writeAppletTag(vLoginName, vPwd, vLang, vSessionID, vSelectFrom, vProductName);
	}
	else if (navigator.platform.toLowerCase().indexOf("win") == 0) {
		writeObjectTag(vLoginName, vPwd, vLang, vSessionID, vSelectFrom, vProductName);
	}
	else {
		writeAppletTag(vLoginName, vPwd, vLang, vSessionID, vSelectFrom, vProductName);
	}
} 