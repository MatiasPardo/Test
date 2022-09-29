package org.openxava.contabilidad.model;

import org.openxava.validators.*;

public enum TipoCuentaContable {
	Ventas("CuentaContableVentas"), Compras("CuentaContableCompras"), Finanzas("CuentaContableFinanzas"), Impuesto("CuentaContableImpuestos");
	
	private String metodo;
	
	TipoCuentaContable(String metodo){
		this.metodo = metodo;
	}
	
	private String getMetodo(){
		return this.metodo;
	}
		
	public CuentaContable CuentaContablePorTipo(Object objetoContable){
		String nombreMetodo = "get" + this.getMetodo();
		CuentaContable cuenta;
		try {
			cuenta = (CuentaContable)objetoContable.getClass().getMethod(nombreMetodo).invoke(objetoContable);
			if (cuenta == null){
				throw new ValidationException("No esta asignada la cuenta contable " + this.toString() + " en " + objetoContable.toString());
			}
			return cuenta;
		}
		catch (NoSuchMethodException e) {
			throw new ValidationException("No se pudo obtener cuenta contable. Método inexistente: " + e.toString());
		} 
		catch (Exception e) {
			throw new ValidationException(e.toString());
		}
	}
}
