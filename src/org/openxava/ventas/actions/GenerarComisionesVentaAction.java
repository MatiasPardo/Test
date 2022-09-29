package org.openxava.ventas.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.ventas.model.*;

public class GenerarComisionesVentaAction extends TabBaseAction{

	private Empresa empresa;
	
	private Date desde;
	
	private Date hasta;
	
	@Override
	public void execute() throws Exception {
		this.desde = (Date)this.getView().getValue("desde");
		this.hasta = (Date)this.getView().getValue("hasta");
		String empresaId = this.getView().getValueString("empresa.id");
		
		if (Is.emptyString(empresaId)){
			addError("Empresa no asignada");
		}
		if (desde == null){
			addError("Fecha desde no asignada");
		}
		else if (hasta == null){
			addError("Fecha hasta no asignada");
		}
		else if (desde.compareTo(hasta) > 0){
			addError("Error en Fechas: desde no puede superar a Hasta");
		}
		
		if (this.getErrors().isEmpty()){
			this.empresa = XPersistence.getManager().find(Empresa.class, empresaId);
			generarComisionesVenta();
			this.closeDialog();
			
			if (this.getMessages().isEmpty()){
				addMessage("ejecucion_OK");
			}
		}
	}

	private void generarComisionesVenta(){
		List<Vendedor> vendedores = new LinkedList<Vendedor>();
		LiquidacionComisionVenta.buscarVendedoresParaComisionar(vendedores);
		if (!vendedores.isEmpty()){
			addMessage("Generadas las comisiones de venta para: ");
			List<String> idsLiquidaciones = new LinkedList<String>();
			for(Vendedor vendedor: vendedores){
				String id = null;
				try{
					LiquidacionComisionVenta liquidacion = new LiquidacionComisionVenta();
					liquidacion.setEmpresa(this.empresa);
					liquidacion.setDesde(this.desde);
					liquidacion.setHasta(this.hasta);
					liquidacion.setVendedor(vendedor);
					XPersistence.getManager().persist(liquidacion);
					id = liquidacion.getId();
					this.commit();
					addMessage(vendedor.getNombre());
					
					idsLiquidaciones.add(id);
				}
				catch(Exception e){
					this.rollback();
					String error = e.getMessage();
					if (Is.emptyString(error)){
						error = e.toString();
					}
					addError("Error en " + vendedor.getNombre() + "; " + error); 
				}
				
			}
			
			if (!idsLiquidaciones.isEmpty()){
				Map<?, ?>[] values = new Map[idsLiquidaciones.size()];
				int i = 0;
				for(String id: idsLiquidaciones){
					Map<String, Object> value = new HashMap<String,Object>();
					value.put("id", id);
					values[i] = value;
					i++;
				}
				this.getTab().setAllSelectedKeys(values);
			}
		}
		else{
			addError("No hay vendedores que liquiden comisiones");
		}
	}
}
