package com.example.sos_app

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    private lateinit var btnAddContact : Button;
    private lateinit var btnToggle : Button;
    private lateinit var contactListAdapter : ContactList;
    private lateinit var db :DbHelper;
    private lateinit var contactsList : List<Contact>;

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnAddContact = findViewById<Button>(R.id.btnAddContacts);
        btnToggle = findViewById<Button>(R.id.btnToggle);
        db = DbHelper(this);
        contactsList = db.getAllContacts();
        contactListAdapter = ContactList(applicationContext, contactsList);


        val listView = findViewById<RecyclerView>(R.id.rvContactList);
        listView.adapter = contactListAdapter;
        listView.layoutManager = LinearLayoutManager(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_DENIED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.READ_CONTACTS
                    ), 100
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 100)
        }

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    @SuppressLint("BatteryLife") val intent =
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = Uri.parse("package:$packageName")
                        startActivityForResult(intent, IGNORE_BATTERY_OPTIMIZATION_REQUEST)
                }
            }
        }

        val service = Services()
        val intent = Intent(this, service.javaClass)
        if (!isMyServiceRunning(service.javaClass)) {
            startService(intent);
        }


        btnAddContact.setOnClickListener {
                // calling of getContacts()
                if (db.count() != 3) {
                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                    startActivityForResult(intent, PICK_CONTACT);
                } else {
                    Toast.makeText(
                        this,
                        "Can't Add more than 5 Contacts",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_CONTACT -> if (resultCode == RESULT_OK) {
                val contactData = data?.data
                val c = managedQuery(contactData, null, null, null, null)
                if (c.moveToFirst()) {
                    val id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val hasPhone =
                        c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    var phone: String? = null
                    try {
                        if (hasPhone.equals("1", ignoreCase = true)) {
                            val phones = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                null,
                                null
                            )
                            phones!!.moveToFirst()
                            phone = phones.getString(phones.getColumnIndex("data1"))
                        }
                        val name =
                            c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                        if(phone!=null){
                            db.addcontact(Contact(0, name, phone))
                            contactsList = db.getAllContacts()
                            contactListAdapter.refresh(contactsList);
                        }
                    } catch (ex: Exception) {
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permissions Denied!\n Can't use the App!", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onDestroy() {
        val broadcastIntent = Intent()
        broadcastIntent.action = "restart service"
        broadcastIntent.setClass(this, ReactivateService::class.java)
        this.sendBroadcast(broadcastIntent)
        super.onDestroy()
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
    companion object{
        val IGNORE_BATTERY_OPTIMIZATION_REQUEST = 1002
        val PICK_CONTACT = 1
    }
}