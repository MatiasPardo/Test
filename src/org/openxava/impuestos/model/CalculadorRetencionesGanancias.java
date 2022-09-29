package org.openxava.impuestos.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.base.calculators.*;
import org.openxava.base.model.*;
import org.openxava.compras.model.*;
import org.openxava.cuentacorriente.model.*;
import org.openxava.jpa.*;
import org.openxava.tesoreria.model.*;
import org.openxava.validators.*;

public class CalculadorRetencionesGanancias implements ICalculadorImpuesto{

	@Override
	public void calcular(Transaccion transaccion, ItemTransaccion item, int nroImpuesto) {
		PagoProveedores pago = (PagoProveedores) transaccion;
		ItemPagoRetencion itemRetencion = (ItemPagoRetencion) item;
		if (pago != null){
			Proveedor proveedor = pago.getProveedor();
			Empresa empresa = transaccion.getEmpresa();
			BigDecimal saldo = pago.getSaldoComprobantes();
			if ((empresa != null) && (proveedor != null) && (saldo.compareTo(BigDecimal.ZERO) > 0) && (empresa.getAgenteRecaudacion())){
				EntidadRetencionProveedor entidadImpuesto = proveedor.configuracionImpuesto(itemRetencion.getImpuesto());
				boolean ponerEnCero = true;
				if (entidadImpuesto != null){
					if (entidadImpuesto.getCalcula()){
						ponerEnCero = false;
						// se busca el neto acumulado y las retenciones anteriores
						calcularNetoAcumulado(pago, itemRetencion);
						AlicuotaImpuesto alicuota = entidadImpuesto.getAlicuota();
						itemRetencion.setMontoNoSujetoRetencion(BigDecimal.ZERO);
						if (alicuota.getPosicion().equals(PosicionAnteRetencion.Inscripto)){
							itemRetencion.setMontoNoSujetoRetencion(itemRetencion.getImpuesto().getMinimoImponible());
						}
						
						BigDecimal importeRetencion = BigDecimal.ZERO;						
						if (alicuota.getEscalas() != null){
							EscalaImpuesto escala = alicuota.getEscalas().buscarEscala(itemRetencion.getNetoGrabado());
							itemRetencion.setAlicuota(escala.getMasPorcentaje());
							importeRetencion = itemRetencion.getNetoGrabado().subtract(escala.getMasDe()).multiply(escala.getMasPorcentaje().divide(new BigDecimal(100), 2, RoundingMode.HALF_EVEN));
							importeRetencion = importeRetencion.add(escala.getImporteFijo());
						}
						else{
							itemRetencion.setAlicuota(alicuota.getPorcentaje());
							importeRetencion = itemRetencion.getNetoGrabado().multiply(alicuota.getPorcentaje().divide(new BigDecimal(100), 2, RoundingMode.HALF_EVEN));
						}
						
						if (importeRetencion.compareTo(alicuota.getMinimo()) > 0){ 
							itemRetencion.setRetencionTotal(importeRetencion);
						}
						else{
							ponerEnCero = true;
						}							
					}
				}
				
				if (ponerEnCero){					
					itemRetencion.setRetencionTotal(BigDecimal.ZERO);
				}
				itemRetencion.calcularRetencionActual();
			}
		}		
	}
	
	private void calcularNetoAcumulado(PagoProveedores pago, ItemPagoRetencion retencion){
		if (pago.getComprobantesPorPagar().isEmpty()){
			throw new ValidationException("No hay comprobantes para pagar: no se puede calcular la retención");
		}
		ItemPagoRetencion retencionAnterior = buscarRetencionesAnterioresDelMes(pago, retencion.getImpuesto());		
		BigDecimal netoAcumulado = BigDecimal.ZERO;
		BigDecimal importeRetencionesAnteriores = BigDecimal.ZERO;
		if (retencionAnterior != null){
			netoAcumulado = retencionAnterior.getNetoAcumulado();
			importeRetencionesAnteriores = retencionAnterior.getRetencionesAnteriores().add(retencionAnterior.getRetencionActual());
		}	
		
		// se acumula sobre el neto las facturas que se están pagando
		for(CuentaCorrienteCompra ctacte: pago.getComprobantesPorPagar()){
			if (! pago.tieneOtroPagoAsociado(ctacte)){
				if ((ctacte.getImputaciones() != null) && (!ctacte.getImputaciones().isEmpty())){
					for(ImputacionCompra imputacion: ctacte.getImputaciones()){
						if (!imputacion.getEstado().equals(Estado.Anulada)){
							throw new ValidationException("El comprobante " + ctacte.toString() + " tiene imputaciones: Para el cálculo de retenciones debe anular la imputación y agregar las notas de crédito a los comprobantes por pagar");
						}
					}
				}
				// si tiene un pago asociado, ya se cobró la retención para dicha factura				
				CompraElectronica compra = (CompraElectronica)ctacte.buscarTransaccion();
				BigDecimal coeficiente = new BigDecimal(1);
				if (((ITransaccionCtaCte)compra).CtaCteImporte().compareTo(BigDecimal.ZERO) < 0){
					coeficiente = coeficiente.negate();
				}
				for(ItemCompraElectronica itemCompra: compra.getItems()){
					if (itemCompra.getProducto().getRegimenRetencionGanancias().equals(retencion.getImpuesto())){						
						netoAcumulado = netoAcumulado.add(itemCompra.getSuma1().multiply(coeficiente));
					}
				}				
			}
		}
		
		retencion.setNetoAcumulado(netoAcumulado);
		retencion.setRetencionesAnteriores(importeRetencionesAnteriores);		
	}
	
	private ItemPagoRetencion buscarRetencionesAnterioresDelMes(PagoProveedores pago, Impuesto impuesto){
		Query query = XPersistence.getManager().createQuery("from ItemPagoRetencion i where i.impuesto.id = :impuesto and i.pago.proveedor.id = :proveedor and " + 
													"i.pago.fecha >= :desde and i.pago.fecha < :hasta and i.pago.estado = :estado and i.pago.id != :pago " + 
													"order by i.pago.fecha desc");
		FechaInicioMesCalculator inicioMes = new FechaInicioMesCalculator();
		inicioMes.setFecha(pago.getFecha());
		Date desde = null;
		try {
			desde = (Date)inicioMes.calculate();
		} catch (Exception e) {
		}
		FechaFinMesCalculator finMes = new FechaFinMesCalculator();
		finMes.setFecha(pago.getFecha());
		Date hasta = null;
		try {
			hasta = (Date)finMes.calculate();
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(hasta);
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			hasta = calendar.getTime();
		} catch (Exception e) {
		}
		
		query.setParameter("impuesto", impuesto.getId());
		query.setParameter("proveedor", pago.getProveedor().getId());
		query.setParameter("estado", Estado.Confirmada);
		query.setParameter("desde", desde);
		query.setParameter("hasta", hasta);
		query.setParameter("pago", pago.getId());
		query.setMaxResults(1);
		
		List<?> list = query.getResultList();		
		for(Object object: list){
			return (ItemPagoRetencion)object;
		}
		return null;
	}
}
