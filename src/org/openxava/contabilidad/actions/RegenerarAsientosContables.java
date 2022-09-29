package org.openxava.contabilidad.actions;

import java.util.List;

import javax.persistence.Query;

import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.openxava.actions.ViewBaseAction;
import org.openxava.base.model.ConfiguracionEntidad;
import org.openxava.base.model.ErroresERP;
import org.openxava.base.model.Esquema;
import org.openxava.base.model.Estado;
import org.openxava.contabilidad.model.Asiento;
import org.openxava.contabilidad.model.ITransaccionContable;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;

import org.openxava.validators.ValidationException;


public class RegenerarAsientosContables extends ViewBaseAction{

	private Integer asientosErrores = 0;
	
	private Integer asientosGenerados = 0;
	
	@Override
	public void execute() throws Exception {
		if (this.getView().isKeyEditable()){
			this.addError("primero_grabar");
		}
		else{
			StringBuilder sql = new StringBuilder();
			Class<?> clase = ConfiguracionEntidad.buscarClase(this.getView().getValueString("entidad"));
			
			String tipoEntidad = this.getView().getValueString("entidad");
			String nombreTabla = null;
			
			SessionFactory sessionFactory = XPersistence.getManager().getEntityManagerFactory().unwrap(SessionFactory.class);
			ClassMetadata hibernateMetadata = sessionFactory.getClassMetadata(clase);
			if (hibernateMetadata == null){
				throw new ValidationException("No se pudo obtener el nombre de la tabla para la entidad " + tipoEntidad);
			}
			else if (hibernateMetadata instanceof AbstractEntityPersister){
				AbstractEntityPersister persister = (AbstractEntityPersister) hibernateMetadata;
				nombreTabla = persister.getTableName();
			}
			else{
				nombreTabla = Esquema.concatenarEsquema(tipoEntidad);
			}
						
			sql.append("select t.id from ").append(nombreTabla).append(" t ");
			sql.append("left join ").append(Esquema.concatenarEsquema("Asiento a")).append(" on a.idTransaccion = t.id ");
			sql.append("where t.estado = :estado and a.idTransaccion is null ");
			if (!Is.equalAsStringIgnoreCase(Esquema.concatenarEsquema(tipoEntidad), nombreTabla)){
				sql.append(" and dtype = :type");
			}
			
			Query query = XPersistence.getManager().createNativeQuery(sql.toString());
			query.setParameter("estado", Estado.Confirmada.ordinal());
			if (!Is.equalAsStringIgnoreCase(Esquema.concatenarEsquema(tipoEntidad), nombreTabla)){
				query.setParameter("type", tipoEntidad);
			}
			List<?> results = query.getResultList();
			this.asientosGenerados = 0;
			this.asientosErrores = 0;
			for(Object result: results){
				this.regenerarAsiento(result.toString(), tipoEntidad);				
			}
			
			if (asientosGenerados > 0){ 
				this.addMessage("Asientos generados correctamente " + asientosGenerados.toString());
			}
			else if (asientosGenerados == 0){
				this.addInfo("No se han generado asientos");
			}
			if (asientosErrores > 0){
				this.addError("Asientos que no se generaron por errores " + asientosErrores.toString());
			}
		}
	}

	private void regenerarAsiento(String id, String tipoEntidad) {
		Query query = XPersistence.getManager().createQuery("from " + tipoEntidad + " where id = :id");
		query.setParameter("id", id);
		List<?> results = query.getResultList();
		if (!results.isEmpty()){
			Object transaccion = results.get(0);	
			if (!ITransaccionContable.class.isAssignableFrom(transaccion.getClass())){
				throw new ValidationException("El tipo de entidad " + tipoEntidad + " no genera asientos contable");
			}
			
			try{
				if (Asiento.crearAsientoContable((ITransaccionContable)transaccion) != null){
					// si es null, significa que esa transacción no genera asiento
					this.asientosGenerados++;
				}
				this.commit();
			}
			catch(Exception e){
				this.asientosErrores++;
				if (this.asientosErrores <= 10){
					ErroresERP.agregarErrores(this.getErrors(), e);
				}
				this.rollback();
			}						
		}
		else{
			this.asientosErrores++;
		}		
	}
}
