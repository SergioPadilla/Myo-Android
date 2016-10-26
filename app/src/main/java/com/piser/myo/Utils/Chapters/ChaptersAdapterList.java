package com.piser.myo.Utils.Chapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.piser.myo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergiopadilla on 22/10/16.
 */

public class ChaptersAdapterList extends ArrayAdapter<Chapter> {
    /**
     * Adapter contain the chapters of the list (usually, the chapters of a season)
     */

    private Activity activity;
    private List<Chapter> chapters;

    public ChaptersAdapterList(Activity act) {
        super(act.getApplicationContext(), 0);
        activity = act;
        chapters = new ArrayList<>();
        loadChapters();
        setSeason(chapters);
    }

    public void setSeason(List<Chapter> season) {
        chapters = season;
        clear();
        addAll(chapters);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        ViewHolder holder;

        if(item == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            item = inflater.inflate(R.layout.item_list, null);

            holder = new ViewHolder();
            holder.text = (TextView) item.findViewById(R.id.text);

            item.setTag(holder);
        }
        else {
            holder = (ViewHolder)item.getTag();
        }

        holder.text.setText(chapters.get(position).getCodePlusTitle());
        item.setId(position);

        return item;
    }

    private void loadChapters() {
        chapters.add(new Chapter("1x01", "Me dicen negro", "ztL8KMLJ9Is"));
        chapters.add(new Chapter("1x02", "La cosecha", "FxO1poo8f2I"));
        chapters.add(new Chapter("1x03", "El próximo antes de ayer", "fD6rngYE12M"));
        chapters.add(new Chapter("1x04", "Chair Driver", "RbJFJEfviY8"));
        chapters.add(new Chapter("1x05", "Callejosos", "kWzgRHC5apY"));
        chapters.add(new Chapter("1x06", "Cuentos y leyendas", "HAwBh4747kE"));
        chapters.add(new Chapter("1x07", "Módulo tres", "uZKl4jY1Jjg"));
        chapters.add(new Chapter("1x08", "No girls", "i--XduiLsI0"));
        chapters.add(new Chapter("1x09", "Cicatrices", "qUGd7sEcK8s"));
        chapters.add(new Chapter("1x10", "Se vende", "aYsjewZQ5Ig"));
    }

    private static class ViewHolder {
        TextView text;
    }
}
