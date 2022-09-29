package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;

public class OnChangeFechaDesdeAction extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if (getNewValue() != null){
			Date fecha = (Date)getNewValue();
			Calendar cal = Calendar.getInstance();
			cal.setTime(fecha);
			
			cal.set(cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH),
					cal.getActualMinimum(Calendar.DAY_OF_MONTH),
					cal.getMinimum(Calendar.HOUR_OF_DAY),
					cal.getMinimum(Calendar.MINUTE),
					cal.getMinimum(Calendar.SECOND));
			getView().setValue("fechaDesde", cal.getTime());
			
			cal.set(cal.get(Calendar.YEAR),
					cal.get(Calendar.MONTH),
					cal.getActualMaximum(Calendar.DAY_OF_MONTH),
					cal.getMaximum(Calendar.HOUR_OF_DAY),
					cal.getMaximum(Calendar.MINUTE),
					cal.getMaximum(Calendar.SECOND));
					cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
			getView().setValue("fechaHasta", cal.getTime());
		}
		
	}
	
}
