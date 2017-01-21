# SDHelper
A 3DS hax install helper

# What is this?
This tool allows for automatic downloading of installer files. An XML file defines what to download and puts it all in one directory. This can be used for even quicker A9LH installations, as it would download all the files, all at once. All you would do is press start, then copy all the files to your SD card (theoretically). That's it.

# How To Use
* Put download.xml in the same directory as the jar file. Example in this repo (update/download.xml)
* Open jar
* Enter your 3DS info
* Press start
* All files will be in /SDHelper/sd

# Features
 * XML based download configurations
 * Auto-updating XMLs
 * Binary version control
 * If statements to get input from user. Includes:
  * Region
  * Version
  * Type (New/Old)
  
 * Support for downloading
  * Direct downloads
  * Zip files
  * 7zip files
  * Web links (Opens in default browser)
  
* Renaming of files downloaded
* Little error handling (Yay)

# Release Goals
* Error handling (cuz its important)
* GUI Enhancements
  * Allow for user input to be used for if statements
* Better if statements
  * Handle multiple regions in one statement

# Future Goals
* Even more GUI stuff!
  * XML requests user input and allows it to be used in if statements
  * XML picker (not hardcoded path)
  * Maybe progress bar be the total progress and not progress of current file?
  
* Naitive support for torrents
