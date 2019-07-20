package jp.project.dev.net;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * 非同期ネット接続
 * @author TAKA@はままつ
 *
 */
public class AsyncNetConnect extends Handler implements Runnable{
	/** プログレス表示 */
	private boolean isShowProgress = true;
	/** プログレスダイアログ */
	private ProgressDialog progressDlg = null;
	/** コンテキスト */
	private Context context = null;
	/** ハンドラメッセージデータ */
	private Message handleMessageData = null;
	/** 接続対象URL */
	private String targetURL = null;
	/** CookieStore */
	private static CookieStore store = null;
	/** 接続処理が終了した際に呼び出されるリスナー */
	private OnAsyncNetConnected listner = null;

	/**
	 * コンストラクタ
	 * @param isShowProgress プログレスダイアログを表示するかどうか
	 * @param context コンテキスト
	 */
	public AsyncNetConnect(boolean isShowProgress, Context context) {
		this.isShowProgress = isShowProgress;
		this.context = context;
	}

	/**
	 * リスナーを設定
	 * @param listner　OnAsyncNetConnected
	 */
	public void setOnAsyncNetConnected(OnAsyncNetConnected listner){
		this.listner = listner;
	}
	
	/**
	 * 通信開始
	 * @param url　接続先URL
	 * @param prmMap パラメタ。?key=valueになる
	 */
	public void start(String url, Map<String, String> prmMap){
		System.out.println("call start " + url);
		if(listner == null){
			throw new NullPointerException("OnAsyncNetConnectedリスナーを設定してください。");
		}
		// URL組み立て
		StringBuilder sb = new StringBuilder(url);
		
		if(prmMap != null){
			Set<Entry<String, String>> entrySet = prmMap.entrySet();
			Iterator<Entry<String, String>> ite = entrySet.iterator();
			Entry<String, String> entry = null;
			boolean isFirst = true;
			while(ite.hasNext()){
				entry = ite.next();
				if(!isFirst){
					sb.append("&");
				}
				isFirst = false;
				sb.append(entry.getKey());
				sb.append("=");
				if(entry.getValue() == null){
					sb.append("null");
				}else{
					sb.append(URLEncoder.encode(entry.getValue()));
				}
			}
		}
		targetURL = sb.toString();
		
		if (isShowProgress) {
			progressDlg = new ProgressDialog(context);
			progressDlg.setTitle("通信中");
			progressDlg.setIndeterminate(false);
			progressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDlg.show();
		}
		handleMessageData = new Message();
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		System.out.println("call run");
		Bundle sendData = handleMessageData.getData();
		DefaultHttpClient client = new DefaultHttpClient();
		client.setCookieStore(store);
		// 接続のタイムアウト（単位：ms）
		HttpParams params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		// データ取得のタイムアウト（単位：ms）サーバ側のプログラム(phpとか)でsleepなどを使えばテストできる
		HttpConnectionParams.setSoTimeout(params, 10000);
		HttpGet method = new HttpGet(targetURL);
		// ヘッダを設定する
		method.setHeader("Connection", "Keep-Alive");
		HttpResponse response = null;
		String res = null;
		try {
			response = client.execute(method);
			store = client.getCookieStore();
			int status = response.getStatusLine().getStatusCode();
			if (status == HttpStatus.SC_OK) {
				res = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
			sendData.putString("Response", res);
			sendData.putString("Message", null);
		} catch (ClientProtocolException e) {
			sendData.putString("Message", e.getMessage());
		} catch (IOException e) {
			sendData.putString("Message", e.getMessage());
		} finally {
			if (isShowProgress) {
				progressDlg.dismiss();
			}
			if (!this.sendMessage(handleMessageData)) {
			}
		}
	}

	/**
	 * ネットワーク接続メイン処理
	 * @see android.os.Handler#handleMessage(android.os.Message)
	 */
	@Override
	public final void handleMessage(Message msg) {
		System.out.println("call handleMessage");
		Bundle sendData = null;
		String res = null;
		String message = null;
		AlertDialog.Builder alertDialog = null;

		try{
			if (isShowProgress) {
				progressDlg.dismiss();
			}
			sendData = msg.getData();
			res = sendData.getString("Response");
			message = sendData.getString("Message");
			if (message != null) {
				alertDialog = new AlertDialog.Builder(context);
				alertDialog.setTitle("エラー");
				alertDialog.setMessage(message);
				alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
			} else {
			}
		}catch(Exception e){
		}finally{
			if(listner != null){
				listner.onNetConected(res, targetURL);
			}
		}
	}
}
