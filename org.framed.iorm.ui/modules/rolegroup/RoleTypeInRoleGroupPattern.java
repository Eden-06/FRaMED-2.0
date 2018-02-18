package rolegroup;

import java.util.List;

import org.eclipse.graphiti.features.IDirectEditingInfo;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.context.impl.MoveShapeContext;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.framed.iorm.model.Model;
import org.framed.iorm.model.Type;
import org.framed.iorm.ui.UIUtil;
import org.framed.iorm.ui.exceptions.NoModelFoundException;
import org.framed.iorm.ui.palette.FeaturePaletteDescriptor;
import org.framed.iorm.ui.palette.PaletteCategory;
import org.framed.iorm.ui.palette.PaletteView;
import org.framed.iorm.ui.palette.ViewVisibility;

import roletype.RoleTypePattern;

/**
 * TODO
 */
public class RoleTypeInRoleGroupPattern extends RoleTypePattern {
	
	rolegroup.Literals roleGroupLiterals = new rolegroup.Literals();
	
	rolegroup.Util roleGroupUtil = new rolegroup.Util();
	
	/**
	 * the feature palette descriptor manages the palette visibility, see {@link FeaturePaletteDescriptor}
	 */	
	private final FeaturePaletteDescriptor spec_FPD = new FeaturePaletteDescriptor(
		PaletteCategory.NONE,
		ViewVisibility.NO_VIEW) {
			@Override
			public boolean featureExpression(List<String> framedFeatureNames, PaletteView paletteView) {
				return false;
		}	};
		
	//TODO	
	public RoleTypeInRoleGroupPattern() {
		FEATURE_NAME = roleGroupLiterals.ROLETYPE_IN_RG_FEATURE_NAME;
		FPD = spec_FPD;
		ICON_IMG_ID = roleGroupLiterals.ROLETYPE_IN_RG_ICON_IMG_ID;
	}
	
	/**
	 * checks if pattern is applicable for a given business object
	 * @return true, if business object is a {@link org.framed.iorm.model.Shape} of type {@link Type#ROLE_GROUP} in Role Group
	 */
	@Override
	public boolean isMainBusinessObjectApplicable(Object businessObject) {
		if(businessObject instanceof org.framed.iorm.model.Shape) {
			org.framed.iorm.model.Shape shape = (org.framed.iorm.model.Shape) businessObject;
			if(shape.getType() == modelType) {
				System.out.println("BBBs " + ((org.framed.iorm.model.Shape) businessObject).getContainer());
				org.framed.iorm.model.Shape parent = ((org.framed.iorm.model.Shape) businessObject).getContainer().getParent();
				if(parent.getType() == Type.ROLE_GROUP)
					return true;
			}			
		}
		return false;
	}

	/**
	 * checks if pattern is applicable for a given pictogram element
	 * @return true, if business object of the pictogram element is a {@link org.framed.iorm.model.Shape} of type {@link Type#ROLE_GROUP}
	 * in a Role Group
	 */
	@Override
	protected boolean isPatternControlled(PictogramElement pictogramElement) {
		Object businessObject = getBusinessObjectForPictogramElement(pictogramElement);
		return isMainBusinessObjectApplicable(businessObject);
	}

	/**
	 * checks if the pictogram element to edit with the pattern is its root
	 * @return true, if business object of the pictogram element is a {@link org.framed.iorm.model.Shape} of type {@link Type#ROLE_GROUP}
	 * in a Role Group
	 */
	@Override
	protected boolean isPatternRoot(PictogramElement pictogramElement) {
		Object businessObject = getBusinessObjectForPictogramElement(pictogramElement);
		return isMainBusinessObjectApplicable(businessObject);
	}
	
