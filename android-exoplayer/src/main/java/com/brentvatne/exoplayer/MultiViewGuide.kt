package com.brentvatne.exoplayer

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.isNotEmpty
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.brentvatne.react.R
import com.diceplatform.doris.ui.entity.LabelsTranslation
import com.facebook.react.modules.i18nmanager.I18nUtil
import me.relex.circleindicator.CircleIndicator2

class MultiViewGuide(context: Context) : FrameLayout(context), OnClickListener, OnFocusChangeListener {

    private val scalePercent: Float = 1.2f
    private val isRtl = I18nUtil.getInstance().isRTL(context)
    private val recyclerView: RecyclerView by lazy { findViewById(R.id.multiview_guide_recycler_view) }
    private val skipButton: Button by lazy { findViewById(R.id.multiview_guide_skip_button) }
    private val prevButton: Button by lazy { findViewById(R.id.multiview_guide_previous_button) }
    private val nextButton: Button by lazy { findViewById(R.id.multiview_guide_next_button) }

    init {
        isFocusable = true
        visibility = View.GONE
        layoutDirection = if (isRtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
    }

    fun show(labelsTranslation: LabelsTranslation?) {
        if (isShowed()) return
        //TODO: test guide feature, remove later
//        context.getMultiviewSharedPrefs().edit { putBoolean("showed", true) }
        visibility = View.VISIBLE
        //TODO: test guide feature, remove later
        if (isNotEmpty()) {
            post { skipButton.requestFocus() }
            return
        }
        LayoutInflater.from(context).inflate(R.layout.comp_multiview_guide, this)
        val adapter = GuideAdapter(context = context, labelsTranslation.getGuideStrings())
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
        val circleIndicator = findViewById<CircleIndicator2>(R.id.multiview_guide_indicator)
        circleIndicator.attachToRecyclerView(recyclerView, PagerSnapHelper())
        adapter.registerAdapterDataObserver(circleIndicator.adapterDataObserver)

        skipButton.apply {
            text = labelsTranslation?.get("skip") ?: "Skip"
            layoutDirection = if (isRtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            setOnClickListener(this@MultiViewGuide)
            onFocusChangeListener = this@MultiViewGuide
        }
        prevButton.apply {
            text = labelsTranslation?.get("previousIcon") ?: "Prev"
            layoutDirection = if (isRtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            setOnClickListener(this@MultiViewGuide)
            onFocusChangeListener = this@MultiViewGuide
        }
        nextButton.apply {
            text = labelsTranslation?.get("next") ?: "Next"
            layoutDirection = if (isRtl) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
            setOnClickListener(this@MultiViewGuide)
            onFocusChangeListener = this@MultiViewGuide
        }
        postDelayed({ nextButton.requestFocus() }, 500L)
    }

    private fun isShowed(): Boolean {
        return context.getMultiviewSharedPrefs().getBoolean("showed", false)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        TextViewCompat.setCompoundDrawableTintList(
            v as TextView,
            AppCompatResources.getColorStateList(context, R.color.dce_watch_from_text_selector)
        )
        ViewCompat.animate(v)
            .scaleX(if (hasFocus) scalePercent else 1.0f)
            .scaleY(if (hasFocus) scalePercent else 1.0f)
            .translationZ(if (hasFocus) 1f else 0f)
            .start()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.multiview_guide_skip_button -> {
                this@MultiViewGuide.visibility = View.GONE
            }

            R.id.multiview_guide_previous_button -> {
                scrollToPrevious()
            }

            R.id.multiview_guide_next_button -> {
                scrollToNext()
            }
        }
    }

    private fun scrollToNext() {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val currentPosition = layoutManager.findFirstVisibleItemPosition()
        if (currentPosition == layoutManager.itemCount - 1) return
        nextButton.requestFocus()
        prevButton.visibility = View.VISIBLE
        recyclerView.smoothScrollToPosition(currentPosition + 1)
        if (currentPosition + 1 == layoutManager.itemCount - 1) {
            nextButton.visibility = View.GONE
            skipButton.requestFocus()
            return
        }
    }

    private fun scrollToPrevious() {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val currentPosition = layoutManager.findFirstVisibleItemPosition()
        if (currentPosition == 0) return
        prevButton.requestFocus()
        nextButton.visibility = View.VISIBLE
        recyclerView.smoothScrollToPosition(currentPosition - 1)
        if (currentPosition - 1 == 0) {
            prevButton.visibility = View.GONE
            nextButton.requestFocus()
            return
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_DPAD_UP
            || event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN
        ) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}

// ---------------------------------------------------
// ---- adapter
// ---------------------------------------------------
private class GuideAdapter(
    context: Context,
    private val guideStrings: List<String>,
) : Adapter<GuideViewHolder>() {
    private val guideCount = 5
    private val layoutInflater = LayoutInflater.from(context)

    override fun getItemCount(): Int = guideCount

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        return when (viewType) {
            0 -> GuideViewHolder(layoutInflater.inflate(R.layout.comp_multiview_guide_item_1, parent, false))
            1 -> GuideViewHolder(layoutInflater.inflate(R.layout.comp_multiview_guide_item_2, parent, false))
            2 -> GuideViewHolder(layoutInflater.inflate(R.layout.comp_multiview_guide_item_3, parent, false))
            3 -> GuideViewHolder(layoutInflater.inflate(R.layout.comp_multiview_guide_item_4, parent, false))
            else -> GuideViewHolder(layoutInflater.inflate(R.layout.comp_multiview_guide_item_5, parent, false))
        }
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        if (position < guideStrings.size) {
            holder.textView.text = guideStrings[position]
        }
    }
}

// ---------------------------------------------------
// ---- view holder
// ---------------------------------------------------
private class GuideViewHolder(itemView: View) : ViewHolder(itemView) {
    val textView: TextView = itemView.findViewById(R.id.text)
}

private fun LabelsTranslation?.getGuideStrings(): List<String> {
    return mutableListOf<String>().apply {
        if (this@getGuideStrings != null) {
            get("multiViewGuide1")?.takeIf { it.isNotBlank() }?.let { add(it) }
            get("multiViewGuide2")?.takeIf { it.isNotBlank() }?.let { add(it) }
            get("multiViewGuide3")?.takeIf { it.isNotBlank() }?.let { add(it) }
            get("multiViewGuide4")?.takeIf { it.isNotBlank() }?.let { add(it) }
            get("multiViewGuide5")?.takeIf { it.isNotBlank() }?.let { add(it) }
        }
    }.toList()
}

private fun Context.getMultiviewSharedPrefs() = getSharedPreferences("multiview_guide", Context.MODE_PRIVATE)