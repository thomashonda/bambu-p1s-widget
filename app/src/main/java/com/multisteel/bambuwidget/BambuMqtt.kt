package com.multisteel.bambuwidget

import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Fala com a P1S pela rede local (MQTT na porta 8883, TLS).
 * A impressora precisa estar em Modo LAN + Modo Desenvolvedor.
 */
object BambuMqtt {

    // ---------- Comandos ----------

    /** Aquece e empurra o filamento (carretel externo). */
    fun loadPayload(tempC: Int, lengthMm: Int, nozzle: Float): String {
        // Velocidade do filamento conforme o bico, para não passar da vazão máxima.
        val feed = when {
            nozzle <= 0.25f -> 40   // 0.2 mm  (~0,7 mm/s de filamento)
            nozzle <= 0.45f -> 150  // 0.4 mm  (~2,5 mm/s)
            else -> 200             // 0.6 mm  (~3,3 mm/s)
        }
        val gcode = "M109 S$tempC\n" +  // aquece e espera
            "M83\n" +                    // extrusão relativa
            "G1 E$lengthMm F$feed\n" +   // empurra o filamento
            "M400\n" +                   // espera terminar
            "M104 S0\n"                  // desliga o aquecimento
        return JSONObject().put(
            "print", JSONObject()
                .put("sequence_id", "0")
                .put("command", "gcode_line")
                .put("param", gcode)
        ).toString()
    }

    /** Mesma rotina de descarregar usada pelo Bambu Studio. */
    fun unloadPayload(): String = JSONObject().put(
        "print", JSONObject()
            .put("sequence_id", "0")
            .put("command", "gcode_file")
            .put("param", "/usr/etc/print/filament_unload.gcode")
    ).toString()

    /** Informa à impressora qual bico está montado (não troca nada fisicamente). */
    fun nozzlePayload(diameter: Float, type: String): String = JSONObject().put(
        "system", JSONObject()
            .put("sequence_id", "0")
            .put("command", "set_accessories")
            .put("accessory_type", "nozzle")
            .put("nozzle_diameter", diameter.toDouble())
            .put("nozzle_type", type)
    ).toString()

    // ---------- Conexão ----------

    /** Conecta, publica e desconecta. Rode fora da thread principal. */
    fun send(prefs: Prefs, payload: String?) {
        require(prefs.isConfigured()) { "Configure IP, código e nº de série no app" }
        val client = MqttClient(
            "ssl://${prefs.ip}:8883",
            "bambuwidget-${System.currentTimeMillis()}",
            MemoryPersistence()
        )
        val opts = MqttConnectOptions().apply {
            userName = "bblp"
            password = prefs.accessCode.toCharArray()
            socketFactory = trustAllFactory()
            isHttpsHostnameVerificationEnabled = false
            connectionTimeout = 8
            isCleanSession = true
            mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1_1
        }
        try {
            client.connect(opts)
            if (payload != null) {
                val msg = MqttMessage(payload.toByteArray()).apply { qos = 0 }
                client.publish("device/${prefs.serial}/request", msg)
                Thread.sleep(300) // dá tempo de a mensagem sair antes de desconectar
            }
            client.disconnect(2000)
        } finally {
            runCatching { client.close() }
        }
    }

    /**
     * A impressora usa um certificado próprio (autoassinado), então aceitamos
     * qualquer certificado. Uso apenas dentro da rede local.
     */
    private fun trustAllFactory(): SSLSocketFactory {
        val tm = object : X509TrustManager {
            override fun checkClientTrusted(c: Array<X509Certificate>, a: String) {}
            override fun checkServerTrusted(c: Array<X509Certificate>, a: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
        val ctx = SSLContext.getInstance("TLS")
        ctx.init(null, arrayOf<TrustManager>(tm), SecureRandom())
        return ctx.socketFactory
    }
}
