import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.awt.Frame; 
import javax.swing.JFrame; 
import com.sun.jna.platform.win32.User32; 
import com.sun.jna.platform.win32.*; 
import com.melloware.jintellitype.HotkeyListener; 
import com.melloware.jintellitype.IntellitypeListener; 
import com.melloware.jintellitype.JIntellitype; 
import processing.video.*; 
import java.io.File; 
import java.awt.Point; 
import java.awt.Rectangle; 
import java.io.IOException; 
import java.lang.reflect.Field; 
import java.util.ArrayList; 
import java.util.Arrays; 
import java.util.List; 
import java.util.regex.Pattern; 
import com.sun.jna.Native; 
import com.sun.jna.Pointer; 
import com.sun.jna.platform.win32.Kernel32; 
import com.sun.jna.platform.win32.Shell32; 
import com.sun.jna.platform.win32.User32; 
import com.sun.jna.platform.win32.WinBase.PROCESS_INFORMATION; 
import com.sun.jna.platform.win32.WinBase.STARTUPINFO; 
import com.sun.jna.platform.win32.WinDef.DWORD; 
import com.sun.jna.platform.win32.WinDef.HWND; 
import com.sun.jna.platform.win32.WinDef.INT_PTR; 
import com.sun.jna.platform.win32.WinDef.RECT; 
import com.sun.jna.platform.win32.WinNT.HANDLE; 
import com.sun.jna.platform.win32.WinUser; 
import com.sun.jna.ptr.IntByReference; 

