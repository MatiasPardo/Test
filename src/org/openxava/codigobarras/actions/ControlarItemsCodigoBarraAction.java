package org.openxava.codigobarras.actions;

import java.math.BigDecimal;

import org.openxava.base.actions.PrimeroGrabarTrDespuesEjecutarItemAction;
import org.openxava.codigobarras.model.CodigoBarrasProducto;
import org.openxava.compras.model.Proveedor;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.calculators.ObjetoPrincipalCalculator;
import org.openxava.util.Is;

public class ControlarItemsCodigoBarraAction extends PrimeroGrabarTrDespuesEjecutarItemAction{
	
	private String proximaAccion = null;

	public String getProximaAccion() {
		return proximaAccion;
	}

	public void setProximaAccion(String proximaAccion) {
		this.proximaAccion = proximaAccion;
	}
	
	@Override
	protected void ejecutarAccionItem() throws Exception{				
		try{
			this.showDialog();
			getView().setTitle("Lector");		
			getView().setModelName("ParametrosLectorCodigoBarras");
			
			CodigoBarrasProducto configurador = null;
			if (configurador == null){
				ObjetoPrincipalCalculator calculator = new ObjetoPrincipalCalculator();
				calculator.setEntidad("CodigoBarrasProducto");
				configurador = (CodigoBarrasProducto)calculator.calculate();
			}
			
			if (configurador != null){
				this.getView().getSubview("tipoCodigoBarras").setModel(configurador);
			}
			// se oculta el total, se muestra únicamente si la transacción utiliza total
			this.getView().setHidden("total", true);
			this.getView().setValue("cantidad", new BigDecimal(1));
			this.getView().setFocus("codigoBarras");		

			this.addActions(this.getProximaAccion(), "LectorCodigoBarras.cancel");
		}
		catch(Exception e){
			this.addError(e.toString());
		}
	}
}
