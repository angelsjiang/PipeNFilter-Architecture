# PipeNFilter-Architecture

Systems Overlook:

    -------------------------->  Architecture of System A <-----------------------------

    [ SourceFilter ] ---> [ MiddleFilter ]  --->  [ SinkFilter ]

    
    Running Instructions:
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

    Running Instructions:
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

    Running Instructions:
        Go to System C directory
        To Compile:
            javac *.java
        To Run:
            java Plumber


    -------------------------->  End of Systems Overlook <-----------------------------

Note:

        - All the data files are hardcoded in the program. To change test input files, please go to:
            System A -> SourceFilter.java -> line 26
            System B ->  SourceFilter.java -> line 26
            System B -> SourceMergeFilter.java -> line 31 & 32

        - All outputs will be generated into "Outputs" directory under each system's directory
