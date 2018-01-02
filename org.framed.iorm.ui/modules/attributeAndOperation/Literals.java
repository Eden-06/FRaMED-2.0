package attributeAndOperation;

import org.eclipse.graphiti.util.IColorConstant;
import org.framed.iorm.ui.literals.UILiterals;

/**
 * This class saves literals in the scope of the attribute and operations feature module.
 * @author Kevin Kassin
 */
public class Literals {

	/**
	 * identifiers used for attributes and operations
	 * <p>
	 * can be for:<br>
	 * (1) the attribute text shape or<br>
	 * (2) the indicator dots if not all attributes fit in to the class or roles type body shape or<br>
	 * (3) the operation text shape or<br>
	 * (4) the indicator dots if not all operations fit in to the class or roles type body shape
	 */
	public final String SHAPE_ID_ATTRIBUTE_TEXT = "shape_att_text",
						SHAPE_ID_ATTRIBUTE_INDICATOR_DOTS = "shape_att_indicator_dots",
						SHAPE_ID_OPERATION_TEXT = "shape_op_text",
						SHAPE_ID_OPERATION_INDICATOR_DOTS = "shape_op_indicator_dots";
	
	//usedInReferences
	//~~~~~~~~~~~~~~~~
	public final String TYPE_USED_IN_REFERENCES = "attributeAndOperation.references.AbstractUsedInReference";
	
	//Common pattern for attributes and operations
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * the features name to identify the common pattern
	 */
	public final String COM_FEATURE_NAME = "AttributeOperationCommonPattern";
	
	/**
	 * the icon path and id
	 * <p>
	 * These are empty because there is no icon to show for the common pattern.
	 */		
	public final String	COM_ICON_IMG_ID = "",
						COM_ICON_IMG_PATH = "";
	
	//Attributes
	//~~~~~~~~~~
	/**
	 * the features name used for the palette entry of the attribute feature
	 */
	public final String ATT_FEATURE_NAME = "Attribute";
	
	/**
	 * the standart name for a pictogramm element and business object created by the attribute feature
	 */
	public final String ATT_STANDARD_NAME = "attribute:type";
						
	/**
	 * the id or the path for icon used for palette entry of the attribute feature
	 */		
	public final String	ATT_ICON_IMG_ID = "org.framed.iorm.ui.modules.attributes",
						ATT_ICON_IMG_PATH = "modules/attributeAndOperation/icon_attribute.gif";
	
	/**
	 * regular expression for identifiers:
	 * <p>
	 * string of letters and digits, first is no digit (for names)
	 */
	private final String identifier = "[a-zA-Z_$][a-zA-Z\\d_$]*"; 
	
	/**
	 * regular expression for qualified identifiers
	 * <p>
	 * full classes: (N.)*N (for types)
	 */
	private final String qualifiedIdentifier = "(" + identifier + "\\.)*" + identifier; 
	
	/**
	 * regular expression for attributes
	 * <p>
	 * name:type
	 */
	public final String ATT_REG_EXP_NAME = identifier + ":" + qualifiedIdentifier; 
	
	//Operations
	//~~~~~~~~~~
	/**
	 * the features name used for the palette entry of the attribute feature
	 */
	public final String OPS_FEATURE_NAME = "Operation";
	
	/**
	 * the standart name for a pictogramm element and business object created by the attribute feature
	 */
	public final String OPS_STANDARD_NAME = "operation():type";
						
	/**
	 * the id or the path for icon used for palette entry of the attribute feature
	 */		
	public final String	OPS_ICON_IMG_ID = "org.framed.iorm.ui.modules.operation",
						OPS_ICON_IMG_PATH = "modules/attributeAndOperation/icon_operation.gif";
	
	/**
	 * regular expression for operations
	 * <p>
	 * name"("[Parameter(","Parameter)*]"):"type, return type is optional
	 */
	public final String OPS_REG_EXP_NAME = identifier + "\\((" + ATT_REG_EXP_NAME + "(," + ATT_REG_EXP_NAME + ")*)?\\)(:" + qualifiedIdentifier + ")?";
	
	/**
	 * messages used as direct editing tips
	 */
	public final String DIRECTEDITING_ATTRIBUTES = "An attributes name has the form <name>:<type>.",
						NAME_ALREADY_USED_ATTRIBUTES = "Another attribute already has the same name!",
						DIRECTEDITING_OPERATIONS = "An operations name has the form <name>(<parameters>):<type>.",
						NAME_ALREADY_USED_OPERATIONS = "Another operation already has the same name!";
	
	//Layout
	//~~~~~~
	/**
	 * layout integers used to add attributes and operations at the right position
	 */
	public final int HEIGHT_NAME_SHAPE = UILiterals.HEIGHT_NAME_SHAPE,
			         PUFFER_BETWEEN_ELEMENTS = UILiterals.PUFFER_BETWEEN_ELEMENTS,
			         HEIGHT_ATTRIBUTE_SHAPE = UILiterals.HEIGHT_ATTRITBUTE_SHAPE,
			         HEIGHT_OPERATION_SHAPE = UILiterals.HEIGHT_OPERATION_SHAPE;
	
	/**
	 * the color of text
	 */
	public final IColorConstant COLOR_TEXT = UILiterals.COLOR_TEXT;
	
	//Names
	//~~~~~
	/**
	 * the limit of the suffix for standard names
	 * <p>
	 * This means that if the limit is 10 the following standard names are used:<br>
	 * <em>standardName</em><br>
	 * <em>standardName1</em><br>
	 * <em>...</em><br>
	 * <em>standardName10</em><br>
	 */
	public final int STANDARD_NAMES_COUNTER_LIMIT = 10;
}
