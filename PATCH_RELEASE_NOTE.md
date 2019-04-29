---
version: 7.1.1
module: https://talend.poolparty.biz/coretaxonomy/42
product:
- https://talend.poolparty.biz/coretaxonomy/183

---

# TPS-3084 <!-- mandatory -->

| Info             | Value |
| ---------------- | ---------------- |
| Patch Name       | Patch_20190429_TPS-3084_v1_7.1.1 |
| Release Date     | 2019-05-04 |
| Target Verson    | 20181026_1147-7.1.1 |
| Product affected | Talend Studio |

## Introduction <!-- mandatory -->

This patch is cumulative. It includes all previous generally available patches for Talend Studio 7.1.1.

**NOTE**: To download this patch, liaise with your Support contact at Talend.

## Fixed issues <!-- mandatory -->

This patch contains the following fixes:

- TPS-3084 [7.1.1]Null pointer exception when executing the jobs (TUP-22438)
- TPS-3074 [7.1.1]NullPointerException When you try to publish a job as docker (TUP-21983)
- TPS-3060 [7.1.1]Docker Image with tRestClient component (TESB-25045)
- TPS-2990 [7.1.1]Non-default Artifactory context path, not working (Artifact, shared libs, talend-update, Studio) (TUP-22182)
- TPS-3010 [7.1.1]Talend Studio - Spring 19 Patch (AccessToken Support) (TESB-24842)
- TPS-3038 [7.1.1]Unchecking 'Use Timestamp format for Date type' checkbox in tFileOutputParquet gives compilation error (TBD-8500)
- TPS-2950 [7.1.1]tMap losing all links in testcase when we do some modification in the main job in the same component (TUP-21252)
- TPS-3065 [7.1.1]Stackoverflow issue in studio (TUP-22425)
- TPS-3028 [7.1.1]Missing Signature in Business Model item => cannot export / import even in 7.1.1 (TUP-22347)
- TPS-2994 [7.1.1]Comment in the query in tJDBCInput component does not work if the job is migrated from 6.2.1 to 7.1.1 (TDI-41898)
- TPS-2921 [7.1.1]tElasticSearch components receiving nullpointer (TBD-8270)
- TPS-3042 [7.1.1]The exception NoSuchDatabaseException:Database"xxxx"not found (TBD-8077)
- TPS-2972 [7.1.1]Run Testcase instance fails : java.lang.NullPointerException (TUP-22015)
- TPS-2975 [7.1.1]Importing a job from 6.2.1 to 7.1.1 is throwing error (TDI-41876)
- TPS-2979 [7.1.1]Default endpoint URI not added to context variable endpoint in cRest (TESB-24920)
- TPS-2982 [7.1.1]Metadata information is not stored properly for Sysbase DB (TUP-22268)
- TPS-2966 [7.1.1]Problems handling SAP HANA Objects in Talend Studio (TUP-21999)
- TPS-2926 [7.1.1]Commit to Git frequently without any user action (TUP-21922)
- TPS-2978 [7.1.1]Intermittent "ClassNotFoundException: javax.mail.Address" errors (TESB-25164)
- TPS-2980 [7.1.1]Published routeName_jobName jar file with size zero in Nexus. (TESB-25130)
- TPS-2946 [7.1.1]Issue with cREST configured with resource class (TESB-25033)
- TPS-2888 [7.1.1]tDBConnection(Snowflake) issue within joblet(TUP-21105)
- TPS-2879 [7.1.1]OnSubJob Links Not Working(TUP-21333)
- TPS-2964 [7.1.1]Unexpected Empty Contexts Variable Values (TUP-22005)
- TPS-2925 [7.1.1]job run well on studio but failed after build (TUP-21871)
- TPS-2937 [7.1.1]Job migrated from 6.5.1 to 7.1.1 throwing UNEXPECTED_EXCEPTION (TDI-41803)
- TPS-2932 [7.1.1]Build job takes about 4 minutes while Run job takes > 40 minutes to start (TUP-21911)
- TPS-2909 [7.1.1]An error has occurred. UNEXPECTED_EXCEPTION when opening job migrated 6.4.1 to 7.1.1 (job using tJDBCInput) (TUP-21895)
- TPS-2904 [7.1.1]NPE when importing job
- TPS-2903 [7.1.1]Import 701 job get error 'Unknown value in the list / Value set not supported by the component' (TDI-41324)
- TPS-2874 [7.1.1]tImpalaConnection component not able to build libraries (TUP-21710)
- TPS-2897 [7.1.1]"talend-bigdata-launcher-1.2.0-20170410.jar" not found when spark job launched from standard job by jobserver (TBD-7933, TBD-8276)
- TPS-2783 [7.1.1]Add support for Elasticsearch 6.x - Spark Streaming (TBD-7700)
- TPS-2860 [7.1.1]Standard DI job which refers to big data batch job having S3 component and standard DI job having S3 connection component fails with an error. (TUP-21532)
- TPS-2848 [7.1.1]Error write data to S3 using the S3a file systsem in the spark job (TBD-8145)
- TPS-2833 [7.1.1]In studio Java Debugging of Routes fails (TUP-21005)
- TPS-2859 [7.1.1]Issue with POM/dependencies causing working jobs to suddenly fail with Could not find or load main class (TUP-21127)
- TPS-2811 [7.1.1]JDBC connection will not commit even if autocommit is set to True (TUP-21160)
- TPS-2782 [7.1.1]ESB - CI for ESB - Update 1 (TESB-24058)


