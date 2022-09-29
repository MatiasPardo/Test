package org.openxava.contabilidad.actions;

import org.openxava.actions.*;
import org.openxava.contabilidad.model.*;
import org.openxava.util.*;

public class OnChangeEstadoPeriodoContableAction extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if (Is.equal(this.getNewValue(), EstadoPeriodoContable.Cerrado)){
			this.addWarning("Advertencia: una vez cerrado no podrá generar asientos contables");
		}
	}

}
