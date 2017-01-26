  
ArrayList slots;
boolean slotsInitialized = false;
int currentSelection = 0;
PFont orator;
void initializeSlots()
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

void handleSelection()
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

void drawLoadingMessage()
{
  fill(0,200);
  rectMode(CORNER);
  rect(0,0,width,height);
  fill(255);
  textAlign(CENTER,CENTER);
  text("LOADING...", width * 0.5, height * 0.5);
}

void newCenteredRoulette()
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

void displaySelector()
{
  fill(0,200);
  noStroke();
  rect(width * 0.5 - 300, 0, 600, height);
  textFont(font);
  
  fill(255);
  textAlign(CENTER,CENTER);
  text("<SELECT GAME>", width * 0.5, 75);
  for(int i = 0; i!= slots.size(); i++)
  {
    Slot current = (Slot) slots.get(i);
    current.update();
  }
}

boolean instructionsSelection = true;
void displayInstructions()
{
  GameData g = dataManager.getGameData(currentSelection );
  
  //fill(0,40);
  int margin = 100;
 // rect(margin+5, margin+5,width-margin * 2, height - margin * 2);
  
  fill(0,200);
  rect( width * 0.5 - 450,0, 900, height);
  
  float topTextHeight = margin + 40;
  
  textSize(40);
  fill(255);
  textAlign(CENTER,TOP);
  text(g._name, width * 0.5, topTextHeight);
  
  textSize(30);
  text(g._designers, width * 0.5,topTextHeight + 40);
  
  textAlign(CENTER,TOP);
 //textSize(20);
 // text(g._description, width * 0.5,topTextHeight + 90);
  
  textAlign(CENTER,CENTER);
  if(instructionsSelection)
    fill(0,255,255);
  else
    fill(150);
  
  text("PLAY", width * 0.5, height - margin - 100);
  
  if(!instructionsSelection)
    fill(0,255,255);
  else
    fill(150);
  text("BACK", width * 0.5, height - margin - 50);
  imageMode(CENTER);
  image(g._instructionsImage, width * 0.5, height * 0.5);
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
  
  void update()
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
  
  void getTargetY()
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
