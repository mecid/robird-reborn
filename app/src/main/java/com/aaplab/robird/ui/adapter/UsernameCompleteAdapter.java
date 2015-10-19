package com.aaplab.robird.ui.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.aaplab.robird.R;
import com.aaplab.robird.data.entity.Contact;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by majid on 17.10.15.
 */
public class UsernameCompleteAdapter extends BaseAdapter implements Filterable {

    private List<Contact> contacts;
    private List<Contact> suggested;
    private LayoutInflater inflater;
    private UsernameFilter filter;

    public UsernameCompleteAdapter(Context context, List<Contact> contacts) {
        this.contacts = contacts;
        this.suggested = new ArrayList<>();
        this.filter = new UsernameFilter();
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return suggested.size();
    }

    @Override
    public Object getItem(int position) {
        return suggested.get(position);
    }

    @Override
    public long getItemId(int position) {
        return suggested.get(position).userId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_user_suggest, parent, false);

        final Contact user = suggested.get(position);

        Glide.with(parent.getContext())
                .load(user.avatar())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(ButterKnife.<ImageView>findById(convertView, R.id.avatar));
        ButterKnife.<TextView>findById(convertView, R.id.screen_name).setText(user.username());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class UsernameFilter extends Filter {

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            Contact user = (Contact) resultValue;
            return "@" + user.username();
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            if (charSequence != null) {
                ArrayList<Contact> suggested = new ArrayList<>();
                String text = charSequence.toString().toLowerCase();
                for (Contact user : contacts) {
                    if (("@" + user.username()).toLowerCase().startsWith(text)) {
                        suggested.add(user);
                    }
                }

                results.values = suggested;
                results.count = suggested.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            if (filterResults != null && filterResults.count > 0) {
                suggested.clear();
                suggested.addAll((List<Contact>) filterResults.values);
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    public static class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;

            while (i > 0 && text.charAt(i - 1) != ' ') {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }

            return i;
        }

        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();

            while (i < len) {
                if (text.charAt(i) == ' ') {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }

        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();

            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }

            if (i > 0 && text.charAt(i - 1) == ' ') {
                return text;
            } else {
                if (text instanceof Spanned) {
                    SpannableString sp = new SpannableString(text + " ");
                    TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                            Object.class, sp, 0);
                    return sp;
                } else {
                    return text + " ";
                }
            }
        }
    }
}
