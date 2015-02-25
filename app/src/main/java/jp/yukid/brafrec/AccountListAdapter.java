package jp.yukid.brafrec;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by yuki on 15/02/25.
 */
public class AccountListAdapter extends ArrayAdapter<Twitter> {
    private LayoutInflater layoutInflater;

    public AccountListAdapter(Context context, int textViewResourceId, List<Twitter> objects) {
        super(context, textViewResourceId, objects);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Twitter twitter = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.account_list, null);
        }

        final TextView userName;
        userName = (TextView) convertView.findViewById(R.id.userName);
        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    return twitter.getScreenName();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                userName.setText(s);
            }
        };
        asyncTask.execute();

        return convertView;
    }
}
