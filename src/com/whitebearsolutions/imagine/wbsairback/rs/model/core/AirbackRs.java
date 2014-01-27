package com.whitebearsolutions.imagine.wbsairback.rs.model.core;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Clase principal de respuesta Restful
 * @author jorgewb
 *
 */
@XmlRootElement(name="wbsairback")
public class AirbackRs {

	private AirbackRequestRs request;
	private AirbackResponseRs response;
	
	public AirbackRs() {}

	public AirbackResponseRs getResponse() {
		return response;
	}

	public void setResponse(AirbackResponseRs response) {
		this.response = response;
	}

	public AirbackRequestRs getRequest() {
		return request;
	}

	public void setRequest(AirbackRequestRs request) {
		this.request = request;
	};
}
