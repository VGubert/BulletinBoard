package com.fiftyonepercent.bulletinboard.utils

import com.fiftyonepercent.bulletinboard.model.Ad
import com.fiftyonepercent.bulletinboard.model.AdFilter
import java.lang.StringBuilder

object FilterManager { //менеджер для фильтра чтоюы использовать DbManager
    fun createFilter(ad: Ad) : AdFilter {
        return AdFilter( //заполняем класс AdFilter чтобы предать в DbManager
            ad.time,
        "${ad.category}_${ad.time}",
        "${ad.category}_${ad.country}_${ad.withSent}_${ad.time}",
        "${ad.category}_${ad.country}_${ad.city}_${ad.withSent}_${ad.time}",
        "${ad.category}_${ad.country}_${ad.city}_${ad.index}_${ad.withSent}_${ad.time}",
        "${ad.category}_${ad.index}_${ad.withSent}_${ad.time}",
        "${ad.category}_${ad.withSent}_${ad.time}",

        "${ad.country}_${ad.withSent}_${ad.time}",
        "${ad.country}_${ad.city}_${ad.withSent}_${ad.time}",
        "${ad.country}_${ad.city}_${ad.index}_${ad.withSent}_${ad.time}",
        "${ad.index}_${ad.withSent}_${ad.time}",
        "${ad.withSent}_${ad.time}"
        )
    }

    fun getFilter(filter: String): String{
        val sBuilderNode = StringBuilder()
        val sBuilderFilter = StringBuilder()
        val tempArray = filter.split("_")
        if(tempArray[0] != "empty") {
            sBuilderNode.append("country_") //если страна не пустой то добавляем в sBuilderNode
            sBuilderFilter.append("${tempArray[0]}_")
        }
        if(tempArray[1] != "empty") {
            sBuilderNode.append("city_") //если город не пустой то добавляем в sBuilderNode
            sBuilderFilter.append("${tempArray[1]}_")
        }
        if(tempArray[2] != "empty") { //если индекс не пустой то добавляем в sBuilderNode
            sBuilderNode.append("index_")
            sBuilderFilter.append("${tempArray[2]}_")
        }
        sBuilderFilter.append(tempArray[3])
        sBuilderNode.append("withSent_time") //в конце добавляется с отправкой и время
        return "$sBuilderNode|$sBuilderFilter"
    }
}