package com.lorepo.icplayer.client;

import com.google.gwt.user.client.ui.Widget;
import com.lorepo.icplayer.client.module.api.player.IContent;
import com.lorepo.icplayer.client.module.api.player.IScoreService;
import com.lorepo.icplayer.client.module.api.player.IStateService;

public interface IPlayerController {

	IContent getModel();
	IScoreService getScoreService();
	int getCurrentPageIndex();
	IStateService getStateService();
	void switchToPage(String pageName);
	void switchToPrevPage();
	void switchToNextPage();
	long getTimeElapsed();
	Widget getView();
	void showPopup(String pageName);
	void closePopup();

}