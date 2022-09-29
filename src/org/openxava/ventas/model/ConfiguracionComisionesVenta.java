package org.openxava.ventas.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;

@Entity

public class ConfiguracionComisionesVenta extends ObjetoNegocio{
	
	private Boolean activo = Boolean.FALSE;
	
	@Required
	private TipoPorcentajeComisionVenta tipoPorcentaje;

	@Required
	private TipoLiquidacionComisionVenta tipoCalculo;
	
	public TipoPorcentajeComisionVenta getTipoPorcentaje() {
		return tipoPorcentaje;
	}

	public void setTipoPorcentaje(TipoPorcentajeComisionVenta tipoPorcentaje) {
		this.tipoPorcentaje = tipoPorcentaje;
	}

	public Boolean getActivo() {
		return activo == null ? Boolean.FALSE : activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public TipoLiquidacionComisionVenta getTipoCalculo() {
		return tipoCalculo;
	}

	public void setTipoCalculo(TipoLiquidacionComisionVenta tipoCalculo) {
		this.tipoCalculo = tipoCalculo;
	}
}
