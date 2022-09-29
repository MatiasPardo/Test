package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import org.openxava.base.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class TipoMovAnulacionIngPorTransfValores extends TipoMovimientoValores{

	@Override
	protected void validarAtributos(Messages errores, IItemMovimientoValores movimientoValores) {
		if (movimientoValores.getTipoValor().getComportamiento().equals(TipoValor.ChequeTercero)){
			Valor valor = movimientoValores.referenciaValor();
			if (valor == null){
				throw new ValidationException("Anulación de ingreso por transferencia de valores: Valor no asignado");
			}
		}
	}
	
	@Override
	public Valor actualizarValor(IItemMovimientoValores movimientoValores, Transaccion tr, Map<String, Object> procesados) {		
		if (movimientoValores.getTipoValor().getComportamiento().equals(TipoValor.ChequeTercero)){
			// no hace nada, solo se define para que genera el detalle financiero
			return movimientoValores.referenciaValor();
		}
		else{
			throw new ValidationException("No esta definida la anulación por ingreso por transferencia de valores");
		}			
	}
	
	@Override
	public BigDecimal coeficiente(IItemMovimientoValores movimientoValores) {
		return new BigDecimal(-1);
	}
}
