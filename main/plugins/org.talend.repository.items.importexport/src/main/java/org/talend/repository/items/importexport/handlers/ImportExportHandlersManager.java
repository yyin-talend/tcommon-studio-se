// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.items.importexport.handlers;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Priority;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.osgi.framework.FrameworkUtil;
import org.talend.commons.CommonsPlugin;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.time.TimeMeasure;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.properties.FolderItem;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.ItemState;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.helper.ByteArrayResource;
import org.talend.core.model.relationship.RelationshipItemBuilder;
import org.talend.core.model.repository.DynaEnum;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.ui.IJobletProviderService;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;
import org.talend.repository.documentation.ERepositoryActionName;
import org.talend.repository.items.importexport.handlers.imports.IImportItemsHandler;
import org.talend.repository.items.importexport.handlers.imports.IImportResourcesHandler;
import org.talend.repository.items.importexport.handlers.imports.ImportBasicHandler;
import org.talend.repository.items.importexport.handlers.imports.ImportCacheHelper;
import org.talend.repository.items.importexport.handlers.imports.ImportExportHandlersRegistryReader;
import org.talend.repository.items.importexport.handlers.model.EmptyFolderImportItem;
import org.talend.repository.items.importexport.handlers.model.ImportItem;
import org.talend.repository.items.importexport.handlers.model.ImportItem.State;
import org.talend.repository.items.importexport.i18n.Messages;
import org.talend.repository.items.importexport.manager.ChangeIdManager;
import org.talend.repository.items.importexport.manager.ResourcesManager;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.RepositoryConstants;
import org.talend.utils.io.FilesUtils;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ImportExportHandlersManager {

    private final ImportExportHandlersRegistryReader registryReader;

    private IImportItemsHandler[] importHandlers;

    private IImportResourcesHandler[] resImportHandlers;

    private ChangeIdManager changeIdManager = new ChangeIdManager();

    public ImportExportHandlersManager() {
        registryReader = ImportExportHandlersRegistryReader.getInstance();
    }

    public IImportItemsHandler[] getImportHandlers() {
        if (importHandlers == null) {
            importHandlers = registryReader.getImportHandlers();
        }
        return importHandlers;
    }

    public IImportResourcesHandler[] getResourceImportHandlers() {
        if (resImportHandlers == null) {
            resImportHandlers = registryReader.getImportResourcesHandlers();
        }
        return resImportHandlers;
    }

    private IImportItemsHandler findValidImportHandler(ResourcesManager resManager, IPath path, boolean enableProductChecking) {
        for (IImportItemsHandler handler : getImportHandlers()) {
            handler.setEnableProductChecking(enableProductChecking);
            if (handler.valid(resManager, path)) {
                return handler;
            }
        }
        // the path is not valid in current product, so ignore to import
        return null;
    }

    private IImportItemsHandler findValidImportHandler(ResourcesManager resManager, IPath path, ImportItem importItem,
            boolean enableProductChecking, boolean checkBuiltIn) {
        for (IImportItemsHandler handler : getImportHandlers()) {
            handler.setEnableProductChecking(enableProductChecking);
            boolean isValid = handler.valid(importItem);
            if (!isValid && !checkBuiltIn) {
                // if don't care builtin/system item, then just use this value
                isValid = handler.isValidSystemItem(importItem);
                if (isValid) {
                    importItem.setSystemItem(true);
                }
            }
            if (isValid) {
                // set the handler
                importItem.setImportHandler(handler);
                return handler;
            }
        }
        // the item is not valid in current product, so ignore to import
        return null;
    }

    public List<ImportItem> populateImportingItems(ResourcesManager resManager, boolean overwrite,
            IProgressMonitor progressMonitor) throws Exception {
        // by default don't check the product.
        return populateImportingItems(resManager, overwrite, progressMonitor, false);
    }

    public List<ImportItem> populateImportingItems(ResourcesManager resManager, boolean overwrite,
            IProgressMonitor progressMonitor, boolean enableProductChecking) throws Exception {
        return populateImportingItems(resManager, overwrite, progressMonitor, enableProductChecking, true);
    }

    public List<ImportItem> populateImportingItems(ResourcesManager resManager, boolean overwrite,
            IProgressMonitor progressMonitor, boolean enableProductChecking, boolean needCheck) throws Exception {
        IProgressMonitor monitor = progressMonitor;
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        TimeMeasure.display = CommonsPlugin.isDebugMode();
        TimeMeasure.displaySteps = CommonsPlugin.isDebugMode();
        TimeMeasure.measureActive = CommonsPlugin.isDebugMode();
        TimeMeasure.begin("populateItems"); //$NON-NLS-1$

        try {
            // pre populate
            prePopulate(monitor, resManager);

            ImportCacheHelper.getInstance().beforePopulateItems();

            if (resManager == null) {
                return Collections.emptyList();
            }

            Set<IPath> resPaths = resManager.getPaths();

            monitor.beginTask(Messages.getString("ImportExportHandlersManager_populatingItemsMessage"), resPaths.size()); //$NON-NLS-1$

            List<ImportItem> items = new ArrayList<ImportItem>();

            // sort the resources.
            List<IPath> resourcesPathsList = new ArrayList<IPath>(resPaths);
            Collections.sort(resourcesPathsList, new Comparator<IPath>() {

                @Override
                public int compare(IPath o1, IPath o2) {
                    return o1.toPortableString().compareTo(o2.toPortableString());
                }
            });

            // check the special resources first.
            List<IPath> doneList = new ArrayList<IPath>();

            ImportHandlerHelper importHandlerHelper = createImportHandlerHelper();

            for (IPath path : resourcesPathsList) {
                if (monitor.isCanceled()) {
                    return Collections.emptyList();
                }
                if (!importHandlerHelper.validResourcePath(path)) { // valid "*.properties" will do it later.
                    IImportItemsHandler importHandler = findValidImportHandler(resManager, path, enableProductChecking);
                    if (importHandler != null) {
                        ImportItem importItem = null;
                        if (needCheck) {
                            importItem = importHandler.createImportItem(progressMonitor, resManager, path, overwrite, items);
                        } else {
                            importItem = importHandler.generateImportItem(progressMonitor, resManager, path, overwrite, items);
                        }
                        // if have existed, won't add again.
                        if (importItem != null && !items.contains(importItem)) {
                            items.add(importItem);
                            doneList.add(path);
                        }
                    }
                    monitor.worked(1);
                }
            }
            // remove done list
            resourcesPathsList.removeAll(doneList);
            //
            // add for TUP-19934,skip the poms folder which under project folder
            List<IPath> skipList = new ArrayList<IPath>();
            for (IPath path : resourcesPathsList) {
                if (monitor.isCanceled()) {
                    return Collections.emptyList();
                }
                IPath projectFilePath = HandlerUtil.getValidProjectFilePath(resManager, path);
                if (projectFilePath != null) {
                    IPath pomPath = projectFilePath.removeLastSegments(1).append("poms");
                    if (pomPath.isPrefixOf(path)) {
                        skipList.add(path);
                    }
                }
            }
            resourcesPathsList.removeAll(skipList);

            for (IPath path : resourcesPathsList) {
                if (monitor.isCanceled()) {
                    return Collections.emptyList(); //
                }
                // process the "*.properties"
                ImportItem importItem = importHandlerHelper.computeImportItem(monitor, resManager, path, overwrite);
                if (importItem != null) {
                    IImportItemsHandler importHandler = findValidImportHandler(resManager, path, importItem,
                            enableProductChecking, needCheck);
                    if (importHandler != null) {
                        if (importHandler instanceof ImportBasicHandler) {
                            // save as the createImportItem of ImportBasicHandler
                            ImportBasicHandler importBasicHandler = (ImportBasicHandler) importHandler;
                            if (needCheck) {
                                if (importBasicHandler.checkItem(resManager, importItem, overwrite)) {
                                    importBasicHandler.checkAndSetProject(resManager, importItem);
                                }
                            } else {
                                importBasicHandler.resolveItem(resManager, importItem);
                            }
                        }
                    } else {
                        // if don't find valid handler, will try to check by noraml path of items, so set null here.
                        importItem = null;
                    }
                }

                // if have existed, won't add again.
                if (importItem != null && !items.contains(importItem)) {
                    items.add(importItem);
                    // doneList.add(path);
                }
                monitor.worked(1);
            }

            // add empty folders for TUP-2716
            List<IPath> emptyFolders = resManager.getEmptyFolders();
            if (!emptyFolders.isEmpty()) {
                Set<String> toIgnore = new HashSet<String>();
                DynaEnum<? extends DynaEnum<?>>[] values = ERepositoryObjectType.values();
                for (IPath folder : emptyFolders) {
                    ERepositoryObjectType folderType = null;
                    if (folder.segmentCount() < 1) {
                        continue;
                    }
                    if (".svn".equals(folder.lastSegment())) {
                        toIgnore.add(folder.toPortableString());
                        continue;
                    }
                    boolean isChildOfIgnored = false;
                    for (String pathToIgnore : toIgnore) {
                        if (folder.toPortableString().startsWith(pathToIgnore)) {
                            isChildOfIgnored = true;
                            break;
                        }
                    }
                    if (isChildOfIgnored) {
                        continue;
                    }
                    IPath folderPathToCheck = folder.removeFirstSegments(1);
                    String generatedPath = ERepositoryObjectType.DOCUMENTATION.getFolder() + "/"
                            + RepositoryConstants.DOCUMENTATION_GENERATED_PATH;
                    if (folderPathToCheck.removeTrailingSeparator().toPortableString().startsWith(generatedPath)) {
                        continue;
                    }
                    for (DynaEnum<? extends DynaEnum<?>> type : ERepositoryObjectType.values()) {
                        ERepositoryObjectType objectType = (ERepositoryObjectType) type;
                        String[] products = objectType.getProducts();
                        boolean isDI = false;
                        for (String product : products) {
                            if (ERepositoryObjectType.PROD_DI.equals(product)) {
                                isDI = true;
                                break;
                            }
                        }
                        if (isDI && objectType.isResouce()) {
                            if (folderPathToCheck.toPortableString().startsWith(objectType.getFolder() + "/")) {
                                folderType = objectType;
                                ERepositoryObjectType fromChildrenType = getTypeFromChildren(objectType.getChildrenTypesArray(),
                                        folderPathToCheck.toPortableString());
                                if (fromChildrenType != null) {
                                    folderType = fromChildrenType;
                                }
                                if (folderType == ERepositoryObjectType.SQLPATTERNS
                                        && folderPathToCheck.removeTrailingSeparator().toPortableString()
                                                .endsWith(RepositoryConstants.USER_DEFINED)
                                        || folderType == ERepositoryObjectType.TEST_CONTAINER) {
                                    folderType = null;
                                }
                                if (folderPathToCheck.removeTrailingSeparator().toPortableString().equals(objectType.getFolder())) {
                                    // don't import if it is system folder
                                    folderType = null;
                                }
                                break;
                            }
                        }
                    }
                    String pattern = null;
                    if (folderType != null && folderType.isDQItemType()) {
                        pattern = RepositoryConstants.TDQ_ALL_ITEM_PATTERN;
                    } else if (folderType == ERepositoryObjectType.METADATA_FILE_XML) {
                        pattern = RepositoryConstants.SIMPLE_FOLDER_PATTERN;
                    } else {
                        pattern = RepositoryConstants.FOLDER_PATTERN;
                    }
                    if (!Pattern.matches(pattern, folder.lastSegment())) {
                        toIgnore.add(folder.toPortableString());
                        continue;
                    }
                    if (folderType != null) {
                        IPath typePath = new Path(folderType.getFolder());
                        IPath folderPath = folder.removeFirstSegments(1 + typePath.segmentCount()).removeLastSegments(1)
                                .removeTrailingSeparator();
                        String folderLabel = folder.lastSegment();

                        EmptyFolderImportItem folderItem = new EmptyFolderImportItem(folderPath);
                        folderItem.setRepositoryType(folderType);
                        folderItem.setLabel(folderLabel);
                        Project project = PropertiesFactory.eINSTANCE.createProject();
                        project.setTechnicalLabel(folder.segment(0));
                        project.setLabel(folder.segment(0));
                        folderItem.setItemProject(project);

                        Property property = PropertiesFactory.eINSTANCE.createProperty();
                        property.setId(ProxyRepositoryFactory.getInstance().getNextId());
                        FolderItem createFolderItem = PropertiesFactory.eINSTANCE.createFolderItem();
                        ItemState createStatus = PropertiesFactory.eINSTANCE.createItemState();
                        property.setItem(createFolderItem);
                        createStatus.setPath(folderPath.toPortableString());
                        createFolderItem.setState(createStatus);
                        items.add(folderItem);
                        folderItem.setProperty(property);
                    }
                }

            }

            // post populate
            postPopulate(monitor, resManager, items.toArray(new ImportItem[0]));

            return items;
        } finally {

            ImportCacheHelper.getInstance().afterPopulateItems();
            //
            TimeMeasure.end("populateItems"); //$NON-NLS-1$
            TimeMeasure.display = false;
            TimeMeasure.displaySteps = false;
            TimeMeasure.measureActive = false;
        }

    }

    private ERepositoryObjectType getTypeFromChildren(ERepositoryObjectType[] types, String folderPath) {
        ERepositoryObjectType objectType = null;
        for (ERepositoryObjectType type : types) {
            if (folderPath.startsWith(type.getFolder() + "/")) {
                objectType = type;
                ERepositoryObjectType[] childrenTypesArray = objectType.getChildrenTypesArray();
                if (childrenTypesArray.length > 0) {
                    objectType = getTypeFromChildren(childrenTypesArray, folderPath);
                }
                break;
            }

        }
        return objectType;
    }

    protected ImportHandlerHelper createImportHandlerHelper() {
        // TODO Auto-generated method stub
        return new ImportHandlerHelper();
    }

    public void importItemRecords(final IProgressMonitor progressMonitor, final ResourcesManager resManager,
            final List<ImportItem> checkedItemRecords, final boolean overwrite, final ImportItem[] allImportItemRecords,
            final IPath destinationPath) throws InvocationTargetException {
        importItemRecords(progressMonitor, resManager, checkedItemRecords, overwrite, allImportItemRecords, destinationPath,
                /*
                 * disable by default, but provide possibility to enable it
                 */
                /* Boolean.getBoolean("studio.import.option.alwaysRegenId") */ true);
    }

    public void importItemRecords(final IProgressMonitor progressMonitor, final ResourcesManager resManager,
            final List<ImportItem> checkedItemRecords, final boolean overwrite, final ImportItem[] allImportItemRecords,
            final IPath destinationPath, final boolean alwaysRegenId) throws InvocationTargetException {
        TimeMeasure.display = CommonsPlugin.isDebugMode();
        TimeMeasure.displaySteps = CommonsPlugin.isDebugMode();
        TimeMeasure.measureActive = CommonsPlugin.isDebugMode();
        TimeMeasure.begin("importItemRecords"); //$NON-NLS-1$

        changeIdManager.clear();
        final List<EmptyFolderImportItem> checkedFolders = new ArrayList<EmptyFolderImportItem>();
        for (ImportItem importItem : checkedItemRecords) {
            if (importItem instanceof EmptyFolderImportItem) {
                checkedFolders.add((EmptyFolderImportItem) importItem);
            } else {
                changeIdManager.add(importItem);
            }
        }
        checkedItemRecords.removeAll(checkedFolders);

        /*
         * Re-order the import items according to the priority of extension point.
         */
        final List<IImportItemsHandler> importItemHandlersList = new ArrayList<IImportItemsHandler>(
                Arrays.asList(getImportHandlers()));
        Collections.sort(checkedItemRecords, new Comparator<ImportItem>() {

            @Override
            public int compare(ImportItem o1, ImportItem o2) {
                IImportItemsHandler importHandler1 = o1.getImportHandler();
                IImportItemsHandler importHandler2 = o2.getImportHandler();
                if (importHandler1 != null && importHandler2 != null) {
                    int index1 = importItemHandlersList.indexOf(importHandler1);
                    int index2 = importItemHandlersList.indexOf(importHandler2);
                    if (index1 > -1 && index2 > -1) { // both found
                        return index1 - index2;
                    }
                }
                return 0;
            }
        });
        Collections.sort(checkedItemRecords, new Comparator<ImportItem>() {

            @Override
            public int compare(ImportItem o1, ImportItem o2) {
                if (o1.getRepositoryType().getType().equals("SERVICES")) {
                    return -1;
                } else if (o2.getRepositoryType().getType().equals("SERVICES")) {
                    return 1;
                }
                return 0;
            }
        });
        ImportCacheHelper importCacheHelper = ImportCacheHelper.getInstance();
        try {
            // cache
            importCacheHelper.beforeImportItems();

            if (resManager == null || checkedItemRecords.isEmpty() && checkedFolders.isEmpty()) {
                return;
            }
            progressMonitor.beginTask(
                    Messages.getString("ImportExportHandlersManager_importingItemsMessage"), checkedItemRecords.size() * 2 + 1); //$NON-NLS-1$

            /*
             * FIXME ????? why need sort it?
             *
             * Maybe, Have done by priority for import handler, so no need.
             */
            // Collections.sort(itemRecords, new Comparator<ImportItem>() {
            //
            // @Override
            // public int compare(ImportItem o1, ImportItem o2) {
            // if (o1.getProperty().getItem() instanceof RoutineItem && o2.getProperty().getItem() instanceof
            // RoutineItem) {
            // return 0;
            // } else if (!(o1.getProperty().getItem() instanceof RoutineItem)
            // && !(o2.getProperty().getItem() instanceof RoutineItem)) {
            // return 0;
            // } else if (o1.getProperty().getItem() instanceof RoutineItem) {
            // return -1;
            // } else {
            // return 1;
            // }
            // }
            // });

            //

            RepositoryWorkUnit repositoryWorkUnit = new RepositoryWorkUnit(
                    Messages.getString("ImportExportHandlersManager_importingItemsMessage")) { //$NON-NLS-1$

                private boolean hasJoblet = false;

                @Override
                public void run() throws PersistenceException {
                    final IWorkspaceRunnable op = new IWorkspaceRunnable() {

                        @Override
                        public void run(final IProgressMonitor monitor) throws CoreException {
                            try {
                                // pre import
                                preImport(monitor, resManager, checkedItemRecords.toArray(new ImportItem[0]),
                                        allImportItemRecords);
                            } catch (IllegalArgumentException e) {
                                if (e.getCause() instanceof OperationCanceledException) {
                                    throw e; // if invalid project, with cancel
                                }
                            }
                            if (monitor.isCanceled()) {
                                return;
                            }
                            final IProxyRepositoryFactory factory = CoreRuntimePlugin.getInstance().getProxyRepositoryFactory();

                            // bug 10520
                            final Set<String> overwriteDeletedItems = new HashSet<String>();
                            final Set<String> idDeletedBeforeImport = new HashSet<String>();

                            try {
                                importItemRecordsWithRelations(monitor, resManager, checkedItemRecords, overwrite,
                                        allImportItemRecords, destinationPath, overwriteDeletedItems, idDeletedBeforeImport,
                                        alwaysRegenId);
                            } catch (Exception e) {
                                if (Platform.inDebugMode()) {
                                    ExceptionHandler.process(e);
                                }
                                throw generateCoreException(e);
                            }

                            ImportCacheHelper.getInstance().checkDeletedFolders();
                            ImportCacheHelper.getInstance().checkDeletedItems();
                            monitor.done();

                            // import empty folders
                            if (!checkedFolders.isEmpty()) {
                                for (EmptyFolderImportItem folder : checkedFolders) {
                                    checkCancel(monitor);
                                    boolean exist = false;
                                    ERepositoryObjectType repositoryType = folder.getRepositoryType();
                                    IPath path = folder.getPath();
                                    if (destinationPath != null) {
                                        IPath desPath = destinationPath.makeRelativeTo(new Path(repositoryType.getFolder()));
                                        path = desPath.append(folder.getPath());
                                    }
                                    String label = folder.getLabel();
                                    FolderItem getFolderItem = factory.getFolderItem(ProjectManager.getInstance()
                                            .getCurrentProject(), repositoryType, path);
                                    if (getFolderItem != null) {
                                        for (Object obj : getFolderItem.getChildren()) {
                                            if (obj instanceof FolderItem) {
                                                FolderItem existFolder = (FolderItem) obj;
                                                if (label.equals(existFolder.getProperty().getLabel())) {
                                                    exist = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (!exist) {
                                        try {
                                            factory.createFolder(repositoryType, path, label);
                                        } catch (PersistenceException e) {
                                            ExceptionHandler.process(e);
                                        }
                                    }

                                }
                                TimeMeasure.step("importItemRecords", "import empty folders"); //$NON-NLS-1$//$NON-NLS-2$
                            }

                            TimeMeasure.step("importItemRecords", "before allocate new ids"); //$NON-NLS-1$ //$NON-NLS-2$
                            try {
                                changeIdManager.changeIds(monitor);
                            } catch (InterruptedException e) {
                                throw generateCoreException(e);
                            } catch (Exception e) {
                                ExceptionHandler.process(e);
                            }
                            TimeMeasure.step("importItemRecords", "allocate new ids"); //$NON-NLS-1$//$NON-NLS-2$

                            if (hasJoblet && PluginChecker.isJobLetPluginLoaded()) {
                                checkCancel(monitor);
                                monitor.subTask(Messages.getString("ImportExportHandlersManager_progressReloadingJoblets"));
                                TimeMeasure.step("importItemRecords", "before reload joblets"); //$NON-NLS-1$//$NON-NLS-2$
                                IJobletProviderService jobletService = (IJobletProviderService) GlobalServiceRegister.getDefault()
                                        .getService(IJobletProviderService.class);
                                if (jobletService != null) {
                                    jobletService.loadComponentsFromProviders();
                                }
                                TimeMeasure.step("importItemRecords", "reload joblets"); //$NON-NLS-1$//$NON-NLS-2$
                            }

                            // post import
                            List<ImportItem> importedItemRecords = ImportCacheHelper.getInstance().getImportedItemRecords();
                            for (ImportItem importedItem : importedItemRecords) {
                                checkCancel(monitor);
                                String label = importedItem.getLabel();
                                IImportItemsHandler importHandler = importedItem.getImportHandler();
                                try {
                                    monitor.subTask(
                                            Messages.getString("ImportExportHandlersManager_progressApplyMigrationTasks", label));
                                    importHandler.applyMigrationTasks(importedItem, progressMonitor);
                                    TimeMeasure.step("importItemRecords", "applyMigrationTasks: " + label); //$NON-NLS-1$//$NON-NLS-2$
                                } catch (Exception e) {
                                    ExceptionHandler.process(e);
                                }
                                try {
                                    monitor.subTask(
                                            Messages.getString("ImportExportHandlersManager_progressDoFinalCheck", label));
                                    importHandler.afterImportingItems(progressMonitor, resManager, importedItem);
                                    TimeMeasure.step("importItemRecords", "operation after importing item: " + label); //$NON-NLS-1$ //$NON-NLS-2$
                                } catch (Exception e) {
                                    ExceptionHandler.process(e);
                                }
                            }

                            TimeMeasure.step("importItemRecords", "before save"); //$NON-NLS-1$ //$NON-NLS-2$
                            if (RelationshipItemBuilder.getInstance().isNeedSaveRelations()) {
                                RelationshipItemBuilder.getInstance().saveRelations();

                                TimeMeasure.step("importItemRecords", "save relations"); //$NON-NLS-1$ //$NON-NLS-2$
                            } else {
                                // only save the project here if no relation need to be saved, since project will
                                // already be
                                // saved
                                // with relations
                                try {
                                    monitor.subTask(Messages
                                            .getString("ImportExportHandlersManager_progressSavingProjectConfigurations"));
                                    factory.saveProject(ProjectManager.getInstance().getCurrentProject());
                                } catch (PersistenceException e) {
                                    if (Platform.inDebugMode()) {
                                        ExceptionHandler.process(e);
                                    }
                                    throw new CoreException(
                                            new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(),
                                                    Messages.getString("ImportExportHandlersManager_importingItemsError"), e)); //$NON-NLS-1$
                                }
                                TimeMeasure.step("importItemRecords", "save project"); //$NON-NLS-1$//$NON-NLS-2$
                            }

                            postImport(monitor, resManager, importedItemRecords.toArray(new ImportItem[0]));
                        }

                        private void importItemRecordsWithRelations(final IProgressMonitor monitor,
                                final ResourcesManager manager, final List<ImportItem> processingItemRecords,
                                final boolean overwriting, ImportItem[] allPopulatedImportItemRecords, IPath destinationPath,
                                final Set<String> overwriteDeletedItems, final Set<String> idDeletedBeforeImport,
                                final boolean alwaysRegenId) throws Exception {
                            for (ImportItem itemRecord : processingItemRecords) {
                                changeIdManager.add(itemRecord);
                                allocateInternalId(itemRecord, overwrite, alwaysRegenId);
                            }
                            for (ImportItem itemRecord : processingItemRecords) {
                                checkCancel(monitor);
                                if (itemRecord.isImported()) {
                                    continue; // have imported
                                }

                                try {
                                    if ((ERepositoryObjectType.JOBLET == itemRecord.getRepositoryType())
                                            || (ERepositoryObjectType.PROCESS_ROUTELET == itemRecord.getRepositoryType())
                                            || (ERepositoryObjectType.SPARK_JOBLET == itemRecord.getRepositoryType())
                                            || (ERepositoryObjectType.SPARK_STREAMING_JOBLET == itemRecord.getRepositoryType())) {
                                        hasJoblet = true;
                                    } else if (ERepositoryObjectType.TEST_CONTAINER == itemRecord.getRepositoryType()) {
                                        changeIdManager.updateTestContainerParentId(monitor, itemRecord.getItem());
                                    }
                                    final IImportItemsHandler importHandler = itemRecord.getImportHandler();
                                    if (importHandler != null && itemRecord.isValid()) {
                                        List<ImportItem> relatedItemRecord = importHandler.findRelatedImportItems(monitor,
                                                manager, itemRecord, allPopulatedImportItemRecords);
                                        // import related items first
                                        if (importHandler.isPriorImportRelatedItem()) {
                                            if (!relatedItemRecord.isEmpty()) {
                                                importItemRecordsWithRelations(monitor, manager, relatedItemRecord, overwriting,
                                                        allPopulatedImportItemRecords, destinationPath, overwriteDeletedItems,
                                                        idDeletedBeforeImport, alwaysRegenId);
                                            }
                                        }

                                        changeIdManager.add(itemRecord);
                                        allocateInternalId(itemRecord, overwrite, alwaysRegenId);
                                        // will import
                                        importHandler.doImport(monitor, manager, itemRecord, overwriting, destinationPath,
                                                overwriteDeletedItems, idDeletedBeforeImport);

                                        checkCancel(monitor);
                                        // if import related items behind current item
                                        if (!importHandler.isPriorImportRelatedItem()) {
                                            if (!relatedItemRecord.isEmpty()) {
                                                importItemRecordsWithRelations(monitor, manager, relatedItemRecord, overwriting,
                                                        allPopulatedImportItemRecords, destinationPath, overwriteDeletedItems,
                                                        idDeletedBeforeImport, alwaysRegenId);
                                            }
                                        }

                                        // record the imported items with related items too.
                                        ImportCacheHelper.getInstance().getImportedItemRecords().add(itemRecord);

                                        monitor.worked(1);
                                    }
                                } catch (Exception e) {
                                    // ???, PTODO if there one error, need throw error or not.
                                    if (Platform.inDebugMode()) {
                                        // FIXME, catch the exception, and don't block others to import
                                        itemRecord.addError(e.getMessage());
                                        // same the the ImportBasicHandler.logError
                                        ImportCacheHelper.getInstance().setImportingError(true);
                                        ExceptionHandler.process(e);
                                    }
                                }

                            }

                        }

                    };
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    try {
                        ISchedulingRule schedulingRule = workspace.getRoot();
                        // the update the project files need to be done in the workspace runnable to avoid all
                        // notification
                        // of changes before the end of the modifications.
                        workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, progressMonitor);
                    } catch (CoreException e) {
                        if (Platform.inDebugMode()) {
                            ExceptionHandler.process(e);
                        }
                    }
                    // fire import event out of workspace runnable
                    fireImportChange(ImportCacheHelper.getInstance().getImportedItemRecords());
                }

                private void checkCancel(IProgressMonitor monitor) throws CoreException {
                    if (monitor == null) {
                        return;
                    }
                    if (monitor.isCanceled() || Thread.currentThread().isInterrupted()) {
                        throw generateCoreException(
                                new InterruptedException(Messages.getString("IProgressMonitor_UserCancelled")));
                    }
                }

                private CoreException generateCoreException(Exception e) {
                    return new CoreException(new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(),
                            Messages.getString("ImportExportHandlersManager_importingItemsError"), e)); //$NON-NLS-1$
                }

            };
            repositoryWorkUnit.setAvoidUnloadResources(true);
            repositoryWorkUnit.setUnloadResourcesAfterRun(true);
            ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(repositoryWorkUnit);

            progressMonitor.done();


            unloadImportItems(allImportItemRecords);

            if (ImportCacheHelper.getInstance().hasImportingError()) {
                throw new InvocationTargetException(new CoreException(
                        new Status(IStatus.ERROR, FrameworkUtil.getBundle(this.getClass()).getSymbolicName(),
                                Messages.getString("ImportExportHandlersManager_importingItemsError")))); //$NON-NLS-1$
            }
        } finally {
            // cache
            importCacheHelper.afterImportItems();

            //
            final Object root = resManager.getRoot();
            if (root instanceof File) {
                final File workingFolder = (File) root;
                File tmpdir = new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
                if (workingFolder.toString().startsWith(tmpdir.toString()) && !CommonsPlugin.isJUnitTest()) { // remove
                    FilesUtils.deleteFolder(workingFolder, true);
                }
            }
            //
            TimeMeasure.end("importItemRecords"); //$NON-NLS-1$
            TimeMeasure.display = false;
            TimeMeasure.displaySteps = false;
            TimeMeasure.measureActive = false;
        }
    }

    private void fireImportChange(List<ImportItem> importedItemRecords) {
        Set<Item> importedItems = new HashSet<>();
        for (ImportItem importedItem : importedItemRecords) {
            importedItems.add(importedItem.getItem());
        }
        ProxyRepositoryFactory.getInstance().fireRepositoryPropertyChange(ERepositoryActionName.IMPORT.getName(), null, importedItems);
    }

    /**
     *
     * DOC ggu Comment method "prePopulate".
     *
     * Bofore populate the items.
     */
    public void prePopulate(IProgressMonitor monitor, ResourcesManager resManager) {
        IImportResourcesHandler[] importResourcesHandlers = getResourceImportHandlers();
        for (IImportResourcesHandler resHandler : importResourcesHandlers) {
            resHandler.prePopulate(monitor, resManager);
        }
        changeIdManager.clear();
    }

    /**
     *
     * DOC ggu Comment method "postPopulate".
     *
     * after populate the items from resources
     */
    public void postPopulate(IProgressMonitor monitor, ResourcesManager resManager, ImportItem[] populatedItemRecords) {
        IImportResourcesHandler[] importResourcesHandlers = getResourceImportHandlers();
        for (IImportResourcesHandler resHandler : importResourcesHandlers) {
            resHandler.postPopulate(monitor, resManager, populatedItemRecords);
        }

    }

    /**
     *
     * DOC ggu Comment method "preImport".
     *
     * Before import items.
     */
    public void preImport(IProgressMonitor monitor, ResourcesManager resManager, ImportItem[] checkedItemRecords,
            ImportItem[] allImportItemRecords) {
        IImportResourcesHandler[] importResourcesHandlers = getResourceImportHandlers();
        for (IImportResourcesHandler resHandler : importResourcesHandlers) {
            resHandler.preImport(monitor, resManager, checkedItemRecords, allImportItemRecords);
        }

    }

    /**
     *
     * DOC ggu Comment method "postImport".
     *
     * After import items
     */
    public void postImport(IProgressMonitor monitor, ResourcesManager resManager, ImportItem[] importedItemRecords) {
        IImportResourcesHandler[] importResourcesHandlers = getResourceImportHandlers();
        for (IImportResourcesHandler resHandler : importResourcesHandlers) {
            resHandler.postImport(monitor, resManager, importedItemRecords);
        }
    }

    private void unloadImportItems(ImportItem[] importItems) {
        for (ImportItem importItem : importItems) {
            try {
                unloadImportItem(importItem);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }

        changeIdManager.clear();
    }

    private void unloadImportItem(ImportItem importItem) throws Exception {
        ProxyRepositoryFactory proxyFactory = ProxyRepositoryFactory.getInstance();
        // unload the imported resources
        EList<Resource> resources = importItem.getResourceSet().getResources();
        Iterator<Resource> iterator = resources.iterator();
        while (iterator.hasNext()) {
            Resource res = iterator.next();
            // Due to the system of lazy loading for db repository of ByteArray,
            // it can't be unloaded just after create the item.
            if (res != null && !(res instanceof ByteArrayResource)) {
                res.unload();
                iterator.remove();
            }
        }

        Item item = importItem.getItem();
        if (item != null) {
            if (item.getProperty().eResource() != null) {
                proxyFactory.unloadResources(item.getProperty());
                if (item.getParent() != null && item.getParent() instanceof FolderItem) {
                    ((FolderItem) item.getParent()).getChildren().remove(item);
                    item.setParent(null);
                }
            }
        }
        Property property = importItem.getProperty();
        if (property != null) {
            proxyFactory.unloadResources(property);
        }
        importItem.setProperty(null);
        importItem.clear();
    }

    private void allocateInternalId(ImportItem itemRecord, final boolean overwrite, final boolean alwaysRegenId) {
        if (itemRecord.isImported()) {
            return;
        }
        if (alwaysRegenId || itemRecord.getState() == State.ID_EXISTED
                || itemRecord.getState() == State.NAME_AND_ID_EXISTED_BOTH
                || itemRecord.getState() == State.NAME_EXISTED) {
            Map<String, String> nameToIdMap = changeIdManager.getNameToIdMap();
            String id = nameToIdMap.get(itemRecord.getProperty().getLabel()
                    + ERepositoryObjectType.getItemType(itemRecord.getProperty().getItem())
                            .toString());
            if (id == null) {
                try {
                    boolean reuseExistingId = false;
                    if (overwrite && (itemRecord.getState() == State.NAME_AND_ID_EXISTED_BOTH
                            || itemRecord.getState() == State.NAME_EXISTED)) {
                        // just try to reuse the id of the item which will be overwrited
                        reuseExistingId = true;
                    } else if (alwaysRegenId) {
                        switch (itemRecord.getState()) {
                        case NAME_EXISTED:
                        case NAME_AND_ID_EXISTED:
                        case NAME_AND_ID_EXISTED_BOTH:
                            reuseExistingId = true;
                            break;
                        default:
                            break;
                        }
                    }
                    if (reuseExistingId) {
                        IRepositoryViewObject object = itemRecord.getExistingItemWithSameName();
                        if (object != null) {
                            if (ProjectManager.getInstance().isInCurrentMainProject(
                                    object.getProperty())) {
                                // in case it is in reference project
                                id = object.getId();
                            }
                        }
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e, Priority.WARN);
                }
                if (id == null) {
                    /*
                     * if id exsist then need to genrate new id for this job,in this case
                     * the job won't override the old one
                     */
                    id = EcoreUtil.generateUUID();
                }
                nameToIdMap.put(itemRecord.getProperty().getLabel()
                        + ERepositoryObjectType.getItemType(itemRecord.getProperty().getItem())
                                .toString(), id);
            }
            String oldId = itemRecord.getProperty().getId();
            itemRecord.getProperty().setId(id);
            try {
                changeIdManager.mapOldId2NewId(oldId, id);
            } catch (Exception e) {
                ExceptionHandler.process(e);
            }
        }
    }

}
