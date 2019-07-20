package jaghama.sound;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author TAKA@はままつ
 */
public class MediaSpinnerAdapter extends BaseAdapter {
	private List<String> dirList = null;
	private Context context = null;
	public MediaSpinnerAdapter(Context context) {
		dirList = new ArrayList<String>();
		this.context = context;
	}

	@Override
	public int getCount() {
		return dirList.size();
	}

	@Override
	public Object getItem(int position) {
		return dirList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void add(String str){
		dirList.add(str);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		convertView = inflater.inflate(R.layout.mediaitem, null);
		TextView txtFileName = (TextView)convertView.findViewById(R.id.txtItem);
		txtFileName.setTextColor(Color.BLACK);
		txtFileName.setText(dirList.get(position));
		return convertView;
	}

}
