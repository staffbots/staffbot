/*
** Sketch for ESP-WROOM-32 controller
*/
#include <HTTPClient.h> // library for work with WiFi and HTTP
#include <EEPROM.h>
#include <DFRobot_PH.h> // library for work with analog pH sensor
#include <DFRobot_EC.h> // library for work with analog EC sensor (K = 1.0)
#include <OneWire.h>  // library for work with 1-Wire protocol
#include <DallasTemperature.h>  // library for work with DS18B20 sensor

const char* WIFI_SSID = "RT-WiFi_5B84";
const char* WIFI_PASSWORD = "23834659";
IPAddress address(${device_address}); //unique static ip address
IPAddress gateway(${device_gateway}); //gateway and dns servers
IPAddress subnetMask(${device_subnetMask}); //subnet mask

WiFiServer server(${http_port});
String deviceName = "${device_name}";

char inputLine[80]; // input buffer
int charCount = 0; // count of input buffer
String requestValue;

const int phSensorPin = 25; // GPIO pin of analog pH sensor
const int ecSensorPin = 26; // GPIO pin of analog pH sensor
const int aquaTemperaturePin = 32; // GPIO pin of DS18B20 sensor
const int ecPumpRelayPin = 21; // GPIO pin of EC pump relay
const int phUpPumpRelayPin = 22; // GPIO pin of Ph+ pump relay
const int phDownPumpRelayPin = 23; // GPIO pin of Ph- pump relay

DFRobot_PH ph;
DFRobot_EC ec;
OneWire oneWire(aquaTemperaturePin); // object of OneWire library
DallasTemperature temperatureSensor(&oneWire); // object of DallasTemperature library

const int pinCount = 39; // pin count
boolean pinStatus[pinCount]; // statuses of pins

/************************************************************************************************/
void setup() {   
    Serial.begin(9600);// иницилизируем монитор порта
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
    ph.begin();
    ec.begin();
    temperatureSensor.begin();// начинаем работу с датчиком
    temperatureSensor.setResolution(12);// устанавливаем разрешение датчика от 9 до 12 бит
    for (int i = 0; i < pinCount; i++)
        pinStatus[i] = false;
    pinMode(ecPumpRelayPin, OUTPUT);
    pinMode(phUpPumpRelayPin, OUTPUT);
    pinMode(phDownPumpRelayPin, OUTPUT);
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
            }else if (isPostingValue("ecPumpRelay")){
                setRelay(ecPumpRelayPin, requestValue);
            }else if (isPostingValue("phUpPumpRelay")){
                setRelay(phUpPumpRelayPin, requestValue);
            }else if (isPostingValue("phDownPumpRelay")){
                setRelay(phDownPumpRelayPin, requestValue);
            }else if (isGettingValue("ecPumpRelay")){
                requestValue = getRelay(ecPumpRelayPin);
            }else if (isGettingValue("phUpPumpRelay")){
                requestValue = getRelay(phUpPumpRelayPin);
            }else if (isGettingValue("phDownPumpRelay")){
                requestValue = getRelay(phDownPumpRelayPin);
            }else if (isGettingValue("aquaTemperature")){
                requestValue = getAquaTemperature();
            }else if (isGettingValue("phProbe")){
                requestValue = getPhProbe();
            }else if (isGettingValue("ecProbe")){
                requestValue = getEcProbe();
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
    digitalWrite(pin, pinStatus[pin] ? HIGH : LOW);
}
/************************************************************************************************/
bool getRelay(int pin){
    return pinStatus[pin] ? "on" : "off";
}
/************************************************************************************************/
float getAquaTemperature(){
    temperatureSensor.requestTemperatures(); // send request to measuring
    return temperatureSensor.getTempCByIndex(0); // read data from sensor register
}
/************************************************************************************************/
float getVoltage(int pin){
    float vmin = 0.0; // min of sensor;
    float vmax = 3.3; // max of sensor;
    float voltage = vmin + (vmax - vmin) * analogRead(pin) / 4095;
    if (voltage < vmin) voltage = vmin;
    if (voltage > vmax) voltage = vmax;
    return voltage;
}
/************************************************************************************************/
float getPhProbe(){
    float voltage = getVoltage(phSensorPin) * 1000;  // read the voltage
    Serial.print("voltage:");
    Serial.println(voltage / 1000);
    float temperature = getAquaTemperature();  // read your temperature sensor to execute temperature compensation
    float phValue = ph.readPH(voltage,temperature);  // convert voltage to pH with temperature compensation
    Serial.print("temperature:");
    Serial.print(temperature,1);
    Serial.print("^ C  pH:");
    Serial.println(phValue,2);
//    ph.calibration(voltage,temperature);  // calibration process by Serail CMD
    return phValue;
}
/************************************************************************************************/
float getEcProbe(){
    float voltage = analogRead(ecSensorPin)/1024.0*5000;  // read the voltage
    float temperature = getAquaTemperature();  // read your temperature sensor to execute temperature compensation
    float ecValue =  ec.readEC(voltage,temperature);  // convert voltage to EC with temperature compensation
    Serial.print("temperature:");
    Serial.print(temperature,1);
    Serial.print("^C  EC:");
    Serial.print(ecValue,2);
    Serial.println("ms/cm");
    return ecValue;
//    ec.calibration(voltage,temperature);  // calibration process by Serail CMD
}
/************************************************************************************************/