## Prerequisites <!-- mandatory -->

Consider the following requirements for your system:

- Talend Studio 7.2.1 must be installed.

## Installation <!-- mandatory -->

- For publishing docker images in studio, please set maven online mode in Preferences and publish it once to download several missing jars/poms, then switch back to offline mode.

- To make TPS-2990 totally work with TAC, please install TPS-3085 also.

- For Cloud Studio
  - If you want to fetch license by cloud token 
    a) Get the bundles.info which is under the folder named "configuration/org.talend.configurator" from patch zip
    b) Replace the bundles.info from "{Studio_Home}/configuration/org.talend.configurator" with the bundles.info file from patch zip
  - If you want to use CI with Cloud token
    a) Get the file cloudpublisher-maven-plugin-7.1.1.jar which is under the folder named "repository/org/talend/ci/cloudpublisher-maven-plugin/7.1.1" from patch zip
    b) Copy the file cloudpublisher-maven-plugin-7.1.1.jar into Talend Studio in /configuration/.m2/repository/org/talend/ci/cloudpublisher-maven-plugin/7.1.1

<!--
- Detailed installation steps for the customer.
- If any files need to be backed up before installation, it should be mentioned in this section.
- Two scenarios need to be considered for the installation:
 1. The customer has not yet installed any patch before => provide instructions for this
 2. The customer had installed one previous cumulative patch => provide instructions for this
-->
### Installing the patch using Software update <!-- if applicable -->

1) Logon TAC and switch to Configuration->Software Update, then enter the correct values and save referring to the documentation: https://help.talend.com/reader/f7Em9WV_cPm2RRywucSN0Q/j9x5iXV~vyxMlUafnDejaQ

2) Switch to Software update page, where the new patch will be listed. The patch can be downloaded from here into the nexus repository.

3) On Studio Side: Logon Studio with remote mode, on the logon page the Update button is displayed: click this button to install the patch.

### Installing the patch using Talend Studio <!-- if applicable -->

1) Create a folder named "patches" under your studio installer directory and copy the patch .zip file to this folder.

2) Restart your studio: a window pops up, then click OK to install the patch, or restart the commandline and the patch will be installed automatically.

### Installing the patch using Commandline <!-- if applicable -->

Execute the following commands:

1. Talend-Studio-win-x86_64.exe -nosplash -application org.talend.commandline.CommandLine -consoleLog -data commandline-workspace startServer -p 8002 --talendDebug
2. initRemote {tac_url} -ul {TAC login username} -up {TAC login password}
3. checkAndUpdate -tu {TAC login username} -tup {TAC login password}

## Uninstallation <!-- if applicable -->

<!--
Detailed instructions to uninstall the patch

In case this patch cannot be uninstalled, it is your responsability to define the backup procedures for your organization before installing.

-->

## Affected files for this patch <!-- if applicable -->

The following files are installed by this patch:

- <File-1>  
- <File-2>
