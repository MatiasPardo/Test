package org.openxava.impuestos.model;

import java.math.*;

import org.openxava.base.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

public class CalculadorPercepcionesBsAs implements ICalculadorImpuesto {
	
	@Override
	public void calcular(Transaccion transaccion, ItemTransaccion item, int nroImpuesto) {
		try {
			ITrCalculaPercepcionVenta tr = (ITrCalculaPercepcionVenta)transaccion;
			Cliente cliente = tr.getCliente();
			BigDecimal percepcion = BigDecimal.ZERO;
			BigDecimal alicuota = BigDecimal.ZERO;
			if (!tr.revierteTransaccion()){
				if ((cliente != null) && (tr.getSubtotal().compareTo(BigDecimal.ZERO) > 0)){
					if (cliente.getPercepcionARBA() != null){
						if (cliente.getPercepcionARBA().debeCalcularImpuesto(tr.getFecha())){
							// código 1: BsAs (ARBA)
							//if (tr.domicilioCalculoPercepcion() != null){
							//	if (tr.domicilioCalculoPercepcion().getCiudad().getProvincia().getCodigo() == 1){ 
									alicuota = cliente.buscarAlicuotaPercepcionBsAs(tr.getFecha());
									if (alicuota == null){
										Impuesto impuesto = Impuesto.buscarPorDefinicionImpuesto(DefinicionImpuesto.PercepcionBsAs);
										alicuota = impuesto.getAlicuotaGeneral();
									}
									if (alicuota == null){
										throw new ValidationException("No se encontró alicuota para la percepción de BsAs: debe asignar una alicuota general en el impuesto");
									}
									percepcion = calcularImportePercepcion(tr, alicuota);							
								//}
							//}
						}
					}				
				}				
			}
			else{
				// como es un movimiento de reversión, se toma la alicuota que tenga, no se recalcula 
				alicuota = (BigDecimal)transaccion.getClass().getMethod("getAlicuota" + Integer.toString(nroImpuesto)).invoke(transaccion);
				percepcion = calcularImportePercepcion(tr, alicuota);
			}
			
			transaccion.getClass().getMethod("setPercepcion" + Integer.toString(nroImpuesto), BigDecimal.class).invoke(transaccion, percepcion);
			transaccion.getClass().getMethod("setAlicuota" + Integer.toString(nroImpuesto), BigDecimal.class).invoke(transaccion, alicuota);
		} catch (Exception e) {
			String error = e.getMessage();
			if (error == null){
				error = e.toString();
			}
			throw new ValidationException(error);
		}
	}
	
	private BigDecimal calcularImportePercepcion(ITrCalculaPercepcionVenta tr, BigDecimal alicuota){
		return tr.getSubtotal().multiply(alicuota).divide(new BigDecimal(100)).setScale(4, RoundingMode.HALF_EVEN);
	}

}
