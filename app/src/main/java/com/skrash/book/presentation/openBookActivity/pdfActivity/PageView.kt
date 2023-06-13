package com.skrash.book.presentation.openBookActivity.pdfActivity

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs

class PageView(context: Context, attributeSet: AttributeSet): AppCompatImageView(context, attributeSet) {

    private val instance = this
    var disableScrollOnRecyclerCallback: ((Boolean) -> Unit)? = null

    private val gestureDoubleTouchDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (instance.scaleX > 1f) {
                    instance.x = 0f
                    instance.y = 0f
                    instance.scaleX = 1f
                    instance.scaleY = 1f
                    disableScrollOnRecyclerCallback?.invoke(true)
                } else {
                    instance.scaleX = 2f
                    instance.scaleY = 2f
                    disableScrollOnRecyclerCallback?.invoke(false)
                }
                return false
            }
        })

    private val gestureMoveDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (e1.x - e2.x > 120 && abs(distanceX) > 20) {
                    if (instance.x + ((e1.x - e2.x) * -1) < ((instance.width * instance.scaleX) - instance.width) / 2 && // right limiter
                        instance.x + ((e1.x - e2.x) * -1) > -(((instance.width * instance.scaleX) - instance.width) / 2) // left limiter
                    ){
                        instance.x += e2.x - e1.x
                    }
                    return false
                }
                if (e1.y - e2.y > 120 && abs(distanceY) > 20) {
                    if (instance.y + ((e1.y - e2.y) * -1) < ((instance.height * instance.scaleY) - instance.height) / 2 && // right limiter
                        instance.y + ((e1.y - e2.y) * -1) > -(((instance.height * instance.scaleY) - instance.height) / 2) // left limiter
                    ) {
                        instance.y += e2.y - e1.y
                    }
                    return false
                }
                if (e2.y - e1.y > 120 && abs(distanceY) > 20) {
                    if (instance.y + (e2.y - e1.y) < ((instance.height * instance.scaleY) - instance.height) / 2 && // right limiter
                        instance.y + (e2.y - e1.y) > -(((instance.height * instance.scaleY) - instance.height) / 2) // left limiter
                    ) {
                        instance.y += e2.y - e1.y
                    }
                    return false
                }
                if (e2.x - e1.x > 120 && abs(distanceX) > 20) {
                    if (instance.x + e2.x - e1.x < ((instance.width * instance.scaleX) - instance.width) / 2 && // right limiter
                        instance.x + e2.x - e1.x > -(((instance.width * instance.scaleX) - instance.width) / 2) // left limiter
                    ){
                        instance.x += e2.x - e1.x
                    }
                    return false
                }
                return true
            }
        })

    private val gesturePinch =
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    if (detector.scaleFactor < 1f) {
                        if (instance.scaleY - detector.scaleFactor > 1f) {
                            instance.scaleY -= 1f - detector.scaleFactor
                            instance.scaleX -= 1f - detector.scaleFactor
                            disableScrollOnRecyclerCallback?.invoke(false)
                        }
                    } else {
                        if (instance.scaleY + detector.scaleFactor < 5f) {
                            instance.scaleY += detector.scaleFactor - 1f
                            instance.scaleX += detector.scaleFactor - 1f
                            disableScrollOnRecyclerCallback?.invoke(false)
                        }
                    }
                    if (detector.scaleFactor == 1f) {
                        instance.scaleY = 1f
                        instance.scaleX = 1f
                        disableScrollOnRecyclerCallback?.invoke(true)
                    }
                    if (instance.scaleX != 1f){
                        disableScrollOnRecyclerCallback?.invoke(false)
                    } else {
                        disableScrollOnRecyclerCallback?.invoke(true)
                    }
                    return false
                }
            })

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            gestureDoubleTouchDetector.onTouchEvent(event)
            if (event.pointerCount == 2){
                gesturePinch.onTouchEvent(event)
            } else {
                gestureMoveDetector.onTouchEvent(event)
            }
        }
        return true
    }

}