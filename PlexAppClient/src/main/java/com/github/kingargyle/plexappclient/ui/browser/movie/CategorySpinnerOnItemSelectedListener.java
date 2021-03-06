/**
 * The MIT License (MIT)
 * Copyright (c) 2012 David Carver
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
 * OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.kingargyle.plexappclient.ui.browser.movie;

import java.util.List;

import com.github.kingargyle.plexappclient.R;
import com.github.kingargyle.plexappclient.core.model.CategoryInfo;
import com.github.kingargyle.plexappclient.core.model.SecondaryCategoryInfo;
import com.github.kingargyle.plexappclient.core.services.MovieSecondaryCategoryRetrievalIntentService;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.Toast;

/**
 * @author dcarver
 * 
 */
public class CategorySpinnerOnItemSelectedListener implements
		OnItemSelectedListener {

	private String selected;
	private static String key;
	private boolean firstSelection = true;
	private static Activity context;
	private static String category;
	private Handler secondaryCategoryHandler;

	public CategorySpinnerOnItemSelectedListener(String defaultSelection,
			String key) {
		selected = defaultSelection;
		this.key = key;
		secondaryCategoryHandler = new SecondaryCategoryHandler();
	}

	public void onItemSelected(AdapterView<?> viewAdapter, View view,
			int position, long id) {
		context = (Activity) view.getContext();
		CategoryInfo item = (CategoryInfo) viewAdapter
				.getItemAtPosition(position);

		if (firstSelection) {
			View bgLayout = context
					.findViewById(R.id.movieBrowserBackgroundLayout);
			Gallery posterGallery = (Gallery) context
					.findViewById(R.id.moviePosterGallery);
			posterGallery.setAdapter(new MoviePosterImageGalleryAdapter(
					context, key, item.getCategory()));
			posterGallery
					.setOnItemSelectedListener(new MoviePosterOnItemSelectedListener(
							bgLayout, context));
			posterGallery
					.setOnItemClickListener(new MoviePosterOnItemClickListener());
			firstSelection = false;
			return;
		}

		if (selected.equalsIgnoreCase(item.getCategory())) {
			return;
		}

		selected = item.getCategory();
		category = item.getCategory();

		Spinner secondarySpinner = (Spinner) context
				.findViewById(R.id.movieCategoryFilter2);

		if (item.getLevel() == 0) {
			secondarySpinner.setVisibility(View.INVISIBLE);
			View bgLayout = context
					.findViewById(R.id.movieBrowserBackgroundLayout);
			Gallery posterGallery = (Gallery) context
					.findViewById(R.id.moviePosterGallery);
			posterGallery.setAdapter(new MoviePosterImageGalleryAdapter(
					context, key, item.getCategory()));
			posterGallery
					.setOnItemSelectedListener(new MoviePosterOnItemSelectedListener(
							bgLayout, context));
			posterGallery
					.setOnItemClickListener(new MoviePosterOnItemClickListener());
		} else {
			Messenger messenger = new Messenger(secondaryCategoryHandler);
			Intent categoriesIntent = new Intent(context,
					MovieSecondaryCategoryRetrievalIntentService.class);
			categoriesIntent.putExtra("key", key);
			categoriesIntent.putExtra("category", category);
			categoriesIntent.putExtra("MESSENGER", messenger);
			context.startService(categoriesIntent);
		}

	}

	public void onNothingSelected(AdapterView<?> va) {

	}

	private static class SecondaryCategoryHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			List<SecondaryCategoryInfo> secondaryCategories = (List<SecondaryCategoryInfo>) msg.obj;

			if (secondaryCategories == null || secondaryCategories.isEmpty()) {
				Toast.makeText(context, "No Entries available for category.",
						Toast.LENGTH_LONG).show();
				return;
			}

			Spinner secondarySpinner = (Spinner) context
					.findViewById(R.id.movieCategoryFilter2);
			secondarySpinner.setVisibility(View.VISIBLE);

			ArrayAdapter<SecondaryCategoryInfo> spinnerSecArrayAdapter = new ArrayAdapter<SecondaryCategoryInfo>(
					context, R.layout.serenity_spinner_textview,
					secondaryCategories);
			spinnerSecArrayAdapter
					.setDropDownViewResource(R.layout.serenity_spinner_textview_dropdown);
			secondarySpinner.setAdapter(spinnerSecArrayAdapter);
			secondarySpinner
					.setOnItemSelectedListener(new SecondaryCategorySpinnerOnItemSelectedListener(
							category, key));

		}

	}

}