import com.sun.jna.*; 
import com.sun.jna.ptr.*; 
import com.sun.jna.win32.*; 
import com.sun.jna.platform.*; 
import com.sun.jna.platform.dnd.*; 
import com.sun.jna.platform.mac.*; 
import com.sun.jna.platform.unix.*; 
import com.sun.jna.platform.win32.*; 
import com.sun.jna.platform.win32.COM.*; 
import com.sun.jna.platform.win32.COM.tlb.*; 
import com.sun.jna.platform.win32.COM.tlb.imp.*; 
import com.sun.jna.platform.wince.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ArcadeMenu extends PApplet {














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

public void settings()
{
  //smaller for testing purposes
 // size(1080,720);
  
  fullScreen();
}

boolean gameIsRunning = false;

public void setup()
{
  staticWindow = new Window(null);
  joy2keyPath = dataPath("joyToKey/JoyToKey.exe");
  lastTimeCheck = millis();
  readConfig();

  noCursor();
  
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

public void draw()
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

public void readConfig()
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

public boolean evaluateIfTrue(String str)
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



public void stop() 
{
  for(int i = 0; i!= dataManager.games.size(); i++)
  {
    GameData g = (GameData) dataManager.games.get(i);
    g._video.stop();
  }
} 

public void drawLogo()
{
    int temp = PApplet.parseInt(width * .09f);
    logo.resize(temp, 0);
    image(logo, 30, 0);
}
class GameData
{
  boolean _completeData;  
  public boolean hasVideo;
  public int slideshowPos = 0;
  
  //text data to be displayed with the game, loaded from a JSON file in the parent directory for the game
  String _name;
  String _designers;
  String _description;
  
  
  //contains any command arguments the game needs to run properly with the menu
  String _commandArgs;
  
  //Game maker launches two exes, both of which must be killed on kill(). this contains the name of the other exe.
  String[] associatedExes;
  
  File _gameDirectory; //the parent directory for the game, containing folders for each platform, video/image data, and the JSON data file
  File _executable; //the actual address of the executable, assigned in the constructor based on the global string platformName
  
  File[] _videoFiles; //the address of the video, if any, to be displayed with the game.
  File[] _imageFiles; //the address of the images, if any, to be displayed with the game.
  
  public PImage[] _images;
  public PImage _instructionsImage;
  public Movie _video;
  
  public boolean findJSON(File gameDirectory)
  {
    File[] JSONs = gameDirectory.listFiles(jsonFilter);
    if(JSONs.length != 0)
    { 
      String tempGame = JSONs[0].toString();
      JSONObject gameInfo = loadJSONObject(tempGame);
      
      if(gameInfo != null)
      {
        _name = gameInfo.getString("name");
        _designers = gameInfo.getString("designers");
        //_name = gameInfo.getString("name", notFound);
        _description = gameInfo.getString("description");
        //_description = gameInfo.getString("description", notFound);
        _commandArgs = gameInfo.getString("command arguments", "");
        try{
          JSONArray associatedExeList = gameInfo.getJSONArray("extra exes to kill");
          associatedExes = new String[associatedExeList.size()];
          for(int i = 0; i!= associatedExes.length; i++)
          {
            associatedExes[i] = associatedExeList.getJSONObject(i).getString("exe", "");
          }
          println("found extra exes to kill:");
          for(int i = 0; i!= associatedExes.length; i++)
          {
            println(associatedExes[i]);
          }
        }catch(RuntimeException ex)
        {
          println("didn't find any extra exes to kill");
        }
      }
      
      println("name: " + _name);
      println("description: " + _description);
      println("command arguments: " + _commandArgs);
      return true;
    } 
    else
    {
      println("name: didn't find a JSON object in " + _gameDirectory.getName());
      println("description: didn't find a JSON object in " + _gameDirectory.getName());
      return false;
    }
  }

  GameData (File gameDirectory, PApplet parent)
  {
    println("\n----------\nCreating a new GameData object\n----------");
    
    _completeData = true;
    _gameDirectory = gameDirectory;
    
    //look for the JSON file
    _completeData = findJSON(gameDirectory);
   

    //Get all child directories in the game folder
    File[] childDirectories = gameDirectory.listFiles(directoryFilter);
   
    for(File f : childDirectories)
    {
      String filename = f.getName();
      
      //get image addresses
      if(filename.equals("image"))
      {
        println("found image folder");
        _imageFiles = f.listFiles();
       listImages();
        continue;
      }
      
      if(filename.equals("instructions"))
      {
        File[] imgs = f.listFiles();
        
        if(imgs.length <= 0 )continue;
        
        _instructionsImage = loadImage(imgs[0].getAbsolutePath());
      }
      
      //get video addresses
      if(filename.equals("video"))
      {
        _videoFiles = f.listFiles(videoFilter);
        listVideos();
        continue;
      }
      
      //if current folder isn't the selected platform, skip it.
      if(!filename.equals( platform ))
         continue;
         
      
      File[] contents = f.listFiles();
      
      //if we found the folder but it has nothing in it, quit the loop and print an error.
      if(contents.length<1)
      {
        println("executable: " + _gameDirectory.getName() + "/" + filename + " does not contain anything, skipping it.");
        _completeData = false;
        break;
      }
      
      //if we found a non-directory file, make that the executable.
      if(contents.length != 0)
      {
        int i = 0;
        for(; i!= contents.length; i++)
        {
          if(!contents[i].isDirectory() && contents[i].getName().indexOf(".exe") != -1)
          {
            _executable = contents[i];
            break;
          }
        }
        
        if(i == contents.length)
        {
          _completeData = false;
          println("executable: "+ _gameDirectory.getName() + "/" + filename + " does not contain any files, only directories. skipping it");
        }
        else
        {
          println("executable: " + _executable.getName());
          break;
        }
      }
    }
    
    loadImages();
    loadVideo(parent, _videoFiles);
    
  }
  
  public void loadImages()
  {
    //load the images from the addresses found.
    if(_imageFiles == null) return;
        
    _images = new PImage[_imageFiles.length];
    
    for(int i = 0; i!= _imageFiles.length; i++)
      _images[i] = loadImage(_imageFiles[i].getAbsolutePath());
      
  }
  
  public void loadVideo(PApplet parent, File[] files)
  {
    if(files != null && files.length > 0)
    {
      _video = new Movie(parent, files[0].getAbsolutePath());
      
      _video.stop();
     // _video.volume(1);
      hasVideo = true;
    }
    else
    {
      hasVideo = false;
     //_video = noVid; 
     
    // _video.loop();
     //_video.volume(0);
    }
  }
  
  
  
 public void launchGame()
 {
    if(!gameIsRunning)
    {
      try 
      {
        if(useJoyToKey == true)
        {
         String configName = stripExtension(_executable.getName());
          Runtime.getRuntime().exec(joy2keyPath + " " + configName + ".cfg");
        }
        //String command = "cmd /c start /d \"" +  _executable.getParentFile().getAbsolutePath() + "\" " + _executable.getName() + " " + _commandArgs + " /c start";
        //String command = "pushd \"" +  _executable.getParentFile().getAbsolutePath() + "\" & " + _executable.getName() + " " + _commandArgs;// + " /c start";
        //print("commmmmmmmmmmmmmmmande : " + command);
        String exePlusArgs =  _executable.getName() + " " + _commandArgs;// + " /c start";
        
        currentGame = Runtime.getRuntime().exec(_executable.getAbsolutePath() + " " + _commandArgs + "/c start", null, _executable.getParentFile());
         
        //currentGame = Runtime.getRuntime().exec(_executable.getAbsolutePath() + " " + _commandArgs + "/c start");
        
         
         println(staticWindow.getProcessID(currentGame));
         //print(process);
         println("Program should now open.");
         gameIsRunning = true;
      } catch (Exception ex) {
         ex.printStackTrace();
      } 
    }
  }
  
  public void kill() {
    if(gameIsRunning)
    {
      try {
        loop();
          
          //kill game
         Runtime.getRuntime().exec("taskkill /F /IM "+_executable.getName());
         System.out.println("Program should now be closed.");
        
        //switch joy2key to the menu keybindings
        Runtime.getRuntime().exec(joy2keyPath + " menuselect.cfg");
          gameIsRunning = false;
      } catch (Exception ex) {
         ex.printStackTrace();
      } 
     
    }
  }
  
  public void listVideos()
  {
    println("videos: " + _videoFiles.length);
    for(File vid : _videoFiles)
    {
      println("  " + vid.getName());
    }
  }
  
  public void listImages()
  {
     println("images: " + _imageFiles.length);
        for(File img : _imageFiles)
        {
          println("  " + img.getName());
        }
  }
}

public String stripExtension(String str)
{
  if(str == null) return null;
  
  int pos = str.lastIndexOf(".");
  if(pos == -1) return str;
  return str.substring(0,pos);
}

java.io.FilenameFilter directoryFilter = new java.io.FilenameFilter()
{
   @Override 
    public boolean accept(File current, String name) 
      {
        return new File(current, name).isDirectory();
      }
};

java.io.FilenameFilter jsonFilter = new java.io.FilenameFilter() {
  public boolean accept(File dir, String name) {
    return name.toLowerCase().endsWith(".json");
  }
};

java.io.FilenameFilter imageFilter = new java.io.FilenameFilter() 
{
  public boolean accept(File dir, String name)
  {
    return (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".png"));
  }
};

java.io.FilenameFilter videoFilter = new java.io.FilenameFilter() 
{
  public boolean accept(File dir, String name)
  {
    return (name.toLowerCase().endsWith(".mov") || name.toLowerCase().endsWith(".mp4") || name.toLowerCase().endsWith(".m4v") );
  }
};

//This is needed for the video stuff.
public void movieEvent(Movie m) 
{
  m.read();
}
//
//
/*
* GameDataManager Class
* Generates and contains the list of all GameData Objects.
* Also contains helper functions for:
*   drawing the media associated with a game to the screen,
*   Getting references to GameData objects based on their index in the list,
*   loading/killing games based on their index in the list
*/

class GameDataManager
{
  File gamesFolder;         //the folder in data that contains folders for each game
  File[] gamesDirectories; //all the child directories of gamesFolder
  ArrayList games;          //contains GameData objects for each game.

  

  public GameDataManager(PApplet parent)
  {

    gamesFolder = new java.io.File(dataPath("games"));

    gamesDirectories = gamesFolder.listFiles (directoryFilter); 

    games = new ArrayList();
    for (File f : gamesDirectories)
    {
      GameData g = new GameData(f, parent);
      games.add(g);
    }
  }
  
  public GameData getGameData(int gameIndex)
  {
    return (GameData) games.get(gameIndex % dataManager.games.size());
  }
  
  ////////////////////////////  
  //DRAWING HELPER FUNCTIONS//
  ////////////////////////////

  //draws a slideshow of all the images associated with a game
  public void drawSlideshow(int gameIndex, float x, float y, float w, float h, float d)
  {
    if(games.size() == 0) return;

    GameData g = (GameData) games.get(gameIndex % games.size());
    
    if(g._images == null || g._images.length == 0) return;
        
    //total duration of the cycle
    float duration = d * g._images.length * 1000;
    
    //get the index of the frame we want
    int imgIndex = (int) ((millis() % duration) / duration * g._images.length);
    
    //draw the image!
    image(g._images[imgIndex], x, y, w, h);
  }
  
  //draws the "best" media available for a given game:
  //displays video if available, else a slideshow if available, else a single image if available, else a frowny face because there's no pix!
  public void drawMedia(int gameIndex, float x, float y, float w, float h)
  {
    GameData g = (GameData) games.get(gameIndex % games.size());
    /*
      if (g.hasVideo)
      {
        //draw a video!
        if(gameIsRunning)
          g._video.pause();
        else
          g._video.loop();
        image(g._video, x, y, w, h);
      }
      else */
      if(g._images == null || g._images.length <1)
      {
        //no images at all! draw a frowny face to let you know you messed up.
        fill(0xffff00ff);
        rect(x,y,w,h);
        fill(0xffffffff);
        textAlign(CENTER, CENTER);
        textSize(h/2);
        text(":(", x+w/2, y+h*0.3f);
        textSize(h/6);
        text("no images", x + w/2, y+h*0.7f);
      }
      else if(g._images.length > 1)
      {
        //draw a slideshow
         drawSlideshow(gameIndex, x + random(-250), y+ random(-250), w+500,h+500, 1);
      }
      else 
      {
        //draw a single image
        image(g._images[0], x, y, w, h);
      }
  }
  public void launchGame(int gameIndex)
  {
    GameData g = (GameData) games.get(gameIndex % games.size());
    g.launchGame();
  }
  
  public void killGame(int gameIndex)
  {
    GameData g = (GameData) games.get(gameIndex % games.size());
    g.kill();
  }  
  
  
  
  //FOR YOUR DEBUGGING CONVENIENCE//
  //These functions will display all the images/videos found for each game in a row of 100x100 squares.
  //If you have too many games/still images to fit on the screen, try the displayAllImgSlideshow method to save some space. 
  
  //Displays all images found for all games, separating images for different games by 10px
  public void displayAllImages(float y)
  {
    
    float i = 0;
    for (int k = 0; k!= games.size(); k++)
    {
            fill(100,100);
      rect(i*100, y-10, 10,110);
      i += 0.1f;
      GameData g = (GameData) games.get(k);
      fill(255);
      textAlign(LEFT, CENTER);
      text(g._name, i * 100, y);
      if(g._images== null) continue;
      for (PImage p : g._images)
      {
        image(p, i*100, y+10, 100, 100);
        i++;
      }

    }
  }
  
  //Displays a slideshow of all images for each game (for debugging)
  public void displayAllImgSlideshow (float y)
  {
    for (int k = 0; k!= games.size(); k++)
    {
      GameData g = (GameData) games.get(k);
      

      drawSlideshow(k,k*100, y+20, 100, 100, 0.5f);
      fill(100,100);
      rect(k*100,y,100,20);
      fill(255);
      textAlign(CENTER, CENTER);
      text(g._name, k * 100, y, 100,20);
    }
 
  }
  
  //Displays the video for each game. (for debugging
  public void displayAllVideos(float y)
  {
    for (int k = 0; k!= games.size(); k++)
    {
      GameData g = (GameData) games.get(k);

      fill(100,100);
      rect(k*100,y,100,20);
      fill(255);
      textAlign(CENTER, CENTER);
      text(g._name, k * 100, y, 100,20);

      if (g._video != null)
      {
        image(g._video, k * 100, y+20, 100, 100);
      }
    }
  }
}
Window staticWindow;
Window appWindow;
Process currentGame;
Field globalHandle;
int pid;
int lastTimeCheck;
int timeIntervalFlag = 2000;


public class JIntellitypeTester extends JFrame implements HotkeyListener {

  public void onHotKey(int aIdentifier) {
    if (aIdentifier == 1 || aIdentifier == 2) 
    {
      if(gameIsRunning)
        {
        loop();
        dataManager.killGame(theme.currentSelection);
        gameIsRunning = false;
        println("just attempted to kill game");
        
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow (null, "ArcadeMenu"); // window title
        if (hwnd == null) {
          System.out.println("CANT FIND MY WINDOW");
        }
        else
        {
          User32.INSTANCE.ShowWindow(hwnd, 9 );        // SW_RESTORE
          User32.INSTANCE.SetForegroundWindow(hwnd);   // bring to front
        }
      }  
      else 
        exit();
    }
  }
}


public void manageTopWindow()
{
  if ( millis() > lastTimeCheck + timeIntervalFlag ) 
  {
    if(gameIsRunning)
      ensureGameIsOnTop();
    else
      ensureMenuIsOnTop();
      
    lastTimeCheck += timeIntervalFlag;
  }
}

public void ensureGameIsOnTop()
{
  fill(255);
  ellipse(10,10,20,20);
  println("ensuring game is on top");
  appWindow = staticWindow.getProcessWindow(pid);
  println(appWindow.getTitle());
  if(appWindow == null) println("appwindow is null");
  else if(appWindow.getProcessID() != staticWindow.getForegroundWindow().getProcessID())
  {
     println("NOT ON TOP! SETTING NOW" + appWindow.getProcessID());
     appWindow.maximize();
     appWindow.setForeground();
     
  }
  println("just set foreground");
}

public void ensureMenuIsOnTop()
{
  println("ensuring menu is on top");
  Window menu = staticWindow.findWindow(null, "ArcadeMenu");
  if(menu.getProcessID() != staticWindow.getForegroundWindow().getProcessID())
  {
    println("menu not on top, setting to foreground");
    menu.setForeground();
    menu.maximize();
  }
}
float kRelease = 0;
public void keyPressed() 
{ 
  println(millis() + " " + key);
  if(millis()-kRelease <200)return;
  else kRelease = millis();
  if(instructionsUp)
  {
    if(key == 'w' || key == 's')
      instructionsSelection = !instructionsSelection;
  }
  
  if (!instructionsUp && (key == 'w' || key == 's') )
  {
    transition = true;
    theme.transition = true; // Tell Program that you are transitioning to next video
    
      if (key == 'w') 
      {
          theme.dir = -1; //UPS
          dir = -1;
      }
      
      if (key == 's')
      {
          theme.dir = 1; //DOWN
          dir = 1;
      }
  }
  
   if (keyCode == UP) 
   {
     if(instructionsUp)
     {
       if(instructionsSelection)
       {
          drawLoadingMessage();
          launchApplication();
          instructionsUp = false;
       }
       else
       {
         instructionsUp = false;
       }
     }
     else
     {
       instructionsUp = true;
     }

   }
}


public void launchApplication()
{
  try
  {
    dataManager.launchGame(theme.currentSelection);
     gameIsRunning = true;
     if (currentGame.getClass().getName().equals("java.lang.Win32Process") || currentGame.getClass().getName().equals("java.lang.ProcessImpl")) {
      /* determine the pid on windows plattforms */
      try {
        globalHandle = currentGame.getClass().getDeclaredField("handle");
        globalHandle.setAccessible(true);
        long handl = globalHandle.getLong(currentGame);

        Kernel32 kernel = Kernel32.INSTANCE;
        WinNT.HANDLE handle = new WinNT.HANDLE();
        handle.setPointer(Pointer.createConstant(handl));
        pid = kernel.GetProcessId(handle);
        appWindow = staticWindow.getProcessWindow(pid);
        println("Detected pid: " + pid);
      
      }
      catch (Throwable e)
      {
        e.printStackTrace();
      }
    }
  }
  catch(Exception ex)
  {
  }
}

public void initGlobalKeyListener()
{
     // Initialize JIntellitype 
  JIntellitype.getInstance();
  JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_ALT + JIntellitype.MOD_SHIFT, 'B');
  JIntellitype.getInstance().registerHotKey(2, 0, 27); // registering ESC key as well to avoid killing the game process without changing gameIsRunning
  try {
    JIntellitype.getInstance().addHotKeyListener(mainFrame);
    println("JIntellitype initialized");
  } 
  catch (RuntimeException ex) 
  {
    println("Either you are not on Windows, or there is a problem with the JIntellitype library!");
  }
}
  
