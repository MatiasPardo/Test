package org.openxava.tesoreria.actions;

import java.util.List;

import org.openxava.actions.CollectionBaseAction;
import org.openxava.actions.IChainAction;
import org.openxava.base.model.Transaccion;
import org.openxava.model.MapFacade;
import org.openxava.tesoreria.model.ICuponTarjeta;

public class BorrarCuponesLiquidacionTarjetaAction extends CollectionBaseAction implements IChainAction{

	@Override
	public void execute() throws Exception {
		List<?> cupones = this.getSelectedObjects();
		if (!cupones.isEmpty()){
			for(Object cupon: cupones){
				this.borrarCupon((ICuponTarjeta)cupon);
			}			
			this.commit();

			addMessage("association_removed", getCollectionElementView().getModelName(), 
					getCollectionElementView().getParent().getModelName());
			
			Transaccion tr = (Transaccion)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
			tr.grabarTransaccion();
			this.commit();
		}
	}
	
	private void borrarCupon(ICuponTarjeta cupon) throws Exception{
		cupon.setLiquidacionTarjeta(null);
	}
	
	@Override
	public String getNextAction() throws Exception {
		if (this.getErrors().isEmpty()){
			return "Transaccion.editar";
		}
		else{
			return null;
		}
	}
}
