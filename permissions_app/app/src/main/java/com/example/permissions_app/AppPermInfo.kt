package com.example.permissions_app

import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppPermInfo(private var appPermList: Array<PermissionInfo>, private var appName: String):RecyclerView.Adapter<AppPermInfo.AppPermInfoViewHolder>(){
    class AppPermInfoViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppPermInfoViewHolder {
        return AppPermInfoViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.app_perm_info_item,
            parent,
            false));
    }

    override fun onBindViewHolder(holder: AppPermInfoViewHolder, position: Int) {
        var currAppPerm = appPermList[position];

        holder.itemView.apply {
            var permName = findViewById<TextView>(R.id.tvPermTitle);
            permName.text = currAppPerm.toString();
        }
    }

    override fun getItemCount(): Int {
        return appPermList.size;
    }
}