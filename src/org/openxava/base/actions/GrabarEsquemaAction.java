package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;

public class GrabarEsquemaAction extends SaveAction{

	@Override
	public void execute() throws Exception {
		
		super.execute();
		
		if (this.getErrors().isEmpty()){
			Esquema esquema = (Esquema)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
			esquema.grabar();
			this.commit();
		}
	}
}
