package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.util.*;

public class OnChangeProximoNumeroEnNumerador extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {

		if (this.getNewValue() != null){			
			if (getView().getValue("id") != null){
				this.getView().setValue("modificadoPor", Users.getCurrent());
				this.getView().setValue("fechaModificacion", new Date());			
			}
		}
		
	}

}
