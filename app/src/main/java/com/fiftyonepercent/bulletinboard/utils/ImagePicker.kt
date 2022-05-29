package com.fiftyonepercent.bulletinboard.utils

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import com.fiftyonepercent.bulletinboard.R
import com.fiftyonepercent.bulletinboard.act.EditAdsAct
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ImagePicker {
    const val MAX_IMAGE_COUNT = 3 //максимальное число фотографий которе можно добавить
    const val REQUEST_CODE_GET_IMAGES = 999
    const val REQUEST_CODE_GET_SINGLE_IMAGES = 998 //для одной картинки
    private fun getOptions(imageCounter: Int): Options {
        val options = Options().apply {
            count = imageCounter
            isFrontFacing = false
            mode = Mode.Picture
            path = "/pix/images"
        }

        return options
    }

    fun getMultiImages (edAct: EditAdsAct, imageCounter: Int) { //открывает фрагмент от бибилотеки pix где мы можешь выбрать картинки
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result -> //библиотека pix займет place holder, то есть теперь pix фрагмент
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    getMultiSelectedImages(edAct, result.data) //функция выбора картинок. result.data ссылки которые пользователь выбра
                }
            }
        }
    }

    fun addImages (edAct: EditAdsAct, imageCounter: Int) { //открывает фрагмент от бибилотеки pix где мы можешь выбрать картинки
        edAct.addPixToActivity(R.id.place_holder, getOptions(imageCounter)) { result -> //библиотека pix займет place holder, то есть теперь pix фрагмент
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {
                    openChooseImageFrag(edAct) //открываем фрагмент который передадим
                        edAct.chooseImageFrag?.updateAdapter(result.data as ArrayList<Uri>, edAct) //обновляем фрагмент, не перезаписуя все данные
                }
            }
        }
    }


    fun getSingleImage (edAct: EditAdsAct) { //открывает фрагмент от бибилотеки pix где мы можешь выбрать одну картинку
        edAct.addPixToActivity(R.id.place_holder, getOptions(1)) { result -> //библиотека pix займет place holder, то есть теперь pix фрагмент
            when (result.status) {
                PixEventCallback.Status.SUCCESS -> {

                    openChooseImageFrag(edAct)
                    singleImage(edAct, result.data[0]) // result.data[0] отсюда берем uri по 0 позиции так как картинка одна
                }
            }
        }
    }

    private fun openChooseImageFrag(edAct: EditAdsAct) {
        edAct.supportFragmentManager.beginTransaction().replace(R.id.place_holder, edAct.chooseImageFrag!!).commit() //R.id.place_holder, меняем на f
    }

    private fun closePixFrag(edAct: EditAdsAct) { //закрытие pix фрагмента
        val fList = edAct.supportFragmentManager.fragments //берем фрагмент
        fList.forEach{
            if(it.isVisible) edAct.supportFragmentManager.beginTransaction().remove(it).commit() //закрывется фрагмент
        }
    }

    fun getMultiSelectedImages(edAct: EditAdsAct, uris: List<Uri>) { //с помощью данной функции мы будем запускать лаунчер для того чтобы получить несколько картинок

        if (uris.size > 1 && edAct.chooseImageFrag == null) {
            edAct.openChooseImageFrag(uris as ArrayList<Uri>)//выбор изображения
        }  else if (uris.size == 1 && edAct.chooseImageFrag == null) { //если выбираем одну картинку
            CoroutineScope(Dispatchers.Main).launch { //создаем корутину, делаем все на основном потоке
                edAct.binding.pBarLoad.visibility = View.VISIBLE //делаем видимым прогресс бар
                val bitMapArray = ImageManager.imageResize(uris as ArrayList<Uri>, edAct) as ArrayList<Bitmap> //функция imageResize выдаст массив с bitMap
                edAct.binding.pBarLoad.visibility = View.GONE //делаем не видимым прогресс бар
                edAct.imageAdapter.update(bitMapArray) //добавляем в адартер
                closePixFrag(edAct)
            }
        }
    }

    private fun singleImage(edAct: EditAdsAct, uri: Uri) { // для выбора одинароной картинки
        edAct.chooseImageFrag?.setSingleImage(uri, edAct.editImagePos) //применям функцию для редактирования одного фото. Передаем ссылку и позицию

    }
}