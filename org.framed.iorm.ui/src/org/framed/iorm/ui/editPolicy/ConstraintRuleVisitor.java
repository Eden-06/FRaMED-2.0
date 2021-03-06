package org.framed.iorm.ui.editPolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.framed.iorm.model.ModelElement;
import org.framed.iorm.model.Relation;
import org.framed.iorm.model.Type;
import org.framed.iorm.ui.UIUtil;
import org.framed.iorm.ui.exceptions.NoDiagramFoundException;

import editpolicymodel.AndConstraintRule;
import editpolicymodel.ConstraintRule;
import editpolicymodel.ContainsType;
import editpolicymodel.FalseConstraintRule;
import editpolicymodel.InType;
import editpolicymodel.IsSourceType;
import editpolicymodel.IsTargetConnectionType;
import editpolicymodel.IsTargetType;
import editpolicymodel.NotConstraintRule;
import editpolicymodel.OrConstraintRule;
import editpolicymodel.SourceEqualsTarget;
import editpolicymodel.SourceEqualsTargetType;
import editpolicymodel.TrueConstraintRule;

/**
 * This class provides the rule-parse for the command-rules. Using
 * VisitorPattern
 *
 * @author Christian Deussen
 *
 */
public class ConstraintRuleVisitor {

	public Map<Type, String> groupingFeatureCache = new HashMap<Type, String>();

	/**
	 * command to check rules against
	 */
	private Object context;

	@SuppressWarnings("unused")
	private Type type;

	public ConstraintRuleVisitor(Object context, Type type, Diagram diagram) {
		this.context = context;
		this.type = type;
	}

	public Diagram getDiagramWithName(String name, Diagram rootDiagram) {
		Diagram containerDiagram = UIUtil.getContainerDiagramForAnyDiagram(rootDiagram);
		if (containerDiagram == null)
			throw new NoDiagramFoundException();
		for (Shape shape : containerDiagram.getChildren()) {
			if (shape instanceof Diagram) {
				if (((Diagram) shape).getName().equals(name))
					return ((Diagram) shape);
			}
		}
		return null;
	}

	/**
	 * dispatch abstract rule
	 *
	 * returns whether this.cmd is allowed according to @param rule
	 *
	 * @param rule
	 * @return Boolean
	 */
	public boolean checkRule(ConstraintRule rule) {
		if (rule instanceof InType) {
			return isInTypeRuleVisitor((InType) rule);
		}

		if (rule instanceof AndConstraintRule)
			return andRuleVisitor((AndConstraintRule) rule);

		if (rule instanceof OrConstraintRule)
			return orRuleVisitor((OrConstraintRule) rule);

		if (rule instanceof NotConstraintRule)
			return notRuleVisitor((NotConstraintRule) rule);

		if (rule instanceof IsSourceType) {
			return isSourceTypeVisitor((IsSourceType) rule);
		}

		if (rule instanceof SourceEqualsTargetType) {
			return sourceEqualsTargetTypeVisitor((SourceEqualsTargetType) rule);
		}

		if (rule instanceof SourceEqualsTarget) {
			return sourceEqualsTargetVisitor((SourceEqualsTarget) rule);
		}

		if (rule instanceof IsTargetType) {
			return isTargetTypeVisitor((IsTargetType) rule);
		}

		if (rule instanceof ContainsType) {
			return containsTypeVisitor((ContainsType) rule);
		}
		
		if (rule instanceof IsTargetConnectionType) {
			return isTargetConnectionTypeVisitor((IsTargetConnectionType) rule);
		}

		if (rule instanceof TrueConstraintRule) {
			return true;
		}

		if (rule instanceof FalseConstraintRule) {
			return false;
		}

		System.out.println("checkRule for " + rule.getClass().toString() + " not implemented");
		return true;
	}

