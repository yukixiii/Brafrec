package jp.yukid.brafrec;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;

import twitter4j.Status;

/**
 * Created by yuki on 15/02/27.
 */
public class TimelineAdapter extends ArrayAdapter<Status> {
    private LayoutInflater layoutInflater;
    private Context mContext;

    public TimelineAdapter(Context context, int resource, List<Status> objects) {
        super(context, resource, objects);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Status status;
        ImageView icon;
        TextView header;
        TextView text;
        TextView footer;
        BitmapWorkerTask bitmapWorkerTask;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.timeline, null);
        }

        status = getItem(position);

        header = (TextView) convertView.findViewById(R.id.header);
        text = (TextView) convertView.findViewById(R.id.text);
        footer = (TextView) convertView.findViewById(R.id.footer);

        header.setText(status.getUser().getName() + " " + "@" + status.getUser().getScreenName());
        text.setText(status.getText());
        footer.setText(status.getCreatedAt().toString() + " " + "via" + " " + Jsoup.parse(status.getSource()).text());

        // アイコンのセット処理
        icon = (ImageView) convertView.findViewById(R.id.icon);
        loadBitmap(mContext, status, icon, null);

        /*
        bitmapWorkerTask = new BitmapWorkerTask(icon, status.getId());
        AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), null, bitmapWorkerTask);
        icon.setImageDrawable(asyncDrawable);

        bitmapWorkerTask.execute(status.getUser().getProfileImageURL());
         */

        return convertView;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public static boolean cancelPotentialWork(long statusId, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final long bitmapData = bitmapWorkerTask.statusId;
            if (bitmapData != statusId) {
                // 以前のタスクをキャンセル
                bitmapWorkerTask.cancel(true);
            } else {
                // 同じタスクがすでに走っているので、このタスクは実行しない
                return false;
            }
        }
        // この ImageView に関連する新しいタスクを実行する
        return true;
    }


    public void loadBitmap(Context context, Status status, ImageView imageView, Bitmap loadingBitmap) {
        // 同じタスクが走っていないか、同じ ImageView で古いタスクが走っていないかチェック
        if (cancelPotentialWork(status.getId(), imageView)) {
            final BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(imageView, status.getId());
            final AsyncDrawable asyncDrawable = new AsyncDrawable(context.getResources(), loadingBitmap, bitmapWorkerTask);
            imageView.setImageDrawable(asyncDrawable);
            bitmapWorkerTask.execute(status.getUser().getProfileImageURL());
        }
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewWeakReference;
        long statusId;

        public BitmapWorkerTask(ImageView imageView, long _statusId) {
            imageViewWeakReference = new WeakReference<>(imageView);
            statusId = _statusId;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                InputStream input = new URL(params[0]).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                input.close();

                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // キャンセルされていたらなにもしない
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewWeakReference != null && bitmap != null) {
                ImageView imageView;
                imageView = imageViewWeakReference.get();

                if (imageView != null) {
                    // ImageViewからタスクを取り出す
                    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
                    if (this == bitmapWorkerTask && imageView != null) {
                        // 同じタスクならImageViewにBitmap をセット
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
}
