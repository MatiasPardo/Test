package org.openxava.ventas.calculators;

import org.openxava.jpa.XPersistence;
import org.openxava.negocio.calculators.ObjetoPrincipalCalculator;
import org.openxava.util.Is;
import org.openxava.ventas.model.Cliente;
import org.openxava.ventas.model.TipoOperacionVenta;

@SuppressWarnings("serial")
public class TipoOperacionVentaCalculator extends ObjetoPrincipalCalculator{

	private String idCliente;

	public String getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(String idCliente) {
		this.idCliente = idCliente;
	}
	
	private Cliente cliente = null;
	
	private Cliente getCliente(){
		if (this.cliente == null){
			if (!Is.emptyString(this.idCliente)){
				this.cliente = (Cliente)XPersistence.getManager().find(Cliente.class, this.idCliente);
			}	
		}
		return this.cliente;
	}

	@Override
	public Object calculate() throws Exception {
		Cliente cliente = this.getCliente();
		if (cliente != null){
			if (cliente.getConsignacion()){
				return TipoOperacionVenta.buscarTipoOperacionPorConsignacionPcipal();
			}
			else{
				return super.calculate();
			}
		}
		else{
			return super.calculate();
		}
	}
}

