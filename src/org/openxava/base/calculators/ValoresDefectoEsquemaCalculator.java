package org.openxava.base.calculators;

import java.lang.reflect.*;

import org.openxava.base.model.*;
import org.openxava.calculators.*;

@SuppressWarnings("serial")
public class ValoresDefectoEsquemaCalculator implements ICalculator{

	private String atributo;
	
	public String getAtributo() {
		return atributo;
	}

	public void setAtributo(String atributo) {
		this.atributo = atributo;
	}

	@Override
	public Object calculate() throws Exception {		
		Method method = Esquema.class.getMethod("get" + this.getAtributo());
		return method.invoke(Esquema.getEsquemaApp());		
	}

}
