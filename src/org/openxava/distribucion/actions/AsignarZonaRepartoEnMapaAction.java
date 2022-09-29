package org.openxava.distribucion.actions;

import org.openxava.util.Is;

import com.clouderp.maps.actions.ShowMapSelectedForPolygonAction;
import com.clouderp.maps.actions.polygon.AsignarZonaRepartoPolygonAction;

public class AsignarZonaRepartoEnMapaAction  extends ShowMapSelectedForPolygonAction{

	@Override
	public void execute() throws Exception {
		if (!Is.emptyString(this.getView().getValueString("zona.id"))){
			this.getRequest().getSession().setAttribute(AsignarZonaRepartoPolygonAction.PARAMETROZONA, this.getView().getValueString("zona.id"));
		}
		else{
			this.addError("Falta asignar zona");
		}
		
		super.execute();
	}
}
