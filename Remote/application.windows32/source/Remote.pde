//import ui library for buttons
import controlP5.*;
ControlP5 cp5;

//import udp library
import hypermedia.net.*;
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
float angle = 0.01;
boolean showsensorsData=false;

void setup() {
    
   //set window dimensions
   size(745, 745,P3D);

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


void draw() {
  
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
      angle += 0.01;
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
