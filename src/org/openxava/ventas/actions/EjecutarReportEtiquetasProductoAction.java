package org.openxava.ventas.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

public class EjecutarReportEtiquetasProductoAction extends TabBaseAction implements IChainAction{

	private List<Producto> productos = null;
	
	private int cantidad = 0;
	
	private Integer numero = 1;
	
	private ListaPrecio listaPrecio;
	
	@SuppressWarnings("rawtypes")
	@Override
	public void execute() throws Exception {
		
		int etiquetasPorFila = Esquemas.getEsquemaApp().getEtiquetasPorFila();
		if (etiquetasPorFila <= 0){
			throw new ValidationException("No esta configurada la impresión de etiquetas: Debe configurar las etiquetas por fila");
		}
		
		this.cantidad = getView().getValueInt("cantidad");		
		if (cantidad <= 0){
			this.addError("Cantidad requerida");
		}
		
		String idListaPrecio = getView().getValueString("listaPrecio.id");
		if (Is.emptyString(idListaPrecio)){
			this.addError("Lista de precio requerida");
		}
		else{
			this.listaPrecio = XPersistence.getManager().find(ListaPrecio.class, idListaPrecio);
		}
		
		Map [] selectedOnes = getSelectedKeys();
		this.productos = new LinkedList<Producto>();		
		if (selectedOnes != null) {
			for (int i = 0; i < selectedOnes.length; i++) {
				Map clave = selectedOnes[i];
				Producto producto = (Producto)MapFacade.findEntity(this.getTab().getModelName(), clave);
				this.productos.add(producto);				
			}
			if (this.productos.isEmpty()){
				this.addError("Debe seleccionar al menos un producto");
			}			
		}
		else{
			this.addError("No hay productos seleccionados");
		}
		
		if (this.getErrors().isEmpty()){
			this.getTab().deselectAll();			
		}
	}
	
	@Override
	public String getNextAction() throws Exception {
		if (this.getErrors().isEmpty()){
			this.getRequest().setAttribute("cantidad", this.cantidad);
			this.getRequest().setAttribute("productos", this.productos);
			this.getRequest().setAttribute("listaPrecio", this.listaPrecio);
			this.getRequest().setAttribute("numero", this.numero);
			return "Producto.ReportEtiquetas";
		}
		else{
			return null;
		}
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		if (numero != null){
			this.numero = numero;
		}
	}	
}
