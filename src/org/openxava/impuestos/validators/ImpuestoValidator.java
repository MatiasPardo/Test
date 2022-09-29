package org.openxava.impuestos.validators;

import java.math.BigDecimal;

import org.openxava.base.validators.*;
import org.openxava.impuestos.model.*;
import org.openxava.util.*;

@SuppressWarnings("serial")
public class ImpuestoValidator extends UnicidadValidator{
	
	private Boolean pagos;
	
	public Boolean getPagos() {
		return pagos == null ? Boolean.FALSE : this.pagos;
	}

	public void setPagos(Boolean pagos) {
		this.pagos = pagos;
	}
	
	private BigDecimal minimoImponible;

	public BigDecimal getMinimoImponible() {
		return minimoImponible;
	}

	public void setMinimoImponible(BigDecimal minimoImponible) {
		this.minimoImponible = minimoImponible;
	}

	private BigDecimal alicuotaGeneral = BigDecimal.ZERO;
	
	public BigDecimal getAlicuotaGeneral() {
		return alicuotaGeneral;
	}

	public void setAlicuotaGeneral(BigDecimal alicuotaGeneral) {
		if (alicuotaGeneral != null){
			this.alicuotaGeneral = alicuotaGeneral;
		}
	}

	@Override
	public void validate(Messages errors) throws Exception {
		DefinicionImpuesto tipo = (DefinicionImpuesto)this.getValor();
		if (tipo != null){
			if (!tipo.isMultiplesRegimenes()){
				super.validate(errors);
			}
			
			if ((!tipo.isPagos()) && (this.getPagos())){
				errors.add("El tipo de impuesto " + tipo.name() + " no puede estar habilitado para los pagos");
			}
			
			if (tipo.equals(DefinicionImpuesto.RetencionMonotributo)){
				if (this.getMinimoImponible() == null){
					errors.add("Falta asignar el mínimo imponible");					
				}
				else if (this.getMinimoImponible().compareTo(BigDecimal.ZERO) < 0){
					errors.add("Mínimo imponible debe ser mayor a cero");
				}
				
				if (this.getAlicuotaGeneral().compareTo(BigDecimal.ZERO) <= 0){
					errors.add("Alicuota general debe ser mayor a cero");
				}
			}
		}
	}
}
