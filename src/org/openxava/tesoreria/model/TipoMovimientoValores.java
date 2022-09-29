package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

public abstract class TipoMovimientoValores {
	
	public abstract Valor actualizarValor(IItemMovimientoValores movimientoValores, Transaccion tr, Map<String, Object> procesados);
	
	public void validarAtributosValores(IItemMovimientoValores movimientoValores){
		Messages errores = new Messages();
		this.validarAtributos(errores, movimientoValores);
		if (!errores.isEmpty()){
			throw new ValidationException(errores);
		}
	}
	
	protected abstract void validarAtributos(Messages errores, IItemMovimientoValores movimientoValores);
	
	public boolean debeConsolidar(Tesoreria tesoreria, TipoValorConfiguracion tipoValor){
		return tipoValor.getComportamiento().consolidaAutomaticamente();
	}
		
	public TipoValorConfiguracion tipoValorConsolida(Tesoreria tesoreria, TipoValorConfiguracion tipoValor){
		if (this.debeConsolidar(tesoreria, tipoValor)){
			return tipoValor.consolidaCon(tesoreria);
		}
		else{
			return null;
		}
	}
		
	public BigDecimal coeficiente(IItemMovimientoValores movimientoValores) {
		return new BigDecimal(1);
	}
		
	public void bloquearValorParaActualizar(IItemMovimientoValores item, Map<String, Object> bloqueos) {
		TipoValor comportamiento = item.getTipoValor().getComportamiento();
		if ((comportamiento.equals(TipoValor.Efectivo)) || (comportamiento.equals(TipoValor.TransferenciaBancaria) ||
				comportamiento.equals(TipoValor.TarjetaCreditoCobranza) )){			
			TipoValorConfiguracion tipoValorConsolida = this.tipoValorConsolida(item.tesoreriaAfectada(), item.getTipoValor());
			if (tipoValorConsolida != null){
				// Se bloque la relación Tesorería/Efectivo
				String key = item.tesoreriaAfectada().getId().concat(tipoValorConsolida.getId());
				if (!bloqueos.containsKey(key)){
					String sql = "select t.tesoreria_id from " +  Esquema.concatenarEsquema("Tesoreria_TipoValorConfiguracion") + " t where t.tesoreria_id = :tesoreria and valoresposibles_id = :tipoValor for update";
					Query query = XPersistence.getManager().createNativeQuery(sql);
					query.setParameter("tesoreria", item.tesoreriaAfectada().getId());
					query.setParameter("tipoValor", tipoValorConsolida.getId());
									
					List<?> result = query.getResultList();
					if (result.isEmpty()){
						throw new ValidationException("No se encontró configurado el tipo de valor " + tipoValorConsolida.toString() + " en " + item.tesoreriaAfectada().toString());
					}
					// se registra el bloqueo para no volver a bloquear
					bloqueos.put(key, null);
				}
			}
			else{
				throw new ValidationException("No se pudo bloquear " + item.getTipoValor().toString());
			}
		}
		else if (item.referenciaValor() != null){
			String key = item.referenciaValor().getId();
			if(!bloqueos.containsKey(key)){
				String sql = "select v.id from " +  Esquema.concatenarEsquema("valor") + " v where v.id = :valor for update";
				Query query = XPersistence.getManager().createNativeQuery(sql);
				query.setParameter("valor", key);
				query.setMaxResults(1);
				List<?> result = query.getResultList();
				if (result.isEmpty()){
					throw new ValidationException("No se encontró el id del valor a ser bloqueado");
				}
				bloqueos.put(key,  null);
			}
		}
		else{
			throw new ValidationException("No se pudo bloquear: " + item.getTipoValor().toString());
		}
	}
	
	protected Valor buscarEfectivo(Map<String, Object> valoresProcesados, TipoValorConfiguracion tipoValor, Tesoreria tesoreria, Empresa empresa){
		String key = tipoValor.getId().concat(tesoreria.getId()).concat(empresa.getId());
		Valor efectivo = null;
		if (valoresProcesados.containsKey(key)){
			efectivo = (Valor)valoresProcesados.get(key);
		}
		else{
			// se busca y se bloquea
			String sql = "from Valor where tesoreria_id = :tesoreria and tipovalor_id = :tipoValor and empresa_id = :empresa";
			Query query = XPersistence.getManager().createQuery(sql);
			query.setParameter("tesoreria", tesoreria.getId());
			query.setParameter("tipoValor", tipoValor.getId());
			query.setParameter("empresa", empresa.getId());
			query.setMaxResults(1);
			
			List<?> list = query.getResultList();
			if (list.isEmpty()){
				efectivo = Valor.crearValorConsolidacion(tipoValor, tesoreria, empresa);
			}
			else{
				efectivo = (Valor)list.get(0);
				XPersistence.getManager().refresh(efectivo);
			}
			valoresProcesados.put(key, efectivo);	
		}
		return efectivo;
	}
	
	protected Valor buscarValor(Map<String, Object> valoresProcesados, String id){
		Valor valor = null;
		if (valoresProcesados.containsKey(id)){
			valor = (Valor)valoresProcesados.get(id);
		}
		else{
			valor = (Valor)XPersistence.getManager().find(Valor.class, id);
			XPersistence.getManager().refresh(valor);
			valoresProcesados.put(id, valor);
		}
		return valor;
	}
}
