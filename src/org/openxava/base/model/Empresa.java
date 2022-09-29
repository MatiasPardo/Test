package org.openxava.base.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.impuestos.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

import com.openxava.naviox.model.*;

@Entity

@Views({
	@View(name="Simple", members="codigo, nombre"),
	@View(name="SoloCodigo", members="codigo"), 
	@View(members=
	"Principal{" +
			"codigo, activo, numero;" +
			"nombre, razonSocial;" +
			"mail, telefono, web;" + 
			"cuit, inicioActividad, ingresosBrutos;" +
			"domicilio;" +
			"usuarios;" +
			"}" + 
	
	"Configuracion{" +
			"Impuestos[posicionIva, inscriptoIva, agenteRecaudacion, precioMasIva; percepcion1, percepcion2];" +
			"Moneda[moneda1, moneda2]" + 
			"generaContabilidad;" + 
			"conceptoDiferenciaCambio;" + 
	"}" +			
	"LiquidacionComisiones{comisionesVenta}" + 
	"Consignaciones{consignacionPrecioActual}"
	)
})

@Tabs({
	@Tab(properties="codigo, nombre, activo, razonSocial",
		name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)	
})

public class Empresa extends ObjetoEstatico{
	
	public static final int CANTIDADEMPRESAS = 2;
	
	// Cuidado: máximo pueden ser 10
	public static final int CANTIDADPERCEPCIONESVENTA = 2;
	
	public static Empresa buscarEmpresaPorNro(int numero){
		Query query = XPersistence.getManager().createQuery("from Empresa where numero = :numero");
		query.setParameter("numero", numero);
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		Empresa emp = null;
		if (!result.isEmpty()){
			for(Object empresa: result){
				emp = (Empresa)empresa;
			}
		}
		return emp;		
	}
	
	@Override
	protected void onPrePersist() {
		super.onPrePersist();
	}
	
	@Override
	protected void onPreUpdate() {
		super.onPreUpdate();		
	}
	
	
	@Column(length=50)
	@Required
	@SearchKey
	private String razonSocial;
	
	@Column(length=50)
	@Required
	private String cuit;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
    @NoSearch
    @ReferenceView("Simple")
	@AsEmbedded
    private Domicilio domicilio;
	
	@Column(length=50)
	@Stereotype("EMAIL")
	private String mail;
	
	@Column(length=20) 
	private String telefono;
	
	@Column(length=50)
	@Stereotype("WEBURL")
	private String web;
	
	@Required @DefaultValueCalculator(CurrentDateCalculator.class) 
	private Date inicioActividad;
	
	@Column(length=50)
	private String ingresosBrutos;
	
	@DescriptionsList @NoCreate @NoModify @ReadOnly
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private PosicionAnteImpuesto posicionIva;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	@ReadOnly
	private Boolean inscriptoIva;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean precioMasIva = true;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	@ReadOnly
	private Boolean agenteRecaudacion = Boolean.FALSE;
	
	@ReadOnly
	private DefinicionImpuesto percepcion1;
	
	@ReadOnly
	private DefinicionImpuesto percepcion2;
	
	@ReadOnly
	private int numero;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Moneda moneda1;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Moneda moneda2;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean generaContabilidad;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate 
	@ReferenceView("Simple")
	private Producto conceptoDiferenciaCambio;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@ListProperties("name, familyName, givenName, jobTitle, middleName, nickName")
	@OrderBy("name asc")
	private Collection<User> usuarios;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@AsEmbedded
	@NoSearch
	private ConfiguracionComisionesVenta comisionesVenta;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	@Hidden
	private Boolean consignacionPrecioActual = Boolean.FALSE;
	
	public Collection<User> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(Collection<User> usuarios) {
		this.usuarios = usuarios;
	}

	public static void buscarEmpresasHabilitadas(List<String> idsEmpresa) {
		List<Empresa> empresas = new LinkedList<Empresa>();
		buscarObjetosEmpresasHabilitadas(empresas);
		for(Empresa e: empresas){
			idsEmpresa.add(e.getId());
		}
	}
	
