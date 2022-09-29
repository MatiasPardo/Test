package org.openxava.base.actions;

import org.openxava.actions.*;

public class CubosEmpresaAction extends BaseAction implements IForwardAction{
	@Override
	public void execute() throws Exception {
	}

	@Override
	public String getForwardURI() {
		return "javascript:void(window.open('http://138.36.238.47:8080/pivot/web/'))";
	}

	@Override
	public boolean inNewWindow() {
		return false;
	}
}
