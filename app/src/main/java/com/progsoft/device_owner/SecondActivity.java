package com.progsoft.device_owner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import androidx.biometric.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.progsoft.device_owner.iflytek.TtsDemo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

public class SecondActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "PROG_SecondActivity";
    List<itemInfo> list = new ArrayList<>();
    List<itemInfo> alist = new ArrayList<>();
    List<itemInfo> tiku = new ArrayList<>();
    List<itemInfo> pwds = new ArrayList<>();
    long nowti = 0;
    long count = 0;
    MarkAdapter adapter, adapter2;
    TextView question, tinum;

    private void ReadTextFile(List<itemInfo> dataBase, String filePath) {
        itemInfo info;
        File file = new File(Environment.getExternalStorageDirectory(), filePath);
        try {
            InputStream inputStream = new FileInputStream(file);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] ls = line.split("-");
                    if (ls.length == 2) { //输入模式，只用输入题目和答案
                        info = new itemInfo(ls[0], ls[1], "FGH", 0, 0, 0, Color.TRANSPARENT);
                        dataBase.add(info);
                    } else if (ls.length == 7) { //正常模式+max
                        info = new itemInfo(Integer.parseInt(ls[0]), ls[1], ls[2], "FGH",
                                Integer.parseInt(ls[3]),Integer.parseInt(ls[4]),Integer.parseInt(ls[5]), Integer.parseInt(ls[6]), Color.TRANSPARENT);
                        dataBase.add(info);
                    }
                }
                inputStream.close();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(TAG, "Text UnsupportedEncodingException.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "File doesn't exist.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Text IOException.");
        }
    }

    @SuppressLint("NewApi")
    public void WriteTextFile() {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "progsoft/Kiosk/3.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
            tiku.sort(new Comparator<itemInfo>() {
                @Override
                public int compare(itemInfo o1, itemInfo o2) {
                    return o1.number - o2.number;
                }
            });
            int count = 0;
            bw.write("题库系统，最后更新时间\n" + new Date() + "\n输入格式：\n1.题目+答案\n2.序号+题目+答案+答题次数+正确+等级+最大等级\n=======================");
            bw.newLine();
            for (itemInfo info: tiku) {
                bw.write(count + "-" + info.question + "-" + info.answer + "-" + info.total + "-" + info.right + "-" + info.weight + "-" + info.max);
                bw.newLine();
                count += 1;
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        enableKioskMode(true);
        FileWrite("SecondActivity onResume:" + this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileWrite("SecondActivity onDestroy:" + this);
    }

    private void init_tiku() {
        itemInfo info;
        itemInfo info2;
        String keyboard = "QWERTYUIOPASDFGHJKL  ZXCVBNM  qwertyuiopasdfghjkl! zxcvbnm?.";
        list.clear();
        alist.clear();
        tiku.clear();
        pwds.clear();

        for (int i = 0; i < 60; i++) {
            info = new itemInfo(keyboard.substring(i, i + 1),"CDE", "FGH", Color.TRANSPARENT);
            list.add(info);
            if (i < 28) {
                info2 = new itemInfo("", "CDE", "FGH", Color.TRANSPARENT);
                alist.add(info2);
            }
        }
        ReadTextFile(tiku, "progsoft/Kiosk/3.txt");
        ReadTextFile(pwds, "progsoft/Kiosk/pwd.txt");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileWrite("SecondActivity onCreate:" + this);
        setContentView(R.layout.activity_second);

        TtsDemo.getInstance(getApplicationContext()).playText("开始测验");

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        Button btmSubmit = findViewById(R.id.submit);
        btmSubmit.setOnClickListener(this);

        Button btnClear = findViewById(R.id.clear);
        btnClear.setOnClickListener(this);

        question = findViewById(R.id.chinese);
        tinum = findViewById(R.id.num);

        setUpAdmin();

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerView recyclerView2 = findViewById(R.id.rv_list);
        init_tiku();


        adapter = new MarkAdapter(this, alist);
        adapter2 = new MarkAdapter(this, list);

        Configuration mConfiguration = this.getResources().getConfiguration();
        int ori = mConfiguration.orientation;
        if (ori == Configuration.ORIENTATION_LANDSCAPE) {
            GridLayoutManager linearLayoutManager = new GridLayoutManager(this ,28);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView2.setLayoutManager(new GridLayoutManager(this ,28));
        } else if (ori == Configuration.ORIENTATION_PORTRAIT) {
            GridLayoutManager linearLayoutManger = new GridLayoutManager(this, 14);
            recyclerView.setLayoutManager(linearLayoutManger);
            recyclerView2.setLayoutManager(new GridLayoutManager(this ,10));
        }

        recyclerView.setItemViewCacheSize(-1);
        recyclerView.setAdapter(adapter);
        recyclerView2.setItemViewCacheSize(-1);
        recyclerView2.setAdapter(adapter2);

        adapter.oldv = 0;
        adapter.setBColor(Color.YELLOW);

        adapter.setOnClickListener((v, postion) -> {
            if (v instanceof TextView) {
                double r = Math.random();
                String nStr = "" + (char)(r * 26 + 64);
                if (adapter.oldv != -1) {
                    adapter.setBColor(Color.TRANSPARENT);
                }
                /*
                if (postion == 0) {
                    KioskModeApp.setIsInLockMode(false);
                }
                 */
                adapter.oldv = postion;
                //adapter.changeData(postion, nStr);
                adapter.setBColor(Color.YELLOW);
            } else {
                Log.e(TAG, "adapter.setOnClickListener View:" + v + " Pos:" + postion);
            }

        });

        adapter2.setOnClickListener((v, postion) -> {
            if (v instanceof TextView) {
                String nStr = list.get(postion).question;
                if (adapter.oldv != -1) {
                    adapter.setBColor(Color.TRANSPARENT);
                }
                /*
                if (postion == 0) {
                    KioskModeApp.setIsInLockMode(false);

                }
                 */
                adapter.changeData(adapter.oldv, nStr);
                adapter.oldv++;
                if (adapter.oldv >= adapter.list.size())
                    adapter.oldv = adapter.list.size() - 1;
                adapter.setBColor(Color.YELLOW);
            } else {
                Log.e(TAG, "adapter2.setOnClickListener View:" + v + " Pos:" + postion);
            }

        });
        count = 0;
        updateTimu();
    }

    @SuppressLint("SetTextI18n")
    private void updateTimu() {
        itemInfo info;
        double []weight = {0.0d, 0.0d, 0.6d, 0.75d, 0.88d, 0.90d, 0.93d, 0.96d, 0.98d, 0.99d, 0.995d};
        int min = KioskModeApp.getMin();
        int max = KioskModeApp.getMax();

        if (min < 0 || min > max) min = 0;
        if (min > max) max = 300;

        do {
            nowti = Math.round(Math.random() * 5000) + min;
            if (nowti < tiku.size() && nowti <= max) {
                info = tiku.get((int) nowti);
                if (KioskModeApp.getTestNum() == 0) {
                    if (!info.selected) {
                        info.selected = true;
                        break;
                    }
                } else {
                    double level = Math.random();
                    if (weight[info.getWeight()] <= level)
                        break;
                }
            }
        } while (true);

        info.setDelta(0);
        count++;
        int testNum = KioskModeApp.getTestNum();
        String testmode = "";
        if (testNum == 0) {
            testNum = KioskModeApp.getMax() - KioskModeApp.getMin() + 1;
            testmode = "测试模式_";
        }
        FileWrite("SecondActivity " + testmode + "第" + count + "题（正确"+ KioskModeApp.getRightNum() + "/错误" + (count - KioskModeApp.getRightNum() - 1) + "/" + testNum +"题）（" + (nowti) + "/" + tiku.size() + ")");
        FileWrite("SecondActivity " + tiku.get((int) nowti).question + " (" + info.getWeight() + "): " + tiku.get((int) nowti).answer);
        question.setText(tiku.get((int) nowti).question + "(" + info.getWeight() + ")");
        for (int i = 0; i < 28; i++) {
            adapter.changeData(i, "");
            adapter.setBColor(Color.TRANSPARENT);
        }
        adapter.oldv = 0;
        tinum.setText(testmode + "第" + count + "题（正确"+ KioskModeApp.getRightNum() + "/错误" + (count - KioskModeApp.getRightNum() - 1) + "/" + testNum +"题）（" + (nowti) + "/" + tiku.size() + ")");
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        FileWrite("SecondActivity onBackPressed");
        Executor executor = ContextCompat.getMainExecutor(this);

        final BiometricPrompt biometricPrompt = new BiometricPrompt(SecondActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        FileWrite("SecondActivity Finger Error" + errorCode + " " + errString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        enableKioskMode(false);
                        KioskModeApp.addTimeToLock(KioskModeApp.LOCK_TIME_10MIN * 6); // 指纹 增加 30分钟
                        //KioskModeApp.setIsInLockMode(false);
                        WriteTextFile();
                        FileWrite("SecondActivity Finger Succ");
                        Toast.makeText(SecondActivity.this, "Login Successful !!!!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        FileWrite("SecondActivity Finger Failed");
                    }
                });

        final BiometricPrompt.PromptInfo  promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("本次将保存记录")
                .setSubtitle("请输入您的指纹")
                .setDescription("Use your finger print to quit application ")
                .setNegativeButtonText("取消本次尝试")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.submit) {
            String s = "";
            for (int i = 0; i < adapter.list.size(); i++)
                s = s + adapter.list.get(i).question;
            s = s.trim() + adapter.list.get(14).question;
            if (true && (s.isEmpty()))
                return;
            FileWrite("SecondActivity Print:" + adapter.list.size() + ":" + s);
            for (itemInfo pwd:pwds) {
                if (pwd.question.equals(s)) {
                    for (int i = 0; i < 28; i++) {
                        adapter.changeData(i, "");
                        adapter.setBColor(Color.TRANSPARENT);
                        adapter.oldv = 0;
                    }
                    enableKioskMode(false);
                    //KioskModeApp.setIsInLockMode(false);
                    int setTime = Integer.parseInt(pwd.answer);
                    KioskModeApp.setTimeToLock(KioskModeApp.LOCK_TIME_10MIN * setTime / 5);
                    finish();
                    moveTaskToBack(true);
                    return;
                }
            }
            /*
            if ("exit".equals(s) || "hqy".equals(s) || "qwer".equals(s) || s.isEmpty()) {
                for (int i = 0; i < 28; i++) {
                    adapter.changeData(i, "");
                    adapter.setBColor(Color.TRANSPARENT);
                    adapter.oldv = 0;
                }
                enableKioskMode(false);
                //KioskModeApp.setIsInLockMode(false);
                switch (s) {
                    case "exit":
                        KioskModeApp.setTimeToLock(KioskModeApp.LOCK_TIME_10MIN * 2); // exit 只给10分钟
                        break;
                    case "hqy":
                        KioskModeApp.setTimeToLock(KioskModeApp.LOCK_TIME_10MIN * 6); // hqy 给30分钟
                        break;
                    case "qwer":
                        KioskModeApp.setTimeToLock(KioskModeApp.LOCK_TIME_10MIN * 60); // qwer 给5个小时
                        break;
                    default:
                        KioskModeApp.setTimeToLock(KioskModeApp.LOCK_TIME_10MIN / 5); // 空 给1分钟
                        break;
                }
                finish();
                moveTaskToBack(true);
                return;
            }
             */

            itemInfo info = tiku.get((int) nowti);
            if (info.answer.equals(s)) {
                info.setDelta(1);
                info.update();
                if (info.delta > 0) {
                    KioskModeApp.RightNum++;
                }
                //作对题量8题或20题, 或者做两倍以上题量
                int testNum = KioskModeApp.getTestNum();
                if  ((testNum > 0 && (count >= 2L * testNum || KioskModeApp.RightNum >= testNum))
                    || (testNum == 0) && (count > KioskModeApp.getMax() - KioskModeApp.getMin())){
                    enableKioskMode(false);
                    //KioskModeApp.setIsInLockMode(false);
                    WriteTextFile();
                    KioskModeApp.addTimeToLock(KioskModeApp.LOCK_TIME_10MIN * 3); // 做题加15分钟
                    int left = (KioskModeApp.getTimeToLock() * 3) / 60;
                    TtsDemo.getInstance(getApplicationContext()).playText("已经累计" + left + "分钟");
                    finish();
                    moveTaskToBack(true);
                } else {
                    updateTimu();
                }
            } else {
                info.setDelta(-1);
                s = info.answer;
                for (int i = 0; i < s.length() && i < adapter.list.size(); i++) {
                    adapter.changeData(i + 14, s.substring(i, i + 1));
                    adapter.setBColor(Color.TRANSPARENT);
                }
            }

        } else if (view.getId() == R.id.clear) {
            for (int i = 0; i < 28; i++) {
                adapter.changeData(i, "");
                adapter.setBColor(Color.TRANSPARENT);
                adapter.oldv = 0;
            }
        } else if (view.getId() == R.id.btnBack) {
            Executor executor = ContextCompat.getMainExecutor(this);

            final BiometricPrompt biometricPrompt = new BiometricPrompt(SecondActivity.this, executor,
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                            super.onAuthenticationError(errorCode, errString);
                            FileWrite("SecondActivity Finger Error" + errorCode + " " + errString);
                        }

                        @Override
                        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                            super.onAuthenticationSucceeded(result);
                            enableKioskMode(false);
                            KioskModeApp.addTimeToLock(KioskModeApp.LOCK_TIME_10MIN * 6); // 指纹 增加30分钟
                            //KioskModeApp.setIsInLockMode(false);
                            FileWrite("SecondActivity Finger Succ");
                            Toast.makeText(SecondActivity.this, "Login Successful !!!!", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onAuthenticationFailed() {
                            super.onAuthenticationFailed();
                            FileWrite("SecondActivity Finger Failed");
                        }
                    });

            final BiometricPrompt.PromptInfo  promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Finger Check")
                    .setSubtitle("请输入您的指纹")
                    .setDescription("Use your finger print to quit application ")
                    .setNegativeButtonText("取消本次尝试")
                    .build();

            biometricPrompt.authenticate(promptInfo);
        }
    }

    public static void startThisActivity(Context context) {
        Intent intent = new Intent(context, SecondActivity.class);
        context.startActivity(intent);
    }
}
