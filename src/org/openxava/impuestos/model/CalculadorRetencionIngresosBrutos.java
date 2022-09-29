package org.openxava.impuestos.model;

import java.math.*;
import java.util.*;

import org.openxava.base.model.*;
import org.openxava.compras.model.*;
import org.openxava.cuentacorriente.model.*;
import org.openxava.tesoreria.model.*;
import org.openxava.validators.*;

import com.allin.percepciones.model.*;

public abstract class CalculadorRetencionIngresosBrutos implements ICalculadorImpuesto {

	@Override
	public void calcular(Transaccion transaccion, ItemTransaccion item, int nroImpuesto) {
		PagoProveedores pago = (PagoProveedores)transaccion;
		ItemPagoRetencion itemRetencion = (ItemPagoRetencion) item;
		if ((pago != null) && (pago.getEmpresa() != null) && (pago.getProveedor() != null)){
			Proveedor proveedor = pago.getProveedor();
			Empresa empresa = transaccion.getEmpresa();
			
			if ((empresa.getAgenteRecaudacion()) && (pago.getSaldoComprobantes().compareTo(BigDecimal.ZERO) > 0)){
				EntidadImpuesto entidadImpuesto = determinarEntidadImpuesto(proveedor);
				if (entidadImpuesto != null){
					if (entidadImpuesto.debeCalcularImpuesto(transaccion.getFecha())){
						BigDecimal alicuota = null;
						if (entidadImpuesto.tieneAlicuota(transaccion.getFecha())){
							alicuota = entidadImpuesto.getAlicuotaVigente();
						}
						else{
							AlicuotaPadron alicuotaPadron = buscarAlicuotaPadron(transaccion.getFecha(), proveedor);
							if (alicuotaPadron != null){
								alicuota = alicuotaPadron.getAlicuota();
								entidadImpuesto.asignarAlicuota(alicuotaPadron.getAlicuota(), alicuotaPadron.getDesde(), alicuotaPadron.getHasta());
							}
						}						
						if (alicuota == null){
							Impuesto impuesto = Impuesto.buscarPorDefinicionImpuesto(itemRetencion.getImpuesto().getTipo());
							alicuota = impuesto.getAlicuotaGeneral();
						}
						if (alicuota == null){
							throw new ValidationException("No se encontró alicuota para " + itemRetencion.getImpuesto().toString() + " debe asignar una alicuota general en el impuesto");
						}
						// se calcular toda la retención de una, aunque no pague todo (igual que las retenciones de ganancias)
						BigDecimal neto = BigDecimal.ZERO;
						for(CuentaCorrienteCompra ctacte: pago.getComprobantesPorPagar()){
							if (!pago.tieneOtroPagoAsociado(ctacte)){
								if ((ctacte.getImputaciones() != null) && (!ctacte.getImputaciones().isEmpty())){
									for(ImputacionCompra imputacion: ctacte.getImputaciones()){
										if (!imputacion.getEstado().equals(Estado.Anulada)){
											throw new ValidationException("El comprobante " + ctacte.toString() + " tiene imputaciones: Para el cálculo de retenciones debe anular la imputación y agregar las notas de crédito a los comprobantes por pagar");
										}
									}
								}
								neto = neto.add(ctacte.getNeto1()); 
							}
						}						
						// los impuestos se calculan en moneda1
						itemRetencion.setRetencionesAnteriores(BigDecimal.ZERO);
						itemRetencion.setMontoNoSujetoRetencion(BigDecimal.ZERO);
						itemRetencion.setAlicuota(alicuota);												 
						itemRetencion.setNetoAcumulado(neto);
						itemRetencion.setRetencionTotal(neto.multiply(alicuota).divide(new BigDecimal(100), 2, RoundingMode.HALF_EVEN));						
					}
				}
				
			}
		}		
	}
	
	protected abstract EntidadImpuesto determinarEntidadImpuesto(Proveedor proveedor);
	
	protected abstract AlicuotaPadron buscarAlicuotaPadron(Date fecha, Proveedor proveedor);

}