	private boolean isInTypeRuleVisitor(InType rule) {
		ContainerShape container = null;
		if (this.context instanceof AddContext) {
			container = ((AddContext) this.context).getTargetContainer();
		} else if (this.context instanceof CreateContext) {
			container = ((CreateContext) this.context).getTargetContainer();
		}
		if (container == null)
			return false;
		// travers Containers to find the compartment type.
		Diagram compartmentDiagram = findContainerDiagramOfType(container, this.getTypeByLiteral(rule.getType().getLiteral()));
		return compartmentDiagram != null;
	}

	private Type getTypeByLiteral(String typeString) {
		for (Type type : Type.VALUES ) {
			if(type.getLiteral().equals(typeString)) {
				return type;
			}
		}
		return null;
	}
	
	/**
	 * Returns the grouping reference (diagram kind) of the given container type, if
	 * it exists. Note: this method is cached for better performance.
	 * 
	 * @param type
	 *            the IORM::Type of the container element
	 * @return the string reference of the diagram kind of the given container type
	 */
	private String getGroupingReference(Type type) {
		if (groupingFeatureCache.containsKey(type))
			return groupingFeatureCache.get(type);
		Optional<String> result = UIUtil.getGroupingFeatureReferences().stream().filter(a -> a.getModelType() == type) // rule.getType();
				.map(a -> a.getDiagramKind()).findFirst();
		if (!result.isPresent())
			return null;
		groupingFeatureCache.put(type, result.get());
		return result.get();
	}

	/**
	 * This method checks if the given container shape is a Diagram of the Diagram
	 * Kind of the given type. Otherwise, the method traverses the containers to
	 * find a suitable candidate, if any exists.
	 * 
	 * @param sourceShape
	 *            the ContainerShape to investigate
	 * @param type
	 *            the IORM::Type of the container to look for
	 * @return the diagram of the correct diagram kind or null if no corresponding
	 *         diagram was not found.
	 */
	public Diagram findContainerDiagramOfType(ContainerShape sourceShape, Type type) {
		String groupingReference = getGroupingReference(type);
		while ((sourceShape != null) && !((sourceShape instanceof Diagram)
				&& (UIUtil.isDiagram_KindValue((Diagram) sourceShape, groupingReference)))) {
			sourceShape = sourceShape.getContainer();
		}
		if (sourceShape == null)
			return null;
		if (sourceShape instanceof Diagram)
			return (Diagram) sourceShape;
		throw new IllegalStateException("Invariant violated! findDiagramOfType found a sourceShape that was not a Diagram.");
	}

	private boolean andRuleVisitor(AndConstraintRule rule) {
		for (ConstraintRule abstractRule : rule.getRules()) {
			if (!checkRule(abstractRule))
				return false;
		}
		return true;
	}

	private boolean isSourceTypeVisitor(IsSourceType rule) {
		Anchor sourceAnchor = this.getSourceAnchorFromContext(this.context);
		if (sourceAnchor == null) {
			System.out.println("failed isSourceType() sourceAnchor== null,  this.context: " + this.context.toString());
			return false;
		}
		ModelElement source = UIUtil.getModelElementForAnchor(sourceAnchor);
		return rule.getType().getLiteral().equals(source.getType().getLiteral());
	}

	private boolean isTargetTypeVisitor(IsTargetType rule) {
		Anchor anchor = this.getTargetAnchorFromContext(this.context);
		if (anchor == null) {
			System.out.println("failed isTargetType() anchor== null,  this.context: " + this.context.toString());
			return false;
		}
		ModelElement source = UIUtil.getModelElementForAnchor(anchor);
		return rule.getType().getLiteral().equals(source.getType().getLiteral());
	}
	
	private boolean isTargetConnectionTypeVisitor(IsTargetConnectionType rule) {
		Connection targetConnection = this.getTargetConnectionFromContext(this.context);
		if (targetConnection != null) {
			Object m = UIUtil.getBusinessObjectForPictogramElement(targetConnection);
			if (m instanceof Relation && ((Relation) m).getType().getLiteral().equals(rule.getType().getLiteral())) { 
				return true;
			}
		}
		return false;
	}

