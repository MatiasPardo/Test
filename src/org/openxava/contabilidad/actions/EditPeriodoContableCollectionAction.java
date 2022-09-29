package org.openxava.contabilidad.actions;

import org.openxava.actions.*;
import org.openxava.contabilidad.model.*;
import org.openxava.util.*;

public class EditPeriodoContableCollectionAction extends EditElementInCollectionAction{
	public void execute() throws Exception {
		super.execute();
		
		if (Is.equal(this.getCollectionElementView().getValue("estado"), EstadoPeriodoContable.Cerrado)){
			this.getCollectionElementView().setEditable(false);
		}
		else{
			this.getCollectionElementView().setEditable(true);
		}
	}
}
