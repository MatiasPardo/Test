package org.openxava.graficosflowchar.actions;

import java.text.*;

import javax.persistence.FlushModeType;
import javax.persistence.Query;

import org.openxava.base.model.*;
import org.openxava.jpa.XPersistence;
import org.openxava.model.*;
import org.openxava.negocio.model.IGeneradoPor;
import org.openxava.validators.ValidationException;

import com.clouderp.flowchart.actions.*;
import com.clouderp.flowchart.model.*;

public class GraficarTranzabilidadTrAction extends FlowChartCloudBaseAction{

	@Override
	public void drawFlow(FlowCloud flow) {
		if (!this.getView().isKeyEditable()){
			try{
				flow.setName("Trazabilidad");
				Object objeto = MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
				Transaccion transaccion = null;
				if (objeto instanceof IGeneradoPor){
					Query query = XPersistence.getManager().createQuery("from " + ((IGeneradoPor)objeto).generadaPorTipoEntidad() + " where id = :id");
					query.setParameter("id", ((IGeneradoPor)objeto).generadaPorId());
					query.setMaxResults(1);
					query.setFlushMode(FlushModeType.COMMIT);					
					objeto = query.getSingleResult(); 
				}
				
				if (objeto instanceof Transaccion){
					transaccion = (Transaccion)objeto;
				}
				else{
					throw new ValidationException("No se puede graficar la trazabilidad");
				}
				
				for(Trazabilidad trazabilidad: transaccion.getTrazabilidad()){
					NodeCloud nodoOrigen = flow.addNode(trazabilidad.getComprobanteOrigen());
					nodoOrigen.setDescription(this.getDescripcion(trazabilidad.trOrigen(), trazabilidad));
					NodeCloud nodoDestino = flow.addNode(trazabilidad.getComprobanteDestino());
					nodoDestino.setDescription(this.getDescripcion(trazabilidad.trDestino(), trazabilidad));
					flow.addLink(nodoOrigen, nodoDestino);
				}
			}
			catch(Exception e){
				this.addError(e.toString());
			}
		}		
	}
	
	private String getDescripcion(Transaccion tr, Trazabilidad trazabilidad){
		StringBuffer desc = new StringBuffer();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat formatHora = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		desc.append(format.format(tr.getFecha())).append(FINLINEA);
		desc.append(tr.getEstado().toString()).append(" ").append(tr.getUsuario()).append(FINLINEA);
		desc.append(formatHora.format(trazabilidad.getFechaCreacion()));		
		return desc.toString();		
	}

}
