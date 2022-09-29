package org.openxava.negocio.validators;

import org.openxava.base.model.ObjetoEstatico;
import org.openxava.base.validators.ItemTransaccionValidator;
import org.openxava.util.Messages;

@SuppressWarnings("serial")
public class ActivoObjetoEstaticoEnItemTransaccionValidator extends ItemTransaccionValidator{

	private ObjetoEstatico objetoEstatico;
	
	public ObjetoEstatico getObjetoEstatico() {
		return objetoEstatico;
	}

	public void setObjetoEstatico(ObjetoEstatico objetoEstatico) {
		this.objetoEstatico = objetoEstatico;
	}
	
	@Override
	protected void validarItemTransaccion(Messages errores) {
		if (this.getObjetoEstatico() != null){
			if (!this.getObjetoEstatico().getActivo()){
				errores.add(this.getObjetoEstatico().getCodigo() + " no esta activo");
			}
		}
	}

}
