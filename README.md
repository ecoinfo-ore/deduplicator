## Deduplicator

 >  ( Remove Duplicate Lines from multime Files - Using Gawk )

==

#### For Linux / Windows

==

```bash
   mvn clean install assembly:single
```

Example :

```bash

        java -DFromDirectory="Data"     \
             -DSizeFile=2000000         \
             -DToDirectory="Data/Uiniq" \
             -DExtension="*.txt"        \
             -jar deduplicator.jar

```
