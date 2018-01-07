package group;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.framed.iorm.ui.literals.UILiterals;
import org.framed.iorm.ui.util.UIUtil;

import group.references.AbstractInnerGroupingReference;

/**
 * This class offers utility operations in the scope of the group feature module.
 * @author Kevin Kassin
 */
public class Util {
	
	/**
	 * the object to get names, id and so on for this feature
	 */
	private final Literals literals = new Literals();
	
	//usedInReferences
	//~~~~~~~~~~~~~~~~
	/**
	 * get all sub classes of {@link AbstractInnerGroupingReference} for a given list of classes 
	 * @param classes a given list of classes of different types
	 * @return a list of classes have {@link AbstractInnerGroupingReference} as their super class
	 */
	public List<AbstractInnerGroupingReference> getUsedInReferences(List<Class<?>> classes) {
		List<AbstractInnerGroupingReference> innerGroupingReference = new ArrayList<AbstractInnerGroupingReference>();
		for(Class<?> cl : classes) {
			if(!Modifier.isAbstract(cl.getModifiers())) {
				if(UIUtil.getSuperClasses(cl).contains(AbstractInnerGroupingReference.class)) {
					try {
						Object object = cl.newInstance();
						innerGroupingReference.add((AbstractInnerGroupingReference) object);
					} catch (InstantiationException | IllegalAccessException e) { e.printStackTrace(); }
		} 	}	}
		return innerGroupingReference;
	}
	
	/**
	 * Convenience operation to call {@link UIUtil#getGroupOrCompartmentTypeDiagramForItsShape(Shape, Diagram, String, String, String)} 
	 * with the correct parameters.
	 */
	public Diagram getGroupDiagramForItsShape(Shape groupOrCompartmentTypeShape, Diagram diagram) {
		return UIUtil.getGroupOrCompartmentTypeDiagramForItsShape(groupOrCompartmentTypeShape, diagram, 
				literals.SHAPE_ID_GROUP_TYPEBODY, literals.SHAPE_ID_GROUP_NAME, UILiterals.DIAGRAM_KIND_GROUP_DIAGRAM);
	}

	
	
}