	public static void buscarObjetosEmpresasHabilitadas(List<Empresa> empresas){
		String sql = "from Empresa e, User u where " +
				"u member of e.usuarios and u.name = :user and e.activo = :activo"; 			
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("user", Users.getCurrent());
		query.setParameter("activo", Boolean.TRUE);
		try{
			@SuppressWarnings("unchecked")
			Collection<Object[]> resultList = query.getResultList();
			for(Object[] array: resultList){
				Empresa empresa = (Empresa)array[0];
				empresas.add(empresa);
			}
		}
		catch(ElementNotFoundException e){
		
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void onPreCreate(){
		super.onPreCreate();
		
		Query query = XPersistence.getManager().createQuery("from Empresa e");
		try{
			Collection<Object> resultList = query.getResultList();
			if (resultList.size() >= Empresa.CANTIDADEMPRESAS){
				throw new ValidationException("No se puede crear más de 2 empresas");
			}
			
		}
		catch(ElementNotFoundException e){
			
		}
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getCuit() {
		return cuit;
	}

	public void setCuit(String cuit) {
		if (cuit != null){
			this.cuit = cuit.trim();
		}
		else{
			this.cuit = null;
		}
	}

	public Boolean getAgenteRecaudacion() {
		return agenteRecaudacion;
	}

	public void setAgenteRecaudacion(Boolean agenteRecaudacion) {
		this.agenteRecaudacion = agenteRecaudacion;
	}

	public Domicilio getDomicilio() {
		return domicilio;
	}

	public void setDomicilio(Domicilio domicilio) {
		this.domicilio = domicilio;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public Date getInicioActividad() {
		return inicioActividad;
	}

	public void setInicioActividad(Date inicioActividad) {
		this.inicioActividad = inicioActividad;
	}

	public String getIngresosBrutos() {
		return ingresosBrutos;
	}

	public void setIngresosBrutos(String ingresosBrutos) {
		this.ingresosBrutos = ingresosBrutos;
	}

	public Boolean getInscriptoIva() {
		return inscriptoIva;
	}

	public void setInscriptoIva(Boolean inscriptoIva) {
		this.inscriptoIva = inscriptoIva;
	}

	public Moneda getMoneda1() {
		return moneda1;
	}

	public void setMoneda1(Moneda moneda1) {
		this.moneda1 = moneda1;
	}

	public Moneda getMoneda2() {
		return moneda2;
	}

	public void setMoneda2(Moneda moneda2) {
		this.moneda2 = moneda2;
	}

	public Boolean getGeneraContabilidad() {
		return generaContabilidad;
	}

	public void setGeneraContabilidad(Boolean generaContabilidad) {
		this.generaContabilidad = generaContabilidad;
	}

	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesSoloLectura.add("moneda1");
		propiedadesSoloLectura.add("moneda2");
		propiedadesSoloLectura.add("generaContabilidad");
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}

	public Producto getConceptoDiferenciaCambio() {
		return conceptoDiferenciaCambio;
	}

	public void setConceptoDiferenciaCambio(Producto conceptoDiferenciaCambio) {
		this.conceptoDiferenciaCambio = conceptoDiferenciaCambio;
	}

	public DefinicionImpuesto getPercepcion1() {
		return percepcion1;
	}

	public void setPercepcion1(DefinicionImpuesto percepcion1) {
		this.percepcion1 = percepcion1;
	}
	
	public DefinicionImpuesto getPercepcion2() {
		return percepcion2;
	}

	public void setPercepcion2(DefinicionImpuesto percepcion2) {
		this.percepcion2 = percepcion2;
	}

	public void calcularPercepciones(Transaccion transaccion) {
		if (this.getAgenteRecaudacion()){
			for (int i = 1; i <= Empresa.CANTIDADPERCEPCIONESVENTA; i++){
				DefinicionImpuesto impuesto;
				try {
					impuesto = (DefinicionImpuesto)this.getClass().getMethod("getPercepcion" + Integer.toString(i)).invoke(this);
					if (impuesto != null){
						impuesto.calculadorImpuesto().calcular(transaccion, null, i);
					}
				} 
				catch (ValidationException e){
					throw e;
				}	
				catch (Exception e) {
					throw new ValidationException("Calculo percepciones: " + e.toString());
				}
			}			
		}
		else{
			for (int i = 1; i <= Empresa.CANTIDADPERCEPCIONESVENTA; i++){
				try{
					transaccion.getClass().getMethod("setPercepcion" + Integer.toString(i), BigDecimal.class).invoke(transaccion, BigDecimal.ZERO);
				} catch (Exception e) {
					throw new ValidationException("Poner en cero las percepciones: " + e.toString());
				}	
			}			
		}
	}
		
	public void buscarRetencionesPago(List<Impuesto> retenciones){
		if (this.getAgenteRecaudacion()){			
			Query query = XPersistence.getManager().createQuery("from Impuesto where pagos = :pagos");
			query.setParameter("pagos", Boolean.TRUE);
			List<?> list = query.getResultList();
			for(Object result: list){
				retenciones.add((Impuesto)result);
			}
		}
	}
	
	public boolean usuarioHabilitado(String usuario){
		String sql = "from Empresa e, User u where " +
				"u member of e.usuarios and u.name = :usuario and e.id = :empresa";
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("usuario", usuario);
		query.setParameter("empresa", this.getId());
		query.setMaxResults(1);
		return !query.getResultList().isEmpty();
	}

	public ConfiguracionComisionesVenta getComisionesVenta() {
		return comisionesVenta;
	}

	public void setComisionesVenta(ConfiguracionComisionesVenta comisionesVenta) {
		this.comisionesVenta = comisionesVenta;
	}
	
	public boolean calculaComisiones(){
		boolean calcula = false;
		if (this.getComisionesVenta() != null){
			calcula = this.getComisionesVenta().getActivo();
		}
		return calcula;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getWeb() {
		return web;
	}

	public void setWeb(String web) {
		this.web = web;
	}

	public Boolean getConsignacionPrecioActual() {
		return consignacionPrecioActual == null ? Boolean.FALSE : this.consignacionPrecioActual;
	}

	public void setConsignacionPrecioActual(Boolean consignacionPrecioActual) {
		this.consignacionPrecioActual = consignacionPrecioActual;
	}

	public PosicionAnteImpuesto getPosicionIva() {
		return posicionIva;
	}

	public void setPosicionIva(PosicionAnteImpuesto posicionIva) {
		this.posicionIva = posicionIva;
	}
	
	public boolean esMonotributista(){
		boolean monotributista = false; 
		if (this.getPosicionIva() != null){
			monotributista = (this.getPosicionIva().getCodigo().equals(PosicionAnteImpuesto.MONOTRIBUTISTA));
		}
		return monotributista;
	}

	public Boolean getPrecioMasIva() {
		return precioMasIva == null ? Boolean.TRUE : precioMasIva;
	}

	public void setPrecioMasIva(Boolean precioMasIva) {
		this.precioMasIva = precioMasIva;
	}
	
	public boolean utilizaPrecioMasIva() {
		return !this.getInscriptoIva() && this.getPrecioMasIva();
	}
}
