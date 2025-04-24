package com.lortey.cardflare

import android.content.Context
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

public var FSRSInstance:FSRS? = null
fun fsrsSafeInit(context: Context):FSRS{
    if(FSRSInstance == null){
        val newFsrs = FSRS()
        newFsrs.initialize(context)
        FSRSInstance = newFsrs
    }
    return FSRSInstance!!
}
public class FSRS{
    private var module:PyObject? = null
    private fun getPython(context: Context):Python{
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }

        val python = Python.getInstance()
        return python
    }
    private fun getModule(context:Context): PyObject {
        return module ?: getPython(context).getModule("FSRSManager")
    }
    public fun initialize(context: Context){
        val module = getModule(context)
        module.callAttr("initialization")
    }

    public fun reviewCard(context: Context, cardId:Int, ){

    }

}