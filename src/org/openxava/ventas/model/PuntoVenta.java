package org.openxava.ventas.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.fisco.model.Regionalidad;
import org.openxava.fisco.model.TipoComprobante;
import org.openxava.jpa.*;
import org.openxava.negocio.calculators.*;
import org.openxava.negocio.model.*;
import org.openxava.negocio.validators.*;
import org.openxava.ventas.calculators.*;

@Entity

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@EntityValidators({
	@EntityValidator(
		value=PrincipalSucursalValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="modelo", value="PuntoVenta"),
			@PropertyValue(name="principal"),
			@PropertyValue(name="sucursal")
		}
	)
	
})

@Views({
	@View(members="codigo, numero;" + 
				"nombre;" +  
				"activo, principal, tipo;" +
				"domicilio;" +
				"sucursal;" +
				"digitosPuntoVenta, digitosNumero;" +
				"numeradores"),
	@View(name="Numerador", members="nombre; numero, tipo;")
})

public class PuntoVenta extends ObjetoEstatico{
	
	private static final int DIGITOSPUNTOVENTA_ANTERIORES = 4;

	private static final int DIGITOSNUMERO_ANTERIORES = 12;
	
	@Required
	private Integer numero;

	@DefaultValueCalculator(FalseCalculator.class)
	private Boolean principal = false;
	
	@DefaultValueCalculator(TipoPuntoVentaDefaultCalculator.class)
	@Required
	private TipoPuntoVenta tipo = TipoPuntoVenta.Electronico;
	
	@OneToMany(mappedBy="puntoVenta", cascade=CascadeType.ALL)
	@ReadOnly
	private Collection<NumeradorPuntoVenta> numeradores;
	
	@DefaultValueCalculator(value=IntegerCalculator.class, properties={@PropertyValue(name="value", value="8")})
	private Integer digitosNumero = 8;
	
	@DefaultValueCalculator(value=IntegerCalculator.class, properties={@PropertyValue(name="value", value="5")})
	private Integer digitosPuntoVenta = 5;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=ObjetoPrincipalCalculator.class, 
				properties={@PropertyValue(name="entidad", value="Sucursal")})
	private Sucursal sucursal;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
    @NoSearch
    @ReferenceView("Simple")
    private Domicilio domicilio;
	
	public TipoPuntoVenta getTipo() {
		return tipo;
	}

	public void setTipo(TipoPuntoVenta tipo) {
		this.tipo = tipo;
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	public Collection<NumeradorPuntoVenta> getNumeradores() {
		return numeradores;
	}

	public void setNumeradores(Collection<NumeradorPuntoVenta> numeradores) {
		this.numeradores = numeradores;
	}

	public void generarNumeradores() {
		// FALTA REGIONALIZAR
		Collection<TipoComprobante> tipos = TipoComprobante.comprobantesPorRegion(Regionalidad.AR);
		for(TipoComprobante tipoComprobante: tipos){
			for(int i=0; i < TipoComprobante.TIPOSTRANSACCIONVENTA.length; i++){
				Integer codigoTipoComprobante = tipoComprobante.codigoFiscal(TipoComprobante.TIPOSTRANSACCIONVENTA[i]);
				if (!this.existeNumerador(codigoTipoComprobante)){
					this.crearNumerador(codigoTipoComprobante);
				}
			}
		}
		
		/*for (int i = 1; i <= 8; i = i + 1) {
			if (!this.existeNumerador(i)){
				crearNumerador(i);
			}
		} 		
		
		for (int i = 11; i<= 13; i = i + 1) {
			if (!this.existeNumerador(i)){
				crearNumerador(i);
			}
		} 		
		
		for(int i = 19; i <= 21; i++){
			if (!this.existeNumerador(i)){
				crearNumerador(i);
			}				
		}*/
	}
	
	private void crearNumerador(int tipoComprobante){
		NumeradorPuntoVenta numerador = new NumeradorPuntoVenta();
		numerador.setProximoNumero(new Long(1));
		numerador.setReservadoPor(new String(""));
		numerador.setPuntoVenta(this);
		numerador.setTipoComprobante(tipoComprobante);
		
		XPersistence.getManager().persist(numerador);
		
		if (this.getNumeradores() == null){
			this.setNumeradores(new LinkedList<NumeradorPuntoVenta>());
		}
		this.getNumeradores().add(numerador);
	}
	
	private boolean existeNumerador(int tipoComprobante){		
		if ((this.getNumeradores() != null) && (!this.getNumeradores().isEmpty())){
			for(NumeradorPuntoVenta numerador: this.getNumeradores()){
				if (numerador.getTipoComprobante() == tipoComprobante){
					return true;
				}
			}
		}
		return false;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Domicilio getDomicilio() {
		return domicilio;
	}

	public void setDomicilio(Domicilio domicilio) {
		this.domicilio = domicilio;
	}

	// A futuro el punto de venta debe tener asociada la empresa
	public Empresa empresaAsociada() {
		return Empresa.buscarEmpresaPorNro(1);
	}
	
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesSoloLectura.add("tipo");
	}
	
	@Override
	public void propiedadesSoloLecturaAlCrear(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion){
		super.propiedadesSoloLecturaAlCrear(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesEditables.add("tipo");
	}

	public Integer getDigitosNumero() {
		return digitosNumero == null ? PuntoVenta.DIGITOSNUMERO_ANTERIORES : this.digitosNumero;
	}

	public void setDigitosNumero(Integer digitosNumero) {
		this.digitosNumero = digitosNumero;
	}

	public Integer getDigitosPuntoVenta() {
		return digitosPuntoVenta == null ? PuntoVenta.DIGITOSPUNTOVENTA_ANTERIORES : this.digitosPuntoVenta;
	}

	public void setDigitosPuntoVenta(Integer digitosPuntoVenta) {
		this.digitosPuntoVenta = digitosPuntoVenta;
	}
}
