package org.openxava.tesoreria.model;

import java.util.Date;

public interface IChequera {
	
	public Long getProximoNumeroChequera();
	
	public void setProximoNumeroChequera(Long proximoNumeroChequera);
	
	public Long getUltimoNumeroChequera();
	
	public String getId();
	
	public Date getFechaCreacion();
	
	public Banco getBanco();
}
