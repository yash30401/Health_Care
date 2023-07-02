package com.healthcare.yash.preeti

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class AppCard(context: Context) : LinearLayout(context) {
    private val appIconView: ImageView
    private val appLabelView: TextView
    var appIntent: Intent? = null

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.card_app, this)

        appIconView = findViewById(R.id.app_icon)
        appLabelView = findViewById(R.id.app_label)
    }

    fun setAppIcon(icon: Drawable) {
        appIconView.setImageDrawable(icon)
    }

    fun setAppLabel(label: CharSequence) {
        appLabelView.text = label
    }
}
