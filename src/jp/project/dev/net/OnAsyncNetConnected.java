package jp.project.dev.net;

/**
 * AsyncNetConnect用リスナー
 * @author TAKA@はままつ
 *
 */
public interface OnAsyncNetConnected {
	/**
	 * AsyncNetConnectでネットワーク接続が終了した際に呼び出されます
	 * @param response レスポンス
	 * @param url URL
	 */
	public abstract void onNetConected(String response, String url);
}
