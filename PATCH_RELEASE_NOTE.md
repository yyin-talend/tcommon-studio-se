---
version: 7.1.1
module: https://talend.poolparty.biz/coretaxonomy/42
product: https://talend.poolparty.biz/coretaxonomy/22
---
# TPS-3109
| Info             | Value |
| ---------------- | ---------------- |
| Patch Name       | Patch\_20190708\_TPS-3109\_v1|
| Release Date     | 2019-07-12 |
| Target Version   | 20181026\_1147-V7.1.1 |
| Product affected | Talend Studio |
## Introduction <!-- mandatory -->
This is a self-contained patch.
**NOTE**: For information on how to obtain this patch, reach out to your Support contact at Talend.
## Fixed issues <!-- mandatory -->
This patch contains the following fixes:
- TPS-3109 Backport tacokit upgrade to 1.1.9 in the 7.1.1 (TUP-22850)
## Prerequisites <!-- mandatory -->
Consider the following requirements for your system:
- Talend Studio 7.1.1 must be installed.
## Installation <!-- mandatory -->
### Installing the patch manually <!-- if applicable -->
1) Shut down Talend studio if it is opened.
2) Extract the patch zip.
3) Merge the folder "plugins" and its content to {studio}/plugins and overwrite the existing files.
4) Merge the folder "configuration" and its content to {studio}/configuration and overwrite the existing files.
5) Delete the folder "org.eclipse.osgi" and its content from {studio}/configuration.
6) Start the Talend studio.

