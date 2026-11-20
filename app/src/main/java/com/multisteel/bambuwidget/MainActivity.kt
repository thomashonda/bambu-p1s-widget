package com.multisteel.bambuwidget

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import kotlin.concurrent.thread

/** Tela de configuração. */
class MainActivity : Activity() {

    private val types = listOf("stainless_steel", "hardened_steel")
    private val typeLabels = listOf("Aço inox", "Aço endurecido")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val p = Prefs(this)

        val ip = findViewById<EditText>(R.id.ip)
        val code = findViewById<EditText>(R.id.code)
        val serial = findViewById<EditText>(R.id.serial)
        val tPla = findViewById<EditText>(R.id.temp_pla)
        val tPetg = findViewById<EditText>(R.id.temp_petg)
        val len = findViewById<EditText>(R.id.length)
        val type = findViewById<Spinner>(R.id.nozzle_type)
        val result = findViewById<TextView>(R.id.result)

        type.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, typeLabels)

        ip.setText(p.ip)
        code.setText(p.accessCode)
        serial.setText(p.serial)
        tPla.setText(p.tempPla.toString())
        tPetg.setText(p.tempPetg.toString())
        len.setText(p.loadLength.toString())
        type.setSelection(types.indexOf(p.nozzleType).coerceAtLeast(0))

        fun save() {
            p.ip = ip.text.toString()
            p.accessCode = code.text.toString()
            p.serial = serial.text.toString()
            p.tempPla = tPla.text.toString().toIntOrNull()?.coerceIn(180, 280) ?: 220
            p.tempPetg = tPetg.text.toString().toIntOrNull()?.coerceIn(180, 280) ?: 250
            p.loadLength = len.text.toString().toIntOrNull()?.coerceIn(5, 150) ?: 50
            p.nozzleType = types[type.selectedItemPosition]
            BambuWidget.refreshAll(this)
        }

        findViewById<Button>(R.id.save).setOnClickListener {
            save()
            result.text = "Salvo. Agora adicione o widget na tela inicial."
        }

        findViewById<Button>(R.id.test).setOnClickListener {
            save()
            result.text = "Testando conexão…"
            thread {
                val msg = try {
                    BambuMqtt.send(p, null)
                    "✓ Conectou na impressora"
                } catch (e: Exception) {
                    "✗ Falhou: ${e.message ?: e.javaClass.simpleName}\n" +
                        "Confira IP, código de acesso e se o Modo Desenvolvedor está ligado."
                }
                runOnUiThread { result.text = msg }
            }
        }
    }
}
