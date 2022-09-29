package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import org.openxava.base.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class TipoMovAcreditarDebitarValores extends TipoMovimientoValores{

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
		if (tipoValor.consolidaAutomaticamente()){
			errores.add(movimientoValores.getTipoValor().toString() + " no esta soportado para la operación de acreditar/debitar valores");
		}		
		else{
			Valor valor = movimientoValores.referenciaValor();
			if (valor != null){
				CuentaBancaria cuenta = (CuentaBancaria)movimientoValores.tesoreriaAfectada();
				if (!cuenta.getEfectivo().getMoneda().equals(valor.getMoneda())){
					throw new ValidationException("No se puede acreditar/debitar dos monedas diferentes");
				}
			}
			else{
				errores.add("No esta asignado el valor para acreditar/debitar");
			}
		}
	}

	@Override
	public BigDecimal coeficiente(IItemMovimientoValores movimientoValores) {
		return movimientoValores.getTipoValor().getComportamiento().coeficienteConsolidacion().negate();
	}
}
