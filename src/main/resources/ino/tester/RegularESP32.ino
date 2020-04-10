/*
** Sketch for ESP-WROOM-32 controller
*/

#include <HTTPClient.h>
#include <Wire.h>
#include <BH1750.h>
#include <DHT.h>

const char* WIFI_SSID = "RT-WiFi_5B84";
const char* WIFI_PASSWORD = "13213455";
IPAddress address(${device_address}); //unique static ip address
IPAddress gateway(${device_gateway});   //gateway and dns servers
IPAddress subnetMask(${device_subnetMask}); //subnet mask

WiFiServer server(${http_port});
String deviceName = "${device_name}";

char inputLine[80]; // input buffer
int charCount = 0; // count of input buffer
String requestValue;

const int valveRelayPin = 16; // GPIO pin of valve relay
const int dhtPin = 17; // GPIO pin of dht11 sensor
const int soilMoisturePin = 32; // GPIO pin of capacitive soil moisture sensor v1.2

BH1750 lightMeter(0x23); //(0x5C) - if addr pin to 3.3V; SCL - GPIO22, SDA - GPIO21
DHT dht(dhtPin, DHT11);

const int pinCount = 39; // pin count
boolean pinStatus[pinCount]; // pin status

/************************************************************************************************/
void setup() {   
    Serial.begin(9600); // иницилизируем монитор порта
    delay(2000); // запас времени на открытие монитора порта — 2 секунды
    if (!WiFi.config(address, gateway, subnetMask, gateway, gateway)){
        Serial.println("Address configure failed");
    }
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);// подключаемся к Wi-Fi сети
    while (WiFi.status() != WL_CONNECTED) {
        delay(1000);
        Serial.println("Connecting to Wi-Fi..");
    }
    Serial.println("Connected to the Wi-Fi network!");
    server.begin();
    for (int i = 0; i < pinCount; i++)
        pinStatus[i] = false;
    pinMode(valveRelayPin, OUTPUT);
    Wire.begin();
    lightMeter.begin();
    dht.begin();
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
        if (c == '\n' && currentLineIsBlank) { // send request
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
            if (isPostingValue("")){
                setDeviceName(requestValue);
            }else if (isPostingValue("valveRelay")){
                setRelay(valveRelayPin, requestValue);
            }else if (isGettingValue("valveRelay")){
                requestValue = getRelay(valveRelayPin);
            }else if (isGettingValue("lightLevel")){
                requestValue = getLightLevel();
            }else if (isGettingValue("airTemperature")){
                requestValue = getAirTemperature();
            }else if (isGettingValue("airHumidity")){
                requestValue = getAirHumidity();
            }else if (isGettingValue("soilMoisture")){
                requestValue = getSoilMoisture();
            }
            currentLineIsBlank = true; // new line begin
            memset(inputLine, 0, sizeof(inputLine));
            charCount = 0;
        } else if (c != '\r') {// в строке попался новый символ
            currentLineIsBlank = false;
        }
    }
    delay(1);  // The time for web-browser to get data
    client.stop(); //  Connect closing
    Serial.println("client disconnected");
}
/************************************************************************************************/
void setDeviceName(String value){
    deviceName = value;
    Serial.println("Set device name is " + value);
}
/************************************************************************************************/
void setRelay(int pin, String value){
    pinStatus[pin] = value.equals("on");
    if (pinStatus[pin]) {
        digitalWrite(pin, HIGH);
    } else {
        digitalWrite(pin, LOW);
    }
}
/************************************************************************************************/
bool getRelay(int pin){
    if (pinStatus[pin]) {
        return "on";
    } else {
        return "off";
    }
}
/************************************************************************************************/
float getLightLevel(){
  return lightMeter.readLightLevel();
}
/************************************************************************************************/
float getAirTemperature(){
  return dht.readTemperature();
}
/************************************************************************************************/
float getAirHumidity(){
  return dht.readHumidity();
}
/************************************************************************************************/
float getSoilMoisture(){
  //float vmin = 0.1; // min of sensor;
  //float vmax = 3.2; // max of sensor;
  //float volt = vmin + (vmax - vmin) * analogRead(soilMoisturePin) / 4095;
  //if (volt < vmin) volt = vmin;
  //if (volt > vmax) volt = vmax;
  //return (vmax - volt) * 100 / (vmax - vmin); // 46% - air, 84% - water
  float xmin = 640; // value in water
  float xmax = 2600; // value in air
  return (xmax - analogRead(soilMoisturePin)) * 100 / (xmax - xmin); // 0% - air, 100% - water
}
/************************************************************************************************/

