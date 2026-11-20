# Bambu P1S Widget

Widget para Android com seis botões:

- **Load PLA / Load PETG**: aquece o bico, empurra o filamento e desliga o aquecimento.
- **Unload**: roda a mesma rotina de descarregar do Bambu Studio.
- **0.2 / 0.4 / 0.6**: informa à impressora qual bico está montado. O bico atual aparece marcado com ●.

## 1. Preparar a impressora

Na tela da P1S, vá em Configurações → WLAN → **LAN Only: ON** → **Developer Mode: ON**.
Anote o **IP** e o **código de acesso**. O número de série fica em Configurações → Dispositivo.

> Com o Modo LAN ligado, a impressora deixa de funcionar pelo app Bambu Handy e pela nuvem.
> O celular precisa estar no mesmo Wi-Fi que ela.

## 2. Gerar o APK

**Opção A: GitHub, sem instalar nada**
1. Crie um repositório (pode ser privado) e envie esta pasta inteira.
2. Abra a aba **Actions**. O "Build APK" roda sozinho e leva uns 3 a 5 minutos.
3. Abra a execução e baixe o artefato **BambuWidget-apk**, que vem num .zip com o `app-debug.apk` dentro.
4. Passe o APK para o celular e instale. É preciso permitir "instalar apps desconhecidos".

**Opção B: Android Studio**
Abra a pasta (File → Open). O Android Studio baixa o Gradle e o SDK sozinho.
Depois é só clicar em Run com o celular conectado por USB.

## 3. Usar

1. Abra o app "Bambu P1S" e preencha IP, código e número de série. Toque em **Testar conexão**.
2. Ajuste as temperaturas (padrão: PLA 220 °C, PETG 250 °C), quantos mm empurrar (padrão: 50) e o tipo dos bicos.
3. Segure na tela inicial → Widgets → "Bambu P1S" e arraste o widget para a tela.
4. Tocar na linha de status do widget abre a configuração.

## Cuidados

- **Load:** o filamento sai por onde o cabeçote estiver. Use com o cabeçote parado na posição
  de descanso (atrás, junto à saída de purga), como fica depois de uma impressão ou do home.
  Coloque o filamento na entrada da extrusora antes de tocar.
- A velocidade do load depende do bico selecionado no widget: mais devagar no 0.2.
  Por isso, mantenha o bico do widget igual ao que está montado.
- **Unload:** se o primeiro teste não aquecer o bico, aqueça antes pela tela e me avise para ajustarmos.
- Botões de bico: o formato do comando (`set_accessories`) vem de bibliotecas da comunidade.
  Na primeira vez, confira em Configurações → Acessórios → Bico se o valor mudou.
- Não use os botões durante uma impressão.
