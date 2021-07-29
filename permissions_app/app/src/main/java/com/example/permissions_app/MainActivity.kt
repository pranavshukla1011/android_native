package com.example.permissions_app

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var btnAppList : Button;
    private lateinit var appListView : RecyclerView;
    private lateinit var appPerm: EditText;
    private lateinit var appListAdapter: AppList;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAppList = findViewById<Button>(R.id.btnAppList)
        appListView = findViewById<RecyclerView>(R.id.rvAppList);
        appPerm = findViewById<EditText>(R.id.etPerms);

        var getContext = this;

        btnAppList.setOnClickListener{
            var targetPerm = appPerm.text.toString();
            var targetPermArray = targetPerm.split(" ").toMutableList();

            for(index in targetPermArray.indices){
                var temp = targetPermArray[index];
                temp = "android.permission.$temp";
                targetPermArray[index] = temp;
            }

            var permissions = targetPermArray.toTypedArray();
            var packagesHoldingPerm = packageManager.getPackagesHoldingPermissions(permissions, PackageManager.GET_PERMISSIONS)
            appListAdapter = AppList(packagesHoldingPerm);
            appListView.adapter = appListAdapter;
            appListView.layoutManager = LinearLayoutManager(getContext);
        }
    }
}
