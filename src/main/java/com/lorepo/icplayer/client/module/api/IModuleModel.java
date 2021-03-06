package com.lorepo.icplayer.client.module.api;

import com.google.gwt.xml.client.Element;
import com.lorepo.icf.properties.IPropertyProvider;
import com.lorepo.icplayer.client.framework.module.IStyledModule;

public interface IModuleModel extends IStyledModule, IRectangleItem, IPropertyProvider{
	public String getModuleTypeName(); // get module type
	public String getModuleName(); // get translated module name
	public String getId();
	public void setId(String id);
	public void release();
	public void load(Element node, String baseUrl);
	public String toXML();
	public void addNameValidator(INameValidator validator);
	public boolean isLocked();
	public void lock(boolean state);
	public String getButtonType();
	public boolean isModuleInEditorVisible();
	public void setModuleInEditorVisibility(boolean moduleInEditorVisibility);
}
