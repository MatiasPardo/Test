package org.openxava.base.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;

@Entity

public class ConfiguracionSMTP extends ObjetoNegocio{

	@Column(length=20)
	@SearchKey
	@Required
	private String nombre;
	
	@Column(length=50)
	@Required
	private String host;
	
	@Required
	private int port;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean startTLSenable = Boolean.TRUE;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean hostTrusted = Boolean.TRUE;

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Boolean getStartTLSenable() {
		return startTLSenable;
	}

	public void setStartTLSenable(Boolean startTLSenable) {
		this.startTLSenable = startTLSenable;
	}

	public Boolean getHostTrusted() {
		return hostTrusted;
	}

	public void setHostTrusted(Boolean hostTrusted) {
		this.hostTrusted = hostTrusted;
	}
}
