#include <WiFi.h>
//#include <HTTPClient.h>
#include "Connectivity.h"

const char* WIFI_SSID = "BT-2JCPFQ";
const char* WIFI_PASSWORD = "LondonDublin";

Connectivity::Connectivity(){
  //empty constructor
}

void Connectivity::setup_wifi(){
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println(" Connected!");
}