ArrayList slots;
boolean slotsInitialized = false;
int currentSelection = 0;
PFont orator;
public void initializeSlots()
{
  orator = createFont("OratorStd", 30);
  textFont(orator);
  if(slotsInitialized) 
    return;
  slots = new ArrayList();
  
  for(int i = 0; i!= dataManager.games.size(); i++)
    slots.add( new Slot( (GameData) dataManager.games.get(i), i ) );
    
  slotsInitialized = true;
}

boolean instructionsUp = false;

boolean transition = false;
int dir = 0;

public void handleSelection()
{
  if(transition)
  {
    currentSelection += dir;
    
    if(currentSelection < 0)
      currentSelection = slots.size() - 1;
    else if(currentSelection >= slots.size())
      currentSelection = 0;
    
    transition = false;
  }
}

public void drawLoadingMessage()
{
  fill(0,200);
  rectMode(CORNER);
  rect(0,0,width,height);
  fill(255);
  textAlign(CENTER,CENTER);
  text("LOADING...", width * 0.5f, height * 0.5f);
}

public void newCenteredRoulette()
{
 
  initializeSlots();
   textFont(orator);
  handleSelection();
  theme.currentSelection = currentSelection;
  theme.drawMediaInBackground();
  
  if(instructionsUp)
    displayInstructions();
  else
    displaySelector();
  
  if(gameIsRunning) 
    drawLoadingMessage();
}

