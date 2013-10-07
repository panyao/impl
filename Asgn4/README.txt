The .zargo file is a class diagram that can be viewed with the ArgoUML program.
The other file is the literal text to past into entry at http://www.websequencediagrams.com/ for the sequence diagram.  Sorry. :(

This is an overview of how the Zygote process starts up in Android.  It is preinitialized with a bunch of Android-relevant classes and resources, and then listens for connections on a local socket to read startup instructions from.  Then, it forks and specializes the child process accordingly.
- AUstin Hanlin