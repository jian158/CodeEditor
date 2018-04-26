package wei.editor;

import android.Manifest;
import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import wei.editor.Editor.Editor;
import wei.editor.Editor.TextEditor;

public class CodeEditorActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment mNavigationDrawerFragment;

    private CharSequence mTitle;

    private ArrayMap<String, TextEditor> map = new ArrayMap<>();

    private TextEditor currentView;

    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        frameLayout = (FrameLayout) findViewById(R.id.container);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(String key) {
        if (currentView != null) {
            frameLayout.removeView(currentView);
        }
        currentView = map.get(key);
        if (currentView == null) {
            currentView = new TextEditor(this);
            map.put(key, currentView);
            currentView.Open(key);
        }
        if (getActionBar() != null) {
            getActionBar().setTitle(currentView.getName());
        }
        frameLayout.addView(currentView);
    }


    @Override
    public void onNavigationDrawerItemClosed(String key) {
        TextEditor editor = map.remove(key);
        editor.clear();
        editor = null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //四个参数的含义。1，group的id,2,item的id,3,是否排序，4，将要显示的内容
        menu.add(0, 1, 0, "打开");
        menu.add(0, 2, 0, "保存");
        menu.add(0, 3, 0, "测试");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                openNewFile();
                break;
            case 2:
                saveFile();
                break;
            case 3:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openNewFile() {
        showFileChooser();
    }

    public void saveFile() {
        currentView.save();
    }

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public void showFileChooser() {
        int permission = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            permission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "拒绝读取", Toast.LENGTH_SHORT).show();
                // We don't have permission so prompt the user
                requestPermissions(PERMISSIONS_STORAGE, 10);
            }
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 10);

    }


    public void addNewFragment(String key) {
        mNavigationDrawerFragment.addNewInstance(key);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;
        if (requestCode == 10 && data != null && (uri = data.getData()) != null) {
            addNewFragment(uri.getPath());
        } else {
            Toast.makeText(this, "Open Fail!", Toast.LENGTH_SHORT).show();
        }
    }


}
