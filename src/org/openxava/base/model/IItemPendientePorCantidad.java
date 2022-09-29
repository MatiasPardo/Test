package org.openxava.base.model;

import java.math.*;

import org.openxava.negocio.model.*;

public interface IItemPendientePorCantidad extends IItemPendiente{

	public UnidadMedida getUnidadMedida();
	
	public BigDecimal getCantidadPendiente();
	
	public BigDecimal getCantidadOriginal();
	
	public Cantidad getCantidadACancelar();
}
