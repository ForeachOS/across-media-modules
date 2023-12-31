package com.foreach.imageserver.core;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.test.AbstractAcrossModuleConventionsTest;

/**
 * @author Arne Vandamme
 */
public class TestImageServerCoreModuleConventions extends AbstractAcrossModuleConventionsTest
{
	@Override
	protected AcrossModule createModule() {
		return new ImageServerCoreModule();
	}
}
