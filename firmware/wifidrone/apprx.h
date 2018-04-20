/*
 Based on Project ESP8266 Wifi Quadrotor Quad230_v1_0_esp8266_androidWifi  tinnakon kheowree
 
Hardware support: 
• ESP8266, Nodemcu ESP-12E
• GY-521, MPU6050 6 axis gyro/accel with Motion Processing Unit
• WiFidrone, 100mm frame
• i2c pin GPIO4=SDA GPIO5=SCL
*/
#define DEFAULT_SSID_LENGTH 16
#define ARM_Address 55
#define LED 2
#define Print_Debug

int CH_THR = 1000;
int CH_AIL = 1500;
int CH_ELE = 1500;
int CH_RUD = 1500;
float CH_AILf = 1500;
float CH_ELEf = 1500;
float CH_RUDf = 1500;
int CH_AIL_Cal = 1500;
int CH_ELE_Cal = 1500;
int CH_RUD_Cal = 1500;
int AUX_1 = 1000;
int AUX_2 = 1500;
int AUX_3 = 1500;
int AUX_4 = 1500;

int roll_mid = 1500;//1480
int pitch_mid = 1500;//1500
int yaw_mid = 1500;

int ch1;
int ch2;
int ch3;
int ch4;
int ch_aux1;
int ch_aux2;
int ch_aux3;

typedef struct
{
  int8_t startByte;
  int8_t roll;
  int8_t pitch;
  int8_t throttle;
  int8_t yaw;
  int8_t checksum;
  char ssid[DEFAULT_SSID_LENGTH];
} ControlData;

typedef struct
{
  int8_t startByte;
  int8_t startByte2;
  int8_t yawPitchRoll;
  int16_t kp;
  int16_t ki;
  int16_t kd;
  int16_t checksum;
  char ssid[DEFAULT_SSID_LENGTH];
} TuningData;

WiFiUDP udp;
byte data[512] = {0};
unsigned int localPort = 12345;
TuningData tuningData[3] = {0};

#ifdef Print_Debug
String yawPitchRollText[3] = {"Yaw....:", "Pitch..:", "Roll...:"};
String output = "";
String lastOutput = "";
#endif

int8_t Trim_value[3] = {0};  // trim //  yaw : pitch : roll
char accessPointName[DEFAULT_SSID_LENGTH] = {'\0'};
Ticker ticker_DEGUG;

String readEEPROM(int index, int length);
int writeEEPROM(int index, String text);
// void loadTuningData(void);
// void saveTuningData(int i);
void loadTrimData(void);
void saveTrimData(int8_t *tmp);
String ipToString(IPAddress ip);
String floatToString(float value, int length, int decimalPalces);
String intToString(int value, int length);
String hexToString(byte value);
void blink(void);
uint8_t setPIDgain(void);
void sentControlcommand(int8_t roll_tmp, int8_t pitch_tmp, int8_t throttle_tmp, int8_t yaw_tmp);
void Read_udp(void);

IPAddress local_ip(192, 168, 4, 1);
IPAddress gateway(192, 168, 4, 1);
IPAddress subnet(255, 255, 255, 255);

void computeRC(void)
{
  int numberOfBytes = udp.parsePacket();

  if (numberOfBytes > 0)
  {
    udp.read(data, numberOfBytes);
    
    if (data[0] == 0XFE) // trim and control
    {
      static ControlData controlData_prev;
      ControlData controlData = {0};
      memcpy(&controlData, data, sizeof(ControlData));
      memcpy(&controlData_prev, data, sizeof(ControlData));

      int8_t checksum = controlData.roll + controlData.pitch + controlData.throttle + controlData.yaw;
      controlData.ssid[DEFAULT_SSID_LENGTH - 1] = '\0';

      if (controlData.checksum == checksum)// && strcmp(controlData.ssid, accessPointName) == 0)
      {
        int8_t trimFlag = 0XFF;

        if (controlData.roll == trimFlag && controlData.pitch == trimFlag && controlData.throttle == trimFlag && controlData.yaw == trimFlag)
        {
          // Trim //
//          int8_t trim_tmp_0[3] = {0};
//          trim_tmp_0[0] = controlData_prev.roll;
//          trim_tmp_0[1] = controlData_prev.pitch;
//          trim_tmp_0[2] = controlData_prev.yaw;
//          saveTrimData(trim_tmp_0);
//          memcpy(&Trim_value, &trim_tmp_0, 3);
//
//
//
//#ifdef Print_Debug
//          Serial.println("Trim");
//#endif
        }
        else
        {
          // control //

          //sentControlcommand(controlData.roll + Trim_value[0] , controlData.pitch + Trim_value[1], controlData.throttle, controlData.yaw + Trim_value[2] );
//          sentControlcommand(controlData.roll, controlData.pitch , controlData.throttle, controlData.yaw);
          memcpy(&controlData_prev, data, sizeof(ControlData));
          blink();
#ifdef Print_Debug
          output = String("Roll ") + intToString(controlData.roll, 4) + ", Pitch " + intToString(controlData.pitch, 4) + ", Throttle " + intToString(controlData.throttle, 3) + ", Yaw " + intToString(controlData.yaw, 4);

          if (output != lastOutput)
          {
            lastOutput = output;
            CH_THR = map(controlData.throttle, 0, 100, 0, 1000);//1000 - 2000
            CH_AIL = map(controlData.roll, -100, 100, 1000, 2000);
            CH_ELE = map(controlData.pitch, -100, 100, 1000, 2000);
            CH_RUD = map(controlData.yaw, -100, 100, 1000, 2000);
            AUX_1 = 2000;
            
            //Serial.println(output);
          }
#endif
        }
      }

    }
  }
}

String ipToString(IPAddress ip)
{
  return String(ip[0]) + "." + ip[1] + "." + ip[2] + "." + ip[3];
}

String floatToString(float value, int length, int decimalPalces)
{
  String stringValue = String(value, decimalPalces);
  String prefix = "";

  for (int i = 0; i < length - stringValue.length(); i++)
  {
    prefix += " ";
  }

  return prefix + stringValue;
}

String intToString(int value, int length)
{
  String stringValue = String(value);
  String prefix = "";

  for (int i = 0; i < length - stringValue.length(); i++)
  {
    prefix += " ";
  }

  return prefix + stringValue;
}

String hexToString(byte value)
{
  int length = 2;
  String stringValue = String(value, HEX);
  String prefix = "";

  for (int i = 0; i < length - stringValue.length(); i++)
  {
    prefix += "0";
  }

  return "0x" + prefix + stringValue;
}

void blink(void)
{
  static int8_t led = 0;
  led = 1 - led;
  digitalWrite(LED, led);
}

