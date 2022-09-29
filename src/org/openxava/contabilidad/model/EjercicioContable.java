package org.openxava.contabilidad.model;

import java.text.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;


/**
 * @author Sergio
 *
 */
@Entity

@Views({
	@View(members="Principal[codigo, activo;" +
			"nombre;" +
			"desde, hasta];" +
			"Reportes[fechaDesde, fechaHasta];" +
			"periodos;" + 
			"cuentasContables;"),
	@View(name="Simple",
		members="codigo, nombre"),
	@View(name="Afip",
		members="codigo, activo; nombre;" + 
				"desde, hasta;"),
	@View(name="AjusteInflacion",
		members="codigo, activo, desde, hasta;" +  
			"Indices{periodos}, Cuentas{cuentasContables}"),
})

@Tabs({
	@Tab(properties="codigo, nombre, activo, desde, hasta, usuario, fechaCreacion", 
			defaultOrder="${desde} desc")	
})


public class EjercicioContable extends ObjetoEstatico implements IParametrosReporteContable{
	
	public static PeriodoContable buscarPeriodo(Date fecha){
	
		Query query = (Query)org.openxava.jpa.XPersistence.getManager().createQuery("from PeriodoContable p where " +  		 
				"p.desde <= :desde AND " + 
				"p.hasta >= :hasta");
		query.setParameter("desde", fecha);
		query.setParameter("hasta", fecha);
		query.setFlushMode(FlushModeType.COMMIT);
		try{
	    	PeriodoContable periodo = (PeriodoContable)query.getSingleResult();
	    	return periodo;
	    }
	    catch(javax.persistence.NoResultException noResultEx){
	    	return null;
	    }
	}
	
	
	@Required
	@DefaultValueCalculator(FechaInicioMesCalculator.class)
	private Date desde;
	
	@Required
	private Date hasta;

	@OneToMany(mappedBy="ejercicio", cascade=CascadeType.ALL)
	@ListsProperties({
		@ListProperties(value="codigo, nombre, activo, desde, hasta, estado, usuario, fechaCreacion", 
						notForViews="AjusteInflacion"),
		@ListProperties(value="codigo, nombre, indiceInflacion, desde, hasta, estado", 
			forViews="AjusteInflacion"),
	})	
	@NewAction(value="ColeccionPeriodosContables.new")
	@EditAction(value="ColeccionPeriodosContables.edit")
	private Collection<PeriodoContable> periodos = new ArrayList<PeriodoContable>();
	
	@Condition("${activo} = 't'")
	@ReadOnly
	@ListProperties("codigo, nombre, inflacion")	
	public Collection<CuentaContable> getCuentasContables(){
		return null;
	}
	
	public Date getDesde() {
		return desde;
	}

