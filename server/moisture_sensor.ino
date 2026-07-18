#include <WiFi.h>
#include <HTTPClient.h>
#include <esp_mac.h>

const char* ssid = "geffen";
const char* password = "0502205544";

// Conversion factor for microseconds to seconds
#define uS_TO_S_FACTOR 1000000ULL  
// Sleep for 30 minutes (600 seconds)
#define TIME_TO_SLEEP  1800       

int sensorPins[] = {34, 35, 32};
int powerPins[] = {25, 26, 27};
int numSensors = sizeof(sensorPins) / sizeof(sensorPins[0]);
const char* serverUrl = "https://apneustic-casen-redundant.ngrok-free.dev/sensors/moisture";

void setup() {
  Serial.begin(9600);
  delay(1000); // Take a second to open the Serial Monitor

  Serial.println("Wake up! Reading sensors...");

  // 1. Initialize WiFi
  WiFi.begin(ssid, password);
  int retryCount = 0;
  while (WiFi.status() != WL_CONNECTED && retryCount < 20) {
    delay(500);
    Serial.print(".");
    retryCount++;
  }

  // 2. Read and Send Data (Only if WiFi connected)
  if (WiFi.status() == WL_CONNECTED) {
    String deviceId = getChipID();
    
    for (int i = 0; i < numSensors; i++) {

      int powerPin = powerPins[i];
      // Define the power pin as output
      pinMode(powerPin, OUTPUT);
      // 1. Turn sensors ON
      digitalWrite(powerPin, HIGH);
      delay(1000); // Give the sensors a moment to stabilize power


      int pin = sensorPins[i];
      analogRead(pin); // "Dummy" read to prime the ADC
      delay(10);
      int moisture = analogRead(pin);

      HTTPClient http;
      http.begin(serverUrl);
      http.addHeader("Content-Type", "application/json");

      String body = "{";
      body += "\"device_mac_address\":\"" + deviceId + "\",";
      body += "\"sensor_id\":\"" + String(pin) + "\",";
      body += "\"moisture\":" + String(moisture);
      body += "}";
      int httpResponseCode = http.POST(body);
      
      Serial.printf("Sensor %d: %d (Res: %d)\n", pin, moisture, httpResponseCode);
      http.end();

      digitalWrite(powerPin, LOW);
    }
  } else {
    Serial.println("WiFi failed, going back to sleep to save power.");
  }


  // 3. Configure Sleep Timer
  esp_sleep_enable_timer_wakeup(TIME_TO_SLEEP * uS_TO_S_FACTOR);
  
  Serial.println("Going to sleep now...");
  Serial.flush(); 
  
  // 4. Enter Deep Sleep
  esp_deep_sleep_start();
}

void loop() {
  // This is never reached
}

String getChipID() {
  uint8_t mac[6];
  // This function is the "official" way to get the MAC in the correct order
  esp_efuse_mac_get_default(mac); 
  
  char chipIdStr[13];
  // %02X ensures we always get 2 characters (e.g., '0A' instead of just 'A')
  snprintf(chipIdStr, 13, "%02X%02X%02X%02X%02X%02X", 
           mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
           
  return String(chipIdStr);
}