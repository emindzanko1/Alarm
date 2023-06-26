package ba.etf.us2022.alarm

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import ba.etf.rma22.alarm.R

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gasenje)
        val button = findViewById<View>(R.id.button2) as Button
        val pass = findViewById<View>(R.id.editText) as EditText
        button.setOnClickListener {
            if (pass.text.toString() == "password") {
                val output = Intent()
                output.putExtra("password", pass.text.toString())
                setResult(RESULT_OK, output)
                finish()
            }
        }
    }
}