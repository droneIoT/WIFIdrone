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

void setupJoystick(int centerx, int centery)
{
  joyDisplayCenterX = centerx-530;
  joyDisplayCenterY = centery;
  curJoyDisplayWidth = maxJoyRange * .85;
  curJoyDisplayHeight = curJoyDisplayWidth;
  maxJoyRange = curJoyDisplayWidth / 2;

  joystickLabel = cp5.addTextlabel("label")
    .setText("JOYSTICK")
      .setPosition(joyDisplayCenterX - 25, joyDisplayCenterY + maxJoyRange + 5)
       .setId(300)
        ;
}

void joystickDraw()
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


void joystickButton(float x, float y, float a)
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
  ellipse(curJoyRange * 0.5, 0, maxJoyRange * 1.5, maxJoyRange * 1.5);

  popMatrix();
}

// Thtrottle controled by mouse wheel
void mouseWheel(MouseEvent event) {
  int e = event.getCount();
  if(e == 1 && throttleValue > 0 ){
     knobTurretRotation.setValue(throttleValue = throttleValue - 5);
  }
  if(e == -1 && throttleValue < 100) {
    knobTurretRotation.setValue(throttleValue = throttleValue + 5);
  }  
}
