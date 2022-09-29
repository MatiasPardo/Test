package org.openxava.conciliacionbancaria.actions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.openxava.actions.ViewBaseAction;
import org.openxava.conciliacionbancaria.model.ExtractoBancario;
import org.openxava.model.MapFacade;
import org.openxava.tab.Tab;
import org.openxava.tesoreria.model.MovimientoValores;

public class ConciliarBancoAction extends ViewBaseAction{

	@Override
	public void execute() throws Exception {
		
		Tab extracto = this.getView().getSubview("extractoBancario").getCollectionTab();
		Tab movimientos = this.getView().getSubview("movimientosFinancieros").getCollectionTab();
		
		Map<?, ?>[] extractoSeleccionados = extracto.getSelectedKeys();
		Map<?, ?>[] movimientosSeleccionados = movimientos.getSelectedKeys();
		
		Collection<MovimientoValores> movimientosAConciliar = new LinkedList<MovimientoValores>();
		for(Map<?, ?> key: movimientosSeleccionados){
			movimientosAConciliar.add((MovimientoValores)MapFacade.findEntity("MovimientoValores", key));					
		}
		Collection<ExtractoBancario> extractoAConciliar = new LinkedList<ExtractoBancario>();
		for(Map<?, ?> key: extractoSeleccionados){
			extractoAConciliar.add((ExtractoBancario)MapFacade.findEntity("ExtractoBancario", key));
		}
				
		ExtractoBancario.conciliar(movimientosAConciliar, extractoAConciliar);
		this.commit();
		this.addMessage("Conciliados");
					
		movimientos.deselectAll();
		extracto.deselectAll();
		this.getView().refreshCollections();								
	}

}
