package fragmentdemo.fujisoft.com.yunzhisheng;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
import com.unisound.client.SpeechUnderstanderListener;

import org.json.JSONArray;
import org.json.JSONObject;

public class VoiceInputActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * 当前识别状态
     */
    enum AsrStatus {
        idle, recording, recognizing
    }

    public static final int ASR_ONLINE_TYPE = 0;
    public static final int ASR_ONLINE_WEIXIN_TYPE = 2;

    private ProgressBar mVolume;
    private EditText mResultTextView;
    private Button mRecognizerButton;
    private View mStatusView;
    private View mStatusLayout;
    private ImageView mLogoImageView;
    private TextView mStatusTextView;
    private Button mDomainButton;
    private Button mSampleButton;
    private Dialog mDomainDialog;
    private Dialog mSampleDialog;

    private static String arraySampleStr[] = new String[] { "RATE_AUTO  ",
            "RATE_16K  ", "RATE_8K  " };
    private static String arrayDomain[] = new String[] { "general", "poi",
            "song", "movietv", "medical" };
    private static String arrayDomainChina[] = new String[] { "通用识别  ",
            "地名识别  ", "歌名识别  ", "影视名识别  ", "医药领域识别  " };
    private static int arraySample[] = new int[] { SpeechConstants.ASR_SAMPLING_RATE_BANDWIDTH_AUTO,
            SpeechConstants.ASR_SAMPLING_RATE_16K, SpeechConstants.ASR_SAMPLING_RATE_8K };
    private static int currentSample = 0;
    private static int currentDomain = 0;

    private AsrStatus statue = AsrStatus.idle;
    private SpeechUnderstander mUnderstander;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voiceinput);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.status_bar_main);

        initData();

        // 初始化识别对象
        initRecognizer();
    }

    /**
     * 初始化识别
     */
    private void initRecognizer() {

        // 创建识别对象，appKey通过 http://dev.hivoice.cn/ 网站申请
        mUnderstander = new SpeechUnderstander(this, "f2cleiy2gwmut6vchkja34b3o4avxzyyst2nfwyo", "59bd450770b61242d9cbe60e1e2ee11e");
        //设置无语义结果
        mUnderstander.setOption(SpeechConstants.NLU_ENABLE, false);

        mUnderstander.setListener(new SpeechUnderstanderListener() {

            @Override
            public void onResult(int type, String jsonResult) {
                switch (type) {
                    case SpeechConstants.ASR_RESULT_NET:
                        // 在线识别结果，通常onResult接口多次返回结果，保留识别结果组成完整的识别内容。
                        log_v("onRecognizerResult");
                        // 通常onResult接口多次返回结果，保留识别结果组成完整的识别内容。
                        if (jsonResult.contains("net_asr")) {
                            try {
                                JSONObject json = new JSONObject(jsonResult);
                                JSONArray jsonArray = json.getJSONArray("net_asr");
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                String status = jsonObject.getString("result_type");
                                if (status.equals("partial")) {
                                    mResultTextView.append((String)jsonObject.get("recognition_result"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onEvent(int type, int timeMs) {
                switch (type) {
                    case SpeechConstants.ASR_EVENT_VAD_TIMEOUT:
                        // 说话音量实时返回
                        log_v("onVADTimeout");
                        // 收到用户停止说话事件，停止录音
                        stopRecord();
                        break;
                    case SpeechConstants.ASR_EVENT_VOLUMECHANGE:
                        // 说话音量实时返回
                        int volume = (Integer)mUnderstander.getOption(SpeechConstants.GENERAL_UPDATE_VOLUME);
                        mVolume.setProgress(volume);
                        break;
                    case SpeechConstants.ASR_EVENT_SPEECH_DETECTED:
                        //用户开始说话
                        log_v("onSpeakStart");
                        mStatusTextView.setText(R.string.speaking);
                        break;
                    case SpeechConstants.ASR_EVENT_RECORDING_START:
                        //录音设备打开，开始识别，用户可以开始说话
                        mStatusTextView.setText(R.string.please_speak);
                        mRecognizerButton.setEnabled(true);
                        statue = AsrStatus.recording;
                        mRecognizerButton.setText(R.string.say_over);
                        break;
                    case SpeechConstants.ASR_EVENT_RECORDING_STOP:
                        // 停止录音，请等待识别结果回调
                        log_v("onRecordingStop");
                        statue = AsrStatus.recognizing;
                        mRecognizerButton.setText(R.string.give_up);
                        mStatusTextView.setText(R.string.just_recognizer);
                        break;
                    case SpeechConstants.ASR_EVENT_USERDATA_UPLOADED:
                        log_v("onUploadUserData");
                        Toast.makeText(VoiceInputActivity.this,
                                R.string.info_upload_success, Toast.LENGTH_LONG)
                                .show();
                    case SpeechConstants.ASR_EVENT_NET_END:
                        mRecognizerButton.setText(R.string.click_say);
                        statue = AsrStatus.idle;
                        mStatusLayout.setVisibility(View.INVISIBLE);
                        break;
                }
            }

            @Override
            public void onError(int type, final String errorMSG) {
                if (errorMSG != null) {
                    // 显示错误信息
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mResultTextView.setText(errorMSG);
                        }

                    });
                } else {
                    if ("".equals(mResultTextView.getText().toString())) {
                        mResultTextView.setText(R.string.no_hear_sound);
                    }

                }
            }
        });
        mUnderstander.init("");
    }

    /**
     * 初始化控件
     */
    private void initData() {
        mVolume = (ProgressBar) findViewById(R.id.volume_progressbar);
        mResultTextView = (EditText) findViewById(R.id.result_textview);
        mResultTextView.setVisibility(View.INVISIBLE);
        mDomainButton = (Button) findViewById(R.id.domain_button);
        mSampleButton = (Button) findViewById(R.id.sample_button);
        mStatusView = findViewById(R.id.status_panel);
        mStatusTextView = (TextView) findViewById(R.id.status_show_textview);
        mStatusLayout = findViewById(R.id.status_layout);
        mLogoImageView = (ImageView) findViewById(R.id.logo_imageview);
        mDomainButton.setOnClickListener(this);
        mSampleButton.setOnClickListener(this);
        mDomainButton.setText(arrayDomainChina[0]);
        mSampleButton.setText(arraySampleStr[0]);

        mRecognizerButton = (Button) findViewById(R.id.recognizer_btn);

        // 识别领域
        mDomainDialog = new Dialog(this, R.style.dialog);
        mDomainDialog.setContentView(R.layout.domain_list_item);
        mDomainDialog.findViewById(R.id.medical_text).setOnClickListener(this);
        mDomainDialog.findViewById(R.id.general_text).setOnClickListener(this);
        mDomainDialog.findViewById(R.id.movietv_text).setOnClickListener(this);
        mDomainDialog.findViewById(R.id.poi_text).setOnClickListener(this);
        mDomainDialog.findViewById(R.id.song_text).setOnClickListener(this);
        // 采样率
        mSampleDialog = new Dialog(this, R.style.dialog);
        mSampleDialog.setContentView(R.layout.sample_list_item);
        mSampleDialog.findViewById(R.id.rate_16k_text).setOnClickListener(this);
        mSampleDialog.findViewById(R.id.rate_8k_text).setOnClickListener(this);
        mSampleDialog.findViewById(R.id.rate_auto_text)
                .setOnClickListener(this);

        mRecognizerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (statue == AsrStatus.idle) {
                    mResultTextView.setVisibility(View.VISIBLE);
                    mRecognizerButton.setEnabled(false);
                    mResultTextView.setText("");
                    mStatusView.setVisibility(View.VISIBLE);
                    mStatusLayout.setVisibility(View.VISIBLE);
                    mLogoImageView.setVisibility(View.GONE);
                    // 在收到 onRecognizerStart 回调前，录音设备没有打开，请添加界面等待提示，
                    // 录音设备打开前用户说的话不能被识别到，影响识别效果。
                    mStatusTextView.setText(R.string.opening_recode_devices);
                    // 修改识别领域
                    mUnderstander.setOption(SpeechConstants.ASR_DOMAIN, arrayDomain[currentDomain]);

                    mUnderstander.start();
                } else if (statue == AsrStatus.recording) {
                    stopRecord();
                } else if (statue == AsrStatus.recognizing) {

                    mUnderstander.cancel();

                    mRecognizerButton.setText(R.string.click_say);
                    statue = AsrStatus.idle;
                }
            }
        });
    }

    /**
     * 打印日志信息
     *
     * @param msg
     */
    private void log_v(String msg) {
        Log.v("demo", msg);
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        mStatusTextView.setText(R.string.just_recognizer);
        mUnderstander.stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.domain_button:
                mDomainDialog.show();
                break;

            case R.id.sample_button:
                mSampleDialog.show();
                break;

            case R.id.general_text:
                currentDomain = 0;
                setDomain(currentDomain);
                break;

            case R.id.poi_text:
                currentDomain = 1;
                setDomain(currentDomain);
                break;

            case R.id.song_text:
                currentDomain = 2;
                setDomain(currentDomain);
                break;

            case R.id.movietv_text:
                currentDomain = 3;
                setDomain(currentDomain);
                break;

            case R.id.medical_text:
                currentDomain = 4;
                setDomain(currentDomain);
                break;

            case R.id.rate_auto_text:
                currentSample = 0;
                setSample(currentSample);
                break;

            case R.id.rate_16k_text:
                currentSample = 1;
                setSample(currentSample);
                break;

            case R.id.rate_8k_text:
                currentSample = 2;
                setSample(currentSample);
                break;

            default:
                break;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mUnderstander != null) {
            mUnderstander.stop();
        }
    }

    private void setSample(int index) {
        mSampleButton.setText(arraySampleStr[index]);
        mSampleDialog.dismiss();
    }

    private void setDomain(int index) {
        mDomainButton.setText(arrayDomainChina[index]);
        mDomainDialog.dismiss();
    }
}
