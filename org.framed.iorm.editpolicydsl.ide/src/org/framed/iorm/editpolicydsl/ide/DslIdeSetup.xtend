/*
 * generated by Xtext 2.13.0
 */
package org.framed.iorm.editpolicydsl.ide

import com.google.inject.Guice
import org.eclipse.xtext.util.Modules2
import org.framed.iorm.editpolicydsl.DslRuntimeModule
import org.framed.iorm.editpolicydsl.DslStandaloneSetup

/**
 * Initialization support for running Xtext languages as language servers.
 */
class DslIdeSetup extends DslStandaloneSetup {

	override createInjector() {
		Guice.createInjector(Modules2.mixin(new DslRuntimeModule, new DslIdeModule))
	}
	
}