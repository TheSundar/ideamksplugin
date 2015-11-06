
```
si locks --usage
Usage: si locks options...; options are:
        --fields=field1[:width1],field2[:width2]... where fieldn can be any of: archive,cpid,devpath,host,member,project,revision,sandbox,timestamp,user
        --height=value  The height in pixels of the windows
        --locker=value  The user whose locks are to be retrieved.
        --width=value  The width in pixels of the windows
        -x value  The x location in pixels of the window
        -y value  The y location in pixels of the window
        -?  Shows the usage for a command
        -F value  Read the selection from a specified file
        -N  Responds to all confirmations with "no"
        -Y  Responds to all confirmations with "yes"
        --[no]batch  Control batch mode (no user interaction in batch mode)
        --cwd=value  Act as if command executed in specified directory
        --forceConfirm=[yes|no]  Specify an answer to all confirmation questions
        -g  User interaction should happen via the GUI
        --gui  User interaction should happen via the GUI
        --hostname=value  Hostname of server
        --no  Responds to all confirmations with "no"
        --password=value  Credentials (e.g., password) to login with
        --[no]persist  Control persistence of CLI views
        --port=value  TCP/IP port number of server
        --quiet  Control status display
        --selectionFile=value  Read the selection from a specified file
        --settingsUI=[gui|default]  Control UI for command options
        --status=[none|gui|default]  Control status display
        --usage  Shows the usage for a command
        --user=value  Username to login to server with
        --yes  Responds to all confirmations with "yes"
```

Example output
```
si locks --fields=cpid,revision,sandbox --locker=79310750
92136:1 1.1     subsanbdox/project.pj
92136:1 1.5     subsanbdox/project.pj
92136:1 1.5     subsanbdox/project.pj
```