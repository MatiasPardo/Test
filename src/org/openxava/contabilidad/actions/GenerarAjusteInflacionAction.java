package org.openxava.contabilidad.actions;

import org.openxava.actions.ViewBaseAction;
import org.openxava.base.model.Empresa;
import org.openxava.base.model.Esquema;
import org.openxava.contabilidad.model.AjusteInflacion;
import org.openxava.contabilidad.model.Asiento;
import org.openxava.contabilidad.model.EjercicioContable;
import org.openxava.jpa.XPersistence;
import org.openxava.model.MapFacade;
import org.openxava.negocio.model.Sucursal;
import org.openxava.util.Is;

public class GenerarAjusteInflacionAction extends ViewBaseAction{

	@Override
	public void execute() throws Exception {
		EjercicioContable ejercicio = (EjercicioContable)MapFacade.findEntity(this.getPreviousView().getModelName(), this.getPreviousView().getKeyValues());
		Empresa empresa = null;
		Sucursal sucursal = null;
		String idEmpresa = this.getView().getValueString("empresa.id");
		if (Is.emptyString(idEmpresa)){
			this.addError("Empresa no asignada");
		}
		else{
			empresa = XPersistence.getManager().find(Empresa.class, idEmpresa);
		}
		
		String idSucursal = this.getView().getValueString("sucursal.id");
		if (Is.emptyString(idSucursal)){
			if (Esquema.getEsquemaApp().getSucursalUnica()){
				sucursal = Sucursal.sucursalDefault();
			}
			else{
				this.addError("Sucursal no asignada");
			}
		}
		else{		
			sucursal = XPersistence.getManager().find(Sucursal.class, idSucursal);
		}
		
		if (this.getErrors().isEmpty()){
			AjusteInflacion ajusteInflacion = new AjusteInflacion();
			ajusteInflacion.setEjercicio(ejercicio);
			ajusteInflacion.setEmpresa(empresa);
			ajusteInflacion.setSucursal(sucursal);
			Asiento asiento = ajusteInflacion.generarAjusteInflacion();
			this.commit();
			this.closeDialog();
			this.addMessage("ejecucion_OK");			
		}
	}
}
