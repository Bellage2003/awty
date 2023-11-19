package edu.uw.ischool.gehuijun.awty

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.SmsMessage
import android.widget.Toast

class SMSBroadcastReceiver : BroadcastReceiver() {

    private val SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (SMS_RECEIVED == intent?.action) {
            val bundle: Bundle? = intent.extras
            if (bundle != null) {
                val pdus = bundle["pdus"] as Array<Any>
                val format = bundle.getString("format")
                val messages = arrayOfNulls<SmsMessage>(pdus.size)
                for (i in pdus.indices) {
                    messages[i] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        SmsMessage.createFromPdu(pdus[i] as ByteArray, format)
                    } else {
                        SmsMessage.createFromPdu(pdus[i] as ByteArray)
                    }
                    Toast.makeText(context, messages[i]?.messageBody, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
