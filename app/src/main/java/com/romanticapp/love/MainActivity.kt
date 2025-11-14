package com.romanticapp.love

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.button.MaterialButton
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import java.lang.Math

class MainActivity : AppCompatActivity() {

    private lateinit var btnQuestion: MaterialButton
    private lateinit var tvAnswer: TextView
    private lateinit var confettiView: ConfettiView
    private lateinit var heartsContainer: FrameLayout
    private lateinit var pukingEmojiContainer: FrameLayout
    private lateinit var mainLayout: ConstraintLayout

    private var handler = Handler(Looper.getMainLooper())
    private var isFirstQuestion = true
    private lateinit var soundPool: SoundPool
    private var heartSoundId: Int = 0
    private var isSoundLoaded = false

    // Accelerometer sensor
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastShakeTime = 0L
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    private val SHAKE_THRESHOLD = 9f
    private val SHAKE_TIMEOUT = 1000L // 1 second between shakes
    private var isAnimating = false
    private var shouldAnimateButtonAfterEffect = false
    private var rainbowColorRunnable: Runnable? = null
    private var originalBackground: android.graphics.drawable.Drawable? = null
    private var partyToneGenerator: ToneGenerator? = null
    private var momoSignatureTypeface: Typeface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnQuestion = findViewById(R.id.btnQuestion)
        tvAnswer = findViewById(R.id.tvAnswer)
        confettiView = findViewById(R.id.confettiView)
        heartsContainer = findViewById(R.id.heartsContainer)
        pukingEmojiContainer = findViewById(R.id.pukingEmojiContainer)
        mainLayout = findViewById(R.id.mainLayout)

