package org.openxava.negocio.model;

import org.openxava.base.model.*;
import org.openxava.ventas.model.*;

public interface IDivisionItemTransaccion {
	
	public ItemTransaccion dividirConNuevoItem();
	
	public Producto getProducto();
	
	public void validacionesPreDividir();


}
