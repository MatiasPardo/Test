package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import org.openxava.base.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class TipoMovAnulacionEgrValores extends TipoMovimientoValores{

	private boolean emitirChequeEnHistorico = false;
	
	public TipoMovAnulacionEgrValores(){
		
	}
	
	public TipoMovAnulacionEgrValores(boolean emisionChequeHistorico){
		this.emitirChequeEnHistorico = emisionChequeHistorico;
	}
	
	@Override
	public Valor actualizarValor(IItemMovimientoValores movimientoValores, Transaccion tr, Map<String, Object> procesados) {
		
		Valor valor = null;
		if (movimientoValores.getTipoValor().getComportamiento().equals(TipoValor.ChequePropio)){
			valor = this.buscarValor(procesados, movimientoValores.referenciaValor().getId());
			if (valor.getAnulado()){
				throw new ValidationException(valor.toString() + " esta anulado");
			}
			else if (valor.getEstado().equals(EstadoValor.Rechazado)){
				throw new ValidationException(valor.toString() + " esta rechazado");
			}
			else if (valor.getHistorico()){
				if (!movimientoValores.getTipoValor().getConsolidaAutomaticamente()){
					if (!emitirChequeEnHistorico){
						throw new ValidationException(valor.toString() + " no esta en cartera");
					}
				}
			}
			valor.setEstado(EstadoValor.Anulado);	
		}	
		else if (movimientoValores.getTipoValor().getComportamiento().equals(TipoValor.ChequeTercero)){
			valor = this.buscarValor(procesados, movimientoValores.referenciaValor().getId());
			if (movimientoValores.transfiere() != null){
				if (movimientoValores.transfiere().esCuentaBancaria()){
					if (!valor.getEstado().equals(EstadoValor.Historico)){
						throw new ValidationException(valor.toString() + " esta en estado " + valor.getEstado().toString());
					}
				}
				else{
					if (!valor.getEstado().equals(EstadoValor.EnCartera)){			
						throw new ValidationException(valor.toString() + " esta en estado " + valor.getEstado().toString());
					}
				}
			}
			else if (!valor.getEstado().equals(EstadoValor.Historico)){			
				throw new ValidationException(valor.toString() + " esta en estado " + valor.getEstado().toString());
			}
			
			Tesoreria debeEstarValor = movimientoValores.transfiere();
			if (debeEstarValor == null){
				debeEstarValor = movimientoValores.tesoreriaAfectada();
			}
			if (!valor.getTesoreria().equals(debeEstarValor)){
				throw new ValidationException(valor.toString() + " no se encuentra en " + movimientoValores.tesoreriaAfectada().toString());
			}				
						
			//valor.setHistorico(Boolean.FALSE);
			valor.setEstado(EstadoValor.EnCartera);
			if (movimientoValores.transfiere() != null){
				valor.setTesoreria(movimientoValores.tesoreriaAfectada());
			}
			valor.setProveedor(null);
		}
		else{
			TipoValorConfiguracion tipoValorConsolidar = this.tipoValorConsolida(movimientoValores.tesoreriaAfectada(), movimientoValores.getTipoValor());
			if (tipoValorConsolidar != null){
				valor = this.buscarEfectivo(procesados, tipoValorConsolidar, movimientoValores.tesoreriaAfectada(), movimientoValores.getEmpresa());
				BigDecimal importe = movimientoValores.importeOriginalValores().abs();
				valor.setImporte(valor.getImporte().add(importe));	
			}				
			else{
				throw new ValidationException(movimientoValores.getTipoValor().toString() + " no esta soportado para la anulación de egreso de valores");
			}
		}
		return valor;
	}

	@Override
	protected void validarAtributos(Messages errores, IItemMovimientoValores movimientoValores) {
	}

}
