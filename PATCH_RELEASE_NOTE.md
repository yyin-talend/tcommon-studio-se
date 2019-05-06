---
version: 6.4.1
module: https://talend.poolparty.biz/coretaxonomy/42
product:
- https://talend.poolparty.biz/coretaxonomy/16
- https://talend.poolparty.biz/coretaxonomy/17
- https://talend.poolparty.biz/coretaxonomy/18
---

# TPS-3062

| Info             | Value |
| ---------------- | ---------------- |
| Patch Name       | Patch_20190506_TPS-3062_v1-6.4.1 |
| Release Date     | 2019-05-10 |
| Target Version    | 20170623_1246-V6.4.1 |
| Product affected | Talend Studio |

## Introduction

This patch is cumulative.
It includes all previous generally available patches for Talend Studio 6.4.1.

**NOTE**: To download this patch, liaise with your Support contact at Talend.

## Fixed issues

This patch is cumulative and contains the following fixes:

- TPS-3062 [6.4.1] tFileInputDelimited on spark job - Column(s) missing issue with field separator "ò" (TBD-7687)

The patch also includes the following patches:

- TPS-2051 [6.4.1]Remote run in studio fails with file not found exception(TUP-18118)
- TPS-2056 [6.4.1]PATCH broke Build Job with sub-jobs from Reference project (TUP-18276)
- TPS-2108 [6.4.1]tDataMasking code cannot generate(TDQ-14231)
- TPS-2125 [6.4.1]Remote job server execution fails with child job (TUP-18448)
- TPS-2111 [6.4.1]Cannot get globalMap after tFlowToIterate (TUP-18388)
- TPS-2154 [6.4.1]IndexOutOfBoundsException happen after migrated to 6.4.1 (TDQ-14269)
- TPS-2149 [6.4.1]Could not find or load main class exception after building with CI Builder(TUP-18278)
- TPS-2135 [6.4.1]When Deploying one customer's job in CI, the build failed with error  constant string too long (TDI-39029)
- TPS-2148 [6.4.1]AMC Main Chart Displays The Wrong Execution Times (TUP-18497)
- TPS-2153 [6.4.1]Add commons-codec to HDP 2.6 HDFS libraries (TBD-5535)
- TPS-2167 [6.4.1]tXMLMap miss some fields while import schema from repository (TUP-18382)
- TPS-2142 [6.4.1]tAS400Input - Guess Schema on tAS400Input fails with MissingDriverException (TUP-18453,TUP-18774)
- TPS-2217 [6.4.1]Can't initialize the datamart if service name is not same with sid of oracle 12 (TDQ-14384)
- TPS-2171 [6.4.1]Complete URL Pop-up with LDAP SSH Git Authentication (TUP-18466)
- TPS-2223 [6.4.1]Username and passwords to remote job servers stored in cleartext in Studio (TUP-18741)
- TPS-2247 [6.4.1]Cannot use name "oracle" for row name (Main) (TUP-18825)
- TPS-2219 [6.4.1]Issues with Hyphen in ServiceName(TESB-20417)
- TPS-2242 [6.4.1]tELTOracleMap / ELT Oracle Map Editor / left panel empty after migration from 6.1.1 to 6.3.1(TUP-18158)
- TPS-2234 [6.4.1]Studio sending multiple "getLibLocation" metaservlet calls for each build activity (TUP-18208)
- TPS-2218 [6.4.1]Testcase for tWriteJSONFiled gives junitGlobalMap cannot be resolved exception (TUP-18217)
- TPS-2264 [6.4.1]Jobscript generated shows empty with proper job design (TUP-19001)
- TPS-2250 [6.4.1]Hebrew in tSandardizeRow (TDQ-14435)
- TPS-2284 [6.4.1]Comparison in studio doesnt work for tXMLMap (TUP-19067)
- TPS-2179 [6.4.1]Get error when build big data batch job (TBD-5390)
- TPS-2321 [6.4.1]Offline SVN project changes not committed to remote project (TUP-18380)
- TPS-2077 [6.4.1]Spark Streaming hive job when migrated from 6.3 to 6.4 cannot compile (TBD-5427,TBD-5476)
- TPS-2198 [6.4.1]tFileOutputParquet doesn't handle null values(TBD-5681)
- TPS-2354 [6.4.1]MapR 52 Spark Streaming job with tMapRStreamOutput components cannot generate code (TBD-5521)
- TPS-2346 [6.4.1]BASE64Decoder and BASE64Encoder should be replaced by other class(TDQ-14095)
- TPS-2342 [6.4.1]Implicit context load behavior change (TUP-19342)
- TPS-2384 [6.4.1]Custom GroupId causing "cannot be resolved to a type" errors (TUP-18769)
- TPS-2393 [6.4.1]Child jobs are not being generated via itemfilter in CI Builder (TUP-19756)
- TPS-2324 [6.4.1]Cannot connect from studio to TAC via proxy server with basic authentication (TUP-19270)
- TPS-2408 [6.4.1]with git project the Studio Connection windows does not behave consistently (TUP-18674)
- TPS-2424 [6.4.1]After sending a commit the dialog "Finishing integrity check" never ends (TUP-18215)
- TPS-2427 [6.4.1]Deactivating components in a joblet results in a job that won't run (TUP-19875)
- TPS-2445 [6.4.1]tELTMSSqlInput component does not support the schema with a DB-column name having Degree symbol in it (TUP-19961)
- TPS-2452 [6.4.1]Error  Could not find or load main class / Remote Execution / routines.jar missing in ZIP file (TUP-19806) (TUP-19806)
- TPS-2476 [6.4.1]Datamart migration task "Update context information for TdqVAnalyizedElement and TdqVLastAnalyzedElement" failed for oracle and postgresql (TDQ-14224)
- TPS-2400 [6.4.1]Studio crashing during startup with Failed to load the service  org.ops4j.pax.url.mvn.MavenResolver (TUP-17411)
- TPS-2497 [6.4.1]"Browse Reports" issue on the component tDQReportRun (TDQ-15036)
- TPS-2530 [6.4.1]"Build Job" for Report does not prompt at all, only Empty Job (TDQ-15320)
- TPS-2505 [6.4.1]tSqoopImport with Parquet File Format Fails Against Kerberized Ha HDP 2.6 Cluster (TBD-6877)
- TPS-2547 [6.4.1]Wrong branch value in the jobs generated by CI (TUP-20223)
- TPS-2567 [6.4.1]Preview in Metadata Delimited File fails with NullPointerException (TUP-20267)
- TPS-2572 [6.4.1]IllegalArgumentException when Running a Spark Job Containing two tRuleSurvivorship Components (TDQ-15484,TDQ-14308,TBD-7049)
- TPS-2565 [6.4.1]STUDIO - cTalendJob component uses context parameters defined in another cTalendJob component(TESB-19834)
- TPS-2574 [6.4.1]Unable to create a tag (Cloud) (TUP-20251)
- TPS-2570 [6.4.1]Compile error after refactoring job (TDQ-15455)
- TPS-2587 [6.4.1]Change REST API Swagger documentation based on Talend job settings (TESB-21643)
- TPS-2268 [6.4.1]Talend Studio  Import org.apache.camel not resolved in custom beans (TESB-20667, TESB-23071)
- TPS-2665 [6.4.1]tRuleSurvivorship places all data into a single Spark partition (TDQ-15756)
- TPS-2694 [6.4.1]Can't save the URL string in report editor (TDQ-15840)
- TPS-2711 [6.4.1]cKafka code generation fails to compile with SSL Truststore Password (TESB-22755)
- TPS-2758 [6.4.1]Unable to use a Custom Hadoop metadata connection in Spark configuration in a Spark job (TUP-20816)
- TPS-2774 [6.4.1]Studio Crash using tmap component after McOS Mojave Upgrade (TUP-20866)
- TPS-2796 [6.4.1]Only job artifacts must be deployed (TUP-21110)
- TPS-2838 [6.4.1]Context variable passed from route to routelet is not working  as expected (TESB-24306)
- TPS-2863 [6.4.1][Spark]Compile error on tUnite (TBD-6792)
- TPS-2939 [6.4.1]Studio/TAC Performance Degradation (TUP-21985)
- TPS-2889 [6.4.1]NullPointerException when calling SOAP Service with empty body (TESB-24761)
- TPS-2902 [6.4.1]Child jobs are not being generated via itemfilter in CI Builder (TUP-19756)
- TPS-3020 [6.4.1]Performing HDFS operation after a tDataPrepRun (TUP-17688)


## Prerequisites

Consider the following requirements for your system:

- Talend Studio 6.4.1 must be installed.

## Installation

<!--
Detailed installation steps need to be documented for customer.
If any files need to be backed up before installation, it should be mentioned in this section.

Two scenarios need to be considered for the installation:
* The customer has not yet installed any patch before
* The customer had installed one previous cumulative patch

-->

### Installing the patch using Software update

1. Logon TAC and switch to Configuration->Software Update, enter the correct values and save referring to the documentation: https://help.talend.com/reader/f7Em9WV_cPm2RRywucSN0Q/j9x5iXV~vyxMlUafnDeja
2. Switch to Software update page, where the new patch will be listed. The patch can be downloaded from here into the nexus repository.
3. On Studio Side: Logon Studio with remote mode, on the logon page the Update button is displayed, click this button to install the patch.

### Installing the patch using Talend Studio

1. Create folder which named "patches" under your studio installer directory and copy patch .zip file to this folder.
2. Restart your studio,will popup up prompt window at first time and then click ok button to install the patch, or restart commandline, patch will be installed automatically.

The following files are installed by this patch: <!-- if applicable -->

- 'org.talend.librariesmanager_6.4.1.20190506_1208-patch.jar'
