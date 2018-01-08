package org.framed.iorm.ui.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.IFeature;
import org.eclipse.graphiti.features.IMappingProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.graphiti.ui.editor.IDiagramEditorInput;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.framed.iorm.model.Model;
import org.framed.iorm.model.ModelElement;
import org.framed.iorm.model.NamedElement;
import org.framed.iorm.model.Type;
import org.framed.iorm.ui.exceptions.NoDiagramFoundException;
import org.framed.iorm.ui.exceptions.NoFeatureForPatternFound;
import org.framed.iorm.ui.exceptions.NoLinkedModelYet;
import org.framed.iorm.ui.exceptions.NoModelFoundException;
import org.framed.iorm.ui.literals.UILiterals;
import org.framed.iorm.ui.multipage.MultipageEditor;
import org.framed.iorm.ui.providers.ToolBehaviorProvider;
import org.framed.iorm.ui.wizards.RoleModelWizard;
import org.osgi.framework.Bundle;

/**
 * This class offers utility operations in the scope of the UI. Modules can use these operations if they want to,
 * e.g. for UI specific aspects like diagrams, models and the property service.
 * @author Kevin Kassin
 */
public class UIUtil {
	
	//General
	//~~~~~~~
	/**
	 * gets all superclasses of a given class in a list
	 * @param class 
	 * @return
	 */
	public static List<Class<?>> getSuperClasses(Class<?> cl) {
		List<Class<?>> classList = new ArrayList<Class<?>>();	
		Class<?> superclass = cl.getSuperclass();
		if(superclass != null) {	
			classList.add(superclass);
			while (superclass != null) {   
				cl = superclass;
			    superclass = cl.getSuperclass();
			    classList.add(superclass);
		}	}	
		return classList;
	}
	
	/**
	 * sets the values of a given {@link AddContext} using a given {@link CreateContext}
	 * <p>
	 * This operation only deals with add and create contexts for graphiti shapes since graphiti connections use
	 * a special type of create contexts.
	 * @param addContext the {@link AddContext} to set the values in
	 * @param createContext the {@link CreateContext} to get the values of
	 * @return the given {@link AddContext} with set values
	 */
	public static AddContext getAddContextForCreateShapeContext(AddContext addContext, ICreateContext createContext) {
		addContext.setHeight(createContext.getHeight());
		addContext.setWidth(createContext.getWidth());
		addContext.setX(createContext.getX());
		addContext.setY(createContext.getY());
		addContext.setLocation(createContext.getX(), createContext.getY());
		addContext.setSize(createContext.getWidth(), createContext.getHeight());
		return addContext;
	}
	
	/**
	 * gets the {@link IFile} of the CROM for a diagram
	 * @param diagram_resource the resource of the diagram to get the CROM file for
	 * @return the file of the CROM
	 */
	public static IFile getCROMFileForDiagramResource(Resource diagram_resource) {
		URI sourceURI = diagram_resource.getURI();
		sourceURI = sourceURI.trimFileExtension();
		sourceURI = sourceURI.appendFileExtension("crom");
		Path path = new Path(sourceURI.toFileString());
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
	}
	
	//Model
	//~~~~~
	/**
	 * This operation gets the root {@link Model} for a given {@link Diagram}.
	 * @param diagram The diagram to get the model from
	 * @return the root model of the given diagram if there is exactly one model linked and returns null else
	 */
	public static final Model getLinkedModelForDiagram(Diagram diagram) {
		if(diagram.getLink() != null) {
			if(diagram.getLink().getBusinessObjects().size() == 1 &&
			   diagram.getLink().getBusinessObjects().get(0) instanceof Model) {
				return (Model) diagram.getLink().getBusinessObjects().get(0);
			}
		}
		return null;
	}
	
	/**
	 * helper method to get the {@link ModelElement} for a given anchor
	 * @param anchor the anchor that belongs to the model element to get
	 * @return the model element that has the give anchor
	 */
	public static org.framed.iorm.model.ModelElement getModelElementForAnchor(Anchor anchor) {
		Object object = null;
		if (anchor != null) { object = getBusinessObjectForPictogramElement(anchor.getParent()); }
		if (object != null) {
			if (object instanceof org.framed.iorm.model.Shape)
				return (org.framed.iorm.model.Shape) object;
			if (object instanceof org.framed.iorm.model.Relation)
				return (org.framed.iorm.model.Relation) object;
		}
		return null;
	}
	
	/**
	 * fetches the root model of role model which contains the given diagram
	 * <p>
	 * Note: See {@link NoLinkedModelYet} and {@link ToolBehaviorProvider#getListOfFramedFeatureNames} for further
	 * informations.
	 * @param diagram the diagram to search the root model for
	 * @return the root model of a role model
	 */
	public static Model getRootModelForAnyDiagram(Diagram diagram) throws NullPointerException {
		Model rootModel = null;
		Diagram containerDiagram = DiagramUtil.getContainerDiagramForAnyDiagram(diagram);
		for(Shape shape : containerDiagram.getChildren()) {
			if(shape instanceof Diagram &&
			   UIUtil.isDiagram_KindValue((Diagram) shape, UILiterals.DIAGRAM_KIND_MAIN_DIAGRAM)) {
				//Note
				if(shape.getLink() == null) throw new NoLinkedModelYet();
				else { 
					if(shape.getLink().getBusinessObjects().size() == 1) {
						rootModel = (Model) shape.getLink().getBusinessObjects().get(0);
		}	}	}	}
		if(rootModel == null) throw new NoModelFoundException();
		else return rootModel;
	}	
	
