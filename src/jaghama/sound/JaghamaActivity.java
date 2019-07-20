package jaghama.sound;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import jp.project.dev.net.AsyncNetConnect;
import jp.project.dev.net.OnAsyncNetConnected;
import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * 日本Androidの会　浜松支部 第14回　勉強ソース
 * @author TAKA@はままつ
 *
 */
public class JaghamaActivity extends Activity implements OnItemSelectedListener, OnClickListener, OnAsyncNetConnected, OnCompletionListener, OnErrorListener, OnPreparedListener{
	/**
	 * 接続オブジェクト
	 */
	private AsyncNetConnect connect = null;
	/**
	 * MediaPlayerオブジェクト
	 */
	private MediaPlayer mplayer = null;

	/**
	 * 接続先。変数名はHOSTがついてるけど、HOSTじゃないね
	 */
	private static String URL_HOST = "http://taka-hama.sakura.ne.jp/";
//	private static String URL_HOST = "http://10.0.2.2/sakura/";
	/**
	 * ベースとなるURL
	 */
	private static String URL_BASE = URL_HOST +"jaghama_meeting/20120609/";
	/**
	 * メディアのリストを取得するURL
	 */
	private static String URL_GETMEDIALIST = URL_BASE + "getmedialist.php?dirName=";

	/**
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Spinner spMedia = (Spinner)findViewById(R.id.spMedia);
        Spinner spFiles = (Spinner)findViewById(R.id.spFiles);
        Button btnStop = (Button)findViewById(R.id.btnStop);
        spMedia.setPrompt("以下のリストより選択して下さい。");
        spFiles.setPrompt("以下のリストより選択して下さい。");

        btnStop.setOnClickListener(this);
        spFiles.setOnItemSelectedListener(this);
        spMedia.setOnItemSelectedListener(this);

        MediaSpinnerAdapter adapter = new MediaSpinnerAdapter(this);
        adapter.add("MediaPlayer");
        adapter.add("Movie");
        spMedia.setAdapter(adapter);
        
		connect = new AsyncNetConnect(true, this);
		connect.setOnAsyncNetConnected(this);
		
		VideoView video = (VideoView)findViewById(R.id.viVideo);
		video.setOnCompletionListener(this);
		video.setOnPreparedListener(this);
    }

	/**
	 * コンボボックス選択
	 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch(parent.getId()){
		case R.id.spMedia:
			updateMediaList();
			break;
		case R.id.spFiles:
	        Spinner spMedia = (Spinner)findViewById(R.id.spMedia);
	        Spinner spFiles = (Spinner)findViewById(R.id.spFiles);
			String dir = spMedia.getSelectedItem().toString().toLowerCase();
			StringBuffer sbURL = new StringBuffer(URL_BASE);

			sbURL.append(dir);
			sbURL.append("/");
			sbURL.append(URLEncoder.encode(spFiles.getSelectedItem().toString()));
			Uri uri = Uri.parse(sbURL.toString()) ;
			System.out.println(sbURL.toString());
			System.out.println(uri.toString());
			
			if("mediaplayer".equals(dir)){
				try {
					// データソース
					if(mplayer != null){
						mplayer.release();
					}
					mplayer = new MediaPlayer();
					mplayer.setOnErrorListener(this);
					mplayer.setOnCompletionListener(this);
					mplayer.setOnPreparedListener(this);
					mplayer.setDataSource(this, uri);
					// 再生準備
			        spMedia.setEnabled(false);
			        spFiles.setEnabled(false);
			        mplayer.prepareAsync();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else if("movie".equals(dir)){
				VideoView video = (VideoView)findViewById(R.id.viVideo);
				// 動画のURIを設定
				video.setVideoURI(uri);
		        spMedia.setEnabled(false);
		        spFiles.setEnabled(false);
		        //video.start();
			}
			break;
		}
	}

	/**
	 * 
	 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
	 */
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	/**
	 * コンボボックス更新処理
	 */
	private void updateMediaList(){
		Spinner dir = (Spinner)findViewById(R.id.spMedia);
		String dirName = dir.getSelectedItem().toString().toLowerCase();
		// メディア一覧を取得する
		connect.start(URL_GETMEDIALIST + dirName, null);
	}

	/**
	 * 接続終了
	 * @see jp.project.dev.net.OnAsyncNetConnected#onNetConected(java.lang.String, java.lang.String)
	 */
	@Override
	public void onNetConected(String response, String url) {
		// ネットワーク接続が終わった時の処理
		// JSONで返却されるので、デコードする
		System.out.println(response);
		JSONTokener json = new JSONTokener(response);
		Object obj = null;
		try{
			while(response != null && json != null && json.more()){
				obj = json.nextValue();
				if(obj  == null){
					System.out.println("NULL");
				}else if(obj instanceof JSONArray){
					// 配列が返ってきたな場合
					System.out.println("JSONArray");
					JSONArray array = (JSONArray)obj;
					int len = array.length();

					String[] split = url.split("/");
					String fileName = split[split.length - 1];
					int  pos = fileName.indexOf("?");
					if(pos != -1){
						fileName = fileName.substring(0, pos);
					}
					
					// メディアリストを更新
			        MediaSpinnerAdapter adapter = new MediaSpinnerAdapter(this);
					for(int i = 0; i < len; i++){
						adapter.add(array.getString(i));
					}
			        Spinner spFiles = (Spinner)findViewById(R.id.spFiles);
			        spFiles.setAdapter(adapter);
				}else if(obj instanceof JSONObject){
					System.out.println("JSONObject");
				}else{
					System.out.println(obj.toString());
				}
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
	}

	/**
	 * ボタンのクリック
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
        Spinner spMedia = (Spinner)findViewById(R.id.spMedia);
        Spinner spFiles = (Spinner)findViewById(R.id.spFiles);
		String dir = spMedia.getSelectedItem().toString().toLowerCase();
		VideoView video = (VideoView)findViewById(R.id.viVideo);
		switch(v.getId()){
		case R.id.btnStop:
			// 停止ボタン
			if("mediaplayer".equals(dir)){
				if(mplayer != null){
					// 再生停止
					mplayer.stop();
				}
			}else if("movie".equals(dir)){
				// 再生停止
				video.stopPlayback();
			}
	        spMedia.setEnabled(true);
	        spFiles.setEnabled(true);
			break;
		}
	}

	/**
	 * エラー
	 * @see android.media.MediaPlayer.OnErrorListener#onError(android.media.MediaPlayer, int, int)
	 */
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Toast.makeText(this, "Error", Toast.LENGTH_SHORT);
		return false;
	}

	/**
	 * 再生終了
	 * @see android.media.MediaPlayer.OnCompletionListener#onCompletion(android.media.MediaPlayer)
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
        Spinner spMedia = (Spinner)findViewById(R.id.spMedia);
        Spinner spFiles = (Spinner)findViewById(R.id.spFiles);
        spMedia.setEnabled(true);
        spFiles.setEnabled(true);
	}

	/**
	 * 再生準備完了
	 * @see android.media.MediaPlayer.OnPreparedListener#onPrepared(android.media.MediaPlayer)
	 */
	@Override
	public void onPrepared(MediaPlayer mp) {
        Spinner spMedia = (Spinner)findViewById(R.id.spMedia);
 		String dir = spMedia.getSelectedItem().toString().toLowerCase();
		if("mediaplayer".equals(dir)){
			try {
				// 再生
				mplayer.start();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}else if("movie".equals(dir)){
			VideoView video = (VideoView)findViewById(R.id.viVideo);
			// 再生
			video.start();
		}
	}
}