package org.openxava.ventas.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.*;
import org.openxava.ventas.model.*;

public class ParametrosGenerarComisionesVentaAction extends TabBaseAction{

	@Override
	public void execute() throws Exception {
		this.showDialog();
		getView().setTitle("Comisiones");
		getView().setModelName(ParametrosLiquidacionComisionesVenta.class.getSimpleName());
		this.addActions("LiquidacionComisionVenta.Calcular", "Dialog.cancel");
		
		String entidad = "LiquidacionComisionVenta";
		ConfiguracionEntidad conf = ConfiguracionEntidad.buscarConfigurador(entidad);
		if (conf != null){
			this.getView().setValue("empresa.id", conf.getEmpresa().getId());
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.MONTH, -1);
			Date mesAnterior = calendar.getTime();
			
			FechaInicioMesCalculator inicioMes = new FechaInicioMesCalculator();			
			inicioMes.setFecha(mesAnterior);
			FechaFinMesCalculator finMes = new FechaFinMesCalculator();
			finMes.setFecha(mesAnterior);
			try{
				this.getView().setValue("desde", inicioMes.calculate());
				this.getView().setValue("hasta", finMes.calculate());
			}
			catch(Exception e){				
			}
		}
		else{
			addError("No esta definido el configurador de la entidad " + entidad);
		}
	}

}
