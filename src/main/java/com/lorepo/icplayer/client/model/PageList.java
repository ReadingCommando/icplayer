package com.lorepo.icplayer.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.lorepo.icf.properties.BasicPropertyProvider;
import com.lorepo.icf.properties.IProperty;
import com.lorepo.icf.utils.StringUtils;
import com.lorepo.icf.utils.XMLUtils;
import com.lorepo.icf.utils.i18n.DictionaryWrapper;
import com.lorepo.icplayer.client.module.api.player.IChapter;
import com.lorepo.icplayer.client.module.api.player.IContentNode;
import com.lorepo.icplayer.client.module.api.player.IPlayerServices;



public class PageList extends BasicPropertyProvider implements IChapter{

	private IPlayerServices playerServices;
	private final List<IContentNode>	nodes = new ArrayList<IContentNode>();
	private IPageListListener listener;
	public String name;

	public PageList(){
		this("Chapter");
	}



	public PageList(String name){
		super("Chapter");
		this.name = name;
		addPropertyName();
	}

	@Override
	public String getName(){
		return name;
	}

	public void setPlayerServices(IPlayerServices ps) {
		this.playerServices = ps;
		List<Page> pages = getAllPages();
		for (Page page : pages) {
			page.setPlayerServices(ps);
		}
	}


	public void addListener(IPageListListener l){
		this.listener = l;
	}


	@Override
	public boolean add(IContentNode node){

		boolean result = nodes.add(node);
		if(listener != null){
			listener.onNodeAdded(node);
			if(node instanceof PageList){
				PageList pages = (PageList) node;
				pages.addListener(listener);
			}
		}

		return result;
	}


	@Override
	public List<Page> getAllPages(){
		List<Page> pages = new Vector<Page>();
		for(IContentNode node : nodes){
			if(node instanceof Page){
				pages.add((Page) node);
			}
			else if(node instanceof PageList){
				PageList chapter = (PageList) node;
				pages.addAll(chapter.getAllPages());
			}
		}

		return pages;
	}

	public void insertBefore(int index, IContentNode node){

		nodes.add(index, node);

		if(listener != null){
			listener.onNodeAdded(node);
		}
	}


	public IContentNode removePage(int index){

		IContentNode node = nodes.remove(index);

		if(listener != null){
			listener.onNodeRemoved(node, this);
		}

		return node;
	}


	public boolean remove(IContentNode node){

		boolean result = nodes.remove(node);

		if(listener != null && result){
			listener.onNodeRemoved(node, this);
		}

		return result;
	}


	public boolean removeFromTree(IContentNode node){

		boolean result = nodes.remove(node);

		if(result){
			if(listener != null){
				listener.onNodeRemoved(node, this);
			}
		}
		else{
			for(IContentNode item : nodes){
				if(item instanceof PageList){
					PageList chapter = (PageList) item;
					if(chapter.removeFromTree(node)){
						break;
					}
				}
			}
		}

		return result;
	}


	public boolean remove(String name){

		for(IContentNode node : nodes){
			if(node instanceof Page){
				Page page = (Page) node;
				if(page.getName().equals(name)){
					nodes.remove(node);
					if(listener != null){
						listener.onNodeRemoved(page, this);
					}
					return true;
				}
			}
			else if(node instanceof PageList){
				PageList chapter = (PageList) node;
				if(chapter.remove(name)){
					return true;
				}
			}
		}


		return false;
	}


	public int findPageIndexByName(String pageName) {

		int index = 0;
		String strippedSourceName = pageName.replaceAll("\\s+", "");
		List<Page> pages = getAllPages();
		for(Page page : pages){

			String strippedName = page.getName().replaceAll("\\s+", "");
			if(strippedName.compareToIgnoreCase(strippedSourceName) == 0){
				return index;
			}
			index++;
		}

		return -1;
	}

	public int findPageIndexById(String pageId) {

		int index = 0;
		List<Page> pages = getAllPages();
		for(Page page : pages){
			if(page.getId().equals(pageId)){
				return index;
			}
			index++;
		}

		return -1;
	}


	public String generateUniquePageName() {

		String pageName = "New page";

		for(int i = 1; i < 200; i++){
			pageName = DictionaryWrapper.get("page") + " " + i;
			if(findPageIndexByName(pageName) == -1){
				break;
			}
		}

		return pageName;
	}


	public int getTotalPageCount(){
		int counter = 0;
		for(IContentNode node : nodes){
			if(node instanceof Page){
				counter += 1;
			}
			else if(node instanceof PageList){
				PageList chapter = (PageList) node;
				counter += chapter.getTotalPageCount();
			}
		}
		return counter;
	}

	@Override
	public void load(Element rootElement, String url) {
		//for IXMLSerializable interface
		load(rootElement, url, null, 0);
	}


