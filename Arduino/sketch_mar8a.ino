#include <WiFi.h>
#include <HTTPClient.h>
#include "Connectivity.h"
#include <ArduinoJson.h>
#include <FastLED.h>
#include <WiFiClientSecure.h>

const char* API_BASE = "https://qocfzpp98l.execute-api.eu-west-2.amazonaws.com/prod";
const char* aws_root_ca = \
"-----BEGIN CERTIFICATE-----\n" \
"MIIDQTCCAimgAwIBAgITBmyfz5m/jAo54vB4ikPmljZbyjANBgkqhkiG9w0BAQsF\n" \
"ADA5MQswCQYDVQQGEwJVUzEPMA0GA1UEChMGQW1hem9uMRkwFwYDVQQDExBBbWF6\n" \
"b24gUm9vdCBDQSAxMB4XDTE1MDUyNjAwMDAwMFoXDTM4MDExNzAwMDAwMFowOTEL\n" \
"MAkGA1UEBhMCVVMxDzANBgNVBAoTBkFtYXpvbjEZMBcGA1UEAxMQQW1hem9uIFJv\n" \
"b3QgQ0EgMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALJ4gHHKeNXj\n" \
"ca9HgFB0fW7Y14h29Jlo91ghYPl0hAEvrAIthtOgQ3pOsqTQNroBvo3bSMgHFzZM\n" \
"9O6II8c+6zf1tRn4SWiw3te5djgdYZ6k/oI2peVKVuRF4fn9tBb6dNqcmzU5L/qw\n" \
"IFAGbHrQgLKm+a/sRxmPUDgH3KKHOVj4utWp+UhnMJbulHheb4mjUcAwhmahRWa6\n" \
"VOujw5H5SNz/0egwLX0tdHA114gk957EWW67c4cX8jJGKLhD+rcdqsq08p8kDi1L\n" \
"93FcXmn/6pUCyziKrlA4b9v7LWIbxcceVOF34GfID5yHI9Y/QCB/IIDEgEw+OyQm\n" \
"jgSubJrIqg0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMC\n" \
"AYYwHQYDVR0OBBYEFIQYzIU07LwMlJQuCFmcx7IQTgoIMA0GCSqGSIb3DQEBCwUA\n" \
"A4IBAQCY8jdaQZChGsV2USggNiMOruYou6r4lK5IpDB/G/wkjUu0yKGX9rbxenDI\n" \
"U5PMCCjjmCXPI6T53iHTfIUJrU6adTrCC2qJeHZERxhlbI1Bjjt/msv0tadQ1wUs\n" \
"N+gDS63pYaACbvXy8MWy7Vu33PqUXHeeE6V/Uq2V8viTO96LXFvKWlJbYK8U90vv\n" \
"o/ufQJVtMVT8QtPHRh8jrdkPSHCa2XV4cdFyQzR1bldZwgJcJmApzyMZFo6IQ6XU\n" \
"5MsI+yMRQ+hDKXJioaldXgjUkK642M4UwtBV8ob2xJNDd2ZhwLnoQdeXeGADbkpy\n" \
"rqXRfboQnoZsG4q5WTP468SQvvG5\n" \
"-----END CERTIFICATE-----\n";

#define DATA_PIN 27
#define NUM_LEDS 72
#define BREAK_BEAM_1 13
#define BREAK_BEAM_2 14
CRGB leds[NUM_LEDS];
int hue;
int saturation;
int value;
bool isOn;
int room_id;
int people_count;
bool motion_sensing_enabled;
int loop_count;
int last_beam = 0;
unsigned long lastBeamTime = 0;


void setup() {
  room_id = 1;
  loop_count = 0;

  Serial.begin(115200);
  delay(1000);//does not like when its not there before WiFi.begin
  Connectivity::setup_wifi();

  //WiFiClientSecure client;
  //WiFiClient client;

  pinMode(BREAK_BEAM_1, INPUT_PULLUP);
  pinMode(BREAK_BEAM_2, INPUT_PULLUP);

  FastLED.addLeds<WS2812B, DATA_PIN, GRB>(leds, NUM_LEDS);
  FastLED.setBrightness(255);

  fill_solid(leds, NUM_LEDS, CRGB::Black);
  FastLED.show();
}