	/**
	 * returns the first linked business object of a pictogram
	 * <p>
	 * This operation is build after method {@link IMappingProvider#getBusinessObjectForPictogramElement} to avoid
	 * a dependency.<br>
	 * This is a convenience method for getAllBusinessObjectsForPictogramElement(PictogramElement), because in many 
	 * usecases only a single business object is linked.
	 * @param pictogramElement the pictogram element to get business object for
	 * @return the first business object of a pictogram element
	 */
	public static EObject getBusinessObjectForPictogramElement(PictogramElement pictogramElement) {
		return pictogramElement.getLink().getBusinessObjects().get(0);
	}
	
	/**
	 * return a linked business object for a pictogram element if there is exactly one business object linked 
	 * @param pictogramElement the element to get the linked business object for
	 * @return the one linked business object if or null 
	 */
	public static EObject getBusinessObjectIfExactlyOne(PictogramElement pictogramElement) {
		if(pictogramElement.getLink() != null &&
		   pictogramElement.getLink().getBusinessObjects().size() == 1) {
			return getBusinessObjectForPictogramElement(pictogramElement);
		}	
		return null;
	}		
	
	//EditorInput
	//~~~~~~~~~~~
	/**
	 * fetches the {@link Resource} for a given {@link IEditorInput}
	 * @param editorInput the editor input to get the resource for
	 * @return the resource if edtior input is of type {@link IFileEditorInput} and the resource and be loaded 
	 * and returns null else
	 */
	public static Resource getResourceFromEditorInput(IEditorInput editorInput) {
		ResourceSet resourceSet = new ResourceSetImpl();
		Resource resource = null;
	 	if (editorInput instanceof IFileEditorInput) {
	    	IFileEditorInput fileInput = (IFileEditorInput) editorInput;
	    	IFile file = fileInput.getFile();
	    	resource = resourceSet.createResource(URI.createURI(file.getLocationURI().toString()));
	 	} 
	 	if(editorInput instanceof IDiagramEditorInput) {
	 		IDiagramEditorInput diagramInput = (IDiagramEditorInput) editorInput;
	 		resource = resourceSet.createResource(diagramInput.getUri());
	 	}	
	 	if(resource != null) {
	    	try {
	    		resource.load(null);
	    		return resource;
	    	} catch (IOException e) { e.printStackTrace(); }
	    }
	 	return null;
	}
	
	/**
	 * generates an {@link IFileEditorInput} for a given resource
	 * @param resource the resource to create the editor input for
	 * @return the generated editor input
	 */
	public static IFileEditorInput getIFileEditorInputForResource(Resource resource) {
		IPath path = new Path(resource.getURI().toFileString());
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
		return new FileEditorInput(file);
	}

	/**
	 * This operation creates an {@link IFile} of an file in the role model project for a given {@link IEditorInput} using two steps:
	 * <p>
	 * Step 1: It gets the project the editor input is located in.<br>
	 * Step 2: The method searches for the file in the project the editor input is located in. With the found file
	 * 		   it creates an IFile.
	 * @param editorInput the editor input to get the IFile for
	 * @return the created IFile
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static final IFile getIFileForFile(IEditorInput editorInput, IPath PathToFile) throws IOException, URISyntaxException {
		//Step 1
		IFileEditorInput fileInput = (IFileEditorInput) editorInput;
		IFile file = fileInput.getFile();
		String projectNameOfDiagram = file.getParent().getParent().getName();
		//Step 2		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot(); 
		IPath pathToEmptyTectFile = new Path(projectNameOfDiagram + PathToFile);
		return root.getFile(pathToEmptyTectFile);		
	}
	
	/**
	 * gets an {@link IFile} for a resource used as a given editor input 
	 * @param editorInput the given editor input
	 * @return the corresponding IFile to the resource used as editor input
	 */
	public static final IFile getIFileForEditorInput(IEditorInput editorInput) {
		Resource resource = getResourceFromEditorInput(editorInput);
		IPath path = new Path(resource.getURI().toFileString());
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
	}
	
	//Diagram
	//~~~~~~~
	/**
	 * returns the diagram for a resource fetched from a {@link DiagramEditorInput}
	 * @param resource the resource to get the diagram from
	 * @return the fetched diagram
	 * @throws NoDiagramFoundException
	 */
	public static Diagram getDiagramForResourceOfDiagramEditorInput(Resource resource) {
		Diagram diagram = null;
		if(resource.getEObject(resource.getURI().fragment()) instanceof Diagram) {
			diagram = (Diagram) resource.getEObject(resource.getURI().fragment());
			return diagram;
		}	
		throw new NoDiagramFoundException();
	}
	
