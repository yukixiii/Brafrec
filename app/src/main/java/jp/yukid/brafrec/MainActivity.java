package jp.yukid.brafrec;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


public class MainActivity extends ActionBarActivity {
    private Twitter twitter;
    private RequestToken requestToken;
    private AccountListAdapter accountListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addAccountButton;
        ListView accountList;

        addAccountButton = (Button) findViewById(R.id.addAccount);
        accountList = (ListView) findViewById(R.id.accountList);

        TwitterAccounts.initTwitterInstances(this);

        accountListAdapter = new AccountListAdapter(this, 0, TwitterAccounts.accounts);
        accountList.setAdapter(accountListAdapter);

        addAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // OAuth認可を開始する
                startOAuth();
            }
        });

        accountList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // クリックしたアカウントのTwitterインスタンスを取得して、次のアクティビティに移動する
                Twitter selectedAccount;
                Intent intent;

                selectedAccount = TwitterAccounts.accounts.get(position);
                intent = new Intent(MainActivity.this, HomeTimelineActivity.class);

                intent.putExtra("twitter", selectedAccount);
                MainActivity.this.startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void startOAuth() {
        twitter = TwitterAccounts.getInstance(this);

        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    // RequestTokenを取得
                    requestToken = twitter.getOAuthRequestToken(getString(R.string.twitter_callback_url));
                    return requestToken.getAuthorizationURL();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String url) {
                if (url != null) {
                    // URLを開く 暗黙的Intent アプリはユーザー側で指定
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "認証失敗", Toast.LENGTH_SHORT).show();
                }
            }
        };
        asyncTask.execute();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String verifier = intent.getData().getQueryParameter("oauth_verifier");
        
        AsyncTask<String, Void, AccessToken> asyncTask = new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... params) {
                try {
                    return twitter.getOAuthAccessToken(requestToken, params[0]);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                if (accessToken != null) {
                    finishOAuth(accessToken);
                } else {
                    Toast.makeText(MainActivity.this, "認証失敗", Toast.LENGTH_SHORT).show();
                }

            }
        };
        asyncTask.execute(verifier);
    }

    public void finishOAuth(AccessToken accessToken) {
        TwitterAccounts.addAccount(accessToken, twitter);

        // ListViewの更新
        // accountListAdapter.add(twitter);
        accountListAdapter.notifyDataSetChanged();
    }
}
