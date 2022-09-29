package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;


public class TipoMovEgresoValores extends TipoMovimientoValores{

	private boolean emitirChequeEnHistorico = false;
	
	public TipoMovEgresoValores(){		
	}
	
	public TipoMovEgresoValores(boolean emisionChequeHistorico){
		this.emitirChequeEnHistorico = emisionChequeHistorico;
	}
	
	@Override
	public Valor actualizarValor(IItemMovimientoValores movimientoValores, Transaccion tr, Map<String, Object> procesados) {
		Valor valor = null;
		if (movimientoValores.getTipoValor().getComportamiento().equals(TipoValor.ChequePropio)){
			// asignar cheque desde la chequera de la cuenta bancaria, y asignar número

			String id = movimientoValores.tesoreriaAfectada().getId();
			IChequera chequera = movimientoValores.chequera();			
			if (chequera != null){
				id = chequera.getId();
			}
			AdministradorChequesPropios adminCheques = null;
			if (!procesados.containsKey(id)){
				if (chequera != null){
					adminCheques = new AdministradorChequesPropios(chequera);
				}
				else{
					// La instancio desde el Manager, porque no puedo hacer el cast directamente
					CuentaBancaria cuenta = (CuentaBancaria)XPersistence.getManager().find(CuentaBancaria.class, movimientoValores.tesoreriaAfectada().getId());
					XPersistence.getManager().refresh(cuenta);
					adminCheques = new AdministradorChequesPropios(cuenta);
				}
				procesados.put(id, adminCheques);
			}
			else{
				//cuenta = (CuentaBancaria)procesados.get(movimientoValores.tesoreriaAfectada().getId());
				adminCheques = (AdministradorChequesPropios) procesados.get(id);
			}			 				
			String numero = movimientoValores.getNumeroValor();
			if (Is.emptyString(numero)){
				numero = adminCheques.numerarCheque(movimientoValores.getTipoValor());
				movimientoValores.setNumeroValor(numero);
			}
			else{
				// el usuario asignó un número.
				// se le notifica a la chequera el último número utilizado
				adminCheques.notificarUsoCheque(numero);
			}
			movimientoValores.setBanco(adminCheques.getChequera().getBanco());
			if (!Valor.existeCheque(movimientoValores.getTipoValor(), numero, adminCheques.getChequera().getBanco(), movimientoValores.getEmpresa())){
				valor = Valor.crearValor(tr, movimientoValores);
				if (this.emitirChequeEnHistorico && valor.getTipoValor().getComportamiento().equals(TipoValor.ChequePropio)){
					valor.setEstado(EstadoValor.Historico);
				}
			}
			else{
				throw new ValidationException("No se puede volver a emitir " + movimientoValores.getTipoValor().toString() + " - " + numero);
			}	
		}
		else if (movimientoValores.getTipoValor().getComportamiento().equals(TipoValor.ChequeTercero)){
			String id = movimientoValores.referenciaValor().getId();
			if (!procesados.containsKey(id)){
				valor = this.buscarValor(procesados, id);
				if (valor.getTesoreria().equals(movimientoValores.tesoreriaAfectada())){
					if (!valor.getAnulado()){
						if (!valor.getHistorico()){
							//valor.setHistorico(Boolean.TRUE);
							if (movimientoValores.transfiere() != null){
								valor.setTesoreria(movimientoValores.transfiere());
								if (movimientoValores.transfiere().esCuentaBancaria()){
									valor.setEstado(EstadoValor.Historico);
								}
							}
							else{
								valor.setEstado(EstadoValor.Historico);
							}							
							// a la salida se tiene que marcar a que proveedor fue
							movimientoValores.asignarOperadorComercial(valor, tr);							
						}
						else{
							throw new ValidationException(valor.toString() + " no esta en cartera");
						}
					}
					else{
						throw new ValidationException(valor.toString() + " esta anulado");
					}
				}
				else{
					throw new ValidationException(valor.toString() + " no se encuentra en " + movimientoValores.tesoreriaAfectada().toString());
				}					
			}
			else{
				throw new ValidationException(movimientoValores.referenciaValor().toString() + " procesado más de una vez");
			}			
		}
		else{
			TipoValorConfiguracion tipoValorConsolidar = this.tipoValorConsolida(movimientoValores.tesoreriaAfectada(), movimientoValores.getTipoValor());			
			if (tipoValorConsolidar != null){
				valor = this.buscarEfectivo(procesados, tipoValorConsolidar, movimientoValores.tesoreriaAfectada(), movimientoValores.getEmpresa());
				BigDecimal importe = movimientoValores.importeOriginalValores().abs();
				valor.setImporte(valor.getImporte().subtract(importe));
				if (tipoValorConsolidar.getComportamiento().equals(TipoValor.TarjetaCreditoCobranza)){
					if (valor.getImporte().compareTo(BigDecimal.ZERO) < 0){
						throw new ValidationException(tipoValorConsolidar.getNombre() + " no puede quedar en negativo: " + UtilERP.convertirString(valor.getImporte()));
					}
				}
			}
			else{
				throw new ValidationException(movimientoValores.getTipoValor().toString() + " no esta soportado para egreso de valores");
			}
		}		
		return valor;
	}

