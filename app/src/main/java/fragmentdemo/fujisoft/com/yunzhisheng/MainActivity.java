package fragmentdemo.fujisoft.com.yunzhisheng;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String VOICE_INPUT = "语音输入";
    private ArrayList<String> mFunctionsArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.status_bar_main);
        initFunctionArray();
        ((ListView) findViewById(R.id.lv_functions)).setAdapter(new FunctionsAdapter());
    }


    @Override
    public void onClick(View view) {
        Intent intent = null;
        Object tag = view.getTag();
        if (tag.equals(VOICE_INPUT)) {
            intent = new Intent(this,VoiceInputActivity.class);
        }
        if (intent != null) {
            startActivity(intent);
        }

    }


    private class FunctionsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mFunctionsArray.size();
        }

        @Override
        public Object getItem(int arg0) {
            return arg0;
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.button_list_item, null);
                holder = new ViewHolder();
                holder.btn = (Button) convertView.findViewById(R.id.btn);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.btn.setText(mFunctionsArray.get(position));
            holder.btn.setTag(mFunctionsArray.get(position));
            holder.btn.setOnClickListener(MainActivity.this);
            return convertView;
        }
    }

    public final class ViewHolder {
        public Button btn;
    }
    private void initFunctionArray() {
        mFunctionsArray = new ArrayList<String>();
        mFunctionsArray.add(VOICE_INPUT);
    }
}
