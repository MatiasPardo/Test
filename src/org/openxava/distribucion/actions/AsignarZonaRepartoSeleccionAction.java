package org.openxava.distribucion.actions;

import java.util.Map;

import org.openxava.actions.TabBaseAction;
import org.openxava.distribucion.model.AsignacionZonaReparto;
import org.openxava.distribucion.model.ZonaReparto;
import org.openxava.jpa.XPersistence;
import org.openxava.model.MapFacade;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

public class AsignarZonaRepartoSeleccionAction extends TabBaseAction{

	@Override
	public void execute() throws Exception {
		ZonaReparto zona = null;
		if (!Is.emptyString(this.getView().getValueString("zona.id"))){
			zona = XPersistence.getManager().find(ZonaReparto.class, this.getView().getValueString("zona.id"));
		}
		for(@SuppressWarnings("rawtypes") Map key: this.getSelectedKeys()){
			AsignacionZonaReparto asignacion = (AsignacionZonaReparto)MapFacade.findEntity(this.getTab().getModelName(), key);
			try{
				asignacion.asignarZonaReparto(zona);
				this.commit();
			}
			catch(ValidationException v){
				this.rollback();
				this.addErrors(v.getErrors());
			}
			catch(Exception e){
				this.rollback();
				this.addError(e.toString());
			}
		}
		this.getTab().deselectAll();
		this.closeDialog();				
	}

}
