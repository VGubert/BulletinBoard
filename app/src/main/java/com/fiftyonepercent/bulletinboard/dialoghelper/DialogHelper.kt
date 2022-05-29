package com.fiftyonepercent.bulletinboard.dialoghelper

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import com.fiftyonepercent.bulletinboard.MainActivity
import com.fiftyonepercent.bulletinboard.R
import com.fiftyonepercent.bulletinboard.accounthelper.AccountHelper
import com.fiftyonepercent.bulletinboard.databinding.SignDialogBinding

class DialogHelper(val act:MainActivity) { //Передаем MA чтобы был контекс и доступ к элементам класса.передаем act(MainActivity), чтобы использовать в функциях

    val accHelper = AccountHelper(act) //иницилизрум класс AH

    fun createSignDialog(index: Int){ // создаем функцию для диалога
        val builder = AlertDialog.Builder(act) //иницилизируем Builder, которым создадим диалог. act передаем с MainActivity
        val rootDialogElement = SignDialogBinding.inflate(act.layoutInflater) //Получаем доступ которые находятся на MA
        val view = rootDialogElement.root
        builder.setView(view) //Создаем диалог
        setDialogState(index, rootDialogElement)

        val dialog = builder.create() // для закрытия диалога
        rootDialogElement.btSignUpIn.setOnClickListener { // слушатель на кнопку регистрация или вход
            setOnClickSignUpIn(index, rootDialogElement, dialog)// передаем почту и пароль
        }
        rootDialogElement.btForgetP.setOnClickListener { // слушатель на кнопку забыли пароль
            setOnClickResetPassword(rootDialogElement, dialog)// передаем почту и пароль
        }
        rootDialogElement.btGoogleSignIn.setOnClickListener { // слушатель на кнопку войти по гугл аккаунту
            accHelper.signInWithGoogle()// передаем почту и пароль
            dialog.dismiss() //Выход из диалогового окна
        }

        dialog.show() // показует диалог
    }

    private fun setOnClickResetPassword(rootDialogElement: SignDialogBinding, dialog: AlertDialog?) { //функция для восстановления

        if(rootDialogElement.edSignEmail.text.isNotEmpty()) { //проверка на пустоту
          act.mAuth.sendPasswordResetEmail(rootDialogElement.edSignEmail.text.toString()).addOnCompleteListener { task ->  //оправка письма для воостановления
              if(task.isSuccessful){ //task слушатель отправки
                  Toast.makeText(act, R.string.email_reset_password_was_sent, Toast.LENGTH_LONG).show()
              }
          }
            dialog?.dismiss() //закрываем диалог
        } else {
            rootDialogElement.tvDialogMessage.visibility = View.VISIBLE

        }
    }

    private fun setOnClickSignUpIn(index: Int, rootDialogElement: SignDialogBinding, dialog: AlertDialog?) { // функция для пердачи почты и пароля
        dialog?.dismiss() // Закрываем диалог когда нажали на кнопку регистрации,
        if(index == DialogConst.SIGN_UP_STATE) { //Регистрация
            accHelper.signUpWithEmail(rootDialogElement.edSignEmail.text.toString(),
                rootDialogElement.edSignPassword.text.toString()) // С класса AH берем фун signUpWithEmail. Передаем email и пароль

        } else{ // Вход
            accHelper.signInWithEmail(rootDialogElement.edSignEmail.text.toString(),
                rootDialogElement.edSignPassword.text.toString())

        }
    }

    private fun setDialogState(index: Int, rootDialogElement: SignDialogBinding) { // функция для состояния диалога. Для реги. или входа
        if (index == DialogConst.SIGN_UP_STATE) { // Проверяем вход или регистрация. Ипользуем константы. Приходят с MA

            rootDialogElement.tvSignTitle.text =
                act.resources.getString(R.string.ac_sign_up) // Показываем текст для регистрации
            rootDialogElement.btSignUpIn.text =
                act.resources.getString(R.string.sign_up_action) // Кнопка для регистрации
        } else {
            rootDialogElement.tvSignTitle.text =
                act.resources.getString(R.string.ac_sign_in) // Показываем текст для входа
            rootDialogElement.btSignUpIn.text =
                act.resources.getString(R.string.sign_in_action) // Кнопка для входа
            rootDialogElement.btForgetP.visibility = View.VISIBLE // Кнопка Забыли пароль
        }
    }

    }
