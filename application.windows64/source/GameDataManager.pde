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
        fill(#ff00ff);
        rect(x,y,w,h);
        fill(#ffffff);
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