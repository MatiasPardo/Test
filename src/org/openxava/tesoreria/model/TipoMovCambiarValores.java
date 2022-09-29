package org.openxava.tesoreria.model;

import java.math.BigDecimal;
import java.util.Map;

import org.openxava.base.model.Transaccion;
import org.openxava.util.Messages;
import org.openxava.validators.ValidationException;

public class TipoMovCambiarValores extends TipoMovimientoValores{

	@Override
	public Valor actualizarValor(IItemMovimientoValores movimientoValores, Transaccion tr, Map<String, Object> procesados) {
		Valor valor = movimientoValores.referenciaValor();
		String key = valor.getId();
		if (!procesados.containsKey(key)){
			valor = this.buscarValor(procesados, key);
			if (!valor.getEstado().equals(EstadoValor.EnCartera)){
				throw new ValidationException(valor.toString() + " en estado " + valor.getEstado().toString());
			}
			else if (!valor.getTesoreria().equals(movimientoValores.tesoreriaAfectada())){
				throw new ValidationException(valor.toString() + " no se encuentra en " + movimientoValores.tesoreriaAfectada().toString());
			}	
			valor.setEstado(EstadoValor.Historico);						
			return valor;
		}
		else{
			throw new ValidationException(valor.toString() + " procesado más de una vez");
		}
	}

	@Override
	protected void validarAtributos(Messages errores, IItemMovimientoValores movimientoValores) {
		TipoValor tipoValor = movimientoValores.getTipoValor().getComportamiento();
		if (!tipoValor.equals(TipoValor.ChequePropio)){
			errores.add(movimientoValores.getTipoValor().toString() + " no esta soportado para la operación de cambio de cheques valores");
		}		
		else{
			Valor valor = movimientoValores.referenciaValor();
			if (valor == null){
				errores.add("No esta asignado el cheque que se cambia");
			}
		}
	}

	@Override
	public BigDecimal coeficiente(IItemMovimientoValores movimientoValores) {
		return movimientoValores.getTipoValor().getComportamiento().coeficienteConsolidacion().negate();
	}
	
}
