package org.openxava.contabilidad.validators;

import org.openxava.base.validators.ItemTransaccionValidator;
import org.openxava.contabilidad.model.CentroCostos;
import org.openxava.contabilidad.model.CuentaContable;
import org.openxava.util.Messages;

@SuppressWarnings("serial")
public class ItemAsientoValidator  extends ItemTransaccionValidator{

	private CuentaContable cuenta;
	
	public CuentaContable getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaContable cuenta) {
		this.cuenta = cuenta;
	}

	private CentroCostos centroCostos;
	
	public CentroCostos getCentroCostos() {
		return centroCostos;
	}

	public void setCentroCostos(CentroCostos centroCostos) {
		this.centroCostos = centroCostos;
	}
	
	@Override
	protected void validarItemTransaccion(Messages errores) {
		if (this.getCuenta() != null){
			if (this.getCuenta().getCentroCostoObligatorio()){
				if (this.getCentroCostos() == null){
					errores.add("Falta asignar centro de costos " + this.getCuenta().getCodigo() + " - " + this.getCuenta().getNombre());
				}
			}
		}
	}
	
}
