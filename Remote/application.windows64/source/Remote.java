import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 
import hypermedia.net.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Remote extends PApplet {

//import ui library for buttons

ControlP5 cp5;

//import udp library

UDP udpTX; ///Create UDP object for recieving and transmitting

//variables for udp
String ip_addr = "192.168.4.1";
int udp_port_TX = 12345;
int udp_port_RX = 54321;

//variables for control
float th =0;
int throttle =0;
int Ticks2Value = 0;
int Ticks2ValueX = 0;
float Ticks3Value = 0;
int Ticks3ValueX = 0;
float Ticks4Value = 0;
int Ticks4ValueX = 0;
int Ticks1Value = 0;
int Ticks1ValueX = 0;
float q = 0;
int qx = 0;

boolean armed=false;

byte bXAxis, bYAxis, bthrottle, bqx, bchecksum;
int motorPower;

String command;
byte[] message  = new byte[6]; 

int XAxis;
int YAxis;

int XAxisBase;
int YAxisBase;

int qxBase;
int sxBase;

int Ticks2ValueBase;
int Ticks1ValueBase;
 
int throttleValue  = 0;

Toggle tgl1, tgl2, tgl3, tgl4, tgl12;
Slider altitute, rudder,aileronTrim, elevatorTrim; 
Knob knobTurretRotation;

int xDegree = 500;
int yDegree = 10;

//variable to show 3D model
PShape quad;
float angle = 0.01f;
boolean showsensorsData=false;

public void setup() {
    
   //set window dimensions
   

   //disables drawing outlines
   noStroke();
   
  //create new cp5 object for this user
  cp5=new ControlP5(this);

  //Create Arm On/Off toggle switch
   tgl2 = cp5.addToggle("tglArm")
   .setCaptionLabel("DISARMED")
     .setPosition(-200+xDegree, 600+yDegree)
       .setSize(40, 20)
         .setValue(false)
          .setMode(ControlP5.SWITCH)
           .setId(101)
           ; 
   //Throttle control knob 
    knobTurretRotation = cp5.addKnob("Motor Power")
    .setCaptionLabel("Motor Power")
    .setRange(0, 100)
      .setValue(0)
        .setPosition(-80+xDegree, 490+yDegree)
          .setRadius(60)
            .setNumberOfTickMarks(50)
              .setTickMarkLength(7)
                .snapToTickMarks(true)
                // .setColorForeground(color(255))
                // .setColorBackground(color(0, 160, 100))
                // .setColorActive(color(255,255,0))
                .setDragDirection(Knob.HORIZONTAL)
                .setId(102)
                ;
    //Create Throttle trim slider
      cp5.addSlider("sliderTicks4")
     .setCaptionLabel("Throttle trim")
     .setSize(200,22)
     .setPosition(-110+xDegree,680+yDegree)
     .setRange(-30,30) // values can range from big to small as well
     .setValue(0)
     //.setNumberOfTickMarks(61)
     .setSliderMode(Slider.FLEXIBLE)
     .setId(103)
     ;

    //Create Rudder trim slider     
      cp5.addSlider("sliderTicks3")
     .setCaptionLabel("Rudder trim")
     .setSize(20,200)
     .setPosition(100+xDegree,475+yDegree)
     .setRange(-30,30) // values can range from big to small as well
     //.setNumberOfTickMarks(61)
     .setSliderMode(Slider.FLEXIBLE)
     .setValue(0)
     .setId(104)
     ;
     
    //Create Aileron trim slider       
    aileronTrim = cp5.addSlider("sliderTicks1")
     .setCaptionLabel("Aileron trim")
     .setSize(20,200)
     .setPosition(-470+xDegree,475+yDegree)
     .setRange(-50,50) // values can range from big to small as well
     //.setNumberOfTickMarks(101)
     .setSliderMode(Slider.FLEXIBLE)
     .setValue(0)
     .setId(105)
     ;
     
    //Create Elevator trim slider      
     elevatorTrim= cp5.addSlider("sliderTicks2")
     .setCaptionLabel("Elevator trim")
     .setSize(200,22)
     .setPosition(-413+xDegree,680+yDegree)
     .setRange(-50,50) // values can range from big to small as well
     //.setNumberOfTickMarks(101)
     .setSliderMode(Slider.FLEXIBLE)
     .setValue(0)
     .setId(106)
     ;
     
    //Create Rudder slider       
    rudder= cp5.addSlider("slider")
     .setCaptionLabel("Rudder")
     .setPosition(-110+xDegree,640+yDegree)
     .setSize(200,20)
     .setRange(-100,100)
     .setValue(0)
     .setSliderMode(Slider.FLEXIBLE)
     .setId(107)
     ;
     
    //Create Joystick
  setupJoystick(220+xDegree, 575+yDegree);
  
         
  //create new object for transmitting
  udpTX =new UDP(this, udp_port_TX);
  udpTX.log(true);
  udpTX.setBuffer(22);
  udpTX.loopback(false);

  quad = loadShape("wifidrone.obj");  //Load wifidrone 3D model ( waveform .OBJ format) 

  //stop looping setup  
 noLoop();
}//end of setup()


public void draw() {
  
    //set background color, also clears the screen
    background(50, 50, 50);
    shapeMode(CORNER);
    pushMatrix();
    
     translate(width/2, height/3);
  if(!showsensorsData) {
      rotateX(angle);
      rotateY(angle);
      rotateZ(angle);
  } else {
      rotateX(radians(x_gyr));
      rotateY(radians(y_gyr));
  }
      angle += 0.01f;
      shape(quad,width/100, height/100, 300,300 );   //show quad 3D model
   
    popMatrix();
  
   showSensorsData(); 
   joystickDraw();
   appTX();
   
  loop();
 }


public void controlEvent(ControlEvent theEvent) {
    
     if((theEvent.getController().getId()== 101 ) && (!armed)){
     message[0] = (byte) 0xFE;
     message[1] = (byte) 0x00;
     message[2] = (byte) 0x00;
     message[3] = (byte) 0x00;
     message[4] = (byte) 0x3d;
     message[5] = (byte) 0x3d;
     udpTX.send(message,ip_addr,udp_port_TX);
     armed = !armed;
     tgl2.setCaptionLabel("ARMED");
     tgl2.setColorValue(color(255, 0, 0));
     tgl2.setColorBackground(color(255, 0, 0));
     } else if((theEvent.getController().getId()== 101 ) && (armed)){
     message[0] = (byte) 0xFE;
     message[1] = (byte) 0x00;
     message[2] = (byte) 0x00;
     message[3] = (byte) 0x00;
     message[4] = (byte) 0xa0;
     message[5] = (byte) 0xa0;
     udpTX.send(message,ip_addr,udp_port_TX);
     armed = !armed;
     tgl2.setCaptionLabel("DISARMED");
     tgl2.setColorValue(color(0, 0, 255));
     tgl2.setColorBackground(color(0, 0, 255));
     }
          
}//end of event
boolean isMouseTracking=false;

float x = 0;
float y = 0;

int joyOutputRange = 10;  //Maximum value for full horiz or vert position where centered is 0.

float curJoyDisplayWidth;
float curJoyDisplayHeight;

float maxJoyRange = 200;     //Maximum joystick range
float curJoyAngle;     //Current joystick angle
float curJoyRange;     //Current joystick range
float joyDisplayCenterX;  //Joystick displayed Center X
float joyDisplayCenterY;  //Joystick displayed Center Y

Textlabel joystickLabel;

public void setupJoystick(int centerx, int centery)
{
  joyDisplayCenterX = centerx-530;
  joyDisplayCenterY = centery;
  curJoyDisplayWidth = maxJoyRange * .85f;
  curJoyDisplayHeight = curJoyDisplayWidth;
  maxJoyRange = curJoyDisplayWidth / 2;

  joystickLabel = cp5.addTextlabel("label")
    .setText("JOYSTICK")
      .setPosition(joyDisplayCenterX - 25, joyDisplayCenterY + maxJoyRange + 5)
       .setId(300)
        ;
}

public void joystickDraw()
{

  float dx = mouseX - joyDisplayCenterX;
  float dy = mouseY - joyDisplayCenterY;

  if ((mousePressed && (mouseButton == LEFT)) && dist(mouseX, mouseY, joyDisplayCenterX, joyDisplayCenterY) <= 100 ) { //JOYSTICK controlled by mouse only if left button of mouse pressed.
    isMouseTracking = true;
  } 
  else {
   isMouseTracking = false;
  }

  if (isMouseTracking)
  {    
    curJoyAngle = atan2(dy, dx);
    curJoyRange = dist(mouseX, mouseY, joyDisplayCenterX, joyDisplayCenterY);
  }
  else
  {
    curJoyRange = 0;
  }

  noStroke();
  fill(2, 52, 77);
  ellipse(joyDisplayCenterX, joyDisplayCenterY, curJoyDisplayHeight, curJoyDisplayWidth);

  joystickButton(joyDisplayCenterX, joyDisplayCenterY, curJoyAngle);
  y = (joyOutputRange*(cos(curJoyAngle) * curJoyRange)/ maxJoyRange);
  x = (joyOutputRange*(-(sin(curJoyAngle) * curJoyRange)/maxJoyRange));
     
}


public void joystickButton(float x, float y, float a)
{
  pushMatrix();
  translate(x, y);
  rotate(a);

  if (curJoyRange > maxJoyRange)
    curJoyRange = maxJoyRange;

  if (isMouseTracking) {
    fill(0, 180, 234);
  } 
  else {
    fill(1, 108, 158);
  }
  ellipse(curJoyRange * 0.5f, 0, maxJoyRange * 1.5f, maxJoyRange * 1.5f);

  popMatrix();
}

// Thtrottle controled by mouse wheel
public void mouseWheel(MouseEvent event) {
  int e = event.getCount();
  if(e == 1 && throttleValue > 0 ){
     knobTurretRotation.setValue(throttleValue = throttleValue - 5);
  }
  if(e == -1 && throttleValue < 100) {
    knobTurretRotation.setValue(throttleValue = throttleValue + 5);
  }  
}
public void setupUI() {
  //create new cp5 object for this user
  cp5=new ControlP5(this);
  
     quad = loadShape("wifidrone.obj");  //Load wifidrone 3D model ( waveform .OBJ format) 

     altitute = cp5.addSlider("Altitute")
     .setCaptionLabel("Altitude")
     .setSize(20,245)
     .setPosition(160+xDegree,475+yDegree)
     .setRange(0,30) // values can range from big to small as well
     .setNumberOfTickMarks(31)
     .setSliderMode(Slider.FLEXIBLE)
     .setValue(4)
     ;
     

         
   //Light On/Off 
   tgl3 = cp5.addToggle("tglLight")
   .setCaptionLabel("LIGHT")
     .setPosition(-200+xDegree, 500+yDegree)
       .setSize(40, 20)
         .setValue(false);
 
          








}

public void appTX(){
  th = cp5.getController("Motor Power").getValue();
  throttle = round(map(th,0,900,0,100 ));
 
  Ticks4Value =  cp5.getController("sliderTicks4").getValue();    // Get Throttle trim
  Ticks3Value = cp5.getController("sliderTicks3").getValue();     //Get Rudder 

  Ticks2Value = round(cp5.getController("sliderTicks1").getValue());
  Ticks1Value = round(cp5.getController("sliderTicks2").getValue());
  
  Ticks1Value = round(map(Ticks1Value,-200,200, -100,100));
  Ticks2Value = round(map(Ticks2Value,-200,200, -100,100));

   q = cp5.getController("slider").getValue();
   qx = round(q) +round(Ticks3Value);

   XAxis = PApplet.parseInt(20*round(x));
   YAxis =  PApplet.parseInt(20*round(y));

   XAxis= round(map(XAxis, -200, 200, -100,100));
   YAxis= round(map(YAxis, -200, 200, -100,100));
   qx= round(map(qx, -200, 200, -100,100));
 
 command = "Y:" + YAxis + ", X:" + XAxis + ", Throttle:" + PApplet.parseInt((9*throttle)+round(Ticks3Value) )+ ", Rudder:" + qx +".";
 
 if( (XAxisBase != XAxis) ||  (YAxisBase !=  YAxis) || (qxBase !=  qx) || (throttle !=  sxBase) ){
     println("To client: " + command);
     message[0] = (byte) 0xFE;
     message[1] = (byte) YAxis;
     message[2] = (byte) XAxis;
     message[3] = (byte) throttle;
     message[4] = (byte) qx;
     message[5] = (byte) (YAxis + XAxis + qx +throttle);
     println("String Sent: "+ message);
     udpTX.send(message,ip_addr,udp_port_TX);
     
    XAxisBase = XAxis;
    YAxisBase = YAxis;
    qxBase = qx;
    sxBase = throttle;
    } 

}

String  inString;
int datain;
float   dt;
float   x_gyr;  //Gyroscope data
float   y_gyr;
float   z_gyr;
float   x_acc;  //Accelerometer data
float   y_acc;
float   z_acc;
float   x_fil;  //Filtered data
float   y_fil;
float   z_fil;
float   w_fil;
 public void showSensorsData(){
   
  int satir= 70;
  textSize(20);
  
  String accStr = "x = " + (int) x_acc;
  String accStr2 ="y = " + (int) y_acc;
  String accStr3 ="z = " + (int) z_acc;
   
  String gyrStr = "x = " + (int) x_gyr;
  String gyrStr2 ="y = " + (int) y_gyr;
  String gyrStr3 ="z = " + (int) z_gyr;
  
  String filStr = "Degree = " + (int) x_fil;
  String filStr2 ="Z Sensor = " + (int) y_fil;
  String filStr3 ="Altitude = " + (float) z_fil/100;
  String filStr4 ="Temperature = " + (float) w_fil/100;
  
    fill(249, 250, 50);
  text("Gyroscope:", (int) 30, satir);
  text(gyrStr, (int) 30, satir+25);
  text(gyrStr2, (int) 30, satir+50);
  text(gyrStr3, (int) 30, satir+75);

  fill(56, 140, 206);
  text("Accelerometr:", (int) 30, satir+125);
  text(accStr, (int) 30, satir+150); 
  text(accStr2, (int) 30, satir+175);
  text(accStr3, (int) 30, satir+200);
    
  fill(83, 175, 93);
  text("Sensors:", (int) 30, satir+250);
  text(filStr, (int) 30, satir+275);
  text(filStr2, (int) 30, satir+300);
  text(filStr3, (int) 30, satir+325);
  text(filStr4, (int) 30, satir+350);
  
/*  if (myClient.available() > 0) { 
   inString = (myClient.readString());
     println("calculation from string: " + inString);
    

  try {
    // Parse the data
    String[] dataStrings = split(inString, '#');
    for (int i = 0; i < dataStrings.length; i++) {
      String type = dataStrings[i].substring(0, 4);
      String dataval = dataStrings[i].substring(4);
    if (type.equals("DEL:")) {
        dt = float(dataval);
        
      } else if (type.equals("ACC:")) {
        String data[] = split(dataval, ',');
        x_acc = float(data[0]);
        y_acc = float(data[1]);
        z_acc = float(data[2]);
 
      } else if (type.equals("GYR:")) {
        String data[] = split(dataval, ',');
        x_gyr = float(data[0]);
        y_gyr = float(data[1]);
        z_gyr = float(data[2]);
      } else if (type.equals("FIL:")) {
        String data[] = split(dataval, ',');
        x_fil = float(data[0]);
        y_fil = float(data[1]);
        z_fil = float(data[2]);
        w_fil = float(data[3]);
      }
    }
  } catch (Exception e) {
      //println("Caught Exception");
  }
    }
    */
 }

 
  public void settings() {  size(745, 745,P3D); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc", "Remote" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