void loop(){
  //delay(1000);
  loop_count++;
  int beam1 = digitalRead(13);
  int beam2 = digitalRead(14);
  
  Serial.print("Beam 1: ");
  Serial.print(beam1);
  Serial.print(" | Beam 2: ");
  Serial.println(beam2);

  if ((digitalRead(BREAK_BEAM_1)==LOW) || (digitalRead(BREAK_BEAM_2)==LOW)){
    //check which one is low, save that info for the next loop. If small amount of time has passed between executions, add or remove 1 from occupancy and send to database.
    unsigned long now = millis();
    int current_beam = (digitalRead(BREAK_BEAM_1) == LOW) ? 1 : 2;
    
    if(last_beam != 0 && last_beam != current_beam && (now - lastBeamTime < 2000)){
      if(last_beam == 1 && current_beam ==2){
        //person entered
        people_count++;
        Serial.println("Person detected entering");
      }else if(last_beam == 2 && current_beam ==1){
        //person exited
        if(people_count==0){
          people_count++;
        }
        people_count--;
        Serial.println("Person detected leving");
      }
        postOccupancy(people_count);
      }
      last_beam = current_beam;
      lastBeamTime = now;
    }

  if(loop_count >= 20){
    JsonDocument state_doc = GET_Endpoint(room_id, "state");
    JsonDocument occupancy_doc = GET_Endpoint(room_id, "occupancy");
    JsonDocument motion_doc = GET_Endpoint(room_id, "motion");

    if (hue != state_doc["hue"]) {hue = state_doc["hue"];}
    if(saturation != state_doc["saturation"]) {saturation = state_doc["saturation"];}
    if(value != state_doc["value"]) {value = state_doc["value"];}
    if(isOn != state_doc["is_on"]) {isOn = state_doc["is_on"];}

    if(motion_sensing_enabled != motion_doc["motion_sensing_enabled"]) {motion_sensing_enabled = motion_doc["motion_sensing_enabled"];}

    if(people_count != occupancy_doc["people_count"]) {people_count = occupancy_doc["people_count"];}


    Serial.printf("H:%d S:%d V:%d On:%d\n", hue, saturation, value, isOn);

    if(isOn &&(!motion_sensing_enabled || people_count > 0)){
      //FastLED expects hue to be 0-255 instead of 0-360 so it needs some conversion
      Serial.printf("motion sensing enabled:%d  people count:%d\n", motion_sensing_enabled, people_count);
      int hue_255 = (int)((hue/360.0) * 255);
      CHSV hsvColor(hue_255, saturation, value);
      CRGB rgbColor;
      hsv2rgb_rainbow(hsvColor, rgbColor);
      Serial.printf("RGB: R=%d G=%d B=%d\n", rgbColor.r, rgbColor.g, rgbColor.b);
      fill_solid(leds, NUM_LEDS, rgbColor);
      FastLED.show();
      delay(50);
    }else{
      fill_solid(leds, NUM_LEDS, CRGB::Black);
      FastLED.show();
    }
  
  loop_count = 0;
  }
  delay(50);
}
void postOccupancy(int number_of_people){
  WiFiClientSecure client;
  client.setCACert(aws_root_ca);
  HTTPClient http;
  http.begin(client, String(API_BASE) + "/rooms/" + room_id + "/occupancy");
  http.addHeader("Content-Type", "application/json");
  http.POST("{\"people_count\":" + String(number_of_people) + "}");
  http.end();
}
JsonDocument GET_Endpoint(int roomID, String endpoint){
  WiFiClientSecure client;
  client.setCACert(aws_root_ca);
  HTTPClient http;
  String url = String(API_BASE) + "/rooms/" + String(roomID) + "/" + endpoint;
  http.begin(client, url);
  int httpcode = http.GET();
  JsonDocument doc;
  if(httpcode == 200){
    String payload = http.getString();
    DeserializationError error = deserializeJson(doc, payload);
    if(error){
      Serial.println("Json Could not be parsed");
      Serial.println(error.f_str());
    }else{
    //Serial.printf("HTTP %d\n", httpcode);
  }
  }
  http.end();
  return doc;
}


