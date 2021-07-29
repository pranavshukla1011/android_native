package com.example.permissions_app

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView


class AppList (private var appList: List<PackageInfo>):RecyclerView.Adapter<AppList.AppNameViewHolder>(){

    class AppNameViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppNameViewHolder {

        return AppNameViewHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.app_list_item,
                parent,
                false
            ))
    }

    override fun onBindViewHolder(holder: AppNameViewHolder, position: Int) {
            val currApp = appList[position]


            holder.itemView.apply {
                var packageName = currApp.packageName.toString();

                //package manager import
                var pm = context.packageManager
                var info = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                var appName = pm.getApplicationLabel(info).toString();

                val listItem = findViewById<Button>(R.id.btnAppTitle)

                listItem.text = appName;

                listItem.setOnClickListener{

                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(context, intent, null)
                }
            }
    }

    override fun getItemCount(): Int {
        return appList.size
    }
}