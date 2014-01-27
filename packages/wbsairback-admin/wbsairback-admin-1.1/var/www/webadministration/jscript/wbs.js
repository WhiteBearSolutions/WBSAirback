function checkUncheckAll(theElement) {
	var theForm = theElement.form, z = 0;
	for(z=0; z<theForm.length;z++){
		if(theForm[z].type == 'checkbox' && theForm[z].name != 'checkall'){
			theForm[z].checked = theElement.checked;
		}
	}
}
function submitForm(form) {
	showLoadingPage();
	eval(form);
}

function goLoading(url) {
	showLoadingPage();
	window.location.href = url;
}

function sendForm(form) {
	var urlBase=document.URL;
	urlBase=urlBase.substring(0,urlBase.indexOf('/',8));
	showLoadingPage();
	window.location = urlBase+form;
}

function showLoadingPage() {
	if (document.getElementById) { // DOM3 = IE5, NS6
		document.getElementById('hidepage').style.visibility = 'visible';
	} else {
		if (document.layers) { // Netscape 4
			document.hidepage.visibility = 'show';
		} else { // IE 4
			document.all.hidepage.style.visibility = 'visible';
		}
	}
}

function clavar(obj, posic)	{
	dObj =document.getElementById(obj);
	var a= window.pageYOffset+posic; dObj.style.top =a+'px';
}

function loadImages() {
	if (document.getElementById) { // DOM3 = IE5, NS6
		document.getElementById('hidepage').style.visibility = 'hidden';
	} else {
		if (document.layers) { // Netscape 4
			document.hidepage.visibility = 'hidden';
		} else { // IE 4
			document.all.hidepage.style.visibility = 'hidden';
		}
	}
	document.disabled = 'true';
}

function changeLanguage() {
	document.lang_form.submit();
}