	/**
	 * adds the graphical representation of a role type to the pictogram model
	 * <p>
	 * It creates the following structure:<br>
	 * <ul>
	 *   <li>container shape</li>
	 * 	   <ul>
	 * 	     <li>drop shadow shape</li>
	 *         <ul><li>drop shadow rectangle</li></ul>
	 * 		<li>occurence costraints shape</li>
	 *         <ul><li>occurence costraints rectangle</li></ul>
	 * 		<li>type body shape</li>
	 * 		   <ul><li>type body rectangle</li></ul>
	 * 		   <ul>
	 * 		     <li>name container</li>
	 * 			  <ul><li>name text</li></ul>
	 * 			<li>first line container</li>
	 * 			  <ul><li>first polyline</li></ul>
	 * 			<li>attribute container shape</li>
	 * 			  <ul><li>attribute container rectangle</li></ul>
	 * 			<li>second line container</li>
	 * 			  <ul><li>second polyline</li></ul>
	 * 			<li>operation container shape</li>
	 * 			  <ul><li>operation container rectangle</li></ul>
	 * 		   </ul>
	 * 	   </ul>
	 * </ul> 
	 * <p>
	 * It uses follows this steps:<br>
	 * Step 1: It gets the new object, the diagram to create the role type in and calculates the height and width 
	 * 		   of the role types representation.<br>
	 * Step 2: It creates the structure shown above.<br>
	 * Step 3: It sets the shape identifiers for the created graphics algorithms of the role type.<br>
	 * Step 4: It links the created shapes of the role to its business objects.<br> 
	 * Step 5: It enables direct editing, anchors and layouting of the role. It also updates the compartment type in which 
	 * 		   its created, if any.
	 */
	@Override
	public PictogramElement add(IAddContext addContext) {
		//Step 2
		org.framed.iorm.model.Shape newRoleType = (org.framed.iorm.model.Shape) addContext.getNewObject();
		Diagram targetDiagram = roleGroupUtil.getRoleGroupDiagramForItsShape(addContext.getTargetContainer(), getDiagram()); 
		Model model = UIUtil.getLinkedModelForDiagram(getDiagram());
		if(model == null) throw new NoModelFoundException();
		if(newRoleType.eResource() != null) getDiagram().eResource().getContents().add(newRoleType);
		model.getElements().add(newRoleType);
		newRoleType.setContainer(model);
		
		//Step 1
		org.framed.iorm.model.Shape addedRoleType = (org.framed.iorm.model.Shape) addContext.getNewObject();
		int x =  addContext.getX() + targetDiagram.getGraphicsAlgorithm().getX(),
			y =  addContext.getY() + targetDiagram.getGraphicsAlgorithm().getY(), 
			width = addContext.getWidth(), height = addContext.getHeight();
		if(addContext.getWidth() < literals.MIN_WIDTH) width = literals.MIN_WIDTH;
		if(addContext.getHeight() < literals.MIN_HEIGHT) height = literals.MIN_HEIGHT;
					
		//Step 2
		//container for body shape and shadow
		ContainerShape containerShape = pictogramElementCreateService.createContainerShape(targetDiagram, false);
								  
		//drop shadow
		ContainerShape dropShadowShape = pictogramElementCreateService.createContainerShape(containerShape, true);
		RoundedRectangle dropShadowRectangle = graphicAlgorithmService.createRoundedRectangle(dropShadowShape, literals.ROLE_CORNER_RADIUS, literals.ROLE_CORNER_RADIUS);
		dropShadowRectangle.setForeground(manageColor(literals.COLOR_SHADOW));
		dropShadowRectangle.setBackground(manageColor(literals.COLOR_SHADOW));
		graphicAlgorithmService.setLocationAndSize(dropShadowRectangle, x+literals.SHADOW_SIZE, y+literals.SHADOW_SIZE, width, height);
			
		//occurence costraint
		Shape cardinalityShape = pictogramElementCreateService.createShape(containerShape, true);
		Text cardinalityText = graphicAlgorithmService.createText(cardinalityShape, addedRoleType.getDescription().getName());
		cardinalityText.setForeground(manageColor(literals.COLOR_TEXT));													
		graphicAlgorithmService.setLocationAndSize(cardinalityText, 
			x+width/2-literals.HEIGHT_OCCURRENCE_CONSTRAINT/2, 
			y-literals.HEIGHT_OCCURRENCE_CONSTRAINT-literals.PUFFER_BETWEEN_ELEMENTS, 
			literals.WIDTH_OCCURRENCE_CONSTRAINT, 
			literals.HEIGHT_OCCURRENCE_CONSTRAINT);
			
		//body shape of type
		ContainerShape typeBodyShape = pictogramElementCreateService.createContainerShape(containerShape, true);		
		RoundedRectangle typeBodyRectangle = graphicAlgorithmService.createRoundedRectangle(typeBodyShape, literals.ROLE_CORNER_RADIUS, literals.ROLE_CORNER_RADIUS);
		typeBodyRectangle.setForeground(manageColor(literals.COLOR_LINES));
		typeBodyRectangle.setBackground(manageColor(literals.COLOR_BACKGROUND));
		graphicAlgorithmService.setLocationAndSize(typeBodyRectangle, x, y, width, height);
		
		
		//name
		Shape nameShape = pictogramElementCreateService.createShape(typeBodyShape, false);
		Text text = graphicAlgorithmService.createText(nameShape, addedRoleType.getName());	
		text.setForeground(manageColor(literals.COLOR_TEXT));	
		text.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);	
		graphicAlgorithmService.setLocationAndSize(text, 0, 0, width, literals.HEIGHT_NAME_SHAPE);	
					
