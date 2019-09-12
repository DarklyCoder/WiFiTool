package com.darklycoder.wifitool.lib.info.action

import com.darklycoder.wifitool.lib.type.Types

abstract class IWiFiAction internal constructor() {

    @Types.ActionStateType
    var actionState: Int = 0
        private set

    private val actionName: String
        get() = javaClass.simpleName

    init {
        setState(Types.ActionStateType.WAITING)
    }

    fun setState(@Types.ActionStateType state: Int) {
        this.actionState = state
    }

    open fun end() {
        this.actionState = Types.ActionStateType.END
    }

    override fun toString(): String {
        return "$actionName | actionState:$actionState"
    }

}
