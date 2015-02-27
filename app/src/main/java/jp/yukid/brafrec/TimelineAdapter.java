package jp.yukid.brafrec;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;

import java.util.List;

import twitter4j.Status;

/**
 * Created by yuki on 15/02/27.
 */
public class TimelineAdapter extends ArrayAdapter<Status> {
    private LayoutInflater layoutInflater;

    public TimelineAdapter(Context context, int resource, List<Status> objects) {
        super(context, resource, objects);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Status status;
        ImageView icon;
        TextView header;
        TextView text;
        TextView footer;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.timeline, null);
        }

        status = getItem(position);

        if (status != null) {
            header = (TextView) convertView.findViewById(R.id.header);
            text = (TextView) convertView.findViewById(R.id.text);
            footer = (TextView) convertView.findViewById(R.id.footer);

            header.setText(status.getUser().getName() + " " + "@" + status.getUser().getScreenName());
            text.setText(status.getText());
            footer.setText(status.getCreatedAt().toString() + " " + "via" + " " + Jsoup.parse(status.getSource()).text());
        } else {
            Log.d("TimelineAdapter", "status is null");
        }

        return convertView;
    }
}
