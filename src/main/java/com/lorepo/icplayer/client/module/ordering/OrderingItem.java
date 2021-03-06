package com.lorepo.icplayer.client.module.ordering;

import java.util.ArrayList;

import com.lorepo.icf.properties.BasicPropertyProvider;
import com.lorepo.icf.properties.IHtmlProperty;
import com.lorepo.icf.utils.StringUtils;
import com.lorepo.icf.utils.i18n.DictionaryWrapper;

public class OrderingItem extends BasicPropertyProvider {

	private String html;
	private final String baseURL;
	private final int index;
	private final ArrayList<Integer> alternativeIndexes = new ArrayList<Integer>();

	public OrderingItem(int index, String safeHtml, String baseURL) {

		super(DictionaryWrapper.get("ordering_item"));
		this.index = index;
		this.html = safeHtml;
		this.baseURL = baseURL;

		addPropertyText();
	}

	public String getText() {
		return baseURL == null ? html : StringUtils.updateLinks(html, baseURL);
	}

	private void addPropertyText() {

		IHtmlProperty property = new IHtmlProperty() {

			@Override
			public void setValue(String newValue) {
				html = newValue;
				sendPropertyChangedEvent(this);
			}

			@Override
			public String getValue() {
				return html;
			}

			@Override
			public String getName() {
				return DictionaryWrapper.get("ordering_item_text");
			}

			@Override
			public String getDisplayName() {
				return DictionaryWrapper.get("ordering_item_text");
			}

			@Override
			public boolean isDefault() {
				return false;
			}
		};

		addProperty(property);
	}

	public void addAlternativeIndex(int index) {
		alternativeIndexes.add(index);
	}

	public int getIndex() {
		return index;
	}

	public boolean isCorrect(int position) {

		if (index == position) return true;

		for (int index : alternativeIndexes)
			if (index == position) return true;

		return false;
	}

	public void clearAlternativeIndexes() {
		alternativeIndexes.clear();
	}
}
