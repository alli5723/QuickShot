package com.alli5723.android.quickshot.adapters

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.alli5723.android.quickshot.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.gallery_item.view.*
import java.io.File

/**
 * Created by omo_lanke on 28/01/2018.
 */
class ImageListAdapter(private var mCursor: Cursor?) : RecyclerView.Adapter<ImageListAdapter.GalleryHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null
    private var mContext: Context? = null

    /* Callback for list item click events */
    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int)
        fun onShareClicked(v: View, position: Int)
    }

    /* ViewHolder for each task item */
    inner class GalleryHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val idIndex = mCursor!!.getColumnIndex(MediaStore.Files.FileColumns._ID)
        val nameIndex = mCursor!!.getColumnIndex(MediaStore.Files.FileColumns.TITLE)
        val dataIndex = mCursor!!.getColumnIndex(MediaStore.Files.FileColumns.DATA)
//        var nameView: TextView
//        var id: TextView
//        var imageView: ImageView
//        var play: ImageView

        init {
//            nameView =  itemView.findViewById(R.id.item_name) as TextView
//            id = itemView.findViewById(R.id.item_id) as TextView
//            imageView = itemView.findViewById(R.id.item_image) as ImageView
//            play = itemView.findViewById(R.id.play) as ImageView

            itemView.setOnClickListener(this)
        }

        fun bindImage(position: Int){
            mCursor!!.moveToPosition(position)
            itemView.item_name.text = mCursor!!.getString(nameIndex)
            itemView.item_id.text = mCursor!!.getString(idIndex)
            itemView.item_share.setOnClickListener(View.OnClickListener {
                postShareClick(this)
            })

            val data = mCursor!!.getString(dataIndex)
            Picasso.with(mContext).load(File(data)).fit().into(itemView.item_image)

        }

        override fun onClick(v: View) {
            postItemClick(this)
        }
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mOnItemClickListener = listener
    }

    private fun postItemClick(holder: GalleryHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener!!.onItemClick(holder.itemView, holder.adapterPosition)
        }
    }

    private fun postShareClick(holder: GalleryHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener!!.onShareClicked(holder.itemView, holder.adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryHolder {
        mContext = parent.context
        val itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.gallery_item, parent, false)

        itemView.setFocusable(true)
        return GalleryHolder(itemView)
    }

    override fun onBindViewHolder(holder: GalleryHolder, position: Int) {
        holder.bindImage(position)
//        val idIndex = mCursor!!.getColumnIndex(MediaStore.Files.FileColumns._ID)
//        val nameIndex = mCursor!!.getColumnIndex(MediaStore.Files.FileColumns.TITLE)
//        val dateIndex = mCursor!!.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)
//        val dataIndex = mCursor!!.getColumnIndex(MediaStore.Files.FileColumns.DATA)
//
//        mCursor!!.moveToPosition(position)
//        Log.e("Data", "onBindViewHolder: " + mCursor!!.getString(dataIndex) + " : " +
//                mCursor!!.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)
//                + " : " + mCursor!!.getString(nameIndex) + " : " + mCursor!!.getString(dateIndex))
//
//        holder.nameView.text = mCursor!!.getString(nameIndex)
//        holder.id.text = mCursor!!.getString(idIndex)
//        val data = mCursor!!.getString(dataIndex)
//        Picasso.with(mContext).load(File(data)).fit().into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return if (mCursor != null) mCursor!!.count else 0
    }

    fun swapCursor(cursor: Cursor) {
        if (mCursor != null) {
            mCursor!!.close()
        }
        mCursor = cursor
        notifyDataSetChanged()
    }
}