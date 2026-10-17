package com.example.c001apk.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.hihonor.smartgripkit.SmartGripEventManager

/**
 * 荣耀 AI 随心握：跨组件共享的握持状态（荣耀机型专用）。
 *
 * 状态值：GRIP_UNKNOWN=0 / GRIP_LEFT=1 / GRIP_RIGHT=2，为唯一真源，MainActivity 只读它贴底栏。
 *
 * 严格遵循荣耀官方「握持检测能力」生命周期建议：
 *   - firstInit  （Activity.onCreate）：仅查询设备支持状态并构建官方监听器，不注册。
 *   - register   （Activity.onResume）：重新注册官方监听器。官方把注册时机放在 onResume，
 *     意味着每次（重新）注册系统可能补推一次「当前握持状态」——这是首开/切回前台跟手的关键
 *     （荣耀 SmartGrip 只在握持「变化」时回调，无查询当前状态接口，靠重注册补推）。
 *   - unregister （Activity.onPause）：解注册官方监听器。
 *
 * 纯官方 API，不使用加速度计/陀螺仪兜底。
 */
object GripStateHolder {
    const val GRIP_UNKNOWN = 0
    const val GRIP_LEFT = 1
    const val GRIP_RIGHT = 2

    @Volatile
    var currentState: Int = GRIP_UNKNOWN
        private set

    private val observers = LinkedHashSet<() -> Unit>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var smartGripListener: com.hihonor.smartgripkit.SmartGripEventListener? = null
    private var initialized = false
    private var supported = false

    /** 进程内首次调用（MainActivity.onCreate）：查询设备支持状态并构建官方监听器。不在此注册。 */
    fun firstInit(context: Context) {
        if (initialized) return
        initialized = true
        val app = context.applicationContext
        val support = try {
            SmartGripEventManager.getSmartGripSupportState(app)
        } catch (t: Throwable) {
            Log.i(TAG, "SmartGrip unavailable: ${t.javaClass.simpleName}")
            return
        }
        if (support != SmartGripEventManager.SMART_GRIP_SUPPORT) {
            Log.i(TAG, "SmartGrip off, supportState=$support")
            return
        }
        supported = true
        smartGripListener = object : com.hihonor.smartgripkit.SmartGripEventListener() {
            override fun onSmartGripEventChanged(state: Int) {
                val mapped = when (state) {
                    SmartGripEventManager.GRIP_STATE_LEFT_HAND -> GRIP_LEFT
                    SmartGripEventManager.GRIP_STATE_RIGHT_HAND -> GRIP_RIGHT
                    else -> GRIP_UNKNOWN
                }
                setState(mapped)
                Log.i(TAG, "smartgrip state=$mapped")
            }
        }
        Log.i(TAG, "SmartGrip supported, listener built")
    }

    /**
     * 跟随 Activity 生命周期（onResume 调）：重新注册官方监听器。
     * 官方建议的注册时机——每次重新注册系统可能补推一次当前握持状态，解决首开/切回前台不跟手。
     */
    fun register(context: Context) {
        if (!supported) return
        val ok = try {
            SmartGripEventManager.registerSmartGripMotionListener(
                context.applicationContext, smartGripListener
            )
        } catch (t: Throwable) {
            Log.e(TAG, "registerSmartGripMotionListener failed", t)
            false
        }
        Log.i(TAG, "SmartGrip registered=$ok")
    }

    /** 跟随 Activity 生命周期（onPause 调）：解注册官方监听器。 */
    fun unregister(context: Context) {
        if (!supported) return
        try {
            SmartGripEventManager.unregisterSmartGripMotionListener(
                context.applicationContext, smartGripListener
            )
        } catch (t: Throwable) {
            Log.e(TAG, "unregisterSmartGripMotionListener failed", t)
        }
    }

    private fun setState(state: Int) {
        if (state == currentState) return
        currentState = state
        mainHandler.post { observers.forEach { it() } }
    }

    fun addObserver(observer: () -> Unit) {
        observers.add(observer)
        observer()
    }

    fun removeObserver(observer: () -> Unit) {
        observers.remove(observer)
    }

    private const val TAG = "GripStateHolder"
}
