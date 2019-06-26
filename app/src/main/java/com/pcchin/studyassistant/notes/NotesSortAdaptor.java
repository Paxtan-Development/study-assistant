package com.pcchin.studyassistant.notes;

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

class NotesSortAdaptor extends ArrayAdapter {
    private final int[] titles;
    private final int[] images;
    private final Context context;

    NotesSortAdaptor(@NonNull Context context, int[] titles, int[] images) {
        super(context, R.layout.n2_sorting_spinner);
        this.titles = titles;
        this.images = images;
        this.context = context;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

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

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return super.getDropDownView(position, convertView, parent);
    }

    private static class ViewHolder {
        ImageView sortLogo;
        TextView sortTitle;
    }
}
