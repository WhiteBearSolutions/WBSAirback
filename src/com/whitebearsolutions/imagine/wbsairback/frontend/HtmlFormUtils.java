package com.whitebearsolutions.imagine.wbsairback.frontend;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HtmlFormUtils {

	/**
	 * Devuelve la cadena html de un input text standard y un objeto de entidad Map<String,String>
	 * @param inputId
	 * @param labelText
	 * @param entity
	 * @param required
	 * @return
	 * @throws Exception
	 */
	public static String inputText(String inputId, String labelText, Map<String, String> entity, boolean required) throws Exception {
		StringBuilder _sb = new StringBuilder();
		
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<input class=\"form_text\" name=\""+inputId+"\" id=\""+inputId+"\"value=\"");
    	if (entity != null && !entity.isEmpty()) {
    		if (entity.get(inputId) != null)
    			_sb.append(entity.get(inputId));
    	}
    	_sb.append("\" />\n");
    	if (required)
    		_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
    	_sb.append("</div>\n");
    	_sb.append("<div class=\"clear\"></div>\n");
    	
    	return _sb.toString();
	}
	
	/**
	 * Devuelve la cadena html de un input text standard y un objeto de entidad Map<String,String>
	 * @param inputId
	 * @param labelText
	 * @param entity
	 * @param required
	 * @return
	 * @throws Exception
	 */
	public static String inputTextLabelTitle(String inputId, String labelText, Map<String, String> entity, String title, boolean required, boolean password) throws Exception {
		StringBuilder _sb = new StringBuilder();
		
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label title=\""+title+"\" for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<input class=\"form_text\" ");
		if (password)
			_sb.append(" type=\"password\" ");
		_sb.append("name=\""+inputId+"\" id=\""+inputId+"\"value=\"");
    	if (entity != null && !entity.isEmpty()) {
    		if (entity.get(inputId) != null)
    			_sb.append(entity.get(inputId));
    	}
    	_sb.append("\" />\n");
    	if (required)
    		_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
    	_sb.append("</div>\n");
    	_sb.append("<div class=\"clear\"></div>\n");
    	
    	return _sb.toString();
	}
	
	public static String inputTextLabelTitleSimple(String inputId, String labelText, String entity, String title, boolean required, boolean password) throws Exception {
		StringBuilder _sb = new StringBuilder();
		
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label title=\""+title+"\" for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<input class=\"form_text\" ");
		if (password)
			_sb.append(" type=\"password\" ");
		_sb.append("name=\""+inputId+"\" id=\""+inputId+"\"value=\"");
    	if (entity != null && !entity.isEmpty()) {
    			_sb.append(entity);
    	}
    	_sb.append("\" />\n");
    	if (required)
    		_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
    	_sb.append("</div>\n");
    	_sb.append("<div class=\"clear\"></div>\n");
    	
    	return _sb.toString();
	}
	
	
	/**
	 * Devuelve la cadena html de un input text standard y un objeto de entidad Map<String, Object>
	 * @param inputId
	 * @param labelText
	 * @param entity
	 * @param required
	 * @return
	 * @throws Exception
	 */
	public static String inputTextObj(String inputId, String labelText, Map<String, Object> entity, boolean required) throws Exception {
		StringBuilder _sb = new StringBuilder();
		
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<input class=\"form_text\" name=\""+inputId+"\" id=\""+inputId+"\" value=\"");
    	if (entity != null && !entity.isEmpty()) {
    		if (entity.get(inputId) != null)
    			_sb.append(entity.get(inputId));
    	}
    	_sb.append("\" />\n");
    	if (required)
    		_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
    	_sb.append("</div>\n");
    	_sb.append("<div class=\"clear\"></div>\n");
    	
    	return _sb.toString();
	}
	
	public static String inputTextObj(String inputId, String labelText, Map<String, Object> entity, boolean required, String clase) throws Exception {
		StringBuilder _sb = new StringBuilder();
		
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<input class=\""+clase+" form_text\" name=\""+inputId+"\" id=\""+inputId+"\" value=\"");
    	if (entity != null && !entity.isEmpty()) {
    		if (entity.get(inputId) != null)
    			_sb.append(entity.get(inputId));
    	}
    	_sb.append("\" />\n");
    	if (required)
    		_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
    	_sb.append("</div>\n");
    	_sb.append("<div class=\"clear\"></div>\n");
    	
    	return _sb.toString();
	}
	
	
	/**
	 * Devuelve la cadena html de un input text standard y un objeto de entidad Map<String, Object>
	 * @param inputId
	 * @param labelText
	 * @param entity
	 * @param required
	 * @return
	 * @throws Exception
	 */
	public static String inputTextObjReadOnly(String inputId, String labelText, Map<String, Object> entity, boolean required) throws Exception {
		StringBuilder _sb = new StringBuilder();
		
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<input class=\"form_text\" readOnly=\"readOnly\" name=\""+inputId+"\" id=\""+inputId+"\"  style=\"background-color:#E6E9EA;\" value=\"");
    	if (entity != null && !entity.isEmpty()) {
    		if (entity.get(inputId) != null)
    			_sb.append(entity.get(inputId));
    	}
    	_sb.append("\" />\n");
    	if (required)
    		_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
    	_sb.append("</div>\n");
    	_sb.append("<div class=\"clear\"></div>\n");
    	
    	return _sb.toString();
	}
	
	public static String inputTextObjReadOnly(String inputId, String labelText, Map<String, Object> entity, boolean required, String clase) throws Exception {
		StringBuilder _sb = new StringBuilder();
		
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<input class=\""+clase+" form_text\" readOnly=\"readOnly\" name=\""+inputId+"\" id=\""+inputId+"\"  style=\"background-color:#E6E9EA;\" value=\"");
    	if (entity != null && !entity.isEmpty()) {
    		if (entity.get(inputId) != null)
    			_sb.append(entity.get(inputId));
    	}
    	_sb.append("\" />\n");
    	if (required)
    		_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
    	_sb.append("</div>\n");
    	_sb.append("<div class=\"clear\"></div>\n");
    	
    	return _sb.toString();
	}
	
	
	/**
	 * Devuelve la cadena html de un select standard y un objeto de entidad Map<String,String>
	 * @param inputId
	 * @param labelText
	 * @param entity
	 * @param selectValues
	 * @param required
	 * @return
	 * @throws Exception
	 */
	public static String selectOption(String inputId, String labelText, Map<String, String> entity, Map<String, String> selectValues, boolean required) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<select class=\"form_select\" name=\""+inputId+"\" id=\""+inputId+"\" >\n");
		for (String key : selectValues.keySet()) {
			String text = selectValues.get(key);
			_sb.append("<option value=\""+key+"\" ");
	    	if (entity != null && !entity.isEmpty()) {
	    		if (entity.get(inputId) != null && entity.get(inputId).equals(key))
	    			_sb.append("selected=\"selected\" ");
	    	}
	    	_sb.append(">"+text+"</option>\n");
		}
    	
		_sb.append("</select>\n");
		if (required)
			_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
		_sb.append("</div>\n");
		_sb.append("<div class=\"clear\"></div>\n");
		
		return _sb.toString();
	}
	
	public static String selectOptionSimple(String inputId, String labelText, String entity, Map<String, String> selectValues, boolean required) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<select class=\"form_select\" name=\""+inputId+"\" id=\""+inputId+"\" >\n");
		for (String key : selectValues.keySet()) {
			String text = selectValues.get(key);
			_sb.append("<option value=\""+key+"\" ");
	    	if (entity != null && !entity.isEmpty()) {
	    		if (entity.equals(key))
	    			_sb.append("selected=\"selected\" ");
	    	}
	    	_sb.append(">"+text+"</option>\n");
		}
    	
		_sb.append("</select>\n");
		if (required)
			_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
		_sb.append("</div>\n");
		_sb.append("<div class=\"clear\"></div>\n");
		
		return _sb.toString();
	}
	
	
	/**
	 * Devuelve la cadena html de un select standard y un objeto de entidad Map<String,String>
	 * @param inputId
	 * @param labelText
	 * @param entity
	 * @param selectValues
	 * @param required
	 * @return
	 * @throws Exception
	 */
	public static String selectOptionReadOnly(String inputId, String labelText, Map<String, String> entity, Map<String, String> selectValues, boolean required) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<input type=\"hidden\" name=\""+inputId+"\" id=\""+inputId+"\" value=\""+entity.get(inputId)+"\"/>\n");
		_sb.append("<select class=\"form_select\" name=\""+inputId+"\" id=\""+inputId+"\" disabled=\"disabled\" style=\"background-color:#E6E9EA;\" >\n");
		for (String key : selectValues.keySet()) {
			String text = selectValues.get(key);
			_sb.append("<option value=\""+key+"\" ");
	    	if (entity != null && !entity.isEmpty()) {
	    		if (entity.get(inputId) != null && entity.get(inputId).equals(key))
	    			_sb.append("selected=\"selected\" ");
	    	}
	    	_sb.append(">"+text+"</option>\n");
		}
    	
		_sb.append("</select>\n");
		if (required)
			_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
		_sb.append("</div>\n");
		_sb.append("<div class=\"clear\"></div>\n");
		
		return _sb.toString();
	}
	
	
	public static String selectOption(String inputId, String labelText, Map<String, String> entity, Map<String, String> selectValues, boolean required, String onChange) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<select class=\"form_select\" name=\""+inputId+"\" id=\""+inputId+"\" onChange=\""+onChange+"\">\n");
		for (String key : selectValues.keySet()) {
			String text = selectValues.get(key);
			_sb.append("<option value=\""+key+"\" ");
	    	if (entity != null && !entity.isEmpty()) {
	    		if (entity.get(inputId) != null && entity.get(inputId).equals(key))
	    			_sb.append("selected=\"selected\" ");
	    	}
	    	_sb.append(">"+text+"</option>\n");
		}
    	
		_sb.append("</select>\n");
		if (required)
			_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
		_sb.append("</div>\n");
		_sb.append("<div class=\"clear\"></div>\n");
		
		return _sb.toString();
	}
	
	
	/**
	 * Devuelve la cadena html de un select standard y un objeto de entidad Map<String,String>
	 * @param inputId
	 * @param labelText
	 * @param entity
	 * @param selectValues
	 * @param required
	 * @return
	 * @throws Exception
	 */
	public static String selectOption(String inputId, String labelText, Map<String, String> entity, List<String> selectValues, boolean required, String onChange) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<select class=\"form_select\" name=\""+inputId+"\" id=\""+inputId+"\" onChange=\""+onChange+"\" >\n");
		for (String val : selectValues) {
			_sb.append("<option value=\""+val+"\" ");
	    	if (entity != null && !entity.isEmpty()) {
	    		if (entity.get(inputId) != null && entity.get(inputId).equals(val))
	    			_sb.append("selected=\"selected\" ");
	    	}
	    	_sb.append(">"+val+"</option>\n");
		}
    	
		_sb.append("</select>\n");
		if (required)
			_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
		_sb.append("</div>\n");
		_sb.append("<div class=\"clear\"></div>\n");
		
		return _sb.toString();
	}
	
	
	/**
	 * Devuelve la cadena html de un select standard y un objeto de entidad Map<String,String>
	 * @param inputId
	 * @param labelText
	 * @param entity
	 * @param selectValues
	 * @param required
	 * @return
	 * @throws Exception
	 */
	public static String selectOption(String inputId, String labelText, Map<String, String> entity, List<String> selectValues, boolean required) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<select class=\"form_select\" name=\""+inputId+"\" id=\""+inputId+"\" >\n");
		for (String val : selectValues) {
			_sb.append("<option value=\""+val+"\" ");
	    	if (entity != null && !entity.isEmpty()) {
	    		if (entity.get(inputId) != null && entity.get(inputId).equals(val))
	    			_sb.append("selected=\"selected\" ");
	    	}
	    	_sb.append(">"+val+"</option>\n");
		}
    	
		_sb.append("</select>\n");
		if (required)
			_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
		_sb.append("</div>\n");
		_sb.append("<div class=\"clear\"></div>\n");
		
		return _sb.toString();
	}
	
	public static String selectOptionSimple(String inputId, String labelText, String entity, List<String> selectValues, boolean required) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<select class=\"form_select\" name=\""+inputId+"\" id=\""+inputId+"\" >\n");
		for (String val : selectValues) {
			_sb.append("<option value=\""+val+"\" ");
	    	if (entity != null && !entity.isEmpty() && entity.equals(val)) {
	    			_sb.append("selected=\"selected\" ");
	    	}
	    	_sb.append(">"+val+"</option>\n");
		}
    	
		_sb.append("</select>\n");
		if (required)
			_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
		_sb.append("</div>\n");
		_sb.append("<div class=\"clear\"></div>\n");
		
		return _sb.toString();
	}
	
	
	/**
	 * Devuelve la cadena html de un select standard y un objeto de entidad Map<String,Object>
	 * @param inputId
	 * @param labelText
	 * @param entity
	 * @param selectValues
	 * @param required
	 * @return
	 * @throws Exception
	 */
	public static String selectOptionObj(String inputId, String labelText, Map<String, Object> entity, Map<String, String> selectValues, boolean required) throws Exception {
		return selectOptionObj(inputId, labelText, entity, selectValues, required,null);
	}
	
	public static String selectOptionObj(String inputId, String labelText, Map<String, Object> entity, List<String> selectValues, boolean required, boolean multiple) throws Exception {
		return selectOptionObjList(inputId, labelText, entity, selectValues, required, null, multiple);
	}
	
	
	/**
	 * Devuelve la cadena html de un select standard y un objeto de entidad Map<String,Object>
	 * @param inputId
	 * @param labelText
	 * @param entity
	 * @param selectValues
	 * @param required
	 * @param onchange
	 * @return
	 * @throws Exception
	 */
	public static String selectOptionObj(String inputId, String labelText, Map<String, Object> entity, Map<String, String> selectValues, boolean required, String onChange) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		_sb.append("<select "+(onChange!=null && !onChange.isEmpty() ? "onchange=\""+onChange+"\"" : "")+" class=\"form_select\" name=\""+inputId+"\" id=\""+inputId+"\" />\n");
		for (String key : selectValues.keySet()) {
			String text = selectValues.get(key);
			_sb.append("<option value=\""+key+"\" ");
	    	if (entity != null && !entity.isEmpty()) {
	    		if (entity.get(inputId) != null && entity.get(inputId).equals(key))
	    			_sb.append("selected=\"selected\" ");
	    	}
	    	_sb.append(">"+text+"</option>\n");
		}
    	
		_sb.append("</select>\n");
		if (required)
			_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
		_sb.append("</div>\n");
		_sb.append("<div class=\"clear\"></div>\n");
		
		return _sb.toString();
	}
	
	public static String selectOptionObjList(String inputId, String labelText, Map<String, Object> entity, List<String> selectValues, boolean required, String onChange, boolean multiple) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<div class=\"standard_form\">\n");
		_sb.append("<label for=\""+inputId+"\">");
		_sb.append(labelText);
		_sb.append(": </label>\n");
		String sMultiple = "";
		if (multiple)
			sMultiple = " multiple=\"multiple\" ";
		_sb.append("<select "+(onChange!=null && !onChange.isEmpty() ? "onchange=\""+onChange+"\"" : "")+" class=\"form_select\" name=\""+inputId+"\" id=\""+inputId+"\" "+sMultiple+" />\n");
		for (String key : selectValues) {
			_sb.append("<option value=\""+key+"\" ");
	    	if (entity != null && !entity.isEmpty()) {
	    		if (entity.get(inputId) != null && entity.get(inputId).equals(key))
	    			_sb.append("selected=\"selected\" ");
	    	}
	    	_sb.append(">"+key+"</option>\n");
		}
    	
		_sb.append("</select>\n");
		if (required)
			_sb.append("<img src=\"/images/asterisk_orange_16.png\"/>\n");
		_sb.append("</div>\n");
		_sb.append("<div class=\"clear\"></div>\n");
		
		return _sb.toString();
	}
	
	/**
	 * Devuelve la cadena html de un boton de guardar standard
	 * @param formId
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public static String saveHeaderButton(String formId, String text) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<a href=\"javascript:submitForm(document."+formId+".submit());\"><img src=\"/images/disk_16.png\" title=\"");
		_sb.append(text);
		_sb.append("\" alt=\"");
		_sb.append(text);
		_sb.append("\"/></a>\n");
		return _sb.toString();
	}
	
	public static String saveHeaderButtonEngineValidation(String formId, String text) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<a href=\"javascript:if ($('#"+formId+"').validationEngine('validate')) submitForm(document."+formId+".submit());\"><img src=\"/images/disk_16.png\" title=\"");
		_sb.append(text);
		_sb.append("\" alt=\"");
		_sb.append(text);
		_sb.append("\"/></a>\n");
		return _sb.toString();
	}
	
	
	/**
	 * Devuelve la cadena html de un boton de añadir standard
	 * @param baseUrl
	 * @param type
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public static String addHeaderButton(String baseUrl, Integer type, String text) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<a href=\""+baseUrl+"?type=");
		_sb.append(type);
		_sb.append("\"><img src=\"/images/add_16.png\" title=\"");
		_sb.append(text);
		_sb.append("\" alt=\"");
		_sb.append(text);
		_sb.append("\"/></a>\n");
		return _sb.toString();
	}
	
	public static String addReloadButton(String baseUrl, String text) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<a href=\"javascript:submitForm(document.location.reload());\"><img src=\"/images/arrow_refresh_16.png\" title=\"");
		_sb.append(text);
		_sb.append("\" alt=\"");
		_sb.append(text);
		_sb.append("\"/></a>\n");
		return _sb.toString();
	}
	
	/**
	 * Devuelve la cadena html de un boton de añadir standard
	 * @param baseUrl
	 * @param type
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public static String addHeaderButton(String baseUrl, Integer type, String text, String params) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<a href=\""+baseUrl+"?"+params+"&type=");
		_sb.append(type);
		_sb.append("\"><img src=\"/images/add_16.png\" title=\"");
		_sb.append(text);
		_sb.append("\" alt=\"");
		_sb.append(text);
		_sb.append("\"/></a>\n");
		return _sb.toString();
	}
	
	
	/**
	 * Devuelve la cadena html de un boton de añadir standard
	 * @param baseUrl
	 * @param type
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public static String addHeaderButtonSubmit(String baseUrl, Integer type, String text) throws Exception {		
		return addHeaderButtonSubmit(baseUrl, type, text,null);
	}
	
	/**
	 * Devuelve la cadena html de un boton de añadir standard
	 * @param baseUrl
	 * @param type
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public static String addHeaderButtonSubmit(Integer type, String text) throws Exception {		
		return addHeaderButtonSubmit(null, type, text,null);
	}
	
	/**
	 * Devuelve la cadena html de un boton de añadir standard
	 * @param baseUrl
	 * @param type
	 * @param text
	 * @param nameForm
	 * @return
	 * @throws Exception
	 */
	public static String addHeaderButtonSubmit(String baseUrl, Integer type, String text, String form) throws Exception {
		return addHeaderButtonSubmit(type, text, form, null);
	}
	
	/**
	 * Devuelve la cadena html de un boton de añadir standard
	 * @param baseUrl
	 * @param type
	 * @param text
	 * @param nameForm
	 * @param javascriptPre
	 * @return
	 * @throws Exception
	 */
	public static String addHeaderButtonSubmit(Integer type, String text, String form, String javascriptPre) throws Exception {
		StringBuilder _sb = new StringBuilder();
		StringBuilder _formName=new StringBuilder();
		StringBuilder _script=new StringBuilder();
		StringBuilder _typeB=new StringBuilder();
		if (javascriptPre!=null && !javascriptPre.isEmpty()){
			_script.append(javascriptPre);			
		}
		if (type!=null && type!=-1){
			if (form!=null && !form.isEmpty()){
				_typeB.append("document.form.type.value="+type+";");
			} else {
				_typeB.append("document.getElementById('type').value="+type+";");
			}
		}
		if (form!=null && !form.isEmpty()){
			if (!"noSubmitForm".equals(form)){
				_formName.append("submitForm(document."+form+".submit());");
			}
		} else{
			_formName.append("submitForm(document.form.submit());");		
		}
		_sb.append("<a href=\"javascript:"+_script.toString()+_typeB.toString()+_formName.toString()+"\"><img src=\"/images/add_16.png\" title=\"");
		_sb.append(text);
		_sb.append("\" alt=\"");
		_sb.append(text);
		_sb.append("\"/></a>\n");
		return _sb.toString();
	}
	
	
	
	/**
	 * Devuelve la cadena html de un boton de editar standard
	 * @param baseUrl
	 * @param type
	 * @param text
	 * @param img
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String editButton(String baseUrl, Integer type, String text, String img, String params) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<a href=\""+baseUrl+"?type=");
		_sb.append(type);
		_sb.append(params);
		_sb.append("\"><img src=\"/images/"+img+"\" title=\"");
		_sb.append(text);
		_sb.append("\" alt=\"");
		_sb.append(text);
		_sb.append("\"/></a>\n");
		return _sb.toString();
	}
	
	
	/**
	 * Devuelve la cadena html de un boton de borrar standard
	 * @param baseUrl
	 * @param type
	 * @param text
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String removeButton(String baseUrl, Integer type, String text, String params) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<a href=\""+baseUrl+"?type=");
		_sb.append(type);
		_sb.append(params);
		_sb.append("\"><img src=\"/images/delete_16.png\" title=\"");
		_sb.append(text);
		_sb.append("\" alt=\"");
		_sb.append(text);
		_sb.append("\"/></a>\n");
         return _sb.toString();
	}
	
	public static String getNoResultsWindow(String title, String text) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<div class=\"window\">");
		_sb.append("<h2>");
		_sb.append(title);
		_sb.append("</h2>");
		_sb.append("<fieldset>");
		_sb.append("<table>");
    	_sb.append("<tr>");
    	_sb.append("<td>");
    	_sb.append(text);
    	_sb.append("</td>");
        _sb.append("</tr>");
        _sb.append("</table>");
        _sb.append("<br />");
        _sb.append("</fieldset>");
        _sb.append("<div class=\"clear\"/></div>");
        _sb.append("</div>");
        return _sb.toString();
	}
	
	public static String printJSValidationHeader(Locale locale) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/validationEngine.jquery.css\" />\n");
		 if("es".equals(locale.getLanguage()))
			 _sb.append("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-es.js\"></script>\n");
		 else
			 _sb.append("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine-en.js\"></script>\n");
		 _sb.append("<script type=\"text/javascript\" src=\"/jscript/jquery.validationEngine.js\"></script>\n");
		 _sb.append("<script type=\"text/javascript\" src=\"/jscript/jquery.multiselect.min.js\"></script>\n");
		 _sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/jquery.multiselect.css\" />\n");
		return _sb.toString();
	}
	
	public static String printJSTimePickerHeader(Locale locale) throws Exception {
		StringBuilder _sb = new StringBuilder();
		 _sb.append("<script type=\"text/javascript\" src=\"/jscript/jquery-ui-timepicker.js\"></script>\n");
		 if (locale.getLanguage().equals("es"))
 			_sb.append("<script type=\"text/javascript\" src=\"/jscript/jquery.ui.datepicker-es.js\"></script>\n");
		return _sb.toString();
	}
	
	public static String printJSJqgridHeader(Locale locale) throws Exception {
		StringBuilder _sb = new StringBuilder();
		 _sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/ui.jqgrid.css\" />\n");
		if("es".equals(locale.getLanguage()))
			 _sb.append("<script type=\"text/javascript\" src=\"/jscript/grid.locale-es.js\"></script>\n");
		else
			 _sb.append("<script type=\"text/javascript\" src=\"/jscript/grid.locale-en.js\"></script>\n");
		 _sb.append("<script type=\"text/javascript\" src=\"/jscript/jquery.jqGrid.min.js\"></script>\n");
	    return _sb.toString();
	}
	
	public static String printJSJqMultiSelection() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/jquery.lwMultiSelect.css\" />\n");
		_sb.append("<script type=\"text/javascript\" src=\"/jscript/jquery.lwMultiSelect.js\"></script>\n");
		return _sb.toString();
	}
	
	public static String printJsApprise() throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<script type=\"text/javascript\" src=\"/jscript/apprise.min.js\"></script>\n");
		_sb.append("<link media=\"screen\" type=\"text/css\" href=\"/css/apprise.min.css\" rel=\"stylesheet\" />\n");
		return _sb.toString();
	}
	
	public static String getInfoTooltip(String text) throws Exception {
		StringBuilder _sb = new StringBuilder();
		_sb.append("<img src=\"/images/information_16.png\" style=\"vertical-align:middle;cursor:help;\" class=\"tooltip\" alt=\"+info\" title=\""+text+"\" /> ");
		return _sb.toString();
	}

}
