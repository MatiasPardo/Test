package org.openxava.base.actions;

import javax.persistence.*;
import javax.persistence.Entity;

import org.hibernate.annotations.*;
import org.openxava.annotations.*;

@Entity

@Views({
	@View(name="Foto", members="foto"),
	@View(name="FotoSoloLectura", members="foto")
})

public class ObjetoImagen {

	@Id @GeneratedValue(generator="system-uuid") @Hidden 
	@GenericGenerator(name="system-uuid", strategy = "uuid")
	@Column(length=32)
	private String id = "";
	
	@Stereotype("PHOTO")
	@Column(length=30000)
	@ReadOnly(forViews="FotoSoloLectura")
	private byte [] foto;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public byte[] getFoto() {
		return foto;
	}

	public void setFoto(byte[] foto) {
		this.foto = foto;
	}
}
