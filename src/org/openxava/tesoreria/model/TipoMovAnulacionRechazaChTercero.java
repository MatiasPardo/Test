package org.openxava.tesoreria.model;

import java.util.*;

import org.openxava.base.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class TipoMovAnulacionRechazaChTercero extends TipoMovimientoValores{
	@Override
	public Valor actualizarValor(IItemMovimientoValores movimientoValores, Transaccion tr, Map<String, Object> procesados) {
		
		Valor cheque = this.buscarValor(procesados, movimientoValores.referenciaValor().getId());
		if (!cheque.getEstado().equals(EstadoValor.Rechazado)){					
			throw new ValidationException(cheque.toString() + " en estado " + cheque.getEstado().toString());
		}
		cheque.setEstado(EstadoValor.Historico);
		
		return cheque;
	}

	@Override
	protected void validarAtributos(Messages errores, IItemMovimientoValores movimientoValores) {
		Valor cheque = movimientoValores.referenciaValor();
		if (cheque != null){
			if (!cheque.getTipoValor().getComportamiento().equals(TipoValor.ChequeTercero)){
				errores.add("Tipo de valor no se puede rechazar");
			}
		}
		else{
			errores.add("No esta asignado el cheque");
		}
	}
}
