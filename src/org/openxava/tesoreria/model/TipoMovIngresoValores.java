package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public class TipoMovIngresoValores extends TipoMovimientoValores{
	
	@Override
	public void validarAtributos(Messages errores, IItemMovimientoValores movimientoValores){
		TipoValor tipoValor = movimientoValores.getTipoValor().getComportamiento();
		
		if (tipoValor.equals(TipoValor.ChequeTercero)){
			if (movimientoValores.getFechaEmision() == null){
				errores.add("Fecha de emisión no asignada");
			}
			if (movimientoValores.getFechaVencimiento() == null){
				errores.add("Fecha de vencimiento no asignada");
			}
			if ((movimientoValores.getFechaEmision() != null) && (movimientoValores.getFechaVencimiento() != null)){
				if (movimientoValores.getFechaEmision().compareTo(movimientoValores.getFechaVencimiento()) > 0){
					errores.add("Fecha de vencimiento no puede ser menor a la Fecha de emisión");
				}
			}
			if (Is.emptyString(movimientoValores.getNumeroValor())){
				errores.add("Número no asignado en " + movimientoValores.getTipoValor().getNombre());
			}
			if (movimientoValores.importeOriginalValores().compareTo(BigDecimal.ZERO) <= 0){
				errores.add("Importe debe ser mayor a 0");
			}
			if (Is.emptyString(movimientoValores.getFirmante())){
				errores.add("Firmante no asignado");
			}
			if (Is.emptyString(movimientoValores.getNroCuentaFirmante())){
				errores.add("Firmante: Número de cuenta no asignado");
			}
			if (Is.emptyString(movimientoValores.getCuitFirmante())){
				errores.add("Firmante: Número de cuit no asignado");
			}
		}
		else if (tipoValor.equals(TipoValor.TarjetaCreditoCobranza)){
			ObjetoNegocio item = movimientoValores.itemTrValores();
			if (item instanceof ItemReciboCobranza){
				ItemReciboCobranza itemCobranza = (ItemReciboCobranza)item;				
				if (itemCobranza.getLote() == null){
					errores.add("Número de Lote no asignado");
				}
				if (itemCobranza.getCupon() == null){
					errores.add("Número de Cupón no asignado");
				}
			}			
		}
	}
	
	@Override
	public void bloquearValorParaActualizar(IItemMovimientoValores item, Map<String, Object> bloqueos) {
		TipoValor tipoValor = item.getTipoValor().getComportamiento();
		if (tipoValor.equals(TipoValor.ChequeTercero)){
			String key = item.getTipoValor().getId();
			if (!bloqueos.containsKey(key)){
				String sql = "select t.id from " + Esquema.concatenarEsquema("TipoValorConfiguracion") + " t where t.id = :id for update";
				Query query = XPersistence.getManager().createNativeQuery(sql);
				query.setParameter("id", key);
				query.setMaxResults(1);
				List<?> result = query.getResultList();
				if (result.isEmpty()){
					throw new ValidationException("No se pudo bloquear el tipo de valor " + item.getTipoValor().toString());
				}
				bloqueos.put(key, null);
			}
		}		
		else{
			super.bloquearValorParaActualizar(item, bloqueos);
		}
	}

	@Override
	public Valor actualizarValor(IItemMovimientoValores movimientoValores, Transaccion tr, Map<String, Object> procesados) {
		Valor valorActualizado = null;
		TipoValorConfiguracion tipoValorConsolidar = this.tipoValorConsolida(movimientoValores.tesoreriaAfectada(), movimientoValores.getTipoValor());

		if (movimientoValores.getTipoValor().getComportamiento().equals(TipoValor.ChequeTercero)){
			String key = movimientoValores.getTipoValor().getId().concat(movimientoValores.tesoreriaAfectada().getId()).concat(movimientoValores.getNumeroValor());
			if (movimientoValores.getBanco() != null){
				key += movimientoValores.getBanco().getId();
			}
			if (!procesados.containsKey(key)){
				if (!Valor.existeCheque(movimientoValores.getTipoValor(), movimientoValores.getNumeroValor(), movimientoValores.getBanco(), movimientoValores.getEmpresa())){
					valorActualizado = Valor.crearValor(tr, movimientoValores);
				}
				else{
					throw new ValidationException(movimientoValores.getTipoValor().getNombre() + " " + movimientoValores.getNumeroValor().toString() + " ya fue ingresado");
				}
				procesados.put(key, valorActualizado);
			}
			else{
				throw new ValidationException(movimientoValores.getTipoValor().toString() + " " + movimientoValores.getNumeroValor().toString() + " procesado más de una vez");
			}
		}		
		else if (tipoValorConsolidar != null){
			valorActualizado = this.buscarEfectivo(procesados, tipoValorConsolidar, movimientoValores.tesoreriaAfectada(), movimientoValores.getEmpresa());
			BigDecimal importe = movimientoValores.importeOriginalValores();
			valorActualizado.setImporte(valorActualizado.getImporte().add(importe));	
		}
		else{
			throw new ValidationException(movimientoValores.getTipoValor().toString() + " no esta soportado para ingreso de valores");
		}	
		
		return valorActualizado;
	}
	

}
