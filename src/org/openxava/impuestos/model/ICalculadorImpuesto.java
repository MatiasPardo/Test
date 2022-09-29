package org.openxava.impuestos.model;

import org.openxava.base.model.*;

public interface ICalculadorImpuesto {
	
	public void calcular(Transaccion transaccion, ItemTransaccion item, int nroImpuesto);
	
}
