API Usability Analyzer
======================

This tool can be used to qualitatively analyze the API of software fragments (especially software libraries and frameworks).
It has been implemented to research the API usability of the C++ based software library [SeqAn](http://www.seqan.de) - a library used by bioinformaticists to solve sequence analysis problems.

This qualitative analysis tool is a modular Eclipse RCP based application.
It supports the analysis using the Grounded Theory Methodology (GTM) of data captured alongside programming sessions. 

SUA currently supports the following data collected during or with respect to programming sessions:
- interviews
- questionnaires
- changes the user made to his code between two moments in time (diff files)
- broad range of events that occur on instrumented web pages like online documentations (doclog files) - examples: opening, resizing or scrolling on a web page

SUA is accompagnied by a [data collection server](http://github.com/bkahlert/api-usability-analyzer-server) based on Java EE.
There is also a [data collection client for CMake](http://github.com/bkahlert/api-usability-analyzer-cmake-client) that generically modifies the created build files to transparently enable the data collection.
The data collection circumvents various problems that arise from browser's security measurements.

SUA strengths are:
- support of the GTM elements "open coding", "axial coding", "memos" and "constant comparison"
- entity detection, e.g. each person's actions on the observed web pages can be matched with the respective user observed during a programming session
- joint visualization of the different types of data, e.g. all data are put on a timeline; actions on the web page are rendered to see what the user actually saw in each moment, ... 
- modular architecture that makes SUA easily extendable

Installation
------------

The analyzer is currently pretty much alpha since it has only been tested in the scope of the creator's research.  
If you still want to try it out you need to:
1. Download Eclipse Java Edition
2. Add the following third-party projects to your workspace
  - https://github.com/bkahlert/com.bkahlert.nebula
  - org.eclipse.nebula.cwt (available on http://www.eclipse.org/nebula/downloads.php)
  - org.eclipse.nebula.widgets.cdatetime (available on http://www.eclipse.org/nebula/downloads.php)
3. Checkout and add all projects available on https://github.com/bkahlert/api-usability-analyzer to your workspace
4. Run the file APIUA.product contained in the project de.fu_berlin.imp.apiua. 


Research Project
----------------

This project is part of the [BioStore project](http://www.seqan-biostore.de/wp/) sponsored by the [Federal Ministry of Education and Research, Germany](http://www.bmbf.de).

<table style="border-collapse: collapse; border: none; margin: 15px auto;">
    <tr>
        <td style="padding: 15px;"><a href="http://www.seqan.de"><img src="http://www.seqan-biostore.de/wp/wp-content/uploads/2012/01/seqan_logo_115x76.png"></a></td>
        <td style="padding: 15px;"><a href="http://www.fu-berlin.de"><img src="http://www.seqan-biostore.de/wp/wp-content/uploads/2012/02/fu_logo.gif"></a></td>
        <td style="padding: 15px;"><a href="https://research.nvidia.com/content/fuberlin-crc-summary" ><img src="http://www.seqan-biostore.de/wp/wp-content/uploads/2013/11/NV_CUDA_Research_Center_3D_small.png" width="63" height="76"></a></td>
        <td style="padding: 15px;"><a href="http://bmbf.de/" style="margin-left: 15px;"><img src="http://www.seqan-biostore.de/wp/wp-content/uploads/2011/09/BMBF_CMYK_Gef_150_e.png" width="100" height="76"></a></td>
    </tr>
</table>

License
-------

[The MIT License (MIT)](../../LICENCE)  
Copyright (c) 2011-2014 [Björn Kahlert, Freie Universität Berlin](http://www.mi.fu-berlin.de/w/Main/BjoernKahlert)