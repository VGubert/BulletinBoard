package com.fiftyonepercent.bulletinboard.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemTouchMoveCallback(val adapter: ItemTouchAdapter) : ItemTouchHelper.Callback() { //callback для ItemTouchHelper чтобы перетаскивать картинки
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int { //какие движения мы хотим замечать. Например вверх или низ
        val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN //предвигаем вверх или низ
        return makeMovementFlags(dragFlag, 0)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean { //Функция для реретаскивания и замены картинок. target меняет картинку
        adapter.onMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) { //функц запускается когда нажали на наш элемент
        if(actionState != ItemTouchHelper.ACTION_STATE_IDLE)viewHolder?.itemView?.alpha = 0.5f //делаем передвигаемый элемент полупрозразным
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) { //Функция чтобы полупрозрачный элемент вернуть в начальное состояние
        viewHolder.itemView.alpha = 1.0f
        adapter.onClear() //для того чтобы обновляеть номера при претаскивании item
        super.clearView(recyclerView, viewHolder)
    }

    interface ItemTouchAdapter{ //интерфейс для изменения позиции картинок
        fun onMove(startPos:Int, targetPos: Int) //пердаем позицию где сейчас Item находится, и позицию которую надо заменить targetPos
        fun onClear()

    }
}