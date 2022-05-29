package com.fiftyonepercent.bulletinboard.adapters

import androidx.recyclerview.widget.DiffUtil
import com.fiftyonepercent.bulletinboard.model.Ad

class DiffUtilHelper(val oldList: List<Ad>, val newList: List<Ad>): DiffUtil.Callback() { //анимация для удаления. Передаем старый и новй список

    override fun getOldListSize(): Int {
        return oldList.size //предаем размер старого списка
    }

    override fun getNewListSize(): Int {
        return newList.size //передаем размер нового списка
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].key == newList[newItemPosition].key //сравниваем ключ по старой и новой позиции
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}