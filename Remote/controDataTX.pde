
void appTX(){
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

   XAxis = int(20*round(x));
   YAxis =  int(20*round(y));

   XAxis= round(map(XAxis, -200, 200, -100,100));
   YAxis= round(map(YAxis, -200, 200, -100,100));
   qx= round(map(qx, -200, 200, -100,100));
 
 command = "Y:" + YAxis + ", X:" + XAxis + ", Throttle:" + int((9*throttle)+round(Ticks3Value) )+ ", Rudder:" + qx +".";
 
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
