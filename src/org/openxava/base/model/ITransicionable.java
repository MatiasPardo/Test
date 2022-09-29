package org.openxava.base.model;

public interface ITransicionable {

	EstadoEntidad ejecutarTransicion(TransicionEstado transicion);

	EstadoEntidad getSubestado();
	
	void setSubestado(EstadoEntidad subestado);
	
	TransicionEstado getUltimaTransicion();
		
	void setUltimaTransicion(TransicionEstado ultimaTransicion);
	
}
