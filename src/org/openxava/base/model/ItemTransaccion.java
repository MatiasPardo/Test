package org.openxava.base.model;

import javax.persistence.*;

import org.openxava.validators.*;
import org.openxava.view.*;

@MappedSuperclass

public abstract class ItemTransaccion extends ObjetoNegocio{
	
	/*public static void copiarValoresCabecera(View cabecera, View items) {
		String classNameView = items.getMetaModel().getPOJOClassName() + "View";
		try{
			ItemTransaccionView itemTransaccionView = null;
			try{
				Class<?> classView = Class.forName(classNameView);
				itemTransaccionView = (ItemTransaccionView)classView.newInstance();
			}
			catch(Exception e){
				itemTransaccionView = new ItemTransaccionView();
			}
			itemTransaccionView.copiarValoresCabecera(cabecera, items);
		}
		catch(Exception e){
			throw new ValidationException("Error al copiar valores de la cabecera a los items: " + e.toString());
		}
	}*/
	public static ItemTransaccionView itemTransaccionView(View items){
		String classNameView = items.getMetaModel().getPOJOClassName() + "View";
		try{
			ItemTransaccionView itemTransaccionView = null;
			try{
				Class<?> classView = Class.forName(classNameView);
				itemTransaccionView = (ItemTransaccionView)classView.newInstance();
			}
			catch(Exception e){
				itemTransaccionView = new ItemTransaccionView();
			}
			return itemTransaccionView;
		}
		catch(Exception e){
			throw new ValidationException("Error ItemTransactionView: " + e.toString());
		}
	}

	
	@Override
	protected void onPrePersist(){
		super.onPrePersist();
		
		if (this.transaccion() != null){
			this.transaccion().verificarEstadoParaModificarTr();
		}	
	}

	@Override
	protected void onPreUpdate(){
		super.onPreUpdate();
		/*this.recalcular();
		
		if (this.transaccion() != null){
			if (this.transaccion().ejecutarRecalculoTotales()){
				this.transaccion().recalcularTotales();
			}
		}*/
	}
	
	@Override
	public void onPostRemove(){
		super.onPostRemove();
		
		if (this.transaccion() != null){
			this.transaccion().verificarEstadoParaModificarTr();
		}
	}
	
	@Override
	public Boolean soloLectura(){
		return Boolean.TRUE;		
	}
	
	public abstract Transaccion transaccion();
	
	public abstract void recalcular();
}
