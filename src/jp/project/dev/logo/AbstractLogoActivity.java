package jp.project.dev.logo;

import jaghama.sound.R;
import android.app.Activity;
import android.os.Bundle;

/**
 * ロゴ表示用Activity　抽象クラス
 * @author TAKA@はままつ
 * 
 *
 */
public abstract class AbstractLogoActivity extends Activity implements OnLogoEnd {
	/**
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo);
		LogoView view = (LogoView)findViewById(R.id.logoView);
		view.setOnLogoEndFunc(this);
		view.init();
	}
}
