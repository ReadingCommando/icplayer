package com.lorepo.icplayer.client.module.button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.lorepo.icplayer.client.module.api.player.IPlayerCommands;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;


class ResetButton extends PushButton{

	private Element element;
	private com.google.gwt.dom.client.Element parent;
	private String confInfo = "";
	private String confInfoYes = "";
	private String confInfoNo = "";

	public ResetButton(final IPlayerCommands pageService, final boolean confirmReset, final String confirmInfo, final String confirmYesInfo, final String confirmNoInfo){
		
		setStyleName("ic_button_reset");
		
		addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				event.stopPropagation();
				event.preventDefault();

				if(confirmReset){
					confInfo = confirmInfo;
					confInfoYes = confirmYesInfo;
					confInfoNo = confirmNoInfo;
					
					if(confInfo == "") {
						confInfo = "Are you sure that you want to reset the modules?";
					}
					
					if(confInfoYes == "") {
						confInfoYes = "Yes";
					}
					
					if(confInfoNo == "") {
						confInfoNo = "No";
					}
					
					final DialogBox dialogBox = new DialogBox();
					dialogBox.setStyleName("ic_confim_box");
			        dialogBox.setHTML("<center>" + confInfo + "</center>");
			        Button yesButton =  new Button(confInfoYes);
			        Button noButton = new Button(confInfoNo);
			        HorizontalPanel dialogHPanel = new HorizontalPanel();
			        dialogHPanel.setStyleName("ic_confirm_box_buttons");
			        yesButton.setStyleName("ic_confirm_box_yes");
			        noButton.setStyleName("ic_confirm_box_no");
			        
			        dialogHPanel.setWidth("100%");
			        dialogHPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			        dialogHPanel.add(yesButton);
			        dialogHPanel.add(noButton);
			        element = getElement();
			        parent = element.getParentElement();
			        int top = 200 + Window.getScrollTop();
			        int left = (parent.getClientWidth() / 2) - 150;
	
			        noButton.addClickHandler(new ClickHandler() {
			            @Override
			            public void onClick(ClickEvent event) {
			                dialogBox.hide();
			              }
			        });
	
			        yesButton.addClickHandler(new ClickHandler() {
			            @Override
			            public void onClick(ClickEvent event) {
			            	pageService.reset();
			                dialogBox.hide();
			              }
			        });
	
			        dialogBox.setWidget(dialogHPanel);
			        dialogBox.setPopupPosition(left, top);
			        dialogBox.show();
				} else {
					pageService.reset();
				}				
			}
		});
		
	}
}
