package com.breez.senmit2sapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Класс Application, необходимый для инициализации Hilt.
 * Эта аннотация запускает генерацию кода Dagger, который будет использоваться
 * для внедрения зависимостей во всем приложении.
 */
@HiltAndroidApp
class App : Application()