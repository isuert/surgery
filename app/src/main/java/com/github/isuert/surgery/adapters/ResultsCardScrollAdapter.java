package com.github.isuert.surgery.adapters;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

import java.util.List;

public class ResultsCardScrollAdapter extends CardScrollAdapter {

    private List<CardBuilder> cards;

    public ResultsCardScrollAdapter(List<CardBuilder> cards) {
        this.cards = cards;
    }

    @Override
    public int getPosition(Object item) {
        return cards.indexOf(item);
    }

    @Override
    public int getCount() {
        return cards.size();
    }

    @Override
    public Object getItem(int position) {
        return cards.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return CardBuilder.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        return cards.get(position).getItemViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return cards.get(position).getView(convertView, parent);
    }
}
