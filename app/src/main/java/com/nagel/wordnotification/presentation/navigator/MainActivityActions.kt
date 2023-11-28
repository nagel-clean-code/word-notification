package com.nagel.wordnotification.presentation.navigator

import com.nagel.wordnotification.presentation.MainActivity

typealias MainActivityAction = (MainActivity) -> Unit

class MainActivityActions {
    var mainActivity: MainActivity? = null
        set(activity) {
            field = activity
            if (activity != null) {
                actions.forEach { it(activity) }
                clear()
            }
        }
    private val actions = mutableListOf<MainActivityAction>()

    operator fun invoke(action: MainActivityAction){
        if(mainActivity == null){
            actions += action
        }else{
            action.invoke(mainActivity!!)
        }
    }

    fun clear() {
        actions.clear()
    }
}