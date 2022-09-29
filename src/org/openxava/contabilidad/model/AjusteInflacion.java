package org.openxava.contabilidad.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Query;

import org.openxava.base.model.Empresa;
import org.openxava.base.model.Esquema;
import org.openxava.base.model.Estado;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.model.Sucursal;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

public class AjusteInflacion {
	
	private Empresa empresa;
	
	private Sucursal sucursal;
	
	private EjercicioContable ejercicio;
	
	public Asiento generarAjusteInflacion(){
		Asiento asiento = this.existeAsientoPorInflacion();
		if (asiento == null){
			CuentaContable cuentaAjusteInflacion = buscarCuentaAjustePorInflacion();
			this.validarIndicesInflacion();
			PeriodoContable ultimoPeriodo = this.getEjercicio().ultimoPeriodo();
			this.validarUltimoPeriodo(ultimoPeriodo);
						
			Query query = queryCalculoAjusteInflacion();
			asiento = this.crearAsientoAjusteInflacion(ultimoPeriodo.getHasta());
			
			List<?> result = query.getResultList();
			CuentaContable cuenta = null;
			List<ItemAsiento> items = new LinkedList<ItemAsiento>();
			for(Object res: result){
				String idCuenta = (String)((Object[])res)[0];
				if (cuenta == null){
					cuenta = XPersistence.getManager().find(CuentaContable.class, idCuenta);
				}
				else if (!Is.equalAsString(cuenta.getId(), idCuenta)){
					cuenta = XPersistence.getManager().find(CuentaContable.class, idCuenta);
				}
				BigDecimal saldo = (BigDecimal)((Object[])res)[1];
				BigDecimal indice = (BigDecimal)((Object[])res)[2];
				
				if (indice.compareTo(ultimoPeriodo.getIndiceInflacion()) != 0){
					BigDecimal coeficienteAjuste = (ultimoPeriodo.getIndiceInflacion().divide(indice, 4, RoundingMode.HALF_EVEN)).subtract(new BigDecimal(1));
					BigDecimal saldoPorInflacion = saldo.multiply(coeficienteAjuste).setScale(2, RoundingMode.HALF_EVEN);
					
					ItemAsiento item = new ItemAsiento();
					item.setAsiento(asiento);
					item.setCuenta(cuenta);				
					// nombre del periodo
					item.setDetalle((String)((Object[])res)[3]);
					
					ItemAsiento itemAjusteInflacion = new ItemAsiento();
					itemAjusteInflacion.setAsiento(asiento);
					itemAjusteInflacion.setCuenta(cuentaAjusteInflacion);
					itemAjusteInflacion.setDetalle((String)((Object[])res)[3]);
															
					if (saldoPorInflacion.compareTo(BigDecimal.ZERO) > 0){
						item.setDebe(saldoPorInflacion);
						itemAjusteInflacion.setHaber(saldoPorInflacion);
					}
					else{
						itemAjusteInflacion.setDebe(saldoPorInflacion.negate());
						item.setHaber(saldoPorInflacion.negate());
					}					
					items.add(item);
					items.add(itemAjusteInflacion);					
				}
				
				// se persisten los items
				for(ItemAsiento itemAsiento: items){
					XPersistence.getManager().persist(itemAsiento);
				}
				asiento.setItems(items);
				asiento.grabarTransaccion();
			}
		}
		else{
			throw new ValidationException("Ya existe un asiento de ajuste por inflación");
		}
		
		return asiento;
	}
	
	private Asiento crearAsientoAjusteInflacion(Date fecha) {
		Asiento nuevo = Asiento.crearAsiento(this.getEmpresa(), this.getSucursal(), fecha, "Ajuste por Inflación");
		nuevo.setClasificacionAsiento(TipoComportamientoAsiento.AjusteInflacion);
		nuevo.setEjercicio(this.getEjercicio());
		XPersistence.getManager().persist(nuevo);
		return nuevo;
	}

	public Asiento existeAsientoPorInflacion(){
		Query query = XPersistence.getManager().createQuery("from Asiento where clasificacionAsiento = :inflacion and ejercicio.id = :ejercicio " +
									"and empresa.id = :empresa and sucursal.id = :sucursal and estado != :anulado");
		query.setParameter("inflacion", TipoComportamientoAsiento.AjusteInflacion);
		query.setParameter("empresa", this.getEmpresa().getId());
		query.setParameter("sucursal", this.getSucursal().getId());
		query.setParameter("ejercicio", this.getEjercicio().getId());
		query.setParameter("anulado", Estado.Anulada);
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			return (Asiento)result.get(0);
		}
		else{
			return null;
		}
	}
	
	private Query queryCalculoAjusteInflacion(){
		StringBuffer sql = new StringBuffer();
		sql.append("select cc.id, (saldos.debe - saldos.haber) as saldo, p.indiceInflacion, p.nombre, cc.codigo, p.desde ");
		sql.append("from ").append(Esquema.concatenarEsquema("cuentacontable cc ")).append("join (");
			sql.append("select i.cuenta_id as cuenta_id, p.id as periodo_id, sum(i.debe) as debe, sum(i.haber) as haber ");
			sql.append("from ").append(Esquema.concatenarEsquema("periodocontable p ")); 
			sql.append("left join ").append(Esquema.concatenarEsquema("asiento a on a.periodo_id = p.id and a.empresa_id = :empresa and a.sucursal_id = :sucursal and a.estado = 1 "));			
			sql.append("left join ").append(Esquema.concatenarEsquema("itemasiento i ")).append("on i.asiento_id = a.id "); 
			sql.append("group by i.cuenta_id, p.id) saldos on saldos.cuenta_id = cc.id ");
		sql.append("join ").append(Esquema.concatenarEsquema("periodocontable p ")).append("on p.id = saldos.periodo_id ");
		sql.append("where cc.inflacion = 1 and p.ejercicio_id = :ejercicio ");
		sql.append("order by cc.codigo asc, p.desde asc");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("ejercicio", this.getEjercicio().getId());
		query.setParameter("empresa", this.getEmpresa().getId());
		query.setParameter("sucursal", this.getSucursal().getId());
		return query;
	}
	
	private CuentaContable buscarCuentaAjustePorInflacion(){
		Query query = XPersistence.getManager().createQuery("from CuentaContable where inflacion = :inflacion");
		query.setParameter("inflacion", TipoCuentaInflacion.cuentaInflacion);
		query.setMaxResults(2);
		try{
			return (CuentaContable)query.getSingleResult();
		}
		catch(Exception e){
			throw new ValidationException("Debe estar definida una única cuenta contable como ajuste por inflación");
		}
	}
	
	private void validarIndicesInflacion(){
		Query query = XPersistence.getManager().createQuery("from PeriodoContable where ejercicio.id = :ejercicio and (indiceInflacion is null or indiceInflacion = 0)");
		query.setParameter("ejercicio", this.getEjercicio().getId());
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			throw new ValidationException("Deben estar definidos los índices de inflación de todos los periodos");
		}
	}

	private void validarUltimoPeriodo(PeriodoContable ultimo){
		if (ultimo.permiteAsientos()){
			if (ultimo.getIndiceInflacion().compareTo(BigDecimal.ZERO) == 0){
				throw new ValidationException("El índice de inflación del periodo " + ultimo.toString() + " es cero");
			}
		}
		else{
			throw new ValidationException("El periodo " + ultimo.toString() + " esta " + ultimo.getEstado().toString());
		}
	}
	
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public EjercicioContable getEjercicio() {
		return ejercicio;
	}

	public void setEjercicio(EjercicioContable ejercicio) {
		this.ejercicio = ejercicio;
	}		
}
