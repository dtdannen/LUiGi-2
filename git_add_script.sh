# git_add_script.sh
#
# Author: Dustin Dannenhauer
#
# This file runs a series of add and rm commands ensuring that any new files
# are automatically added, and that certain files never get added. Basically
# I use this script to add every file, and then remove the ones that I will
# always remove. Note: I am only removing them from my git repo, not locally.

# first copy runtime files via the cp_runtime_files_script
./cp_runtime_files_script.sh

# also, go ahead and delete all game state files
rm core/StarcraftBot/GameStateOutput/*

# now add all files
git add --all
#git add core/OntGDA/src
#git add runtime/*
git add cp_runtime_files_script.sh
git add git_add_script.sh # can I git add myself?

# remove .class files
git rm -r --cached core/OntGDA/bin/
git rm -r --cached core/OntGDA/lib/pellet-2.3.0/*

# now remove unnecessary files
git rm --cached core/StarcraftBot/BWSAL_0.9.12/Debug/*
git rm --cached core/StarcraftBot/BWSAL_0.9.12/HierarchicalGDABot.sdf
git rm --cached core/OtherBots/DefendBaseBot/DefendBaseBot.sdf
git rm --cached core/OtherBots/DefendBaseBot/RushBot.sdf
#git rm --cached core/StarcraftBot/GameStateOutput/*
#clear
echo "--- finished adding files ---"
