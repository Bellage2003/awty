package edu.uw.ischool.gehuijun.awty

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var phoneNumberEditText: EditText
    private lateinit var intervalEditText: EditText
    private lateinit var startButton: Button
    private var isSending = false
    private var intervalMinutes = 0

    private val handler = Handler(Looper.getMainLooper())

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
    }

    private fun startSendingMessages() {
        val message = messageEditText.text.toString()
        val phoneNumber = formatPhoneNumber(phoneNumberEditText.text.toString())
        val intervalText = intervalEditText.text.toString()

        if (message.isNotEmpty() && intervalText.isNotEmpty() && intervalText.toInt() > 0) {
            intervalMinutes = intervalText.toInt()
            isSending = true
            startButton.text = "Stop"

            val runnable = object : Runnable {
                override fun run() {
                    if (isSending) {
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
        } else {
            // Display a Toast explaining the issue
            Toast.makeText(
                this@MainActivity,
                "Please enter valid values here.",
                Toast.LENGTH_SHORT
            ).show()
        }
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
}

