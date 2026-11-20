package com.multisteel.bambuwidget

import android.content.Context
import android.content.SharedPreferences

/** Configurações salvas no aparelho. */
class Prefs(context: Context) {
    private val sp: SharedPreferences =
        context.getSharedPreferences("bambu", Context.MODE_PRIVATE)

    var ip: String
        get() = sp.getString("ip", "") ?: ""
        set(v) = sp.edit().putString("ip", v.trim()).apply()

    var accessCode: String
        get() = sp.getString("code", "") ?: ""
        set(v) = sp.edit().putString("code", v.trim()).apply()

    var serial: String
        get() = sp.getString("serial", "") ?: ""
        set(v) = sp.edit().putString("serial", v.trim().uppercase()).apply()

    var tempPla: Int
        get() = sp.getInt("tempPla", 220)
        set(v) = sp.edit().putInt("tempPla", v).apply()

    var tempPetg: Int
        get() = sp.getInt("tempPetg", 250)
        set(v) = sp.edit().putInt("tempPetg", v).apply()

    /** Quantos mm de filamento empurrar no load. */
    var loadLength: Int
        get() = sp.getInt("loadLength", 50)
        set(v) = sp.edit().putInt("loadLength", v).apply()

    /** "stainless_steel" ou "hardened_steel". */
    var nozzleType: String
        get() = sp.getString("nozzleType", "stainless_steel") ?: "stainless_steel"
        set(v) = sp.edit().putString("nozzleType", v).apply()

    /** Último bico escolhido no widget (usado para ajustar a velocidade do load). */
    var currentNozzle: Float
        get() = sp.getFloat("currentNozzle", 0.4f)
        set(v) = sp.edit().putFloat("currentNozzle", v).apply()

    var lastStatus: String
        get() = sp.getString("lastStatus", "Pronto") ?: "Pronto"
        set(v) = sp.edit().putString("lastStatus", v).apply()

    fun isConfigured() = ip.isNotEmpty() && accessCode.isNotEmpty() && serial.isNotEmpty()
}