	@Override
	protected void validarAtributos(Messages errores, IItemMovimientoValores movimientoValores) {
		TipoValorConfiguracion tipoValor = movimientoValores.getTipoValor();
						
		
		if (tipoValor.getComportamiento().equals(TipoValor.ChequeTercero)){			
			if (movimientoValores.referenciaValor() == null){
				errores.add("Cheque no asignado");
			}
			else{
				if (movimientoValores.referenciaValor().getImporte().compareTo(movimientoValores.importeOriginalValores()) != 0){
					errores.add("El importe del cheque " + movimientoValores.referenciaValor().toString() + " es " + movimientoValores.referenciaValor().getImporte().toString() + ", pero esta asignado " + movimientoValores.importeOriginalValores());
				}
			}
		}
		else if (tipoValor.getComportamiento().equals(TipoValor.ChequePropio)){
			if (movimientoValores.getFechaEmision() == null){
				errores.add("Fecha de emisión no asignada");
			}
			if (movimientoValores.getFechaVencimiento() == null){
				errores.add("Fecha de vencimiento no asignada");
			}
			if ((movimientoValores.getFechaEmision() != null) && (movimientoValores.getFechaVencimiento() != null) && 
					movimientoValores.getFechaEmision().compareTo(movimientoValores.getFechaVencimiento()) > 0){
				errores.add("Fecha de vencimiento no puede ser menor a la Fecha de emisión");
			}
			
			if (movimientoValores.tesoreriaAfectada().getMultiplesChequeras()){
				if (movimientoValores.chequera() == null){
					errores.add("Falta asignar la chequera");
				}
			}
			else if (movimientoValores.chequera() != null){
				errores.add("Chequera asignada incorrectamente: la cuenta bancaria no tiene habilitadas múltiples chequeras");	
			}
		}
	}
		
	@Override
	public BigDecimal coeficiente(IItemMovimientoValores movimientoValores) {
		return new BigDecimal(-1);
	}
		
	@Override
	public void bloquearValorParaActualizar(IItemMovimientoValores item, Map<String, Object> bloqueos) {
		if (item.getTipoValor().getComportamiento().equals(TipoValor.ChequePropio)){			
			String key = item.tesoreriaAfectada().getId();
			String table = "Tesoreria";
			if (item.chequera() != null){
				key = item.chequera().getId();
				table = "Chequera";
			}
			if (!bloqueos.containsKey(key)){
				String sql = "select t.id from " +  Esquema.concatenarEsquema(table) +" t where t.id = :id for update";
				Query query = XPersistence.getManager().createNativeQuery(sql);
				query.setParameter("id", key);
				query.setMaxResults(1);
				List<?> result = query.getResultList();
				if (result.isEmpty()){
					throw new ValidationException("No se pudo bloquear la tesorería/chequera " + item.tesoreriaAfectada().toString());
				}
				bloqueos.put(key, null);
			}			
		}
		else{
			super.bloquearValorParaActualizar(item, bloqueos);
		}
	}
}
