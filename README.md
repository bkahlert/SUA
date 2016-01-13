# API Usability Analyzer

This tool can be used to qualitatively analyze the API of software fragments (especially software libraries and frameworks).
It has been implemented to [research the API usability of the C++ based software library](https://github.com/bkahlert/seqan-thesis) [SeqAn](http://www.seqan.de) - a library used by bioinformaticists to solve sequence analysis problems.

This qualitative analysis tool is a modular Eclipse RCP based application.
It supports the analysis using the Grounded Theory Methodology (GTM) of data captured alongside programming sessions. 

APIUA currently supports the following data collected during or with respect to programming sessions:
- interviews
- questionnaires
- changes the user made to his code between two moments in time (diff files)
- broad range of events that occur on instrumented web pages like online documentations (doclog files) - examples: opening, resizing or scrolling on a web page

APIUA is accompagnied by a [data collection server](https://github.com/bkahlert/api-usability-analyzer-server-java-ee) based on Java EE. It collects the changes made to file and various actions done on observed web pages.
There is also a [Python based data collection client](https://github.com/bkahlert/api-usability-analyzer-client-python) that checks for changed user file to transmit to the server.

APIUA strengths are:
- support of the GTM elements "open coding", "axial coding", "memos" and "constant comparison"
- entity detection, e.g. each person's actions on the observed web pages can be matched with the respective user observed during a programming session
- joint visualization of the different types of data, e.g. all data are put on a timeline; actions on the web page are rendered to see what the user actually saw in each moment, ... 
- modular architecture that makes APIUA easily extendable

## Screenshots

![Splash Screen](images/logo.jpg)
Splash Screen

![Open Coding Perspective](images/open-coding.png)
Open Coding Perspective

![Axial Coding Perspective](images/axial-coding.png)
Axial Coding Perspective

![Open Coding Perspective](images/codes.png)
Color Coded Hierarchical Code Structures

![Axial Coding Perspective](images/properties.png)
Dimension and Property Support


## Installation

The analyzer is currently pretty much alpha since it has only been tested in the scope of the creator's research.  
If you still want to try it out you need to:

1. Download Eclipse Java Edition
1. Add the following third-party projects to your workspace
   - https://github.com/bkahlert/com.bkahlert.nebula
   - org.eclipse.nebula.cwt (available on http://www.eclipse.org/nebula/downloads.php)
   - org.eclipse.nebula.widgets.cdatetime (available on http://www.eclipse.org/nebula/downloads.php)
1. Checkout and add all projects available on https://github.com/bkahlert/api-usability-analyzer to your workspace
1. Run the file APIUA.product contained in the project de.fu_berlin.imp.apiua. 

## Research Project

This project is part of the [BioStore project](http://www.seqan-biostore.de/wp/) sponsored by the [Federal Ministry of Education and Research, Germany](http://www.bmbf.de).

<table style="border-collapse: collapse; border: none; margin: 15px auto;">
    <tr>
        <td style="padding: 15px;"><a href="http://www.seqan.de"><img src="http://www.seqan-biostore.de/wp/wp-content/uploads/2012/01/seqan_logo_115x76.png"></a></td>
        <td style="padding: 15px;"><a href="http://www.fu-berlin.de"><img src="http://www.seqan-biostore.de/wp/wp-content/uploads/2012/02/fu_logo.gif"></a></td>
        <td style="padding: 15px;"><a href="https://research.nvidia.com/content/fuberlin-crc-summary" ><img src="http://www.seqan-biostore.de/wp/wp-content/uploads/2013/11/NV_CUDA_Research_Center_3D_small.png" width="63" height="76"></a></td>
        <td style="padding: 15px;"><a href="http://bmbf.de/" style="margin-left: 15px;"><img src="http://www.seqan-biostore.de/wp/wp-content/uploads/2011/09/BMBF_CMYK_Gef_150_e.png" width="100" height="76"></a></td>
    </tr>
</table>

## Further reading

1. [Eclipse Rich Client Platform](http://wiki.eclipse.org/index.php/Rich_Client_Platform)
2. [Nebula Widgets for Eclipse](https://github.com/bkahlert/com.bkahlert.nebula)
3. [API Usability Analyzer Server for Java EE](https://github.com/bkahlert/api-usability-analyzer-server-java-ee)
4. [Python based Data Collection Client](https://github.com/bkahlert/api-usability-analyzer-client-python)
5. [Research results of the usability analysis of SeqAn's API](https://github.com/bkahlert/seqan-research)

## License

[The MIT License (MIT)](../../LICENCE)  
Copyright (c) 2011-2014 [Björn Kahlert, Freie Universität Berlin](http://www.mi.fu-berlin.de/w/Main/BjoernKahlert)
