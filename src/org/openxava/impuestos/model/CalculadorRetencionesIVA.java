package org.openxava.impuestos.model;

import java.math.BigDecimal;

import org.openxava.afip.model.TipoComprobanteArg;
import org.openxava.base.model.Empresa;
import org.openxava.base.model.Estado;
import org.openxava.base.model.ItemTransaccion;
import org.openxava.base.model.Transaccion;
import org.openxava.compras.model.CompraElectronica;
import org.openxava.compras.model.EntidadRetencionProveedor;
import org.openxava.cuentacorriente.model.CuentaCorrienteCompra;
import org.openxava.cuentacorriente.model.ImputacionCompra;
import org.openxava.tesoreria.model.ItemPagoRetencion;
import org.openxava.tesoreria.model.PagoProveedores;
import org.openxava.validators.ValidationException;

public class CalculadorRetencionesIVA implements ICalculadorImpuesto{

	@Override
	public void calcular(Transaccion transaccion, ItemTransaccion item, int nroImpuesto) {
		PagoProveedores pago = (PagoProveedores) transaccion;
		ItemPagoRetencion itemRetencion = (ItemPagoRetencion) item;
		if (pago != null){
			Empresa empresa = transaccion.getEmpresa();
			if ((empresa != null) && (empresa.getAgenteRecaudacion())){
				EntidadRetencionProveedor entidadImpuesto = pago.getProveedor().configuracionImpuesto(itemRetencion.getImpuesto());
				boolean ponerEnCero = true;
				if (entidadImpuesto != null && entidadImpuesto.getCalcula()){
					ponerEnCero = false;
					itemRetencion.setMontoNoSujetoRetencion(BigDecimal.ZERO);
					BigDecimal importeRetencion = BigDecimal.ZERO;						
					for(CuentaCorrienteCompra ctacte: pago.getComprobantesPorPagar()){
						if (!pago.tieneOtroPagoAsociado(ctacte)){
							if ((ctacte.getImputaciones() != null) && (!ctacte.getImputaciones().isEmpty())){
								for(ImputacionCompra imputacion: ctacte.getImputaciones()){
									if (!imputacion.getEstado().equals(Estado.Anulada)){
										throw new ValidationException("El comprobante " + ctacte.toString() + " tiene imputaciones: Para el cálculo de retenciones debe anular la imputación y si tiene notas de crédito debe agregar a los comprobantes por pagar");
									}
								}
							}
							
							CompraElectronica compra = (CompraElectronica)ctacte.buscarTransaccion();
							if (compra.getTipo().tipoComprobanteAfip().equals(TipoComprobanteArg.M)){
								// Se retiene todo el iva para los comprobantes de tipo M
								Integer coeficiente = compra.CtaCteCoeficiente();
								BigDecimal iva = compra.getIva1();
								if (coeficiente == -1){
									iva = iva.negate();
								}
								importeRetencion = importeRetencion.add(iva);
							}
						}
					}
					itemRetencion.setAlicuota(BigDecimal.ZERO);
					
					if (importeRetencion.compareTo(BigDecimal.ZERO) < 0) importeRetencion = BigDecimal.ZERO;
					itemRetencion.setRetencionTotal(importeRetencion);											
				}
				
				if (ponerEnCero){					
					itemRetencion.setRetencionTotal(BigDecimal.ZERO);
				}
				itemRetencion.calcularRetencionActual();
			}
		}		
	}

}
