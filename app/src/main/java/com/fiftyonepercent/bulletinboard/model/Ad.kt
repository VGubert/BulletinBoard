package com.fiftyonepercent.bulletinboard.model

import java.io.Serializable

data class Ad(
    val country: String? = null,
    val city: String? = null,
    val tel: String? = null,
    val index: String? = null,
    val withSent: String? = null,
    val category: String? = null,
    val title: String? = null,
    val price: String? = null,
    val description: String? = null,
    val email: String? = null,
    val mainImage: String? = null,
    val image2: String? = null,
    val image3: String? = null,
    val key: String? = null,
    var favCounter: String = "0", //счетсчик
    val uid: String? = null, //индетификатор дается когда создается приложение
    val time: String = "0",

    var isFav: Boolean = false, //избранное

    var viewsCounter: String = "0", //счетсчик просмотров
    var emailsCounter: String = "0",
    var callsCounter: String? = "0"
) :Serializable