public void displaySelector()
{
  fill(0,200);
  noStroke();
  rect(width * 0.5f - 300, 0, 600, height);
  textFont(font);
  
  fill(255);
  textAlign(CENTER,CENTER);
  text("<SELECT GAME>", width * 0.5f, 75);
  for(int i = 0; i!= slots.size(); i++)
  {
    Slot current = (Slot) slots.get(i);
    current.update();
  }
}

boolean instructionsSelection = true;
public void displayInstructions()
{
  GameData g = dataManager.getGameData(currentSelection );
  
  //fill(0,40);
  int margin = 100;
 // rect(margin+5, margin+5,width-margin * 2, height - margin * 2);
  
  fill(0,200);
  rect( width * 0.5f - 450,0, 900, height);
  
  float topTextHeight = margin + 40;
  
  textSize(40);
  fill(255);
  textAlign(CENTER,TOP);
  text(g._name, width * 0.5f, topTextHeight);
  
  textSize(30);
  text(g._designers, width * 0.5f,topTextHeight + 40);
  
  textAlign(CENTER,TOP);
 //textSize(20);
 // text(g._description, width * 0.5,topTextHeight + 90);
  
  textAlign(CENTER,CENTER);
  if(instructionsSelection)
    fill(0,255,255);
  else
    fill(150);
  
  text("PLAY", width * 0.5f, height - margin - 100);
  
  if(!instructionsSelection)
    fill(0,255,255);
  else
    fill(150);
  text("BACK", width * 0.5f, height - margin - 50);
  imageMode(CENTER);
  image(g._instructionsImage, width * 0.5f, height * 0.5f);
  imageMode(CORNER);
}
  