		//first line
		Shape firstLineShape = pictogramElementCreateService.createShape(typeBodyShape, false);
		Polyline firstPolyline = graphicAlgorithmService.createPolyline(firstLineShape, new int[] {0, literals.HEIGHT_NAME_SHAPE, width, literals.HEIGHT_NAME_SHAPE});
		firstPolyline.setForeground(manageColor(literals.COLOR_LINES));
					
		//attribute container
		ContainerShape attributeContainer = pictogramElementCreateService.createContainerShape(typeBodyShape, false);
		Rectangle attributeRectangle = graphicAlgorithmService.createRectangle(attributeContainer);
		attributeRectangle.setLineVisible(false);
		attributeRectangle.setBackground(manageColor(literals.COLOR_BACKGROUND));
		int horizontalCenter = util.calculateHorizontalCenter(height);
		graphicAlgorithmService.setLocationAndSize(attributeRectangle, literals.PUFFER_BETWEEN_ELEMENTS, literals.HEIGHT_NAME_SHAPE+literals.PUFFER_BETWEEN_ELEMENTS, 
															   width-2*literals.PUFFER_BETWEEN_ELEMENTS, horizontalCenter-literals.HEIGHT_NAME_SHAPE-2*literals.PUFFER_BETWEEN_ELEMENTS);
		//second line
		Shape secondLineShape = pictogramElementCreateService.createShape(typeBodyShape, false);	
		Polyline secondPolyline = graphicAlgorithmService.createPolyline(secondLineShape, new int[] {0, horizontalCenter, width, horizontalCenter});
		secondPolyline.setForeground(manageColor(literals.COLOR_LINES));
					
		//operation container
		ContainerShape operationContainer = pictogramElementCreateService.createContainerShape(typeBodyShape, false);
		Rectangle operationRectangle = graphicAlgorithmService.createRectangle(operationContainer);
		operationRectangle.setLineVisible(false);
		operationRectangle.setBackground(manageColor(literals.COLOR_BACKGROUND));
		graphicAlgorithmService.setLocationAndSize(operationRectangle, 
			literals.PUFFER_BETWEEN_ELEMENTS, horizontalCenter+literals.PUFFER_BETWEEN_ELEMENTS, 
			width-2*literals.PUFFER_BETWEEN_ELEMENTS, horizontalCenter-literals.ROLE_CORNER_RADIUS/2);
					
		//Step 3
		UIUtil.setShape_IdValue(containerShape, literals.SHAPE_ID_ROLETYPE_CONTAINER);
		UIUtil.setShape_IdValue(cardinalityShape, literals.SHAPE_ID_ROLETYPE_OCCURRENCE_CONSTRAINT);
		UIUtil.setShape_IdValue(typeBodyShape, literals.SHAPE_ID_ROLETYPE_TYPEBODY);
		UIUtil.setShape_IdValue(dropShadowShape, literals.SHAPE_ID_ROLETYPE_SHADOW);
		UIUtil.setShape_IdValue(nameShape, literals.SHAPE_ID_ROLETYPE_NAME);
		UIUtil.setShape_IdValue(firstLineShape, literals.SHAPE_ID_ROLETYPE_FIRSTLINE);
		UIUtil.setShape_IdValue(attributeContainer, literals.SHAPE_ID_ROLETYPE_ATTRIBUTECONTAINER);
		UIUtil.setShape_IdValue(secondLineShape, literals.SHAPE_ID_ROLETYPE_SECONDLINE);
		UIUtil.setShape_IdValue(operationContainer, literals.SHAPE_ID_ROLETYPE_OPERATIONCONTAINER);
					
