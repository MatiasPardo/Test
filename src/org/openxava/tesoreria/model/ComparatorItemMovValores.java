package org.openxava.tesoreria.model;

import java.util.*;

public class ComparatorItemMovValores implements Comparator<IItemMovimientoValores>{

	boolean reversion = false;
	
	public boolean isReversion() {
		return reversion;
	}

	public void setReversion(boolean reversion) {
		this.reversion = reversion;
	}



	@Override
	public int compare(IItemMovimientoValores o1, IItemMovimientoValores o2) {
		TipoMovimientoValores mov1 = o1.tipoMovimientoValores(this.isReversion());
		TipoMovimientoValores mov2 = o2.tipoMovimientoValores(this.isReversion());
		
		TipoValorConfiguracion tipoValor1 = o1.getTipoValor();
		TipoValorConfiguracion tipoValor2 = o2.getTipoValor();		
		if (mov1.debeConsolidar(o1.tesoreriaAfectada(), tipoValor1)){
			tipoValor1 = mov1.tipoValorConsolida(o1.tesoreriaAfectada(), tipoValor1);
		}
		if (mov2.debeConsolidar(o2.tesoreriaAfectada(), tipoValor2)){
			tipoValor2 = mov2.tipoValorConsolida(o2.tesoreriaAfectada(), tipoValor2);
		}
		
		int comparacion = 0;
		if (tipoValor1.getComportamiento().consolidaAutomaticamente()){
			if (tipoValor2.getComportamiento().consolidaAutomaticamente()){
				// Cuando ambos son efectivo, se ordena por tesoreria y después por tipo de valor
				comparacion = o1.tesoreriaAfectada().getFechaCreacion().compareTo(o2.tesoreriaAfectada().getFechaCreacion());
				if (comparacion == 0){
					comparacion = o1.tesoreriaAfectada().getId().compareTo(o2.tesoreriaAfectada().getId());
				}
				if (comparacion == 0){
					comparacion = tipoValor1.getFechaCreacion().compareTo(tipoValor2.getFechaCreacion());
					if (comparacion == 0){
						comparacion = tipoValor1.getId().compareTo(tipoValor2.getId());
					}
				}
			}
			else{
				// primero se actualiza los que consolidan
				comparacion = -1;
			}
		}
		else if(tipoValor2.getComportamiento().consolidaAutomaticamente()){
			comparacion = 1;
		}
		else{
			if (o1.referenciaValor() == null){
				if (o2.referenciaValor() == null){
					if (tipoValor1.getComportamiento().equals(TipoValor.ChequePropio)){
						if (tipoValor2.getComportamiento().equals(TipoValor.ChequePropio)){
							// se compara por chequera
							Date fechaCreacion1 = o1.tesoreriaAfectada().getFechaCreacion();
							String id1 = o1.tesoreriaAfectada().getId();
							if (o1.chequera() != null){
								fechaCreacion1 = o1.chequera().getFechaCreacion();
								id1 = o1.chequera().getId();
							}
							Date fechaCreacion2 = o2.tesoreriaAfectada().getFechaCreacion();
							String id2 = o2.tesoreriaAfectada().getId();
							if (o2.chequera() != null){
								fechaCreacion2 = o2.chequera().getFechaCreacion();
								id2 = o2.chequera().getId();
							}							
							comparacion = fechaCreacion1.compareTo(fechaCreacion2);
							if (comparacion == 0){
								comparacion = id1.compareTo(id2);
							}
						}
						else{
							comparacion = -1;
						}
					}
					else if (tipoValor2.getComportamiento().equals(TipoValor.ChequePropio)){
						comparacion = 1;
					}
					else{
						comparacion = tipoValor1.getFechaCreacion().compareTo(tipoValor2.getFechaCreacion());
						if (comparacion == 0){
							comparacion = tipoValor1.getId().compareTo(tipoValor2.getId());
						}
					}
					
				}
				else{
					// primero se actualizan los movimientos que crean valores
					comparacion = -1;
				}
			}
			else if (o2.referenciaValor() == null){
				comparacion = 1;
			}
			else{
				Valor valor1 = o1.referenciaValor();
				Valor valor2 = o2.referenciaValor();
				comparacion = valor1.getFechaCreacion().compareTo(valor2.getFechaCreacion());
				if (comparacion == 0){
					comparacion = valor1.getId().compareTo(valor2.getId());
				}
			}
		}
		return comparacion;
	}
}
