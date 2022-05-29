package com.fiftyonepercent.bulletinboard.frag

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fiftyonepercent.bulletinboard.R
import com.fiftyonepercent.bulletinboard.act.EditAdsAct
import com.fiftyonepercent.bulletinboard.databinding.SelectImageFragItemBinding
import com.fiftyonepercent.bulletinboard.utils.AdapterCallback
import com.fiftyonepercent.bulletinboard.utils.ImageManager
import com.fiftyonepercent.bulletinboard.utils.ImagePicker
import com.fiftyonepercent.bulletinboard.utils.ItemTouchMoveCallback

class SelectImageRvAdapter(val adapterCallback: AdapterCallback) : RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(), ItemTouchMoveCallback.ItemTouchAdapter { //ItemTouchMoveCallback.ItemTouchAdapter передаем для замены картинок
    val mainArray = ArrayList<Bitmap>() //Создаем массив где будут хранится все item

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder { //когда все рисуется
        val viewBinding = SelectImageFragItemBinding.inflate(LayoutInflater.from(parent.context), parent, false) //надуваем разметку
        return ImageHolder(viewBinding, parent.context, this)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) { //когда все заполняется
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size //передаем размер массива
    }

    override fun onMove(startPos: Int, targetPos: Int) { //Функция для передвижения картинок и замены
        val targetItem = mainArray[targetPos] //с позиции которую хотим заменить сохраняем чтобы item не исчез
        mainArray[targetPos] = mainArray[startPos] //с элемента startPos(элемент который мы взяли переписуем на место которое меняем targetPos
        mainArray[startPos] = targetItem //меням элемент на который ставиться другой item. И преставляем его на место претаскиваемого item
        notifyItemMoved(startPos, targetPos) //указываем откуда и куда перетащили

    }

    override fun onClear() {
        notifyDataSetChanged() //Когда отпускам item обновляем номер
    }


    class ImageHolder(private val viewBinding: SelectImageFragItemBinding, val context: Context, val adapter : SelectImageRvAdapter) : RecyclerView.ViewHolder(viewBinding.root) { //передаем адаптер для возможности удаления

        fun setData(bitMap: Bitmap) {

            viewBinding.imEditImage.setOnClickListener{

                ImagePicker.getSingleImage(context as EditAdsAct) // imageCounter кол-во картинок которое я хочу редактировать.
                context.editImagePos = adapterPosition
            }

            viewBinding.imDelete.setOnClickListener { //слушатель на кнопку удаления
                adapter.mainArray.removeAt(adapterPosition) //удаляем изображение по adapterPosition
                adapter.notifyItemRemoved(adapterPosition)
                for(n in 0 until adapter.mainArray.size) adapter.notifyItemChanged(n) //цикл от 0 до размера нашего массивы. notifyItemChanged презаписует заголовки когла удаляем картинку
                adapter.adapterCallback.onItemDelete() //когда 3 картинки, и нажали на кнопку удланеия отдельного item, появится кнопка добавить картинку
            }

            viewBinding.tvTitle.text = context.resources.getStringArray(R.array.title_array)[adapterPosition]  //берем из массива текст для надпись над фото
            ImageManager.chooseScaleType(viewBinding.imageView, bitMap) //добавляем функцию которая определяет ориентацию картинки. И правлильно ее расплогает
            viewBinding.imageView.setImageBitmap(bitMap) //берем из нашего setImageBitmap изображение. Из класса ImageListFrag
        }
    }

    fun updateAdapter(newList: List<Bitmap>, needClear : Boolean) {
        if(needClear) mainArray.clear() //очищаем список. Если true значит нужно очищать
        mainArray.addAll(newList) //заполняем новыми данными
        notifyDataSetChanged() //чтобы запустилисб обновления
    }


}