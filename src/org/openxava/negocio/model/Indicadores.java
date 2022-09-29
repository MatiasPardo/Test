package org.openxava.negocio.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.openxava.annotations.Hidden;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.Tab;
import org.openxava.annotations.Tabs;

@Entity


@Tabs({
	@Tab(defaultOrder="${fecha} desc")
})

public class Indicadores {
	@Id
	@Hidden
	@Column(length=32)
	private String id;
	
	@ReadOnly
	private Date fecha;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}	
}
