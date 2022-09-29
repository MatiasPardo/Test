package org.openxava.ventas.calculators;

import static org.openxava.jpa.XPersistence.getManager;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.*;

import org.openxava.calculators.*;
import org.openxava.fisco.model.RegimenFacturacionFiscal;
import org.openxava.negocio.model.*;
import org.openxava.ventas.model.*;

@SuppressWarnings("serial")
public class PuntoVentaDefaultCalculator implements ICalculator{

	public Object calculate() throws Exception {
		Sucursal sucursalDefault = this.getSucursal();
		if (sucursalDefault == null){
			sucursalDefault = Sucursal.sucursalDefault();
		}
		
		boolean exportacion = false;
		if ((this.getCliente() != null) && 
			(this.getCliente().getRegimenFacturacion().getRegimenFacturacion().equals(RegimenFacturacionFiscal.Exportacion))){
			exportacion = true;
		}
				
		StringBuffer sql = new StringBuffer();
		sql.append("from PuntoVenta p where");
		if (exportacion){
			sql.append(" p.tipo in :tipos");						
		}
		else{
			sql.append(" p.principal = :principal");
		}
		if (sucursalDefault != null){
			sql.append(" and p.sucursal.id = :sucursal");
		}
		
	
		Query query = getManager().createQuery(sql.toString());		
		if (exportacion){
			List<TipoPuntoVenta> tipos = new LinkedList<TipoPuntoVenta>();
			tipos.add(TipoPuntoVenta.ExportacionElectronico);
			tipos.add(TipoPuntoVenta.ExportacionManual);
			query.setParameter("tipos", tipos);
		}
		else{
			query.setParameter("principal", Boolean.TRUE);
		}		
		if (sucursalDefault != null){
			query.setParameter("sucursal", sucursalDefault.getId());
		}		
						
		PuntoVenta puntoVentaPpal = null;
		if (exportacion){
			List<?> ptosVta = query.getResultList();
			if (ptosVta.size() == 1){
				puntoVentaPpal = (PuntoVenta)ptosVta.get(0);
			}
			else{
				// se busca el electrónico, si hay uno solo
				for(Object pto: query.getResultList()){
					if (((PuntoVenta)pto).getTipo().equals(TipoPuntoVenta.ExportacionElectronico)){
						if (puntoVentaPpal == null){
							puntoVentaPpal = (PuntoVenta)pto; 
						}
						else{
							// hay más de un electrónico, no se puede determinar un punto principal
							puntoVentaPpal = null;
							break;
						}
					}
				}
			}
		}
		else{
			query.setMaxResults(1);
			try{
				puntoVentaPpal = (PuntoVenta)query.getSingleResult();
			}
			catch(Exception e){
			}
		}
		return puntoVentaPpal;
	}

	private Cliente cliente;
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	private Sucursal sucursal;

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
}
