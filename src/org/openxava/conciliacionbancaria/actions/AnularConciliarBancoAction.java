package org.openxava.conciliacionbancaria.actions;

import java.util.List;

import org.openxava.actions.CollectionBaseAction;
import org.openxava.conciliacionbancaria.model.GrupoConciliacion;
import org.openxava.conciliacionbancaria.model.IObjetoConciliable;

public class AnularConciliarBancoAction extends CollectionBaseAction {

	@Override
	public void execute() throws Exception {
		
		List<?> lista = this.getSelectedObjects();
		try{
			for(Object obj: lista){
				if (obj instanceof GrupoConciliacion){
					GrupoConciliacion grupo = (GrupoConciliacion)obj;
					grupo.anularConciliacion();
				}
				else{
					IObjetoConciliable objeto = (IObjetoConciliable)obj;
					objeto.anularConciliacion();
				}
			}
			this.commit();
			addMessage("ejecucion_OK");
		}
		finally{
			this.getCollectionElementView().collectionDeselectAll();
			this.getView().refreshCollections();
		}
	}
	
}
