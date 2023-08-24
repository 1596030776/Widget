package com.example.widget;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.DecimalFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, FragmentPage.selectBtnListener {

    public static ArrayList<Document> documents = new ArrayList<>();
    public static ArrayList<Document> documentsPDF = new ArrayList<>();
    public static ArrayList<Document> documentsDOC = new ArrayList<>();
    public static ArrayList<Document> documentsXLS = new ArrayList<>();
    public static ArrayList<Document> documentsPPT = new ArrayList<>();
    public static ArrayList<Document> documentsTXT = new ArrayList<>();
    public static ArrayList<Document> documentsVCF = new ArrayList<>();
    public static ArrayList<Document> documentsOthers = new ArrayList<>();
    public static ArrayList<Uri> uris = new ArrayList<>();
    public static Context context;
    public ViewPagerAdapter viewPagerAdapter;
    private Handler workHandler;
    private HandlerThread mHandlerThread;
    public static ImageButton selectBtn;
    public static boolean clickFlag = false;

    //Fragment的List，包含所有要显示的Fragment，newInstance中的参数为显示在Fragment中的的文档信息
    private static ArrayList<FragmentPage> documentFragment = new ArrayList<>();
    private TabLayout tabLayout;
    private int selectedTabIndex = 0;
    public static ViewPager2 viewPager2;
    public static FragmentPage fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取当前页面的context
        context = getApplicationContext();

        //请求一个权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // 未被授予权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                // 已被授予权限
                Log.d("mytag.MainActivity", "已有读取文件权限");
            }
        }

        //请求另一个权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    "Manifest.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE"
            }, 1);
            initActivity();
        } else {
            initActivity();
        }
    }

    private void initActivity() {
        //获取viewPager2对象
        viewPager2 = findViewById(R.id.view_pager2);

        // 步骤1：创建HandlerThread实例对象
        // 传入参数:线程名字,作用:标记该线程
        mHandlerThread = new HandlerThread("handlerThread");
        // 步骤2：启动线程
        mHandlerThread.start();
        // 步骤3：创建工作线程Handler和主线程mainHandler & 复写handleMessage（）
        // 作用：关联HandlerThread的Looper对象,实现消息处理操作 & 与其他线程进行通信
        workHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                refreshPage(getApplicationContext());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        documentFragment.add(FragmentPage.newInstance(documents));
                        documentFragment.add(FragmentPage.newInstance(documentsPDF));
                        documentFragment.add(FragmentPage.newInstance(documentsDOC));
                        documentFragment.add(FragmentPage.newInstance(documentsXLS));
                        documentFragment.add(FragmentPage.newInstance(documentsPPT));
                        documentFragment.add(FragmentPage.newInstance(documentsTXT));
                        documentFragment.add(FragmentPage.newInstance(documentsVCF));
                        documentFragment.add(FragmentPage.newInstance(documentsOthers));

                        //为viewPager2设置Adapter，传入Fragment的List
                        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), documentFragment);
                        viewPager2.setAdapter(viewPagerAdapter);

                        //设置tabLayout字体颜色
                        tabLayout = findViewById(R.id.tab_layout);
                        tabLayout.setTabTextColors(ContextCompat.getColor(getApplicationContext(), R.color.gray),
                                ContextCompat.getColor(getApplicationContext(), R.color.black));
                        //设置每个tab上面的文字
                        new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
                            @Override
                            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                                switch (position) {
                                    case 0:
                                        tab.setText("全部");
                                        break;
                                    case 1:
                                        tab.setText("PDF");
                                        break;
                                    case 2:
                                        tab.setText("DOC");
                                        break;
                                    case 3:
                                        tab.setText("XLS");
                                        break;
                                    case 4:
                                        tab.setText("PPT");
                                        break;
                                    case 5:
                                        tab.setText("TXT");
                                        break;
                                    case 6:
                                        tab.setText("VCF");
                                        break;
                                    case 7:
                                        tab.setText("其他");
                                        break;
                                }
                            }
                        }).attach();

                        // 添加选项卡选中监听器
                        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                            @Override
                            public void onTabSelected(TabLayout.Tab tab) {
                                // 获取选中的选项卡的索引
                                selectedTabIndex = tab.getPosition();
                                fragment = documentFragment.get(selectedTabIndex);
                            }
                            @Override
                            public void onTabUnselected(TabLayout.Tab tab) {
                                //无事可做
                            }

                            @Override
                            public void onTabReselected(TabLayout.Tab tab) {
                                //无事可做
                            }
                        });
                    }
                });
            }
        };
        // 步骤4：使用工作线程Handler向工作线程的消息队列发送消息
        // 在工作线程中，当消息循环时取出对应消息 & 在工作线程执行相关操作
        // 定义要发送的消息
        Message msg = Message.obtain();
        // 通过Handler发送消息到其绑定的消息队列
        workHandler.sendMessage(msg);

        // 添加数据到 list 中
        Intent intent = new Intent(this, MyAppWidgetProvider.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("documentsList", documents);
        intent.putExtra("documentsBundle", bundle);
        sendBroadcast(intent);

        //为返回，搜索，选择按钮注册点击事件
        ImageButton buttonBack = findViewById(R.id.back_button);
        ImageButton buttonSearch = findViewById(R.id.search_button);
        ImageButton buttonSelect = findViewById(R.id.select_button);
        selectBtn = findViewById(R.id.select_button);

        buttonBack.setOnClickListener(this);
        buttonSearch.setOnClickListener(this);
        buttonSelect.setOnClickListener(this);

        SharedPreferences prefs = getSharedPreferences("uri", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        for (int i=0;i<MainActivity.documents.size();i++) {
            editor.putString("uri_" + i, MainActivity.documents.get(i).getUri().toString());
            editor.apply();
        }
    }

    public static void refreshPage(Context context) {
        //先清空上次运行留下的内容
        documents.clear();
        documentsPDF.clear();
        documentsDOC.clear();
        documentsXLS.clear();
        documentsPPT.clear();
        documentsTXT.clear();
        documentsVCF.clear();
        documentsOthers.clear();
        uris.clear();

        String rootPath1 = "/storage/self/primary/documents";
        String rootPath2 = "/storage/self/primary/Download"; //下载文件的路径
        File root1 = new File(rootPath1); // 根据路径创建File对象
        File root2 = new File(rootPath2); // 根据路径创建File对象

        try {
            getDocumentUris(root1, context); // 递归获取所有文档文件的URI和相关信息,信息存到documents中
            getDocumentUris(root2,context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //按时间排序文件
        sortByDate(documents);
        sortByDate(documentsPDF);
        sortByDate(documentsDOC);
        sortByDate(documentsXLS);
        sortByDate(documentsPPT);
        sortByDate(documentsTXT);
        sortByDate(documentsVCF);
        sortByDate(documentsOthers);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.back_button) {
            //返回按钮，直接退出当前activity
            this.finish();
        } else if (v.getId() == R.id.search_button) {
            //搜索按钮，跳转到搜索页面的activity
            Intent intent = new Intent("com.example.widget.search");
            startActivity(intent);
        } else if (v.getId() == R.id.select_button) {
            //获取当前tab对应的fragment页面
            fragment = documentFragment.get(selectedTabIndex);
            fragment.switchSelectBtn();

            //禁用点击tablayout的按钮
            TabLayout tabLayout = findViewById(R.id.tab_layout);

            TabLayout.Tab tab = tabLayout.getTabAt(0);
            if (clickFlag) {
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    View tabView = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
                    tabView.setEnabled(false);
                }
                //禁止viewPager2的滑动
                viewPager2.setUserInputEnabled(false);
                tabLayout.setTabTextColors(ContextCompat.getColor(getApplicationContext(), R.color.light_gray),
                        ContextCompat.getColor(getApplicationContext(), R.color.gray));
            } else {
                //重新启用tablayout的按钮，滑动以及允许打开文件
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    View tabView = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
                    tabView.setEnabled(true);
                }
                viewPager2.setUserInputEnabled(true);
                tabLayout.setTabTextColors(ContextCompat.getColor(getApplicationContext(), R.color.gray),
                        ContextCompat.getColor(getApplicationContext(), R.color.black));
            }
        }
    }

    // 递归获取所有文档文件的URI
    private static void getDocumentUris(File file, Context context) throws IOException {
        if (file.isDirectory()) { // 如果是目录
            File[] files = file.listFiles(); // 获取目录下的所有文件
            if (files != null) {
                for (File f : files) { // 遍历所有文件
                    getDocumentUris(f, context); // 递归获取URI
                }
            }
        } else { // 如果是文件
            String name = file.getName();
            //安卓7以上需要用这种办法
            Uri uri = FileProvider.getUriForFile(context.getApplicationContext(),
                    "com.example.widget.fileprovider", file);

            if (name.endsWith(".doc") || name.endsWith("docx") || name.endsWith(".pdf") || name.endsWith(".ppt") || name.endsWith(".pptx")
                    || name.endsWith(".xlsx") || name.endsWith(".xls") || name.endsWith(".txt") || name.endsWith(".vcf")) {

                if (uris != null && documents != null) {
                    uris.add(uri); // 如果是文档文件，则添加到URI列表中

                    //获取创建时间
                    long createTime = file.lastModified();
                    Date date = new Date(createTime);

                    // 获取文件大小
                    ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
                    String fileSize = formatFileSize(parcelFileDescriptor.getStatSize());
                    parcelFileDescriptor.close();

                    //根据文件类型将文件放入不同的集合，documents表示全部文件
                    Document document = new Document(name, fileSize, date, uri);
                    documents.add(document);
                    if (name.endsWith(".doc") || name.endsWith(".docx")) {
                        documentsDOC.add(document);
                    } else if (name.endsWith(".pdf")) {
                        documentsPDF.add(document);
                    } else if (name.endsWith(".ppt") || name.endsWith(".pptx")) {
                        documentsPPT.add(document);
                    } else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
                        documentsXLS.add(document);
                    } else if (name.endsWith(".txt")) {
                        documentsTXT.add(document);
                    } else if (name.endsWith(".vcf")) {
                        documentsVCF.add(document);
                    } else {
                        documentsOthers.add(document);
                    }
                }
            }
        }
    }

    //格式化文件大小的函数
    public static String formatFileSize(long size) {
        if (size <= 0) {
            return "0B";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    //将文件按日期进行排序的函数
    private static void sortByDate(ArrayList<Document> list) {
        Collections.sort(list, new Comparator<Document>() {
            @Override
            public int compare(Document doc1, Document doc2) {
                Date date1 = doc1.getDate();
                Date date2 = doc2.getDate();
                // 对日期字段进行升序，如果欲降序可采用after方法
                if (date1.before(date2)) {
                    return 1;
                }
                return -1;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清空 Handler 中未执行或正在执行的 Callback 和 Message
        workHandler.removeCallbacksAndMessages(null);
        // 退出handlerThread
        mHandlerThread.quit();
    }

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {
        if (fragment instanceof FragmentPage) {
            FragmentPage fragmentPage = (FragmentPage) fragment;
            fragmentPage.setOnFragmentInteractionListener(this);
        }
    }

    @Override
    public void switchSelectBtn() {
        //clickFlag为true表示进入多选状态，为false表示退出多选状态
        clickFlag = !clickFlag;

        //根据clickFlag将多选按钮，操作栏设为可见或不可见
        if (clickFlag) {
            fragment.selectAllBtn.setVisibility(View.VISIBLE);
            fragment.documentActionBar.setVisibility(View.VISIBLE);
            //多选时禁止下拉刷新
            fragment.mSwipeRefreshLayout.setEnabled(false);
        } else {
            fragment.selectAllBtn.setVisibility(View.GONE);
            fragment.documentActionBar.setVisibility(View.GONE);
            fragment.mSwipeRefreshLayout.setEnabled(true);
            TextView selectAllBtn = findViewById(R.id.selectAll_btn);
            selectAllBtn.setText("全选");
            FragmentPage.selectAll = false;
            for (int i = 0; i < fragment.recyclerView.getChildCount(); i++) {
                CheckBox checkBox = fragment.recyclerView.getChildAt(i).findViewById(R.id.document_checkBox);
                checkBox.setChecked(false);
            }
        }
    }
}