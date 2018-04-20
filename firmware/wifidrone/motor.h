/*
 Based on Project ESP8266 Wifi Quadrotor Quad230_v1_0_esp8266_androidWifi  tinnakon kheowree
 
Hardware support: 
• ESP8266, Nodemcu ESP-12E
• GY-521, MPU6050 6 axis gyro/accel with Motion Processing Unit
• WiFidrone, 100mm frame
• i2c pin GPIO4=SDA GPIO5=SCL

-------- Frame type Quad-X -------------      

           16>>         <<14 
              \         / 
                \ --- /
                 |   |
                / --- \
              /         \ 
           12>>         <<13 

--------- DC motor assignment ---------
FontLeft  => GPIO16
FontRight => GPIO14
BackLeft  => GPIO12
BackRight => GPIO13
*/

//motor
int MOTOR_FRONTL_PIN = 16;//>>
int MOTOR_FRONTR_PIN = 14;//<<
int MOTOR_REARL_PIN = 12;//>>
int MOTOR_REARR_PIN = 13;//<<

int motorCommand_FRONTL;
int motorCommand_FRONTR;
int motorCommand_REARL;
int motorCommand_REARR;

int motor_FRONTL;
int motor_FRONTR;
int motor_REARL;
int motor_REARR;

#define MINCOMMAND 1000
#define MIDCOMMAND 1500
#define MAXCOMMAND 2000

void configureMotors() {
  
  pinMode(MOTOR_FRONTL_PIN,OUTPUT);  
  pinMode(MOTOR_FRONTR_PIN,OUTPUT); 
  pinMode(MOTOR_REARL_PIN,OUTPUT); 
  pinMode(MOTOR_REARR_PIN,OUTPUT);
  analogWriteFreq(500);//pwm_freq = 1000; 2000 , 4000 , Hz
}

void commandAllMotors(int motorCommand) {

  analogWrite(MOTOR_FRONTL_PIN, (motorCommand-1000)*1.136);
  analogWrite(MOTOR_FRONTR_PIN, (motorCommand-1000)*1.136);
  analogWrite(MOTOR_REARL_PIN, (motorCommand-1000)*1.136);
  analogWrite(MOTOR_REARR_PIN, (motorCommand-1000)*1.136);

}

void commandMotors() {
 analogWrite(MOTOR_FRONTL_PIN, motor_FRONTL);//PWM 0 - 1023 ,,10 bit
 analogWrite(MOTOR_FRONTR_PIN, motor_FRONTR);
 analogWrite(MOTOR_REARL_PIN, motor_REARL);
 analogWrite(MOTOR_REARR_PIN, motor_REARR);
 }
