package com.example.pullcity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
////////////////////////////////////////////////////ddddddddddddddddddddddddddddddddddddddddddddddddddddddddd/////////////////////
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;

public class AsyncJSONLoader extends AsyncTaskLoader<JSONObject> {
	/**LogTag用単純クラス名取得*/
    private static final String TAG = AsyncJSONLoader.class.getSimpleName();
    /**通信URL*/
    private final String urlStr;
    /**結果保存用*/
    private JSONObject result;
    /*コンストラクタでContextとURLを受け取る*/    
    public AsyncJSONLoader(Context context, String urlStr) {
        super(context);
        this.urlStr = urlStr;
    }
    /*
     * バックグラウンドで行う処理をここに記入する。
     * この部分の処理が終了後に
     * 呼び出した側のonLoadFinishedが呼ばれる。
     * 引数に指定した型で値を返す必要がある。
     */
    @Override
    public JSONObject loadInBackground() {
        /*URL生成*/
    	URL url;
        try {
            url = new URL(this.urlStr);
        } catch (MalformedURLException e) {//URLとして無効な場合
            Log.e(TAG, "invalid URL : " + this.urlStr, e);
            return null;
        }
        /*通信部分*/
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "close");
            conn.setFixedLengthStreamingMode(0);
            conn.connect();
            /*通信コード取得（状態を確認する）*/
            int code = conn.getResponseCode();
            Log.d(TAG, "Responce code : " + code);
            if (code != 200) {//200番以外は失敗
                Log.e(TAG, "HTTP GET Error : code=" + code);
                return null;
            }
            String content = readContent(conn);
            return TextUtils.isEmpty(content) ? null : new JSONObject(content);//空の場合NULL、実態がある場合JSONObjectを返す
        } catch (IOException e) {
            Log.e(TAG, "Failed to get content : " + url, e);
            return null;
        } catch (JSONException e) {
            Log.e(TAG, "invalid JSON String", e);
            return null;
        } finally {
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception ignore) {
                }
            }
        }
    }
	/**HttpURLConnectionで得た内容をバイト変換してString型で返すメソッド*/
    private String readContent(HttpURLConnection conn) throws IOException {
        String charsetName;

        String contentType = conn.getContentType();
        if (! TextUtils.isEmpty(contentType)) {
            int idx = contentType.indexOf("charset=");
            if (idx != -1) {
                charsetName = contentType.substring(idx + "charset=".length());
            } else {
                charsetName = "UTF-8";
            }
        } else {
            charsetName = "UTF-8";
        }

        InputStream is = new BufferedInputStream(conn.getInputStream());

        int length = conn.getContentLength();
        ByteArrayOutputStream os = length > 0 ? new ByteArrayOutputStream(length) : new ByteArrayOutputStream();

        byte[] buff = new byte[10240];
        int readLen;
        while ((readLen = is.read(buff)) != -1) {
            if (isReset()) {
                return null;
            }

            if (readLen > 0) {
                os.write(buff, 0, readLen);
            }
        }

        return new String(os.toByteArray(), charsetName);
    }

    @Override
    public void deliverResult(JSONObject data) {
        if (isReset()) {
            if (this.result != null) {
                this.result = null;
            }
            return;
        }

        this.result = data;

        if (isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (this.result != null) {
            deliverResult(this.result);
        }
        if (takeContentChanged() || this.result == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
    }

    public String getUrlStr() {
        return urlStr;
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        writer.print(prefix); writer.print("urlStr="); writer.println(this.urlStr);
        writer.print(prefix); writer.print("result="); writer.println(this.result);
    }

}
