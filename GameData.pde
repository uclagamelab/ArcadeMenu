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
  
  boolean findJSON(File gameDirectory)
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
  
  void loadImages()
  {
    //load the images from the addresses found.
    if(_imageFiles == null) return;
        
    _images = new PImage[_imageFiles.length];
    
    for(int i = 0; i!= _imageFiles.length; i++)
      _images[i] = loadImage(_imageFiles[i].getAbsolutePath());
      
  }
  
  void loadVideo(PApplet parent, File[] files)
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
  
  void kill() {
    if(gameIsRunning)
    {
      try {
        System.out.println("killing.....");
        loop();
          
          //kill game
         Runtime.getRuntime().exec("taskkill /F /IM "+_executable.getName());
         System.out.println("Program should now be closed.");
        
        //switch joy2key to the menu keybindings
        Runtime.getRuntime().exec(joy2keyPath + " menuselect.cfg");
          gameIsRunning = false;
      } catch (Exception ex) {
        System.out.print("???? : ");
         ex.printStackTrace();
      } 
     
    }
  }
  
  void listVideos()
  {
    println("videos: " + _videoFiles.length);
    for(File vid : _videoFiles)
    {
      println("  " + vid.getName());
    }
  }
  
  void listImages()
  {
     println("images: " + _imageFiles.length);
        for(File img : _imageFiles)
        {
          println("  " + img.getName());
        }
  }
}

String stripExtension(String str)
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
  boolean accept(File dir, String name) {
    return name.toLowerCase().endsWith(".json");
  }
};

java.io.FilenameFilter imageFilter = new java.io.FilenameFilter() 
{
  boolean accept(File dir, String name)
  {
    return (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".png"));
  }
};

java.io.FilenameFilter videoFilter = new java.io.FilenameFilter() 
{
  boolean accept(File dir, String name)
  {
    return (name.toLowerCase().endsWith(".mov") || name.toLowerCase().endsWith(".mp4") || name.toLowerCase().endsWith(".m4v") );
  }
};

//This is needed for the video stuff.
void movieEvent(Movie m) 
{
  m.read();
}