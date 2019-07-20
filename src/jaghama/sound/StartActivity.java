package jaghama.sound;

import android.content.Intent;
import jp.project.dev.logo.AbstractLogoActivity;

/**
 * 
 * @author TAKA@はままつ
 * 
 *
 */
public class StartActivity extends AbstractLogoActivity {

	@Override
	public void onLogoEnd() {
		Intent intent = new Intent(this, JaghamaActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

}