	/**
	 * This operation fetches a groups or compartment types diagram for a shape that is a part of a groups
	 * or compartments pictogram representation using the following steps:
	 * <p>
	 * Step 1: It gets the name of the group or compartment depending on the given shape.<br>
	 * Step 2: It searches in the list of children of the container diagram for a diagram with the name
	 * 		   found in step 2. If no such diagram can be found, throw a {@link NoDiagramFoundException}
	 * <p>
	 * If its not clear what <em>container diagram</em> means, see {@link RoleModelWizard#createEmfFileForDiagram} for reference.
	 * @param groupOrCompartmentTypeShape the shape to start the search for the groups diagram 
	 * @param diagram the diagram the group or compartment type is located in
	 * @param the type either {@link Type#GROUP} or {@link Type#COMPARTMENT_TYPE}
	 * @return the groups or compartment types diagram, if the given shape was a name shape or the type body shape
	 * @throws NoDiagramFoundException
	 */
	public static Diagram getGroupOrCompartmentTypeDiagramForItsShape(Shape groupOrCompartmentTypeShape, Diagram diagram,
																	  String SHAPE_ID_TYPEBODY, String SHAPE_ID_NAME, 
																	  String DIAGRAM_KIND) {
		//Step 1
		String name = null;
		if(UIUtil.isShape_IdValue(groupOrCompartmentTypeShape, SHAPE_ID_TYPEBODY)) {
			Shape nameShape = ((ContainerShape) groupOrCompartmentTypeShape).getChildren().get(0);
			if(UIUtil.isShape_IdValue(nameShape, SHAPE_ID_NAME))
				name = ((Text) nameShape.getGraphicsAlgorithm()).getValue();
			}	
		if(UIUtil.isShape_IdValue(groupOrCompartmentTypeShape, SHAPE_ID_NAME))
			name = ((Text) groupOrCompartmentTypeShape.getGraphicsAlgorithm()).getValue();	
		//Step 2
		Diagram containerDiagram = DiagramUtil.getContainerDiagramForAnyDiagram(diagram);
		if(containerDiagram == null) throw new NoDiagramFoundException();
		for(Shape shape : containerDiagram.getChildren()) {
			if(shape instanceof Diagram) {
				if((UIUtil.isDiagram_KindValue((Diagram) shape, DIAGRAM_KIND))) {
					if(((Diagram) shape).getName().equals(name))
						return ((Diagram) shape);
		}	}	}	
		throw new NoDiagramFoundException();	
	}
	
	
	/**
	 * uses an recursive algorithm to find the <em>container diagram</em> of a role model
	 * <p>
	 * If its not clear what <em>container diagram</em> means, see {@link RoleModelWizard#createEmfFileForDiagram} for reference.
	 * @param diagram the diagram to search the container diagram from
	 * @return the container diagram of a role model
	 */
	public static Diagram getContainerDiagramForAnyDiagram(Diagram diagram) {
		if(UIUtil.isDiagram_KindValue(diagram, UILiterals.DIAGRAM_KIND_CONTAINER_DIAGRAM)) return diagram;
		else {
			if(diagram.getContainer() instanceof Diagram)
				return getContainerDiagramForAnyDiagram((Diagram) diagram.getContainer());
			else return null;
		}	
	}
	
	/**
	 * finds the <em>main diagram</em> of a role model
	 * <p>
	 * If its not clear what <em>main diagram</em> means, see {@link RoleModelWizard#createEmfFileForDiagram} for reference.
	 * @param diagram the diagram to search the main diagram from
	 * @return the container diagram of a role model
	 */
	public static Diagram getMainDiagramForAnyDiagram(Diagram diagram) {
		Diagram containerDiagram = getContainerDiagramForAnyDiagram(diagram);
		for(Shape shape : containerDiagram.getChildren()) {
			if(shape instanceof Diagram &&
			   UIUtil.isDiagram_KindValue((Diagram) shape, UILiterals.DIAGRAM_KIND_MAIN_DIAGRAM));
				return (Diagram) shape;
		}
		throw new NoDiagramFoundException();
	}
	
