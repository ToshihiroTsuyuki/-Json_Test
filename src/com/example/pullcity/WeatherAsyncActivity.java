package com.example.pullcity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class WeatherAsyncActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<JSONObject>, OnClickListener, OnItemSelectedListener {
	/**Bundle保存（取出）用KEY*/
	private static final String KEY_URL_STR = "urlStr";
	/**取得用URL*/
	private static final String WEATHER_API_URL = "http://weather.livedoor.com/forecast/webservice/json/v1";
	public static final String ASSETS_URL = "https://api.github.com/repos/sekaiya/iena/commits";
	static private final String ASSET_PATH = "Untitled.txt";

	private Spinner mCitySpinner;	//スピナーを宣言
	private String[] mCity;			//都市名を入れる配列
	private String[] mCodes;		//都市コードを入れる配列
	String mCode;					//選択された都市コードを入れる変数
	JSONObject jsonObject;			//JSONObject型の変数をここで先に宣言。

	@Override
	protected void onCreate(Bundle saved) {
		super.onCreate(saved);
		setContentView(R.layout.weather_async);

		findViewById(R.id.btn_referesh).setOnClickListener(this);
		mCitySpinner = (Spinner) findViewById(R.id.spinner_city);
		mCitySpinner.setOnItemSelectedListener(this);

		//別ファイルで作られているCityReader.javaのクラスを使って、
		//CityItem.javaクラスで定義されているCityItem型の配列を宣言する。
		final List<CityItem> cityItems = CityReader.getCityList();

		//Spinnerに都市名とidをセットする為に、上で作った配列から
		//都市名とidそれぞれのString型の配列を宣言する。
		//配列の長さは上で作成したcityItemsと同じとする。
		mCity = new String[cityItems.size()];
		mCodes = new String[cityItems.size()];

		//for文を使い、CityItemのgetterで値を順番に代入。
		for (int i = 0; i < cityItems.size(); i++) {
			mCity[i] = cityItems.get(i).getCity();
			mCodes[i] = cityItems.get(i).getCode();
		}

		//都市名の配列mCityをSpinnerに値をセットする為のArrayAdapterを宣言する。
		ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mCity);

		//上で作成したArrayAdapterをDropDownViewResourceとしてセットする。
		ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		//上でセットしたアダプタをSpinnerに設定する。
		mCitySpinner.setAdapter(ad);

		//アプリ起動時に現在選択されているSpinnerの位置をint型で保存。
		int selectPosition = mCitySpinner.getSelectedItemPosition();

		//ネットに接続されていなくて、上記で配列が作成されていない場合、
		//Spinnerの位置の値は -1になってしまうので、その場合データが無いとみなし、
		//if文で分岐させ、TextViewのtitleViewを使ってメッセージを表示させる。
		if (selectPosition < 0) {
			TextView titleView = (TextView) findViewById(R.id.text_title);
			titleView.setText("データの取得に失敗しました。");
		} else {

			//配列がきちんとSpinnerにセットされている場合は、
			//そのまま選択されたSpinnerの開始位置の都市のidを取得する。
			mCode = mCodes[selectPosition];
		}

	}
	/*
	 * Loaderが正しく生成されたときに呼び出される。
	 */
	@Override
	public Loader<JSONObject> onCreateLoader(int id, Bundle args) {
		ProgressDialogFragment dialog = new ProgressDialogFragment();	//プログレスダイアログ表示
		Bundle pa = new Bundle();
		pa.putString("message", "データを読み込んでいます。");
		dialog.setArguments(pa);
		dialog.show(getSupportFragmentManager(), "progress");

		String urlStr = args.getString(KEY_URL_STR);					//KEY情報を基にBundle内のURLを取り出す。
		if (! TextUtils.isEmpty(urlStr)) {
			return new AsyncJSONLoader(getApplication(), urlStr);		//メインアクティビティのContextを渡す。
		}
		return null;
	}

	/*
	 * loader内の処理が終了したときに呼び出される。
	 */
	@Override
	public void onLoadFinished(Loader<JSONObject> loader, JSONObject data) {
		/*プログレスダイアログを閉じる */
		ProgressDialogFragment dialog = (ProgressDialogFragment)getSupportFragmentManager().findFragmentByTag("progress");
		if (dialog != null) {//ダイアログが存在している時のみ消す
			dialog.onDismiss(dialog.getDialog());
		}
		/*ここで取得したJSONObjectを変数に代入して保持。*/
		jsonObject = data;
	}

	/*
	 *　loaderがリセットされた時に呼び出される。
	 */
	@Override
	public void onLoaderReset(Loader<JSONObject> data) {
		// 特に何もしない
	}

	//取得ボタンが押された場合の処理。
	public void onClick(View v) {
		if (v.getId() == R.id.btn_referesh) {

			/** assetsの中のデータ取得
			 * 
			 */
			//ここからassetsのデータを読み込む
			AssetManager as = getResources().getAssets(); 
			InputStream is = null;  
			BufferedReader br = null;  
			StringBuilder sb = new StringBuilder();   
			try{  
				try {            	
					is = as.open(ASSET_PATH);
					br = new BufferedReader(new InputStreamReader(is));   
					String str;     
					while((str = br.readLine()) != null){     
						sb.append(str +"\n");

					}      
				} finally {  
					if (br != null) br.close();  
				}  
			} catch (IOException e) {  //assetsのデータ取得に失敗した時に表示
				e.printStackTrace();
				Toast.makeText(this, "読み込み失敗", Toast.LENGTH_SHORT).show();  
			} 
			String assetString =  new String(sb);
			//assetsの中のデータをJSONObjectに入れて、getStringで取り出す
			try {
				JSONObject assetObject = new JSONObject(assetString);
				String a_title = assetObject.getString("title");
				String a_created_at = assetObject.getString("created_at");

				TextView a_titleView = (TextView) findViewById(R.id.a_2);
				TextView a_created_atView = (TextView) findViewById(R.id.a_3);
				a_titleView.setText("【title】\n"+a_title);
				a_created_atView.setText("【a_created_at】\n"+a_created_at);

			} catch (JSONException e2) {
				// TODO 自動生成された catch ブロック
				e2.printStackTrace();
			}

			TextView label = (TextView)this.findViewById(R.id.label);  
			label.setText("【全体】\n"+sb.toString());





			/** 天気情報の取得
			 * 
			 */

			//取得した値を入れる変数
			String title = "";	//都市名。

			//description は詳細な天気情報だが、今回は使用しない為コメントアウト。
			//			String description = "";
			String[] datelabel;	//今日とか明日とか明後日の表示。
			String[] telop;	//晴れとか雨とか曇りの表示。
			String[] date;	//日付。
			int maxcelsius, mincelsius;	//最高気温と最低気温。
			String[] celsius;	//最高気温と最低気温から平均気温を取得して代入する変数。
			//String[] imagepath; //画像のフルパス。

			//ここからJSONの記述内容を変数に代入していく。
			try {
				title = jsonObject.getString("title");
				String title2 = jsonObject.getString("title");
				//この下の二行も今回は descriptionは使わない為、コメントアウト。
				//JSONObject descObj = jsonObject.getJSONObject("description");
				//description = descObj.getString("text"); 

				//forecastsの情報は、さらに入れ子構造になっていて、
				//しかも同じ要素の名前があるので、それらを配列型で変数に代入。
				JSONArray forecasts = jsonObject.getJSONArray("forecasts");

				//ここで一旦配列の長さをint型で取得しておく。後に使う為。
				int arraysize = forecasts.length();

				//３日分の情報を代入する為、配列型で変数宣言。
				datelabel = new String[arraysize];
				telop = new String[arraysize];
				date = new String[arraysize];
				celsius = new String[arraysize];
				//imagepath = new String[arraysize];

				//for文で回しながら変数に代入。
				for (int i = 0; i < arraysize; i++) {
					//JSONArray型の配列情報を0から取得。大体明後日まであるので0～2の3回って取得することになる。
					JSONObject foreObj = forecasts.getJSONObject(i);
					datelabel[i] = foreObj.getString("dateLabel");
					telop[i] = foreObj.getString("telop");
					date[i] = foreObj.getString("date");

					//気温に関してはさらに入れ子構造になっているので、
					//一旦JSONOBjectを作成してからその中の値を取得。
					JSONObject tempObj = foreObj.getJSONObject("temperature");

					//気温に関しては最高気温だけ、もしくは最低気温だけの場合はその数値、
					//そうでない場合は最高と最低を2で割って平均気温を出す。
					if (tempObj.get("max") == JSONObject.NULL) {
						maxcelsius = 0;
					} else {
						JSONObject maxObj = tempObj.getJSONObject("max");
						maxcelsius = maxObj.getInt("celsius");
					}
					if (tempObj.get("min") == JSONObject.NULL) {
						mincelsius = 0;
					} else {
						JSONObject minObj = tempObj.getJSONObject("min");
						mincelsius = minObj.getInt("celsius");
					}
					if (maxcelsius == 0 && mincelsius == 0) {
						celsius[i] = "？";
					} else if (maxcelsius != 0 && mincelsius == 0) {
						celsius[i] = String.valueOf(maxcelsius);
					} else if (maxcelsius == 0 && mincelsius != 0) {
						celsius[i] = String.valueOf(mincelsius);
					} else {
						celsius[i] = String.valueOf((maxcelsius + mincelsius) / 2);
					}

					//画像もさらに入れ子構造になっている中にあるので、一旦JSONObjectを作成。
					JSONObject imgObj = foreObj.getJSONObject("image");
					//imagepath[i] = imgObj.getString("url");
				}
				//表示用のTextView、ImageViewを用意。
				TextView titleView = (TextView) findViewById(R.id.text_title);

				//天気の詳細内容は今回使用しないのでコメントアウト。
				//TextView descriptionView = (TextView) findViewById(R.id.text_description);

				TextView datelabelView1 = (TextView) findViewById(R.id.text_datelabel1);				
				TextView telopView1 = (TextView) findViewById(R.id.text_telop1);
				TextView celsiusView1 = (TextView) findViewById(R.id.text_celsius1);
				TextView dateView1 = (TextView) findViewById(R.id.text_date1);
				ImageView imgView1 = (ImageView) findViewById(R.id.imageView1);
				TextView datelabelView2 = (TextView) findViewById(R.id.text_datelabel2);
				TextView telopView2 = (TextView) findViewById(R.id.text_telop2);
				TextView celsiusView2 = (TextView) findViewById(R.id.text_celsius2);
				TextView dateView2 = (TextView) findViewById(R.id.text_date2);
				ImageView imgView2 = (ImageView) findViewById(R.id.imageView2);
				TextView datelabelView3 = (TextView) findViewById(R.id.text_datelabel3);
				TextView telopView3 = (TextView) findViewById(R.id.text_telop3);
				TextView celsiusView3 = (TextView) findViewById(R.id.text_celsius3);
				TextView dateView3 = (TextView) findViewById(R.id.text_date3);
				ImageView imgView3 = (ImageView) findViewById(R.id.imageView3);

				TextView[] dlv = new TextView[3];
				dlv[0] = datelabelView1;
				dlv[1] = datelabelView2;
				dlv[2] = datelabelView3;
				TextView[] tv = new TextView[3];
				tv[0] = telopView1;
				tv[1] = telopView2;
				tv[2] = telopView3;
				TextView[] cv = new TextView[3];
				cv[0] = celsiusView1;
				cv[1] = celsiusView2;
				cv[2] = celsiusView3;
				TextView[] dv = new TextView[3];
				dv[0] = dateView1;
				dv[1] = dateView2;
				dv[2] = dateView3;
				ImageView[] iv = new ImageView[3];
				iv[0] = imgView1;
				iv[1] = imgView2;
				iv[2] = imgView3;

				//title、つまり都市名はそのまま代入。
				titleView.setText(title);

				//それ以外は１日分か２日分、もしくは３日分等の違いに対処出来る様にfor文で代入していく。
				for (int i = 0; i < arraysize; i++) {
					dlv[i].setText(datelabel[i] + ":");
					tv[i].setText(telop[i] + " ");
					cv[i].setText("平均気温" + celsius[i] + "℃ ");
					dv[i].setText(date[i]);

					//画像に関してはネットでhttpConnectionを使ったgetBitmapFromURLメソッドを
					//使って下さいとの表記と共に発見したので有難く使用させていただく。
					//iv[i].setImageBitmap(getBitmapFromURL(imagepath[i]));
				}

			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	//ネットから拝借したURLからBitmapへの変換メソッド。
	public static Bitmap getBitmapFromURL(String src) {  
		try {  
			URL url = new URL(src);  
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
			connection.setDoInput(true);  
			connection.connect();  
			InputStream input = connection.getInputStream();  
			Bitmap myBitmap = BitmapFactory.decodeStream(input);  
			return myBitmap;  
		} catch (IOException e) {  
			e.printStackTrace();  
			return null;  
		}  
	}

	/*
	 * (non-Javadoc)
	 * スピナーのアイテムが変更された場合に実行される処理。
	 * 引数のposirion(上から何番目か)を使用して、都市名から都市番号へ変換する
	 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
	 */
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		mCode = mCodes[position];
		/*AsyncTaskLoaderに値を渡す為にBundleに情報を入れる*/
		Bundle args = new Bundle(1);

		/*
		 * AsyncTaskLoaderを初期化後、起動する
		 * 第1引数と第2引数が下記Loaderの引数に渡される。
		 * onCreateLoader() に渡される id の値に応じて
		 * 戻り値にする Loader<T> の インスタンスを切り替えたり、
		 * Loader<T> インスタンスの初期化に必要なパラメーターを argsで渡すことができる
		 * 
		 * */
		args.putString(KEY_URL_STR, WEATHER_API_URL + "?city=" + mCode);
		//args.putString(KEY_URL_STR,ASSETS_URL);
		getSupportLoaderManager().restartLoader(0, args, this);
	}

	public void onNothingSelected(AdapterView<?> parent) {
		/* NOP */
	}


}