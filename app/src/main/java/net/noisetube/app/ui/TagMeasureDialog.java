/**
 * --------------------------------------------------------------------------------
 *  NoiseTube Mobile client (Java implementation; Android version)
 *
 *  Copyright (C) 2008-2010 SONY Computer Science Laboratory Paris
 *  Portions contributed by Vrije Universiteit Brussel (BrusSense team), 2008-2015
 *  Portions contributed by University College London (ExCiteS group), 2012
 *  Android port by Vrije Universiteit Brussel (BrusSense team), 2010-2015
 * --------------------------------------------------------------------------------
 *  This library is free software; you can redistribute it and/or modify it under
 *  the terms of the GNU Lesser General Public License, version 2.1, as published
 *  by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along
 *  with this library; if not, write to:
 *    Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor,
 *    Boston, MA  02110-1301, USA.
 *
 *  Full GNU LGPL v2.1 text: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *  NoiseTube project source code repository: http://code.google.com/p/noisetube
 * --------------------------------------------------------------------------------
 *  More information:
 *   - NoiseTube project website: http://www.noisetube.net
 *   - Sony Computer Science Laboratory Paris: http://csl.sony.fr
 *   - VUB BrusSense team: http://www.brussense.be
 * --------------------------------------------------------------------------------
 */

package net.noisetube.app.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import net.noisetube.R;
import net.noisetube.api.model.Track;
import net.noisetube.api.ui.NTColor;
import net.noisetube.app.config.AndroidPreferences;
import net.noisetube.app.ui.delegate.TagMeasureViewModel;
import net.noisetube.app.ui.widget.MultiSpinner;
import net.noisetube.app.ui.widget.MultiSpinner.MultiSpinnerItem;
import net.noisetube.app.ui.widget.MultiSpinner.MultiSpinnerListener;
import net.noisetube.app.ui.widget.SPLGraphView;

import java.util.Collections;
import java.util.List;


/**
 * @author sbarthol, mstevens, humberto
 */
public class TagMeasureDialog extends Dialog {

    private EditText txtTags;
    private MultiSpinner tagSpinner;
    private Button btnSkipTagging;
    private Button btnApplyTagging;
    private int drawable;

    private AndroidPreferences preferences;

    private Track track;
    private TagMeasureViewModel delegate;
    private SPLGraphView slpGraphView;

    public TagMeasureDialog(Context context, Track track, int numberFirstM, int numberLastM) {
        super(context);
        this.slpGraphView = slpGraphView;
        this.track = track;
        this.delegate = new TagMeasureViewModel(track, numberFirstM, numberLastM);
        preferences = AndroidPreferences.getInstance();
    }

//    public TagMeasureDialog(Context context, Track track, int numberFirstM, int numberLastM) {
//        super(context);
//        this.track = track;
//        this.delegate = new TagMeasureViewModel(track, numberFirstM, numberLastM);
//        preferences = AndroidPreferences.getInstance();
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_tag_measure);
        setTitle(R.string.title_dialog_tag_measure);

		/* get child views */
        txtTags = (EditText) findViewById(R.id.txtTags);
        btnSkipTagging = (Button) findViewById(R.id.btnSkipTagging);
        btnSkipTagging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        btnApplyTagging = (Button) findViewById(R.id.btnApplyTagging);
        drawable = btnApplyTagging.getDrawingCacheBackgroundColor();
        btnApplyTagging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delegate.invokeTagggingAction(txtTags.getText().toString(), tagSpinner.getItems());
                cancel();
            }
        });


        txtTags.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isValidTag(s.toString()) || tagSpinner.isSomethingSelected())
                    enableApplyTaggingButton(true);
                else
                    enableApplyTaggingButton(false);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        //Spinner with previous tags
        tagSpinner = (MultiSpinner) findViewById(R.id.tagsSpinner);
        if (preferences.getTags().isEmpty()) {
            tagSpinner.setDefaultText(getContext().getString(R.string.empty_spinner));
            tagSpinner.setEnabled(false);
        } else {
            tagSpinner.setEnabled(true);
            tagSpinner.setDefaultText(getContext().getString(R.string.label_spinner));

            tagSpinner.setSeparator(", ");
            Collections.sort(preferences.getTags()); //put tags in alphabetical order
            for (String oldTag : preferences.getTags())
                tagSpinner.addItem(oldTag, false);
        }
        tagSpinner.setListener(new MultiSpinnerListener() {

            public void onSelectionChanged(MultiSpinner spinner, List<MultiSpinnerItem> items) {
                if (spinner.isSomethingSelected() || isValidTag(txtTags.getText().toString())) {
                    enableApplyTaggingButton(true);
                } else {
                    enableApplyTaggingButton(false);
                }
            }
        });

    }

    private boolean isValidTag(String tag) {
        return (tag.length() > 0 && !tag.trim().isEmpty());
    }

    public void enableApplyTaggingButton(boolean enable) {
        if (enable) {
            btnApplyTagging.setEnabled(true);
            btnApplyTagging.setTextColor(new NTColor(255, 255, 255).getRGBValue());
            btnApplyTagging.setBackgroundColor(new NTColor(0, 150, 136).getRGBValue());
        } else {
            btnApplyTagging.setEnabled(false);
            btnApplyTagging.setTextColor(new NTColor(182, 182, 182).getRGBValue());
            btnApplyTagging.setBackgroundColor(drawable);


        }
    }

    public void onBackPressed() {
        cancel();
    }


}
