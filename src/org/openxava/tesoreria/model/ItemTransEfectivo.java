package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import org.openxava.base.model.*;
import org.openxava.validators.*;

public class ItemTransEfectivo extends ItemTransMovimientoValores{

	private TipoValorConfiguracion tipoValorEfectivo;

	public TipoValorConfiguracion getTipoValorEfectivo() {
		return tipoValorEfectivo;
	}

	public void setTipoValorEfectivo(TipoValorConfiguracion tipoValorEfectivo) {
		this.tipoValorEfectivo = tipoValorEfectivo;
	}

	private BigDecimal importeEfectivo = BigDecimal.ZERO;
	
	public BigDecimal getImporteEfectivo() {
		return importeEfectivo == null ? BigDecimal.ZERO : this.importeEfectivo;
	}

	public void setImporteEfectivo(BigDecimal importeEfectivo) {
		this.importeEfectivo = importeEfectivo;
	}
	
	@Override
	public TipoValorConfiguracion getTipoValor() {
		if (this.getTipoValorEfectivo() != null){
			return this.getTipoValorEfectivo();
		}
		else{
			throw new ValidationException("No se asignó el tipo de valor efectivo");
		}
	}
	
	@Override
	public BigDecimal importeOriginalValores() {
		return this.getImporteEfectivo().abs();		
	}

	@Override
	public BigDecimal importeMonedaTrValores(Transaccion transaccion) {
		BigDecimal cotizacion = transaccion.buscarCotizacionTrConRespectoA(this.getTipoValorEfectivo().getMoneda());		
		return this.importeOriginalValores().divide(cotizacion, 2, RoundingMode.HALF_EVEN);
	}

	@Override
	public TipoMovimientoValores tipoMovimientoValores(boolean reversion) {
		int comparacion = this.getImporteEfectivo().compareTo(BigDecimal.ZERO); 
		if (comparacion > 0){
			if (!reversion){
				return new TipoMovIngresoValores();
			}
			else{
				return new TipoMovEgresoValores();
			}	
		}
		else if (comparacion < 0){
			if (!reversion){
				return new TipoMovEgresoValores();
			}
			else{
				return new TipoMovIngresoValores();
			}
		}
		else{
			throw new ValidationException("Importe de efectivo no puede ser 0");
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
	public void setNumeroValor(String numeroValor) {		
	}
	
	@Override
	public void asignarOperadorComercial(Valor valor, Transaccion transaccion) {
	}
}
