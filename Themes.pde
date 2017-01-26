PFont font;
PFont fontHeading;
PFont fontDesigners;

class Themes 
{
  int currentSelection= 0;
  boolean transition = false;
  float y = 0;
  float x = 0;
  float easing = .4;
  String titleOfGame = "ssswwwwtest";
  String artist = "name";
  float totalTextWidth;
  float targetX;
  float dx;
  float dy;
  boolean transitionX = false; 
  int dir = 1; 
  int dirNum = 1; 
  String state = "none";
  
  boolean drawADefaultTheme(String theme)
  {
     if(theme.equals("centered roulette"))
    {
      centeredRoulette();
      return true;
    }
    if(theme.equals("centered list"))
    {
      centeredList();
      return true;
    }
    
    if(theme.equals("two side by side"))
    {
      sideBySide2Games();
      return true;
    }
    
    if(theme.equals("slideshow"))
    {
      Slideshow();
      return true;
    }
    
    fill(255,0,0);
    rect(0,0,width, height);
    fill(255);
    textSize(30);
    textAlign(LEFT, TOP);
    text("selected theme not valid :(", 30, 30);
    return false;
  }
  
  void setText(GameData g)
  {
    artist = g._designers;
    titleOfGame = g._name;
  }
    
    void timer() {
  int passedTime = millis() - savedTime;
  // Has five seconds passed?
  if (passedTime > totalTime) {
    theme.state = "none";
    savedTime = millis(); // Save the current time to restart the timer!
  }
}
    
  Themes()
  {
    if(selectedTheme.equals("two side by side") || selectedTheme.equals("slideshow"))
    {
        font = createFont("Futura-CondensedExtraBold", 30);
        fontHeading = createFont("Futura-CondensedExtraBold", 48);
        fontDesigners = createFont("Futura-CondensedExtraBold", 30);
    }
    else
    {
      //For centeredList
      font = createFont("OratorStd", 30);
      fontHeading = createFont("OratorStd", 36);
      fontDesigners = createFont("OratorStd-Slanted", 24);
    }
  }

  void drawMediaInBackground()
  {
    if( currentSelection >= dataManager.games.size())
      currentSelection = dataManager.games.size() - 1;
      
    //draw video in the background
      for(int i = 0; i!= dataManager.games.size(); i++)
      {
        GameData g = (GameData) dataManager.games.get(i);
        
        if(i == currentSelection)
        {
          
          dataManager.drawMedia(currentSelection, 0, 0, width, height);
        }
        else
        {
          if(g.hasVideo)
          g._video.pause();
        }
      }
  }
  
  void handleSelection()
  {
    if(transition)
    {
      currentSelection += dir;
      if( currentSelection < 0 )
        currentSelection= dataManager.games.size() -1;
      else if( currentSelection >= dataManager.games.size())
        currentSelection = 0;
      transition = false;
    }
    
    if( currentSelection >= dataManager.games.size())
      currentSelection = dataManager.games.size() - 1;
  }
  
 
  