	public void setDesde(Date desde) {
		this.desde = desde;
	}

	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}

	public Collection<PeriodoContable> getPeriodos() {
		return periodos;
	}

	public void setPeriodos(Collection<PeriodoContable> periodos) {
		this.periodos = periodos;
	}
	
	public void generarPeriodos(){
		if (this.getPeriodos() != null){
			if (this.getPeriodos().isEmpty()){
				Date desde = this.getDesde();
				SimpleDateFormat formatoMes = new SimpleDateFormat("MM");
				SimpleDateFormat formatoAnioMes = new SimpleDateFormat("yyyy/MM");
				while (desde.compareTo(this.getHasta()) < 0){
					PeriodoContable periodo = new PeriodoContable();
					periodo.setEjercicio(this);
					this.getPeriodos().add(periodo);
					periodo.setDesde(desde);
					Calendar cal = Calendar.getInstance();
					cal.setTime(desde);
					cal.add(Calendar.MONTH, 1);
					desde = cal.getTime();
					cal.add(Calendar.DAY_OF_MONTH, -1);
					periodo.setHasta(cal.getTime());
									
					periodo.setCodigo(formatoMes.format(periodo.getDesde()));
					periodo.setNombre(formatoAnioMes.format(periodo.getDesde()));
					periodo.setEstado(EstadoPeriodoContable.Borrador);
					
					XPersistence.getManager().persist(periodo);
				}
			}
		}
	}

	public void consolidarAsientos() {
		
		List<Empresa> empresas = new LinkedList<Empresa>();
		this.empresas(empresas);
		for(Empresa empresa: empresas){
			Numerador numerador = ConfiguracionEntidad.buscarNumeradorPorEmpresaParaActualizar(AsientoConsolidado.class.getSimpleName(), empresa);
			if (numerador == null){
				throw new ValidationException("Falta definir el numerador para empresa " + empresa.getNombre() + " en la entidad AsientoConsolidado");
			}
		}
		
		this.copiarAsientosManualesAlConsolidado();
		this.copiarItemsAsientosManualesAlConsolidado();
		this.generarAsientosConsolidados();
		this.generarItemsAsientosConsolidados();
	}	
	

	public void renumerarAsientosConsolidados() {
		List<Empresa> empresas = new LinkedList<Empresa>();
		this.empresas(empresas);
		for(Empresa empresa: empresas){
			Numerador numerador = ConfiguracionEntidad.buscarNumeradorPorEmpresaParaActualizar(AsientoConsolidado.class.getSimpleName(), empresa);
			Long ultimoNumero = this.primerNumeroAsientoConsolidado(empresa);
			numerador.setProximoNumero(ultimoNumero + 1);
			
			Query query = queryAsientosConsolidadosOrdenados(empresa);
			List<?> results = query.getResultList();
			for(Object res: results){
				AsientoConsolidado asiento = (AsientoConsolidado)res;
				numerador.numerarObjetoNegocio(asiento);
				asiento.actualizarDetalle();
			}
		}
	}
	
	public void empresas(List<Empresa> empresas){
		Empresa.buscarObjetosEmpresasHabilitadas(empresas);
		empresas.sort(new Comparator<Empresa>(){
			@Override
			public int compare(Empresa arg0, Empresa arg1) {
				return arg0.getCodigo().compareTo(arg1.getCodigo());
			}
			
		});
	}
	
	private Query queryAsientosConsolidadosOrdenados(Empresa empresa){
		StringBuffer sql = new StringBuffer();
		sql.append("from AsientoConsolidado ");
		sql.append("where periodo.ejercicio.id = :ejercicio and empresa.id = :empresa ");
		sql.append("order by fecha asc, tipotransaccion asc, sucursal.codigo asc");
		
		Query query = XPersistence.getManager().createQuery(sql.toString());
		query.setParameter("empresa", empresa.getId());
		query.setParameter("ejercicio", this.getId());		
		return query;
	}
	
	private Long primerNumeroAsientoConsolidado(Empresa empresa){
		StringBuffer sql = new StringBuffer();
		sql.append("select a.numerointerno "); 
		sql.append("from ").append(Esquema.concatenarEsquema("AsientoConsolidado")).append(" a ");
		sql.append("where a.numerointerno is not null ");
		sql.append("and a.empresa_id = :empresa ");
		sql.append("and a.fecha < :fecha ");
		sql.append("order by numerointerno desc ");
		sql.append("limit 1 ");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("empresa", empresa.getId());
		query.setParameter("fecha", this.getDesde());
				
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			return (Long)((Object[])result.get(0))[0];
		}
		else{
			return new Long(0);
		}
	}
		
	private void copiarAsientosManualesAlConsolidado(){
		StringBuffer sql = new StringBuffer();
		
		sql.append("insert into ").append(Esquema.concatenarEsquema("asientoconsolidado"));
		sql.append(" select ").append(this.columnsInsertSQLAsientoConsolidado(true));
		sql.append(" from ").append(Esquema.concatenarEsquema("asiento a "));
		sql.append("join ").append(Esquema.concatenarEsquema("periodocontable p ")).append("on p.id = a.periodo_id ");
		sql.append("left join ").append(Esquema.concatenarEsquema("asientoconsolidado ac ")).append("on ac.id = a.id ");
		sql.append("where a.estado = 1 and (a.tipotransaccion is null or a.tipotransaccion = '') ");
		sql.append("and p.estado = 2 and p.ejercicio_id = :ejercicio and ac.id is null");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("ejercicio", this.getId());
		query.executeUpdate();
	}
		
	private void generarAsientosConsolidados(){
		StringBuffer sql = new StringBuffer();
		
		sql.append("insert into ").append(Esquema.concatenarEsquema("asientoconsolidado"));
		sql.append(" select ").append(this.columnsInsertSQLAsientoConsolidado(false));
		sql.append(" from ").append(Esquema.concatenarEsquema("asiento a "));
		sql.append("join ").append(Esquema.concatenarEsquema("periodocontable p ")).append("on p.id = a.periodo_id "); 
		sql.append("left join ").append(Esquema.concatenarEsquema("asientoconsolidado ac ")).append("on ");
		sql.append("ac.empresa_id = a.empresa_id and "); 
		sql.append("ac.tipoTransaccion = a.tipoTransaccion and "); 
		sql.append("ac.moneda_id = a.moneda_id and "); 
		sql.append("ac.sucursal_id = a.sucursal_id and "); 
		sql.append("ac.periodo_id = a.periodo_id ");
		sql.append("where a.tipotransaccion is not null and a.tipotransaccion != '' and a.ejercicio_id = :ejercicio and a.estado = 1 and ");
		sql.append("ac.id is null and p.estado = 2 ");
		sql.append("group by a.empresa_id, p.desde, a.tipoTransaccion, a.moneda_id, a.sucursal_id, a.periodo_id");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("ejercicio", this.getId());
		query.executeUpdate();
	}

	private String columnsInsertSQLAsientoConsolidado(boolean asientoManuales){
		ArrayList<String> columnas = new ArrayList<String>();
		BaseDatosERP.getColumnasTabla("AsientoConsolidado", Esquema.nombreEsquema(), columnas);
		StringBuffer select = new StringBuffer();
		
		for(String columna: columnas){
			if (select.length() > 0) select.append(", ");
			
			if (this.perteneceAsientoConsolidado(columna)){
				select.append("a.").append(columna);
			}
			else if (this.valoresNulosAsientoConsolidado(columna)){
				select.append("null");
			}
			else if (Is.equalAsStringIgnoreCase(columna, "fechacreacion")){
				select.append(BaseDatosERP.valorDateTime(new Date()));
			}
			else if (Is.equalAsStringIgnoreCase(columna, "usuario")){
				select.append(BaseDatosERP.valorString(Users.getCurrent()));
			}			
			else if (Is.equalAsStringIgnoreCase(columna, "fecha")){
				select.append("p.desde");
			}
			else if (Is.equalAsStringIgnoreCase(columna, "id")){
				if (asientoManuales){
					select.append("a.").append("id");
				}
				else{
					// se genera un id nuevo
					select.append(BaseDatosERP.crearGUID());
				}
			}
			else if (Is.equalAsStringIgnoreCase(columna, "detalle")){
				if (asientoManuales){
					select.append("a.").append("detalle");
				}
				else{
					// se pone el tipo de transacción
					select.append("a.tipoTransaccion");
				}
			}
			else if (Is.equalAsStringIgnoreCase(columna, "tipotransaccion")){
				if (asientoManuales){
					select.append("null");
				}
				else{
					select.append("a.tipoTransaccion");
				}
			}
			else{
				throw new ValidationException(columna + " no esta definida");
			}
		}		
		return select.toString();
	}
	
	private boolean perteneceAsientoConsolidado(String columna){
		if (Is.equalAsStringIgnoreCase(columna, "empresa_id") ||			 
			Is.equalAsStringIgnoreCase(columna, "sucursal_id") ||
			Is.equalAsStringIgnoreCase(columna, "periodo_id") ||
			Is.equalAsStringIgnoreCase(columna, "moneda_id")){
			return true;
		}
		else{
			return false;
		}
	}
	
	private boolean valoresNulosAsientoConsolidado(String columna){
		if (Is.equalAsStringIgnoreCase(columna, "numero") ||
			Is.equalAsStringIgnoreCase(columna, "numerointerno")){
			return true;
		}
		else{
			return false;
		}
		
	}
	
	private void copiarItemsAsientosManualesAlConsolidado(){
		StringBuffer sql = new StringBuffer();
		
		sql.append("insert into ").append(Esquema.concatenarEsquema("itemasientoconsolidado"));
		sql.append(" select ").append(this.columnsInsertSQLItemsAsientoConsolidado(true));
		sql.append(" from ").append(Esquema.concatenarEsquema("itemasiento i "));
		sql.append("join ").append(Esquema.concatenarEsquema("asientoconsolidado ac ")).append("on ac.id = i.asiento_id ");
		sql.append("join ").append(Esquema.concatenarEsquema("periodocontable p ")).append("on p.id = ac.periodo_id ");
		sql.append("left join ").append(Esquema.concatenarEsquema("ItemAsientoConsolidado iac ")).append(" on iac.id = i.id ");
		sql.append("where iac.id is null and (ac.tipotransaccion is null or ac.tipotransaccion = '') "); 
		sql.append("and p.ejercicio_id = :ejercicio"); 
	
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("ejercicio", this.getId());
		query.executeUpdate();
	}
		
	private void generarItemsAsientosConsolidados(){
		StringBuffer sql = new StringBuffer();
		
		sql.append("insert into ").append(Esquema.concatenarEsquema("itemasientoconsolidado"));
		sql.append(" select ").append(this.columnsInsertSQLItemsAsientoConsolidado(false));
		sql.append(" from ").append(Esquema.concatenarEsquema("itemasiento i "));
		sql.append("join ").append(Esquema.concatenarEsquema("asiento a ")).append("on a.id = i.asiento_id ");
		sql.append("join ").append(Esquema.concatenarEsquema("asientoconsolidado ac ")).append("on ");
		sql.append("ac.empresa_id = a.empresa_id and "); 
		sql.append("ac.tipoTransaccion = a.tipoTransaccion and "); 
		sql.append("ac.moneda_id = a.moneda_id and "); 
		sql.append("ac.sucursal_id = a.sucursal_id and "); 
		sql.append("ac.periodo_id = a.periodo_id ");
		sql.append("left join ").append(Esquema.concatenarEsquema("ItemAsientoConsolidado iac ")).append("on iac.asiento_id = ac.id ");
		sql.append("where a.tipotransaccion is not null and a.tipotransaccion != '' and a.ejercicio_id = :ejercicio "); 
		sql.append("and iac.id is null "); 
		sql.append("group by ac.id, i.cuenta_id");
	
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("ejercicio", this.getId());
		query.executeUpdate();
	}
	
	private String columnsInsertSQLItemsAsientoConsolidado(boolean manual){
		ArrayList<String> columnas = new ArrayList<String>();
		BaseDatosERP.getColumnasTabla("ItemAsientoConsolidado", Esquema.nombreEsquema(), columnas);
		StringBuffer select = new StringBuffer();
		
		for(String columna: columnas){
			if (select.length() > 0) select.append(", ");
			
			if (Is.equalAsStringIgnoreCase(columna, "id")){
				if (manual){
					select.append("i.id");
				}
				else{
					select.append(BaseDatosERP.crearGUID());
				}
			}
			else if (Is.equalAsStringIgnoreCase(columna, "debe") || Is.equalAsStringIgnoreCase(columna, "haber")){
				if (manual){
					select.append("i.").append(columna);
				}
				else{
					select.append("sum(i.").append(columna).append(")");
				}
			}
			else if (perteneceItemAsientoConsolidado(columna)){
				select.append("i.").append(columna);
			}
			else if (Is.equalAsStringIgnoreCase(columna, "asiento_id")){
				select.append("ac.id");
			}
			else{
				throw new ValidationException(columna + " no esta definida");
			}
		}
		return select.toString();
	}
	
	private boolean perteneceItemAsientoConsolidado(String columna){
		if(Is.equalAsStringIgnoreCase(columna, "cuenta_id")){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public EjercicioContable ejercicio() {
		return this;
	}

	public void enumerarLibroDiario(Integer primerNumero, Empresa empresa) {
		StringBuffer sql = new StringBuffer();
		sql.append("select id from ").append(Esquema.concatenarEsquema("Asiento a "));
		sql.append("where a.estado = 1 and empresa_id = :empresa ");
		sql.append("order by a.fecha asc, a.fechaCreacion asc, a.numeroInterno asc");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("empresa", empresa.getId());
		List<?> results = query.getResultList();
		Integer numero = primerNumero;
		for(Object res: results){
			String id = (String)res;
			String update = "update " + Esquema.concatenarEsquema("Asiento") + " set librodiario = " + BaseDatosERP.valorInteger(numero) + " where id = :id";
			Query queryUpdate = XPersistence.getManager().createNativeQuery(update);
			queryUpdate.setParameter("id", id);
			queryUpdate.executeUpdate();
			numero++;
		}
		XPersistence.commit();
	}
	
	public PeriodoContable buscarPeriodo(String codigo) {
		Query query = XPersistence.getManager().createQuery("from PeriodoContable where codigo = :codigo and ejercicio.id = :ejercicio");
		query.setParameter("codigo", codigo);
		query.setParameter("ejercicio", this.getId());
		try{
			return (PeriodoContable)query.getSingleResult();
		}
		catch(Exception e){
			throw new ValidationException("No se pudo encontrar o hay más de un periodo contable con código " + codigo);
		}
	}
	
	public PeriodoContable ultimoPeriodo(){
		Query query = XPersistence.getManager().createQuery("from PeriodoContable where ejercicio.id = :ejercicio order by hasta desc");
		query.setParameter("ejercicio", this.getId());
		query.setMaxResults(1);
		return (PeriodoContable)query.getSingleResult();
	}

	public void periodosOrdenados(List<PeriodoContable> periodos) {
		periodos.addAll(this.getPeriodos());				
		Collections.sort(periodos, new Comparator<PeriodoContable>(){
			public int compare(PeriodoContable periodo1, PeriodoContable periodo2){
				return periodo1.getDesde().compareTo(periodo2.getDesde());
			}
		});
		
	}
	
	// Fechas para que el usuario cambie para los reportes
	@Transient
	@Hidden
	private Date fechaDesde;

	@Transient
	@Hidden
	private Date fechaHasta;
	
	public Date getFechaDesde() {
		return this.getDesde();
	}

	public void setFechaDesde(Date fechaDesde) {
		this.fechaDesde = fechaDesde;
	}

	public Date getFechaHasta() {
		return this.getHasta();
	}

	public void setFechaHasta(Date fechaHasta) {
		this.fechaHasta = fechaHasta;
	}
		
	
	public void validarRangoFechas(Date fechaDesde, Date fechaHasta){
		if (fechaDesde.compareTo(fechaHasta) > 0){
			throw new ValidationException(UtilERP.convertirString(fechaDesde) + " debe ser anterior a " + UtilERP.convertirString(fechaHasta));
		}
		if (fechaDesde.compareTo(this.getDesde()) < 0){
			throw new ValidationException(UtilERP.convertirString(fechaDesde) + " no puede ser anterior a " + UtilERP.convertirString(this.getDesde()));			
		}
		if (fechaDesde.compareTo(this.getHasta()) > 0){
			throw new ValidationException(UtilERP.convertirString(fechaDesde) + " no puede ser posterior a " + UtilERP.convertirString(this.getHasta()));			
		}
		if (fechaHasta.compareTo(this.getDesde()) < 0){
			throw new ValidationException(UtilERP.convertirString(fechaHasta) + " no puede ser anterior a " + UtilERP.convertirString(this.getDesde()));			
		}
		if (fechaHasta.compareTo(this.getHasta()) > 0){
			throw new ValidationException(UtilERP.convertirString(fechaHasta) + " no puede ser posterior a " + UtilERP.convertirString(this.getHasta()));			
		}
	}

}
