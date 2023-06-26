package ba.etf.us2022.alarm

import android.app.Activity
import android.content.Intent
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import ba.etf.rma22.alarm.R
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.nio.charset.StandardCharsets
import java.util.*

class MainActivity : AppCompatActivity() {
    var alarm: SwitchCompat? = null
    var detekcija: TextView? = null
    var mqtt: MqttAndroidClient? = null

    fun detekcija() {
        val kalendar = Calendar.getInstance()
        val dateFormat: DateFormat = SimpleDateFormat("dd-mm-yyyy HH:mm:ss")
        val datum = dateFormat.format(kalendar.time)
        detekcija!!.text = "Detektovana neželjena aktivnost u: $datum"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("jajaja", "onCreate: NEce main")
        alarm = findViewById<View>(R.id.swOnOff) as SwitchCompat
        mqtt = MqttAndroidClient(
            applicationContext,
            "tcp://broker.hivemq.com",
            MqttClient.generateClientId()
        )
        val o = MqttConnectOptions()
        o.isCleanSession = true
        mqtt!!.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable) {
                Log.i("TAG", "connection lost")
            }
            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                if (topic.contains("alarmus/detekcija")) detekcija()
                Log.i("TAG", "topic: " + topic + ", msg: " + String(message.payload))
            }
            override fun deliveryComplete(token: IMqttDeliveryToken) {
                Log.i("TAG", "msg delivered")
            }
        })
        try {
            mqtt!!.connect(o, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i("TAG", "connect succeed")
                    subscribeTopic("alarmus/detekcija")
                    if (!alarm!!.isChecked) {
                        try {
                            mqtt!!.publish(
                                "alarmus/password",
                                ("password").toByteArray(StandardCharsets.UTF_8),
                                0,
                                false
                            )
                        } catch (e: MqttException) {
                            e.printStackTrace()
                        }
                    } else try {
                        mqtt!!.publish(
                            "alarmus/password",
                            ("1").toByteArray(StandardCharsets.UTF_8),
                            0,
                            false
                        )
                    } catch (e: MqttException) {
                        e.printStackTrace()
                    }
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.i("TAG", "connect failed")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
        val mStartForResult = registerForActivityResult(StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent = result.data!!
                posalji(intent.getStringExtra("password"))
            }
        }
        alarm!!.setOnCheckedChangeListener { _, checked ->
            if (!checked) {
                Log.i("moj", "uspjeh")
                mStartForResult.launch(Intent(applicationContext, MainActivity2::class.java))
            } else try {
                mqtt!!.publish(
                    "alarmus/password",
                    ("1").toByteArray(StandardCharsets.UTF_8),
                    0,
                    false
                )
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }

    fun posalji(t: String?) {
        Log.i("moj", "ne radi")
        try {
            mqtt!!.publish("alarmus/password", t!!.toByteArray(StandardCharsets.UTF_8), 0, false)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribeTopic(topic: String?) {
        try {
            mqtt!!.subscribe(topic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i("TAG", "subscribed succeed")
                }
                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.i("TAG", "subscribed failed")
                }
            })
        } catch (e: MqttException) {
        }
    }
}