package com.nightn.minofo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UsebikeActivity extends AppCompatActivity {

    EditText codeEdit = null;
    TextView codeHint = null;
    TextView hint = null;
    Button checkCode = null;
    ImageView saoma = null;
    ImageView bgBike = null;

    LinearLayout inputLinear = null;
    LinearLayout showLinear = null;
    LinearLayout upLinear = null;

    String input = null;
    int index;

    TextView textCode1 = null;
    TextView textCode2 = null;
    TextView textCode3 = null;
    TextView textCode4 = null;

    TextView textSql = null;

    //数据库
    String sql;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usebike);

        codeEdit = (EditText)findViewById(R.id.code_edit);
        codeHint = (TextView)findViewById(R.id.code_hint);
        hint = (TextView)findViewById(R.id.hint);
        checkCode = (Button)findViewById(R.id.check_code);
        saoma = (ImageView)findViewById(R.id.saoma);
        bgBike = (ImageView)findViewById(R.id.bg_bike);
        inputLinear = (LinearLayout)findViewById(R.id.input_plate_linear);

        showLinear = (LinearLayout)findViewById(R.id.show_code_linear);

        textCode1 = (TextView)findViewById(R.id.code1);
        textCode2 = (TextView)findViewById(R.id.code2);
        textCode3 = (TextView)findViewById(R.id.code3);
        textCode4 = (TextView)findViewById(R.id.code4);

        //textSql = (TextView)findViewById(R.id.text_sql);


        //单独设置hint的大小
        SpannableString ss = new SpannableString("请输入车牌号");
        // 新建一个属性对象,设置文字的大小
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan(20,true);
        // 附加属性到文本
        ss.setSpan(ass, 0, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 设置hint
        codeEdit.setHint(new SpannedString(ss)); // 一定要进行转换,否则属性会消失


        //codeEdit输入发生变化的监听器
        codeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                input = codeEdit.getText().toString();
                if(!input.isEmpty()){
                    checkCode.setClickable(true);
                    checkCode.setBackgroundResource(R.drawable.checkt);
                    if(input.length() < 4){
                        codeHint.setText("车牌号一般为4-8位的数字");
                    }else{
                        codeHint.setText("温馨提示：若输错车牌号，将无法打开车锁。");
                    }
                }else{
                    checkCode.setClickable(false);
                    checkCode.setBackgroundResource(R.drawable.checkf);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        //确定按钮监听器
        checkCode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                input = codeEdit.getText().toString();
                if(input.length() < 4){
                    Toast.makeText(UsebikeActivity.this, "请输入正确的车牌号！", Toast.LENGTH_SHORT).show();
                    return;
                }

                //TODO 访问远程数据库，检验是否匹配
                String url = "http://www.cloudviewer.xyz";
                Utils.sendOkHttpRequest(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String temp = response.body().string(); //这句 final 很重要
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sql = temp;
                                //sql数据格式为：密码 + ":" + 车牌 + "分号"
                                index = sql.indexOf(":" + input);
                                if(index == -1){
                                    //复制到剪贴板
                                    copy(input, UsebikeActivity.this);
                                    //跳转至另一个APP的具体活动
                                    Intent intent = new Intent();
                                    Toast.makeText(UsebikeActivity.this, "已复制车牌号 " + input, Toast.LENGTH_SHORT).show();
                                    ComponentName cn = new ComponentName("so.ofo.labofo", "so.ofo.labofo.activities.EntryActivity");
                                    try {
                                        intent.setComponent(cn);
                                        intent.putExtra("车牌", input);
                                        startActivity(intent);
                                    } catch(Exception e) {
                                        //TODO  可以在这里提示用户没有安装应用或找不到指定Activity，或者是做其他的操作
                                        Toast.makeText(UsebikeActivity.this, "未安装 ofo 共享单车", Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }

                                }else{
                                    //TODO 这里验证 input 是否包含在云端数据库
                                    //获取并设置密码
                                    String code1 = String.valueOf(sql.charAt(index-4));
                                    String code2 = String.valueOf(sql.charAt(index-3));
                                    String code3 = String.valueOf(sql.charAt(index-2));
                                    String code4 = String.valueOf(sql.charAt(index-1));

                                    textCode1.setText(code1);
                                    textCode2.setText(code2);
                                    textCode3.setText(code3);
                                    textCode4.setText(code4);

                                    bgBike.setImageResource(R.drawable.unlock_bg_card);
                                    hint.setText("车牌号 " + input + " 的解锁码");
                                    hint.setTextSize(20);
                                    hint.setBackgroundResource(R.drawable.bg_white);
                                    codeHint.setText("骑行结束后，记得在手机上结束行程");
                                    inputLinear.setVisibility(View.GONE);
                                    showLinear.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                });
            }
        });
    }


    //复制文字到剪贴板
    public static void copy(String content, Context context)
    {
        ClipboardManager clipboard = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", content);
        clipboard.setPrimaryClip(clip);
    }
}
