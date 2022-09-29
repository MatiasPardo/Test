package org.openxava.tesoreria.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import org.openxava.base.model.Transaccion;
import org.openxava.negocio.model.Moneda;

public class ItemTransCambiarValor extends ItemTransMovimientoValores{
	
	@Override
	public BigDecimal importeOriginalValores() {
		if (this.referenciaValor() != null){
			return this.referenciaValor().getImporte();
		}
		else{
			return BigDecimal.ZERO;
		}
	}
	
	@Override
	public BigDecimal importeMonedaTrValores(Transaccion transaccion) {
		BigDecimal importeOriginal = this.importeOriginalValores();		
		if (this.referenciaValor() != null){
			Moneda moneda = this.referenciaValor().getMoneda();
			BigDecimal cotizacion = transaccion.buscarCotizacionTrConRespectoA(moneda);
			return importeOriginal.divide(cotizacion, 2, RoundingMode.HALF_EVEN);
		}	
		else{
			return BigDecimal.ZERO;
		}	
	}
	
	@Override
	public TipoMovimientoValores tipoMovimientoValores(boolean reversion) {
		if (!reversion){
			return new TipoMovCambiarValores();
		}
		else{
			return new TipoMovAnulacionCambiarValores();
		}
	}
	
	@Override
	public void setBanco(Banco banco) {
	}

	@Override
	public void setDetalle(String detalle) {		
	}

	@Override
	public void setFechaEmision(Date fechaEmision) {		
	}

	@Override
	public void setFechaVencimiento(Date fechaVencimiento) {		
	}

	@Override
	public void asignarOperadorComercial(Valor valor, Transaccion transaccion) {	
	}
	
	@Override
	public IChequera chequera(){
		return null;
	}
}
