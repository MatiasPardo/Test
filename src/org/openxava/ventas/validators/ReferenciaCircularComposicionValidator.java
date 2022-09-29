package org.openxava.ventas.validators;

import java.util.*;

import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@SuppressWarnings("serial")
public class ReferenciaCircularComposicionValidator implements IValidator{

	private Producto producto;
	
	private Producto componente;
	
	private String id;
	
	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	public Producto getComponente() {
		return componente;
	}

	public void setComponente(Producto componente) {
		this.componente = componente;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void validate(Messages errors) throws Exception {
		if ((this.getProducto() != null) && (this.getComponente() != null)){
			
			if (this.getProducto().equals(this.getComponente())){
				errors.add(this.getComponente().toString() + " coincide con el producto ");
			}
			else{
				Collection<Producto> estructuraComponente = new LinkedList<Producto>();
				this.getComponente().BOM(estructuraComponente);
				
				Map<String, Producto> estructuraSuperior = new HashMap<String, Producto>();
				this.getProducto().BOMNivelesSuperiores(estructuraSuperior);
				
				boolean noError = true;
				for(Producto comp: estructuraComponente){
					if (estructuraSuperior.containsKey(comp.getId())){
						if (noError){
							errors.add(this.getComponente().toString() + " no puede ser componente de " + this.getProducto().toString());
							noError = false;
						}
						errors.add("Referencia circular: El componente " + comp.toString() + " se encuentra en un nivel superior" );
					}
				}
			}
		}		
	}

}
