
#include <HTTPClient.h>
#include <Wire.h>
#include <BH1750.h>

const char* WIFI_SSID = "RT-WiFi_5B84";
const char* WIFI_PASSWORD = "23834659";
IPAddress address(${device_address}); //unique static ip address
IPAddress gateway(10,10,10,1);   //gateway and dns servers

WiFiServer server(${http_port});
String deviceName = "${device_name}";

char inputLine[80]; // input buffer
int charCount = 0; // count of input buffer
String requestValue;

int valveRelay = 4; // GPIO pin of valve relay
BH1750 lightMeter(0x23); //(0x5C) - if addr pin to 3.3V 

/************************************************************************************************/
void setup() {   
    Serial.begin(9600);// иницилизируем монитор порта
    delay(2000); // запас времени на открытие монитора порта — 2 секунды
    if (!WiFi.config(address, gateway, IPAddress(255,255,255,0), gateway, gateway)){
        Serial.println("Address configure failed");
    }
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);// подключаемся к Wi-Fi сети
    while (WiFi.status() != WL_CONNECTED) {
        delay(1000);
        Serial.println("Connecting to Wi-Fi..");
    }
    Serial.println("Connected to the Wi-Fi network!");
    server.begin();

    pinMode(valveRelay, OUTPUT);
    Wire.begin();
    lightMeter.begin();
}
/************************************************************************************************/
bool isGettingValue(char* variable){
    if (deviceName.isEmpty()) return false;
    String sampleStr = "GET /" + deviceName + "_" + variable;
    int sampleLen = sampleStr.length();
    char sampleChr[sampleLen];
    sampleStr.toCharArray(sampleChr, sampleLen);
    return (strstr(inputLine, sampleChr) > 0);
}
/************************************************************************************************/
bool isPostingValue(char* variable){
    String sampleStr = "POST /";
    if (String(variable).isEmpty()) {
        sampleStr += "device_name=";
    } else {
        if (deviceName == "") return false;
        sampleStr += deviceName + "_" + variable + "=";
    };
    int sampleLen = sampleStr.length();
    char sampleChr[sampleLen];
    sampleStr.toCharArray(sampleChr, sampleLen);
    if (!(strstr(inputLine, sampleChr) > 0)) return false;
    int lastSpace = String(inputLine).lastIndexOf(' ');
    requestValue = String(inputLine).substring(sampleLen, lastSpace);
    return true;
}
/************************************************************************************************/
void loop(){
    WiFiClient client = server.available();
    if (!client) return;
    memset(inputLine, 0, sizeof(inputLine));
    charCount = 0;
    requestValue = "";    
    boolean currentLineIsBlank = true; // HTTP-запрос заканчивается пустой строкой
    while (client.connected()) {
        if (!client.available()) continue;
        char c = client.read();
        Serial.write(c);
        inputLine[charCount] = c;
        if (charCount < sizeof(inputLine) - 1) charCount++;

        // на символ конца строки отправляем ответ
        if (c == '\n' && currentLineIsBlank) {
            // отправляем стандартный заголовок HTTP-ответа
            if (deviceName.isEmpty()){
                client.println("HTTP/1.1 500 INTERNAL SERVER ERROR");
            } else if (requestValue.isEmpty()){
                client.println("HTTP/1.1 400 BAD REQUEST");
            } else { 
                client.println("HTTP/1.1 200 OK");
            }
            client.println("Content-Type: text/html");
            client.println("Connection: close");
            client.println();
            client.println(requestValue);
            break;
        }
        if (c == '\n') {
            currentLineIsBlank = true;
            char* value;
            if (isPostingValue("")){
                setDeviceName(requestValue);
            }else if (isPostingValue("valveRelay")){
                setValveRelay(requestValue);
            }else if (isGettingValue("lightLevel")){
                requestValue = getLightLevel();
            }

            currentLineIsBlank = true; // начинаем новую строку
            memset(inputLine, 0, sizeof(inputLine));
            charCount = 0;
        } else if (c != '\r') {
            // в строке попался новый символ
            currentLineIsBlank = false;
        }
    }
    delay(1);  // даем веб-браузеру время, чтобы получить данные
    client.stop(); // закрываем соединение
    Serial.println("client disconnected");
}
/************************************************************************************************/
void setDeviceName(String value){
    deviceName = value;
    Serial.println("Set device name is " + value);
}
/************************************************************************************************/
void setValveRelay(String value){
    if (value = "on") {
        digitalWrite(valveRelay, HIGH);
    } else {
        digitalWrite(valveRelay, LOW);
    }
    Serial.println("Set valve relay is " + value);
}
/************************************************************************************************/
float getLightLevel(){
  return lightMeter.readLightLevel();
}
/************************************************************************************************/

