package org.openxava.impuestos.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.openxava.base.model.ItemTransaccion;
import org.openxava.base.model.Transaccion;
import org.openxava.validators.ValidationException;


public class CalculadorPercepcionesGlobal implements ICalculadorImpuesto{

	private DefinicionImpuesto tipo;
	
	public CalculadorPercepcionesGlobal(DefinicionImpuesto defImpuesto){
		this.tipo = defImpuesto;
	}	
	
	// En la configuración de impuesto, se busca la alicuota que corresponda al cliente según su posición de iva.
	// Si no esta definida, se toma la alicuota general del impuesto	
	@Override
	public void calcular(Transaccion transaccion, ItemTransaccion item, int nroImpuesto) {
		try {
			ITrCalculaPercepcionVenta tr = (ITrCalculaPercepcionVenta)transaccion;			
			BigDecimal percepcion = BigDecimal.ZERO;
			BigDecimal alicuota = BigDecimal.ZERO;
			if (!tr.revierteTransaccion()){
				if (tr.getSubtotal().compareTo(BigDecimal.ZERO) > 0){
					Impuesto impuesto = Impuesto.buscarPorDefinicionImpuesto(this.tipo);					
					AlicuotaImpuesto alicuotaImp = impuesto.buscarAlicuota(tr.getPosicionIva().posicion());
					if (alicuotaImp != null){
						alicuota = alicuotaImp.getPorcentaje();
					}
					else{
						alicuota = impuesto.getAlicuotaGeneral();
					}
					if (alicuota == null){
						throw new ValidationException("No se encontró alicuota general para la percepción " + tipo.toString());
					}
					percepcion = calcularImportePercepcion(tr, alicuota);							
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
