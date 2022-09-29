package org.openxava.negocio.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.actions.*;
import org.openxava.negocio.calculators.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@Entity

@Views({
	@View(members="origen;"
			+ "destino;"
			+ "cotizacion, cotizacionAnterior;"
			+ "descripcion;"
			+ "fechaCreacion, usuario")
})

@Tabs({
	@Tab(properties="fecha, origen.nombre, cotizacion, destino.nombre, fechaCreacion",
		defaultOrder="${fechaCreacion} desc")
})

public class Cotizacion extends ObjetoNegocio{
	
	public static BigDecimal buscarCotizacion(Moneda origen, Moneda destino, Date dia){
		BigDecimal cotizacionImporte = BigDecimal.ZERO;
		if ((origen != null) && (destino != null) && (dia != null)){
			String sql = "from Cotizacion c where " +
					"((c.origen = :origen and c.destino = :destino) or " + 
					"(c.destino = :origen and c.origen = :destino)) and " +
					"c.fecha <= :fecha order by c.fechaCreacion desc";
			Query query = XPersistence.getManager().createQuery(sql);
			query.setParameter("origen", origen);
			query.setParameter("destino", destino);
			query.setParameter("fecha", dia);
			query.setMaxResults(1);
			query.setFlushMode(FlushModeType.COMMIT);
			List<?> resultados= (List<?>)query.getResultList();
			if (!resultados.isEmpty()){
				Cotizacion cotizacion = (Cotizacion)resultados.get(0);
				if (cotizacion.getOrigen().equals(origen)){
					return cotizacion.getCotizacion();
				}
				else{
					// se usan muchos decimales para no perder presición, luego es responsabilidad del que calcula redondear.
					return (new BigDecimal(1)).divide(cotizacion.getCotizacion(), 16, RoundingMode.HALF_EVEN);
				}
			}
			else{
				throw new ValidationException("No se encontró cotización para " + origen.getNombre() + " y " + destino.getNombre());
			}
		}
		return cotizacionImporte;
	}
	
	public static BigDecimal buscarCotizacionAnterior(Moneda origen, Moneda destino, Date diaYHora){
		BigDecimal cotizacionImporte = BigDecimal.ZERO;
		if ((origen != null) && (destino != null) && (diaYHora != null)){
			String sql = "from Cotizacion c where " +
					"((c.origen = :origen and c.destino = :destino) or " + 
					"(c.destino = :origen and c.origen = :destino)) and " +
					"c.fechaCreacion < :fecha order by c.fechaCreacion desc";
			Query query = XPersistence.getManager().createQuery(sql);
			query.setParameter("origen", origen);
			query.setParameter("destino", destino);
			query.setParameter("fecha", diaYHora);
			query.setMaxResults(1);
			Cotizacion cotizacion = null;
			try{
				cotizacion = (Cotizacion)query.getSingleResult();
			}
			catch(NoResultException e){
				
			}
			if (cotizacion != null){
				if (cotizacion.getOrigen().equals(origen)){
					return cotizacion.getCotizacion();
				}
				else{
					return (new BigDecimal(1)).divide(cotizacion.getCotizacion());
				}
			}
		}
		return cotizacionImporte;
	}
	
	@DefaultValueCalculator(Moneda2Calculator.class)
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Moneda origen;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Moneda destino;
	
	@Depends(value="origen.id, destino.id")
	public BigDecimal getCotizacionAnterior(){
		Date fecha = this.getFechaCreacion();
		if (fecha == null){
			fecha = new Date();
		}
		return Cotizacion.buscarCotizacionAnterior(this.getOrigen(), this.getDestino(), fecha);
	}
	
	@Depends(value="origen.id, destino.id, cotizacion")
	public String getDescripcion(){
		String desc = "";
		if ((this.getOrigen() != null) && (this.getDestino() != null)){
			desc = this.getOrigen().getSimbolo() + " 1 cotiza a " + this.getDestino().getSimbolo() + " " + this.getCotizacion().toString(); 
		}
		return desc;
	}
	
	@Digits(integer=16, fraction=2)
	@PropertyValidator(value=NotZeroValidator.class)
	@OnChange(OnChangeCotizacion.class)
	private BigDecimal cotizacion;
	
	public BigDecimal getCotizacion() {
		return cotizacion == null ? BigDecimal.ZERO : this.cotizacion;
	}

	public void setCotizacion(BigDecimal cotizacion) {
		this.cotizacion = cotizacion;
	}

	@ReadOnly
	private Date fecha;
	
	public Moneda getOrigen() {
		return origen;
	}

	public void setOrigen(Moneda origen) {
		this.origen = origen;
	}

	public Moneda getDestino() {
		return destino;
	}

	public void setDestino(Moneda destino) {
		this.destino = destino;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	@Override
	public Boolean soloLectura(){
		Boolean soloLectura = super.soloLectura();
		if (!soloLectura){
			if (!Is.emptyString(this.getId())){
				soloLectura = Boolean.TRUE;
			}
		}
		return soloLectura;
	}
	
	@PrePersist
	protected void onPrePersist() {
		super.onPrePersist();
		// se asigna la fecha actual sin horas, minutos ni segundos
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.clear(Calendar.MILLISECOND);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MINUTE);
		cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
		
		this.setFecha(cal.getTime());
	}
	
	@Override
	public void onPreDelete(){
		super.onPreDelete();
		throw new ValidationException("Debe cargar una cotización nueva para reemplazar la que desea borrar");
	}
}
