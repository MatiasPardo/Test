package org.openxava.tesoreria.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;

@Entity

@Views({
	@View(name="", members=
			"destino;" + 
			"tipoValor;" +
			"importeOriginal, cotizacion, importe;" + 
			"detalle;" +
			"Datos[" +
				"numero;" + 
				"fechaEmision, fechaVencimiento;" + 
				"banco;" + 
			"];" + 
			"Firmante[" +
				"firmante;" + 
				"cuitFirmante, nroCuentaFirmante" +
			"]")
})

public class ItemCobranza extends ItemIngresoValores{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	private Cobranza cobranza;
		
	public Cobranza getCobranza() {
		return cobranza;
	}

	public void setCobranza(Cobranza cobranza) {
		this.cobranza = cobranza;
	}

	@Override
	public Transaccion transaccion() {
		return this.getCobranza();
	}

	@Override
	public void asignarOperadorComercial(Valor valor, Transaccion transaccion) {
		valor.setCliente(((Cobranza)transaccion).getCliente());		
	}
	
	public OperadorComercial operadorComercialValores(Transaccion transaccion){
		return ((Cobranza)transaccion).getCliente();
	}
}
