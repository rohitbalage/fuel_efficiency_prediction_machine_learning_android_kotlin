package com.example.fuelefficiencypredictorkotlin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {
    var mean = floatArrayOf(5.477707f, 195.318471f, 104.869427f, 2990.251592f, 15.559236f, 75.898089f, 0.624204f, 0.178344f, 0.197452f)
    var std = floatArrayOf(1.699788f, 104.331589f, 38.096214f, 843.898596f, 2.789230f, 3.675642f, 0.485101f, 0.383413f, 0.398712f)

    var interpreter: Interpreter? = null
    var sv: ScrollView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        try {
            interpreter = Interpreter(loadModelFile()!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        sv = findViewById(R.id.sv)
        val cylinders: EditText = findViewById(R.id.editText)
        val displacement: EditText = findViewById(R.id.editText2)
        val horsePower: EditText = findViewById(R.id.editText3)
        val weight: EditText = findViewById(R.id.editText4)
        val accelration: EditText = findViewById(R.id.editText5)
        val modelYear: EditText = findViewById(R.id.editText6)
        val origin: Spinner = findViewById(R.id.spinner)
        val arrayAdapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_dropdown_item, arrayOf("USA", "Europe", "Japan"))
        origin.adapter = arrayAdapter
        val result: TextView = findViewById(R.id.textView2)

        val btn: Button = findViewById(R.id.button)
        btn.setOnClickListener {
            sv!!.scrollTo(sv!!.bottom, 0)
            val floats = Array(1) { FloatArray(9) }
            floats[0][0] = (cylinders.text.toString().toFloat() - mean[0]) / std[0]
            floats[0][1] = (displacement.text.toString().toFloat() - mean[1]) / std[1]
            floats[0][2] = (horsePower.text.toString().toFloat() - mean[2]) / std[2]
            floats[0][3] = (weight.text.toString().toFloat() - mean[3]) / std[3]
            floats[0][4] = (accelration.text.toString().toFloat() - mean[4]) / std[4]
            floats[0][5] = (modelYear.text.toString().toFloat() - mean[5]) / std[5]
            when (origin.selectedItemPosition) {
                0 -> {
                    floats[0][6] = (1 - mean[6]) / std[6]
                    floats[0][7] = (0 - mean[7]) / std[7]
                    floats[0][8] = (0 - mean[8]) / std[8]
                }
                1 -> {
                    floats[0][6] = (0 - mean[6]) / std[6]
                    floats[0][7] = (1 - mean[7]) / std[7]
                    floats[0][8] = (0 - mean[8]) / std[8]
                }
                2 -> {
                    floats[0][6] = (0 - mean[6]) / std[6]
                    floats[0][7] = (0 - mean[7]) / std[7]
                    floats[0][8] = (1 - mean[8]) / std[8]
                }
            }
            val res: Float = doInference(floats)
            result.text = res.toString() + ""
        }
    }
    fun doInference(input: Array<FloatArray>): Float {
        val output = Array(1) { FloatArray(1) }
        interpreter!!.run(input, output)
        return output[0][0]
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer? {
        val assetFileDescriptor = this.assets.openFd("automobile.tflite")
        val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val length = assetFileDescriptor.length
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length)
    }
}