---
version: 6.4.1
module: https://talend.poolparty.biz/coretaxonomy/42
product:
- https://talend.poolparty.biz/coretaxonomy/23
---

# TPS-3698

| Info             | Value |
| ---------------- | ---------------- |
| Patch Name       | Patch\_20200116\_TPS-3698\_v1-6.4.1 |
| Release Date     | 2020-01-16 |
| Target Version   | 20170623_1246-V6.4.1 |
| Product affected | Talend Studio |

## Introduction

This is a self-contained patch.

**NOTE**: For information on how to obtain this patch, reach out to your Support contact at Talend.

## Fixed issues
This patch contains this following fix:

- TPS-3698 [6.4.1]For tMicrosoftCRMXX (ODATA), it is unable to get/set data in the Edm.Date property type with the default date pattern(TDI-39572)

## Prerequisites
Consider the following requirements for your system:

- Talend Studio 6.4.1 must be installed.

- This patch is built based on TUP patch, **Patch_20190611_TPS-3149_v1-6.4.1**. Please make sure no any other patch is deployed after that. 


## Installation

**NOTE**: As this patch changed the DB mapping file, it can only be automatically available for the newly created project. So if the customer wants to propagate the changes to the all projects, especially for the projects created before the patch installation, the whole mappings folder in the path ({workspace}/{project\_name}/.settings/mappings) must be removed manually.

### Installing the patch using Software update

1) Logon TAC and switch to Configuration->Software Update, then enter the correct values and save referring to the documentation: https://help.talend.com/reader/f7Em9WV_cPm2RRywucSN0Q/j9x5iXV~vyxMlUafnDejaQ

2) Switch to Software update page, where the new patch will be listed. The patch can be downloaded from here into the nexus repository.

3) On Studio Side: Logon Studio with remote mode, on the logon page the Update button is displayed: click this button to install the patch.

### Installing the patch using Talend Studio

1) Create a folder named "patches" under your studio installer directory and copy the patch .zip file to this folder.

2) Restart your studio: a window pops up, then click OK to install the patch, or restart the commandline and the patch will be installed automatically.

### Installing the patch using Commandline

Execute the following commands:

1. Talend-Studio-win-x86_64.exe -nosplash -application org.talend.commandline.CommandLine -consoleLog -data commandline-workspace startServer -p 8002 --talendDebug
2. initRemote {tac_url} -ul {TAC login username} -up {TAC login password}
3. checkAndUpdate -tu {TAC login username} -tup {TAC login password}

## Uninstallation
Backup the Affected files list below. Uninstall the patch by restore the backup files.

## Affected files for this patch

The following files are installed by this patch:
- {Talend\_Studio\_path}/plugins/org.talend.core.runtime_6.4.1.20190610_1008-patch.jar
