package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import org.openxava.base.model.*;
import org.openxava.validators.*;

public class ItemTransMovValoresCustom extends ItemTransMovimientoValores{
	
	private BigDecimal importeOriginal;
		
	private TipoMovimientoValores tipo;
	
	private TipoMovimientoValores tipoReversion;
	
	private TipoValorConfiguracion tipoValorCustom;
	
	public ItemTransMovValoresCustom(IItemMovimientoValores generadaPor){
		this.setGeneradoPor(generadaPor);
				
		if (generadaPor != null){
			this.setImporteOriginal(generadaPor.importeOriginalValores());
			this.asignarReferenciaValor(generadaPor.referenciaValor());
		}
	}
	
	public ItemTransMovValoresCustom(){
		
	}
	
	public BigDecimal getImporteOriginal() {
		return importeOriginal == null ? BigDecimal.ZERO : importeOriginal;
	}

	public void setImporteOriginal(BigDecimal importeOriginal) {
		this.importeOriginal = importeOriginal;
	}
	
	public TipoMovimientoValores getTipo() {
		return tipo;
	}

	public void setTipo(TipoMovimientoValores tipo) {
		this.tipo = tipo;
	}

	public TipoMovimientoValores getTipoReversion() {
		return tipoReversion;
	}

	public void setTipoReversion(TipoMovimientoValores tipoReversion) {
		this.tipoReversion = tipoReversion;
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
	public BigDecimal importeOriginalValores() {		
		return this.getImporteOriginal();
	}

	@Override
	public BigDecimal importeMonedaTrValores(Transaccion transaccion) {
		if (this.getGeneradoPor() != null){
			return this.getGeneradoPor().importeMonedaTrValores(transaccion);
		}
		else{
			throw new ValidationException("Falta implementar método Importe en Moneda Operacion Valores");
		}
	}

	@Override
	public TipoMovimientoValores tipoMovimientoValores(boolean reversion) {
		if (!reversion){
			return this.getTipo();
		}
		else{
			return this.getTipoReversion();
		}		
	}
	
	@Override
	public TipoValorConfiguracion getTipoValor() {
		if (this.tipoValorCustom != null){
			return this.tipoValorCustom;
		}
		else if (this.getGeneradoPor() != null){
			return this.getGeneradoPor().getTipoValor();
		}
		else{
			return super.getTipoValor();	
		}
	}
	
	@Override
	public Empresa getEmpresa() {
		if (this.getGeneradoPor() != null){
			return this.getGeneradoPor().getEmpresa();
		}
		else{
			return super.getEmpresa();
		}
	}
	
	public void setTipoValorCustom(TipoValorConfiguracion tipoValor){
		this.tipoValorCustom = tipoValor;
	}
}
