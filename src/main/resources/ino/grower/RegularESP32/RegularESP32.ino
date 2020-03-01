#include <HTTPClient.h>
#include <Wire.h>
#include <BH1750.h>

const char* WIFI_SSID = "RT-WiFi_5B84";
const char* WIFI_PASSWORD = "23834659";
IPAddress address(10,10,10,200); //unique ip address
IPAddress gateway(10,10,10,1);

const int HTTP_PORT = 80;
WiFiServer server(HTTP_PORT);
String deviceName = "";

char inputLine[80]; 
int charCount = 0; // и счетчик для буфера
String requestValue;

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
float getLightLevel(){
  return lightMeter.readLightLevel();
}
/************************************************************************************************/
void setDeviceName(String value){
    deviceName = value;
    Serial.println("Set device name is " + value);
}
/************************************************************************************************/


