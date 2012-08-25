#!/bin/bash 
# Script to make input for Mechanical Turk
# TWITTER version

# uncomment to make controls:
# javac MakeControls.java
# java MakeControls [controlfile]

javac MakeInput.java
java MakeInput twitter-hit-data arabic-twitter

# You should change this to a directory that you have
# then edit the .html file's img location to point to that
scp *.png sjeblee@ugradx.cs.jhu.edu:./public_html/img-twitter/

rm *.png
