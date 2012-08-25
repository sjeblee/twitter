#!/usr/bin/env sh
# TWITTER VERSION
#
# Copyright 2008 Amazon Technologies, Inc.
# 
# Licensed under the Amazon Software License (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
# 
# http://aws.amazon.com/asl
# 
# This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
# OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and
# limitations under the License.
 
projectname="arabic-twitter"
cd ../..
cd bin
echo "[Results] Running getResults.sh"
./getResults.sh $1 $2 $3 $4 $5 $6 $7 $8 $9 -successfile ../samples/twitter_class/"$projectname".success -outputfile ../samples/twitter_class/"$projectname".results
cd ..
cd samples/twitter_class
javac ProcessResults.java
echo "[Results] Processing results..."
java ProcessResults "$projectname".input "$projectname".results "$projectname".ann > results-"$projectname".log
echo "[Results] Merging sentences..."
javac Merge.java
java Merge "$projectname".ann
javac CountAnns.java
java CountAnns "$projectname".ann
#cd ../..
#cd bin
echo "[Results] Auto-approval turned off. Please run [ ./approveWork.sh -approvefile ../samples/external_hit_2/approve.txt ] from bin"
#echo "[Results] Running approveWork.sh"
#./approveWork.sh -approvefile ../samples/twitter_class/approve.txt
#cd ../samples/twitter_class
#echo "[Results] NOTE: The specified assignments have been approved. Rejections must be done manually. Please see reject.txt and review the assignments carefully before rejecting them."
echo "[Results] Finished."