		//Step 4
		link(containerShape, addedRoleType);
		link(cardinalityShape, addedRoleType);
		link(typeBodyShape, addedRoleType);
		link(dropShadowShape, addedRoleType);
		link(nameShape, addedRoleType);
		link(firstLineShape, addedRoleType);
		link(attributeContainer, addedRoleType);
		link(secondLineShape, addedRoleType);
		link(operationContainer, addedRoleType);
					
		//Step 5
		getFeatureProvider().getDirectEditingInfo().setActive(true);
		IDirectEditingInfo directEditingInfo = getFeatureProvider().getDirectEditingInfo();
		directEditingInfo.setMainPictogramElement(typeBodyShape);
		directEditingInfo.setPictogramElement(nameShape);
		directEditingInfo.setGraphicsAlgorithm(text);
		pictogramElementCreateService.createChopboxAnchor(typeBodyShape);
		layoutPictogramElement(typeBodyShape);
		updateContainingGroupingFeaturesObject();
		return containerShape;
	}	
	
	@Override
	public boolean canMoveShape(IMoveShapeContext moveContext) {
		if(UIUtil.isShape_IdValue((Shape) moveContext.getPictogramElement(), literals.SHAPE_ID_ROLETYPE_SHADOW)) {
			return false;
		}
		if(UIUtil.isShape_IdValue((Shape) moveContext.getPictogramElement(), literals.SHAPE_ID_ROLETYPE_TYPEBODY)) {
			ContainerShape sourcon = moveContext.getSourceContainer(),
					   tarcon = moveContext.getTargetContainer();
			return sourcon.getContainer().equals(roleGroupUtil.getRoleGroupDiagramForItsShape(tarcon, getDiagram()));
		}
		return false;
	}
	
	//move the type body and the drop shadow 
	@Override
	public void moveShape(IMoveShapeContext moveContext) {
		ContainerShape typeBodyShape = (ContainerShape) moveContext.getPictogramElement();
		RoundedRectangle typeBodyRectangle = (RoundedRectangle) typeBodyShape.getGraphicsAlgorithm();
		ContainerShape dropShadowShape = (ContainerShape) ((ContainerShape) typeBodyShape).getContainer().getChildren().get(0);
		RoundedRectangle dropShadowRectangle = (RoundedRectangle) dropShadowShape.getGraphicsAlgorithm();
		Shape OCShape = (Shape) ((ContainerShape) typeBodyShape).getContainer().getChildren().get(1);
		Text OCText = (Text) OCShape.getGraphicsAlgorithm();
		
		int x, y;
		MoveShapeContext moveContextImpl = (MoveShapeContext) moveContext;
		if(moveContextImpl.getProperty("automated") != null) {
			x =  typeBodyRectangle.getX() + moveContext.getDeltaX();
			y =  typeBodyRectangle.getY() + moveContext.getDeltaY();
		} else {
			x =  moveContext.getTargetContainer().getGraphicsAlgorithm().getX() + typeBodyRectangle.getX() + moveContext.getDeltaX();
			y =  moveContext.getTargetContainer().getGraphicsAlgorithm().getY() + typeBodyRectangle.getY() + moveContext.getDeltaY();
		}
			dropShadowRectangle.setX(x + literals.SHADOW_SIZE);
			dropShadowRectangle.setY(y + literals.SHADOW_SIZE);
			Graphiti.getGaService().setLocation(OCText, 
					x+typeBodyRectangle.getWidth()/2-literals.HEIGHT_OCCURRENCE_CONSTRAINT/2, 
					y-literals.HEIGHT_OCCURRENCE_CONSTRAINT-literals.PUFFER_BETWEEN_ELEMENTS);
			typeBodyRectangle.setX(x);
			typeBodyRectangle.setY(y);
	}	
}

