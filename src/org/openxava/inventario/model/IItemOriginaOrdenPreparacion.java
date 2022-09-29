package org.openxava.inventario.model;

import java.math.BigDecimal;

import org.openxava.negocio.model.UnidadMedida;

public interface IItemOriginaOrdenPreparacion {
	
	public BigDecimal getPendientePreparacion();
	
	public void setPendientePreparacion(BigDecimal pendientePreparacion);
	
	public BigDecimal getNoEntregado();
	
	public void setNoEntregado(BigDecimal noEntregado);
	
	public BigDecimal getCantidad();
	
	public UnidadMedida getUnidadMedida();
}
