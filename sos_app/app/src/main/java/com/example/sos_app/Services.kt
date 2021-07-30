package com.example.sos_app

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.os.*
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.sos_app.ShakeDetector.OnShakeListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener


class Services : Service() {
    private var mSensorManager: SensorManager? = null
    private var mAccelerometer: Sensor? = null
    private var mShakeDetector: ShakeDetector? = null

    private fun messageType( location : Location?){

        val smsManager = SmsManager.getDefault()
        val db = DbHelper(this)
        val contactsList: List<Contact> = db.getAllContacts()
        when(location){
            null -> {
                val message = "I am in danger, Help me!  GPS was turned off. Couldn't find location. Call your nearest Police Station."
                for (c in contactsList) {
                    smsManager.sendTextMessage(
                        c.getPhoneNo(),
                        null,
                        message,
                        null,
                        null
                    )
                }
            }
            else -> {
                for (c in contactsList) {
                    val message = "Hey, " + c.getName()
                        .toString() + "I am in danger, Help me! \n Location " + "http://maps.google.com/?q=" + location.latitude.toString() + "," + location.longitude
                    smsManager.sendTextMessage(
                        c.getPhoneNo(),
                        null,
                        message,
                        null,
                        null
                    )
                }
            }
        }
    }

    fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val vibEff: VibrationEffect

        // Android Q and above have some predefined vibrating patterns
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibEff = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            vibrator.cancel()
            vibrator.vibrate(vibEff)
        } else {
            vibrator.vibrate(500);
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "example.permanence"
        val channelName = "Background Service"
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        )
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(channel)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
            .setContentTitle("You are protected.")
            .setContentText("We are there for you")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }

    private fun stopService(){
        stopForeground(true);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            true -> startMyOwnForeground()
            else -> startForeground(1, Notification())
        }

//        check for shake
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mShakeDetector = ShakeDetector()
        mShakeDetector!!.setOnShakeListener(object : OnShakeListener {
            @SuppressLint("MissingPermission")
            override fun onShake(count: Int) {
                if (count == 3) {
                    vibrate()
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(
                        applicationContext
                    )
                    fusedLocationClient.getCurrentLocation(
                        LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                        object : CancellationToken() {
                            override fun isCancellationRequested(): Boolean {
                                return false
                            }

                            override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                                return CancellationTokenSource().token;
                            }

                        }).addOnSuccessListener {
                            location ->  messageType(location)
                    }.addOnFailureListener {
                        messageType(null);
                    }
                }
            }
        })

        mSensorManager!!.registerListener(
            mShakeDetector,
            mAccelerometer,
            SensorManager.SENSOR_DELAY_UI
        )

//        checking for power button
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)

        filter.addAction(Intent.ACTION_SCREEN_OFF)
        val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            var count = 0
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_SCREEN_OFF) {
                    count++
                } else if (intent.action == Intent.ACTION_SCREEN_ON) {
                    count++
                }
                if (count == 2) {
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(
                        applicationContext
                    )
                    fusedLocationClient.getCurrentLocation(
                        LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                        object : CancellationToken() {
                            override fun isCancellationRequested(): Boolean {
                                return false
                            }
                            override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
                                return CancellationTokenSource().token;
                            }
                        }).addOnSuccessListener { location ->
                        if (location != null) {
                            Log.e("location", "$location");
                            messageType(location)
                        }
                    }.addOnFailureListener {
                        messageType(null);
                    }
                    count = 0
                }
            }
        }
        registerReceiver(mReceiver, filter)
    }

    override fun onDestroy() {
        val broadcastIntent = Intent()
        broadcastIntent.action = "restart service"
        broadcastIntent.setClass(this, ReactivateService::class.java)
        this.sendBroadcast(broadcastIntent)
        super.onDestroy()
    }

}