class Slot
{
  public GameData game;
  public int indx;
  float targetY;
  float y;
  
  Slot(GameData g, int index)
  {
    indx = index;
    game = g;
    targetY = height * 0.5f + (indx - currentSelection) * 40f;
    y = targetY;
  }
  
  public void update()
  {
    //println("y: " +  y + " index: " + indx);
    y += (targetY-y) * 0.5f;
    
    if(abs(targetY-y) > 60f) 
    {
      if(targetY > y) y = targetY + 59f;
      else y = targetY - 59f;
    }
    
    getTargetY();
    
    textAlign(CENTER);
    
    float a = 255 - abs(y-(height * 0.5f)) * 1.5f;
    
    if(currentSelection == indx)
    {
      fill(0,255,255);
    }else
      fill(255, a);
      
    textSize(30);
    if(game == null) game = dataManager.getGameData(indx );
    if(game != null)
    {
        
        text(game._name, width * 0.5f, y);
        textSize(20);
        text(game._designers, width * 0.5f, y + 20);
    
    }

  }
  
  public void getTargetY()
  {
    float spacing = 100f;
    int offsetFromSelection = indx - currentSelection;
    
    if(abs(offsetFromSelection) < dataManager.games.size() * 0.5f)
      targetY = height * 0.5f + offsetFromSelection * spacing;
     else
     {
       if(offsetFromSelection < 0)
       {
         int offsetFromCenter = offsetFromSelection + dataManager.games.size();
         targetY = height * 0.5f + offsetFromCenter * spacing;
       }
       else
       {
         int offsetFromCenter = offsetFromSelection - dataManager.games.size();
         targetY = height * 0.5f + offsetFromCenter * spacing;
       }
     }
  }
}
PFont font;
PFont fontHeading;
PFont fontDesigners;

