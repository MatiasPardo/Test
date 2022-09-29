package org.openxava.graficosflowchar.actions;

import java.util.*;

import javax.persistence.*;

import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;

import com.clouderp.flowchart.actions.*;
import com.clouderp.flowchart.model.*;
import com.clouderp.web.permanentlinks.PermanentLinks;

public class GraficarCircuitosAction extends FlowChartCloudBaseAction{

	@Override
	public void drawFlow(FlowCloud flow) {
		flow.setName("Circuitos");
		
		Query query = XPersistence.getManager().createQuery("from ConfiguracionCircuito");
		List<?> result = query.getResultList();
		for(Object res: result){
			ConfiguracionCircuito circuito = (ConfiguracionCircuito)res;
			
			NodeCloud nodoOrigen = flow.addNode(this.nombreEntidad(circuito.getOrigen()));
			nodoOrigen.setDescription(this.descripcionEntidad(circuito.getOrigen()));
			nodoOrigen.assignHyperLink("Listado", PermanentLinks.relativeUrlModeList(circuito.getOrigen().getEntidad()));
			
			NodeCloud nodoDestino = flow.addNode(this.nombreEntidad(circuito.getDestino()));
			nodoDestino.setDescription(this.descripcionEntidad(circuito.getDestino()));
			nodoDestino.assignHyperLink("Listado", PermanentLinks.relativeUrlModeList(circuito.getDestino().getEntidad()));
			
			flow.addLink(nodoOrigen, nodoDestino);
		}
	}

	private String nombreEntidad(ConfiguracionEntidad entidad){
		return Labels.get(entidad.getEntidad());
	}
	
	private String descripcionEntidad(ConfiguracionEntidad entidad){
		StringBuffer desc = new StringBuffer();
		desc.append(entidad.getMoneda().getNombre()).append(FINLINEA);
		if (entidad.getActivarEnvioMail()){
			desc.append("Envio email").append(FINLINEA);
		}
		if (entidad.getMonedaSoloLectura()){
			desc.append("Moneda solo lectura").append(FINLINEA);
		}
		if (entidad.getCotizacionSoloLectura()){
			desc.append("Cotizacion solo lectura").append(FINLINEA);
		}
		return desc.toString();
	}

}
