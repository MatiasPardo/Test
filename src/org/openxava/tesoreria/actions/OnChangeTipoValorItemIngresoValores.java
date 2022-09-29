package org.openxava.tesoreria.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.tesoreria.model.*;
import org.openxava.util.*;

public class OnChangeTipoValorItemIngresoValores extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if(this.getNewValue() != null){
			TipoValorConfiguracion tipoValor = XPersistence.getManager().find(TipoValorConfiguracion.class, this.getNewValue());
			if (this.getView().isKeyEditable()){
				this.copiarValoresItemAnterior(tipoValor);
			}
			
			ItemIngresoValores item = (ItemIngresoValores)this.getView().getMetaModel().getPOJOClass().newInstance();
			item.setTipoValor(tipoValor);
			List<String> ocultar = new LinkedList<String>();
			List<String> visualizar = new LinkedList<String>();
			item.propiedadesOcultas(ocultar, visualizar);
			for(String str: ocultar){
				this.getView().setHidden(str, true);
			}
			for(String str: visualizar){
				this.getView().setHidden(str, false);
			}
		}		
	}
	
	private void copiarValoresItemAnterior(TipoValorConfiguracion tipoValor){
		if (!Is.emptyString(this.getNewValue().toString())){
			if (tipoValor.getComportamiento().equals(TipoValor.ChequeTercero)){
				try {
					ITransaccionValores tr = (ITransaccionValores)MapFacade.findEntity(this.getView().getParent().getModelName(), this.getView().getParent().getKeyValues());
					List<IItemMovimientoValores> items = new LinkedList<IItemMovimientoValores>();
					tr.movimientosValores(items);
					IItemMovimientoValores ultimoItemRegistrado = null;
					for(IItemMovimientoValores item: items){
						if (item.getTipoValor().equals(tipoValor)){
							if (ultimoItemRegistrado == null){
								ultimoItemRegistrado = item;
							}
							else if (item.itemTrValores().getFechaCreacion().compareTo(ultimoItemRegistrado.itemTrValores().getFechaCreacion()) > 0){
								ultimoItemRegistrado = item;								
							}
						}
					}
					
					if (ultimoItemRegistrado != null){
						try{
							if (ultimoItemRegistrado.getBanco() != null){
								Map<String, Object> banco = new HashMap<String, Object>();
								banco.put("id", ultimoItemRegistrado.getBanco().getId());
								this.getView().setValue("banco", banco);
							}
							this.getView().setValue("firmante", ultimoItemRegistrado.getFirmante());
							this.getView().setValue("cuitFirmante", ultimoItemRegistrado.getCuitFirmante());
							this.getView().setValue("nroCuentaFirmante", ultimoItemRegistrado.getNroCuentaFirmante());
						}
						catch(Exception e){							
						}
					}	
				} catch (Exception e) {					
				}				
			}
		}
	}

}