class Themes 
{
  int currentSelection= 0;
  boolean transition = false;
  float y = 0;
  float x = 0;
  float easing = .4f;
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
  
  public boolean drawADefaultTheme(String theme)
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
  
  public void setText(GameData g)
  {
    artist = g._designers;
    titleOfGame = g._name;
  }
    
    public void timer() {
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

  public void drawMediaInBackground()
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
  
  public void handleSelection()
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
     rect(width * 0.5f - 300, 0, 600, height);
    
    textFont(font);
    fill(225);
    text("<SELECT GAME>", width * 0.5f, 75);
    
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
        fill(0xffFFEF34);
      else
        fill(0xff90E8FF);
      
      
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
  public void initializeRoulette()
  {
    if(rouletteInitialized) return;
    rouletteInitialized = true;
    
    
  }
  
  

  
  public void centeredRoulette()
  {
    handleSelection();
   
    
    drawMediaInBackground();
    //pushMatrix();
    //translate(width*0.35, 0);
     fill(0,200);
     noStroke();
     rect(width * 0.5f - 300, 0, 600, height);
    
    textFont(font);
    fill(225);
    textAlign(CENTER,CENTER);
    text("<SELECT GAME>", width * 0.5f, 75);
    
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
        fill(0xffFFEF34);
      else
        fill(0xff90E8FF);
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
  public void flicker()
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


























/**
 * This is a wrapper for the HWND type that is in JNA 4.0.
 * Based on a class written by  John Henckel, Oct 2014
 * which can be found at: https://answers.launchpad.net/sikuli/+question/255504
 */
public class Window
{
    public HWND hWnd;

    public Window(HWND hwnd)
    {
        this.hWnd = hwnd;
    }

    public boolean isNull()
    {
        return hWnd == null || hWnd.hashCode() == 0;
    }

    public Window getForegroundWindow()
    {
        return new Window(User32.INSTANCE.GetForegroundWindow());
    }

    public void setForeground()
    {
        User32.INSTANCE.SetForegroundWindow(hWnd);
        minimize();
        restore();
    }

    /**
     * Flash the window three times
     */
    public void flash()
    {
        WinUser.FLASHWINFO pfwi = new WinUser.FLASHWINFO();
        pfwi.cbSize = 20;
        pfwi.hWnd = hWnd;
        pfwi.dwFlags = 3; // 3 = FLASHW_ALL
        pfwi.uCount = 3;
        pfwi.dwTimeout = 250;
        User32.INSTANCE.FlashWindowEx(pfwi);
        sleep(1000);
    }

    /**
     * Returns the class of the window, such as "SWT_Window0" for any SWT application. See AutoIt Window Info Tool
     */
    public String getClassName()
    {
        char[] buffer = new char[2048];
        User32.INSTANCE.GetClassName(hWnd, buffer, 1024);
        return Native.toString(buffer);
    }

    public boolean isVisible()
    {
        return User32.INSTANCE.IsWindowVisible(hWnd);
    }

    /**
     * 3=SW_MAXIMIZE, etc
     */
    private void showWindow(int nCmdShow)
    {
        User32.INSTANCE.ShowWindow(hWnd, nCmdShow);
    }

    public void minimize()
    {
        showWindow(6);
    }

    public void maximize()
    {
        showWindow(3);
    }

    /**
     * Activate and displays the window in normal mode (not minimized or maximized)
     */
    public void restore()
    {
        showWindow(9);
    }

    /**
     * Return true if the window message queue is idle, false if timeout
     */
    public boolean waitForInputIdle(int timeout_ms)
    {
        IntByReference lpdwProcessId = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hWnd, lpdwProcessId);
        return User32.INSTANCE.WaitForInputIdle(new HANDLE(lpdwProcessId.getPointer()), new DWORD(timeout_ms)).intValue() == 0;
    }

    public void setRectangle(Rectangle rect)
    {
        User32.INSTANCE.MoveWindow(hWnd, rect.x, rect.y, rect.width, rect.height, true);
    }

    public String getTitle()
    {
        char[] buffer = new char[2048];
        User32.INSTANCE.GetWindowText(hWnd, buffer, 1024);
        return Native.toString(buffer);
    }

    public int getProcessID()
    {
        IntByReference processID = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hWnd, processID);
        return processID.getValue();
    }

    public Rectangle getRectangle()
    {
        RECT rect = new RECT();
        User32.INSTANCE.GetWindowRect(hWnd, rect);
        return new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
    }

    /**
     * In windows the user can customize the border and title bar size.
     * @return x = border thickness in pixels, y = x + titlebar height
     */
    public Point getClientOffset()
    {
        return new Point(getBorderSize(), getTitleHeight());
    }

    /**
     * In windows the user can customize the border and title bar size.
     * @return titlebar height
     */
    public int getTitleHeight()
    {
        int f = User32.INSTANCE.GetSystemMetrics(33); // 33 = SM_CYSIZEFRAME
        return User32.INSTANCE.GetSystemMetrics(4) + f; // 4 = SM_CYCAPTION
    }

    /**
     * In windows the user can customize the border and title bar size.
     * @return border thickness in pixels
     */
    public int getBorderSize()
    {
        int f = User32.INSTANCE.GetSystemMetrics(32); // 32 = SM_CXSIZEFRAME
        return User32.INSTANCE.GetSystemMetrics(5) + f; // 5 = SM_CXBORDER
    }

    /**
     * Find the FIRST top level window with specified class and title.
     * @param className such as "SWT_Window0" or null
     * @param title such as "QREADS" or null
     * @return first window found, or null if not found.
     */
    public Window findWindow(String className, String title)
    {
        return new Window(User32.INSTANCE.FindWindow(className, title));
    }

    /**
     * Get the next top-level window in Z-order, (from foreground to background).
     *
     * To iterate all windows, use
     *
     * for (Window w = getForegroundWindow(); !w.isNull(); w = w.next()) ...
     *
     * @return
     */
    public Window next()
    {
        return new Window(User32.INSTANCE.GetWindow(hWnd, new DWORD(2))); // 2 = GW_HWNDNEXT
    }

    /**
     * This is used to gather results of EnumWindows
     */
    private class WindowList implements WinUser.WNDENUMPROC
    {
        ArrayList<Window> list = new ArrayList<Window>();
        Pattern titlePattern = null;
        int processID;

        @Override
        public boolean callback(HWND hWnd, Pointer data)
        {
            Window w = new Window(hWnd);
            if (titlePattern == null || titlePattern.matcher(w.getTitle()).matches())
            {
                if (processID == 0 || processID == w.getProcessID())
                {
                    list.add(new Window(hWnd));
                    if (processID > 0)
                    {
                        return false; // if matching processID, only need one result
                    }
                }
            }
            return true; // keep going
        }

        // Convert the list into an ordinary array
        public Window[] toArray()
        {
            return list.toArray(new Window[list.size()]);
        }

    }

    public  Window[] getTopLevelWindows()
    {
        WindowList result = new WindowList();
        User32.INSTANCE.EnumWindows(result, null);
        return result.toArray();
    }

    public  Window[] getTopLevelWindows(String titleRegex)
    {
        WindowList result = new WindowList();
        result.titlePattern = Pattern.compile(titleRegex);
        User32.INSTANCE.EnumWindows(result, null);
        return result.toArray();
    }

    public Window[] getChildren()
    {
        WindowList result = new WindowList();
        User32.INSTANCE.EnumChildWindows(hWnd, result, null);
        return result.toArray();
    }

    public  Window getProcessWindow(int processID)
    {
        WindowList result = new WindowList();
        result.processID = processID;
        User32.INSTANCE.EnumWindows(result, null);
        return result.list.size() > 0 ? result.list.get(0) : null;
    }

    /**
     * Sleep for milliseconds.
     */
    public  void sleep(int milliseconds)
    {
        try
        {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private  long getProcessHandle(Process process)
    {
        String n = process.getClass().getName();
        if (n.equals("java.lang.ProcessImpl") || n.equals("java.lang.Win32Process"))
        {
            try
            {
                Field field = process.getClass().getDeclaredField("handle");
                field.setAccessible(true);
                return field.getLong(process);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public  int getProcessID(Process process)
    {
        long id = getProcessHandle(process);
        if (id != 0)
        {
            HANDLE handle = new HANDLE();
            handle.setPointer(Pointer.createConstant(id));
            return Kernel32.INSTANCE.GetProcessId(handle);
        }
        return 0;

    }

    /**
     * Runs a program and returns the process
     */
    public  Process runCommand(List<String> cmdline) throws IOException
    {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(cmdline).redirectErrorStream(true);
        return builder.start();
    }

    /**
     * Runs a program and returns the process
     */
    public  Process runCommand(String... cmdline) throws IOException
    {
        return runCommand(Arrays.asList(cmdline));
    }

    /**
     * Runs a program and returns the process id. Note: use runCommand instead of this.
     */
     /*
    public  int createProcess(String program, String args, String currentDirectory)
    {
        STARTUPINFO startupInfo = new STARTUPINFO(); // input
        PROCESS_INFORMATION processInformation = new PROCESS_INFORMATION(); // output
        String cmdline = (args == null || args.length() == 0) ? null : program + " " + args;
        boolean ok = Kernel32.INSTANCE.CreateProcess(program, cmdline, null, null, false, new DWORD(0), null, currentDirectory, startupInfo, processInformation);
        if (!ok) System.out.println("CreateProcess failed err="+Kernel32.INSTANCE.GetLastError());
        return processInformation.dwProcessId.intValue();
    }
*/
    /**
     * Note: use runCommand instead of this, if possible.
     * Use this to open a program or any kind of file, like pdf, jpeg, html, etc.
     * @return true on success.
     */
    public  boolean shellExecute(String filename, String args, String currentDirectory)
    {
        String verb = null; // possible values 'open' (or null), 'edit', 'print', etc.
        INT_PTR intPtr = Shell32.INSTANCE.ShellExecute(null, verb, filename, args, currentDirectory, 1); // 1=SW_SHOWNORMAL
        int rc = intPtr.intValue();
        if (rc <= 32) System.out.println("ShellExecute failed err="+rc+" for filename="+filename);
        return rc > 32;
    }

}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--hide-stop", "ArcadeMenu" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
