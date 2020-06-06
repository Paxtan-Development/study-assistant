/*
 * Copyright 2020 PC Chin. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pcchin.studyassistant.utils.notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pcchin.studyassistant.R;

/** An adaptor used to sort notes in each subject. **/
public class NotesSortAdaptor extends ArrayAdapter<String> {
    private final int[] titles;
    private final int[] images;
    private final Context context;

    /** Default constructor. Receives the titles and image references. **/
    public NotesSortAdaptor(@NonNull Context context, int[] titles, int[] images) {
        super(context, R.layout.n2_sorting_spinner);
        this.titles = titles;
        this.images = images;
        this.context = context;
    }

    /** Returns the number of sorting methods in the Spinner. **/
    @Override
    public int getCount() {
        return titles.length;
    }

    /** Creates the view for the object when it is selected.  **/
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                convertView = inflater.inflate(R.layout.n2_spinner_mini, parent, false);
                viewHolder.sortLogo = convertView.findViewById(R.id.n2_sorting_img);
                viewHolder.sortTitle = convertView.findViewById(R.id.n2_sorting_logo);
                convertView.setTag(viewHolder);
            }
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.sortLogo.setImageResource(images[position]);
        viewHolder.sortTitle.setText(titles[position]);

        if (convertView != null) {
            return convertView;
        } else {
            return parent;
        }
    }

    /** Creates the view for the object when it is in the list. Same as getView(). **/
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getView(position, convertView, parent);
    }

    /** Class used by the adaptor to store the ImageView and TextView references. **/
    private static class ViewHolder {
        ImageView sortLogo;
        TextView sortTitle;
    }
}
