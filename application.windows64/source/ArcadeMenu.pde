import java.awt.Frame;
import javax.swing.JFrame;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.*;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.IntellitypeListener;
import com.melloware.jintellitype.JIntellitype;

import processing.video.*;
import java.io.File;

JIntellitypeTester mainFrame = new JIntellitypeTester();

GameData currentlySelectedGame;

String platform = "windows";
String selectedTheme = "centered list";
boolean useJoyToKey = true;
boolean displayLogo = true;

String joy2keyPath;

int savedTime;
int totalTime = 5000;
PImage logo;


GameDataManager dataManager;
Themes theme;
Movie noVid;

void settings()
{
  //smaller for testing purposes
 // size(1080,720);
  
  fullScreen();
}

boolean gameIsRunning = false;

void setup()
{
  staticWindow = new Window(null);
  joy2keyPath = dataPath("joyToKey/JoyToKey.exe");
  lastTimeCheck = millis();
  readConfig();

  noCursor();
  size(displayWidth, displayHeight);
  savedTime = millis();
  
  logo = loadImage("logo.png");
 // noVid = new Movie(this, "default.mov");
  dataManager = new GameDataManager(this);
  theme = new Themes();
  
  initGlobalKeyListener();
  try{
    Runtime.getRuntime().exec(joy2keyPath + " menuselect.cfg");
  }
  catch(Exception ex)
  {
    println("error launching joy2key");
  }
}

float escapeKeyTimer = 0;

void draw()
{
  background(0);
  
  
  //theme.drawADefaultTheme(selectedTheme);
  
  newCenteredRoulette();
  
  if(displayLogo)
    drawLogo();
    
    
  if(gameIsRunning)
  {
    /*
    if(HoldingEscapeKeys())
    {
      println("holding escape keys");
      if(escapeKeyTimer == 0) 
        escapeKeyTimer = millis();
      if(millis() > escapeKeyTimer + 2000)
      {
        closeGameOpenMenu();
        escapeKeyTimer = 0;
      }
    }
    else escapeKeyTimer = 0;
    */
  }
  manageTopWindow();
    //drawLoadingMessage();
    
    
}

void readConfig()
{
  try
  {
     JSONObject config = loadJSONObject(dataPath("config.json"));
     platform = config.getString("platform");
     selectedTheme = config.getString("theme");
     
     useJoyToKey = evaluateIfTrue(config.getString("use joy2key"));
     println(useJoyToKey);
    displayLogo = evaluateIfTrue(config.getString("display logo"));
    
  
  }
  catch(RuntimeException ex)
  {
    println("error reading config.JSON file! using defaults instead.");
    println("make sure config.json exists in the data folder and is formatted according to the instructions in the README file.");
  }
}

boolean evaluateIfTrue(String str)
{
  str = str.toLowerCase();
     if(str.equals("true")|| str.equals("t") || str.equals("yes") || str.equals("y"))
     {
       return true;
     }
     else if(str.equals("false") || str.equals("f") || str.equals("no")|| str.equals("n"))
     {
       return false;
     }else{
       println("error! I don't understand \"" + str + "\"");
       return false;
     }
}



void stop() 
{
  for(int i = 0; i!= dataManager.games.size(); i++)
  {
    GameData g = (GameData) dataManager.games.get(i);
    g._video.stop();
  }
} 

void drawLogo()
{
    int temp = int(width * .09);
    logo.resize(temp, 0);
    image(logo, 30, 0);
}