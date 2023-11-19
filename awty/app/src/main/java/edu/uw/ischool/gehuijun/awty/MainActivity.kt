package edu.uw.ischool.gehuijun.awty

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var intervalEditText: EditText
    private lateinit var startButton: Button
    private var isSending = false
    private var intervalMinutes = 0

    private val handler = Handler(Looper.getMainLooper())
    private val smsManager: SmsManager = SmsManager.getDefault()

    companion object {
        const val SMS_PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageEditText = findViewById(R.id.messageEditText)
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        intervalEditText = findViewById(R.id.intervalEditText)
        startButton = findViewById(R.id.startButton)

        startButton.setOnClickListener {
            if (!isSending) {
                startSendingMessages()
            } else {
                stopSendingMessages()
            }
        }

        if (!checkPermission(Manifest.permission.RECEIVE_SMS)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECEIVE_SMS),
                SMS_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun checkPermission(permission: String): Boolean {
        val permissionCheck = ContextCompat.checkSelfPermission(this, permission)
        return (permissionCheck == PackageManager.PERMISSION_GRANTED)
    }

    private fun startSendingMessages() {
        val message = messageEditText.text.toString()
        val phoneNumber = formatPhoneNumber(phoneNumberEditText.text.toString())
        val intervalText = intervalEditText.text.toString()

        if (message.isNotEmpty() && intervalText.isNotEmpty() && intervalText.toInt() > 0) {
            intervalMinutes = intervalText.toInt()
            isSending = true
            startButton.text = "Stop"

            // Request SMS permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    SMS_PERMISSION_REQUEST_CODE
                )
            } else {
                startSendingMessagesWithPermission(message, phoneNumber)
            }
        } else {
            // Display a Toast explaining the issue
            Toast.makeText(
                this@MainActivity,
                "Please enter valid values here.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun startSendingMessagesWithPermission(message: String, phoneNumber: String) {
        val sentIntent = PendingIntent.getBroadcast(
            this@MainActivity,
            0,
            Intent("SMS_SENT"),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val deliveredIntent = PendingIntent.getBroadcast(
            this@MainActivity,
            0,
            Intent("SMS_DELIVERED"),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        showToast("Sending messages started")

        val runnable = object : Runnable {
            override fun run() {
                if (isSending) {
                    // Send SMS
                    smsManager.sendTextMessage(
                        phoneNumber,
                        null,
                        message,
                        sentIntent,
                        deliveredIntent
                    )

                    // Create custom Toast
                    val toastView = layoutInflater.inflate(R.layout.custom_toast, null)
                    val toastCaption = toastView.findViewById<TextView>(R.id.toastCaption)
                    val toastMessage = toastView.findViewById<TextView>(R.id.toastMessage)

                    toastCaption.text = "Texting $phoneNumber"
                    toastMessage.text = message

                    val toast = Toast(this@MainActivity)
                    toast.duration = Toast.LENGTH_SHORT
                    toast.view = toastView
                    toast.show()

                    handler.postDelayed(this, (intervalMinutes * 60 * 1000).toLong())
                }
            }
        }

        handler.postDelayed(runnable, (intervalMinutes * 60 * 1000).toLong())
    }

    private fun stopSendingMessages() {
        isSending = false
        startButton.text = "Start"
        handler.removeCallbacksAndMessages(null)
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        val cleanedNumber = phoneNumber.replace(Regex("[^0-9]"), "")

        if (cleanedNumber.length == 10) {
            return "(${cleanedNumber.substring(0, 3)}) ${cleanedNumber.substring(3, 6)}-${cleanedNumber.substring(6)}"
        }

        return cleanedNumber
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
