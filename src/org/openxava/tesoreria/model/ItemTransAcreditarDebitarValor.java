package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import org.openxava.base.model.*;
import org.openxava.negocio.model.*;

public class ItemTransAcreditarDebitarValor extends ItemTransMovimientoValores{

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
			return new TipoMovAcreditarDebitarValores();
		}
		else{
			return new TipoMovAnulacionAcredDebitarValores();
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
