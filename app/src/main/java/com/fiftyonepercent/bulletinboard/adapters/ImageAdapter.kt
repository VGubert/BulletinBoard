package com.fiftyonepercent.bulletinboard.adapters

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.fiftyonepercent.bulletinboard.R

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ImageHolder>() {
    val mainArray = ArrayList<Bitmap>() //создаем массив


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_adapter_item, parent, false) //раздуваем разметку
        return ImageHolder(view)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position]) //добавляет по каждой позиции ссылку
    }

    override fun getItemCount(): Int {
        return mainArray.size //передаем размер массива чтобы знать сколько нужно нарисовать элементов
    }
    class ImageHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        lateinit var imItem: ImageView

        fun setData(bitmap: Bitmap) {
            imItem = itemView.findViewById(R.id.imItem) //заполняем imageView
            imItem.setImageBitmap(bitmap)
        }
    }

        fun update(newList : ArrayList<Bitmap>){
            mainArray.clear()
            mainArray.addAll(newList)
            notifyDataSetChanged()
    }
}