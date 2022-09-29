package org.openxava.base.calculators;

import java.util.*;

import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class FechaFinMesCalculator implements ICalculator{

	private Date fecha = new Date();
	
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	public Object calculate() throws Exception {	
		Calendar cal = Calendar.getInstance();
		cal.setTime(this.getFecha());
		cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
		cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
		cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
		cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}
	
}
