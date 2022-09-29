package org.openxava.impuestos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.openxava.base.calculators.FechaInicioMesCalculator;
import org.openxava.base.model.Empresa;
import org.openxava.base.model.Esquema;
import org.openxava.base.model.ItemTransaccion;
import org.openxava.base.model.Transaccion;
import org.openxava.base.model.UtilERP;
import org.openxava.compras.model.EntidadRetencionProveedor;
import org.openxava.compras.model.Proveedor;
import org.openxava.cuentacorriente.model.CuentaCorrienteCompra;
import org.openxava.jpa.XPersistence;
import org.openxava.tesoreria.model.ItemPagoRetencion;
import org.openxava.tesoreria.model.PagoProveedores;
import org.openxava.validators.ValidationException;

public class CalculadorRetencionesMonotributo implements ICalculadorImpuesto{

	@Override
	public void calcular(Transaccion transaccion, ItemTransaccion item, int nroImpuesto) {
		PagoProveedores pago = (PagoProveedores) transaccion;
		ItemPagoRetencion itemRetencion = (ItemPagoRetencion) item;
		if (pago != null){
			Proveedor proveedor = pago.getProveedor();
			Empresa empresa = transaccion.getEmpresa();
			if (empresa.getAgenteRecaudacion() && pago.getSaldoComprobantes().compareTo(BigDecimal.ZERO) > 0){
				EntidadRetencionProveedor entidadImpuesto = proveedor.configuracionImpuesto(itemRetencion.getImpuesto());
				boolean ponerEnCero = true;
				if (entidadImpuesto != null){
					if (entidadImpuesto.getCalcula()){
						ponerEnCero = false;
						// se busca el neto acumulado y las retenciones anteriores
						calcularNetoCalculaRetencion(pago, itemRetencion);
						BigDecimal alicuota = itemRetencion.getImpuesto().getAlicuotaGeneral();
						
						itemRetencion.setAlicuota(alicuota);
						BigDecimal importeRetencion = itemRetencion.getNetoGrabado().multiply(alicuota.divide(new BigDecimal(100), 2, RoundingMode.HALF_EVEN));
						itemRetencion.setRetencionTotal(importeRetencion);
					}
				}
				
				if (ponerEnCero){					
					itemRetencion.setRetencionTotal(BigDecimal.ZERO);
				}
				itemRetencion.calcularRetencionActual();
			}
		}		
	}

	private void calcularNetoCalculaRetencion(PagoProveedores pago, ItemPagoRetencion retencion){
		if (pago.getComprobantesPorPagar().isEmpty()){
			throw new ValidationException("No hay comprobantes para pagar: no se puede calcular la retención " + retencion.getImpuesto().getNombre());
		}
		
		Date hasta = UtilERP.trucarDateTime(pago.getFecha());
		FechaInicioMesCalculator inicioMes = new FechaInicioMesCalculator();
		Date desde = hasta;
		try{
			desde = (Date)inicioMes.calculate();
			Calendar cal = Calendar.getInstance();
			cal.setTime(desde);
			cal.add(Calendar.MONTH, -11);
			desde = cal.getTime();
		}
		catch(Exception e){			
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("select cc.id, cc.neto1 from ");
		sql.append(Esquema.concatenarEsquema("CuentaCorriente cc "));
		sql.append("where cc.anulado = :anulado and cc.proveedor_id = :proveedor and tipo != :pago ");
		sql.append("and cc.fecha <= :hasta and cc.fecha >= :desde ");
		sql.append("order by cc.fecha asc, cc.neto1 asc ");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("anulado", false);
		query.setParameter("proveedor", pago.getProveedor().getId());
		query.setParameter("pago", "PAGO");
		query.setParameter("desde", desde);
		query.setParameter("hasta", hasta);
		query.setFlushMode(FlushModeType.COMMIT);
				
		BigDecimal netoUltimoAnio = BigDecimal.ZERO;
		BigDecimal minimoImponible = retencion.getImpuesto().getMinimoImponible();
		List<?> results = query.getResultList();
		Map<String, Object> idsCtaCte = new HashMap<String, Object>();
		for(Object res: results){
			BigDecimal neto = (BigDecimal)((Object[])res)[1];
			netoUltimoAnio = netoUltimoAnio.add(neto);
			if (netoUltimoAnio.compareTo(minimoImponible) > 0 && neto.compareTo(BigDecimal.ZERO) > 0){
				idsCtaCte.put((String)((Object[])res)[0], null);
			}
		}
		
		BigDecimal netoGrabado = BigDecimal.ZERO;
		if (!idsCtaCte.isEmpty()){
			// en los comprobantes por pagar, se revisa que facturas exceden el mínimo imponible acumulado en 12 meses, para calcular la retencion
			for(CuentaCorrienteCompra ctacte: pago.getComprobantesPorPagar()){
				if (!pago.tieneOtroPagoAsociado(ctacte) && idsCtaCte.containsKey(ctacte.getId())){
					netoGrabado = netoGrabado.add(ctacte.getNeto1());	
				}
			}
		}
		
		retencion.setMontoNoSujetoRetencion(retencion.getImpuesto().getMinimoImponible());
		retencion.setNetoAcumulado(netoUltimoAnio);
		retencion.setNetoGrabado(netoGrabado);
	}	
}
