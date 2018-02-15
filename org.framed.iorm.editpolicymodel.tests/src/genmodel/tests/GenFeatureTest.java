/**
 */
package genmodel.tests;

import genmodel.GenmodelFactory;

import junit.textui.TestRunner;

import org.eclipse.emf.codegen.ecore.genmodel.GenFeature;

/**
 * <!-- begin-user-doc -->
 * A test case for the model object '<em><b>Gen Feature</b></em>'.
 * <!-- end-user-doc -->
 * @generated
 */
public class GenFeatureTest extends GenTypedElementTest {

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static void main(String[] args) {
		TestRunner.run(GenFeatureTest.class);
	}

	/**
	 * Constructs a new Gen Feature test case with the given name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GenFeatureTest(String name) {
		super(name);
	}

	/**
	 * Returns the fixture for this Gen Feature test case.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected GenFeature getFixture() {
		return (GenFeature)fixture;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#setUp()
	 * @generated
	 */
	@Override
	protected void setUp() throws Exception {
		setFixture(GenmodelFactory.eINSTANCE.createGenFeature());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see junit.framework.TestCase#tearDown()
	 * @generated
	 */
	@Override
	protected void tearDown() throws Exception {
		setFixture(null);
	}

} //GenFeatureTest
