void setupUI() {
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
