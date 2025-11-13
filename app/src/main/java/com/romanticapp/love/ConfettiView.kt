package com.romanticapp.love

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import kotlin.random.Random

class ConfettiView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val confettiPieces = mutableListOf<ConfettiPiece>()
    private val paint = Paint().apply {
        isAntiAlias = true
    }
    private var isAnimating = false
    private val colors = listOf(
        Color.RED,
        Color.MAGENTA,
        Color.YELLOW,
        Color.GREEN,
        Color.BLUE,
        Color.CYAN,
        Color.parseColor("#FF69B4"), // Pink
        Color.parseColor("#FF1493")  // Deep pink
    )

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isAnimating) {
                updateConfetti()
                invalidate()
                handler.postDelayed(this, 16) // ~60 FPS
            }
        }
    }

    fun startConfetti() {
        if (width == 0 || height == 0) {
            post { startConfetti() }
            return
        }

        isAnimating = true
        confettiPieces.clear()
        val screenWidth = width.toFloat()
        val screenHeight = height.toFloat()

        // Create confetti pieces
        repeat(100) {
            confettiPieces.add(
                ConfettiPiece(
                    x = Random.nextFloat() * screenWidth,
                    y = -Random.nextFloat() * screenHeight,
                    velocityX = (Random.nextFloat() - 0.5f) * 4f,
                    velocityY = Random.nextFloat() * 8f + 2f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 10f,
                    size = Random.nextFloat() * 20 + 10,
                    color = colors.random()
                )
            )
        }
        handler.post(updateRunnable)
    }

    fun stopConfetti() {
        isAnimating = false
        handler.removeCallbacks(updateRunnable)
        confettiPieces.clear()
        invalidate()
    }

    private fun updateConfetti() {
        val screenHeight = height.toFloat()
        val iterator = confettiPieces.iterator()
        while (iterator.hasNext()) {
            val piece = iterator.next()
            piece.x += piece.velocityX
            piece.y += piece.velocityY
            piece.rotation += piece.rotationSpeed
            piece.velocityY += 0.2f // Gravity

            // Remove pieces that are off screen
            if (piece.y > screenHeight + 100) {
                iterator.remove()
            }
        }

        // Add new pieces at the top
        if (confettiPieces.size < 100 && Random.nextFloat() > 0.7f) {
            val screenWidth = width.toFloat()
            confettiPieces.add(
                ConfettiPiece(
                    x = Random.nextFloat() * screenWidth,
                    y = -50f,
                    velocityX = (Random.nextFloat() - 0.5f) * 4f,
                    velocityY = Random.nextFloat() * 8f + 2f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 10f,
                    size = Random.nextFloat() * 20 + 10,
                    color = colors.random()
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (piece in confettiPieces) {
            paint.color = piece.color
            canvas.save()
            canvas.translate(piece.x, piece.y)
            canvas.rotate(piece.rotation)
            canvas.drawRect(
                -piece.size / 2,
                -piece.size / 2,
                piece.size / 2,
                piece.size / 2,
                paint
            )
            canvas.restore()
        }
    }

    private data class ConfettiPiece(
        var x: Float,
        var y: Float,
        var velocityX: Float,
        var velocityY: Float,
        var rotation: Float,
        var rotationSpeed: Float,
        var size: Float,
        var color: Int
    )
}

