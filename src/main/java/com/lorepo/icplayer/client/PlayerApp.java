package com.lorepo.icplayer.client;

import java.util.HashMap;

import com.google.gwt.user.client.ui.RootPanel;
import com.lorepo.icf.utils.JSONUtils;
import com.lorepo.icf.utils.JavaScriptUtils;
import com.lorepo.icf.utils.URLUtils;
import com.lorepo.icf.utils.dom.DOMInjector;
import com.lorepo.icplayer.client.content.services.ScoreService;
import com.lorepo.icplayer.client.model.Content;
import com.lorepo.icplayer.client.model.Page;
import com.lorepo.icplayer.client.module.api.player.IPlayerServices;
import com.lorepo.icplayer.client.module.api.player.IScoreService;
import com.lorepo.icplayer.client.utils.ILoadListener;
import com.lorepo.icplayer.client.utils.XMLLoader;

/**
 * Static class with JavaScript interface
 * 
 * @author Krzysztof Langner
 *
 */
public class PlayerApp {

	/** Div id */
	private String divId;
	/** Debug mode */
	private boolean testMode = false;
	private	Content				contentModel;
	private PlayerController	appController;
	/** Score service impl */
	private ScoreService		scoreService;
	private DOMInjector domInjector;
	private PlayerEntryPoint	entryPoint;
	private int startPageIndex;
	private HashMap<String, String> loadedState;
	
	
	public PlayerApp(String id, PlayerEntryPoint entryPoint){
		
		this.divId = id;
		this.entryPoint = entryPoint;
		
		scoreService = new ScoreService();
		domInjector = new DOMInjector();
	 }

	
	/**
	 * Get global score service
	 * @return
	 */
	public IScoreService getScoreService() {
		return scoreService;
	}

	/**
	 * Check if app working in debug mode
	 */
	public boolean isDebugMode(){
		return testMode;
	}

	/**
	 * Load content from given URL
	 * @param url
	 * @param pageIndex 
	 */
	public void load(String url, int pageIndex) {
		
		startPageIndex = pageIndex;
		contentModel = new Content();
		XMLLoader	reader = new XMLLoader(contentModel);
		reader.load(url, new ILoadListener() {
			
			@Override
			public void onError(String error) {
				JavaScriptUtils.log("Can't load:" + error);
			}
			
			@Override
			public void onFinishedLoading(Object obj) {
				initPlayer();
			}

		});		
	}

	
	public void setTestMode() {
		testMode = true;
	}

	/**
	 * Init player after content is loaded
	 */
	private void initPlayer() {
	
		appController = new PlayerController(this, contentModel);
	
		RootPanel.get(divId).add(appController.getView());
		String css = URLUtils.resolveCSSURL(contentModel.getBaseUrl(), contentModel.getStyles());
		domInjector.appendStyle(css);

		ContentDataLoader loader = new ContentDataLoader(contentModel.getBaseUrl());
		loader.addAddons(contentModel.getAddonDescriptors().values());
		if(contentModel.getHeader() != null){
			loader.addPage(contentModel.getHeader());
		}
		if(contentModel.getFooter() != null){
			loader.addPage(contentModel.getFooter());
		}
		loader.load(new ILoadListener() {
			public void onFinishedLoading(Object obj) {
				loadFirstPage();
			}

			public void onError(String error) {
			}
		});
		
	}


	private void loadFirstPage() {
		Page page;
		
		if(startPageIndex < contentModel.getPages().size()){
			page = contentModel.getPages().get(startPageIndex);
		}
		else{
			page = contentModel.getPages().get(0);
		}
		appController.showHeaderAndFooter();
		if(loadedState != null){
			appController.getPageController().getPlayerState().loadFromString(loadedState.get("state"));
			appController.getPlayerServices().getScoreService().loadFromString(loadedState.get("score"));
			appController.getPageController().loadPageState();
		}
		appController.switchToPage(page);
	}


	public IPlayerServices getPlayerServices() {
		return appController.getPlayerServices();
	}


	public void onPageLoaded() {

		entryPoint.onPageLoaded();
	}

	public void setState(String state) {
		HashMap<String, String> data = JSONUtils.decodeHashMap(state);
		if(data.containsKey("state") && data.containsKey("score")){
			loadedState = data;
		}
	}

	public String getState() {
		String state = appController.getPageController().getPlayerState().getAsString();
		String score = appController.getPlayerServices().getScoreService().getAsString();
		HashMap<String, String> data = new HashMap<String, String>();
		data.put("state", state);
		data.put("score", score);
		return JSONUtils.toJSONString(data);
	}
}
