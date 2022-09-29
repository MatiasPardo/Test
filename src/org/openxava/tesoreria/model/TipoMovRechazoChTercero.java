package org.openxava.tesoreria.model;

import java.util.*;

import org.openxava.base.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class TipoMovRechazoChTercero  extends TipoMovimientoValores{

	@Override
	public Valor actualizarValor(IItemMovimientoValores movimientoValores, Transaccion tr, Map<String, Object> procesados) {
		String key = movimientoValores.referenciaValor().getId();
		if (!procesados.containsKey(key)){
			Valor cheque = this.buscarValor(procesados, key);
			if (cheque.getEstado().equals(EstadoValor.Historico)){	
				cheque.setEstado(EstadoValor.Rechazado);
				return cheque;
			}
			else{
				throw new ValidationException("No se puede rechazar el cheque " + cheque.toString() + " porque su estado es " + cheque.getEstado().toString());
			}
		}
		else{
			throw new ValidationException(movimientoValores.referenciaValor().toString() + " procesado más de una vez");
		}
	}

	@Override
	protected void validarAtributos(Messages errores, IItemMovimientoValores movimientoValores) {
		Valor cheque = movimientoValores.referenciaValor();
		if (cheque != null){
			if (!cheque.getTipoValor().getComportamiento().equals(TipoValor.ChequeTercero)){
				errores.add("El tipo de valor no se puede rechazar");
			}
		}
		else{
			errores.add("No esta asignado el cheque");
		}
	}

}
