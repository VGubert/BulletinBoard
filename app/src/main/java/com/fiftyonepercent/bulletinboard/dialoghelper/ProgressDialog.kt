package com.fiftyonepercent.bulletinboard.dialoghelper

import android.app.Activity
import android.app.AlertDialog
import com.fiftyonepercent.bulletinboard.databinding.ProgressDialogLayoutBinding
import com.fiftyonepercent.bulletinboard.databinding.SignDialogBinding

object ProgressDialog { //прогресс бар на картинку когда загружаем

    fun createProgressDialog(act: Activity): AlertDialog{ // создаем функцию для диалога
        val builder = AlertDialog.Builder(act) //иницилизируем Builder, которым создадим диалог. act передаем с MainActivity
        val rootDialogElement = ProgressDialogLayoutBinding.inflate(act.layoutInflater) //Получаем доступ которые находятся на MA
        val view = rootDialogElement.root
        builder.setView(view) //Создаем диалог

        val dialog = builder.create() // для закрытия диалога
        dialog.setCancelable(false) //прогресс бар нельзя будет остановить, например нажав на экран. А только когда он закончится

        dialog.show() // показует диалог
        return dialog
    }


}