        initSound()
        initAccelerometer()
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
        playPartyHorn()
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
        if (!isAnimating) {
            revealButtonWithAnimation()
        } else {
            btnQuestion.visibility = View.GONE
            shouldAnimateButtonAfterEffect = true
        }
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
        if (!isAnimating) {
            revealButtonWithAnimation()
        } else {
            btnQuestion.visibility = View.GONE
            shouldAnimateButtonAfterEffect = true
        }
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
                animator.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        heartsContainer.removeView(heart)
                    }
                })
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
        if (momoSignatureTypeface == null) {
            momoSignatureTypeface = loadMomoSignatureTypeface()
        }
        tvAnswer.typeface = momoSignatureTypeface
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

    private fun initAccelerometer() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(accelerometerListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private val accelerometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                val currentTime = System.currentTimeMillis()

                if (currentTime - lastShakeTime > SHAKE_TIMEOUT) {
                    val deltaX = x - lastX
                    val deltaY = y - lastY
                    val deltaZ = z - lastZ

                    val acceleration = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

                    if (acceleration > SHAKE_THRESHOLD && !isAnimating) {
                        lastShakeTime = currentTime
                        triggerPukingAnimation()
                    }

                    lastX = x
                    lastY = y
                    lastZ = z
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Not needed for this implementation
        }
    }

    private fun triggerPukingAnimation() {
        if (isAnimating) return
        isAnimating = true
        shouldAnimateButtonAfterEffect = false

        // Hide buttons
        btnQuestion.visibility = View.GONE
        tvAnswer.visibility = View.GONE

        // Show container
        pukingEmojiContainer.visibility = View.VISIBLE
        pukingEmojiContainer.removeAllViews()

        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        // Save original background
        originalBackground = mainLayout.background

        // Create the puking emoji (🤮) - bigger and full opacity
        val emojiTextView = TextView(this)
        emojiTextView.text = "🤮"
        val emojiTextSize = 200f
        emojiTextView.textSize = emojiTextSize
        emojiTextView.alpha = 1.0f // Full opacity
        val emojiLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        // Center the emoji properly in the center of the screen
        emojiLayoutParams.gravity = Gravity.CENTER
        pukingEmojiContainer.addView(emojiTextView, emojiLayoutParams)
        
        // Post to get actual measured size after layout
        emojiTextView.post {
            // Re-measure after layout to get accurate size
            emojiTextView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
        }
        
        // Estimate emoji size based on textSize (emojis are roughly square)
        // Convert sp to pixels for more accurate estimation
        val density = resources.displayMetrics.density
        val emojiSizePx = emojiTextSize * density
        val emojiWidth = emojiSizePx
        val emojiHeight = emojiSizePx

        // Animate emoji shaking
        val shakeX = ObjectAnimator.ofFloat(emojiTextView, "translationX", 0f, -20f, 20f, -20f, 20f, -10f, 10f, 0f)
        val shakeY = ObjectAnimator.ofFloat(emojiTextView, "translationY", 0f, -10f, 10f, -10f, 10f, -5f, 5f, 0f)
        shakeX.duration = 500
        shakeY.duration = 500
        val shakeAnimator = AnimatorSet()
        shakeAnimator.playTogether(shakeX, shakeY)
        shakeAnimator.start()

        // Start rainbow background color switching
        startRainbowBackgroundAnimation()

        // Create rainbow particles (🌈 emoji and rainbow colors)
        val rainbowEmojis = listOf("🌈", "💫", "✨", "⭐", "🌟")
        val rainbowColors = listOf(
            Color.RED,
            Color.parseColor("#FF7F00"), // Orange
            Color.YELLOW,
            Color.GREEN,
            Color.BLUE,
            Color.parseColor("#4B0082"), // Indigo
            Color.parseColor("#9400D3")  // Violet
        )

        // Calculate emoji center and mouth position (bottom center of emoji)
        // Since emoji is centered, its center is at screen center
        val emojiCenterX = screenWidth / 2f
        val emojiCenterY = screenHeight / 2f
        // Mouth is at the bottom of the emoji, roughly 70% down from top
        val emojiMouthY = emojiCenterY + (emojiHeight * 0.3f)
        // Mouth width is roughly 30% of emoji width, centered
        val mouthWidth = emojiWidth * 0.3f

        // Create multiple rainbow streams
        repeat(50) { index ->
            handler.postDelayed({
                val particle = TextView(this)
                val useEmoji = Random.nextBoolean()
                if (useEmoji && Random.nextFloat() > 0.3f) {
                    particle.text = rainbowEmojis.random()
                    particle.textSize = Random.nextFloat() * 40 + 20
                } else {
                    particle.text = "●"
                    particle.textSize = Random.nextFloat() * 30 + 15
                    particle.setTextColor(rainbowColors.random())
                }

                // Start position at emoji mouth (bottom center of emoji) - more realistic
                // Particles start from a small area at the mouth
                val mouthOffsetX = (Random.nextFloat() * mouthWidth - mouthWidth / 2f)
                val startX = emojiCenterX + mouthOffsetX
                val startY = emojiMouthY

                val particleLayoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                particleLayoutParams.leftMargin = startX.toInt()
                particleLayoutParams.topMargin = startY.toInt()
                pukingEmojiContainer.addView(particle, particleLayoutParams)
                
                // Use translationX/Y for animation instead of x/y
                particle.translationX = 0f
                particle.translationY = 0f

                // Animate particle flying out in an arc - more realistic trajectory
                // Particles go downward and outward from the mouth
                val angle = if (mouthWidth > 0) (mouthOffsetX / mouthWidth) * 60f else 0f // -30 to +30 degrees
                val speed = Random.nextFloat() * 300 + 200
                val angleRad = Math.toRadians(angle.toDouble())
                val endX = startX + (sin(angleRad) * speed).toFloat()
                val endY = startY + (cos(angleRad) * speed * 1.5f).toFloat()
                val midX = (startX + endX) / 2 + Random.nextFloat() * 50 - 25
                val midY = startY + (endY - startY) * 0.3f - Random.nextFloat() * 50

                // Create arc path using keyframes (using translationX/Y for smooth animation)
                val animX = ObjectAnimator.ofFloat(particle, "translationX", 0f, midX - startX, endX - startX)
                val animY = ObjectAnimator.ofFloat(particle, "translationY", 0f, midY - startY, endY - startY)
                val alpha = ObjectAnimator.ofFloat(particle, "alpha", 1f, 1f, 0f)
                val scale = ObjectAnimator.ofFloat(particle, "scaleX", 0.5f, 1.2f, 0.8f)
                val scaleY = ObjectAnimator.ofFloat(particle, "scaleY", 0.5f, 1.2f, 0.8f)

                val particleAnimator = AnimatorSet()
                particleAnimator.playTogether(animX, animY, alpha, scale, scaleY)
                particleAnimator.duration = (Random.nextLong(1000) + 800)
                particleAnimator.interpolator = AccelerateDecelerateInterpolator()
                particleAnimator.start()

                // Remove particle after animation
                particleAnimator.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        pukingEmojiContainer.removeView(particle)
                    }
                })
            }, index * 30L) // Stagger the particles more frequently
        }

        // Hide container and reset after animation completes
        handler.postDelayed({
            stopRainbowBackgroundAnimation()
            // Restore original background
            originalBackground?.let {
                mainLayout.background = it
            } ?: run {
                // Fallback to drawable resource if originalBackground is null
                mainLayout.setBackgroundResource(R.drawable.background_gradient)
            }
            pukingEmojiContainer.visibility = View.GONE
            pukingEmojiContainer.removeAllViews()
            // Restore button visibility
            btnQuestion.visibility = View.VISIBLE
            if (shouldAnimateButtonAfterEffect) {
                shouldAnimateButtonAfterEffect = false
                revealButtonWithAnimation()
            } else {
                btnQuestion.alpha = 1f
                btnQuestion.scaleX = 1f
                btnQuestion.scaleY = 1f
            }
            isAnimating = false
        }, 3000)
    }

    private fun startRainbowBackgroundAnimation() {
        // Create very faded rainbow colors with low alpha (0.15 = 15% opacity)
        val alpha = 0.15f
        val rainbowColors = listOf(
            Color.argb((alpha * 255).toInt(), 255, 0, 0), // Red
            Color.argb((alpha * 255).toInt(), 255, 127, 0), // Orange
            Color.argb((alpha * 255).toInt(), 255, 255, 0), // Yellow
            Color.argb((alpha * 255).toInt(), 0, 255, 0), // Green
            Color.argb((alpha * 255).toInt(), 0, 0, 255), // Blue
            Color.argb((alpha * 255).toInt(), 75, 0, 130), // Indigo
            Color.argb((alpha * 255).toInt(), 148, 0, 211)  // Violet
        )
        
        var colorIndex = 0
        rainbowColorRunnable = object : Runnable {
            override fun run() {
                if (isAnimating) {
                    mainLayout.setBackgroundColor(rainbowColors[colorIndex])
                    colorIndex = (colorIndex + 1) % rainbowColors.size
                    handler.postDelayed(this, 200) // 0.2 seconds
                }
            }
        }
        handler.post(rainbowColorRunnable!!)
    }

    private fun stopRainbowBackgroundAnimation() {
        rainbowColorRunnable?.let {
            handler.removeCallbacks(it)
            rainbowColorRunnable = null
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(accelerometerListener)
        stopRainbowBackgroundAnimation()
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(accelerometerListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        stopRainbowBackgroundAnimation()
        sensorManager.unregisterListener(accelerometerListener)
        soundPool.release()
        partyToneGenerator?.release()
        partyToneGenerator = null
    }

    private fun revealButtonWithAnimation() {
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

    private fun playPartyHorn() {
        if (partyToneGenerator == null) {
            partyToneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        }
        partyToneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300)
    }

    private fun loadMomoSignatureTypeface(): Typeface {
        return try {
            Typeface.createFromAsset(assets, "fonts/momo_signature_bold.ttf")
        } catch (e: Exception) {
            Typeface.create("momo signature", Typeface.BOLD)
        }
    }
}

