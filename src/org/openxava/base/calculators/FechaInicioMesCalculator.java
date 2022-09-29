package org.openxava.base.calculators;

import java.util.*;

import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class FechaInicioMesCalculator implements ICalculator{

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
		cal.set(cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH),
				cal.getActualMinimum(Calendar.DAY_OF_MONTH),
				cal.getMinimum(Calendar.HOUR_OF_DAY),
				cal.getMinimum(Calendar.MINUTE),
				cal.getMinimum(Calendar.SECOND));
		return cal.getTime();
	}
}
