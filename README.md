##Game Arcade by UCLA Game Lab

#### We are currently working on making the whole project open so that you can build your own custom arcade interface. In the meantime you can use our builds and add your own games.

##### This project was originally developed to be used with the [UCLA Game Lab Arcade Backpack](http://games.ucla.edu/game/ucla-game-lab-arcade-backpack/)

Download the application folder for the operating system that you are running. (64 or 32-bit)

Inside the application folder you will find a "data" folder. Navigate into this folder and then into the "folder".

You should now be in "data/games" and you should see two folders that have example games. One was made in Unity and the other was made in Processing.

Inside of the folder four each game there are four items. Three are folders that lead to "image", "instructions", and "windows". The fourth is a file called "gameInfo.json".

You can edit "gameInfo.json" with any standard text editor. Inside of that file you should see text that is in this format

```
{
	"name": "Spinning Cube",
	"designers": "UCLA GameLab",
	"description": "a spinning cube!",
	"command arguments":"-popupwindow -screen-width 1920 -screen-height 1080"
}
```

You can edit this to match your own game. Just placed the relevant info between the quotes to the right of the colon. For example if I made a game about writing readme's I might change the above to - -

```
{
	"name": "ReadMe Writing Hero",
	"designers": "Shadowy GameLab Member",
	"description": "These readme's won't write themselves!",
	"command arguments":"-popupwindow -screen-width 1920 -screen-height 1080"
}
```

That last value is reserved for command line arguments for launching your game. Many games may not need these so you can empty this value out. You only need to put flags here if you use them to make sure your game launches properly.

Now, the "image" folder is where the menu background for your game lives. You can put one or two .png files here to use as the menu background for your game. Note that with the current theme, if you put more than one image in this folder the images will animate back and forth in the menu.

The "instructions" folder holds a .png file that can be used as control instructions for your game. Give the .png a transparent background for the best look. There is an example instructions image in the included demos.

The "windows" folder is where all of the magic happens! This is where you put the executable for your game, along with any necessary data folders.

Now you can add your own games to the arcade! :tada: :tada:

###Extra Setup Notes
You must add some files to the 'libraries' in your sketch directory
Currently, those aren't included in the repo in the correct way, it's easiest to manually copy them from a working environment.
todo: bundle those with the repo
todo: figure out how much you can take away and still have it work.
todo: find link

Also, it seems only to work with a 32 bit version of processing...
I think I got it partially working on a 64bit, but it was an issue
with JIntellitype. (maybe need to replace that jar/dll?)