package org.openxava.ventas.actions;

import java.rmi.*;
import java.util.*;

import javax.ejb.*;

import org.openxava.actions.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

public class AddProductosToPedidoVentaAction extends AddElementsToCollectionAction implements IChainAction{
	
	private PedidoVenta pedido = null;
	
	private PedidoVenta getPedido(){
		if (pedido == null){
			try {
				this.pedido = (PedidoVenta)MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return this.pedido;
	}
	
	@Override
	public void execute() throws Exception {
		super.execute();
		
		this.getPedido().grabarTransaccion();
		this.commit();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected void associateEntity(Map keyValues) throws ValidationException, XavaException, ObjectNotFoundException, FinderException, RemoteException {		
		super.associateEntity(keyValues);
		Producto producto = (Producto) MapFacade.findEntity("Producto", keyValues);
		try {
			this.getPedido().crearItemPedido(producto);
			this.pedido = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public String getNextAction() throws Exception {
		return "Transaccion.editar";
	}
	
	
}