	public int load(Element rootElement, String url, ArrayList<Integer> subsetOfPages, int pageIndex) {
		String nodeName = XMLUtils.getAttributeAsString(rootElement, "name");

		boolean isLoadedWithSubset = subsetOfPages != null && subsetOfPages.size() > 0;
		name = StringUtils.unescapeXML(nodeName);
		NodeList children = rootElement.getChildNodes();
		for(int i = 0; i < children.getLength(); i++){
			if(children.item(i) instanceof Element){
				Element node = (Element)children.item(i);
				if(node.getNodeName().compareTo("page") == 0){
					if (isLoadedWithSubset && !subsetOfPages.contains(pageIndex)) {
						pageIndex++;
						continue;
					} else {
						Page page = loadPage(node);
						add(page);
						pageIndex++;
					}
				} else if(node.getNodeName().compareTo("chapter") == 0) {
					PageList chapter = new PageList();
					chapter.addListener(listener);
					pageIndex = chapter.load(node, url, subsetOfPages, pageIndex);
					nodes.add(chapter);
				}
			}
		}
		return pageIndex;
	}

	private Page loadPage(Element node) {
		final String name = StringUtils.unescapeXML(node.getAttribute("name"));
		final String href = node.getAttribute("href");
		final String pageId = node.getAttribute("id");
		final String preview = XMLUtils.getAttributeAsString(node, "preview");

		final String pageWeightAttribute = node.getAttribute("pageWeight");
		final boolean isEmpty = pageWeightAttribute == null || pageWeightAttribute.isEmpty();
		final int weight = isEmpty ? 1 : (int) Float.parseFloat(pageWeightAttribute);

		boolean reportable = XMLUtils.getAttributeAsBoolean(node, "reportable", true);
		Page page = new Page(name, href);
		if (pageId != null && pageId.length() > 0 && !pageId.equals("null")) {
			page.setId(pageId);
		}

		page.setPageWeight(weight);
		page.setReportable(reportable);
		page.setPreview(preview);
		return page;
	}

	@Override
	public String toXML() {
		String xml = "";
		for (IContentNode node : nodes) {
			if (node instanceof Page) {
				xml += toXMLPage((Page) node);
			} else if (node instanceof PageList){
				PageList chapter = (PageList) node;
				String name = StringUtils.escapeXML(chapter.getName());
				xml += "<chapter name='" + name + "'>";
				xml += chapter.toXML();
				xml += "</chapter>";
			}
		}

		return xml;
	}

	private String toXMLPage(Page page) {
		String name = StringUtils.escapeXML(page.getName());
		String href = StringUtils.escapeXML(page.getHref());
		String preview = StringUtils.escapeXML(page.getPreview());
		String xml = "<page id='" + page.getId() + "' name='" + name + "'" + " href='" + href + "' preview='" + preview + "'";
		xml += " modulesMaxScore='[s]'".replace("[s]", page.getModulesMaxScore() + "");
		xml += " pageWeight='[w]'".replace("[w]", page.getPageWeight() + "");

		if (page.isReportable()) {
			xml += " reportable='true'/>";
		} else {
			xml += " reportable='false'/>";
		}
		return xml;
	}

	public boolean contains(IContentNode node){
		return nodes.contains(node);
	}

	public int indexOf(IContentNode node){
		return nodes.indexOf(node);
	}

	public void movePage(int from, int to) {
		if (from < nodes.size() && to < nodes.size()){
			IContentNode node = nodes.remove(from);
			nodes.add(to, node);
			if(listener != null && node instanceof Page){
				listener.onNodeMoved(this, from, to);
			}
		}
	}

	@Override
	public int size(){
		return nodes.size();
	}

	@Override
	public IContentNode get(int index){
		return nodes.get(index);
	}

	@Override
	public JavaScriptObject toJavaScript() {
		return javaScriptInterface(this);
	}

	/**
	 * Get JavaScript interface to the page
	 * @param x
	 * @return
	 */
	private native static JavaScriptObject javaScriptInterface(PageList x) /*-{

		var chapter = function(){}
		chapter.type = "chapter";
		chapter.getName = function(){
			return x.@com.lorepo.icplayer.client.model.PageList::getName()();
		}
		chapter.size = function(){
			return x.@com.lorepo.icplayer.client.model.PageList::size()();
		}
		chapter.get = function(index){
			return x.@com.lorepo.icplayer.client.model.PageList::getPageAsJavaScript(I)(index);
		}

		return chapter;
	}-*/;

	private JavaScriptObject getPageAsJavaScript(int index) {
		return get(index).toJavaScript();
	}

	private void addPropertyName() {

		IProperty propertyName = new IProperty() {

			@Override
			public void setValue(String newValue) {
				name = newValue;
				fireChangedEvent();
			}

			@Override
			public String getValue() {
				return name;
			}

			@Override
			public String getName() {
				return DictionaryWrapper.get("name");
			}

			@Override
			public String getDisplayName() {
				return DictionaryWrapper.get("name");
			}

			@Override
			public boolean isDefault() {
				return false;
			}
		};

		addProperty(propertyName);
	}

	protected void fireChangedEvent() {
		if(listener != null){
			listener.onChanged(this);
		}
	}

	public IChapter getParentChapter(IContentNode node) {
		IChapter parent = null;
		for(IContentNode item : nodes){
			if(item == node){
				parent = this;
				break;
			}
			else if(item instanceof PageList){
				parent = ((PageList)item).getParentChapter(node);
				if(parent != null){
					break;
				}
			}
		}
		return parent;
	}

}
