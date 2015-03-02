package jp.yukid.brafrec;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;


public class HomeTimelineActivity extends ActionBarActivity {
    private Twitter twitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_timeline);

        Intent intent;
        // final Twitter twitter;
        // final ResponseList<Status> timeline;
        final ListView timelineView;
        Button refreshButton;
        Button postButton;
        final LinearLayout postArea;

        intent = getIntent();
        twitter = (Twitter) intent.getSerializableExtra("twitter");
        timelineView = (ListView) findViewById(R.id.timeline);

        loadHomeTimeline(timelineView);

        refreshButton = (Button) findViewById(R.id.refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadHomeTimeline(timelineView);
            }
        });

        postArea = (LinearLayout) findViewById(R.id.editPostArea);
        postButton = (Button) findViewById(R.id.post);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editPost;
                InputMethodManager inputMethodManager;

                editPost = (EditText) findViewById(R.id.editPost);
                inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                if (postArea.getVisibility() == View.GONE) {
                    postArea.setVisibility(View.VISIBLE);
                    editPost.requestFocus();
                    inputMethodManager.showSoftInput(editPost, InputMethodManager.SHOW_IMPLICIT);
                } else {
                    if (editPost.getText().toString().length() != 0) {
                        AsyncTask<String, Void, Status> asyncTask = new AsyncTask<String, Void, Status>() {
                            @Override
                            protected twitter4j.Status doInBackground(String... params) {
                                try {
                                    return twitter.updateStatus(params[0]);
                                } catch (TwitterException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(twitter4j.Status status) {
                                if (status != null) {
                                    Toast.makeText(HomeTimelineActivity.this, "投稿しました", Toast.LENGTH_SHORT).show();
                                    loadHomeTimeline(timelineView);
                                } else {
                                    Toast.makeText(HomeTimelineActivity.this, "投稿に失敗しました", Toast.LENGTH_SHORT).show();
                                }
                            }
                        };
                        asyncTask.execute(editPost.getText().toString());

                        editPost.setText("");
                        editPost.clearFocus();
                        inputMethodManager.hideSoftInputFromWindow(editPost.getWindowToken(), 0);
                        postArea.setVisibility(View.GONE);
                    } else {
                        editPost.clearFocus();
                        inputMethodManager.hideSoftInputFromWindow(editPost.getWindowToken(), 0);
                        postArea.setVisibility(View.GONE);
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_timeline, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadHomeTimeline(ListView listView) {
        TimelineLoader timelineLoader;

        timelineLoader = new TimelineLoader(twitter, listView);
        timelineLoader.execute();
    }


    class TimelineLoader extends AsyncTask<Void, Void, ResponseList<Status>> {
        Twitter twitter;
        ListView timelineView;

        public TimelineLoader(Twitter _twitter, ListView _timelineView) {
            twitter = _twitter;
            timelineView = _timelineView;
        }

        @Override
        protected ResponseList<twitter4j.Status> doInBackground(Void... params) {
            try {
                return twitter.getHomeTimeline();
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ResponseList<twitter4j.Status> statuses) {
            if (statuses != null) {
                TimelineAdapter timelineAdapter;
                timelineAdapter = (TimelineAdapter) timelineView.getAdapter();
                if (timelineAdapter == null) {
                    timelineAdapter = new TimelineAdapter(HomeTimelineActivity.this, 0, statuses);
                    timelineView.setAdapter(timelineAdapter);
                } else {
                    timelineAdapter.clear();
                    timelineAdapter.addAll(statuses);
                }
            } else {
                Toast.makeText(HomeTimelineActivity.this, "取得失敗", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
