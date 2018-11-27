package mx.itesm.a01113373.datagraphs

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.graphics.Canvas
import android.graphics.Paint
import android.content.Context
import android.view.View
import android.widget.Toast
import android.R.attr.data
import android.animation.ObjectAnimator
import android.text.TextPaint


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val layout1 = findViewById<android.support.constraint.ConstraintLayout>(R.id.layout1)
        val canvass = Canvass(this, 230f, false)
        val white = Canvass(this, 180f, true)
        val needle = Needle(this)
        var rotation = 30f
        var nameVal = 20
        for (i in 0..10) {
            val grade = Grade(this, rotation, nameVal)
            layout1.addView(grade)
            rotation = rotation + 30f
            nameVal += 20
        }
        layout1.addView(needle)
        layout1.addView(canvass)
        layout1.addView(white)
        needle.setOnClickListener(needle)
    }

    inner class Grade(context: Context, var rotate: Float, var name: Int) : View(context) {
        val paint = Paint()
        val textPaint = TextPaint().apply {
            textSize = 60f
            rotation = -rotate
        }
        override fun onDraw(canvas: Canvas) {
            val width = getWidth()
            val centerX = width.toFloat() / 2
            val centerY = height.toFloat() / 2
            paint.strokeWidth = 15f
            canvas.drawLine(centerX, centerY, centerX, centerY + 320, paint).apply {
                rotation = rotate
            }
            canvas.drawText(name.toString(), centerX, centerY + 370, textPaint). apply {
                rotation = rotate
            }
        }
    }

    inner class Canvass(context: Context, var radius: Float, var white: Boolean)
        : View(context), View.OnClickListener {
        val paint = Paint()
        val textPaint = TextPaint().apply {
            textSize = 80f
        }
        var pressureVal: Int = 90
        override fun onDraw(canvas: Canvas) {
            val width = getWidth()
            val centerX = width.toFloat() / 2
            val centerY = height.toFloat() / 2
            if (white) {
                paint.setARGB(255, 250, 250, 255)
                canvas.drawCircle(centerX, centerY, this.radius, paint)
                canvas.drawText(pressureVal.toString(), centerX - 30, centerY + 30, textPaint)
            } else {
                paint.setARGB(255, 200, 200, 200)
                canvas.drawCircle(centerX, centerY, this.radius, paint)
            }
        }

        override fun onClick(v: View?) {
            pressureVal += 10
        }
    }

    inner class Needle(context:Context) : View(context), View.OnClickListener {
        val paint = Paint()
        val textPaint = TextPaint().apply { textSize = 16f }
        override fun onDraw(canvas: Canvas) {
            val width = getWidth()
            paint.setARGB(255, 255, 0, 0)
            val centerX = width.toFloat() / 2
            val centerY = height.toFloat() / 2
            val downY = centerY + 300f
            paint.strokeWidth = 20f
            canvas.drawLine(centerX, centerY, centerX, downY, paint).apply {
                rotation = 30f
            }
        }
        override fun onClick(v: View?) {
            ObjectAnimator.ofFloat(v, "rotation", v!!.rotation+10f).start()
        }

        fun updateValue(v: View?, newVal: Float) {
            var new_rotation = newVal - v!!.rotation - 20
            if (new_rotation > 260f) {
                new_rotation = 280f
            } else if (new_rotation < 20f) {
                new_rotation = 20f
            }
            new_rotation += 50f

            ObjectAnimator.ofFloat(v, "rotation", new_rotation).start()
        }
    }
}