  public void centeredList()
  {
    handleSelection();
    drawMediaInBackground();
    
    
     fill(0,200);
     noStroke();
     rect(width * 0.5 - 300, 0, 600, height);
    
    textFont(font);
    fill(225);
    text("<SELECT GAME>", width * 0.5, 75);
    
    for(int k = 0; k!= dataManager.games.size(); k++)
    {
      //get the game data object
      GameData g = dataManager.getGameData(k);
      
      
      textFont(font);
      boolean selected = k == currentSelection;
      
      flicker();
      
      //choose color
      if(!selected)
        fill(255,150);
      else if(flick)
        fill(#FFEF34);
      else
        fill(#90E8FF);
      
      
      textLeading(48);
      textAlign(CENTER, CENTER);
      
      int colon = g._name.indexOf(":");
      
          
      if(colon == -1)
      {
        if(selected)
        {
          textSize(38);
          text(g._name, width/2, 188 + k * 100 );
          textSize(20);
          text(g._designers, width/2, 212 + k * 100 );
          ////currentlySelectedGame = g;
        }
        else 
        {
          textSize(32);
          text(g._name, width/2, 190 + k * 100 );
          textSize(16);
          text(g._designers, width/2, 210 + k * 100 );
        }  
    }
      else
      {
        String a = g._name.substring(0, colon);
        String b = g._name.substring(colon+2);
        
        if(selected)
        {
          textSize(38);
          text(a, width/2, 175 + k * 100 );
          textSize(30);
          text(b, width/2, 200 + k * 100 );
          textSize(20);
          text(g._designers, width/2, 220 + k * 100 );
        }
        else 
        {
          textSize(32);
          text(a, width/2, 180 + k * 100 );
          
          textSize(24);
          text(b, width/2, 200 + k *100);
          
          textSize(16);
          text(g._designers, width/2, 220 + k * 100 );
        }
      }
        
    }
  }
  boolean rouletteInitialized = false;
  void initializeRoulette()
  {
    if(rouletteInitialized) return;
    rouletteInitialized = true;
    
    
  }
  
  

  
  void centeredRoulette()
  {
    handleSelection();
   
    
    drawMediaInBackground();
    //pushMatrix();
    //translate(width*0.35, 0);
     fill(0,200);
     noStroke();
     rect(width * 0.5 - 300, 0, 600, height);
    
    textFont(font);
    fill(225);
    textAlign(CENTER,CENTER);
    text("<SELECT GAME>", width * 0.5, 75);
    
    //DRAW UNSELECTED GAMES
   
   //pushMatrix();
    textLeading(48);
    textAlign(CENTER, CENTER);
    int n = 1;
      //translate(-275,0);
      
    for( int i = currentSelection +1; i!= currentSelection+5; i++)
    {
      fill(255,200 - n * 50);
      GameData g = dataManager.getGameData(i);
       int colon = g._name.indexOf(":");
      
      if(colon == -1)
      {
          textSize(32);
          text(g._name, width/2, height/2 - 15 + n * 100 );
          textSize(16);
          text(g._designers, width/2, height/2 + 10 + n * 100 );
      }
      else
      {
        String a = g._name.substring(0, colon);
        String b = g._name.substring(colon+2);

         textSize(32);
         text(a, width/2, height/2 - 25 + n * 100 );
          
         textSize(24);
         text(b, width/2, height/2 + n *100);
          
         textSize(16);
         text(g._designers, width/2, height/2 + 20 + n * 100 );
      }
      n++;
    }
    
   
    n = -4;
    for( int k = currentSelection -4; k!= currentSelection; k++)
    {
      
      fill(255,200 + n * 50);
      
      int i = k;
      while(i < 0)
        i += dataManager.games.size();
     
      
    //  if(i<0) continue;
      GameData g = dataManager.getGameData(i);
       int colon = g._name.indexOf(":");
      
      if(colon == -1)
      {
          textSize(32);
          text(g._name, width/2, height/2 - 15 + n * 100 );
          textSize(16);
          text(g._designers, width/2, height/2 + 10 + n * 100 );
      }
      else
      {
        String a = g._name.substring(0, colon);
        String b = g._name.substring(colon+2);

         textSize(32);
         text(a, width/2, height/2 - 25 + n * 100 );
          
         textSize(24);
         text(b, width/2, height/2 + n *100);
          
         textSize(16);
         text(g._designers, width/2, height/2 + 20 + n * 100 );
      }
      n++;
    }
    
    //DRAW SELECTED GAME
       flicker();
    if(flick)
        fill(#FFEF34);
      else
        fill(#90E8FF);
      GameData g = dataManager.getGameData(currentSelection);
       int colon = g._name.indexOf(":");
      
      if(colon == -1)
      {
          textSize(38);
          text(g._name, width/2, height/2 - 20 );
          textSize(20);
          text(g._designers, width/2, height/2 + 20 );
      }
      else
      {
        String a = g._name.substring(0, colon);
        String b = g._name.substring(colon+2);

         textSize(38);
         text(a, width/2, height/2 - 30 );
          
         textSize(30);
         text(b, width/2, height/2 );
          
         textSize(20);
         text(g._designers, width/2, height/2+ 25);
      }
     // popMatrix();
    fill(255,200);
    textAlign(CENTER);
    textSize(30);
    String num = (currentSelection % dataManager.games.size() + 1) +" of "+ dataManager.games.size();
    text(num, width/2, height - 30);
    //popMatrix();
  }
  
  float   flickT  = 0;
  boolean flick = false;
  void flicker()
  {
    if(millis() > flickT)
    {
      flickT += 100;
      flick = !flick;
    }
  }
  

  public void sideBySide2Games()
  {
    
    float videoWidth = width/2;
    float videoHeight = height;
    float padding = 20;
    if(dataManager.games.size() < 2)
    {
      println("this theme requires at least two games!");
      return;
    }
    if(dataManager.games.size() > 2) 
      println("WARNING: this theme only displays two games, but there are more in the data folder! these will not be displayed.");
      
    GameData g1 = dataManager.getGameData(0);
    GameData g2 = dataManager.getGameData(1);

    if (keyPressed) 
    {
      savedTime = millis();
      if (key == 'a') {
        state = "left";
      }
      if (key == 'd') {
        state = "right";
      }
    }
    
    dataManager.drawMedia(0, 0           , 0, videoWidth, videoHeight);
    dataManager.drawMedia(1, 1*videoWidth, 0, videoWidth, videoHeight);

    noStroke();
    // textSize(72);
    textFont(fontHeading);
    textLeading(48);
    textAlign(CENTER, BOTTOM);

    if (state == "left") {
      fill(0, 0, 255, 255);
      rect(width/2, 0, width, height);
      fill(255);
      text(g1._name, width/2+padding, 0, width/2-padding-padding, height/2);
      textFont(fontDesigners);
      textLeading(30);
      text("by "+ g1._designers, width/2+padding, height/2, width/2-padding-padding, height/2-padding);
    } 
    else if (state == "right") {
      fill(0, 0, 255, 255);
      rect(0, 0, width/2, height);
      fill(255);
      text(g2._name, 0+padding, 0, width/2-padding, height/2 );
      textFont(fontDesigners);
      textLeading(30);
      text("by "+ g2._designers, 0+padding, height/2, width/2-padding-padding, height/2-padding );
    } 
    timer();
  }


  boolean slideMoving = false;
  public void Slideshow() 
  {
    
    GameData g = dataManager.getGameData(currentSelection);
    setText(g);
    // Transition Animation 
    if (transition == true)
    { 
      if(slideMoving == false)
      {
        slideMoving = true;
        currentSelection++;
      }
      dy = dir * height - y;
      if (abs(dy) > 1) { 
        y += dy * easing;
      }    // Easing code
      if (y >= (height - 1) && dir == 1 || y <= (-height + 1) && dir == -1 )
      {                        // If the movie has animated past the heaight of your window then do things. 
        //g._video.stop();
        if(g.hasVideo)
          g._video.pause();
       // currentSelection++; 
        slideMoving = false;
        transition = false;    // Tell the program to stop transtition. 
        y = 0;                 // reset y position of video to 0
      }
    }
    
    
    g = dataManager.getGameData(currentSelection);  // FIX for for video flickr once the transisition is done. 
   
    // Next Movie 
    if (transition == true)
    { 
      dataManager.drawMedia((currentSelection - 1), 0,y,width,height);
      dataManager.drawMedia((currentSelection), 0, y-height*dir,width,height);
    }else{
      dataManager.drawMedia(currentSelection, 0,y,width,height);
    }

    textAlign(LEFT);
    // Check Text Width for both the Title and Artist to see how big the box should be
    textFont(fontHeading);
    textSize(26);
    float w = textWidth(titleOfGame); // Check width of Title Text
    textFont(fontDesigners);
    textSize(22);
    float wartist = textWidth(artist); // Check width of Title Text
    if (w < wartist) { 
      totalTextWidth = wartist;
    } 
    else { 
      totalTextWidth = w;
    } // If Artist names are longer then set totalTextWidth to Artist text length or set it to Title Text Width

    if (transition == true)
    {
      targetX = width + 400;
      dx = targetX - x;
    } 
    else if (transitionX == true || x >= (width - 1)) {
      transitionX = true;
      targetX = width - totalTextWidth - 50;
      dx = targetX - x;
    } 
    else {
      x = width - totalTextWidth - 50;
      transitionX = false;
    }
    if (abs(dx) > 1) { 
      x += dx * easing;
    }

    // Make Box big Enough to hold the text
    fill(0, 174, 239);
    stroke(0, 0);
    rect(x+3, height-140, totalTextWidth+70, 80);
    fill(255);

    // Print Text to Screen
    textFont(fontHeading);
    textSize(26);
    text(titleOfGame, x+30, height-105);  // Text wraps within text box
    textFont(fontDesigners);
    textSize(22);
    text(artist, x+30, height-75);
  }
}
