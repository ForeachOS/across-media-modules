/*
 * Copyright 2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.webcms.web.thymeleaf;

import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateQueue;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.*;
import org.thymeleaf.processor.element.AbstractAttributeModelProcessor;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import org.thymeleaf.templatemode.TemplateMode;

import static com.foreach.across.modules.webcms.web.thymeleaf.ComponentAttributesProcessor.*;
import static com.foreach.across.modules.webcms.web.thymeleaf.WebCmsDialect.PREFIX;

/**
 * Renders the markup represented by the element to a placeholder variable instead of the direct output.
 */
final class PlaceholderAttributeProcessor extends AbstractAttributeModelProcessor
{
	PlaceholderAttributeProcessor() {
		super(
				TemplateMode.HTML,              // This processor will apply only to HTML mode
				PREFIX,                         // Prefix to be applied to name for matching
				null,                           // Tag name: match specifically this tag
				false,                          // Apply dialect prefix to tag name
				"placeholder",                  // No attribute name: will match by tag name
				true,                           // No prefix to be applied to attribute name
				99,
				false
		);
	}

	@Override
	protected void doProcess( ITemplateContext context,
	                          IModel model,
	                          AttributeName attributeName,
	                          String placeholderName,
	                          IElementModelStructureHandler structureHandler ) {
		if ( model.size() > 0 && model.get( 0 ) instanceof IProcessableElementTag ) {
			IProcessableElementTag elementTag = (IProcessableElementTag) model.get( 0 );
			IModelFactory modelFactory = context.getModelFactory();

			if ( placeholderName != null ) {
				enableDisabledComponentBlocks( model, modelFactory );

				boolean shouldRenderToPlaceholder = !elementTag.hasAttribute( ATTR_PARENT_CREATE_INCLUDE );

				removeProcessedAttributes( model, attributeName, modelFactory );

				if ( shouldRenderToPlaceholder ) {
					// only render to placeholder, if we are not to be included
					model.insert( 0, modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.START_PLACEHOLDER, placeholderName ) );
					model.add( modelFactory.createProcessingInstruction( PlaceholderTemplatePostProcessor.STOP_PLACEHOLDER, placeholderName ) );
				}
			}
		}
	}

	private void removeProcessedAttributes( IModel model, AttributeName attributeName, IModelFactory modelFactory ) {
		IProcessableElementTag elementTag = (IProcessableElementTag) model.get( 0 );
		IProcessableElementTag newFirstEvent = modelFactory.removeAttribute( elementTag, attributeName );
		newFirstEvent = modelFactory.removeAttribute( newFirstEvent, ATTR_PARENT_CREATE_INCLUDE );

		if ( newFirstEvent != elementTag ) {
			model.replace( 0, newFirstEvent );
		}
	}

	private void enableDisabledComponentBlocks( IModel model, IModelFactory modelFactory ) {
		for ( int i = 0; i < model.size(); i++ ) {
			ITemplateEvent event = model.get( i );
			if ( event instanceof IOpenElementTag || event instanceof IStandaloneElementTag ) {
				IProcessableElementTag original = (IProcessableElementTag) event;
				IProcessableElementTag openElementTag = original;
				// all components inside (or on) the placeholder should be rendered - even if the ComponentAttributeProcessor had put them hidden
				if ( openElementTag.hasAttribute( ATTR_NEVER_REPLACE ) ) {
					openElementTag = modelFactory.removeAttribute( openElementTag, ATTR_NEVER_REPLACE );
				}

				if ( openElementTag.hasAttribute( ATTR_COMPONENT ) ) {
					// if a wcm:component has container creation scope set, remove it
					if ( WebCmsComponentAutoCreateQueue.CONTAINER_MEMBER_SCOPE.equals( openElementTag.getAttributeValue( ATTR_SCOPE ) ) ) {
						openElementTag = modelFactory.removeAttribute( openElementTag, ATTR_SCOPE );
					}
					// if a wcm:component has no wcm:auto-create - ensure it is disabled (otherwise the container creation will trigger it)
					if ( !openElementTag.hasAttribute( ATTR_AUTO_CREATE ) ) {
						openElementTag = modelFactory.setAttribute( openElementTag, ATTR_AUTO_CREATE, SCOPE_PLACEHOLDER_CREATE,
						                                            AttributeValueQuotes.DOUBLE );
					}
					openElementTag = modelFactory.setAttribute( openElementTag, ATTR_PLACEHOLDER_AS_PARENT, "true", AttributeValueQuotes.DOUBLE );
				}

				if ( original != openElementTag ) {
					model.replace( i, openElementTag );
				}
			}
		}

	}
}