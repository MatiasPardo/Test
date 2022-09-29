package org.openxava.ventas.validators;

import org.openxava.annotations.PropertyValue;
import org.openxava.util.Messages;
import org.openxava.validators.IValidator;
import org.openxava.ventas.model.TipoProducto;

@SuppressWarnings("serial")
public class ProductoValidator implements IValidator{
	
	private TipoProducto tipo; 
	
	private Boolean lote; 
	
	private Boolean despacho;
	
	public TipoProducto getTipo() {
		return tipo;
	}

	public void setTipo(TipoProducto tipo) {
		this.tipo = tipo;
	}

	public Boolean getLote() {
		return lote == null ? Boolean.FALSE : lote;
	}

	public void setLote(Boolean lote) {
		this.lote = lote;
	}

	public Boolean getDespacho() {
		return despacho == null ? Boolean.FALSE : despacho;
	}

	public void setDespacho(Boolean despacho) {
		this.despacho = despacho;
	}

	@Override
	public void validate(Messages errors) throws Exception {
		if (this.getTipo() != null){
			if (!this.getTipo().stock()){
				if (this.getDespacho()){
					errors.add("No puede estar activado el despacho para el tipo " + this.getTipo().toString());
				}
				if (this.getLote()){
					errors.add("No puede estar activado el lote para el tipo " + this.getTipo().toString());
				}
			}
		}
	}
}
