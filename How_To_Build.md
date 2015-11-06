# Introduction #

The plugin uses a maven2 build, and needs installing IDEA and mks libraries in the local repository to build.


# Details #

first checkout the branch you want (/trunk for example), then you need to install in your local repository the following artifacts

| maven id | where to find it |
|:---------|:-----------------|
| com.mks:mkscmapi:8.3.2392 | in the /lib of the checked out branch |
| com.intellij.idea:openapi:${idea.version} | in IDEA's install folder |
| com.intellij.idea:annotations:${idea.version}  | in IDEA's install folder |
| com.intellij.idea:extensions:${idea.version}  | in IDEA's install folder |

7361 is the official 7.0 release, but you should be able to use any 7.0 eaps

The file install-idea-files.bat  found at the root of the branch can act as an example (for windows obviously)

run the mvn package comand in the sandbox

If you then want to install the built plugin in your idea's current plugins (not for debugging but for actual use), you then need to
  * delete ~/.IntelliJIdea70/config/plugins/mks-idea
  * extract the built mks-idea-dist.zip in ~/.IntelliJIdea70/config/plugins, this should recreate the mks-idea dir
  * start idea