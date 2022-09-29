package org.openxava.reclamos.actions;

import java.util.*;
import org.openxava.actions.*;
import org.openxava.reclamos.model.*;

public class AsignarReclamoAction extends TabBaseAction{

	public void execute() throws Exception {
		this.showDialog();
		getView().setTitle("AsignarReclamos");
		getView().setModelName(ParametrosAsignacionReclamo.class.getSimpleName());
		getView().setValue("fechaDeAtencion", new Date());
		this.addActions("AsignarReclamo.confirmar","Dialog.cancel");	
	}
}