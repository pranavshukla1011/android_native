package com.example.permissions_app

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        var appName = intent.getStringExtra("appName");

        var currApp = packageManager.getPackageInfo(appName.toString(), PackageManager.GET_PERMISSIONS);

        if(currApp.permissions != null) {
            var listAdapter = AppPermInfo(currApp.permissions, appName.toString());
            var view = findViewById<RecyclerView>(R.id.rvAppInfo);
            view.adapter = listAdapter;
            view.layoutManager = LinearLayoutManager(this);
        }
        else {
            var textView = findViewById<TextView>(R.id.tvNoPerm);
            textView.text = "No Permissions Found";

        }
    }
}