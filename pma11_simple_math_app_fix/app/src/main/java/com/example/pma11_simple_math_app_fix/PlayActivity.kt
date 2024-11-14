package com.example.pma11_simple_math_app_fix

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class PlayActivity : AppCompatActivity() {

    var timeTextView: TextView? = null
    private var questionText: TextView? = null
    private var scoreTextView: TextView? = null
    private var alertTextView: TextView? = null
    private var finalScoreTextView: TextView? = null
    private var btn0: Button? = null
    private var btn1: Button? = null
    private var btn2: Button? = null
    private var btn3: Button? = null
    private var countDownTimer: CountDownTimer? = null
    private var random: Random = Random
    private var a = 0
    private var b = 0
    private var indexOfCorrectAnswer = 0
    private var answers = ArrayList<Int>()
    private var points = 0
    private var totalQuestions = 0
    private var cals = ""
    private var showDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val calInt = intent.getStringExtra("cals") ?: "+"
        cals = calInt
        timeTextView = findViewById(R.id.TimeTextView)
        questionText = findViewById(R.id.QuestionTextText)
        scoreTextView = findViewById(R.id.ScoreTextView)
        alertTextView = findViewById(R.id.AlertTextView)
        btn0 = findViewById(R.id.button0)
        btn1 = findViewById(R.id.button1)
        btn2 = findViewById(R.id.button2)
        btn3 = findViewById(R.id.button3)

        start()
    }

    private fun nextQuestion(cal: String) {
        if (cal == "/") {
            b = random.nextInt(1, 10)
            a = b * random.nextInt(1, 10)
        } else {
            a = random.nextInt(1, 10)
            b = random.nextInt(1, 10)
        }

        val text = "$a $cal $b"
        questionText!!.text = text
        indexOfCorrectAnswer = random.nextInt(4)
        answers.clear()

        for (i in 0..3) {
            if (indexOfCorrectAnswer == i) {
                when (cal) {
                    "+" -> answers.add(a + b)
                    "-" -> answers.add(a - b)
                    "*" -> answers.add(a * b)
                    "/" -> {
                        if (b != 0) {
                            answers.add(a / b)
                        } else {
                            answers.add(a)
                        }
                    }
                }
            } else {
                var wrongAnswer: Int
                do {
                    wrongAnswer = random.nextInt(20)
                } while (wrongAnswer == a + b || wrongAnswer == a - b || wrongAnswer == a * b || (b != 0 && wrongAnswer == a / b))

                answers.add(wrongAnswer)
            }
        }

        try {
            btn0!!.text = "${answers[0]}"
            btn1!!.text = "${answers[1]}"
            btn2!!.text = "${answers[2]}"
            btn3!!.text = "${answers[3]}"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun optionSelect(view: View?) {
        totalQuestions++
        val selectedAnswer = (view!!.tag.toString().toInt())
        if (selectedAnswer == indexOfCorrectAnswer) {
            points++
            val text = "Correct"
            alertTextView!!.text = text
        } else {
            val text = "Wrong"
            alertTextView!!.text = text
        }
        val text = "$points/$totalQuestions"
        scoreTextView!!.text = text
        nextQuestion(cals)
    }

    private fun playAgain() {
        points = 0
        totalQuestions = 0
        val text1 = "0/0"
        scoreTextView!!.text = text1
        countDownTimer?.cancel()
        countDownTimer!!.start()
        nextQuestion(cals)

        val text2 = "10s"
        timeTextView!!.text = text2
    }

    private fun start() {
        nextQuestion(cals)
        countDownTimer = object : CountDownTimer(10000, 500) {
            override fun onTick(p0: Long) {
                val text = (p0 / 1000).toString() + "s"
                timeTextView!!.text = text
            }

            override fun onFinish() {
                val text = "Konec času"
                timeTextView!!.text = text
                openDialog()
            }
        }.start()
    }

    private fun openDialog() {
        val inflate = LayoutInflater.from(this)
        val winDialog = inflate.inflate(R.layout.win_layout, null)
        finalScoreTextView = winDialog.findViewById(R.id.FinalScoreTextView)
        val btnPlayAgain = winDialog.findViewById<Button>(R.id.buttonPlayAgain)
        val btnBack = winDialog.findViewById<Button>(R.id.buttonBack)
        val dialog = AlertDialog.Builder(this)
        dialog.setCancelable(false)
        dialog.setView(winDialog)
        val text = "$points/$totalQuestions"
        finalScoreTextView!!.text = text

        btnPlayAgain.setOnClickListener {
            showDialog?.dismiss()
            playAgain()
        }
        btnBack.setOnClickListener {
            onBackPressed()
        }
        showDialog = dialog.create()
        showDialog?.show()
    }
}