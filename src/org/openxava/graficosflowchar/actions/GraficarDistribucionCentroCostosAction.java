package org.openxava.graficosflowchar.actions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.openxava.base.model.UtilERP;
import org.openxava.contabilidad.model.CentroCostos;
import org.openxava.contabilidad.model.DistribucionCentroCosto;
import org.openxava.jpa.XPersistence;
import org.openxava.validators.ValidationException;

import com.clouderp.flowchart.actions.FlowChartCloudBaseAction;
import com.clouderp.flowchart.model.FlowCloud;
import com.clouderp.flowchart.model.LinkCloud;
import com.clouderp.flowchart.model.NodeCloud;

public class GraficarDistribucionCentroCostosAction extends FlowChartCloudBaseAction{

	@Override
	public void drawFlow(FlowCloud flow) {
		flow.setName("Centros de Costos");
		
		Query query = XPersistence.getManager().createQuery("from CentroCostos where activo = :activo");
		query.setParameter("activo", true);
		List<?> result = query.getResultList();
		Map<String, NodeCloud> nodos = new HashMap<String, NodeCloud>();
		Map<String, Object> controlRecursividad = new HashMap<String, Object>();
		for(Object res: result){
			CentroCostos centroCostos = (CentroCostos)res;
			if (!nodos.containsKey(centroCostos.getId())){
				this.graficarDistribucion(centroCostos, flow, nodos, controlRecursividad);				
			}
		}
	}
	
	private NodeCloud graficarDistribucion(CentroCostos centroCostos, FlowCloud flow, Map<String, NodeCloud> nodosGraficados, 
										Map<String, Object> controlRecursividad){		
		
		if (controlRecursividad.containsKey(centroCostos.getId())){
			throw new ValidationException("Error en la estructura: Referencias circulares");
		}
		else{
			controlRecursividad.put(centroCostos.getId(), null);
		}
		
		NodeCloud nodoOrigen = null;
		if (nodosGraficados.containsKey(centroCostos.getId())){
			nodoOrigen = nodosGraficados.get(centroCostos.getId());
		}
		else{
			nodoOrigen = flow.addNode(centroCostos.getCodigo());
			nodoOrigen.setDescription(centroCostos.getNombre());
			nodosGraficados.put(centroCostos.getId(), nodoOrigen);
			if (centroCostos.getDistribuye()){
				for(DistribucionCentroCosto distribucion: centroCostos.getDistribucion()){
					NodeCloud nodoDestino = this.graficarDistribucion(distribucion.getDistribucionCostos(), flow, nodosGraficados, controlRecursividad);
					LinkCloud link = flow.addLink(nodoOrigen, nodoDestino);
					link.setDescription("% " + UtilERP.convertirString(distribucion.getPorcentaje()));
				}
			}
		}
		
		controlRecursividad.remove(centroCostos.getId());
		
		return nodoOrigen;
	}

}
