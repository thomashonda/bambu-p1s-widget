package com.multisteel.bambuwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import android.widget.Toast
import kotlin.concurrent.thread

class BambuWidget : AppWidgetProvider() {

    companion object {
        const val LOAD_PLA = "com.multisteel.bambuwidget.LOAD_PLA"
        const val LOAD_PETG = "com.multisteel.bambuwidget.LOAD_PETG"
        const val UNLOAD = "com.multisteel.bambuwidget.UNLOAD"
        const val NOZZLE_02 = "com.multisteel.bambuwidget.NOZZLE_02"
        const val NOZZLE_04 = "com.multisteel.bambuwidget.NOZZLE_04"
        const val NOZZLE_06 = "com.multisteel.bambuwidget.NOZZLE_06"

        private val ACTIONS = listOf(LOAD_PLA, LOAD_PETG, UNLOAD, NOZZLE_02, NOZZLE_04, NOZZLE_06)

        fun refreshAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val cn = ComponentName(context, BambuWidget::class.java)
            mgr.updateAppWidget(cn, buildViews(context))
        }

        private fun buildViews(context: Context): RemoteViews {
            val prefs = Prefs(context)
            val v = RemoteViews(context.packageName, R.layout.widget)

            v.setTextViewText(R.id.status, prefs.lastStatus)

            val cur = prefs.currentNozzle
            v.setTextViewText(R.id.btn_n02, if (cur == 0.2f) "● 0.2" else "0.2")
            v.setTextViewText(R.id.btn_n04, if (cur == 0.4f) "● 0.4" else "0.4")
            v.setTextViewText(R.id.btn_n06, if (cur == 0.6f) "● 0.6" else "0.6")

            v.setOnClickPendingIntent(R.id.btn_load_pla, pi(context, LOAD_PLA, 1))
            v.setOnClickPendingIntent(R.id.btn_load_petg, pi(context, LOAD_PETG, 2))
            v.setOnClickPendingIntent(R.id.btn_unload, pi(context, UNLOAD, 3))
            v.setOnClickPendingIntent(R.id.btn_n02, pi(context, NOZZLE_02, 4))
            v.setOnClickPendingIntent(R.id.btn_n04, pi(context, NOZZLE_04, 5))
            v.setOnClickPendingIntent(R.id.btn_n06, pi(context, NOZZLE_06, 6))

            // Tocar no status abre a tela de configuração
            val open = PendingIntent.getActivity(
                context, 0, Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            v.setOnClickPendingIntent(R.id.status, open)
            return v
        }

        private fun pi(context: Context, action: String, code: Int): PendingIntent {
            val i = Intent(context, BambuWidget::class.java).setAction(action)
            return PendingIntent.getBroadcast(
                context, code, i,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        refreshAll(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        if (action !in ACTIONS) return

        val prefs = Prefs(context)
        val (label, payload, newNozzle) = when (action) {
            LOAD_PLA -> Triple("Load PLA (${prefs.tempPla}°C)",
                BambuMqtt.loadPayload(prefs.tempPla, prefs.loadLength, prefs.currentNozzle), null)
            LOAD_PETG -> Triple("Load PETG (${prefs.tempPetg}°C)",
                BambuMqtt.loadPayload(prefs.tempPetg, prefs.loadLength, prefs.currentNozzle), null)
            UNLOAD -> Triple("Unload", BambuMqtt.unloadPayload(), null)
            NOZZLE_02 -> Triple("Bico 0.2", BambuMqtt.nozzlePayload(0.2f, prefs.nozzleType), 0.2f)
            NOZZLE_04 -> Triple("Bico 0.4", BambuMqtt.nozzlePayload(0.4f, prefs.nozzleType), 0.4f)
            else -> Triple("Bico 0.6", BambuMqtt.nozzlePayload(0.6f, prefs.nozzleType), 0.6f)
        }

        prefs.lastStatus = "Enviando: $label…"
        refreshAll(context)

        val pending = goAsync()
        val app = context.applicationContext
        thread {
            val msg = try {
                BambuMqtt.send(prefs, payload)
                if (newNozzle != null) prefs.currentNozzle = newNozzle
                "✓ $label"
            } catch (e: Exception) {
                "✗ $label: ${e.message ?: e.javaClass.simpleName}"
            }
            prefs.lastStatus = msg
            refreshAll(app)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(app, msg, Toast.LENGTH_SHORT).show()
            }
            pending.finish()
        }
    }
}
