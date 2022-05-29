package com.fiftyonepercent.bulletinboard.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.fiftyonepercent.bulletinboard.adapters.ImageAdapter
import com.fiftyonepercent.bulletinboard.model.Ad
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*

import java.io.File
import java.io.InputStream

object ImageManager { //для работы с размером изображения
    private const val MAX_IMAGE_SIZE = 1000 //максимальный размер картинки
    private const val WIDTH = 0 //ширина
    private const val HEIGHT = 1 //высота

    fun getImageSize(uri: Uri, act: Activity) : List<Int>{ //получаем размер изображения
        val inStream = act.contentResolver.openInputStream(uri) //открываем поток чтобы получить данные
        val options = BitmapFactory.Options().apply { //считываем размер и тип изображения
            inJustDecodeBounds = true //берем край из файла которого получаем. Здесь указываем что хотим сделать с файлом
        }
        BitmapFactory.decodeStream(inStream,null, options) //берем файл
        return listOf(options.outWidth, options.outHeight) //горизонтальное положение

    }

    fun chooseScaleType(im: ImageView, bitMap: Bitmap) { //определяем горизонтальная или вертикальная картинка. И распологаем их в адаптере

        if(bitMap.width > bitMap.height) { //если ширина больше чем высота. ТО картинка горизонтальная
            im.scaleType = ImageView.ScaleType.CENTER_CROP //распологаем как есть, то есть как горизонтальную
        }
        else {
            im.scaleType = ImageView.ScaleType.CENTER_INSIDE //распологаем наоборот, то есть как вертикальную
        }
    }

    suspend fun imageResize(uris: List<Uri>, act: Activity): List<Bitmap> = withContext(Dispatchers.IO) { //Функция уменьшения размера. suspend функция не перейдет дальше пока не закончит свою работу. withContext(Dispatchers.IO) будет работать на второстепенном. Но если suspend то он не может работать ы других функциях
        val tempList = ArrayList<List<Int>>() //создаем массив. В котором еще два элемента: ширина и высота
        val bitmapList = ArrayList<Bitmap>() //передаем Bitmap которые будем сжимать
        for(n in uris.indices) { //перебирает картинки

            val size = getImageSize(uris[n], act) //берем с позиции

            val imageRatio = size[WIDTH].toFloat() / size[HEIGHT].toFloat() //ширину делим на высоту. toFloat() что бы было дробное число

            if(imageRatio > 1) { //проверяем что больше ширина или высота?. Если пропорция больше 1 то картинка горизонтальная. Значит большая сторона то ширина

                if(size[WIDTH] > MAX_IMAGE_SIZE) { //если ширина больше максимального размера

                    tempList.add(listOf(MAX_IMAGE_SIZE, (MAX_IMAGE_SIZE / imageRatio). toInt())) //ширину делаем максимального размера. Высоту соблюдаем пропорцию

                } else { //если ширина не превышает максимальный размер

                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))

                }

            } else { // если imageRatio меньше 1. То картинка вертикальная

                if(size[HEIGHT] > MAX_IMAGE_SIZE) { //если высота больше максимального размера

                    tempList.add(listOf((MAX_IMAGE_SIZE * imageRatio).toInt(), MAX_IMAGE_SIZE)) //ширину умнажаем на пропорцию. Высоту делаем максимальным размером

                } else { //если ширина не превышает максимальный размер

                    tempList.add(listOf(size[WIDTH], size[HEIGHT]))

                }
            }

        }

        for(i in uris.indices) {

            kotlin.runCatching {
            bitmapList.add(
                Picasso.get().load(uris[i]).resize(tempList[i][WIDTH], tempList[i][HEIGHT])
                    .get()
            ) } //с помощью библиотеки picasso берем ссылку. Ссылку уменьшить размер и записать bitmap в список. resize уменьшает размер
        }
        return@withContext bitmapList
    }

    private suspend fun getBitmapFromUris(uris: List<String?>): List<Bitmap> = withContext(Dispatchers.IO) { //функция чтобы получить картиники с firebase без сжатия, то есть получить уже сжатые

        val bitmapList = ArrayList<Bitmap>() //создаем временный массив

        for(i in uris.indices) {

            kotlin.runCatching {
                bitmapList.add(Picasso.get().load(uris[i]).get()) //получаем с помощью библиотеки Picasso битмапы
            }
        }
        return@withContext bitmapList
    }

    fun fillImageArray(ad: Ad, adapter: ImageAdapter) { //будет заполнять массив с ссылками
        val listUris = listOf(ad.mainImage, ad.image2, ad.image3) //передаем все ссылки
        CoroutineScope(Dispatchers.Main).launch { //когда закончатся действий на второстепенном потоке, запустится на основном
            val bitMapList = getBitmapFromUris(listUris) //превращаем ссылки в битмап
            adapter.update(bitMapList as ArrayList<Bitmap>)
        }
    }
}