	//Multipage Editor
	//~~~~~~~~~~~~~~~~
	/**
	 * manages to close a given multipage editor at the next reasonable opportunity using the operation 
	 * {@link Display#asyncExec}
	 * <p>
	 * It also saves the multipage editor before closing it to clean up the dirty state of the whole workbench.
	 * @param multipageEditorToClose
	 */
	public static void closeMultipageEditorWhenPossible(MultipageEditor multipageEditorToClose) {
		Display display = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay();
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				multipageEditorToClose.getDiagramEditor().doSave(new NullProgressMonitor());
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(multipageEditorToClose, false);
			}
		});
	}
	
	//features
	//~~~~~~~~
	/**
	 * finds a feature by its name in an array of general features
	 * @param array the array with features to search in
	 * @param featureName the name of the feature to find
	 * @return the found feature or throw exceptions {@link NoFeatureForPatternFound} if it was not found
	 */
	public static IFeature findFeatureByName(IFeature[] array, String featureName) {
		for(int i = 0; i<array.length; i++) {
			if(array[i].getName().equals(featureName)) 
				return array[i];
		}	
		throw new NoFeatureForPatternFound(featureName);
	}
	
	/**
	 * finds a feature by its name in an array of connection create features
	 * @param array the array with features to search in
	 * @param featureName the name of the feature to find
	 * @return the found feature or throw exceptions {@link NoFeatureForPatternFound} if it was not found
	 */
	public static ICreateConnectionFeature findCreateConnectionFeatureByName(ICreateConnectionFeature[] array, String featureName) {
		for(int i = 0; i<array.length; i++) {
			if(array[i].getCreateName().equals(featureName)) 
				return array[i];
		}	
		throw new NoFeatureForPatternFound(featureName);
	}	
	
	//finding pattern dynamically
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * fetches all java classes in the module source folder
	 * @return all java classes in in the module source folder
	 */
	public static List<Class<?>> findModuleJavaClasses() {
		Bundle bundle = Platform.getBundle("org.framed.iorm.ui");
	    List<URL> moduleClassURLs = Collections.list(bundle.findEntries("/modules", "*.java", true));
	    List<URL> coreClassURLs = Collections.list(bundle.findEntries("/core", "*.java", true));
	    List<Class<?>> patternClasses = new ArrayList<Class<?>>();
	    for(URL classURL : moduleClassURLs) {
	    	try {
	    		Class<?> classForPattern = Class.forName(formatModuleURL(classURL.toString()));
	    		patternClasses.add(classForPattern);
			} catch (ClassNotFoundException e) { e.printStackTrace(); }
	    }
	    for(URL classURL : coreClassURLs) {
	    	try {
	    		Class<?> classForPattern = Class.forName(formatCoreURL(classURL.toString()));
	    		patternClasses.add(classForPattern);
			} catch (ClassNotFoundException e) { e.printStackTrace(); }
	    }
	    return patternClasses;
	}
	
	/**
	 * formats the given string url by cutting and replacing character in specific manner
	 * @param patternURL the string url to format
	 * @return the formatted string url
	 */
	public static String formatModuleURL(String patternURL) {
		int cutStart = patternURL.indexOf("modules/")+"modules/".length(),
			cutEnd = patternURL.indexOf(".java");	
		patternURL = patternURL.substring(cutStart, cutEnd);
		return patternURL.replace("/", ".");
	}
	
	/**
	 * formats the given string url by cutting and replacing character in specific manner
	 * @param patternURL the string url to format
	 * @return the formatted string url
	 */
	public static String formatCoreURL(String patternURL) {
		int cutStart = patternURL.indexOf("core/")+"core/".length(),
			cutEnd = patternURL.indexOf(".java");	
		patternURL = patternURL.substring(cutStart, cutEnd);
		return patternURL.replace("/", ".");
	}
	
	//Properties
	//Shape Identifier
	//~~~~~~~~~~~~~~~~
	/**
	 * the key to identify the property shape id gathered from {@link IdentifierLiterals}
	 */
	public static final String KEY_SHAPE_ID = UILiterals.KEY_SHAPE_ID;
		
	/**
	 * set the property shape id for a given shape
	 * @param shape the shape to set the property for
	 * @param value the new value of the property
	 */
	public static final void setShape_IdValue(Shape shape, String value) {
		Graphiti.getPeService().setPropertyValue(shape, KEY_SHAPE_ID, value);
	}
		
	/**
	 * checks if the property shape id for a given shape equals the given value
	 * @param shape the shape to check the property for
	 * @param value the value to check the property against
	 * @return boolean if the property value equals the given value
	 */
	public static final boolean isShape_IdValue(Shape shape, String value) {
		return (Graphiti.getPeService().getPropertyValue(shape, KEY_SHAPE_ID).equals(value));
	}
	
	//Diagram Kind
	//~~~~~~~~~~~~
	/**
	 * the key to identify the property diagram kind gathered from {@link IdentifierLiterals}
	 * <P>
	 * The word <em>kind</em> is chosen to differ this property from the <em>diagram types</em> of the 
	 * graphiti framework.
	 */
	public static final String KEY_DIAGRAM_KIND = UILiterals.KEY_DIAGRAM_KIND;
	
	/**
	 * set the property diagram kind for a given diagram
	 * <P>
	 * The word <em>kind</em> is chosen to differ this property from the <em>diagram types</em> of the 
	 * graphiti framework.
	 * @param diagram the diagram to set the property for
	 * @param value the new value of the property
	 */
	public static final void setDiagram_KindValue(Diagram diagram, String value) {
		Graphiti.getPeService().setPropertyValue(diagram, KEY_DIAGRAM_KIND, value);
	}
		
	/**
	 * checks if the property diagram kind for a given graphic algorithm equals the given value
	 * <P>
	 * The word <em>kind</em> is chosen to differ this property from the <em>diagram types</em> of the 
	 * graphiti framework.
	 * @param diagram the diagram to check the property for
	 * @param value the value to check the property against
	 * @return boolean if the property value equals the given value
	 */
	public static final boolean isDiagram_KindValue(Diagram diagram, String value) {
		return (Graphiti.getPeService().getPropertyValue(diagram, KEY_DIAGRAM_KIND).equals(value));
	}	
	
	//Grouping
	//~~~~~~~~
	/**
	 * fetches the <em>type body shape</em> of a grouping element (e.g. groups or compartment types) to thats the givem 
	 * <em>container shape</em> belongs to
	 * <p>
	 * If its not clear what <em>type body shape</em> and <em>container shape</em> means, see 
	 * {@link GroupPattern#add} for example.
	 * @param groupContainer the container shape of a grouping element to get the type body shape for
	 * @return the type body shape of a grouping element with the given shape
	 */
	public static ContainerShape getTypeBodyForGroupingContainer(ContainerShape groupContainer, String SHAPE_ID_TYPEBODY) {
		for(Shape shape : groupContainer.getChildren()) {
			if(shape instanceof ContainerShape) {
			   if(UIUtil.isShape_IdValue(shape, SHAPE_ID_TYPEBODY))
				   return (ContainerShape) shape; 
		}	}  
		return null;
	}
		
	//Names
	//~~~~~
	/**
	 * matching operation for the regular expression of identifiers
	 * @param identifier the string to check against
	 * @return if the given string input matches the regular expression
	 */
	public static final boolean matchesIdentifier(String identifier) {
		Matcher identifierMatcher = Pattern.compile(UILiterals.REG_EXP_IDENTIFIER).matcher(identifier);
		return identifierMatcher.matches();
	}
	
	/**
	 * matching operation for the regular expression of operations
	 * @param identifier the string to check against
	 * @return if the given string input matches the regular expression
	 */
	public static final boolean matchesAttribute(String attributeName) {
		Matcher attributeMatcher =  Pattern.compile(UILiterals.REG_EXP_ATTRIBUTE).matcher(attributeName);
		return attributeMatcher.matches();
	}
	
	/**
	 * matching operation for the regular expression of operations
	 * @param identifier the string to check against
	 * @return if the given string input matches the regular expression
	 */
	public static final boolean matchesOperation(String operationName) {
		Matcher operationMatcher =  Pattern.compile(UILiterals.REG_EXP_OPERATION).matcher(operationName);
		return operationMatcher.matches();
	}
	
	/**
	 * matching operation for the regular expression of cardinalities
	 * @param identifier the string to check against
	 * @return if the given string input matches the regular expression
	 */
	public static final boolean matchesCardinality(String cardinality) {
		Matcher cardinalityMatcher = Pattern.compile(UILiterals.REG_EXP_CARDINALITY).matcher(cardinality);
		return cardinalityMatcher.matches();
	}
	
	/**
	 * calculates if another model element of a specific {@link org.framed.iorm.model.Type} already has a name equivalent 
	 * to the new given name when direct editing names using the following steps:
	 * <p>
	 * Step 1: It gets the <em>main diagram</em> of the role model that the given diagram belongs to.<br>
	 * Step 2: It fetches a list of the model element names for the given type and checks if this list contains
	 * 		   the new name.
	 * <p>
	 * If its not clear what <em>main diagram</em> means, see {@link RoleModelWizard#createEmfFileForDiagram} for reference.
	 * @param diagram the diagram that is direct edited
	 * @param type the type to the check for if a model element of that type already has the same name
	 * @param newName the name to check against
	 * @return boolean if another model element of a given type already has the same name when direct editing
	 */
	public static boolean nameAlreadyUsedForClass(Diagram diagram, Type type, String newName) {
		List<String> modelElements = new ArrayList<String>();
		//Step 1
		Model rootModel = UIUtil.getRootModelForAnyDiagram(diagram);
		//Step 2
		getModelElementsNamesRecursive(rootModel, type, modelElements);
		return modelElements.contains(newName);
	}
		
	/**
	 * calculates if another model element in a comparment type already has a name equivalent 
	 * to the new given name in the compartment type when direct editing names using the following steps:
	 * <p>
	 * Step 1: It gets the compartments diagram.<br>
	 * Step 2: It fetches a list of the model element names for the given type and checks if this list contains
	 * 		   the new name.
	 * <p>
	 * @param diagram the diagram that is direct edited
	 * @param type the type to the check for if a model element of that type already has the same name
	 * @param newName the name to check against
	 * @return boolean if another model element of a given type already has the same name when direct editing
	 */
	public static boolean nameAlreadyUsedForCompartmentTypeElements(Diagram diagram, Type type, String newName) {
		List<String> compartmentsElements = new ArrayList<String>();
		//Step 1
		Model compartmentsModel = UIUtil.getLinkedModelForDiagram(diagram);
		//Step 2
		getModelElementsNames(compartmentsModel, type, compartmentsElements);
		return compartmentsElements.contains(newName);
	}
				
	/**
	 * calculates if another attribute or operation in the same class or role already have the same name when 
	 * direct editing names of attributes or operations 
	 * @param attributeOrOperationsContainer the container to search the other attributes or operations to check against
	 * @param newName the name to check against
	 * @return boolean if another attribute or operation in the same class or role already has the same name when 
	 * 		   direct editing
	 */
	public static boolean nameAlreadyUsedForAttributeOrOperation(ContainerShape attributeOrOperationsContainer, String newName) {
		List<String> attributeOrOperationNames = new ArrayList<String>();
		for(Shape shape : attributeOrOperationsContainer.getChildren()) {
			attributeOrOperationNames.add(((Text) shape.getGraphicsAlgorithm()).getValue());
		}
		return attributeOrOperationNames.contains(newName);
	}	
	
	/**
	 * calculates the standard name of a class ({@link Type#NATURAL_TYPE} for example) when creating one
	 * <p>
	 * The standard name will be build by using a given standard name and adding a number as suffix to it
	 * if needed. The limit of this number is set in {@link #STANDART_NAMES_COUNTER_LIMIT}.
	 * @param diagram the diagram in that a class is added
	 * @param type the type to the check for if a model element with the a standard name already exists
	 * @param standardName the normally used standard name for the class
	 * @return
	 */
	public static String calculateStandardNameForClass(Diagram diagram, Type type, String standardName) {
		List<String> modelElements = new ArrayList<String>();
		Model rootModel = DiagramUtil.getRootModelForAnyDiagram(diagram);
		getModelElementsNamesRecursive(rootModel, type, modelElements);
		return calcluateStandardNameForGivenCollection(modelElements, standardName);
	}	
	
	/**
	 * calculates the standard name of a element of a compartment type which should be unique named compartment wide
	 * when creating one
	 * <p>
	 * Standard names of compartment types in a compartment type are calculated by  {@link #calculateStandardNameForClass},
	 * since they need to be unique named over all compartment types in the while role model.
	 * <p>
	 * The standard name will be build by using a given standard name and adding a number as suffix to it
	 * if needed. The limit of this number is set in {@link #STANDART_NAMES_COUNTER_LIMIT}.
	 * @param diagram the diagram in that compartment type element is added
	 * @param type the type to the check for if a model element with the standard name already exists
	 * @param standardName the normally used standard name for the compartment type element
	 * @return
	 */
	public static String calculateStandardNameForCompartmentsTypeElement(Diagram diagram, Type type, String standardName) {
		List<String> compartmentsElements = new ArrayList<String>();
		Model compartmentModel = DiagramUtil.getLinkedModelForDiagram(diagram);
		getModelElementsNames(compartmentModel, type, compartmentsElements);
		return calcluateStandardNameForGivenCollection(compartmentsElements, standardName);
 	}
	
	/**
	 * calculates the standard name of an attribute or operation when creating one
	 * <p>
	 * The standard name will be build by using a given standard name and adding a number as suffix to it
	 * if needed. The limit of this number is set in {@link #STANDART_NAMES_COUNTER_LIMIT}.
	 * @param attributeOrOperationsContainer the container to search the other attributes or operations to check for
	 * 		  already used standard names
	 * @param standardName the normally used standard name for the attribute or operation
	 * @return
	 */
	public static String calculateStandardNameForAttributeOrOperation(ContainerShape attributeOrOperationsContainer, String standardName) {
		List<String> attributeOrOperationNames = new ArrayList<String>();
		for(Shape shape : attributeOrOperationsContainer.getChildren()) {
			attributeOrOperationNames.add(((Text) shape.getGraphicsAlgorithm()).getValue());
		}
		return calcluateStandardNameForGivenCollection(attributeOrOperationNames, standardName);
	}
	
	/**
	 * calculates a standard name checking a given collection if an equivalent name already exists in that collection
	 * <p>
	 * The standard name will be build by using a given standard name and adding a number as suffix to it
	 * if needed. The limit of this number is set in {@link #STANDART_NAMES_COUNTER_LIMIT}.
	 * @param collection
	 * @param standardName
	 * @return
	 */
	private static String calcluateStandardNameForGivenCollection(List<String> collection, String standardName) {
		if(!(collection.contains(standardName))) return standardName;
		for(int i=1; i<=UILiterals.STANDARD_NAMES_COUNTER_LIMIT; i++) {
			if(!(collection.contains(standardName + Integer.toString(i))))
				return standardName + Integer.toString(i);
		}
		return standardName;
	}
		
	/**
	 * fetches all names of model elements only of the given model of a given type in a recursive manner
	 * @param model the model to fetch the model elements names from
	 * @param type the type of the model elements to get the names from
	 * @param modelElementNames the list of model element names to fill
	 */
	private static void getModelElementsNames(Model model, Type type, List<String> modelElementNames) {
		for(ModelElement modelElement : model.getElements()) {
			if(modelElement.getType() == type)  
				modelElementNames.add(modelElement.getName());
	}	}
		
	/**
	 * fetches all names of model elements of the given model and its sub models of a given type in a recursive manner
	 * @param model the model to fetch the model elements names from
	 * @param type the type of the model elements to get the names from
	 * @param modelElementNames the list of model element names to fill while using recursion
	 */
	private static void getModelElementsNamesRecursive(Model model, Type type, List<String> modelElementNames) {
		for(ModelElement modelElement : model.getElements()) {
			if(modelElement.getType() == type)  
				modelElementNames.add(modelElement.getName());
			if(modelElement.getType() == Type.COMPARTMENT_TYPE ||
			   modelElement.getType() == Type.GROUP) 
				getModelElementsNamesRecursive(((org.framed.iorm.model.Shape) modelElement).getModel(), type, modelElementNames);
	}	}	
	
	//Updates
	//~~~~~~~
	/**
	 * This operation gets the name of a pictogram element with text shape as children.
	 * @param pictogramElement the pictogram element to get the name of
	 * @param SHAPE_ID_NAME the identifier of the textshape
	 * @return the name of a pictogram element if it has a text shape as children and return null else 
	 */
	public static String getNameOfPictogramElement(PictogramElement pictogramElement, String SHAPE_ID_NAME) {
		if (pictogramElement instanceof ContainerShape) {
			ContainerShape containerShape = (ContainerShape) pictogramElement;
			for (Shape shape : containerShape.getChildren()) {
				if (shape.getGraphicsAlgorithm() instanceof Text) {
					Text text = (Text) shape.getGraphicsAlgorithm();
					if(UIUtil.isShape_IdValue(shape, SHAPE_ID_NAME)) {
						return text.getValue();
					}
		} 	}	}
		return null;
	}

	/**
	 * This operation gets the name of a business object that is an {@link org.framed.iorm.model.Shape}
	 * @param businessObject the business object to get the name of
	 * @return the name of the business object if it is an {@link org.framed.iorm.model.Shape}
	 */
	public static String getNameOfBusinessObject(Object businessObject) {
		if (businessObject instanceof org.framed.iorm.model.Shape) {
			org.framed.iorm.model.Shape shape = (org.framed.iorm.model.Shape) businessObject;
			return shape.getName();
		}
		return null;
	}
	
	/**
	 * This operation gets the names of the attributes of a pictogram element that has an attribute container shape.
	 * @param pictogramElement the pictogram element to get the attribute names of
	 * @param SHAPE_ID_ATTRIBUTECONTAINER the identifier of the attribute container shape
	 * @return the attribute names of the pictogram element if it has an attribute container shape and returns null else
	 */
	public static List<String> getpictogramAttributeNames(PictogramElement pictogramElement, String SHAPE_ID_ATTRIBUTECONTAINER, String SHAPE_ID_ATTRIBUTE_TEXT) {
		List<String> pictogrammAttributeNames = new ArrayList<String>();
		if (pictogramElement instanceof ContainerShape) {
			ContainerShape containerShape = (ContainerShape) pictogramElement;
			for (Shape shape : containerShape.getChildren()) {
				if(shape instanceof ContainerShape) {
					ContainerShape innerContainerShape = (ContainerShape) shape;
					if(UIUtil.isShape_IdValue(innerContainerShape, SHAPE_ID_ATTRIBUTECONTAINER)) {
						for(Shape attributeShape : innerContainerShape.getChildren()) {
							if(UIUtil.isShape_IdValue(attributeShape, SHAPE_ID_ATTRIBUTE_TEXT)) {
								Text text = (Text) attributeShape.getGraphicsAlgorithm();
								pictogrammAttributeNames.add(text.getValue());
		}	}	}	}	}	}
		return pictogrammAttributeNames;
	}
	
	/**
	 * This operation gets the names of the attributes in a model of a pictogram element that has an attribute container shape.
	 * @param pictogramElement the pictogram element to get the attribute names of
	 * @param SHAPE_ID_ATTRIBUTECONTAINER the identifier of the attribute container shape
	 * @return the attribute names in a model of the pictogram element if it has an attribute container shape and returns null else
	 */
	public static List<String> getBusinessAttributeNames(PictogramElement pictogramElement, String SHAPE_ID_ATTRIBUTECONTAINER, String SHAPE_ID_ATTRIBUTE_TEXT) {
		List<String> businessAttributeNames = new ArrayList<String>();
		if (pictogramElement instanceof ContainerShape) {
			ContainerShape containerShape = (ContainerShape) pictogramElement;
			for (Shape shape : containerShape.getChildren()) {
				if(shape instanceof ContainerShape) {
					ContainerShape innerContainerShape = (ContainerShape) shape;
					if(UIUtil.isShape_IdValue(innerContainerShape, SHAPE_ID_ATTRIBUTECONTAINER)) {
						for(Shape attributeShape : innerContainerShape.getChildren()) {
							if(UIUtil.isShape_IdValue(attributeShape, SHAPE_ID_ATTRIBUTE_TEXT)) {	
								NamedElement attribute = (NamedElement) UIUtil.getBusinessObjectForPictogramElement(attributeShape);
								businessAttributeNames.add(attribute.getName());
		}	}	}	}	}	}	
		return businessAttributeNames;
	}
	
	/**
	 * This method gets the names of the operations of a pictogram element that has an operation container shape.
	 * @param pictogramElement the pictogram element to get the operation names of
	 * @param SHAPE_ID_OPERATIONCONTAINER the identifier of the operation container shape
	 * @return the operation names of the pictogram element if it has an operation container shape and returns null else
	 */
	public static List<String> getpictogramOperationNames(PictogramElement pictogramElement, String SHAPE_ID_OPERATIONCONTAINER, String SHAPE_ID_OPERATION_TEXT) {
		List<String> pictogramOperationNames = new ArrayList<String>();
		if (pictogramElement instanceof ContainerShape) {
			ContainerShape containerShape = (ContainerShape) pictogramElement;
			for (Shape shape : containerShape.getChildren()) {
				if(shape instanceof ContainerShape) {
					ContainerShape innerContainerShape = (ContainerShape) shape;
					if(UIUtil.isShape_IdValue(innerContainerShape, SHAPE_ID_OPERATIONCONTAINER)) {
						for(Shape operationShape : innerContainerShape.getChildren()) {
							if(UIUtil.isShape_IdValue(operationShape, SHAPE_ID_OPERATION_TEXT)) {
								Text text = (Text) operationShape.getGraphicsAlgorithm();
								pictogramOperationNames.add(text.getValue());
		}	}	}	}	}	}
		return pictogramOperationNames;
	}
	
	/**
	 * This method gets the names of the operations in a model of pictogram element that has an operation container shape.
	 * @param pictogramElement the pictogram element to get the operation names of
	 * @param SHAPE_ID_OPERATIONCONTAINER the identifier of the operation container shape
	 * @return the operation names in a model of the pictogram element if it has an operation container shape and returns null else
	 */
	public static List<String> getBusinessOperationNames(PictogramElement pictogramElement, String SHAPE_ID_OPERATIONCONTAINER, String SHAPE_ID_OPERATION_TEXT) {
		List<String> businessOperationNames = new ArrayList<String>();
		if (pictogramElement instanceof ContainerShape) {
			ContainerShape containerShape = (ContainerShape) pictogramElement;
			for (Shape shape : containerShape.getChildren()) {
				if(shape instanceof ContainerShape) {
					ContainerShape innerContainerShape = (ContainerShape) shape;
					if(UIUtil.isShape_IdValue(innerContainerShape, SHAPE_ID_OPERATIONCONTAINER)) {
						for(Shape operationShape : innerContainerShape.getChildren()) {
							if(UIUtil.isShape_IdValue(operationShape, SHAPE_ID_OPERATION_TEXT)) {	
								NamedElement operation = (NamedElement) UIUtil.getBusinessObjectForPictogramElement(operationShape);
								businessOperationNames.add(operation.getName());
		}	}	}	}	}	}	
		return businessOperationNames;
	}
	
	/**
	 * fetches the shown occurrence constraint of a role type or role group by its given type body shape
	 * @param pictogramElement the pictogram element to get the shown occurrence constraint for 
	 * @param SHAPE_ID_OCCURRENCE_CONSTRAINT the identifier of the occurrence constraint shape
	 * @return the value of the occurrence constraint if it can be found
	 */
	public static String getOccurenceConstraintOfPictogramElement(PictogramElement pictogramElement, String SHAPE_ID_OCCURRENCE_CONSTRAINT) {
		if(pictogramElement instanceof Shape) {
			Shape shape = (Shape) pictogramElement;
			for(Shape containerChild : shape.getContainer().getChildren()) {
				if(UIUtil.isShape_IdValue(containerChild, SHAPE_ID_OCCURRENCE_CONSTRAINT))
					return ((Text) containerChild.getGraphicsAlgorithm()).getValue();
		}	}
		return null;
	}
	
	/**
	 * get the occurrence constraint of a role type or role group in the business model
	 * @param businessObject the business object to get occurrence constraint for
	 * @return the occurrence constraint value
	 */
	public static String getOccurrenceConstraintOfBusinessObject(Object businessObject) {
		if (businessObject instanceof org.framed.iorm.model.Shape) {
			org.framed.iorm.model.Shape shape = (org.framed.iorm.model.Shape) businessObject;
			return shape.getDescription().getName();
		}
		return null;
	}	
	
	/**
	 * fetches all the names of the actual direct child elements in a groups model except {@link Relation}s
	 * @param pictogramElement the pictogram/ shape element of the group
	 * @param diagram the diagram the pattern that called this operation works in
	 * @param the type e.g. {@link Type#GROUP} or {@link Type#COMPARTMENT_TYPE}
	 * @return a list of names of all direct child elements in a groups model
	 */
	public static List<String> getGroupOrCompartmentTypeElementNames(PictogramElement pictogramElement, Diagram diagram, Type type) {
		List<String> modelElementsNames = new ArrayList<String>();
		Diagram groupOrCompartmentTypeDiagram = DiagramUtil.getGroupOrCompartmentTypeDiagramForItsShape((ContainerShape) pictogramElement, diagram, type);
		Model groupOrCompartmentTypeModel = DiagramUtil.getLinkedModelForDiagram(groupOrCompartmentTypeDiagram);
		for(ModelElement modelElement : groupOrCompartmentTypeModel.getElements()) {
			if(modelElement instanceof org.framed.iorm.model.Shape)
				modelElementsNames.add(modelElement.getName());
		}
		return modelElementsNames;
	}
	
	/**
	 * fetches all the names of the groups content that are shown in <em>model content preview container</em> of the group
	 * <p>
	 * If its not clear what <em>model content preview container</em> and <em>type body shape</em> means, see 
	 * {@link GroupPattern#add} for example. 
	 * @param pictogramElement the type body shape of of a group
	 * @return a list of the shown names of child elements of a group
	 */
	public static List<String> getContentPreviewElementsNames(PictogramElement pictogramElement, String SHAPE_ID_CONTENT_PREVIEW, String SHAPE_ID_ELEMENT) {
		List<String> modelContainerElementsNames = new ArrayList<String>();
		if(pictogramElement instanceof ContainerShape) {
			ContainerShape containerShape = (ContainerShape) pictogramElement;
			for(Shape shape : containerShape.getChildren()) {
				if(UIUtil.isShape_IdValue(shape, SHAPE_ID_CONTENT_PREVIEW)) {
					ContainerShape previewContentContainer = (ContainerShape) shape; 
					for(Shape previewContentContainerElement : previewContentContainer.getChildren()) {
						if(UIUtil.isShape_IdValue(previewContentContainerElement, SHAPE_ID_ELEMENT)) {
							Text text = (Text) previewContentContainerElement.getGraphicsAlgorithm();
							modelContainerElementsNames.add(text.getValue());
		}	}	}	}	}
		return modelContainerElementsNames;
	}
	
	/**
	 * calculate the string for a group or compartment type element that is shown in the group as
	 * preview of its content
	 * @param modelElement the model elemen in the group or compartment type to calculate the shown string for
	 * @return the value of preview string of group or compartment type content for the given modelElement
	 */
	public static String getGroupOrCompartmentTypeElementText(ModelElement modelElement) {
		return modelElement.getType().toString() + " " + modelElement.getName();
	}
}
