package org.openxava.negocio.model;

import java.math.*;

public class Cantidad {
	
	private BigDecimal cantidad;
	
	private UnidadMedida unidadMedida;

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public void setCantidad(BigDecimal cantidad) {
		this.cantidad = cantidad;
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public void setUnidadMedida(UnidadMedida unidadMedida) {
		this.unidadMedida = unidadMedida;
	}

	public BigDecimal convertir(UnidadMedida um) {
		if (this.getUnidadMedida() == null){
			return BigDecimal.ZERO;
		}
		else if (this.getUnidadMedida().equals(um)){
			return this.getCantidad();
		}
		else{
			return this.getUnidadMedida().convertir(this.getCantidad(), um);
		}
	}
	
	@Override
	public String toString(){
		String str = this.getCantidad().toString();
		if (this.getUnidadMedida() != null){
			str += " " + this.getUnidadMedida().toString();
		}
		return str;
	}
}
