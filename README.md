# PipeNFilter-Architecture


    -------------------------->  Architecture of System A <-----------------------------

    [ SourceFilter ] ---> [ MiddleFilter ]  --->  [ SinkFilter ]

    
    Go to System A directory
    To Compile:
        javac *.java
    To Run:
        java Plumber


    -------------------------->  Architecture of System B <-----------------------------


    [ SourceFilter ] ---> [ MiddleFilter ]  --->  [ SinkFilter ]
                                |                       |
                        Filter out altitude        Print all data
                        wild point data          indicating modified
                                |                   wild points
                                |                       |
                                |                       |
                        WildPoint.csv               OutputB.csv

    Go to System B directory
    To Compile:
        javac *.java
    To Run:
        java Plumber
        

    -------------------------->  Architecture of System C <-----------------------------


    [ InputStream A ] 
                    \
                     \
                     [ MergeFilter ] --->  [ WildPresureFilter ]  --->  [ WildPitchPressureFilter ] --->  [ SinkFilter ]
                     /                                   |                       |                                  |
                    /                                    |                       |                                  |
    [ InputStream B ]                                    |                       |                                  |
                                                    Filter out               Filter out                             |
                                                  pressure < 45psi          pressure > 65                     OutputC.csv
                                                OR pressure > 90psi        AND pitch > 10
                                                  wild point data          indicating modified
                                                         |                   wild points
                                                         |                       |
                                                         |                       |
                                                WildPressureData.csv     WildPitchPressureData.csv


    Go to System B directory
    To Compile:
        javac *.java
    To Run:
        java Plumber