	private boolean containsTypeVisitor(ContainsType rule) {
		ContainerShape container = null;
		if (this.context instanceof AddContext) {
			container = ((AddContext) this.context).getTargetContainer();
		} else if (this.context instanceof CreateContext) {
			container = ((CreateContext) this.context).getTargetContainer();
		}
		if (container == null)
			return false;
		// Check if immediate container element contains another compartment type
		EObject o = UIUtil.getBusinessObjectForPictogramElement(container);
		if (!(o instanceof org.framed.iorm.model.Model))
			return false;
		org.framed.iorm.model.Model model = (org.framed.iorm.model.Model) o;
		System.out.println("containsCompartmentVisitor: size()=" + model.getElements().size());
		for (ModelElement e : model.getElements())
			if (e.getType().getLiteral().equals(rule.getType().getLiteral()))
				return true;
		return false;
	}

	private boolean sourceEqualsTargetVisitor(SourceEqualsTarget rule) {
		Anchor sourceAnchor = this.getSourceAnchorFromContext(this.context);
		ModelElement source = UIUtil.getModelElementForAnchor(sourceAnchor);

		Anchor targetAnchor = this.getTargetAnchorFromContext(this.context);
		ModelElement target = UIUtil.getModelElementForAnchor(targetAnchor);

		if (targetAnchor == null || sourceAnchor == null)
			return false;

		return source.hashCode() == target.hashCode();
	}

	private Anchor getSourceAnchorFromContext(Object context) {
		Anchor sourceAnchor = null;
		if (this.context instanceof AddConnectionContext) {
			AddConnectionContext ctx = (AddConnectionContext) this.context;
			sourceAnchor = ctx.getSourceAnchor();
		} else if (this.context instanceof CreateConnectionContext) {
			CreateConnectionContext ctx = (CreateConnectionContext) this.context;
			sourceAnchor = ctx.getSourceAnchor();
		}
		return sourceAnchor;
	}

	private Anchor getTargetAnchorFromContext(Object context) {
		Anchor anchor = null;
		if (this.context instanceof AddConnectionContext) {
			AddConnectionContext ctx = (AddConnectionContext) this.context;
			anchor = ctx.getTargetAnchor();
		} else if (this.context instanceof CreateConnectionContext) {
			CreateConnectionContext ctx = (CreateConnectionContext) this.context;
			anchor = ctx.getTargetAnchor();
		}
		return anchor;
	}
	

	private Connection getTargetConnectionFromContext(Object context) {
		Connection targetConnection = null;
		if (this.context instanceof AddContext) {
			AddContext addContext = (AddContext)this.context;
			targetConnection = addContext.getTargetConnection();
		} else if (this.context instanceof CreateContext) {
			CreateContext createContext = (CreateContext)this.context;
			targetConnection = createContext.getTargetConnection();
		}
		return targetConnection;
	}

	private boolean sourceEqualsTargetTypeVisitor(SourceEqualsTargetType rule) {
		// System.out.println("class is: " + this.context.getClass());
		try {
			Anchor sourceAnchor = this.getSourceAnchorFromContext(this.context);
			ModelElement source = UIUtil.getModelElementForAnchor(sourceAnchor);

			Anchor targetAnchor = this.getTargetAnchorFromContext(this.context);
			ModelElement target = UIUtil.getModelElementForAnchor(targetAnchor);

			System.out.println("sourceEqualsTargetTypeVisitor comparison: " + source.getType().getLiteral() + " == "
					+ target.getType().getLiteral());

			return target.getType().getLiteral().equals(source.getType().getLiteral());
		} catch (Exception e) {
			System.out.println("failed sourceEqualsTargetVisitor: " + e.getMessage());
		}

		return false;
	}

	private boolean orRuleVisitor(OrConstraintRule rule) {
		for (ConstraintRule abstractRule : rule.getRules()) {
			if (checkRule(abstractRule))
				return true;
		}
		return false;
	}

	private boolean notRuleVisitor(NotConstraintRule rule) {
		return !checkRule(rule.getRule());
	}
}
