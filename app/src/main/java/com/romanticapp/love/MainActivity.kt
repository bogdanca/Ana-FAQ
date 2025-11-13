package com.romanticapp.love

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var btnQuestion: MaterialButton
    private lateinit var tvAnswer: TextView
    private lateinit var confettiView: ConfettiView
    private lateinit var heartsContainer: FrameLayout

    private var handler = Handler(Looper.getMainLooper())
    private var isFirstQuestion = true
    private lateinit var soundPool: SoundPool
    private var heartSoundId: Int = 0
    private var isSoundLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnQuestion = findViewById(R.id.btnQuestion)
        tvAnswer = findViewById(R.id.tvAnswer)
        confettiView = findViewById(R.id.confettiView)
        heartsContainer = findViewById(R.id.heartsContainer)

        initSound()
        styleAnswerText()

        btnQuestion.setOnClickListener {
            showAnswer()
            if (isFirstQuestion) {
                handler.postDelayed({
                    showSecondButton()
                }, 10000)
            } else {
                handler.postDelayed({
                    resetToInitialState()
                }, 10000)
            }
        }
    }

    private fun showAnswer() {
        // Hide button
        btnQuestion.visibility = View.GONE

        // Show answer
        val answerTextRes = if (isFirstQuestion) R.string.answer_yes else R.string.answer_lots
        tvAnswer.setText(answerTextRes)
        tvAnswer.visibility = View.VISIBLE
        tvAnswer.alpha = 0f
        tvAnswer.scaleX = 0.5f
        tvAnswer.scaleY = 0.5f

        // Animate answer appearance
        val fadeIn = ObjectAnimator.ofFloat(tvAnswer, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(tvAnswer, "scaleX", 0.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(tvAnswer, "scaleY", 0.5f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, scaleX, scaleY)
        animatorSet.duration = 500
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.start()

        // Show confetti
        confettiView.visibility = View.VISIBLE
        confettiView.startConfetti()

        // Show hearts
        heartsContainer.visibility = View.VISIBLE
        startHeartAnimation()

        playHeartSound()
    }

    private fun showSecondButton() {
        // Hide answer and effects
        tvAnswer.visibility = View.GONE
        confettiView.visibility = View.GONE
        confettiView.stopConfetti()
        heartsContainer.visibility = View.GONE
        clearHearts()

        // Update button text and show it
        isFirstQuestion = false
        btnQuestion.text = getString(R.string.button_question_2)
        btnQuestion.visibility = View.VISIBLE
        btnQuestion.alpha = 0f
        btnQuestion.scaleX = 0.5f
        btnQuestion.scaleY = 0.5f

        val fadeIn = ObjectAnimator.ofFloat(btnQuestion, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(btnQuestion, "scaleX", 0.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(btnQuestion, "scaleY", 0.5f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, scaleX, scaleY)
        animatorSet.duration = 500
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.start()
    }

    private fun resetToInitialState() {
        // Hide everything
        tvAnswer.visibility = View.GONE
        confettiView.visibility = View.GONE
        confettiView.stopConfetti()
        heartsContainer.visibility = View.GONE
        clearHearts()

        // Reset button to first question and show it
        isFirstQuestion = true
        btnQuestion.text = getString(R.string.button_question_1)
        btnQuestion.visibility = View.VISIBLE
        btnQuestion.alpha = 0f
        btnQuestion.scaleX = 0.5f
        btnQuestion.scaleY = 0.5f

        val fadeIn = ObjectAnimator.ofFloat(btnQuestion, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(btnQuestion, "scaleX", 0.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(btnQuestion, "scaleY", 0.5f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(fadeIn, scaleX, scaleY)
        animatorSet.duration = 500
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.start()
    }

    private fun startHeartAnimation() {
        val heartEmojis = listOf("❤️", "💕", "💖", "💗", "💓", "💝", "💘", "💞")
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        repeat(20) {
            handler.postDelayed({
                val heart = TextView(this)
                heart.text = heartEmojis.random()
                heart.textSize = Random.nextFloat() * 30 + 20
                heart.x = Random.nextFloat() * screenWidth
                heart.y = screenHeight.toFloat()
                heartsContainer.addView(heart)

                // Animate heart floating up
                val animator = ObjectAnimator.ofFloat(heart, "y", screenHeight.toFloat(), -100f)
                animator.duration = (Random.nextLong(3000) + 2000)
                animator.interpolator = AccelerateDecelerateInterpolator()
                animator.start()

                // Remove heart after animation
                animator.addUpdateListener {
                    if (heart.y < -100) {
                        heartsContainer.removeView(heart)
                    }
                }
            }, it * 200L)
        }
    }

    private fun playHeartSound() {
        if (isSoundLoaded) {
            soundPool.play(heartSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    private fun clearHearts() {
        heartsContainer.removeAllViews()
    }

    private fun initSound() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(attributes)
            .build()

        heartSoundId = soundPool.load(this, R.raw.heart, 1)
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId == heartSoundId) {
                isSoundLoaded = true
            }
        }
    }

    private fun styleAnswerText() {
        tvAnswer.typeface = Typeface.create("cursive", Typeface.BOLD)
        tvAnswer.paint.apply {
            style = Paint.Style.FILL_AND_STROKE
            strokeWidth = resources.displayMetrics.density * 0.6f
            isAntiAlias = true
        }
        tvAnswer.invalidate()
        applyTextOutline(tvAnswer)
    }

    private fun applyTextOutline(textView: TextView) {
        val strokeWidthPx = resources.displayMetrics.density // ~1dp
        textView.paint.strokeMiter = 10f
        textView.paint.strokeJoin = Paint.Join.ROUND
        textView.setShadowLayer(strokeWidthPx, 0f, 0f, Color.BLACK)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        soundPool.release()
    }
}

