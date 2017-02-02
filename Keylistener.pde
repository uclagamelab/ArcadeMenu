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


void manageTopWindow()
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

void ensureGameIsOnTop()
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

void ensureMenuIsOnTop()
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
void keyPressed() 
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


void launchApplication()
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

void initGlobalKeyListener()
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