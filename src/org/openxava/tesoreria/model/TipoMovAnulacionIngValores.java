package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import org.openxava.base.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class TipoMovAnulacionIngValores extends TipoMovimientoValores{

	@Override
	public Valor actualizarValor(IItemMovimientoValores movimientoValores, Transaccion tr, Map<String, Object> procesados) {
		Valor valor = null;
		if (movimientoValores.getTipoValor().getComportamiento().equals(TipoValor.ChequeTercero)){
			valor = movimientoValores.referenciaValor();
			if (valor != null){
				valor = this.buscarValor(procesados, valor.getId());
				if (movimientoValores.tesoreriaAfectada().equals(valor.getTesoreria())){
					if (!valor.getAnulado()){
						if (!valor.getHistorico()){
							if (!valor.getEstado().equals(EstadoValor.Rechazado)){								
								valor.setEstado(EstadoValor.Anulado);
							}
							else{
								throw new ValidationException(valor.toString() + " esta rechazado");
							}
						}
						else{
							throw new ValidationException(valor.toString() + " es histórico");
						}
					}
					else{	
						throw new ValidationException(valor.toString() + " ya esta anulado");
					}
				}
				else{
					throw new ValidationException(valor.toString() + " no se encuentra en " + movimientoValores.tesoreriaAfectada().toString()); 
				}
			}
			else{
				throw new ValidationException("No se encontró " + movimientoValores.getTipoValor().toString() + " - " + movimientoValores.getNumeroValor()); 
			}
		}
		else{
			TipoValorConfiguracion tipoValorConsolidar = this.tipoValorConsolida(movimientoValores.tesoreriaAfectada(), movimientoValores.getTipoValor());
		
			if (tipoValorConsolidar != null){
				valor = movimientoValores.referenciaValor();
				if (valor == null){
					valor = this.buscarEfectivo(procesados, tipoValorConsolidar, movimientoValores.tesoreriaAfectada(), movimientoValores.getEmpresa());
				}
				valor.setImporte(valor.getImporte().subtract(movimientoValores.importeOriginalValores()));	
			}
			else{
				throw new ValidationException(movimientoValores.getTipoValor().toString() + " no esta soportado para ingreso de valores");
			}	
		}
		return valor;
	}

	@Override
	protected void validarAtributos(Messages errores, IItemMovimientoValores movimientoValores) {
	}
	
	@Override
	public BigDecimal coeficiente(IItemMovimientoValores movimientoValores) {
		return new BigDecimal(-1);
	}
	
}
