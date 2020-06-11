package com.kirille.lifepriority

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONException
import java.lang.RuntimeException

class DialogImageFragment : DialogFragment() {
    private var mContext: Context? = null
    private var lblCount : TextView? = null
    private var photoArray: JSONArray = JSONArray()
    private val dialogImageTag = "DialogImageTag"
    private var selectedPosition = 0
    private var viewPager: ViewPager? = null

    private val viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            displayMetaInfo(position)
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dialog_image, container, false)

        val bundle = arguments
        if (bundle != null) {
            try {
               photoArray = JSONArray(bundle.getString("photos"))
            } catch (e: JSONException){
                Log.d(dialogImageTag, "JSONException: $e")
            }
            selectedPosition = bundle.getInt("selectedPosition")
        }

        lblCount = view.findViewById(R.id.lbl_count)
        lblCount?.text = getString(R.string.gallery_count, 1, photoArray.length())

        val buttonBack = view.findViewById<ImageButton>(R.id.buttonBack)
        buttonBack.setOnClickListener {
            dismiss()
        }

        viewPager = view.findViewById(R.id.viewpager)
        viewPager?.adapter = ViewPagerAdapter()
        viewPager?.addOnPageChangeListener(viewPagerPageChangeListener)
        viewPager?.currentItem = selectedPosition

        displayMetaInfo(selectedPosition)

        return view
    }

    inner class ViewPagerAdapter: PagerAdapter(){

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutInflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(
                    R.layout.image_fullscreen_preview,
                    container,
                    false
            )

            val imagePreview = view.findViewById<ImageView>(R.id.image_preview)

            val photoURL: String
            try {
                photoURL = photoArray.getJSONObject(position).getString("average")
            } catch (e: JSONException){
                Log.i(dialogImageTag, "JSONException: $e")
                throw RuntimeException(e)
            }

                Glide
                    .with(mContext!!)
                    .load(photoURL)
                    .thumbnail(0.1f)
                    .into(imagePreview)

            container.addView(view)

            return view
        }

        override fun getCount(): Int = photoArray.length()
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }

    private fun displayMetaInfo(position: Int){
        lblCount?.text = getString(
                R.string.gallery_count,
                position + 1,
                photoArray.length()
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

}