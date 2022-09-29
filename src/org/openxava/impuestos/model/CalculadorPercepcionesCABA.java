package org.openxava.impuestos.model;

import java.math.*;

import org.openxava.base.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

public class CalculadorPercepcionesCABA implements ICalculadorImpuesto {

	@Override
	public void calcular(Transaccion transaccion, ItemTransaccion item, int nroImpuesto) {
		try {
			ITrCalculaPercepcionVenta tr = (ITrCalculaPercepcionVenta)transaccion;
			Cliente cliente = tr.getCliente();
			BigDecimal percepcion = BigDecimal.ZERO;
			BigDecimal alicuota = BigDecimal.ZERO;
			if (!tr.revierteTransaccion()){
				if ((cliente != null) && (tr.getSubtotal().compareTo(BigDecimal.ZERO) > 0)){
					if (cliente.getPercepcionCABA() != null){
						if (cliente.getPercepcionCABA().debeCalcularImpuesto(tr.getFecha())){
							// c�digo 2: CABA
							//if (tr.domicilioCalculoPercepcion() != null){
							//	if (tr.domicilioCalculoPercepcion().getCiudad().getProvincia().getCodigo() == 2){ 
									alicuota = cliente.buscarAlicuotaPercepcionCABA(tr.getFecha());
									if (alicuota == null){
										Impuesto impuesto = Impuesto.buscarPorDefinicionImpuesto(DefinicionImpuesto.PercepcionCABA);
										alicuota = impuesto.getAlicuotaGeneral();
									}
									if (alicuota == null){
										throw new ValidationException("No se encontr� alicuota para la percepceci�n de CABA: debe asignar una alicuota general en el impuesto");
									}
									percepcion = calcularImportePercepcion(tr, alicuota);							
							//	}
							//}
						}
					}				
				}
			}
			else{
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
