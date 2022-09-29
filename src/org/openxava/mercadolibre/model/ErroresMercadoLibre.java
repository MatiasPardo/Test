package org.openxava.mercadolibre.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;

@Entity
public class ErroresMercadoLibre extends ObjetoNegocio{
	
	private static final int LONGITUD_ERRORES = 127;

	public ErroresMercadoLibre(){
	}
	
	public ErroresMercadoLibre(String descripcionRequest, String errorException){
		this.setRequest(descripcionRequest);
		this.setException(errorException);
	}
	
	@Column(length=LONGITUD_ERRORES)
	@ReadOnly
	private String request;
	
	@Column(length=LONGITUD_ERRORES)
	@ReadOnly
	private String exception;

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		if(request != null){
			if(request.length() > LONGITUD_ERRORES){
				this.request = request.substring(0, LONGITUD_ERRORES - 1);
			}else{
				this.request = request;
			}
		}
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		if(exception != null){
			if(exception.length() > LONGITUD_ERRORES){
				this.exception = exception.substring(0, LONGITUD_ERRORES - 1);
			}else{
				this.exception = exception;
			}
		}
	}


}
