#include <HTTPClient.h>

const char* WIFI_SSID     = "RT-WiFi_5B84";
const char* WIFI_PASSWORD = "23834659";
//const char* REMOTE_SERVER = "10.10.10.10"; //Raspberry Pi
const char* REMOTE_SERVER = "10.10.10.102"; //Developer station
const char* DEVICE_NAME     = "esp32Device";

WiFiServer server(80);

char inputLine[80]; // заводим буфер
int charCount = 0;// и счетчик для буфера
bool connecting = false;
String requestValue;
/************************************************************************************************/
void setup() {
    Serial.begin(9600);// иницилизируем монитор порта
    delay(5000);// запас времени на открытие монитора порта — 5 секунд
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);// подключаемся к Wi-Fi сети
    while (WiFi.status() != WL_CONNECTED) {
        delay(1000);
        Serial.println("Connecting to Wi-Fi..");
    }
    Serial.println("Connected to the Wi-Fi network");
    server.begin();
}
/************************************************************************************************/
void loop() {
    if (!connecting) connecting = serverConnecting();
    if (connecting) checkRequest();
}
/************************************************************************************************/
bool serverConnecting(){
    String uri = "https://" + String(REMOTE_SERVER) + "/device?name=" + String(DEVICE_NAME);
    HTTPClient http;
    http.begin(uri);
    Serial.println("Send " + uri);
    delay(1000);
    return (http.GET() == 200);
}
/************************************************************************************************/
bool isGettingValue(char* variable){
    String sampleStr = "GET /" + String(DEVICE_NAME) + "_" + variable;
    int sampleLen = sampleStr.length();
    char sampleChr[sampleLen];
    sampleStr.toCharArray(sampleChr, sampleLen);
    return (strstr(inputLine, sampleChr) > 0);
}
/************************************************************************************************/
bool isPostingValue(char* variable){
    String sampleStr = "POST /" + String(DEVICE_NAME) + "_" + variable + "=";
    int sampleLen = sampleStr.length();
    char sampleChr[sampleLen];
    sampleStr.toCharArray(sampleChr, sampleLen);
    if (!(strstr(inputLine, sampleChr) > 0)) return false;
    int lastSpace = String(inputLine).lastIndexOf(' ');
    requestValue = String(inputLine).substring(sampleLen, lastSpace);
    return true;
}
/************************************************************************************************/
void checkRequest(){
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
            client.println("HTTP/1.1 200 OK");
            client.println("Content-Type: text/html");
            client.println("Connection: close");
            client.println();
            client.println(requestValue);
            break;
        }
        if (c == '\n') {
            currentLineIsBlank = true;
            char* value;
            if (isGettingValue("intensity")){
                requestValue = getIntensity();
            }else if (isPostingValue("led")){
                setLed(requestValue);
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
float getIntensity(){
  return 13.55;
}
/************************************************************************************************/
void setLed(String value){
    Serial.println(value);
}