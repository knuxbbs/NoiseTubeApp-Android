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

package net.noisetube.app.ui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-selection Spinner view
 *
 * @author mstevens
 *         <p/>
 *         Based on code written by: David V_vra (Destil)
 *         Shared here: http://stackoverflow.com/questions/5015686/android-spinner-with-multiple-choice
 *         Licenced under CC-Wiki terms: http://creativecommons.org/licenses/by-sa/3.0
 */
public class MultiSpinner extends Spinner implements OnMultiChoiceClickListener, OnCancelListener {

    private static final String DEFAULT_SEPARATOR = ", ";
    private String separator = DEFAULT_SEPARATOR;
    private List<MultiSpinnerItem> items;
    private String defaultText = "";
    private MultiSpinnerListener listener;

    public MultiSpinner(Context context) {
        super(context);
        items = new ArrayList<MultiSpinnerItem>();
    }

    public MultiSpinner(Context context, AttributeSet attribs) {
        super(context, attribs);
        items = new ArrayList<MultiSpinnerItem>();
    }

    public MultiSpinner(Context context, AttributeSet attribs, int defStyle) {
        super(context, attribs, defStyle);
        items = new ArrayList<MultiSpinnerItem>();
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        items.get(which).selected = isChecked;
        //super.onClick(dialog, which);
    }

    public void onCancel(DialogInterface dialog) {
        updateText();
        if (listener != null)
            listener.onSelectionChanged(this, items);
    }

    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        CharSequence[] itemStrings = new CharSequence[items.size()];
        boolean[] itemSelecteds = new boolean[items.size()];
        for (int i = 0; i < items.size(); i++) {
            itemStrings[i] = items.get(i).toString();
            itemSelecteds[i] = items.get(i).isSelected();
        }
        builder.setMultiChoiceItems(itemStrings, itemSelecteds, this);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setOnCancelListener(this);
        builder.show();
        return true;
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
        updateText();
    }

	/*public MultiSpinnerItem getItemAtPosition(int pos)
    {
		return items.get(pos);
	}*/

    public List<MultiSpinnerItem> getItems() {
        return items;
    }

    public boolean isSomethingSelected() {
        for (MultiSpinnerItem item : items)
            if (item.selected)
                return true;
        return false;
    }

    private void updateText() {
        // refresh text on spinner
        StringBuffer spinnerBuffer = new StringBuffer();
        for (MultiSpinnerItem item : items) {
            if (item.selected) {
                if (spinnerBuffer.length() > 0)
                    spinnerBuffer.append(separator);
                spinnerBuffer.append(item.toString());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, new String[]{(spinnerBuffer.length() > 0 ? spinnerBuffer.toString() : defaultText)});
        setAdapter(adapter);
    }

    public void setListener(MultiSpinnerListener listener) {
        this.listener = listener;
    }

    public void addItem(Object item, boolean selected) {
        items.add(new MultiSpinnerItem(item, selected));
    }

    public interface MultiSpinnerListener {
        public void onSelectionChanged(MultiSpinner spinner, List<MultiSpinnerItem> items);
    }

    public class MultiSpinnerItem {

        private Object item;
        private boolean selected;

        /**
         * @param selected
         */
        public MultiSpinnerItem(Object item, boolean selected) {
            this.item = item;
            this.selected = selected;
        }

        /**
         * @return the selected
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * @param selected the selected to set
         */
        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        /**
         * @return the item
         */
        public Object getItem() {
            return item;
        }

        public String toString() {
            return item.toString();
        }

    }

}